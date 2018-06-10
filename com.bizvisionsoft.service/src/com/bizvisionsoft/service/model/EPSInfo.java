package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("eps")
public class EPSInfo implements Comparable<EPSInfo> {

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

	public ObjectId get_id() {
		return _id;
	}

	public EPSInfo setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	@Override
	public int compareTo(EPSInfo o) {
		return id.compareTo(o.id);
	}

	@Structure("EPS浏览-投资分析 /list")
	public List<EPSInfo> listSub() {
		return ServicesLoader.get(EPSService.class).listSubEPSInfo(_id);
	}

	@Structure("EPS浏览-投资分析/count")
	public long countSub() {
		return ServicesLoader.get(EPSService.class).countSubEPSInfo(_id);
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
