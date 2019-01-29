package com.bizvisionsoft.serviceimpl;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.math.scheduling.Consequence;
import com.bizvisionsoft.math.scheduling.Graphic;
import com.bizvisionsoft.math.scheduling.Relation;
import com.bizvisionsoft.math.scheduling.Risk;
import com.bizvisionsoft.math.scheduling.Route;
import com.bizvisionsoft.math.scheduling.Task;
import com.bizvisionsoft.mongocodex.codec.CodexProvider;
import com.bizvisionsoft.service.dps.EmailSender;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.provider.BsonProvider;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.commons.EmailClient;
import com.bizvisionsoft.serviceimpl.commons.EmailClientBuilder;
import com.bizvisionsoft.serviceimpl.commons.NamedAccount;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

public class BasicServiceImpl {

	public static String CHECKIN_SETTING_NAME = "项目计划提交设置";

	public static String START_SETTING_NAME = "项目启动设置";

	public static String CLOSE_SETTING_NAME = "项目关闭设置";

	protected static List<String> PROJECT_SETTING_NAMES = Arrays.asList(CHECKIN_SETTING_NAME, START_SETTING_NAME, CLOSE_SETTING_NAME);

	public Logger logger = LoggerFactory.getLogger(getClass());
	
	protected Document findAndUpdate(BasicDBObject fu, String col) {
		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		BasicDBObject update = (BasicDBObject) fu.get("update");
		update.remove("_id");
		return c(col).findOneAndUpdate(filter, update);
	}

	protected <T> long update(BasicDBObject fu, Class<T> clazz) {
		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		BasicDBObject update = (BasicDBObject) fu.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		UpdateResult updateMany = c(clazz).updateMany(filter, update, option);
		long cnt = updateMany.getModifiedCount();

		return cnt;
	}

	protected <T> long update(BasicDBObject fu, String cname, Class<T> clazz) {
		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		BasicDBObject update = (BasicDBObject) fu.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		UpdateResult updateMany = c(cname, clazz).updateMany(filter, update, option);
		long cnt = updateMany.getModifiedCount();
		return cnt;
	}

	protected <T> long update(BasicDBObject fu, String cname) {
		BasicDBObject filter = (BasicDBObject) fu.get("filter");
		BasicDBObject update = (BasicDBObject) fu.get("update");
		update.remove("_id");
		UpdateOptions option = new UpdateOptions();
		option.upsert(false);
		UpdateResult updateMany = c(cname).updateMany(filter, update, option);
		long cnt = updateMany.getModifiedCount();
		return cnt;
	}

	@SuppressWarnings("unchecked")
	protected <T> T insert(T obj) {
		try {
			c((Class<T>) obj.getClass()).insertOne(obj);
			return obj;
		} catch (Exception e) {
			throw handleMongoException(e, obj.toString());
		}
	}

	protected <T> T insert(T obj, Class<T> clazz) {
		try {
			c(clazz).insertOne(obj);
			return obj;
		} catch (Exception e) {
			throw handleMongoException(e, obj.toString());
		}
	}

	protected <T> T insert(T obj, String cname, Class<T> clazz) {
		try {
			c(cname, clazz).insertOne(obj);
			return obj;
		} catch (Exception e) {
			throw handleMongoException(e, obj.toString());
		}
	}

	protected <T> T get(ObjectId _id, Class<T> clazz) {
		return c(clazz).find(new BasicDBObject("_id", _id)).first();
	}

	protected Document getDocument(ObjectId _id, String col) {
		return c(col).find(new BasicDBObject("_id", _id)).first();
	}

