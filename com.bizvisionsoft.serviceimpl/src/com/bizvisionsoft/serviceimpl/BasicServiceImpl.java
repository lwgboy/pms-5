package com.bizvisionsoft.serviceimpl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.mail.EmailException;
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
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.tools.Util;
import com.bizvisionsoft.serviceimpl.commons.EmailClient;
import com.bizvisionsoft.serviceimpl.commons.EmailClientBuilder;
import com.bizvisionsoft.serviceimpl.commons.NamedAccount;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
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

	public Logger logger = LoggerFactory.getLogger(getClass());

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
		return Optional.ofNullable(c(clazz).find(new BasicDBObject("_id", _id)).first()).orElse(null);
	}

	protected <T> long delete(ObjectId _id, Class<T> clazz) {
		return c(clazz).deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	protected <T> long delete(ObjectId _id, String cname, Class<T> clazz) {
		return c(cname, clazz).deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
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
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		if (appendPipelines != null) {
			pipeline.addAll(Arrays.asList(appendPipelines));
		}

		return c(clazz).aggregate(pipeline).into(new ArrayList<T>());
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
		pipeline.add(new Document("$lookup", new Document("from", from).append("localField", field)
				.append("foreignField", "_id").append("as", newField)));

		pipeline.add(new Document("$unwind",
				new Document("path", "$" + newField).append("preserveNullAndEmptyArrays", true)));
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

	protected void appendUserInfoAndHeadPic(List<Bson> pipeline, String useIdField, String userInfoField,
			String headPicField) {
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
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<String>("projectName", "$project.name"),
				new Field<String>("projectNumber", "$project.id"))));
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
		return Optional.ofNullable(c("calendar").aggregate(pipeline).first()).map(d -> d.getDouble("basicWorks"))
				.map(w -> w.doubleValue()).orElse(0d);
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

		List<? extends Bson> pipeline = new JQ("查询-日历-资源类-工作日").set("resTypeId", resTypeId)
				.set("week", getDateWeek(cal)).set("date", cal.getTime()).array();

		Document doc = c("calendar").aggregate(pipeline).first();
		if (doc != null) {
			Boolean workdate = doc.getBoolean("workdate");
			Boolean workday = doc.getBoolean("workday");
			Boolean workingDay = doc.getBoolean("workingDay");
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
		new HashSet<String>(receivers)
				.forEach(r -> toBeInsert.add(Message.newInstance(subject, content, sender, r, url)));
		return sendMessages(toBeInsert);
	}

	protected boolean sendMessages(List<Message> toBeInsert) {
		if (Util.isEmptyOrNull(toBeInsert))
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

	protected Document getPieChart(String title, Object legendData, Object series) {
		Document option = new Document();
		option.append("title", new Document("text", title).append("x", "center"));
		option.append("tooltip", new Document("trigger", "item").append("formatter", "{b} : {c} ({d}%)"));
		option.append("legend", new Document("orient", "vertical").append("left", "left").append("data", legendData));
		option.append("series", series);
		return option;
	}

	protected Document getBarChart(String text, Object legendData, Object series) {
		Document option = new Document();
		option.append("title", new Document("text", text).append("x", "center"));
		// option.append("tooltip", new Document("trigger",
		// "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", legendData).append("orient", "vertical").append("left", "right"));
		option.append("grid",
				new Document("left", "3%").append("right", "4%").append("bottom", "3%").append("containLabel", true));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", series);
		return option;
	}

	protected void convertGraphic(ArrayList<Document> works, ArrayList<Document> links, final ArrayList<Task> tasks,
			final ArrayList<Route> routes) {
		works.forEach(doc -> createGraphicTaskNode(tasks, doc));
		works.forEach(doc -> setGraphicTaskParent(tasks, doc));
		links.forEach(doc -> createGrphicRoute(routes, tasks, doc));
	}

	protected void convertGraphicWithRisks(ArrayList<Document> works, ArrayList<Document> links,
			ArrayList<Document> riskEffect, final ArrayList<Task> tasks, final ArrayList<Route> routes,
			final ArrayList<Risk> risks) {
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
			Number lag = (Number) doc.get("lag");
			float interval = Optional.ofNullable(lag).map(l -> l.floatValue()).orElse(0f);
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
					Task task = tasks.stream().filter(t -> t.getId().equals(rf.getObjectId("work_id").toHexString()))
							.findFirst().orElse(null);
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

	protected static BasicDBObject getBson(Object input) {
		return getBson(input, true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	 * TODO 获得userId 管理的项目
	 * 
	 * @param condition
	 * @param userId
	 * @return
	 */
	protected List<ObjectId> getAdministratedProjects(String userId) {
		return c("project").distinct("_id", new Document("status", "进行中"), ObjectId.class).into(new ArrayList<>());
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
		c("project").updateOne(new Document("_id", _id),
				new Document("$set", new Document("backgroundScheduling", true)));

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
		scheduleEst = new Document("date", new Date()).append("overdue", (int) overTime)
				.append("finish", gh.getFinishDate()).append("duration", (int) gh.getT());

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
			c("work").updateOne(new Document("_id", doc.getObjectId("_id")),
					new Document("$set", new Document("scheduleEst", update)));
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 后处理：保存排程结果，解除锁定
		c("project").updateOne(new Document("_id", _id), new Document("$set", new Document("overdueIndex", warningLevel)
				.append("scheduleEst", scheduleEst).append("backgroundScheduling", false)));

		return warningLevel;
	}

	public Document getSystemSetting(String name) {
		return c("setting").find(new Document("name", name)).first();
	}

	private boolean sendEmail(Message m, String from, Document setting) {
		String userId = m.getReceiver();
		Document user = c("user").find(new Document("userId", userId)).first();
		if (user == null)
			return false;
		String receiverAddress = user.getString("email");
		if (receiverAddress == null || receiverAddress.isEmpty())
			return false;

		String smtpHost = setting.getString("smtpHost");
		int smtpPort;
		try {
			smtpPort = Integer.parseInt(setting.getString("smtpPort"));
		} catch (Exception e) {
			return false;
		}
		Boolean smtpUseSSL = Boolean.TRUE.equals(setting.get("smtpUseSSL"));
		String senderPassword = setting.getString("senderPassword");
		String senderAddress = setting.getString("senderAddress");

		try {
			return sendEmail(smtpHost, smtpPort, smtpUseSSL, senderAddress, senderPassword, receiverAddress,
					m.getSubject(), m.getContent(), from, null);
		} catch (EmailException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	private boolean sendEmail(String smtpHost, int smtpPort, Boolean smtpUseSSL, String senderAddress,
			String senderPassword, String receiverAddress, String title, String message, String from,
			List<String[]> atts) throws EmailException {
		EmailClient client = new EmailClientBuilder(EmailClientBuilder.SIMPLE)//
				.setSenderAddress(senderAddress)//
				.setSenderPassword(senderPassword)//
				.setSmtpHost(smtpHost)//
				.setSmtpPort(smtpPort)//
				.setSmtpUseSSL(smtpUseSSL)//
				.setCharset("GB2312")//
				.build();
		try {
			client.setSubject(title)//
					.setMessage(message)//
					.setFrom(new NamedAccount(from,senderAddress))//
					.addCc(new NamedAccount(receiverAddress))//
					.send();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	protected void debugPipeline(List<? extends Bson> pipeline) {
		if (logger.isDebugEnabled()) {
			String json = new GsonBuilder().setPrettyPrinting().create().toJson(pipeline);
			logger.debug("Aggregation Pipeline: \n" + json);
		}
	}

}
