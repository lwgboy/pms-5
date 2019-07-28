package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("refDef")
public class RefDef {

	/**
	 * �����գ������Ϊ�����գ�������ȷ���ģ�docu��,��Ҫ���Ƿ����ĸ����󣬸ö�����ֶΣ��Լ�����ű�
	 */
	@Exclude
	public static final String REF_TYPE_FORM = "form";

	/**
	 * �����Ĳ��ա������Ϊ�����Ĳ��գ���Ҫ���Ƿ��������ĵ��ĸ����󣬸ö�����ֶΣ��Լ�����ű�
	 */
	@Exclude
	public static final String REF_TYPE_CONTEXT = "context";

	/**
	 * �ⲿ���ա������Ϊ�ⲿ���գ���Ҫ���Ƿ����ĸ����ϵ���һ�����󣬸ö�����ֶΣ��Լ�����ű���
	 */
	@Exclude
	public static final String REF_TYPE_EXTERNAL = "external";

	/**
	 * �������ͣ�REF_TYPE_FORM,REF_TYPE_CONTEXT,REF_TYPE_EXTERNAL
	 */
	@ReadValue
	@WriteValue
	private String refType;

	/**
	 * ������Id, �����ڶ���������ѡ��
	 */
	private String selectorId;

	/**
	 * ���ռ�¼�����ֶ���
	 */
	private String fieldName;

	/**
	 * ����ű�
	 */
	private String postProc;

}
