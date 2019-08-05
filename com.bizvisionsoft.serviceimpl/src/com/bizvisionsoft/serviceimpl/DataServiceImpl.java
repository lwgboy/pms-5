package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.DataService;
import com.bizvisionsoft.service.QueryCommand;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.model.ValueRule;
import com.bizvisionsoft.service.provider.BsonProvider;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.valuegen.DocumentValueGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

public class DataServiceImpl extends MongoDBService implements DataService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/////////////////////////////////////////////////////////////////////////////////////
	// 删除
	final protected <T> long delete(ObjectId _id, Class<T> clazz, String domain) {
		return deleteOne(c(clazz, domain), new BasicDBObject("_id", _id)).getDeletedCount();
	}

	final protected <T> long delete(ObjectId _id, String cname, String domain) {
		return deleteOne(c(cname, domain), new BasicDBObject("_id", _id)).getDeletedCount();
	}

	final protected <T> long deleteMany(Bson filter, String cname, String domain) {
		return deleteMany(c(cname, domain), filter);
	}

	final protected Document findAndDeleteOne(ObjectId _id, String cname, String domain) {
		return findAndDeleteOne(c(cname, domain), new Document("_id", _id));
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// 更新
	/**
	 * 更新带插入
	 * 
	 * @param filter
	 * @param update
	 * @param col
	 * @param domain
	 * @return
	 */
	final protected Document upsert(Document filter, Document update, String col, String domain) {
		UpdateOptions option = new UpdateOptions().upsert(true);
		UpdateResult ur = c(col, domain).updateOne(filter, update, option);
		return new Document().append("matched", ur.getMatchedCount()).append("modified", ur.getModifiedCount()).append("_id",
				ur.getUpsertedId());
	}

	final protected Document update(Document update, String col, String domain) {
		Document filter = new Document("_id", update.get("_id"));
		update.remove("_id");
		return findAndUpdate(c(col, domain), filter, update);
	}

	final protected <T> long update(BasicDBObject fu, Class<T> clazz, String domain) {
		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		BasicDBObject update = (BasicDBObject) fu.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		return update(c(clazz, domain), filter, update, option);
	}

	final protected long update(BasicDBObject fu, String cname, String domain) {
		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		BasicDBObject update = (BasicDBObject) fu.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		return update(c(cname, domain), filter, update, option);
	}

	final protected Document updateThen(Document d, String lang, String col, String domain, BiFunction<Document, String, Document> func) {
		Document doc = update(d, col, domain);
		return Optional.ofNullable(func).map(f -> f.apply(doc, lang)).orElse(doc);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// 更新
	@SuppressWarnings("unchecked")
	final protected <T> T insert(T obj, String domain) {
		return insert(obj, (Class<T>) obj.getClass(), domain);
	}

	final protected <T> T insert(T obj, Class<T> clazz, String domain) {
		return insert(c(clazz, domain), obj);
	}

	final protected <T> T insert(T obj, String cname, Class<T> clazz, String domain) {
		return insert(c(cname, clazz, domain), obj);
	}

	final protected Document insert(Document doc, String colName, String domain) {
		return insert(c(colName, domain), doc);
	}

	private <T> T insert(MongoCollection<T> col, T obj) {
		try {
			if (obj instanceof Document) {// 如果是Document, 需要手工实现
				MongoNamespace ns = col.getNamespace();
				String dbName = ns.getDatabaseName();
				String colName = ns.getCollectionName();
				generateValue(dbName, colName, (Document) obj);
			}
			return super.insertOne(col, obj);
		} catch (Exception e) {
			throw handleMongoException(e, obj.toString());
		}
	}

	private void generateValue(String domain, String col, Document doc) {
		c(ValueRule.class, domain).find(new Document("colName", col).append("enable", true)).forEach((ValueRule vr) -> {
			try {
				DocumentValueGenerator.generate(vr, doc);
			} catch (Exception e) {
				logger.error("值生成规则运行出错。" + vr, e);
			}
		});
	}


	/////////////////////////////////////////////////////////////////////////////////////
	// 查询
	final protected <T> T get(ObjectId _id, Class<T> clazz, String domain) {
		return find(c(clazz, domain), new BasicDBObject("_id", _id)).first();
	}

	final protected Document getDocument(ObjectId _id, String col, String domain) {
		return find(c(col, domain), new BasicDBObject("_id", _id)).first();
	}

	final protected <T> long count(BasicDBObject filter, Class<T> clazz, String domain) {
		return count(c(clazz, domain), filter);
	}

	final protected long count(BasicDBObject filter, String colName, String domain) {
		return count(c(colName, domain), filter);
	}

	final protected <T> List<T> list(Class<T> clazz, String domain, BasicDBObject condition, Bson... appendPipelines) {
		List<Bson> pipeline = null;
		if (appendPipelines != null)
			pipeline = Arrays.asList(appendPipelines);
		return list(clazz, domain, condition, pipeline);
	}

	final protected List<Document> list(String col, String domain, BasicDBObject condition, Bson... appendPipelines) {
		List<Bson> pipeline = null;
		if (appendPipelines != null)
			pipeline = Arrays.asList(appendPipelines);
		return list(col, domain, condition, pipeline);
	}

	/**
	 * 增加传入pipeline的list类型的实现
	 * 
	 * @param clazz
	 * @param condition
	 * @param appendPipelines
	 * @param domain
	 * @return
	 */
	final protected <T> List<T> list(Class<T> clazz, String domain, BasicDBObject condition, List<Bson> appendPipelines) {
		ArrayList<Bson> pipeline = combinateQueryPipeline(condition, appendPipelines);
		return c(clazz, domain).aggregate(pipeline).into(new ArrayList<T>());
	}

	final protected List<Document> list(String col, String domain, BasicDBObject condition, List<Bson> appendPipelines) {
		ArrayList<Bson> pipeline = combinateQueryPipeline(condition, appendPipelines);
		return c(col, domain).aggregate(pipeline).into(new ArrayList<>());
	}

	final protected long count(String col, String domain, BasicDBObject filter, Bson... prefixPipeline) {
		List<Bson> pipeline = null;
		if (prefixPipeline != null)
			pipeline = Arrays.asList(prefixPipeline);
		return count(col, domain, filter, pipeline);
	}

	final protected long count(String col, String domain, BasicDBObject filter, List<Bson> prefixPipelines) {
		ArrayList<Bson> pipeline = combinateCountPipeline(prefixPipelines, filter);
		return Optional.ofNullable(c(col, domain).aggregate(pipeline).first()).map(d -> (Number) d.get("count")).map(d -> d.longValue())
				.orElse(0l);
	}

	final protected List<Bson> appendConditionToPipeline(List<Bson> pipeline, BasicDBObject condition) {
		Optional.ofNullable((BasicDBObject) condition.get("filter")).map(Aggregates::match).ifPresent(pipeline::add);
		Optional.ofNullable((BasicDBObject) condition.get("sort")).map(Aggregates::sort).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("skip")).map(Aggregates::skip).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("limit")).map(Aggregates::limit).ifPresent(pipeline::add);
		return pipeline;
	}

	final protected ArrayList<Bson> combinateQueryPipeline(BasicDBObject condition, List<Bson> appendPipelines) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		appendConditionToPipeline(pipeline, condition);
		if (appendPipelines != null)
			pipeline.addAll(appendPipelines);
		return pipeline;
	}

	final protected ArrayList<Bson> combinateCountPipeline(List<Bson> prefixPipeline, BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		if (prefixPipeline != null)
			pipeline.addAll(prefixPipeline);
		if (filter != null)
			pipeline.add(Aggregates.match(filter));
		pipeline.add(Aggregates.count());
		return pipeline;
	}

	final protected <T> List<T> createDataSet(BasicDBObject condition, Class<T> clazz, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(skip, limit, filter, sort, clazz, domain);
	}

	final protected <T> List<T> query(Integer skip, Integer limit, BasicDBObject filter, Class<T> clazz, String domain) {
		return query(skip, limit, filter, null, clazz, domain);
	}

	final protected <T> List<T> query(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, Class<T> clazz,
			String domain) {
		return query(null, skip, limit, filter, sort, null, clazz, domain);
	}

	final protected <T> List<T> query(Consumer<List<Bson>> input, Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort,
			Consumer<List<Bson>> output, Class<T> clazz, String domain) {

		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		Optional.ofNullable(input).ifPresent(c -> c.accept(pipeline));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		Optional.ofNullable(output).ifPresent(c -> c.accept(pipeline));

		debugPipeline(pipeline);

		return c(clazz, domain).aggregate(pipeline).into(new ArrayList<T>());
	}

	final protected void appendLookupAndUnwind(List<Bson> pipeline, String from, String field, String newField) {
		appendLookupAndUnwind(pipeline, from, field, newField, true);
	}

	final protected void appendLookupAndUnwind(List<Bson> pipeline, String from, String field, String newField,
			boolean preserveNullAndEmptyArrays) {
		pipeline.add(new Document("$lookup",
				new Document("from", from).append("localField", field).append("foreignField", "_id").append("as", newField)));

		pipeline.add(new Document("$unwind",
				new Document("path", "$" + newField).append("preserveNullAndEmptyArrays", preserveNullAndEmptyArrays)));
	}

	final protected void appendSortBy(List<Bson> pipeline, String fieldName, int i) {
		pipeline.add(Aggregates.sort(new BasicDBObject(fieldName, i)));
	}

	/**
	 * 使用lookupDesentItems替代
	 * 
	 * @param inputIds
	 *            输入的_id列表
	 * @param cName
	 *            集合名称
	 * @param key
	 *            关键字
	 * @return
	 */
	final protected List<ObjectId> getDesentItems(List<ObjectId> inputIds, String cName, String key, String domain) {
		List<ObjectId> result = new ArrayList<ObjectId>();
		lookupDesentItems(inputIds, cName, domain, key, true).forEach(d -> {
			result.add(d.getObjectId("_id"));
		});
		return result;
	}

	/**
	 * 
	 * @param inputIds
	 *            输入的_id列表
	 * @param cName
	 *            集合名称
	 * @param key
	 *            关键字
	 * @param includeCurrentLevel
	 *            是否包含本级
	 * @return
	 */
	final protected Iterable<Document> lookupDesentItems(List<ObjectId> inputIds, String cName, String domain, String key,
			boolean includeCurrentLevel) {
		String jq = includeCurrentLevel ? "查询-通用-下级迭代取出-含本级" : "查询-通用-下级迭代取出";
		ArrayList<Bson> pipe = new ArrayList<>();
		if (inputIds.size() > 1) {
			pipe.add(Aggregates.match(new Document("_id", new Document("$in", inputIds))));
		} else if (inputIds.size() == 1) {
			pipe.add(Aggregates.match(new Document("_id", inputIds.get(0))));
		} else {
			return new ArrayList<Document>();
		}
		pipe.addAll(Domain.getJQ(domain, jq).set("from", cName).set("startWith", "$_id").set("connectFromField", "_id")
				.set("connectToField", key).array());
		return c(cName, domain).aggregate(pipe);
	}

	final public int generateCode(String name, String key, String domain) {
		Document doc = c(name, domain).findOneAndUpdate(Filters.eq("_id", key), Updates.inc("next_val", 1),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
		return doc.getInteger("next_val");
	}

	final protected int generateCode(String name, String key) {
		return generateCode(name, key, null);
	}

	final protected String getName(String cName, ObjectId _id, String domain) {
		return c(cName, domain).distinct("name", new BasicDBObject("_id", _id), String.class).first();
	}

	final protected String getString(String cName, String fName, ObjectId _id, String domain) {
		return c(cName, domain).distinct(fName, new BasicDBObject("_id", _id), String.class).first();
	}

	final protected <T> T getValue(String cName, String fName, Object _id, Class<T> c, String domain) {
		return c(cName, domain).distinct(fName, new BasicDBObject("_id", _id), c).first();
	}

	final protected void debugPipeline(List<? extends Bson> pipeline) {
		if (logger.isDebugEnabled()) {
			String json = new BsonProvider<>().getGson().toJson(pipeline);
			logger.debug("Aggregation Pipeline: \n" + json);
		}
	}

	final protected void debugDocument(Document doc) {
		if (logger.isDebugEnabled()) {
			String json = new BsonProvider<>().getGson().toJson(doc);
			logger.debug("Document: \n" + json);
		}
	}

	final protected void deleteFile(Document remoteFile, String domain) {
		ObjectId _id = remoteFile.getObjectId("_id");
		String namespace = remoteFile.getString("namepace");
		GridFSBuckets.create(Domain.getDatabase(domain), namespace).delete(_id);
	}

	final protected void deleteFileInField(Document delete, String field, String domain) {
		List<?> list = (List<?>) delete.get(field);
		if (list != null)
			list.forEach(itm -> deleteFile((Document) itm, domain));
	}

	final protected BasicDBObject ensureGet(BasicDBObject condition, String field) {
		BasicDBObject doc = (BasicDBObject) condition.get(field);
		if (doc == null) {
			doc = new BasicDBObject();
			condition.put(field, doc);
		}
		return doc;
	}

	final protected Document ensureGet(Document condition, String field) {
		Document doc = (Document) condition.get(field);
		if (doc == null) {
			doc = new Document();
			condition.put(field, doc);
		}
		return doc;
	}

	final protected Document blankChart(String domain) {
		return Domain.getJQ(domain, "图表-无数据").doc();
	}

	@Override
	public List<Document> query(QueryCommand command) {
		if (Check.isAssigned(command.jq)) {
			return queryJQ(command.collection, command.jq, command.parameter, command.domain);
		} else if (Check.isAssigned(command.pipeline)) {
			return query(command.collection, command.pipeline, command.parameter, command.domain);
		}
		return new ArrayList<>();
	}

	protected List<Document> query(String collection, String pipeline, Document parameter, String domain) {
		JQ jq = new JQ();
		if (parameter != null)
			parameter.entrySet().forEach(e -> jq.set(e.getKey(), e.getValue()));
		return c(collection, domain).aggregate(jq.array(pipeline)).into(new ArrayList<>());
	}

	protected List<Document> queryJQ(String collection, String jqName, Document parameter, String domain) {
		JQ jq = Domain.get(domain).jq(jqName);
		if (parameter != null)
			parameter.entrySet().forEach(e -> jq.set(e.getKey(), e.getValue()));
		return c(collection, domain).aggregate(jq.array()).into(new ArrayList<>());
	}

}
