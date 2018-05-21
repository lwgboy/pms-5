package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("rbsItem")
public class RBSItem {

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** 项目_Id **/
	@ReadValue
	@WriteValue
	private ObjectId project_id;

	/** 上级风险标识，如果有该标识，本风向项是次生风险 **/
	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	/** 编号 Y **/
	@ReadValue
	@WriteValue
	private String id;

	/**
	 * 风险名称
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 风险描述
	 */
	@ReadValue
	@WriteValue
	private String description;

	/**
	 * 阶段，这个字段是描述性的。不必和工作关联，但可选取阶段的名称
	 */
	@ReadValue
	@WriteValue
	private String stage;

	/**
	 * 风险类型
	 */
	@ReadValue
	@WriteValue
	private RBSType rbsType;

	/**
	 * 发生概率
	 */
	@ReadValue
	@WriteValue
	private double probability;

	/**
	 * 后果描述
	 */
	@ReadValue
	@WriteValue
	private String result;

	/**
	 * 工期影响（天）
	 */
	@ReadValue
	@WriteValue
	private int timeInf;

	/**
	 * 成本影响（万元）
	 */
	@ReadValue
	@WriteValue
	private int costInf;

	/**
	 * 质量影响（等级）
	 */
	@ReadValue
	@WriteValue
	private int qtyInf;

	/**
	 * 量值
	 */
	@ReadValue
	@WriteValue
	private double infValue;

	/**
	 * 风险关键指数
	 */
	@ReadValue
	@SetValue
	private double rci;

	/**
	 * 预期发生时间
	 */
	@ReadValue
	@WriteValue
	private Date forecast;

	/**
	 * 临近性
	 */
	@ReadValue
	@SetValue
	private int urgency;

	/**
	 * 可探测性级别
	 */
	@ReadValue
	@WriteValue
	private int detectable;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "风险项";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@Structure({ "项目风险清单/list" })
	private List<RBSItem> listSecondryRisks() {
		return ServicesLoader.get(RiskService.class).listRBSItem(
				new Query().filter(new BasicDBObject("project_id", project_id).append("parent_id", _id)).bson());
	}

	@Structure({ "项目风险清单/count" })
	private long countSecondryRisks() {
		return ServicesLoader.get(RiskService.class)
				.countRBSItem(new BasicDBObject("project_id", project_id).append("parent_id", _id));
	}
}
