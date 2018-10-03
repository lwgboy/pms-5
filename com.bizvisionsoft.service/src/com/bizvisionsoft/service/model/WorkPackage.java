package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.Document;
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
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.service.tools.Formatter;

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
		wp.workClass = work.getClass().getSimpleName();
		return wp;
	}

	@ReadValue
	@WriteValue
	private String workClass;

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

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	/**
	 * 工作包的名称，与TrackView名称一致
	 */
	@ReadValue
	@WriteValue
	private String name;

	public String getName() {
		return name;
	}

	/**
	 * 工作包的类型，与TrackView一致
	 */
	@ReadValue
	@WriteValue
	private String catagory;

	public String getCatagory() {
		return catagory;
	}

	@ReadValue
	@WriteValue
	public String description;

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

	@SetValue
	private UserMeta chargerInfo_meta;

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

	@ReadValue("chargerInfoHtml")
	public String getChargerInfoHtml() {
		if (chargerInfo == null) {
			return "";
		}
		return "<div>" + MetaInfoWarpper.userInfo(chargerInfo_meta, chargerInfo) + "</div>";
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
	public String matId;

	@ReadValue
	@WriteValue
	public String matDesc;

	@ReadValue
	@WriteValue
	public String unit;

	@ReadValue
	public double planQty;

	private Date actualFinish;

	public Date getActualFinish() {
		return actualFinish;
	}

	@WriteValue("planQty")
	private void setPlanQty(String _planQty) {
		planQty = Formatter.getDouble(_planQty);
	}

	@ReadValue
	@WriteValue
	public double completeQty;

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
	public String id;

	@ReadValue
	@WriteValue
	public Boolean planStatus;

	@ReadValue
	@WriteValue
	public String verNo;

	@ReadValue
	@WriteValue
	public boolean completeStatus;

	@ReadValue
	@WriteValue
	public String documentType;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId getWork_id() {
		return work_id;
	}

	public Document info;
}
