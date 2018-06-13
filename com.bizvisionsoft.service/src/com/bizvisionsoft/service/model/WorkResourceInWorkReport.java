package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkReportService;

@PersistenceCollection("workResourceInWorkReport")
public class WorkResourceInWorkReport {

	@ReadValue
	private ObjectId _id;

	@ReadValue
	@Persistence
	private ObjectId work_id;

	public ObjectId getWork_id() {
		return work_id;
	}

	@ReadValue
	@Persistence
	private ObjectId workReport_id;

	public ObjectId getWorkReport_id() {
		return workReport_id;
	}

	@ReadValue({ "type", ReadValue.TYPE })
	@SetValue
	private String type;

	@ReadValue
	@SetValue
	private String name;

	@ReadValue
	@SetValue
	private String resId;

	@Persistence
	private String usedHumanResId;

	@Persistence
	private String usedEquipResId;

	@Persistence
	private String usedTypedResId;

	@SetValue
	private ResourceType resType;

	@ReadValue("basicRate")
	private double getBasicRate() {
		return resType.getBasicRate();
	}

	@ReadValue("overtimeRate")
	private double getOvertimeRate() {
		return resType.getOvertimeRate();
	}

	@ReadValue
	private double actualBasicQty;

	@ReadValue
	@SetValue
	private double planBasicQty;

	public void setActualBasicQty(double actualBasicQty) {
		this.actualBasicQty = actualBasicQty;
	}

	@ReadValue
	private double actualOverTimeQty;

	@ReadValue
	@SetValue
	private double planOverTimeQty;

	@Persistence
	private ObjectId resTypeId;

	@Persistence
	private Date id;

	@ReadValue("actualAmount")
	private Double getActualAmount() {
		return getBasicRate() * actualBasicQty + getOvertimeRate() * actualOverTimeQty;
	}

	@ReadValue("planAmount")
	private Double getPlanAmount() {
		return getBasicRate() * planBasicQty + getOvertimeRate() * planOverTimeQty;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + resId + "]";
	}

	@ImageURL("resId")
	private String getLogo() {
		if ("人力资源".equals(type))
			return "/img/user_c.svg";
		else if ("设备设施".equals(type))
			return "/img/equipment_c.svg";
		return "/img/resource_c.svg";
	}

	public WorkResourceInWorkReport setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public WorkResourceInWorkReport setWorkReport_id(ObjectId workReport_id) {
		this.workReport_id = workReport_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public WorkResourceInWorkReport setUsedHumanResId(String usedHumanResId) {
		this.usedHumanResId = usedHumanResId;
		return this;
	}

	public WorkResourceInWorkReport setUsedEquipResId(String usedEquipResId) {
		this.usedEquipResId = usedEquipResId;
		return this;
	}

	public WorkResourceInWorkReport setUsedTypedResId(String usedTypedResId) {
		this.usedTypedResId = usedTypedResId;
		return this;
	}

	public WorkResourceInWorkReport setResTypeId(ObjectId resTypeId) {
		this.resTypeId = resTypeId;
		return this;
	}

	public void setId(Date id) {
		this.id = id;
	}

	public void setActualOverTimeQty(double actualOverTimeQty) {
		this.actualOverTimeQty = actualOverTimeQty;
	}

	public String getUsedHumanResId() {
		return usedHumanResId;
	}

	public String getUsedEquipResId() {
		return usedEquipResId;
	}

	public String getUsedTypedResId() {
		return usedTypedResId;
	}

	public ObjectId getResTypeId() {
		return resTypeId;
	}

	@Structure("工作报告-工作资源用量/list")
	private List<WorkResourceInWorkReport> listChildren() {
		return ServicesLoader.get(WorkReportService.class)
				.listSubWorkResourceInWorkReport(new WorkResourceAssignment().setWork_id(work_id)
						.setWorkReport_id(workReport_id).setUsedEquipResId(usedEquipResId)
						.setUsedHumanResId(usedHumanResId).setUsedTypedResId(usedTypedResId));
	}

	@Structure("工作报告-工作资源用量/count")
	private long countChildren() {
		return 1; // ServicesLoader.get(WorkReportService.class).countSubWorkResourceInWorkReport(work_id,
					// workReport_id);
	}
}
