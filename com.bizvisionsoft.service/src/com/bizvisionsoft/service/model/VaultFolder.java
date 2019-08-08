package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("folder")
public class VaultFolder implements IFolder {

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
	private String projectnumber;

	@ReadValue
	@WriteValue
	private String projectdesc;

	@ReadValue
	@WriteValue
	private String projectworkorder;

	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	@Exclude
	private boolean opened;

	@ReadValue
	@WriteValue
	private String desc;

	@ReadValue
	@WriteValue
	private String path;

	@ReadValue
	@WriteValue
	private Boolean isflderroot;

	@ReadValue
	@WriteValue
	private ObjectId root_id;

	@ReadValue
	@WriteValue
	private String containertype;

	@ReadValue
	@WriteValue
	private String description;

	@ReadValue
	@WriteValue
	private Boolean iscontainer;

	@ReadValue
	@WriteValue
	private Boolean defaultquery;

	@ReadValue
	@WriteValue
	private String wchost;

	@ReadValue
	@WriteValue
	private String containername;

	@ReadValue
	@WriteValue
	private String wcaddress;

	@ReadValue
	@WriteValue
	private String wcview;

	@ReadValue
	@WriteValue
	private String wcfolderservice;

	@ReadValue
	@WriteValue
	private String wcuser;

	@ReadValue
	@WriteValue
	private String wcpassword;

	@ReadValue
	@WriteValue
	@Persistence("org_id")
	private List<ObjectId> organization_id;

	@ReadValue
	@WriteValue
	@SetValue("orgFullName")
	private List<String> orgFullName;

	@WriteValue("organization")
	private void setOrganization(List<Organization> org) {
		organization_id = new ArrayList<ObjectId>();
		org.forEach(o -> organization_id.add(o.get_id()));
	}

	@ReadValue("organization")
	public List<Organization> getOrganization() {
		if (organization_id != null) {
			return ServicesLoader.get(OrganizationService.class)
					.createDataSet(new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", organization_id))).bson(), domain);
		} else {
			return new ArrayList<>();
		}
	}

	@ReadValue
	@WriteValue
	@Persistence
	private List<String> formDefNames;

	@WriteValue("formDef")
	private void setFormDef(List<Document> org) {
		formDefNames = new ArrayList<String>();
		org.forEach(o -> formDefNames.add(o.getString("name")));
	}

	@ReadValue("formDef")
	public List<Document> getFormDef() {
		if (formDefNames != null) {
			return ServicesLoader.get(CommonService.class).listNameOfFormDef(
					new Query().filter(new BasicDBObject("name", new BasicDBObject("$in", formDefNames))).bson(), domain);
		} else {
			return new ArrayList<>();
		}
	}

	@ReadValue("资料库设置/type")
	private String getType() {
		if ("pm".equals(containertype))
			return "PM资料库";
		if ("wc".equals(containertype))
			return "Windchill容器";
		return "";
	}

	@ImageURL("desc")
	private String getHTMLName() {
		// 如果是容器，返回容器图标
		if (isContainer()) {
			return "/img/cabinet_c.svg";
		} else if (isflderroot()) {
			return "/img/folder_project.svg";
		} else if (opened) {
			return "/img/folder_opened.svg";
		} else {
			return "/img/folder_closed.svg";
		}
	}

	// @ReadValue("文件夹选择列表/folderName")
	// private String getHTMLName2() {
	// String iconUrl;
	// if (parent_id == null) {
	// iconUrl = "rwt-resources/extres/img/cabinet_c.svg";
	// } else if (opened) {
	// iconUrl = "rwt-resources/extres/img/folder_opened.svg";
	// } else {
	// iconUrl = "rwt-resources/extres/img/folder_closed.svg";
	// }
	// String html = "<div class='brui_ly_hline'>" + "<img src=" + iconUrl + "
	// style='margin-right:8px;' width='20px' height='20px'/>"
	// + "<div style='flex:auto;'>" + desc + "</div>" + "</div>";
	// return html;
	// }

	@Override
	@Label
	public String toString() {
		return desc;
	}

	public String domain;

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
	}

	public VaultFolder set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public VaultFolder setDesc(String desc) {
		this.desc = desc;
		return this;
	}

	public VaultFolder setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public ObjectId getProject_id() {
		return project_id;
	}

	public VaultFolder setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public VaultFolder setOpened(boolean opened) {
		this.opened = opened;
		return this;
	}

	public boolean isflderroot() {
		return Boolean.TRUE.equals(isflderroot);
	}

	public VaultFolder setflderroot(boolean isflderroot) {
		this.isflderroot = isflderroot;
		return this;
	}

	public ObjectId getRoot_id() {
		return root_id;
	}

	public VaultFolder setRoot_id(ObjectId root_id) {
		this.root_id = root_id;
		return this;
	}

	@Override
	public IFolder getContainer() {
		if (isflderroot)
			return this;
		return root_id != null ? ServicesLoader.get(CommonService.class).getVaultFolder(root_id, domain) : null;
	}

	@Override
	public boolean isContainer() {
		return Boolean.TRUE.equals(iscontainer);
	}

	public VaultFolder setIsContainer(Boolean iscontainer) {
		this.iscontainer = iscontainer;
		return this;
	}

	@Override
	public IFolder setName(String name) {
		this.desc = name;
		return this;
	}

	@Override
	public String getName() {
		return desc;
	}

	public void setProjectdesc(String projectdesc) {
		this.projectdesc = projectdesc;
	}

	public void setProjectnumber(String projectnumber) {
		this.projectnumber = projectnumber;
	}

	public void setProjectworkorder(String projectworkorder) {
		this.projectworkorder = projectworkorder;
	}

	public static VaultFolder getInstance(Project project, boolean isflderroot, String domain) {
		VaultFolder folder = getInstance(domain);
		folder.setflderroot(isflderroot);
		folder.setProject_id(project.get_id());
		folder.setRoot_id(project.getContainer_id());
		return folder;
	}

	public static VaultFolder getInstance(String domain) {
		VaultFolder folder = new VaultFolder();
		folder.set_id(new ObjectId());
		folder.domain = domain;
		return folder;
	}

	public VaultFolder getSubFolderInstance(String domain) {
		VaultFolder vf = getInstance(domain);
		vf.setParent_id(_id);
		vf.setRoot_id(root_id);
		vf.setProject_id(project_id);
		vf.setProjectworkorder(projectworkorder);
		vf.setProjectdesc(projectdesc);
		vf.setProjectnumber(projectnumber);
		return vf;
	}

}
