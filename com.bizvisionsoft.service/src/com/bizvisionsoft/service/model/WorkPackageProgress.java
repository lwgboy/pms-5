package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

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
import com.bizvisionsoft.service.tools.Util;

@PersistenceCollection("workPackageProgress")
public class WorkPackageProgress {

	public ObjectId _id;

	public ObjectId package_id;

	public WorkPackageProgress setPackage_id(ObjectId package_id) {
		this.package_id = package_id;
		return this;
	}

	private WorkPackage workPackage;

	public WorkPackage getWorkPackage() {
		if (workPackage == null) {
			workPackage = ServicesLoader.get(WorkService.class).getWorkPackage(package_id);
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
		return getWorkPackage().getActualFinish() == null;
	}

	@ReadValue
	@WriteValue
	@Label
	private String description;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 采购使用的字段
	@ReadValue
	private double completeQty;

	@WriteValue("completeQty")
	private void setCompleteQty(String _completeQty) {
		completeQty = Util.str_double(_completeQty, "完成数量要求为数值。");
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
		blsl = Util.str_double(_blsl, "备料数量要求为数值。");
	}

	@Persistence
	@ReadValue
	private double jysl;

	@WriteValue("jysl")
	private void setJYSL(String _jysl) {
		jysl = Util.str_double(_jysl, "检验数量要求为数值。");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 检验使用的字段
	@Persistence
	@ReadValue
	private double qualifiedQty;

	@WriteValue("qualifiedQty")
	private void setQualifiedQty(String _qualifiedQty) {
		qualifiedQty = Util.str_double(_qualifiedQty, "合格数量要求为数值。");
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 研发使用的字段
	@Persistence
	@ReadValue
	@WriteValue
	private String completeStatus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
