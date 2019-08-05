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
	 * ���ͣ�ֻ�������¼��� ��������ǰ����ʱ�䣬��ǰʱ������ֶΣ�JavaScript����ѯ����ˮ��
	 * 
	 */
	@ReadValue
	@WriteValue
	public String type;

	/**
	 * ����Ϊ����ʱ��ֵ
	 */
	@ReadValue
	@WriteValue
	public String value;

	/**
	 * ����Ϊ�ֶ�ʱ���ֶ���,cName/fName
	 */
	@ReadValue
	@WriteValue
	public String name;

	/**
	 * ����ΪJavaScriptʱ���õĺ���������Ϊnull,Ϊnullʱ������JavaScript�ķ���ֵ
	 */
	@ReadValue
	@WriteValue
	public String function;

	/**
	 * ����Ϊ��ѯʱʹ�õļ���
	 */
	@ReadValue
	@WriteValue
	public String collection;

	/**
	 * ����Ϊ��ѯʱ�Ĳ�������ʽΪjqParam=cName/fName������jqParamΪJQ�Ĳ�������cName
	 * fNameΪ��ǰ�����ReadValueע�������
	 */
	@ReadValue
	@WriteValue
	public String params;

	/**
	 * JavaScript�ű�
	 */
	@ReadValue
	@WriteValue
	public String script;

	/**
	 * ����Ϊ��ѯʱ��Ӧ��JQ����
	 */
	@ReadValue
	@WriteValue
	public String query;
	
	/**
	 * ����Ϊ��ѯʱ��Ӧ��pipeline
	 */
	@ReadValue
	@WriteValue
	public String pipelineJson;

	/**
	 * ����Ϊ��ѯʱ�����ؽ�����ֶ����ƣ�����Ϊnull,Ϊnullʱ���ؽ����Document����
	 */
	@ReadValue
	@WriteValue
	public String returnField;

	/**
	 * ��ʽ����������������ͣ���ѭDateFormat, ������������ͣ�ʹ��NumberFormat
	 */
	@ReadValue
	@WriteValue
	public String format;

	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	public Integer length;

	/**
	 * ���ݵ�ǰ��ֵ���ı����ȣ���Ҫ��ĳ��Ƚ��д��� �����Ҫ���룬����ķ������ҷֱ��ʾ������Ҳ���
	 * �����Ҫ�ضϣ��ضϵķ������ҷֱ��ʾ����߿�ʼ�ضϺʹ��ұ߿�ʼ�ض�
	 */
	@ReadValue
	@WriteValue
	public String supplymentDir;

	/**
	 * ���볤��ʹ�õ�ռλ����ֻ����һ���ַ�
	 */
	@ReadValue
	@WriteValue
	public String supplymentPlaceholder;

	/**
	 * ��д��Сд�������ⶼ��ʾ��ת��
	 */
	@ReadValue
	@WriteValue
	public String caser;

	/**
	 * ִ��˳��
	 */
	@ReadValue
	@WriteValue
	public int executeSequance;

	/**
	 * �����ַ�����ƴ��˳��
	 */
	@ReadValue
	@WriteValue
	public int index;

	/**
	 * ��ֹ���
	 */
	@ReadValue
	@WriteValue
	public boolean disableOutput;

	/**
	 * ��ˮ��Id��ʹ��index���ü��ŷָ���磺1-2-4��ʾȡ���1,2,4��Ϊǰ׺��indexΪ���ı�ʾ�ÿ��ַ�����index���Խ��ʱ���׳�����
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
	public static final String typeName = "ֵ�����";
}
