package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;

public class WorkSpaceServiceImpl extends BasicServiceImpl implements WorkSpaceService {

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		Document doc = c("workspace").find(condition).sort(new BasicDBObject("index", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	public WorkInfo getWorkInfo(ObjectId _id) {
		return get(_id, WorkInfo.class);
	}

	@Override
	public List<WorkInfo> createTaskDataSet(BasicDBObject condition) {
		return c(WorkInfo.class).find(condition).into(new ArrayList<WorkInfo>());
	}

	@Override
	public List<WorkLinkInfo> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLinkInfo.class).find(condition).into(new ArrayList<WorkLinkInfo>());
	}

	@Override
	public List<Date> getPlanDateRange(ObjectId _id) {
		WorkInfo data = c(WorkInfo.class).find(new BasicDBObject("_id", _id)).first();
		ArrayList<Date> result = new ArrayList<Date>();
		result.add(data.getStart_date());
		result.add(data.getEnd_date());
		return result;
	}

	@Override
	public WorkInfo insertWork(WorkInfo work) {
		return insert(work, WorkInfo.class);
	}

	@Override
	public WorkLinkInfo insertLink(WorkLinkInfo link) {
		return insert(link, WorkLinkInfo.class);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkInfo.class);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkLinkInfo.class);
	}

	@Override
	public long deleteWork(ObjectId _id) {
		return delete(_id, WorkInfo.class);
	}

	@Override
	public long deleteLink(ObjectId _id) {
		return delete(_id, WorkLinkInfo.class);
	}

	@Override
	public String getCheckOutUserId(ObjectId _id) {
		return c("work").distinct("checkOutBy", new BasicDBObject("_id", _id), String.class).first();
	}

	@Override
	public ObjectId getSpaceId(ObjectId _id) {
		return c("work").distinct("space_id", new BasicDBObject("_id", _id), ObjectId.class).first();
	}

	@Override
	public Result checkOutSchedulePlan(BasicDBObject wbsScope, String userId, boolean cancelCheckOutSubSchedule) {
		ObjectId project_id = wbsScope.getObjectId("project_id");
		ObjectId work_id = wbsScope.getObjectId("work_id");
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		if (work_id == null) {
			inputIds = c(Work.class).distinct("_id", new BasicDBObject("project_id", project_id), ObjectId.class)
					.into(new ArrayList<ObjectId>());
		} else {
			inputIds.add(work_id);
			inputIds = getDesentItems(inputIds, "work", "parent_id");
		}

		if (cancelCheckOutSubSchedule) {
			cancelCheckOutSchedulePlan(project_id, inputIds, userId);
		} else {
			long count = c("work").count(new BasicDBObject("_id", new BasicDBObject("$in", inputIds))
					.append("checkOutBy", new BasicDBObject("$ne", null)));
			if (count > 0) {
				return Result.checkOutSchedulePlanError("下级进度已被检出进行编辑。", Result.CODE_HASCHECKOUTSUB);
			}
		}

		ObjectId space_id = new ObjectId();

		if (work_id == null) {
			c("project").updateOne(new BasicDBObject("_id", project_id),
					new BasicDBObject("$set", new BasicDBObject("checkOutBy", userId).append("space_id", space_id)));
		}

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIds))));
		pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));
		List<Document> works = c("work").aggregate(pipeline).into(new ArrayList<Document>());
		if (works.size() > 0) {

			c("work").updateMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIds)),
					new BasicDBObject("$set", new BasicDBObject("checkOutBy", userId).append("space_id", space_id)));

			c("workspace").insertMany(works);

			pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(new BasicDBObject("source", new BasicDBObject("$in", inputIds))
					.append("target", new BasicDBObject("$in", inputIds))));
			pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));

			List<Document> workLinkInfos = c("worklinks").aggregate(pipeline).into(new ArrayList<Document>());

			if (workLinkInfos.size() > 0) {
				c("worklinksspace").insertMany(workLinkInfos);
			}
		}
		return Result.checkOutSchedulePlanSuccess("检出成功。");
	}

	@Override
	public Result schedulePlanCheck(BasicDBObject wbsScope, String userId, Boolean checkManageItem) {
		// 获取需检查的节点。
		ObjectId project_id = wbsScope.getObjectId("project_id");
		ObjectId work_id = wbsScope.getObjectId("work_id");
		ObjectId space_id = wbsScope.getObjectId("space_id");
		List<ObjectId> checkIds = new ArrayList<ObjectId>();
		if (checkManageItem) {
			checkIds.addAll(
					c(Work.class)
							.distinct("_id",
									new BasicDBObject("space_id", space_id).append("manageLevel",
											new BasicDBObject("$ne", null)),
									ObjectId.class)
							.into(new ArrayList<ObjectId>()));
		}

		if (work_id != null) {
			checkIds.add(work_id);
		} else {
			checkIds.addAll(
					c(Work.class).distinct("_id", new BasicDBObject("project_id", project_id).append("parent_id", null),
							ObjectId.class).into(new ArrayList<ObjectId>()));
		}

		// 根据检查检点构建查询语句

		/**
		 * { "$match" : { "_id" : { "$in" : [] } } }, { "$lookup" : { "from" : "work",
		 * "localField" : "_id", "foreignField" : "_id", "as" : "work" } }, { "$unwind"
		 * : "$work" }, { "$lookup" : { "from" : "project", "localField" : "project_id",
		 * "foreignField" : "_id", "as" : "project" }}, { "$unwind" : "$project" }, {
		 * "$project" : { "fullName" : true, "stage" : true, "project_id" : true, "name"
		 * : true, "parent_id" : true, "planFinish" : true, "wpf" : { "$gt" : [
		 * "$planFinish", "$work.planFinish" ] }, "ppf" : { "$gt" : [ "$planFinish",
		 * "$project.planFinish" ] } } },
		 * 
		 * { "$match" : { "$or" : [ { "stage" : true, "ppf" : true }, { "stage" : false,
		 * "wpf" : true } ] } }
		 **/

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject("$in", checkIds))));
		pipeline.add(Aggregates.lookup("work", "_id", "_id", "work"));
		pipeline.add(Aggregates.unwind("work"));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("project"));
		pipeline.add(Aggregates.project(new BasicDBObject("stage", Boolean.TRUE)
				.append("wpf", new BasicDBObject("$gt", new String[] { "$planFinish", "$work.planFinish" }))
				.append("ppf", new BasicDBObject("$gt", new String[] { "$planFinish", "$project.planFinish" }))));
		// 查询不满足条件的节点数量
		pipeline.add(Aggregates.match(new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("stage", Boolean.TRUE).append("ppf", Boolean.TRUE),
						new BasicDBObject("stage", Boolean.FALSE).append("wpf", Boolean.TRUE) })));
		WorkInfo workInfo = c(WorkInfo.class).aggregate(pipeline).first();
		if (workInfo != null) {
			return Result.checkOutSchedulePlanError("检查不通过，管理节点完成时间超过限定。", Result.CODE_UPDATEMANAGEITEM);
		}

		// 返回检查结果

		return Result.checkOutSchedulePlanSuccess("检查完成。");
	}

	@Override
	public Result checkInSchedulePlan(BasicDBObject wbsScope, String userId) {
		// TODO Auto-generated method stub
		return Result.checkOutSchedulePlanSuccess("检入成功。");
	}

	@Override
	public Result cancelCheckOutSchedulePlan(BasicDBObject wbsScope, String userId) {
		ObjectId project_id = wbsScope.getObjectId("project_id");
		ObjectId work_id = wbsScope.getObjectId("work_id");
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		if (work_id == null) {
			inputIds = c(Work.class).distinct("_id", new BasicDBObject("project_id", project_id), ObjectId.class)
					.into(new ArrayList<ObjectId>());
		} else {
			inputIds.add(work_id);
			inputIds = getDesentItems(inputIds, "work", "parent_id");
		}

		return cancelCheckOutSchedulePlan(project_id, inputIds, userId);

	}

	private Result cancelCheckOutSchedulePlan(ObjectId project_id, List<ObjectId> inputIds, String userId) {
		c(WorkInfo.class).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIds)));

		c(WorkLinkInfo.class).deleteMany(new BasicDBObject("source", new BasicDBObject("$in", inputIds))
				.append("target", new BasicDBObject("$in", inputIds)));

		c("project").updateOne(new BasicDBObject("_id", project_id),
				new BasicDBObject("$unset", new BasicDBObject("checkOutBy", 1).append("space_id", 1)));

		c("work").updateMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIds)),
				new BasicDBObject("$set", new BasicDBObject("checkOutBy", 1).append("space_id", 1)));

		return Result.checkOutSchedulePlanSuccess("撤销检出成功。");
	}
}
