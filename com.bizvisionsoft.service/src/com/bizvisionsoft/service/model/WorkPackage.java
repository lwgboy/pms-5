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

		return name == null ? "����������" : text + " " + name;
	}

	@ReadValue
	@WriteValue
	@Persistence
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
		return work.getPlanFinished();
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
	private Double planQty;

	@WriteValue("planQty")
	private void setPlanQty(String _planQty) {
		try {
			planQty = Double.parseDouble(_planQty);
		} catch (Exception e) {
			throw new RuntimeException("������Ϸ�������");
		}
	}

	@ReadValue
	private Double completeQty;

	@ReadValue("residualQty")
	private Double getResidualQty() {
		return (planQty != null ? planQty : 0) - (completeQty != null ? completeQty : 0);
	}

	@ReadValue
	private Date updateTime;
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����ʹ�õ��ֶ�

	@ReadValue
	private Double qualifiedQty;

	@ReadValue("qualifiedRate")
	private Double getQualifiedRate() {
		if (completeQty != null && completeQty != 0d) {
			return (qualifiedQty != null ? qualifiedQty : 0) / completeQty * 100d;
		}
		return 0d;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ���ʹ�õ��ֶ�
	@ReadValue
	@WriteValue
	@Persistence
	private String id;

	@ReadValue
	@WriteValue
	@Persistence
	private String planStatus;
	
	@ReadValue
	@WriteValue
	@Persistence
	private String verNo;

	@ReadValue
	@WriteValue
	private String completeStatus;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
