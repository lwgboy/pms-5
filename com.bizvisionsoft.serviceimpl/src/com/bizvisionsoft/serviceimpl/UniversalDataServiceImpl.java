package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.mongocodex.codec.Codex;
import com.bizvisionsoft.mongocodex.codec.CodexProvider;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UniversalDataService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.provider.BasicDBObjectAdapter;
import com.bizvisionsoft.service.provider.DateAdapter;
import com.bizvisionsoft.service.provider.DocumentAdapter;
import com.bizvisionsoft.service.provider.ObjectIdAdapter;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.JSTools;
import com.bizvisionsoft.service.tools.ServiceHelper;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class UniversalDataServiceImpl extends BasicServiceImpl implements UniversalDataService {

	@Override
	public UniversalResult list(UniversalCommand command, String domain) {
		List<Bson> pipeline;

		String qName = command.getQueryJQ();
		String qPipeline = command.getQueryPipeline();
		if (qName == null && qPipeline == null) {
			pipeline = new ArrayList<>();
		} else {
			if (qName != null) {
				JQ jq = Domain.getJQ(domain, qName);
				List<String> parameters = Formatter.listParameter(jq.getJS());
				setQueryParameters(command, parameters, jq, domain);
				pipeline = jq.array();
			} else {
				List<String> parameters = Formatter.listParameter(qPipeline);
				JQ jq = new JQ();
				setQueryParameters(command, parameters, jq, domain);
				pipeline = jq.array(qPipeline);
			}
		}

		Integer skip = command.getParameter("condition.skip", Integer.class);
		Integer limit = command.getParameter("condition.limit", Integer.class);
		Document filter = command.getParameter("condition.filter", Document.class);
		Document sort = command.getParameter("condition.sort", Document.class);

		if (filter != null && !filter.isEmpty())
			pipeline.add(Aggregates.match(filter));
		if (sort != null && !sort.isEmpty())
			pipeline.add(Aggregates.sort(sort));
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		String className = command.getTargetClassName();
		String bundleName = command.getTargetBundleName();
		ArrayList<Document> result = col(command, domain).aggregate(pipeline).into(new ArrayList<>());
		return listResult(className, bundleName, getGson().toJson(result), domain);
	}

	private void setQueryParameters(UniversalCommand command, List<String> parameters, JQ jq, String domain) {
		// 运行前处理
		String js = command.getQueryPreProcess();
		if (Check.isAssigned(js)) {
			Map<String, Object> cmdParams = command.getParameters();
			Map<String, Object> params = new HashMap<String, Object>();
			Document binding = new Document("cmdParams", cmdParams).append("ServiceHelper", new ServiceHelper(domain)).append("params",
					params);
			JSTools.invoke(js, null, "params", binding);
			params.forEach(jq::set);
		} else {
			// 直接设置参数
			parameters.forEach(p -> {
				Object value = command.getParameter(p);
				jq.set(p, value);
			});
		}
	}

	private MongoCollection<Document> col(UniversalCommand command, String domain) {
		String colName = command.getQueryCollection();
		if (colName != null)
			return c(colName, domain);

		colName = command.getTargetCollection();
		if (colName != null)
			return c(colName, domain);

		String className = command.getTargetClassName();
		String bundleName = command.getTargetBundleName();
		Class<?> clazz = ServicesLoader.getClass(className, bundleName);
		if (clazz == null)
			throw new ServiceException("无法加载类" + className);
		String col = clazz.getAnnotation(PersistenceCollection.class).value();
		return c(col, domain);
	}

	@Override
	public UniversalResult count(UniversalCommand command, String domain) {
		MongoCollection<Document> col = col(command, domain);
		long result = Optional.ofNullable(command.getParameter("filter", Document.class)).map(f -> {
			return col.countDocuments(f);
		}).orElse(col(command, domain).countDocuments());
		return countResult(result);
	}

	@Override
	public UniversalResult insert(UniversalCommand command, String domain) {
		Document document = command.ignoreNull(true).getParameter("object", Document.class);
		String className = command.getTargetClassName();
		String bundleName = command.getTargetBundleName();
		String colName = command.getTargetCollection();
		if (colName != null) {
			return insertDocument(document, colName, className, bundleName, domain);
		} else {
			return insertObject(document, className, bundleName, domain);
		}
	}

	private UniversalResult insertDocument(Document document, String colName, String className, String bundleName, String domain) {
		// 去掉null
		c(colName, domain).insertOne(document);
		return elementResult(className, bundleName, document.toJson(), domain);
	}

	@SuppressWarnings("unchecked")
	private <T> UniversalResult insertObject(Document document, String className, String bundleName, String domain) {
		Class<T> clazz = (Class<T>) ServicesLoader.getClass(className, bundleName);
		if (clazz == null)
			throw new ServiceException("无法加载类" + className);
		Codec<T> codec = CodexProvider.getRegistry().get(clazz);
		if (codec instanceof Codex) {
			Codex<T> codex = (Codex<T>) codec;
			try {
				T obj = clazz.newInstance();
				codex.decode(obj, new JsonReader(document.toJson()), DecoderContext.builder().build());
				c(clazz, domain).insertOne(obj);
				return elementResult(className, bundleName, getGson().toJson(obj), domain);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error(e.getMessage(), e);
				throw new ServiceException(e.getMessage());
			}
		} else {
			String msg = "无法获得类解码器：" + className;
			logger.error(msg);
			throw new ServiceException(msg);
		}

	}

	@Override
	public UniversalResult delete(UniversalCommand command, String domain) {
		String sid = command.getParameter("_id.$oid", String.class);
		if (sid != null) {
			ObjectId _id = new ObjectId(sid);
			DeleteResult r = col(command, domain).deleteOne(new Document("_id", _id));
			return countResult(r.getDeletedCount());
		}
		String msg = "缺少唯一关键字";
		logger.error(msg);
		throw new ServiceException(msg);
	}

	@Override
	public UniversalResult update(UniversalCommand command, String domain) {
		Document filter = command.getParameter("filter_and_update.filter", Document.class);
		Document update = command.getParameter("filter_and_update.update", Document.class);
		UpdateResult r = col(command, domain).updateMany(filter, update);
		long modifiedCount = r.getModifiedCount();
		return countResult(modifiedCount);
	}

	@Override
	public UniversalResult get(UniversalCommand command, String domain) {
		String sid = command.getParameter("_id.$oid", String.class);
		if (sid != null) {
			Document document = col(command, domain).find(new Document("_id", new ObjectId(sid))).first();
			return elementResult(command.getTargetClassName(), command.getTargetBundleName(), getGson().toJson(document), domain);
		}
		String msg = "缺少唯一关键字";
		logger.error(msg);
		throw new ServiceException(msg);
	}

	public Gson getGson() {
		return new GsonBuilder()//
				.serializeNulls().registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())//
				.registerTypeAdapter(Date.class, new DateAdapter())//
				.registerTypeAdapter(BasicDBObject.class, new BasicDBObjectAdapter())//
				.registerTypeAdapter(Document.class, new DocumentAdapter())//
				.create();
	}

	private UniversalResult elementResult(String className, String bundleName, String json, String domain) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(json);
		uResult.setTargetType(bundleName, className, domain);
		uResult.setList(false);
		return uResult;
	}

	private UniversalResult listResult(String className, String bundleName, String json, String domain) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(json);
		uResult.setTargetType(bundleName, className, domain);
		uResult.setList(true);
		return uResult;
	}

	private UniversalResult countResult(long result) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult("" + result);
		uResult.setTargetType(null, Long.class.getName(), null);
		return uResult;
	}

}
