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
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("folder")
public class VaultFolder implements IFolder {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ������һЩ�ֶ�

	/** ��ʶ Y **/
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
	@Persistence("desc")
	private String name;

	@ReadValue
	@WriteValue
	private boolean isflderroot;

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
	private boolean iscontainer;

	@ReadValue
	@WriteValue
	private boolean defaultquery;

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

	@WriteValue("organization")
	private void setOrganization(List<Organization> org) {
		organization_id = new ArrayList<ObjectId>();
		org.forEach(o -> organization_id.add(o.get_id()));
	}

	@ReadValue("organization")
	public List<Organization> getOrganization() {
		if (organization_id != null) {
			return ServicesLoader.get(OrganizationService.class).createDataSet(
					new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", organization_id))).bson(),
					domain);
		} else {
			return new ArrayList<>();
		}
	}

	@ReadValue("type")
	@WriteValue("type")
	private String getType() {
		if ("pm".equals(containertype))
			return "PM���Ͽ�";
		if ("wc".equals(containertype))
			return "Windchill����";
		if ("dcpdm".equals(containertype))
			return "DCPDM";

		return "����";
	}

	@ReadValue({ "���Ͽ��ļ���/folderName", "��Ŀ���Ͽ��ļ���/folderName" })
	private String getHTMLName() {
		String iconUrl;
		if (parent_id == null) {
			iconUrl = "rwt-resources/extres/img/cabinet_c.svg";
		} else if (opened) {
			iconUrl = "rwt-resources/extres/img/folder_opened.svg";
		} else {
			iconUrl = "rwt-resources/extres/img/folder_closed.svg";
		}
		String html = "<div class='brui_ly_hline'>" + "<img src=" + iconUrl
				+ " style='margin-right:8px;' width='20px' height='20px'/>" + "<div style='flex:auto;'>" + name
				+ "</div>"
				+ "<a href='open/' target='_rwt' style='margin-right:8px;'><i class='layui-icon layui-btn layui-btn-primary layui-btn-xs' style='cursor:pointer;'>&#xe671;</i></a>"
				+ "</div>";
		return html;
	}

	@ReadValue("�ļ���ѡ���б�/folderName")
	private String getHTMLName2() {
		String iconUrl;
		if (parent_id == null) {
			iconUrl = "rwt-resources/extres/img/cabinet_c.svg";
		} else if (opened) {
			iconUrl = "rwt-resources/extres/img/folder_opened.svg";
		} else {
			iconUrl = "rwt-resources/extres/img/folder_closed.svg";
		}
		String html = "<div class='brui_ly_hline'>" + "<img src=" + iconUrl
				+ " style='margin-right:8px;' width='20px' height='20px'/>" + "<div style='flex:auto;'>" + name
				+ "</div>" + "</div>";
		return html;
	}

	@Structure("��Ŀ���Ͽ��ļ���/" + DataSet.LIST)
	private List<VaultFolder> listChildren() {
		return ServicesLoader.get(DocumentService.class)
				.listContainer(new Query().filter(new BasicDBObject("parent_id", _id)).bson(), domain);
	}

	@Structure("��Ŀ���Ͽ��ļ���/" + DataSet.COUNT)
	private long countChildren() {
		return ServicesLoader.get(DocumentService.class).countContainer(new BasicDBObject("parent_id", _id), domain);
	}

	@Override
	@Label
	public String toString() {
		return name;
	}

	public String domain;

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

	public VaultFolder setName(String name) {
		this.name = name;
		return this;
	}

	public VaultFolder setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
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

}