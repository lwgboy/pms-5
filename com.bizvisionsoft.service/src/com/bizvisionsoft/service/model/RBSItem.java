package com.bizvisionsoft.service.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.tools.Util;
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
	private int index;

	@ReadValue("id")
	private String getId() {
		return getRbsType().getId() + "." + String.format("%03d", index);
	}

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
	private double probability;

	@WriteValue("probability")
	private void setProbability(String _probability) {
		probability = Util.str_double(_probability, "发生概率影响必须输入浮点数。");
	}

	/**
	 * 后果描述
	 */
	@ReadValue
	@WriteValue
	private String result;

	/**
	 * 工期影响（天）
	 */
	private int timeInf;

	@WriteValue("timeInf")
	private void setTimeInf(String _timeInf) {
		timeInf = Util.str_int(_timeInf, "工期影响必须输入整数。");
	}

	@ReadValue("timeInf")
	private String getTimeInf() {
		if (timeInf > 0) {
			return "+" + timeInf;
		} else if (timeInf < 0) {
			return "" + timeInf;
		} else {
			return "";
		}
	}

	/**
	 * 成本影响（万元）
	 */
	private double costInf;

	@WriteValue("costInf")
	private void setCostInf(String _timeInf) {
		costInf = Util.str_double(_timeInf, "成本影响必须输入数值。");
	}

	@ReadValue("costInf")
	private String getCostInf() {
		if (costInf > 0) {
			return "+" + costInf;
		} else if (costInf < 0) {
			return "" + costInf;
		} else {
			return "";
		}
	}

	/**
	 * 质量影响（等级）
	 */
	@ReadValue
	@WriteValue
	private String qtyInf;

	/**
	 * 量值
	 */
	@ReadValue
	@WriteValue
	@Exclude
	private double infValue;

	/**
	 * 预期发生时间
	 */
	@ReadValue
	@WriteValue
	private Date forecast;

	/**
	 * 临近性
	 */
	@ReadValue("urgency")
	private String getUrgency() {
		long l = (forecast.getTime() - Calendar.getInstance().getTimeInMillis()) / (24 * 60 * 60 * 1000);
		if (l > 60) {
			return "远期";
		} else if (l > 30) {
			return "中期";
		} else if (l > 7) {
			return "近期";
		} else if (l > 0) {
			return "临近";
		} else {
			return "";
		}
	}

	/**
	 * 可探测性级别
	 */
	@ReadValue
	@WriteValue
	private String detectable;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "风险项";

	@Override
	@Label
	public String toString() {
		return name + " [" + getId() + "]";
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

	public RBSType getRbsType() {
		return rbsType;
	}

	public ObjectId getProject_id() {
		return project_id;
	}

	public RBSItem setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}
	
	public RBSItem setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public RBSItem setIndex(int index) {
		this.index = index;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

}
