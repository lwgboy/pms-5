package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
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
	@ReadValue
	@WriteValue
	private String id;

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
	@WriteValue
	private double probability;

	/**
	 * �������
	 */
	@ReadValue
	@WriteValue
	private String result;

	/**
	 * ����Ӱ�죨�죩
	 */
	@ReadValue
	@WriteValue
	private int timeInf;

	/**
	 * �ɱ�Ӱ�죨��Ԫ��
	 */
	@ReadValue
	@WriteValue
	private int costInf;

	/**
	 * ����Ӱ�죨�ȼ���
	 */
	@ReadValue
	@WriteValue
	private int qtyInf;

	/**
	 * ��ֵ
	 */
	@ReadValue
	@WriteValue
	private double infValue;

	/**
	 * ���չؼ�ָ��
	 */
	@ReadValue
	@SetValue
	private double rci;

	/**
	 * Ԥ�ڷ���ʱ��
	 */
	@ReadValue
	@WriteValue
	private Date forecast;

	/**
	 * �ٽ���
	 */
	@ReadValue
	@SetValue
	private int urgency;

	/**
	 * ��̽���Լ���
	 */
	@ReadValue
	@WriteValue
	private int detectable;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "������";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
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
}
