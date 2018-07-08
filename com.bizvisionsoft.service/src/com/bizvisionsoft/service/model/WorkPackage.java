package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.tools.Util;

@PersistenceCollection("workPackage")
public class WorkPackage {

	public static WorkPackage newInstance(IWorkPackageMaster work, TrackView tv) {
		WorkPackage wp = new WorkPackage();
		wp.work_id = work.get_id();
		wp.deadline = work.getPlanFinish();
		if (tv != null) {
			wp.catagory = tv.getCatagory();
			wp.name = tv.getName();
		}
		return wp;
	}

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@Behavior("查看工作包进度")
	public boolean behaviourOpenProgress(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Object root) {
		return !(root instanceof ProjectTemplate);
	}

	@Behavior({ "删除工作包", "编辑工作包" })
	public boolean behaviourEdit(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Object root) {
		return actualFinish == null;
	}

	public ObjectId get_id() {
		return _id;
	}

	/**
	 * 工作包的名称，与TrackView名称一致
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 工作包的类型，与TrackView一致
	 */
	@ReadValue
	@WriteValue
	private String catagory;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "工作包";

	@Override
	@Label
	public String toString() {
		String text = "";
		if (matDesc != null) {
			text += matDesc;
		}

		if (matId != null) {
			text += " [" + matId + "]";
		}

		if (description != null) {
			text += description;
		}

		if (id != null) {
			text += " [" + id + "]";
		}

		return text;
	}

	@ReadValue
	@WriteValue
	private String chargerId;

	@SetValue
	@ReadValue
	private String chargerInfo;

	@WriteValue("charger")
	private void setCharger(User charger) {
		if (charger != null) {
			this.chargerId = charger.getUserId();
			this.chargerInfo = charger.toString();
		} else {
			this.chargerId = null;
			this.chargerInfo = null;
		}
	}

	@ReadValue("charger")
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	@Persistence
	private ObjectId work_id;

	@ReadValue
	@SetValue
	private Date deadline;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 采购使用的字段，不排除其他视图使用
	@ReadValue
	@WriteValue
	private String matId;

	@ReadValue
	@WriteValue
	private String matDesc;

	@ReadValue
	@WriteValue
	private String unit;

	@ReadValue
	private double planQty;

	private Date actualFinish;

	public Date getActualFinish() {
		return actualFinish;
	}

	@WriteValue("planQty")
	private void setPlanQty(String _planQty) {
		planQty = Util.str_double(_planQty, "计划数量要求为数值。");
	}

	@ReadValue
	private double completeQty;

	@ReadValue("requiredQty")
	private double getRequiredQty() {
		return planQty - completeQty;
	}

	@ReadValue
	private Date updateTime;
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 检验使用的字段

	@ReadValue
	private double qualifiedQty;

	@ReadValue("qualifiedRate")
	private Double getQualifiedRate() {
		if (completeQty != 0d) {
			return qualifiedQty / completeQty;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 研发使用的字段
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private Boolean planStatus;

	@ReadValue
	@WriteValue
	private String verNo;

	@ReadValue
	@WriteValue
	private String completeStatus;

	@ReadValue
	@WriteValue
	private String documentType;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId getWork_id() {
		return work_id;
	}
}
