package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class ResourceTransfer {

	private List<ObjectId> workIds;

	private Date from;

	private Date to;

	private int type;

	public static final int TYPE_PLAN = 1;

	public static final int TYPE_ACTUAL = 2;

	private int showType;

	public static final int SHOWTYPE_ONEWORK_MULTIRESOURCE = 1 << 1;

	public static final int SHOWTYPE_MULTIWORK_ONERESOURCE = 1 << 2;

	public static final int SHOWTYPE_MULTIWORK_MULTIRESOURCE = 1 << 3;

	private boolean checkTime;

	private boolean canAdd;

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setShowType(int showType) {
		this.showType = showType;
	}

	public int getShowType() {
		return showType;
	}

	public List<ObjectId> getWorkIds() {
		return workIds;
	}

	public void setWorkIds(List<ObjectId> workIds) {
		this.workIds = workIds;
	}

	public void addWorkIds(ObjectId workId) {
		if (workIds == null) {
			workIds = new ArrayList<ObjectId>();
		}
		workIds.add(workId);
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public boolean isCheckTime() {
		return checkTime;
	}

	public void setCheckTime(boolean checkTime) {
		this.checkTime = checkTime;
	}

	public boolean isCanAdd() {
		return canAdd;
	}

	public void setCanAdd(boolean canAdd) {
		this.canAdd = canAdd;
	}

	private String usedHumanResId;

	private String usedEquipResId;

	private String usedTypedResId;

	private ObjectId resTypeId;

	public String getUsedHumanResId() {
		return usedHumanResId;
	}

	public void setUsedHumanResId(String usedHumanResId) {
		this.usedHumanResId = usedHumanResId;
	}

	public String getUsedEquipResId() {
		return usedEquipResId;
	}

	public void setUsedEquipResId(String usedEquipResId) {
		this.usedEquipResId = usedEquipResId;
	}

	public String getUsedTypedResId() {
		return usedTypedResId;
	}

	public void setUsedTypedResId(String usedTypedResId) {
		this.usedTypedResId = usedTypedResId;
	}

	public ObjectId getResTypeId() {
		return resTypeId;
	}

	public void setResTypeId(ObjectId resTypeId) {
		this.resTypeId = resTypeId;
	}
}
