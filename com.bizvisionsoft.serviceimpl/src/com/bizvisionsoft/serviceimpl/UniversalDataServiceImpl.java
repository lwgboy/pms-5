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
		ArrayList<Document> result = col(className).aggregate(pipeline).into(new ArrayList<>());
		UniversalResult uResult = new UniversalResult();
		uResult.setResult(getGson().toJson(result));
		uResult.setTargetClassName(className);
		uResult.setList(true);
		return uResult;
	}

	private MongoCollection<Document> col(String className) {
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
		String className = command.getTargetClassName();
		long result = Optional.ofNullable(command.getParameter("filter", Document.class))
				.map(f -> col(className).countDocuments(f)).orElse(col(className).countDocuments());
		UniversalResult uResult = new UniversalResult();
		uResult.setResult("" + result);
		uResult.setTargetClassName(Long.class.getName());
		return uResult;
	}

	@Override
	public UniversalResult insert(UniversalCommand command) {
		Document document = command.getParameter("object", Document.class);
		String className = command.getTargetClassName();
		return insert(document, className);
	}

	@SuppressWarnings("unchecked")
	private <T> UniversalResult insert(Document document, String className) {
		Class<T> clazz = (Class<T>) getClass(className);
		Codec<T> codec = CodexProvider.getRegistry().get(clazz);
		if (codec instanceof Codex) {
			Codex<T> codex = (Codex<T>) codec;
			try {
				T obj = clazz.newInstance();
				codex.decode(obj, new JsonReader(document.toJson()), DecoderContext.builder().build());
				c(clazz).insertOne(obj);
				UniversalResult uResult = new UniversalResult();
				uResult.setResult(getGson().toJson(obj));
				uResult.setTargetClassName(className);
				uResult.setList(false);
				return uResult;
			} catch (InstantiationException e) {
				logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	public UniversalResult delete(UniversalCommand command) {
		String sid = command.getParameter("_id.$oid", String.class);
		if (sid != null) {
			ObjectId _id = new ObjectId(sid);
			String className = command.getTargetClassName();
			DeleteResult r = col(className).deleteOne(new Document("_id", _id));
			UniversalResult uResult = new UniversalResult();
			uResult.setResult("" + r.getDeletedCount());
			uResult.setTargetClassName(Long.class.getName());
			uResult.setList(false);
			return uResult;
		}
		return null;
	}

	@Override
	public UniversalResult update(UniversalCommand command) {
		Document filter = command.getParameter("filter_and_update.filter", Document.class);
		Document update = command.getParameter("filter_and_update.update", Document.class);
		String className = command.getTargetClassName();
		UpdateResult r = col(className).updateMany(filter, update);
		UniversalResult uResult = new UniversalResult();
		uResult.setResult("" + r.getModifiedCount());
		uResult.setTargetClassName(Long.class.getName());
		return uResult;
	}

	@Override
	public UniversalResult get(UniversalCommand command) {
		String sid = command.getParameter("_id.$oid", String.class);
		if (sid != null) {
			ObjectId _id = new ObjectId(sid);
			String className = command.getTargetClassName();
			Document document = col(className).find(new Document("_id", _id)).first();
			UniversalResult uResult = new UniversalResult();
			uResult.setResult(getGson().toJson(document));
			uResult.setTargetClassName(className);
			uResult.setList(false);
			return uResult;
		}
		return null;
	}

	public Gson getGson() {
		return new GsonBuilder()//
				.serializeNulls().registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())//
				.registerTypeAdapter(Date.class, new DateAdapter())//
				.registerTypeAdapter(BasicDBObject.class, new BasicDBObjectAdapter())//
				.registerTypeAdapter(Document.class, new DocumentAdapter())//
				.create();
	}

}
