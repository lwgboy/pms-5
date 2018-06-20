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

	public static int TYPE_PLAN = 1;

	public static int TYPE_ACTUAL = 2;

	private int showType;

	public static int SHOWTYPE_ONEWORK_MULTIRESOURCE = 1 << 1;

	public static int SHOWTYPE_MULTIWORK_ONERESOURCE = 1 << 2;

	public static int SHOWTYPE_MULTIWORK_MULTIRESOURCE = 1 << 3;

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
}
