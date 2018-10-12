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
		ArrayList<Document> result = col(command).aggregate(pipeline).into(new ArrayList<>());
		return listResult(className, getGson().toJson(result));
	}

	private MongoCollection<Document> col(UniversalCommand command) {
		String colName = command.getTargetCollection();
		if (colName != null)
			return c(colName);

		String className = command.getTargetClassName();
		Class<?> clazz = getClass(className);
		String col = clazz.getAnnotation(PersistenceCollection.class).value();
		return c(col);
	}

	private Class<?> getClass(String className) {
		try {
			return ServicesLoader.getBundleContext().getBundle().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new ServiceException(e.getMessage());
		}
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
		String colName = command.getTargetCollection();
		if (colName != null) {
			return insertDocument(document,colName,className);
		} else {
			return insertObject(document, className);
		}
	}

	private UniversalResult insertDocument(Document document, String colName, String className) {
		//去掉null
		c(colName).insertOne(document);
		return elementResult(className, document.toJson());
	}

	@SuppressWarnings("unchecked")
	private <T> UniversalResult insertObject(Document document, String className) {
		Class<T> clazz = (Class<T>) getClass(className);
		Codec<T> codec = CodexProvider.getRegistry().get(clazz);
		if (codec instanceof Codex) {
			Codex<T> codex = (Codex<T>) codec;
			try {
				T obj = clazz.newInstance();
				codex.decode(obj, new JsonReader(document.toJson()), DecoderContext.builder().build());
				c(clazz).insertOne(obj);
				return elementResult(className, getGson().toJson(obj));
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error(e.getMessage(), e);
				throw new ServiceException(e.getMessage());
			}
		}else {
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
			ObjectId _id = new ObjectId(sid);
			Document document = col(command).find(new Document("_id", _id)).first();
			UniversalResult uResult = new UniversalResult();
			uResult.setResult(getGson().toJson(document));
			uResult.setTargetClassName(command.getTargetClassName());
			uResult.setList(false);
			return uResult;
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

	private UniversalResult elementResult(String className, String json) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(json);
		uResult.setTargetClassName(className);
		uResult.setList(false);
		return uResult;
	}

	private UniversalResult listResult(String className, String json) {
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(json);
		uResult.setTargetClassName(className);
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
