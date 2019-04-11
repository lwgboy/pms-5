package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
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
import com.bizvisionsoft.service.provider.BasicDBObjectAdapter;
import com.bizvisionsoft.service.provider.DateAdapter;
import com.bizvisionsoft.service.provider.DocumentAdapter;
import com.bizvisionsoft.service.provider.ObjectIdAdapter;
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
	public UniversalResult list(UniversalCommand command) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

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
		ArrayList<Document> result = col(command).aggregate(pipeline).into(new ArrayList<>());
		return listResult(className, bundleName, getGson().toJson(result));
	}

	private MongoCollection<Document> col(UniversalCommand command) {
		String colName = command.getTargetCollection();
		if (colName != null)
			return c(colName);

		String className = command.getTargetClassName();
		String bundleName = command.getTargetBundleName();
		Class<?> clazz = ServicesLoader.getClass(className, bundleName);
		if (clazz == null)
			throw new ServiceException("无法加载类"+className);
		String col = clazz.getAnnotation(PersistenceCollection.class).value();
		return c(col);
	}

	@Override
	public UniversalResult count(UniversalCommand command) {
		MongoCollection<Document> col = col(command);
		long result = Optional.ofNullable(command.getParameter("filter", Document.class)).map(f -> {
			return col.countDocuments(f);
		}).orElse(col(command).countDocuments());
		return countResult(result);
	}

	@Override
	public UniversalResult insert(UniversalCommand command) {
		Document document = command.ignoreNull(true).getParameter("object", Document.class);
		String className = command.getTargetClassName();
		String bundleName = command.getTargetBundleName();
		String colName = command.getTargetCollection();
		if (colName != null) {
			return insertDocument(document, colName, className, bundleName);
		} else {
			return insertObject(document, className, bundleName);
		}
	}

	private UniversalResult insertDocument(Document document, String colName, String className, String bundleName) {
		// 去掉null
		c(colName).insertOne(document);
		return elementResult(className, bundleName, document.toJson());
	}

	@SuppressWarnings("unchecked")
	private <T> UniversalResult insertObject(Document document, String className, String bundleName) {
		Class<T> clazz = (Class<T>) ServicesLoader.getClass(className, bundleName);
		if (clazz == null)
			throw new ServiceException("无法加载类"+className);
		Codec<T> codec = CodexProvider.getRegistry().get(clazz);
		if (codec instanceof Codex) {
			Codex<T> codex = (Codex<T>) codec;
			try {
				T obj = clazz.newInstance();
				codex.decode(obj, new JsonReader(document.toJson()), DecoderContext.builder().build());
				c(clazz).insertOne(obj);
				return elementResult(className, bundleName, getGson().toJson(obj));
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
	public UniversalResult delete(UniversalCommand command) {
		String sid = command.getParameter("_id.$oid", String.class);
		if (sid != null) {
			ObjectId _id = new ObjectId(sid);
			DeleteResult r = col(command).deleteOne(new Document("_id", _id));
			return countResult(r.getDeletedCount());
		}
		String msg = "缺少唯一关键字";
		logger.error(msg);
		throw new ServiceException(msg);
	}

	@Override
	public UniversalResult update(UniversalCommand command) {
		Document filter = command.getParameter("filter_and_update.filter", Document.class);
		Document update = command.getParameter("filter_and_update.update", Document.class);
		UpdateResult r = col(command).updateMany(filter, update);
		long modifiedCount = r.getModifiedCount();
		return countResult(modifiedCount);
	}

	@Override
	public UniversalResult get(UniversalCommand command) {
		String sid = command.getParameter("_id.$oid", String.class);
		if (sid != null) {
			Document document = col(command).find(new Document("_id", new ObjectId(sid))).first();
			return elementResult(command.getTargetClassName(), command.getTargetBundleName(), getGson().toJson(document));
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

	private UniversalResult elementResult(String className, String bundleName, String json) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(json);
		uResult.setTargetClassName(className);
		uResult.setTargetBundleName(bundleName);
		uResult.setList(false);
		return uResult;
	}

	private UniversalResult listResult(String className, String bundleName, String json) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(json);
		uResult.setTargetClassName(className);
		uResult.setTargetBundleName(bundleName);
		uResult.setList(true);
		return uResult;
	}

	private UniversalResult countResult(long result) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult("" + result);
		uResult.setTargetClassName(Long.class.getName());
		return uResult;
	}

}
