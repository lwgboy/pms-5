package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.mongodb.BasicDBObject;

@PersistenceCollection("projectSet")
public class ProjectSet {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ʶ����
	/**
	 * _id
	 */
	@ReadValue
	@WriteValue
	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	/**
	 * ���
	 */
	@ReadValue
	@WriteValue
	private String id;

	/**
	 * �ϼ���Ŀ��Id
	 */
	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	public ProjectSet setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��������
	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	private String description;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ��Ŀ������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String pgmId;

	@SetValue
	@ReadValue
	private String pgmInfo;

	@SetValue
	private UserMeta pgmInfo_meta;

	@ReadValue("pgmInfoHtml")
	private String readPMInfoHtml() {
		if (pgmId == null) {
			return "";
		}
		return "<div style='cursor:pointer;display:inline-flex;width: 100%;justify-content: space-between;'>"
				+ MetaInfoWarpper.userInfo(pgmInfo_meta, pgmInfo) + "</div>";
	}

	@WriteValue("pgm")
	private void setPGM(User pgm) {
		if (pgm == null) {
			this.pgmId = null;
			this.pgmInfo_meta = null;
			this.pgmInfo = "";
		} else {
			this.pgmId = pgm.getUserId();
			this.pgmInfo = pgm.getName();
		}
	}

	@ReadValue("pgm")
	private User getPGM() {
		return Optional.ofNullable(pgmId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Persistence
	@WriteValue
	@ReadValue
	private OperationInfo creationInfo;

	@ReadValue("createOn")
	private Date readCreateOn() {
		return Optional.ofNullable(creationInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("createByInfo")
	private String readCreateBy() {
		return Optional.ofNullable(creationInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("createByConsignerInfo")
	private String readCreateByConsigner() {
		return Optional.ofNullable(creationInfo).map(c -> c.consignerName).orElse(null);
	}

	@Structure("��Ŀ������ /list")
	public List<Object> getSubProjectSetsAndProjects() {
		ArrayList<Object> children = new ArrayList<Object>();

		children.addAll(ServicesLoader.get(ProjectSetService.class)
				.list(new Query().filter(new BasicDBObject("parent_id", _id)).bson()));

		children.addAll(ServicesLoader.get(ProjectService.class)
				.list(new Query().filter(new BasicDBObject("projectSet_id", _id)).bson()));
		return children;
	}

	@Structure("��Ŀ������/count")
	public long countSubProjectSetsAndProjects() {
		// ���¼�
		long cnt = ServicesLoader.get(ProjectService.class).count(new BasicDBObject("projectSet_id", _id));
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
		return cnt;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "��Ŀ��";

	@ImageURL("name")
	@Exclude
	private String icon = "/img/project_set_c.svg";

	public void setCreationInfo(OperationInfo operationInfo) {
		creationInfo = operationInfo;
	}

}
