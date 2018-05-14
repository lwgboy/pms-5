package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
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
import com.bizvisionsoft.service.model.Workspace;
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
	public Result checkout(Workspace workspace, String userId, Boolean cancelCheckoutSubSchedule) {
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		if (workspace.getWork_id() == null) {
			inputIds = c(Work.class)
					.distinct("_id", new BasicDBObject("project_id", workspace.getProject_id()), ObjectId.class)
					.into(new ArrayList<ObjectId>());
		} else {
			inputIds.add(workspace.getWork_id());
			inputIds = getDesentItems(inputIds, "work", "parent_id");
		}

		if (Boolean.TRUE.equals(cancelCheckoutSubSchedule)) {
			cleanWorkspace(workspace);
		} else {
			List<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIds))
					.append("checkoutBy", new BasicDBObject("$ne", null))));
			pipeline.add(Aggregates.lookup("user", "checkoutBy", "userId", "user"));
			pipeline.add(Aggregates.unwind("$user"));
			pipeline.add(Aggregates.project(new BasicDBObject("name", Boolean.TRUE).append("username", "$user.name")));

			BasicDBObject checkout = c("work").aggregate(pipeline, BasicDBObject.class).first();
			if (checkout != null) {
				Result result = Result.checkoutError("�ƻ����ڽ��мƻ��༭��", Result.CODE_HASCHECKOUTSUB);
				result.setResultDate(checkout);
				return result;
			}
		}

		ObjectId space_id = new ObjectId();

		if (workspace.getWork_id() == null) {
			c("project").updateOne(new BasicDBObject("_id", workspace.getProject_id()),
					new BasicDBObject("$set", new BasicDBObject("checkoutBy", userId).append("space_id", space_id)));
		}

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIds))));
		pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));
		List<Document> works = c("work").aggregate(pipeline).into(new ArrayList<Document>());
		if (works.size() > 0) {

			c("work").updateMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIds)),
					new BasicDBObject("$set", new BasicDBObject("checkoutBy", userId).append("space_id", space_id)));

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
		return Result.checkoutSuccess("����ɹ���");
	}

	@Override
	public Result schedulePlanCheck(Workspace workspace, Boolean checkManageItem) {
		// ��ȡ����Ľڵ㡣
		List<ObjectId> checkIds = new ArrayList<ObjectId>();
		if (checkManageItem) {
			checkIds.addAll(
					c(Work.class)
							.distinct("_id",
									new BasicDBObject("space_id", workspace.getSpace_id()).append("manageLevel",
											new BasicDBObject("$ne", null)),
									ObjectId.class)
							.into(new ArrayList<ObjectId>()));
		}

		if (workspace.getWork_id() != null) {
			checkIds.add(workspace.getWork_id());
		} else {
			checkIds.addAll(c(Work.class).distinct("_id",
					new BasicDBObject("project_id", workspace.getProject_id()).append("parent_id", null),
					ObjectId.class).into(new ArrayList<ObjectId>()));
		}

		// ���ݼ���㹹����ѯ���

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
		pipeline.add(Aggregates.unwind("$work"));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.project(new BasicDBObject("stage", Boolean.TRUE)
				.append("wpf", new BasicDBObject("$gt", new String[] { "planFinish", "$work.planFinish" }))
				.append("ppf", new BasicDBObject("$gt", new String[] { "planFinish", "$project.planFinish" }))));
		// ��ѯ�����������Ľڵ�����
		pipeline.add(Aggregates.match(new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("stage", Boolean.TRUE).append("ppf", Boolean.TRUE),
						new BasicDBObject("stage", Boolean.FALSE).append("wpf", Boolean.TRUE) })));
		WorkInfo workInfo = c(WorkInfo.class).aggregate(pipeline).first();
		if (workInfo != null) {
			return Result.checkoutError("����ڵ����ʱ�䳬���޶���", Result.CODE_UPDATEMANAGEITEM);
		}

		// ���ؼ����

		return Result.checkoutSuccess("��ͨ����顣");
	}

	@Override
	public Result checkin(Workspace workspace) {
		List<ObjectId> workIds = c(Work.class)
				.distinct("_id", new BasicDBObject("space_id", workspace.getSpace_id()), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		List<ObjectId> workspaceIds = c(WorkInfo.class)
				.distinct("_id", new BasicDBObject("space_id", workspace.getSpace_id()), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		// ��ȡ���뼯��
		List<ObjectId> insertIds = new ArrayList<ObjectId>();
		insertIds.addAll(workspaceIds);
		insertIds.removeAll(workIds);

		// ��ȡɾ������
		List<ObjectId> deleteIds = new ArrayList<ObjectId>();
		deleteIds.addAll(workIds);
		deleteIds.removeAll(workspaceIds);

		// ��ȡ�޸ļ���
		List<ObjectId> updateIds = new ArrayList<ObjectId>();
		updateIds.addAll(workspaceIds);
		updateIds.removeAll(insertIds);

		// ����ɾ������ɾ��Work
		c(Work.class).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", deleteIds)));

		// ���ݲ��뼯�ϲ���Work
		if (insertIds.size() > 0) {
			ArrayList<Document> insertDoc = c("workspace")
					.find(new BasicDBObject("_id", new BasicDBObject("$in", insertIds)))
					.into(new ArrayList<Document>());
			c("work").insertMany(insertDoc);
		}

		c("workspace").find(new BasicDBObject("_id", new BasicDBObject("$in", updateIds))).forEach((Document d) -> {
			Object _id = d.get("_id");
			d.remove("_id");
			d.remove("space_id");
			d.remove("checkoutBy");
			c("work").updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set", d));
		});

		// ��ȡworklinksspace�еļ�¼
		List<Document> worklinks = c("worklinksspace").find(new BasicDBObject("space_id", workspace.getSpace_id()))
				.into(new ArrayList<Document>());

		// ɾ��worklinks�еļ�¼
		c("worklinks").deleteMany(new BasicDBObject("source", new BasicDBObject("$in", workIds)).append("target",
				new BasicDBObject("$in", workIds)));

		// ����ȡ��worklinksspace�еļ�¼����worklinks
		if (worklinks.size() > 0) {
			c("worklinks").insertMany(worklinks);
		}

		if (Result.CODE_SUCCESS == cleanWorkspace(workspace).code) {
			return Result.checkoutSuccess("�ѳɹ��ύ��");
		} else {
			return Result.checkoutError("�ύʧ�ܡ�", Result.CODE_ERROR);
		}
	}

	@Override
	public Result cancelCheckout(Workspace workspace) {
		if (Result.CODE_SUCCESS == cleanWorkspace(workspace).code) {
			return Result.checkoutSuccess("�ѳɹ�������");
		} else {
			return Result.checkoutError("����ʧ�ܡ�", Result.CODE_ERROR);
		}
	}

	private Result cleanWorkspace(Workspace workspace) {
		c(WorkInfo.class).deleteMany(new BasicDBObject("space_id", workspace.getSpace_id()));

		c(WorkLinkInfo.class).deleteMany(new BasicDBObject("space_id", workspace.getSpace_id()));

		c("project").updateOne(new BasicDBObject("space_id", workspace.getSpace_id()),
				new BasicDBObject("$unset", new BasicDBObject("checkoutBy", null).append("space_id", null)));

		c("work").updateMany(new BasicDBObject("space_id", workspace.getSpace_id()),
				new BasicDBObject("$set", new BasicDBObject("checkoutBy", null).append("space_id", null)));

		return Result.checkoutSuccess("����ɳ����ɹ���");
	}
}
