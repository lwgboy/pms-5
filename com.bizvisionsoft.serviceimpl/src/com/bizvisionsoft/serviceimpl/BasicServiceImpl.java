package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
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

	@SuppressWarnings("unchecked")
	protected <T> T insert(T obj) {
		c((Class<T>) obj.getClass()).insertOne(obj);
		return obj;
	}

	protected <T> T insert(T obj, Class<T> clazz) {
		c(clazz).insertOne(obj);
		return obj;
	}

	protected <T> T insert(T obj, String cname, Class<T> clazz) {
		c(cname, clazz).insertOne(obj);
		return obj;
	}

	protected <T> T get(ObjectId _id, Class<T> clazz) {
		T obj = c(clazz).find(new BasicDBObject("_id", _id)).first();
		return Optional.ofNullable(obj).orElse(null);
	}

	protected <T> long delete(ObjectId _id, Class<T> clazz) {
		return c(clazz).deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	protected <T> long delete(ObjectId _id, String cname, Class<T> clazz) {
		return c(cname, clazz).deleteOne(new BasicDBObject("_id", _id)).getDeletedCount();
	}

	protected <T> long count(BasicDBObject filter, Class<T> clazz) {
		if (filter != null)
			return c(clazz).count(filter);
		return c(clazz).count();
	}

	protected <T> long count(BasicDBObject filter, String colName) {
		if (filter != null)
			return c(colName).count(filter);
		return c(colName).count();
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

		List<T> result = new ArrayList<T>();
		c(clazz).aggregate(pipeline).into(result);
		return result;
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
		String tempField = "_user_" + useIdField;

		pipeline.add(Aggregates.lookup("user", useIdField, "userId", tempField));

		pipeline.add(Aggregates.unwind("$" + tempField, new UnwindOptions().preserveNullAndEmptyArrays(true)));

		pipeline.add(Aggregates.addFields(new Field<BasicDBObject>(userInfoField, new BasicDBObject("$concat",
				new String[] { "$" + tempField + ".name", " [", "$" + tempField + ".userId", "]" }))));

		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));//
	}

	protected void appendUserInfoAndHeadPic(List<Bson> pipeline, String useIdField, String userInfoField,
			String headPicField) {
		String tempField = "_user_" + useIdField;

		pipeline.add(Aggregates.lookup("user", useIdField, "userId", tempField));

		pipeline.add(Aggregates.unwind("$" + tempField, new UnwindOptions().preserveNullAndEmptyArrays(true)));

		pipeline.add(Aggregates.addFields(
				// info字段
				new Field<BasicDBObject>(userInfoField,
						new BasicDBObject("$concat",
								new String[] { "$" + tempField + ".name", " [", "$" + tempField + ".userId", "]" })),
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

	protected static final String WARNING_DAY = "warningDay";

	protected Object getSystemSetting(String settingName) {
		if (settingName.equals(WARNING_DAY)) {
			return 5;
		}
		return null;
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

		List<? extends Bson> pipeline = new JQ("获得资源类别每天工作小时数").set("resTypeId", resTypeId).array();
		return Optional.ofNullable(c("calendar").aggregate(pipeline).first()).map(d -> d.getDouble("works"))
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

		List<? extends Bson> pipeline = new JQ("检验某资源类别某天是否工作日").set("resTypeId", resTypeId)
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

	protected boolean sendMessage(Message message) {
		c(Message.class).insertOne(message);
		return true;
	}

	protected boolean sendMessage(String subject, String content, String sender, String receiver, String url) {
		sendMessage(Message.newInstance(subject, content, sender, receiver, url));
		return true;
	}

	protected boolean sendMessage(String subject, String content, String sender, List<String> receivers, String url) {
		List<Message> toBeInsert = new ArrayList<>();
		receivers.forEach(r -> toBeInsert.add(Message.newInstance(subject, content, sender, r, url)));
		return sendMessages(toBeInsert);
	}

	protected boolean sendMessages(List<Message> toBeInsert) {
		c(Message.class).insertMany(toBeInsert);
		return true;
	}

	protected String getName(String cName, ObjectId _id) {
		return c(cName).distinct("name", new BasicDBObject("_id", _id), String.class).first();
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
//		option.append("tooltip", new Document("trigger", "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", legendData).append("orient", "vertical").append("left", "right"));
		option.append("grid",
				new Document("left", "3%").append("right", "4%").append("bottom", "3%").append("containLabel", true));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", series);
		return option;
	}

}
