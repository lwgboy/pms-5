package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
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

	private ObjectId work_id;

	@WriteValue("work ")
	public void setWork(Work work) {
		this.work_id = Optional.ofNullable(work).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("work ")
	public Work getWork() {
		return Optional.ofNullable(work_id).map(_id -> ServicesLoader.get(WorkService.class).getWork(_id)).orElse(null);
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
	

}
