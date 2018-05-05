package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("eps")
public class EPS implements Comparable<EPS> {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	private ObjectId _id;

	/**
	 * 编号
	 */
	@ReadValue
	@WriteValue
	private String id;

	@ImageURL("id")
	private String logo = "/img/eps_c.svg";
	/**
	 * 父节点
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

	@Behavior({"EPS浏览/创建项目集","项目模板管理/创建项目模板"}) // 控制action
	@Exclude // 不用持久化
	private boolean enabledBehaviors = true;

	public ObjectId get_id() {
		return _id;
	}

	public EPS setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	@Override
	public int compareTo(EPS o) {
		return id.compareTo(o.id);
	}

	@Structure("EPS管理/list")
	public List<EPS> listSubEPS() {
		return ServicesLoader.get(EPSService.class).getSubEPS(_id);
	}

	@Structure("EPS管理/count")
	public long countSubEPS() {
		return ServicesLoader.get(EPSService.class).countSubEPS(_id);
	}

	@Structure("EPS浏览 /list")
	public List<Object> listSubNodes() {
		ArrayList<Object> result = new ArrayList<Object>();

		result.addAll(ServicesLoader.get(EPSService.class).getSubEPS(_id));

		result.addAll(ServicesLoader.get(ProjectSetService.class)
				.createDataSet(new Query().filter(new BasicDBObject("eps_id", _id)).bson()));

		result.addAll(ServicesLoader.get(ProjectService.class)
				.createDataSet(new Query().filter(new BasicDBObject("eps_id", _id)).bson()));

		return result;
	}

	@Structure("EPS浏览/count")
	public long countSubNodes() {
		// 查下级
		long cnt = ServicesLoader.get(EPSService.class).countSubEPS(_id);
		cnt += ServicesLoader.get(ProjectService.class).count(new BasicDBObject("eps_id", _id));
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("eps_id", _id));
		return cnt;
	}

	@Structure("项目模板管理 /list")
	public List<Object> listSubNodesForProjectTemplate() {
		ArrayList<Object> result = new ArrayList<Object>();

		result.addAll(ServicesLoader.get(EPSService.class).getSubEPS(_id));

		result.addAll(ServicesLoader.get(ProjectTemplateService.class)
				.createDataSet(new Query().filter(new BasicDBObject("eps_id", _id)).bson()));

		return result;
	}

	@Structure("项目模板管理/count")
	public long countSubNodesForProjectTemplate() {
		// 查下级
		long cnt = ServicesLoader.get(EPSService.class).countSubEPS(_id);
		cnt += ServicesLoader.get(ProjectTemplateService.class).count(new BasicDBObject("eps_id", _id));
		return cnt;
	}

	@Structure("EPS和项目集选择 /list")
	public List<Object> listSubEPSAndProjectSets() {
		ArrayList<Object> result = new ArrayList<Object>();

		result.addAll(ServicesLoader.get(EPSService.class).getSubEPS(_id));

		result.addAll(ServicesLoader.get(ProjectSetService.class)
				.createDataSet(new Query().filter(new BasicDBObject("eps_id", _id)).bson()));

		return result;

	}

	@Structure("EPS和项目集选择/count")
	public long countSubEPSAndProjectSets() {
		// 查下级
		long cnt = ServicesLoader.get(EPSService.class).countSubEPS(_id);
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("eps_id", _id));
		return cnt;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "EPS";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

}
