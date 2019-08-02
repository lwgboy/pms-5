package com.bizvisionsoft.service.model;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("valueRuleSegment")
public class ValueRuleSegment {

	@ReadValue
	@WriteValue
	public ObjectId _id;

	@ReadValue
	@WriteValue
	public ObjectId rule_id;

	/**
	 * 类型：只能是以下几种 常量，当前日期时间，当前时间戳，字段，JavaScript，查询，流水号
	 * 
	 */
	@ReadValue
	@WriteValue
	public String type;

	/**
	 * 类型为常量时的值
	 */
	@ReadValue
	@WriteValue
	public String value;

	/**
	 * 类型为字段时的字段名,cName/fName
	 */
	@ReadValue
	@WriteValue
	public String name;

	/**
	 * 类型为JavaScript时调用的函数，可以为null,为null时，计算JavaScript的返回值
	 */
	@ReadValue
	@WriteValue
	public String function;

	/**
	 * 类型为查询时使用的集合
	 */
	@ReadValue
	@WriteValue
	public String collection;

	/**
	 * 类型为查询时的参数，格式为jqParam=cName/fName，其中jqParam为JQ的参数名，cName
	 * fName为当前对象的ReadValue注解的名称
	 */
	@ReadValue
	@WriteValue
	public String params;

	/**
	 * JavaScript脚本
	 */
	@ReadValue
	@WriteValue
	public String script;

	/**
	 * 类型为查询时对应的JQ名称
	 */
	@ReadValue
	@WriteValue
	public String query;
	
	/**
	 * 类型为查询时对应的pipeline
	 */
	@ReadValue
	@WriteValue
	public String pipelineJson;

	/**
	 * 类型为查询时，返回结果的字段名称，可以为null,为null时返回结果的Document类型
	 */
	@ReadValue
	@WriteValue
	public String returnField;

	/**
	 * 格式化，如果是日期类型，遵循DateFormat, 如果是数字类型，使用NumberFormat
	 */
	@ReadValue
	@WriteValue
	public String format;

	/**
	 * 长度
	 */
	@ReadValue
	@WriteValue
	public Integer length;

	/**
	 * 根据当前的值的文本长度，和要求的长度进行处理。 如果需要补齐，补齐的方向，左，右分别表示左补齐和右补齐
	 * 如果需要截断，截断的方向，左，右分别表示从左边开始截断和从右边开始截断
	 */
	@ReadValue
	@WriteValue
	public String supplymentDir;

	/**
	 * 补齐长度使用的占位符，只能是一个字符
	 */
	@ReadValue
	@WriteValue
	public String supplymentPlaceholder;

	/**
	 * 大写，小写除此以外都表示不转换
	 */
	@ReadValue
	@WriteValue
	public String caser;

	/**
	 * 执行顺序
	 */
	@ReadValue
	@WriteValue
	public int executeSequance;

	/**
	 * 本段字符串的拼接顺序
	 */
	@ReadValue
	@WriteValue
	public int index;

	/**
	 * 禁止输出
	 */
	@ReadValue
	@WriteValue
	public boolean disableOutput;

	/**
	 * 流水号Id，使用index，用减号分割，例如：1-2-4表示取码段1,2,4作为前缀，index为负的表示用空字符串，index码段越界时，抛出错误
	 */
	@ReadValue
	@WriteValue
	public String snId;
	
	public String domain;

	@Label
	public String label() {
		return type;
	}

	@Exclude
	@ReadValue(ReadValue.TYPE)
	public static final String typeName = "值规则段";
}
