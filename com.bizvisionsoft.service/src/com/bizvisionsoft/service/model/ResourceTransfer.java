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

	private boolean canClose;

	private boolean canDelete;

	private boolean canEditDateValue;

	private boolean showResTypeInfo;

	private boolean showResPlan;

	private boolean showResActual;

	private boolean showConflict;

	private boolean showFooter;

	private boolean isReport;

	private String title;

	public ResourceTransfer() {
		this.canAdd = true;
		this.canClose = true;
		this.canDelete = false;
		this.canEditDateValue = true;
		this.checkTime = false;
		this.showResActual = false;
		this.showResPlan = false;
		this.showResTypeInfo = false;
		this.showConflict = false;
		this.showFooter = false;
		this.isReport = false;
	}

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

	public void setCanClose(boolean canClose) {
		this.canClose = canClose;
	}

	public boolean isCanClose() {
		return canClose;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setCanEditDateValue(boolean canEditDateValue) {
		this.canEditDateValue = canEditDateValue;
	}

	public boolean isCanEditDateValue() {
		return canEditDateValue;
	}

	public void setShowResTypeInfo(boolean showResTypeInfo) {
		this.showResTypeInfo = showResTypeInfo;
	}

	public boolean isShowResTypeInfo() {
		return showResTypeInfo;
	}

	public void setShowResPlan(boolean showResPlan) {
		this.showResPlan = showResPlan;
	}

	public boolean isShowResPlan() {
		return showResPlan;
	}

	public void setShowResActual(boolean showResActual) {
		this.showResActual = showResActual;
	}

	public boolean isShowResActual() {
		return showResActual;
	}

	public void setShowConflict(boolean showConflict) {
		this.showConflict = showConflict;
	}

	public boolean isShowConflict() {
		return showConflict;
	}

	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}

	public boolean isShowFooter() {
		return showFooter;
	}

	public void setIsReport(boolean isReport) {
		this.isReport = isReport;
	}

	public boolean isReport() {
		return isReport;
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

	private ObjectId workReportItemId;

	public ObjectId getWorkReportItemId() {
		return workReportItemId;
	}

	public void setWorkReportItemId(ObjectId workReportItemId) {
		this.workReportItemId = workReportItemId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
