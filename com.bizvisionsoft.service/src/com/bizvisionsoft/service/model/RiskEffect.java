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
import com.bizvisionsoft.annotations.md.service.ReadEditorConfig;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.SelectionValidation;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.tools.Util;

@PersistenceCollection("riskEffect")
public class RiskEffect {

	/** ��ʶ Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** ��Ŀ_Id **/
	@ReadValue
	@WriteValue
	private ObjectId project_id;

	@ReadValue
	@WriteValue
	private ObjectId work_id;

	@Behavior("RBSItem����")
	@Exclude
	private boolean rbsAction = false;

	@SetValue
	private Work work;

	@SetValue
	private RBSItem rbsItem;

	@ReadValue("result")
	private String readReault() {
		if (positive) {
			return "<i class='layui-icon layui-icon-next' style='color:green;'></i> WBS:" + work.getWBSCode() + " "
					+ work.getFullName();
		} else {
			return "<i class='layui-icon layui-icon-next' style='color:red;'></i> WBS:" + work.getWBSCode() + " "
					+ work.getFullName();
		}
	}

	@WriteValue("work")
	public void setWork(Work work) {
		this.work_id = Optional.ofNullable(work).map(o -> o.get_id()).orElse(null);
		this.work = work;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "����Ӱ��";

	@Override
	@Label
	public String toString() {
		return work.getFullName();
	}

	@ReadValue({ "��Ŀ���յǼǲ�/title", "��Ŀ���յǼǲ����鿴��/title" })
	private String getTitle() {
		if (positive) {
			return "<span class='layui-badge layui-bg-green' style='float:right;margin-right:10px;'>����Ӱ��</span>";
		} else {
			return "<span class='layui-badge layui-bg-red' style='float:right;margin-right:10px;'>����Ӱ��</span>";
		}
	}

	@ReadValue
	private CreationInfo creationInfo;

	@ReadEditorConfig("��Ŀ���յǼǲ�/�༭")
	private Object getEditorConfig() {
		return "����Ӱ��༭��";
	}

	@ReadValue("work")
	public Work getWork() {
		return work;
	}

	@ReadValue
	@WriteValue
	private boolean positive;

	@ReadValue
	@WriteValue
	private String description;

	/**
	 * ����Ӱ�죨�죩
	 */
	private int timeInf;

	@WriteValue("timeInf")
	private void setTimeInf(String _timeInf) {
		timeInf = Util.str_int(_timeInf, "����Ӱ���������������");
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
	 * �ɱ�Ӱ�죨��Ԫ��
	 */
	private double costInf;

	@Persistence
	private ObjectId rbsItem_id;

	@WriteValue("costInf")
	private void setCostInf(String _timeInf) {
		costInf = Util.str_double(_timeInf, "�ɱ�Ӱ�����������ֵ��");
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

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����WBSҪ��ֻ��ѡ��Ҷ�ӽڵ�
	@SelectionValidation("work")
	private boolean workSelectionValidation(@MethodParam(MethodParam.OBJECT) Object work) {
		return (work instanceof Work) && !((Work) work).isSummary();
	}

	public RiskEffect setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public RiskEffect setPositive(boolean positive) {
		this.positive = positive;
		return this;
	}

	public RiskEffect setRBSItem_id(ObjectId rbsItem_id) {
		this.rbsItem_id = rbsItem_id;
		return this;
	}

	public RiskEffect setCreationInfo(CreationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	@ReadValue("��Ŀ������������/wbsCode")
	private String readWorkWBSCode() {
		return work.getWBSCode();
	}

	@ReadValue("��Ŀ������������/workName")
	private String readWorkName() {
		return work.getFullName();
	}

	@ReadValue("��Ŀ������������/charger")
	private String readWorkCharger() {
		return work.getChargerInfo();
	}

	@ReadValue("��Ŀ������������/planFinish")
	private Date readWorkPlanFinish() {
		return work.getPlanFinish();
	}

	@ReadValue("��Ŀ������������/rbsCode")
	private String readRBSCode() {
		return rbsItem.getId();
	}

	@ReadValue("��Ŀ������������/riskDescription")
	private String readRiskDescription() {
		return rbsItem.getDescription();
	}

	@ReadValue("��Ŀ������������/riskResult")
	private String readRiskResult() {
		return rbsItem.getResult();
	}

	@ReadValue("��Ŀ������������/rbsType")
	private RBSType readRBSType() {
		return rbsItem.getRbsType();
	}

	@ReadValue("��Ŀ������������/ACP")
	private Double getACP() {
		return work.getACP();
	}

	@ReadValue("��Ŀ������������/ACI")
	private Double getACI() {
		return work.getACI();
	}

}
