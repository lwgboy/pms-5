package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;

public class WorkServiceImpl extends BasicServiceImpl implements WorkService {

	@Override
	public List<WorkInfo> createTaskDataSet(BasicDBObject condition) {
		// TODO
		return c(WorkInfo.class).find(condition).into(new ArrayList<WorkInfo>());
	}

	@Override
	public List<WorkLinkInfo> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLinkInfo.class).find(condition).into(new ArrayList<WorkLinkInfo>());
	}

	@Override
	public List<WorkInfo> listProjectRootTask(ObjectId project_id) {
		return createTaskDataSet(new BasicDBObject("project_id", project_id).append("parent_id", null));
	}

	@Override
	public long countProjectRootTask(ObjectId project_id) {
		return count(new BasicDBObject("project_id", project_id).append("parent_id", null), "work");
	}

	@Override
	public List<WorkInfo> listChildren(ObjectId parent_id) {
		return createTaskDataSet(new BasicDBObject("parent_id", parent_id));
	}

	@Override
	public long countChildren(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "work");
	}

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		Document doc = c("work").find(condition).sort(new BasicDBObject("index", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
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
	public WorkInfo getWork(ObjectId _id) {
		return get(_id, WorkInfo.class);
	}

	@Override
	public WorkLinkInfo getLink(ObjectId _id) {
		return get(_id, WorkLinkInfo.class);
	}

	@Override
	public List<Result> start(ObjectId _id, String executeBy) {
		List<Result> result = startWorkCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改状态
		UpdateResult ur = c(WorkInfo.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Processing)
						.append("actualStart", new Date()).append("startBy", executeBy)));

		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("没有满足启动条件的工作。");
		}

		ObjectId project_id = c("work").distinct("project_id", new BasicDBObject("_id", _id), ObjectId.class).first();
		ur = c(Project.class).updateOne(new BasicDBObject("_id", project_id),
				new BasicDBObject("$set", new BasicDBObject("stage_id", _id)));
		// 根据ur构造下面的结果
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("无法更新项目当前状态。");
		}

		// TODO 通知团队成员，工作已经启动

		return result;
	}

	private List<Result> startWorkCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// 须检查的信息
		// 1. 检查是否创建了第一层的WBS，并有计划，如果没有，提示警告
		// 2. 检查组织结构是否完成，如果只有根，警告
		// 3. 检查第一层的WBS是否指定了必要的角色，如果没有负责人角色，提示警告。
		// 4. 缺少干系人，警告
		// 5. 没有做预算，警告
		// 6. 预算没做完，警告
		// 7. 预算没有分配，警告

		return new ArrayList<Result>();
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
	public Result checkOutSchedulePlan(ObjectId _id, String userId, boolean cancelCheckOutSubSchedule) {
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		inputIds.add(_id);
		inputIds = getDesentItems(inputIds, "work", "parent_id");

		if (cancelCheckOutSubSchedule) {
			c("workspace", WorkInfo.class).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIds)));

			c("worklinksspace", WorkLinkInfo.class)
					.deleteMany(new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target",
							new BasicDBObject("$in", inputIds)));
		} else {
			long count = c("work").count(new BasicDBObject("_id", new BasicDBObject("$in", inputIds))
					.append("checkOutBy", new BasicDBObject("$ne", null)));
			if (count > 0) {
				return Result.checkOutSchedulePlan("", Result.TYPE_HASCHECKOUTSUB);
			}
		}

		List<Document> works = c("work").find(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIds)))
				.into(new ArrayList<Document>());
		if (works != null && works.size() > 0) {
			c(WorkInfo.class).updateMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIds)),
					new BasicDBObject("$set", new BasicDBObject("checkOutBy", userId)));

			c("workspace").insertMany(works);
			
			List<Document> workLinkInfos = c("worklinks")
					.find(new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target",
							new BasicDBObject("$in", inputIds)))
					.into(new ArrayList<Document>());

			if (workLinkInfos != null && workLinkInfos.size() > 0) {
				c("worklinksspace").insertMany(workLinkInfos);
			}
		}
		return Result.checkOutSchedulePlan("检出成功。", Result.TYPE_SUCCESS);
	}

	@Override
	public List<WorkInfo> createTaskDataSetBySpace(BasicDBObject condition) {
		return c("workspace", WorkInfo.class).find(condition).into(new ArrayList<WorkInfo>());
	}

	@Override
	public List<WorkLinkInfo> createLinkDataSetBySpace(BasicDBObject condition) {
		return c("worklinksspace", WorkLinkInfo.class).find(condition).into(new ArrayList<WorkLinkInfo>());
	}

	@Override
	public List<Date> getPlanDateRangeBySpace(ObjectId _id) {
		WorkInfo data = c("workspace", WorkInfo.class).find(new BasicDBObject("_id", _id)).first();
		ArrayList<Date> result = new ArrayList<Date>();
		result.add(data.getStart_date());
		result.add(data.getEnd_date());
		return result;
	}

	@Override
	public WorkInfo insertWorkBySpace(WorkInfo work) {
		return insert(work, "workspace", WorkInfo.class);
	}

	@Override
	public WorkLinkInfo insertLinkBySpace(WorkLinkInfo link) {
		return insert(link, "worklinksspace", WorkLinkInfo.class);
	}

	@Override
	public long updateWorkBySpace(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "workspace", WorkInfo.class);
	}

	@Override
	public long updateLinkBySpace(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, "worklinksspace", WorkLinkInfo.class);
	}

	@Override
	public long deleteWorkBySpace(ObjectId _id) {
		return delete(_id, "workspace", WorkInfo.class);
	}

	@Override
	public long deleteLinkBySpace(ObjectId _id) {
		return delete(_id, "worklinksspace", WorkLinkInfo.class);
	}

	@Override
	public String getCheckOutUserId(ObjectId _id) {
		return getWork(_id).getCheckOutUserId();
	}
}
