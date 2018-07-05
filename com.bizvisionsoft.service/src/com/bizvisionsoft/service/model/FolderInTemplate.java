package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("folderInTemplate")
public class FolderInTemplate {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId template_id;

	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	@Exclude
	private boolean opened;

	@ReadValue
	@WriteValue
	private String name;

	@Override
	@Label
	public String toString() {
		return name;
	}

	@Structure(DataSet.LIST)
	private List<FolderInTemplate> listChildren() {
		return ServicesLoader.get(ProjectTemplateService.class).listChildrenFolderInTemplate(_id);
	}

	@Structure(DataSet.COUNT)
	private long countChildren() {
		return ServicesLoader.get(ProjectTemplateService.class).countChildrenFolderInTemplate(_id);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public String getName() {
		return name;
	}

	public FolderInTemplate setName(String name) {
		this.name = name;
		return this;
	}

	public FolderInTemplate setTempalte_id(ObjectId template_id) {
		this.template_id = template_id;
		return this;
	}

	public FolderInTemplate setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

}