	protected <T> long delete(ObjectId _id, Class<T> clazz) {
		return c(clazz).deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	protected <T> long delete(ObjectId _id, String cname, Class<T> clazz) {
		return c(cname, clazz).deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	protected <T> long deleteMany(Bson filter, String cname) {
		return c(cname).deleteMany(filter).getDeletedCount();
	}

	protected long deleteOne(ObjectId _id, String cname) {
		return c(cname).deleteOne(new Document("_id", _id)).getDeletedCount();
	}
	
	protected Document findAndDeleteOne(ObjectId _id, String cname) {
		return c(cname).findOneAndDelete(new Document("_id", _id));
	}

	protected <T> long count(BasicDBObject filter, Class<T> clazz) {
		if (filter != null)
			return c(clazz).countDocuments(filter);
		return c(clazz).countDocuments();
	}

	protected <T> long count(BasicDBObject filter, String colName) {
		if (filter != null)
			return c(colName).countDocuments(filter);
		return c(colName).countDocuments();
	}

	final protected <T> List<T> list(Class<T> clazz, BasicDBObject condition, Bson... appendPipelines) {
		List<Bson> pipeline = null;
		if (appendPipelines != null)
			pipeline = Arrays.asList(appendPipelines);
		return list(clazz, condition, pipeline);
	}

	final protected List<Document> list(String col, BasicDBObject condition, Bson... appendPipelines) {
		List<Bson> pipeline = null;
		if (appendPipelines != null)
			pipeline = Arrays.asList(appendPipelines);
		return list(col, condition, pipeline);
	}

	/**
	 * 增加传入pipeline的list类型的实现
	 * 
	 * @param clazz
	 * @param condition
	 * @param appendPipelines
	 * @return
	 */
	final protected <T> List<T> list(Class<T> clazz, BasicDBObject condition, List<Bson> appendPipelines) {
		ArrayList<Bson> pipeline = combinateQueryPipeline(condition, appendPipelines);
		return c(clazz).aggregate(pipeline).into(new ArrayList<T>());
	}

	final protected List<Document> list(String col, BasicDBObject condition, List<Bson> appendPipelines) {
		ArrayList<Bson> pipeline = combinateQueryPipeline(condition, appendPipelines);
		return c(col).aggregate(pipeline).into(new ArrayList<>());
	}

	final protected long count(String col, BasicDBObject filter, Bson... prefixPipeline) {
		List<Bson> pipeline = null;
		if (prefixPipeline != null)
			pipeline = Arrays.asList(prefixPipeline);
		return count(col, filter, pipeline);
	}

	final protected long count(String col, BasicDBObject filter, List<Bson> prefixPipelines) {
		ArrayList<Bson> pipeline = combinateCountPipeline(prefixPipelines,filter);
		return Optional.ofNullable(c(col).aggregate(pipeline).first()).map(d -> (Number) d.get("count")).map(d -> d.longValue()).orElse(0l);
	}

	protected List<Bson> appendConditionToPipeline(List<Bson> pipeline, BasicDBObject condition) {
		Optional.ofNullable((BasicDBObject) condition.get("filter")).map(Aggregates::match).ifPresent(pipeline::add);
		Optional.ofNullable((BasicDBObject) condition.get("sort")).map(Aggregates::sort).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("skip")).map(Aggregates::skip).ifPresent(pipeline::add);
		Optional.ofNullable((Integer) condition.get("limit")).map(Aggregates::limit).ifPresent(pipeline::add);
		return pipeline;
	}
	
	protected ArrayList<Bson> combinateQueryPipeline(BasicDBObject condition, List<Bson> appendPipelines) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		appendConditionToPipeline(pipeline,condition);
		if (appendPipelines != null) {
			pipeline.addAll(appendPipelines);
		}
		return pipeline;
	}

	protected ArrayList<Bson> combinateCountPipeline(List<Bson> prefixPipeline,BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();
		if (prefixPipeline != null)
			pipeline.addAll(prefixPipeline);
		if (filter != null)
			pipeline.add(Aggregates.match(filter));
		pipeline.add(Aggregates.count());
		return pipeline;
	}

