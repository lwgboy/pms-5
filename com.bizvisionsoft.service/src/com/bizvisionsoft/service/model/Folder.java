package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@Deprecated
@PersistenceCollection("folder")
public class Folder implements IFolder {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private ObjectId project_id;

	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	@Exclude
	private boolean opened;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private boolean isflderroot;

	@ReadValue
	@WriteValue
	private ObjectId root_id;

	@ReadValue({ "项目档案库文件夹/folderName" })
	private String getHTMLName() {
		String iconUrl;
		if (parent_id == null) {
			iconUrl = "rwt-resources/extres/img/cabinet_c.svg";
		} else if (opened) {
			iconUrl = "rwt-resources/extres/img/folder_opened.svg";
		} else {
			iconUrl = "rwt-resources/extres/img/folder_closed.svg";
		}
		String html = "<div class='brui_ly_hline'>" + "<img src=" + iconUrl + " style='margin-right:8px;' width='20px' height='20px'/>"
				+ "<div style='flex:auto;'>" + name + "</div>"
				+ "<a href='open/' target='_rwt' style='margin-right:8px;'><i class='layui-icon layui-btn layui-btn-primary layui-btn-xs' style='cursor:pointer;'>&#xe671;</i></a>"
				+ "</div>";
		return html;
	}

	@ReadValue("项目文件夹选择列表/folderName")
	private String getHTMLName2() {
		String iconUrl;
		if (parent_id == null) {
			iconUrl = "rwt-resources/extres/img/cabinet_c.svg";
		} else if (opened) {
			iconUrl = "rwt-resources/extres/img/folder_opened.svg";
		} else {
			iconUrl = "rwt-resources/extres/img/folder_closed.svg";
		}
		String html = "<div class='brui_ly_hline'>" + "<img src=" + iconUrl + " style='margin-right:8px;' width='20px' height='20px'/>"
				+ "<div style='flex:auto;'>" + name + "</div>" + "</div>";
		return html;
	}

	@Override
	@Label
	public String toString() {
		return name;
	}

	public String domain;

	@Structure(DataSet.LIST)
	private List<Folder> listChildren() {
		return ServicesLoader.get(DocumentService.class).listChildrenProjectFolder(_id, domain);
	}

	@Structure(DataSet.COUNT)
	private long countChildren() {
		return ServicesLoader.get(DocumentService.class).countChildrenProjectFolder(_id, domain);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public Folder setName(String name) {
		this.name = name;
		return this;
	}

	public Folder setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public Folder setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public Folder setOpened(boolean opened) {
		this.opened = opened;
		return this;
	}

	public boolean isflderroot() {
		return isflderroot;
	}

	public void setflderroot(boolean isflderroot) {
		this.isflderroot = isflderroot;
	}

	public ObjectId getRoot_id() {
		return root_id;
	}

	public void setRoot_id(ObjectId root_id) {
		this.root_id = root_id;
	}

	@ReadValue
	@WriteValue
	@Persistence
	private List<ObjectId> formDef_id;

	@WriteValue("formDef")
	private void setFormDef(List<FormDef> org) {
		formDef_id = new ArrayList<ObjectId>();
		org.forEach(o -> formDef_id.add(o.get_id()));
	}

	@ReadValue("formDef")
	public List<FormDef> getFormDef() {
		if (formDef_id != null) {
			return ServicesLoader.get(CommonService.class)
					.listFormDef(new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", formDef_id))).bson(), domain);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public IFolder getContainer() {
		if (isflderroot)
			return this;
		return root_id != null ? ServicesLoader.get(CommonService.class).getVaultFolder(root_id, domain) : null;
	}

	@Override
	public boolean isContainer() {
		return false;
	}
}
