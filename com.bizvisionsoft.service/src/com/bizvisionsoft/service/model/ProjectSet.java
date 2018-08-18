package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
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
	private String id;

	@ImageURL("name")
	@Exclude
	private String icon = "/img/project_set_c.svg";


	/**
	 * 上级项目集Id
	 */
	@ReadValue
	@WriteValue
	private ObjectId parent_id;


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 描述属性
	/**
	 * 名称
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 描述
	 */
	@ReadValue
	@WriteValue
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "项目集";


	public ProjectSet setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	@Exclude
	private List<Object> chileren;

	@Structure("项目集管理 /list")
	public List<Object> getSubProjectSetsAndProjects() {
		if (chileren == null) {
			chileren = new ArrayList<Object>();

			chileren.addAll(ServicesLoader.get(ProjectSetService.class)
					.createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson()));

			chileren.addAll(ServicesLoader.get(ProjectService.class)
					.createDataSet(new Query().filter(new BasicDBObject("projectSet_id", _id)).bson()));
		}
		return chileren;
	}

	@Structure("项目集管理/count")
	public long countSubProjectSetsAndProjects() {
		// 查下级
		long cnt = ServicesLoader.get(ProjectService.class).count(new BasicDBObject("projectSet_id", _id));
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
		return cnt;
	}


	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

}
