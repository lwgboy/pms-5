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

	@ImageURL("name")
	@Exclude
	private String icon = "/img/eps_c.svg";
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

	@Behavior({ "项目模板管理/创建项目模板" }) // 控制action
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
	
	public String domain;

	@Structure({"EPS管理/list", "EPS选择/list"})
	public List<EPS> listSubEPS() {
		return ServicesLoader.get(EPSService.class).getSubEPS(_id,domain);
	}

	@Structure({"EPS管理/count", "EPS选择/count"})
	public long countSubEPS() {
		return ServicesLoader.get(EPSService.class).countSubEPS(_id,domain);
	}

	@Structure("EPS浏览 /list")
	public List<Object> listSubNodes() {
		ArrayList<Object> result = new ArrayList<Object>();

		result.addAll(ServicesLoader.get(EPSService.class).getSubEPS(_id, domain));

		result.addAll(ServicesLoader.get(ProjectService.class).list(
				new Query().filter(new BasicDBObject("eps_id", _id)).bson(), domain));

		return result;
	}

	@Structure("EPS浏览/count")
	public long countSubNodes() {
		// 查下级
		long cnt = ServicesLoader.get(EPSService.class).countSubEPS(_id, domain);
		cnt += ServicesLoader.get(ProjectService.class).count(new BasicDBObject("eps_id", _id), domain);
		return cnt;
	}

	@Structure("EPS浏览-投资分析 /list")
	public List<Object> listFinishSubNodes() {
		ArrayList<Object> result = new ArrayList<Object>();

		result.addAll(ServicesLoader.get(EPSService.class).getSubEPS(_id, domain));

		result.addAll(ServicesLoader.get(ProjectService.class).list(new Query().filter(
				new BasicDBObject("eps_id", _id).append("status", ProjectStatus.Closed))
				.bson(), domain));

		return result;
	}

	@Structure("EPS浏览-投资分析/count")
	public long countFinishSubNodes() {
		// 查下级
		long cnt = ServicesLoader.get(EPSService.class).countSubEPS(_id, domain);
		cnt += ServicesLoader.get(ProjectService.class).count(
				new BasicDBObject("eps_id", _id).append("status", ProjectStatus.Closed), domain);
		return cnt;
	}
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "EPS";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public String getName() {
		return name;
	}

}
