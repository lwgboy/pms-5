package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

/**
 * 唯一索引 project_id, yeart, halfyear,quarter,month
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
	 * 只能是0, 1 0 表示上半年，1表示下半年
	 */
	@ReadValue
	@WriteValue
	private Integer halfyear;

	/**
	 * 0，1，2，3分别表示1季度到4极端
	 */
	@ReadValue
	@WriteValue
	private Integer quarter;

	/**
	 * 0,1...11 分别表示1月到12月
	 */
	@ReadValue
	@WriteValue
	private Integer month;

	public static final String TYPE_YEAR = "year";
	
	public static final String TYPE_HALFYEAR = "halfyear";
	
	public static final String TYPE_QUARTER = "quarter";
	
	public static final String TYPE_MONTH = "month";

	/**
	 * type = "year","halfyear" "quarter", 或者 是 "month" 分别表示 按年 或 半年，按季度 或 按月
	 */
	@ReadValue
	@WriteValue
	private String type;
	
	/**
	 * 金额
	 */
	@ReadValue
	@WriteValue
	private Double amount;

}
