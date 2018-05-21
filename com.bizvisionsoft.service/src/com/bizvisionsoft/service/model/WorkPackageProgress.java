package com.bizvisionsoft.service.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("workPackageProgress")
public class WorkPackageProgress {

	@Persistence
	public ObjectId _id;

	@Persistence
	public ObjectId package_id;

	public WorkPackageProgress setPackage_id(ObjectId package_id) {
		this.package_id = package_id;
		return this;
	}

	@Persistence
	private Date updateTime;

	public WorkPackageProgress setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 采购使用的字段
	@Persistence
	@ReadValue
	private Double completeQty;

	@WriteValue("completeQty")
	private void setCompleteQty(String _completeQty) {
		try {
			completeQty = Double.parseDouble(_completeQty);
		} catch (Exception e) {
			throw new RuntimeException("请输入合法的数字");
		}
	}

	@Persistence
	@ReadValue
	@WriteValue
	private Date time;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 生产使用的字段
	@Persistence
	@ReadValue
	private Double blsl;

	@WriteValue("blsl")
	private void setBLSL(String _blsl) {
		try {
			blsl = Double.parseDouble(_blsl);
		} catch (Exception e) {
			throw new RuntimeException("请输入合法的数字");
		}
	}
	
	@Persistence
	@ReadValue
	private Double jysl;

	@WriteValue("jysl")
	private void setJYSL(String _jysl) {
		try {
			jysl = Double.parseDouble(_jysl);
		} catch (Exception e) {
			throw new RuntimeException("请输入合法的数字");
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 检验使用的字段
	@Persistence
	@ReadValue
	private Double qualifiedQty;

	@WriteValue("qualifiedQty")
	private void setQualifiedQty(String _qualifiedQty) {
		try {
			qualifiedQty = Double.parseDouble(_qualifiedQty);
		} catch (Exception e) {
			throw new RuntimeException("请输入合法的数字");
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 设计使用的字段
	@Persistence
	@ReadValue
	@WriteValue
	private String completeStatus;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Label
	public String getText() {
		if (time != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.format(time);
		}

		return "";
	}
}
