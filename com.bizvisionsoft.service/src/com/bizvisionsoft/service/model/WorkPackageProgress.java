package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.tools.Util;

@PersistenceCollection("workPackageProgress")
public class WorkPackageProgress {

	public ObjectId _id;

	public ObjectId package_id;

	public WorkPackageProgress setPackage_id(ObjectId package_id) {
		this.package_id = package_id;
		return this;
	}

	public Date updateTime;

	public WorkPackageProgress setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
		return this;
	}

	@ReadValue
	@WriteValue
	@Label
	private String description;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɹ�ʹ�õ��ֶ�
	@ReadValue
	private double completeQty;

	@WriteValue("completeQty")
	private void setCompleteQty(String _completeQty) {
		completeQty = Util.str_double(_completeQty, "�������Ҫ��Ϊ��ֵ��");
	}

	@ReadValue
	@WriteValue
	private Date time;
	
	@ReadValue
	@SetValue
	private String unit;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����ʹ�õ��ֶ�
	@Persistence
	@ReadValue
	private double blsl;

	@WriteValue("blsl")
	private void setBLSL(String _blsl) {
		blsl = Util.str_double(_blsl, "��������Ҫ��Ϊ��ֵ��");
	}

	@Persistence
	@ReadValue
	private double jysl;

	@WriteValue("jysl")
	private void setJYSL(String _jysl) {
		jysl = Util.str_double(_jysl, "��������Ҫ��Ϊ��ֵ��");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����ʹ�õ��ֶ�
	@Persistence
	@ReadValue
	private double qualifiedQty;

	@WriteValue("qualifiedQty")
	private void setQualifiedQty(String _qualifiedQty) {
		qualifiedQty = Util.str_double(_qualifiedQty, "�ϸ�����Ҫ��Ϊ��ֵ��");
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �з�ʹ�õ��ֶ�
	@Persistence
	@ReadValue
	@WriteValue
	private String completeStatus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
