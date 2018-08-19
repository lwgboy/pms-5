package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadEditorConfig;
import com.bizvisionsoft.annotations.md.service.ReadOptions;
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
	
	/**
	 * 风险的状态
	 */
	@ReadValue
	@WriteValue
	private String status;

	/** 编号 Y **/
	private int index;

	@ReadValue("id")
	public String getId() {
		return getRbsType().getId() + "." + String.format("%03d", index);
	}

	@Behavior("RBSItem操作")
	@Exclude
	private boolean rbsAction = true;

	@ReadEditorConfig("项目风险登记簿/编辑")
	private Object getEditorConfig() {
		return "风险项编辑器";
	}

	@ReadValue("title")
	private String getDisplayName() {
		return getId() + " " + name;
	}

	@ReadValue
	private OperationInfo creationInfo;

	/**
	 * 风险名称
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 风险描述
	 */
	@WriteValue
	@ReadValue({ "description", "项目风险登记簿/desc", "项目风险登记簿（查看）/desc" })
	private String description;
	
	public String getDescription() {
		return description;
	}

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

	public String getResult() {
		return result;
	}

	/**
	 * 工期影响（天）
	 */
	private int timeInf;

	@WriteValue("timeInf")
	private void setTimeInf(String _timeInf) {
		timeInf = Util.str_int(_timeInf, "工期影响必须输入整数。");
	}

	@ReadValue("timeInf")
	private String readTimeInf() {
		if (timeInf > 0) {
			return "+" + timeInf;
		} else if (timeInf < 0) {
			return "" + timeInf;
		} else {
			return "";
		}
	}

	public int getTimeInf() {
		return timeInf;
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
	private String readCostInf() {
		if (costInf > 0) {
			return "+" + costInf;
		} else if (costInf < 0) {
			return "" + costInf;
		} else {
			return "";
		}
	}

	public double getCostInf() {
		return costInf;
	}

	/**
	 * 质量影响（等级）
	 */
	@ReadValue
	@WriteValue
	private String qtyInf;

	public String getQtyInf() {
		return qtyInf;
	}

	/**
	 * 量值评分
	 */
	@ReadValue
	private double infValue;

	public void setInfValue(double infValue) {
		this.infValue = infValue;
	}

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
		return ServicesLoader.get(RiskService.class).getUrgencyText(l);
	}

	/**
	 * 可探测性级别
	 */
	@ReadValue
	@WriteValue
	private String detectable;

	/**
	 * 风险序数 rci x 概率
	 */
	@ReadValue("rci")
	private double getRCI() {
		return infValue * probability;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "风险项";

	@Override
	@Label
	public String toString() {
		return name + " [" + getId() + "]";
	}

	@Structure({ "项目风险登记簿/list", "项目风险登记簿（查看）/list" })
	private List<Object> listSecondryRisksNEffect() {
		ArrayList<Object> items = new ArrayList<Object>();
		List<RiskEffect> effects = ServicesLoader.get(RiskService.class).listRiskEffect(
				new Query().filter(new BasicDBObject("project_id", project_id).append("rbsItem_id", _id)).bson(),
				project_id);
		items.addAll(effects);
		List<RBSItem> sndRisks = ServicesLoader.get(RiskService.class).listRBSItem(
				new Query().filter(new BasicDBObject("project_id", project_id).append("parent_id", _id)).bson());
		items.addAll(sndRisks);
		return items;
	}

	@Structure({ "项目风险登记簿/count", "项目风险登记簿（查看）/count" })
	private long countSecondryRisksNEffect() {
		long cnt = ServicesLoader.get(RiskService.class).countRiskEffect(new BasicDBObject("rbsItem_id", _id),
				project_id);

		cnt += ServicesLoader.get(RiskService.class)
				.countRBSItem(new BasicDBObject("project_id", project_id).append("parent_id", _id));

		return cnt;
	}

	@Structure({ "项目风险应对计划/list", "项目风险应对计划（查看）/list" })
	private List<RiskResponseType> listRiskResponseType() {
		return Arrays.asList(RiskResponseType.pervent(_id), RiskResponseType.response(_id),
				RiskResponseType.emergency(_id));
	}

	@Structure({ "项目风险应对计划/count", "项目风险应对计划（查看）/count" })
	private long countRiskResponseType() {
		return listRiskResponseType().size();
	}

	@ReadOptions("qtyInf")
	private Map<String, Object> getQuanlityInfIndOption() {
		Map<String, Object> res = new LinkedHashMap<String, Object>();
		ServicesLoader.get(RiskService.class).listRiskQuanlityInfInd().forEach(qii -> res.put(qii.text, qii.value));
		return res;
	}

	@ReadOptions("detectable")
	private Map<String, Object> getDetectableIndOption() {
		Map<String, Object> res = new LinkedHashMap<String, Object>();
		ServicesLoader.get(RiskService.class).listRiskDetectionInd().forEach(qii -> res.put(qii.text, qii.value));
		return res;
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

	public RBSItem setCreationInfo(OperationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

}
