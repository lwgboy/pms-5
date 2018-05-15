package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.result.UpdateResult;

public class WorkServiceImpl extends BasicServiceImpl implements WorkService {

	@Override
	public List<Work> createTaskDataSet(BasicDBObject condition) {
		// TODO
		return c(Work.class).find(condition).into(new ArrayList<Work>());
	}

	@Override
	public List<WorkLink> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLink.class).find(condition).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> listProjectRootTask(ObjectId project_id) {
		return createTaskDataSet(new BasicDBObject("project_id", project_id).append("parent_id", null));
	}

	@Override
	public long countProjectRootTask(ObjectId project_id) {
		return count(new BasicDBObject("project_id", project_id).append("parent_id", null), "work");
	}

	@Override
	public List<Work> listChildren(ObjectId parent_id) {
		return createTaskDataSet(new BasicDBObject("parent_id", parent_id));
	}

	@Override
	public long countChildren(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "work");
	}

	@Override
	public Work insertWork(Work work) {
		return insert(work, Work.class);
	}

	@Override
	public WorkLink insertLink(WorkLink link) {
		return insert(link, WorkLink.class);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Work.class);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkLink.class);
	}

	@Override
	public long deleteWork(ObjectId _id) {
		return delete(_id, Work.class);
	}

	@Override
	public long deleteLink(ObjectId _id) {
		return delete(_id, WorkLink.class);
	}

	@Override
	public Work getWork(ObjectId _id) {
		return get(_id, Work.class);
	}

	@Override
	public WorkLink getLink(ObjectId _id) {
		return get(_id, WorkLink.class);
	}

	@Override
	public List<Result> start(ObjectId _id, String executeBy) {
		List<Result> result = startWorkCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// 修改状态
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", _id),
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
	public Workspace getWorkspace(ObjectId _id) {
		BasicDBObject dbo = c("work").find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)
						.append("project_id", Boolean.TRUE))
				.first();
		return Workspace.newInstance(dbo.getObjectId("project_id"), _id, dbo.getObjectId("space_id"),
				dbo.getString("checkoutBy"));
	}

	@Override
	public List<WorkLink> createWorkLinkDataSet(ObjectId parent_id) {
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		inputIds.add(parent_id);
		getDesentItems(inputIds, "work", "parent_id");
		return c(WorkLink.class).find(new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target",
				new BasicDBObject("$in", inputIds))).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createWorkTaskDataSet(ObjectId parent_id) {
		List<Work> result = new ArrayList<Work>();
		c(Work.class).find(new BasicDBObject("parent_id", parent_id)).sort(new BasicDBObject("index", 1))
				.forEach(new Block<Work>() {
					@Override
					public void apply(final Work work) {
						result.add(work);
						result.addAll(createWorkTaskDataSet(work.get_id()));
					}
				});
		return result;
	}

	@Override
	public List<WorkLink> createProjectLinkDataSet(ObjectId project_id) {
		return c(WorkLink.class).find(new BasicDBObject("project_id", project_id)).sort(new BasicDBObject("index", 1))
				.into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createProjectTaskDataSet(ObjectId project_id) {
		return c(Work.class).find(new BasicDBObject("project_id", project_id)).sort(new BasicDBObject("index", 1))
				.into(new ArrayList<Work>());
	}
}
