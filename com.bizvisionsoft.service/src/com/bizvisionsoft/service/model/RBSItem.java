package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

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
	private Integer timeInf;

	/**
	 * �ɱ�Ӱ�죨��Ԫ��
	 */
	@ReadValue
	@WriteValue
	private Integer costInf;

	/**
	 * ����Ӱ�죨�ȼ���
	 */
	@ReadValue
	@WriteValue
	private Integer qtyInf;

	/**
	 * Ԥ�ڷ���ʱ��
	 */
	@ReadValue
	@WriteValue
	private Date forecast;

	/**
	 * ��̽���Լ���
	 */
	@ReadValue
	@WriteValue
	private Integer detectable;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "������";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}
}
