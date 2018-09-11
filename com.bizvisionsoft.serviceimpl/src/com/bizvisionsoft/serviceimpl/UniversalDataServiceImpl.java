package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
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

public class UniversalDataServiceImpl extends BasicServiceImpl implements UniversalDataService {

	@Override
	public UniversalResult list(UniversalCommand command) {
		Map<?, ?> mCondition = (Map<?, ?>) command.getParameter("condition");
		Integer skip = (Integer) mCondition.get("skip");
		Integer limit = (Integer) mCondition.get("limit");
		Map<?, ?> mFilter = (Map<?, ?>) mCondition.get("filter");
		Map<?, ?> mSort = (Map<?, ?>) mCondition.get("sort");
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		if (mFilter != null) {
			BasicDBObject filter = new BasicDBObject();
			filter.putAll(mFilter);
			pipeline.add(Aggregates.match(filter));
		}
		if (mSort != null) {
			BasicDBObject sort = new BasicDBObject();
			sort.putAll(mSort);
			pipeline.add(Aggregates.sort(sort));
		}
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
		try {
			Class<?> clazz = ServicesLoader.getBundleContext().getBundle().loadClass(className);
			String col = clazz.getAnnotation(PersistenceCollection.class).value();
			return c(col);
		} catch (ClassNotFoundException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public UniversalResult conut(UniversalCommand command) {
		String className = command.getTargetClassName();
		Map<?, ?> mFilter = (Map<?, ?>) command.getParameter("filter");
		BasicDBObject filter = new BasicDBObject();
		if (mFilter != null) {
			filter.putAll(mFilter);
		}
		long result = col(className).countDocuments(filter);
		UniversalResult uResult = new UniversalResult();
		uResult.setResult("" + result);
		uResult.setTargetClassName(Long.class.getName());
		uResult.setList(true);
		return uResult;
	}

	@Override
	public UniversalResult insert(UniversalCommand command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UniversalResult delete(UniversalCommand command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UniversalResult update(UniversalCommand command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UniversalResult get(UniversalCommand command) {
		// TODO Auto-generated method stub
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
