package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.tools.Formatter;

@PersistenceCollection("workPackageProgress")
public class WorkPackageProgress {

	@Exclude
	public String domain;

	public ObjectId _id;

	public ObjectId package_id;

	public WorkPackageProgress setPackage_id(ObjectId package_id) {
		this.package_id = package_id;
		return this;
	}

	private WorkPackage workPackage;

	public WorkPackage getWorkPackage(String domain) {
		if (workPackage == null) {
			workPackage = ServicesLoader.get(WorkService.class).getWorkPackage(package_id, domain);
		}
		return workPackage;
	}

	public Date updateTime;

	public WorkPackageProgress setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
		return this;
	}

	@Behavior({ "编辑研发进展", "删除研发进展" })
	public boolean behaviourAdd(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Object root) {
		return getWorkPackage(domain).getActualFinish() == null;
	}

	@ReadValue
	@WriteValue
	@Label
	public String description;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 采购使用的字段
	@ReadValue
	private double completeQty;

	@WriteValue("completeQty")
	private void setCompleteQty(String _completeQty) {
		completeQty = Formatter.getDouble(_completeQty);
	}

	@ReadValue
	@WriteValue
	private Date time;

	@ReadValue
	@SetValue
	private String unit;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 生产使用的字段
	@Persistence
	@ReadValue
	private double blsl;

	@WriteValue("blsl")
	private void setBLSL(String _blsl) {
		blsl = Formatter.getDouble(_blsl);
	}

	@Persistence
	@ReadValue
	private double jysl;

	@WriteValue("jysl")
	private void setJYSL(String _jysl) {
		jysl = Formatter.getDouble(_jysl);
	}

	@Persistence
	@ReadValue
	public double qty;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 检验使用的字段
	@Persistence
	@ReadValue
	private double qualifiedQty;

	@WriteValue("qualifiedQty")
	private void setQualifiedQty(String _qualifiedQty) {
		qualifiedQty = Formatter.getDouble(_qualifiedQty);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 研发使用的字段
	@Persistence
	@ReadValue
	@WriteValue
	public String completeStatus;

	@Persistence
	@ReadValue
	@WriteValue
	public String charger;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