	protected <T> List<T> createDataSet(BasicDBObject condition, Class<T> clazz) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(skip, limit, filter, sort, clazz);
	}

	<T> List<T> query(Integer skip, Integer limit, BasicDBObject filter, Class<T> clazz) {
		return query(skip, limit, filter, null, clazz);
	}

	<T> List<T> query(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, Class<T> clazz) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		debugPipeline(pipeline);

		return c(clazz).aggregate(pipeline).into(new ArrayList<T>());
	}

	protected void appendLookupAndUnwind(List<Bson> pipeline, String from, String field, String newField) {
		pipeline.add(new Document("$lookup",
				new Document("from", from).append("localField", field).append("foreignField", "_id").append("as", newField)));

		pipeline.add(new Document("$unwind", new Document("path", "$" + newField).append("preserveNullAndEmptyArrays", true)));
	}

	protected void appendOrgFullName(List<Bson> pipeline, String inputField, String outputField) {
		String tempField = "_org_" + inputField;

		pipeline.add(Aggregates.lookup("organization", inputField, "_id", tempField));

		pipeline.add(Aggregates.unwind("$" + tempField, new UnwindOptions().preserveNullAndEmptyArrays(true)));

		pipeline.add(Aggregates.addFields(new Field<String>(outputField, "$" + tempField + ".fullName")));

		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));//
	}

	protected void appendStage(List<Bson> pipeline, String inputField, String outputField) {
		pipeline.add(Aggregates.lookup("work", inputField, "_id", outputField));

		pipeline.add(Aggregates.unwind("$" + outputField, new UnwindOptions().preserveNullAndEmptyArrays(true)));
	}

	protected void appendUserInfo(List<Bson> pipeline, String useIdField, String userInfoField) {
		appendUserInfo(pipeline, useIdField, userInfoField, userInfoField + "_meta");
	}

	protected void appendUserInfo(List<Bson> pipeline, String useIdField, String userInfoField, String userMetaField) {
		pipeline.addAll(new JQ("追加-用户")//
				.set("$chargerId", "$" + useIdField)//
				.set("chargerInfo_meta", userMetaField)//
				.set("$chargerInfo_meta", "$" + userMetaField)//
				.set("chargerInfo_meta.org_id", userMetaField + ".org_id")//
				.set("chargerInfo", userInfoField)//
				.set("$chargerInfo_meta.name", "$" + userMetaField + ".name")//
				.set("chargerInfo_meta.orgInfo", userMetaField + ".orgInfo")//
				.array());
	}

	protected void appendUserInfoAndHeadPic(List<Bson> pipeline, String useIdField, String userInfoField, String headPicField) {
		String tempField = "_user_" + useIdField;

		pipeline.add(Aggregates.lookup("user", useIdField, "userId", tempField));

		pipeline.add(Aggregates.unwind("$" + tempField, new UnwindOptions().preserveNullAndEmptyArrays(true)));

		pipeline.add(Aggregates.addFields(
				// info字段 去掉userid显示
				// new Field<BasicDBObject>(userInfoField,
				// new BasicDBObject("$concat",
				// new String[] { "$" + tempField + ".name", " [", "$" + tempField + ".userId",
				// "]" })),
				new Field<String>(userInfoField, "$" + tempField + ".name"),
				// headPics字段
				new Field<BasicDBObject>(headPicField,
						new BasicDBObject("$arrayElemAt", new Object[] { "$" + tempField + ".headPics", 0 }))));

		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));//
	}

	protected void appendWork(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("work", "work_id", "_id", "work"));
		pipeline.add(Aggregates.unwind("$work"));
	}

	protected void appendProject(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
	}

	protected void appendSortBy(List<Bson> pipeline, String fieldName, int i) {
		pipeline.add(Aggregates.sort(new BasicDBObject(fieldName, i)));
	}

	@Deprecated
	protected List<Bson> getOBSRootPipline(ObjectId project_id) {
		List<Bson> pipeline = new ArrayList<Bson>();

		String tempField = "_obs" + project_id;
		pipeline.add(Aggregates.match(new BasicDBObject("_id", project_id)));
		pipeline.add(Aggregates.lookup("obs", "obs_id", "_id", tempField));
		pipeline.add(Aggregates.project(new BasicDBObject(tempField, true).append("_id", false)));
		pipeline.add(Aggregates.replaceRoot(new BasicDBObject("$mergeObjects",
				new Object[] { new BasicDBObject("$arrayElemAt", new Object[] { "$" + tempField, 0 }), "$$ROOT" })));
		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));

		return pipeline;
	}

	protected <T> MongoCollection<T> c(Class<T> clazz) {
		return Service.col(clazz);
	}

	protected <T> MongoCollection<T> c(String col, Class<T> clazz) {
		return Service.db().getCollection(col, clazz);
	}

	protected MongoCollection<Document> c(String name) {
		return Service.col(name);
	}

	/**
	 * 使用lookupDesentItems替代
	 * 
	 * @param inputIds
	 * @param cName
	 * @param key
	 * @return
	 */
	@Deprecated
	protected List<ObjectId> getDesentItems(List<ObjectId> inputIds, String cName, String key) {
		List<ObjectId> result = new ArrayList<ObjectId>();
		if (inputIds != null && !inputIds.isEmpty()) {
			result.addAll(inputIds);
			List<ObjectId> childrenIds = c(cName)
					.distinct("_id", new BasicDBObject(key, new BasicDBObject("$in", inputIds)), ObjectId.class)
					.into(new ArrayList<ObjectId>());
			result.addAll(getDesentItems(childrenIds, cName, key));
		}
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
	protected Iterable<Document> lookupDesentItems(List<ObjectId> inputIds, String cName, String key, boolean includeCurrentLevel) {
		String jq = includeCurrentLevel ? "查询-通用-下级迭代取出-含本级" : "查询-通用-下级迭代取出";
		ArrayList<Bson> pipe = new ArrayList<>();
		if (inputIds.size() > 1) {
			pipe.add(Aggregates.match(new Document("_id", new Document("$in", inputIds))));
		} else if (inputIds.size() == 1) {
			pipe.add(Aggregates.match(new Document("_id", inputIds.get(0))));
		} else {
			return new ArrayList<>();
		}
		pipe.addAll(
				new JQ(jq).set("from", cName).set("startWith", "$_id").set("connectFromField", "_id").set("connectToField", key).array());
		return c(cName).aggregate(pipe);
	}

	protected int generateCode(String name, String key) {
		Document doc = c(name).findOneAndUpdate(Filters.eq("_id", key), Updates.inc("value", 1),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
		return doc.getInteger("value");
	}

	public double getWorkingHoursPerDay(ObjectId resTypeId) {
		// List<? extends Bson> pipeline = Arrays.asList(
		// new Document("$lookup",
		// new Document("from", "resourceType").append("localField", "_id")
		// .append("foreignField", "cal_id").append("as", "resourceType")),
		// new Document("$unwind", "$resourceType"),
		// new Document("$addFields", new Document("resTypeId", "$resourceType._id")),
		// new Document("$project", new Document("resourceType", false)),
		// new Document("$match", new Document("resTypeId", resTypeId)),
		// new Document("$project", new Document("works", true)));

		List<? extends Bson> pipeline = new JQ("查询-日历-资源类-每日工时").set("resTypeId", resTypeId).array();
		return Optional.ofNullable(c("calendar").aggregate(pipeline).first()).map(d -> d.getDouble("basicWorks")).map(w -> w.doubleValue())
				.orElse(0d);
	}

	public boolean checkDayIsWorkingDay(Calendar cal, ObjectId resTypeId) {

		// List<? extends Bson> pipeline = Arrays
		// .asList(new Document("$lookup",
		// new Document("from", "resourceType").append("localField", "_id")
		// .append("foreignField", "cal_id").append("as", "resourceType")),
		// new Document("$unwind", "$resourceType"),
		// new Document("$addFields", new Document("resTypeId", "$resourceType._id")),
		// new Document("$project", new Document("resourceType", false)),
		// new Document("$match", new Document("resTypeId", resId)), new
		// Document("$unwind", "$workTime"),
		// new Document("$addFields", new Document("date", "$workTime.date")
		// .append("workingDay", "$workTime.workingDay").append("day",
		// "$workTime.day")),
		// new Document("$project", new Document("workTime",
		// false)),
		// new Document()
		// .append("$unwind", new Document("path",
		// "$day").append("preserveNullAndEmptyArrays",
		// true)),
		// new Document("$addFields", new Document().append("workdate",
		// new Document("$cond",
		// Arrays.asList(new Document("$eq", Arrays.asList("$date", cal.getTime())),
		// true,
		// false)))
		// .append("workday", new Document("$eq", Arrays.asList("$day",
		// getDateWeek(cal))))),
		// new Document("$project",
		// new Document("workdate", true).append("workday", true).append("workingDay",
		// true)),
		// new Document("$match",
		// new Document("$or",
		// Arrays.asList(new Document("workdate", true), new Document("workday",
		// true)))),
		// new Document("$sort", new Document("workdate", -1).append("workingDay",
		// -1)));

		List<? extends Bson> pipeline = new JQ("查询-日历-资源类-工作日").set("resTypeId", resTypeId).set("week", getDateWeek(cal))
				.set("date", cal.getTime()).array();

		Document doc = c("calendar").aggregate(pipeline).first();
		if (doc != null) {
			boolean workdate = doc.getBoolean("workdate", false);
			boolean workday = doc.getBoolean("workday", false);
			boolean workingDay = doc.getBoolean("workingDay", false);
			if (workdate) {
				return workingDay;
			} else {
				return workday;
			}
		}
		return false;
	}

	public String getDateWeek(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case 1:
			return "周日";
		case 2:
			return "周一";
		case 3:
			return "周二";
		case 4:
			return "周三";
		case 5:
			return "周四";
		case 6:
			return "周五";
		default:
			return "周六";
		}
	}

	protected boolean sendMessage(String subject, String content, String sender, String receiver, String url) {
		sendMessage(Message.newInstance(subject, content, sender, receiver, url));
		return true;
	}

	protected boolean sendMessage(String subject, String content, String sender, List<String> receivers, String url) {
		List<Message> toBeInsert = new ArrayList<>();
		new HashSet<String>(receivers).forEach(r -> toBeInsert.add(Message.newInstance(subject, content, sender, r, url)));
		return sendMessages(toBeInsert);
	}

	protected boolean sendMessages(List<Message> toBeInsert) {
		if (Check.isNotAssigned(toBeInsert))
			return false;
		c(Message.class).insertMany(toBeInsert);
		Document setting = getSystemSetting("邮件设置");
		if (setting != null && Boolean.TRUE.equals(setting.get("emailNotice"))) {
			toBeInsert.forEach(m -> sendEmail(m, "系统", setting));
		}
		return true;
	}

	protected boolean sendMessage(Message message) {
		c(Message.class).insertOne(message);
		Document setting = getSystemSetting("邮件设置");
		if (setting != null && Boolean.TRUE.equals(setting.get("emailNotice"))) {
			sendEmail(message, "系统", setting);
		}
		return true;
	}

	protected String getName(String cName, ObjectId _id) {
		return c(cName).distinct("name", new BasicDBObject("_id", _id), String.class).first();
	}

	protected String getString(String cName, String fName, ObjectId _id) {
		return c(cName).distinct(fName, new BasicDBObject("_id", _id), String.class).first();
	}

	protected <T> T getValue(String cName, String fName, ObjectId _id, Class<T> c) {
		return c(cName).distinct(fName, new BasicDBObject("_id", _id), c).first();
	}

	protected void convertGraphic(ArrayList<Document> works, ArrayList<Document> links, final ArrayList<Task> tasks,
			final ArrayList<Route> routes) {
		works.forEach(doc -> createGraphicTaskNode(tasks, doc));
		works.forEach(doc -> setGraphicTaskParent(tasks, doc));
		links.forEach(doc -> createGrphicRoute(routes, tasks, doc));
	}

	protected void convertGraphicWithRisks(ArrayList<Document> works, ArrayList<Document> links, ArrayList<Document> riskEffect,
			final ArrayList<Task> tasks, final ArrayList<Route> routes, final ArrayList<Risk> risks) {
		convertGraphic(works, links, tasks, routes);
		riskEffect.forEach(doc -> createGraphicRiskNode(tasks, risks, doc));
		riskEffect.forEach(doc -> setGraphicRiskParent(risks, doc));
	}

	private Route createGrphicRoute(ArrayList<Route> routes, ArrayList<Task> tasks, Document doc) {
		String end1Id = doc.getObjectId("source").toHexString();
		String end2Id = doc.getObjectId("target").toHexString();
		Task end1 = tasks.stream().filter(t -> end1Id.equals(t.getId())).findFirst().orElse(null);
		Task end2 = tasks.stream().filter(t -> end2Id.equals(t.getId())).findFirst().orElse(null);
		if (end1 != null && end2 != null) {
			String _type = doc.getString("type");
			int type = Relation.FTS;
			if ("FF".equals(_type)) {
				type = Relation.FTF;
			} else if ("SF".equals(_type)) {
				type = Relation.STF;
			} else if ("SS".equals(_type)) {
				type = Relation.STS;
			}
			float interval = Optional.ofNullable((Number) doc.get("lag")).map(l -> l.floatValue()).orElse(0f);
			Relation rel = new Relation(type, interval);
			Route route = new Route(end1, end2, rel);
			routes.add(route);
			return route;
		}
		return null;
	}

	private void setGraphicTaskParent(final ArrayList<Task> tasks, final Document doc) {
		Optional.ofNullable(doc.getObjectId("parent_id")).map(_id -> _id.toHexString()).ifPresent(parentId -> {
			String id = doc.getObjectId("_id").toHexString();
			Task subTask = tasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
			Task parentTask = tasks.stream().filter(t -> parentId.equals(t.getId())).findFirst().orElse(null);
			if (subTask != null && parentTask != null) {
				parentTask.addSubTask(subTask);
			}
		});
	}

	private void setGraphicRiskParent(ArrayList<Risk> risks, Document doc) {
		Optional.ofNullable(doc.getObjectId("parent_id")).map(_id -> _id.toHexString()).ifPresent(parentId -> {
			String id = doc.getObjectId("_id").toHexString();
			Risk subRisk = risks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
			Risk parentRisk = risks.stream().filter(t -> parentId.equals(t.getId())).findFirst().orElse(null);
			if (subRisk != null && parentRisk != null) {
				parentRisk.addSecondaryRisks(subRisk);
			}
		});
	}

	private void createGraphicRiskNode(ArrayList<Task> tasks, ArrayList<Risk> risks, final Document doc) {
		String id = doc.getObjectId("_id").toHexString();
		double prob = doc.getDouble("probability");// 取出的数字需要除100
		List<?> riskEffects = (List<?>) doc.get("riskEffect");
		List<Consequence> cons = new ArrayList<>();
		if (riskEffects != null && !riskEffects.isEmpty()) {
			for (int i = 0; i < riskEffects.size(); i++) {
				Document rf = (Document) riskEffects.get(i);
				Integer timeInf = rf.getInteger("timeInf");
				if (timeInf != null) {
					Task task = tasks.stream().filter(t -> t.getId().equals(rf.getObjectId("work_id").toHexString())).findFirst()
							.orElse(null);
					if (task != null) {
						cons.add(new Consequence(task, timeInf.intValue()));
					}
				}
			}
		}
		if (cons.size() > 0) {
			risks.add(new Risk(id, (float) prob / 100, cons.toArray(new Consequence[0])));
		}
	}

	private Task createGraphicTaskNode(ArrayList<Task> tasks, Document doc) {
		String id = doc.getObjectId("_id").toHexString();
		Date aStart = doc.getDate("actualStart");
		Date aFinish = doc.getDate("actualFinish");
		Date pStart = doc.getDate("planStart");
		Date pFinish = doc.getDate("planFinish");
		Date now = new Date();
		/////////////////////////////////
		// 只考虑完成时取实际工期
		// 未完成时取计划工期
		long duration;
		if (aFinish != null) {// 如果工作已经完成，工期为实际完成-实际开始
			duration = aFinish.getTime() - aStart.getTime();
		} else if (aStart != null) {// 如果工作已开始
			if (now.after(pFinish)) {// 如果超过完成时间还未完成，视作可能立即完成
				duration = now.getTime() - aStart.getTime() + aStart.getTime() - pStart.getTime();
			} else {
				duration = pFinish.getTime() - pStart.getTime() + aStart.getTime() - pStart.getTime();
			}
		} else if (now.after(pStart)) {
			duration = pFinish.getTime() - pStart.getTime() + now.getTime() - pStart.getTime();
		} else {
			duration = pFinish.getTime() - pStart.getTime();
		}

		Task task = new Task(id, duration / (1000 * 60 * 60 * 24));
		task.setName(doc.getString("name"));
		tasks.add(task);
		return task;
	}

	protected void setupStartDate(Graphic gh, ArrayList<Document> works, Date pjStart, ArrayList<Task> tasks) {
		gh.setStartDate(pjStart);
		gh.getStartRoute().forEach(r -> {
			String id = r.end2.getId();
			ObjectId _id = new ObjectId(id);
			Document doc = works.stream().filter(d -> _id.equals(d.getObjectId("_id"))).findFirst().get();
			Date aStart = doc.getDate("actualStart");
			Date pStart = doc.getDate("planStart");
			gh.setStartDate(id, aStart == null ? pStart : aStart);
		});
	}

	/**
	 * 使用BsonTools替代
	 * 
	 * @param input
	 * @return
	 */
	@Deprecated
	protected static BasicDBObject getBson(Object input) {
		return getBson(input, true);
	}

	/**
	 * 使用BsonTools替代
	 * 
	 * @param input
	 * @param ignoreNull
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Deprecated
	private static BasicDBObject getBson(Object input, boolean ignoreNull) {
		Codec codec = CodexProvider.getRegistry().get(input.getClass());
		StringWriter sw = new StringWriter();
		codec.encode(new JsonWriter(sw), input, EncoderContext.builder().build());
		String json = sw.toString();
		BasicDBObject result = BasicDBObject.parse(json);

		BasicDBObject _result = new BasicDBObject();
		Iterator<String> iter = result.keySet().iterator();
		while (iter.hasNext()) {
			String k = iter.next();
			Object v = result.get(k);
			if (v == null && ignoreNull) {
				continue;
			}
			_result.append(k, v);
		}
		return _result;
	}

	/**
	 * 获得userId 管理的项目
	 * 
	 * @param condition
	 * @param userId
	 * @return
	 */
	protected List<ObjectId> getAdministratedProjects(String userId) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$match",
				new Document("status", new Document("$in", Arrays.asList(ProjectStatus.Processing, ProjectStatus.Closing)))));

		// 当前用户具有项目总监权限时显示全部，不显示全部时，加载PMO团队查询
		if (!checkUserRoles(userId, Role.SYS_ROLE_PD_ID)) {
			appendQueryUserInProjectPMO(pipeline, userId, "$_id");
		}

		return c("project").aggregate(pipeline).map(d -> d.getObjectId("_id")).into(new ArrayList<>());
		// return c("project").distinct("_id", new Document("status", "进行中"),
		// ObjectId.class).into(new ArrayList<>());
	}

	/**
	 * 添加获取项目时，只获取当前用户在项目PMO团队中的项目的查询
	 * 
	 * @param pipeline
	 * @param userid
	 */
	protected void appendQueryUserInProjectPMO(List<Bson> pipeline, String userid, String scopeIdName) {
		pipeline.addAll(new JQ("查询-项目PMO成员").set("scopeIdName", scopeIdName).set("userId", userid).array());
	}

	/**
	 * 处理错误
	 * 
	 * @param e
	 * @param message
	 * @return
	 */
	final protected ServiceException handleMongoException(Exception e, String message) {
		if (e instanceof MongoException && ((MongoException) e).getCode() == 11000) {
			return new ServiceException("违反唯一性规则：" + message);
		}

		return new ServiceException(e.getMessage());
	}

	public Integer schedule(ObjectId _id) {
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 锁定
		Document pj = c("project").find(new Document("_id", _id)).first();
		if (pj.getBoolean("backgroundScheduling", false)) {
			return -1;
		}
		c("project").updateOne(new Document("_id", _id), new Document("$set", new Document("backgroundScheduling", true)));

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 前处理：构造图
		ArrayList<Document> works = c("work").find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> links = c("worklinks").find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Task> tasks = new ArrayList<Task>();
		ArrayList<Route> routes = new ArrayList<Route>();
		convertGraphic(works, links, tasks, routes);
		Graphic gh = new Graphic(tasks, routes);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 前处理：初始化日期
		Date start = pj.getDate("planStart");
		Date end = pj.getDate("planFinish");
		setupStartDate(gh, works, start, tasks);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 排程计算
		gh.schedule();

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 后处理：检查项目是否超期
		int warningLevel = 999;
		Document scheduleEst = null;
		// 0级预警检查
		float overTime = gh.getT() - ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
		scheduleEst = new Document("date", new Date()).append("overdue", (int) overTime).append("finish", gh.getFinishDate())
				.append("duration", (int) gh.getT());

		if (overTime > 0) {
			warningLevel = 0;// 0级预警，项目可能超期。
			scheduleEst.append("msg", "项目预计超期");
		}
		for (int i = 0; i < works.size(); i++) {
			Document doc = works.get(i);
			Date planFinish = doc.getDate("planFinish");
			Date estFinish = gh.getTaskEFDate((doc.getObjectId("_id").toHexString()));
			Task task = gh.getTask((doc.getObjectId("_id").toHexString()));
			long overdue = ((estFinish.getTime() - planFinish.getTime()) / (1000 * 60 * 60 * 24));
			Document update = new Document("date", new Date()).append("duration", (int) task.getD().floatValue())
					.append("overdue", (int) overdue).append("finish", estFinish);

			if ("1".equals(doc.getString("manageLevel")) && overdue > 0) {
				warningLevel = warningLevel > 1 ? 1 : warningLevel;
				if (scheduleEst.get("msg") == null) {
					scheduleEst.append("msg", "一级工作预计超期");
				}
			}

			if ("2".equals(doc.getString("manageLevel")) && overdue > 0) {
				warningLevel = warningLevel > 2 ? 2 : warningLevel;
				if (scheduleEst.get("msg") == null) {
					scheduleEst.append("msg", "二级工作预计超期");
				}
			}

			if (update == null) {
				update = new Document();
			}
			update.append("tf", (double) task.getTF()).append("ff", (double) task.getFF());
			c("work").updateOne(new Document("_id", doc.getObjectId("_id")), new Document("$set", new Document("scheduleEst", update)));
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 后处理：保存排程结果，解除锁定
		c("project").updateOne(new Document("_id", _id), new Document("$set",
				new Document("overdueIndex", warningLevel).append("scheduleEst", scheduleEst).append("backgroundScheduling", false)));

		return warningLevel;
	}

	public Document getSystemSetting(String name) {
		return c("setting").find(new Document("name", name)).first();
	}

	public Object getSystemSetting(String name, String parameter) {
		return Optional.ofNullable(getSystemSetting(name)).map(d -> d.get(parameter)).orElse(null);
	}

	private boolean sendEmail(Message m, String from, Document setting) {
		Service.run(() -> {
			String subject = m.getSubject();
			String content = m.getContent();
			String userId = m.getReceiver();
			Document user = c("user").find(new Document("userId", userId)).first();
			if (user == null)
				return;

			String receiverAddress = user.getString("email");
			if (Check.isNotAssigned(receiverAddress)) {
				logger.error("邮件未能发送，原因是没有邮箱地址，用户：" + user);
				return;
			}

			if (logger.isDebugEnabled()) {
				logger.warn("调试模式启动下，只发送到系统设置的接受账号");
				receiverAddress = (String) getSystemSetting("测试邮件接收账户", "testEmail");
				if (Check.isNotAssigned(receiverAddress)) {
					logger.error("邮件未能发送，原因是没有设置系统参数：测试邮件接收账户/testEmail");
					return;
				}
			}

			if (Boolean.TRUE.equals(setting.get("dps"))) {
				EmailSender sender = Service.get(EmailSender.class);
				if (sender != null) {
					try {
						sender.send("BizVision PMS5", receiverAddress, subject, content, from);
						return;
					} catch (Exception e) {
						logger.error("DPS 发送邮件错误。", e);
					}
				} else {
					logger.error("发送邮件失败。");
				}
				return;
			} else {
				String smtpHost = setting.getString("smtpHost");
				int smtpPort;
				try {
					smtpPort = Integer.parseInt(setting.getString("smtpPort"));
				} catch (Exception e) {
					logger.error("smtp端口号配置错误。", e);
					return;
				}
				Boolean smtpUseSSL = Boolean.TRUE.equals(setting.get("smtpUseSSL"));
				String senderPassword = setting.getString("senderPassword");
				String senderAddress = setting.getString("senderAddress");

				try {
					EmailClient client = new EmailClientBuilder(EmailClientBuilder.SIMPLE)//
							.setSenderAddress(senderAddress)//
							.setSenderPassword(senderPassword)//
							.setSmtpHost(smtpHost)//
							.setSmtpPort(smtpPort)//
							.setSmtpUseSSL(smtpUseSSL)//
							.setCharset("GB2312")//
							.build();
					try {
						client.setSubject(subject)//
								.setMessage(content)//
								.setFrom(new NamedAccount(from, senderAddress))//
								.addCc(new NamedAccount(receiverAddress))//
								.send();
						return;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return;
				}
			}
		});
		return true;
	}

	protected void debugPipeline(List<? extends Bson> pipeline) {
		if (logger.isDebugEnabled()) {
			String json = new BsonProvider<>().getGson().toJson(pipeline);
			logger.debug("Aggregation Pipeline: \n" + json);
		}
	}

	protected void debugDocument(Document doc) {
		if (logger.isDebugEnabled()) {
			String json = new BsonProvider<>().getGson().toJson(doc);
			logger.debug("Document: \n" + json);
		}
	}

	/**
	 * 检查当前用户是否具有某些角色
	 * 
	 * @param userid
	 *            用户编号
	 * @param roles
	 *            角色
	 * @return
	 */
	protected boolean checkUserRoles(String userid, List<String> roles) {
		// 检查当前用户是否需要显示全部信息
		return c("funcPermission").countDocuments(new BasicDBObject("id", userid).append("role", new BasicDBObject("$in", roles))) > 0;
	}

	/**
	 * 检查当前用户是否具有某角色
	 * 
	 * @param userid
	 *            用户编号
	 * @param role
	 *            角色
	 * @return
	 */
	protected boolean checkUserRoles(String userid, String role) {
		return checkUserRoles(userid, Arrays.asList(role));
	}

	/**
	 * 获取用户名称
	 * 
	 * @param userId
	 * @return
	 */
	protected String getUserName(String userId) {
		return c("user").distinct("name", new Document("userId", userId), String.class).first();
	}

	protected void deleteFile(Document remoteFile) {
		ObjectId _id = remoteFile.getObjectId("_id");
		String namespace = remoteFile.getString("namepace");
		GridFSBuckets.create(Service.db(), namespace).delete(_id);
	}

	protected void deleteFileInField(Document delete, String field) {
		List<?> list = (List<?>) delete.get(field);
		if (list != null)
			list.forEach(itm -> deleteFile((Document) itm));
	}

	protected Document updateThen(Document d, String lang, String col, BiFunction<Document, String, Document> func) {
		Document doc = update(d, col);
		return Optional.ofNullable(func).map(f -> f.apply(doc, lang)).orElse(doc);
	}

	protected Document update(Document d, String col) {
		Document filter = new Document("_id", d.get("_id"));
		d.remove("_id");
		Document set = new Document("$set", d);
		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
		Document doc = c(col).findOneAndUpdate(filter, set, options);
		return doc;
	}

	protected BasicDBObject ensureGet(BasicDBObject condition, String field) {
		BasicDBObject doc = (BasicDBObject) condition.get(field);
		if (doc == null) {
			doc = new BasicDBObject();
			condition.put(field, doc);
		}
		return doc;
	}

	protected Document ensureGet(Document condition, String field) {
		Document doc = (Document) condition.get(field);
		if (doc == null) {
			doc = new Document();
			condition.put(field, doc);
		}
		return doc;
	}

	protected ArrayList<String> getXAxisData(String xAxis, Date from, Date to) {
		ArrayList<String> xAxisData = new ArrayList<String>();
		SimpleDateFormat sdf;
		if ("date".equals(xAxis)) {
			Calendar cal = Calendar.getInstance();
			from = Formatter.getStartOfDay(from);
			to = Formatter.getEndOfDay(to);
			cal.setTime(from);
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else if ("month".equals(xAxis)) {
			Calendar cal = Calendar.getInstance();
			from = Formatter.getStartOfMonth(from);
			to = Formatter.getEndOfMonth(to);
			cal.setTime(from);
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.MONTH, 1);
			}
		} else if ("year".equals(xAxis)) {
			Calendar cal = Calendar.getInstance();
			from = Formatter.getStartOfYear(from);
			to = Formatter.getEndOfYear(to);
			cal.setTime(from);
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(to)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.YEAR, 1);
			}
		}
		return xAxisData;
	}

	public Document blankChart() {
		return new JQ("图表-无数据").doc();
	}
}
