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
	
	/**
	 * ���յ�״̬
	 */
	@ReadValue
	@WriteValue
	private String status;

	/** ��� Y **/
	private int index;

	@ReadValue("id")
	public String getId() {
		return getRbsType().getId() + "." + String.format("%03d", index);
	}

	@Behavior("RBSItem����")
	@Exclude
	private boolean rbsAction = true;

	@ReadEditorConfig("��Ŀ���յǼǲ�/�༭")
	private Object getEditorConfig() {
		return "������༭��";
	}

	@ReadValue("title")
	private String getDisplayName() {
		return getId() + " " + name;
	}

	@ReadValue
	private OperationInfo creationInfo;

	/**
	 * ��������
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * ��������
	 */
	@WriteValue
	@ReadValue({ "description", "��Ŀ���յǼǲ�/desc", "��Ŀ���յǼǲ����鿴��/desc" })
	private String description;
	
	public String getDescription() {
		return description;
	}

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

	public String getResult() {
		return result;
	}

	/**
	 * ����Ӱ�죨�죩
	 */
	private int timeInf;

	@WriteValue("timeInf")
	private void setTimeInf(String _timeInf) {
		timeInf = Util.str_int(_timeInf, "����Ӱ���������������");
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
	 * �ɱ�Ӱ�죨��Ԫ��
	 */
	private double costInf;

	@WriteValue("costInf")
	private void setCostInf(String _timeInf) {
		costInf = Util.str_double(_timeInf, "�ɱ�Ӱ�����������ֵ��");
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
	 * ����Ӱ�죨�ȼ���
	 */
	@ReadValue
	@WriteValue
	private String qtyInf;

	public String getQtyInf() {
		return qtyInf;
	}

	/**
	 * ��ֵ����
	 */
	@ReadValue
	private double infValue;

	public void setInfValue(double infValue) {
		this.infValue = infValue;
	}

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
		return ServicesLoader.get(RiskService.class).getUrgencyText(l);
	}

	/**
	 * ��̽���Լ���
	 */
	@ReadValue
	@WriteValue
	private String detectable;

	/**
	 * �������� rci x ����
	 */
	@ReadValue("rci")
	private double getRCI() {
		return infValue * probability;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "������";

	@Override
	@Label
	public String toString() {
		return name + " [" + getId() + "]";
	}

	@Structure({ "��Ŀ���յǼǲ�/list", "��Ŀ���յǼǲ����鿴��/list" })
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

	@Structure({ "��Ŀ���յǼǲ�/count", "��Ŀ���յǼǲ����鿴��/count" })
	private long countSecondryRisksNEffect() {
		long cnt = ServicesLoader.get(RiskService.class).countRiskEffect(new BasicDBObject("rbsItem_id", _id),
				project_id);

		cnt += ServicesLoader.get(RiskService.class)
				.countRBSItem(new BasicDBObject("project_id", project_id).append("parent_id", _id));

		return cnt;
	}

	@Structure({ "��Ŀ����Ӧ�Լƻ�/list", "��Ŀ����Ӧ�Լƻ����鿴��/list" })
	private List<RiskResponseType> listRiskResponseType() {
		return Arrays.asList(RiskResponseType.pervent(_id), RiskResponseType.response(_id),
				RiskResponseType.emergency(_id));
	}

	@Structure({ "��Ŀ����Ӧ�Լƻ�/count", "��Ŀ����Ӧ�Լƻ����鿴��/count" })
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
