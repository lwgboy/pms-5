package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

public class UpdateWorkPackages {

	private List<WorkPackage> workPackages;

	private List<WorkPackageProgress> workPackageProgresss;

	private ObjectId work_id;

	private String catagory;

	private String name;

	public UpdateWorkPackages setWorkPackages(List<WorkPackage> workPackages) {
		this.workPackages = workPackages;
		return this;
	}

	public List<WorkPackage> getWorkPackages() {
		return workPackages;
	}

	public UpdateWorkPackages setWorkPackageProgress(List<WorkPackageProgress> workPackageProgresss) {
		this.workPackageProgresss = workPackageProgresss;
		return this;
	}

	public List<WorkPackageProgress> getWorkPackageProgresss() {
		return workPackageProgresss;
	}

	public UpdateWorkPackages setWork_id(ObjectId work_id) {
		this.work_id = work_id;
		return this;
	}

	public ObjectId getWork_id() {
		return work_id;
	}

	public UpdateWorkPackages setCatagory(String catagory) {
		this.catagory = catagory;
		return this;
	}

	public String getCatagory() {
		return catagory;
	}

	public UpdateWorkPackages setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}
}
