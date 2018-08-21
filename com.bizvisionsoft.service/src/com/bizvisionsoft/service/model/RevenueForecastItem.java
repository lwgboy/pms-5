package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

/**
 * Ψһ���� project_id, yeart, halfyear,quarter,month
 * @author hua
 */
@PersistenceCollection("revenueForecastItem")
public class RevenueForecastItem {
	
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId project_id;

	@ReadValue
	@WriteValue
	private Integer year;

	/**
	 * ֻ����0, 1 0 ��ʾ�ϰ��꣬1��ʾ�°���
	 */
	@ReadValue
	@WriteValue
	private Integer halfyear;

	/**
	 * 0��1��2��3�ֱ��ʾ1���ȵ�4����
	 */
	@ReadValue
	@WriteValue
	private Integer quarter;

	/**
	 * 0,1...11 �ֱ��ʾ1�µ�12��
	 */
	@ReadValue
	@WriteValue
	private Integer month;

	public static final String TYPE_YEAR = "year";
	
	public static final String TYPE_HALFYEAR = "halfyear";
	
	public static final String TYPE_QUARTER = "quarter";
	
	public static final String TYPE_MONTH = "month";

	/**
	 * type = "year","halfyear" "quarter", ���� �� "month" �ֱ��ʾ ���� �� ���꣬������ �� ����
	 */
	@ReadValue
	@WriteValue
	private String type;
	
	/**
	 * ���
	 */
	@ReadValue
	@WriteValue
	private Double amount;

}
