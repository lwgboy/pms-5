package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("refDef")
public class RefDef {

	/**
	 * 表单参照，如果作为表单参照，集合是确定的（docu）,需要考虑访问哪个对象，该对象的字段，以及后处理脚本
	 */
	@Exclude
	public static final String REF_TYPE_FORM = "form";

	/**
	 * 上下文参照。如果作为上下文参照，需要考虑访问上下文的哪个对象，该对象的字段，以及后处理脚本
	 */
	@Exclude
	public static final String REF_TYPE_CONTEXT = "context";

	/**
	 * 外部参照。如果作为外部参照，需要考虑访问哪个集合的哪一个对象，该对象的字段，以及后处理脚本。
	 */
	@Exclude
	public static final String REF_TYPE_EXTERNAL = "external";

	/**
	 * 参照类型：REF_TYPE_FORM,REF_TYPE_CONTEXT,REF_TYPE_EXTERNAL
	 */
	@ReadValue
	@WriteValue
	private String refType;

	/**
	 * 表格组件Id, 用于在多条数据中选择
	 */
	private String selectorId;

	/**
	 * 参照记录返回字段名
	 */
	private String fieldName;

	/**
	 * 后处理脚本
	 */
	private String postProc;

}
