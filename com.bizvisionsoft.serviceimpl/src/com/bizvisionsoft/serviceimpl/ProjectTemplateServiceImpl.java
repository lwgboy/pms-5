package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class ProjectTemplateServiceImpl extends BasicServiceImpl implements ProjectTemplateService {

	@Override
	public ProjectTemplate insert(ProjectTemplate prjt) {
		return insert(prjt, ProjectTemplate.class);
	}

	@Override
	public ProjectTemplate get(ObjectId _id) {
		return get(_id, ProjectTemplate.class);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, ProjectTemplate.class);
	}

	@Override
	public List<ProjectTemplate> createDataSet(BasicDBObject condition) {
		return createDataSet(condition, ProjectTemplate.class);
	}

	@Override
	public WorkInTemplate getWorkInTemplate(ObjectId work_id) {
		return get(work_id, WorkInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWorks(ObjectId template_id) {
		return c(WorkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public List<WorkLinkInTemplate> listLinks(ObjectId template_id) {
		return c(WorkLinkInTemplate.class).find(new BasicDBObject("template_id", template_id))
				.into(new ArrayList<WorkLinkInTemplate>());
	}

	@Override
	public WorkInTemplate insertWork(WorkInTemplate work) {
		return insert(work);
	}

	@Override
	public long updateWork(BasicDBObject bson) {
		return update(bson, WorkInTemplate.class);
	}

	@Override
	public long deleteWork(ObjectId work_id) {
		return delete(work_id, WorkInTemplate.class);
	}

	@Override
	public WorkLinkInTemplate insertLink(WorkLinkInTemplate link) {
		return insert(link);
	}

	@Override
	public long updateLink(BasicDBObject bson) {
		return update(bson, WorkLinkInTemplate.class);
	}

	@Override
	public long deleteLink(ObjectId link_id) {
		return delete(link_id, WorkLinkInTemplate.class);
	}

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		Document doc = c("workInTemplate").find(condition).sort(new BasicDBObject("index", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	@Override
	public int nextOBSSeq(BasicDBObject condition) {
		Document doc = c("obsInTemplate").find(condition).sort(new BasicDBObject("seq", -1))
				.projection(new BasicDBObject("seq", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("seq", 0)).orElse(0) + 1;
	}

	@Override
	public List<OBSInTemplate> getOBSTemplate(ObjectId template_id) {
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id));
	}

	private List<OBSInTemplate> queryOBSTemplate(BasicDBObject match) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(match));

		appendUserInfoAndHeadPic(pipeline, "managerId", "managerInfo", "managerHeadPic");

		appendSortBy(pipeline, "seq", 1);

		return c(OBSInTemplate.class).aggregate(pipeline).into(new ArrayList<OBSInTemplate>());
	}

	@Override
	public boolean hasOBS(ObjectId _id) {
		return c("obsInTemplate").count(new BasicDBObject("scope_id", _id)) > 0;
	}

	@Override
	public OBSInTemplate createRootOBS(ObjectId _id) {
		OBSInTemplate obsRoot = new OBSInTemplate()// ��������Ŀ��OBS���ڵ�
				.set_id(new ObjectId())// ����_id����Ŀ����
				.setScope_id(_id)// ����scope_id��������֯�ڵ��Ǹ���Ŀ����֯
				.setParent_id(null)// �����ϼ���id
				.setName("��Ŀ��")// ���ø���֯�ڵ��Ĭ������
				.setRoleId(OBSItem.ID_PM)// ���ø���֯�ڵ�Ľ�ɫid
				.setRoleName(OBSItem.NAME_PM)// ���ø���֯�ڵ������
				.setScopeRoot(true);// ��������ڵ��Ƿ�Χ�ڵĸ��ڵ�
		return insert(obsRoot);// �����¼
	}

	@Override
	public OBSInTemplate insertOBSItem(OBSInTemplate t) {
		return insert(t);
	}

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, OBSInTemplate.class);
	}

	@Override
	public List<WorkInTemplate> listWBSRoot(ObjectId template_id) {
		List<? extends Bson> pipeline = new JQ("�����Ŀģ��WBS���ڵ�").set("template_id", template_id)
				.set("parent_id", null).buildArray();
		return c(WorkInTemplate.class).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSRoot(ObjectId template_id) {
		return count(new BasicDBObject("template_id", template_id).append("parent_id", null), "workInTemplate");
	}

	@Override
	public List<WorkInTemplate> listWBSChildren(ObjectId parent_id) {
		List<? extends Bson> pipeline = new JQ("�����Ŀģ��WBS�¼��ڵ�").set("parent_id", parent_id).buildArray();
		return c(WorkInTemplate.class).aggregate(pipeline).into(new ArrayList<WorkInTemplate>());
	}

	@Override
	public long countWBSChildren(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "workInTemplate");
	}

	@Override
	public List<OBSInTemplate> getOBSRoleTemplate(ObjectId template_id) {
		return queryOBSTemplate(new BasicDBObject("scope_id", template_id).append("isRole", true));
	}

}
