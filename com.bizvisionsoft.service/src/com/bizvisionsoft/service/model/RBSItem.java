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

	/** ��ʶ Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** ��Ŀ_Id **/
	@ReadValue
	@WriteValue
	private ObjectId project_id;

	/** �ϼ����ձ�ʶ������иñ�ʶ�����������Ǵ������� **/
	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	/** ��� Y **/
	private int index;

	@ReadValue("id")
	private String getId() {
		return getRbsType().getId() + "." + String.format("%03d", index);
	}

	/**
	 * ��������
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * ��������
	 */
	@ReadValue
	@WriteValue
	private String description;

	/**
	 * �׶Σ�����ֶ��������Եġ����غ͹�������������ѡȡ�׶ε�����
	 */
	@ReadValue
	@WriteValue
	private String stage;

	/**
	 * ��������
	 */
	@ReadValue
	@WriteValue
	private RBSType rbsType;

	/**
	 * ��������
	 */
	@ReadValue
	private double probability;

	@WriteValue("probability")
	private void setProbability(String _probability) {
		probability = Util.str_double(_probability, "��������Ӱ��������븡������");
	}

	/**
	 * �������
	 */
	@ReadValue
	@WriteValue
	private String result;

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

	/**
	 * ����Ӱ�죨�ȼ���
	 */
	@ReadValue
	@WriteValue
	private String qtyInf;

	/**
	 * ��ֵ
	 */
	@ReadValue
	@WriteValue
	@Exclude
	private double infValue;

	/**
	 * Ԥ�ڷ���ʱ��
	 */
	@ReadValue
	@WriteValue
	private Date forecast;

	/**
	 * �ٽ���
	 */
	@ReadValue("urgency")
	private String getUrgency() {
		long l = (forecast.getTime() - Calendar.getInstance().getTimeInMillis()) / (24 * 60 * 60 * 1000);
		if (l > 60) {
			return "Զ��";
		} else if (l > 30) {
			return "����";
		} else if (l > 7) {
			return "����";
		} else if (l > 0) {
			return "�ٽ�";
		} else {
			return "";
		}
	}

	/**
	 * ��̽���Լ���
	 */
	@ReadValue
	@WriteValue
	private String detectable;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "������";

	@Override
	@Label
	public String toString() {
		return name + " [" + getId() + "]";
	}

	@Structure({ "��Ŀ�����嵥/list" })
	private List<RBSItem> listSecondryRisks() {
		return ServicesLoader.get(RiskService.class).listRBSItem(
				new Query().filter(new BasicDBObject("project_id", project_id).append("parent_id", _id)).bson());
	}

	@Structure({ "��Ŀ�����嵥/count" })
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
