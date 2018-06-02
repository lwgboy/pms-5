package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.tools.Util;

@PersistenceCollection("workPackage")
public class WorkPackage {

	public static WorkPackage newInstance(Work work, TrackView tv) {
		WorkPackage wp = new WorkPackage();
		wp.work_id = work.get_id();
		wp.work = work;
		if (tv != null) {
			wp.catagory = tv.getCatagory();
			wp.name = tv.getName();
		}
		return wp;
	}

	@ReadValue
	@WriteValue
	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	/**
	 * �����������ƣ���TrackView����һ��
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * �����������ͣ���TrackViewһ��
	 */
	@ReadValue
	@WriteValue
	private String catagory;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "������";

	@Override
	@Label
	public String toString() {
		String text = "";
		if (matDesc != null) {
			text += matDesc;
		}

		if (matId != null) {
			text += "[" + matId + "]";
		}
		
		if(description != null) {
			text += description;
		}
		
		if(id != null) {
			text += "[" + id + "]";
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
		this.chargerId = Optional.ofNullable(charger).map(o -> o.getUserId()).orElse(null);
		this.chargerInfo = charger.toString();
	}

	@ReadValue("charger")
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	@SetValue
	private Work work;

	@Persistence
	private ObjectId work_id;

	@ReadValue("deadline")
	private Date getDeadline() {
		return work.getPlanFinish();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɹ�ʹ�õ��ֶΣ����ų�������ͼʹ��
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

	@WriteValue("planQty")
	private void setPlanQty(String _planQty) {
		planQty = Util.str_double(_planQty, "�ƻ�����Ҫ��Ϊ��ֵ��");
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
	// ����ʹ�õ��ֶ�

	@ReadValue
	private double qualifiedQty;

	@ReadValue("qualifiedRate")
	private Double getQualifiedRate() {
		if (completeQty != 0d) {
			return qualifiedQty / completeQty * 100d;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �з�ʹ�õ��ֶ�
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String planStatus;

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

}