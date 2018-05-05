package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("projectSet")
public class ProjectSet {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/**
	 * 编号
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String id;
	
	@ImageURL("id")
	private String logo = "/img/project_set_c.svg";

	/**
	 * 工作令号
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String workOrder;

	/**
	 * 上级项目集Id
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId parent_id;

	/**
	 * EPS节点Id
	 */
	@Persistence
	private ObjectId eps_id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 描述属性
	/**
	 * 名称
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String name;

	/**
	 * 描述
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "项目集";

	@Persistence
	private ObjectId obs_id;

	public ProjectSet setEps_id(ObjectId eps_id) {
		this.eps_id = eps_id;
		return this;
	}

	public ProjectSet setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	@Structure("EPS浏览 /list")
	public List<Object> getSubProjectSetsAndProjects() {
		ArrayList<Object> result = new ArrayList<Object>();

		result.addAll(ServicesLoader.get(ProjectSetService.class)
				.createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson()));

		result.addAll(ServicesLoader.get(ProjectService.class)
				.createDataSet(new Query().filter(new BasicDBObject("projectSet_id", _id)).bson()));

		return result;
	}

	@Structure("EPS浏览/count")
	public long countSubProjectSetsAndProjects() {
		// 查下级
		long cnt = ServicesLoader.get(ProjectService.class).count(new BasicDBObject("projectSet_id", _id));
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
		return cnt;
	}

	@Structure("EPS和项目集选择 /list")
	public List<ProjectSet> getSubProjectSets() {
		return ServicesLoader.get(ProjectSetService.class)
				.createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson());
	}

	@Structure("EPS和项目集选择/count")
	public long countSubProjectSets() {
		return ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
	}

	@Behavior("EPS浏览/编辑项目集") // 控制action
	private boolean enableEdit() {
		return true;//TODO 考虑权限 
	}

	@Behavior("EPS浏览/创建项目集") // 控制action
	private boolean enableAdd() {
		return true;//TODO 考虑权限
	}

	@Behavior("EPS浏览/删除项目集") // 控制action
	private boolean enableDelete() {
		return true;//TODO 考虑权限
	}

	@Behavior("EPS浏览/打开") // 控制action
	private boolean enableOpen() {
		return true;//TODO 考虑权限
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public ObjectId getOBS_id() {
		return obs_id;
	}

}
