package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("folder")
public class Folder {

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

	@ImageURL("��Ŀ�������ļ���/folderName")
	private String getIcon() {
		if (parent_id == null) {
			return "/img/cabinet_c.svg";
		} else if (opened) {
			return "/img/folder_opened.svg";
		} else {
			return "/img/folder_closed.svg";
		}
	}

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue("��Ŀ�������ļ���/folderName")
	private String getHTMLName() {
		String html = "<div style='display:inline-flex;justify-content:space-between;width:100%;padding-right:48px;'>"
				+ name;
		html += "<a href='open/' target='_rwt'><i class='layui-icon layui-btn layui-btn-primary layui-btn-xs' style='cursor:pointer;'>&#xe671;</i></a></div>";
		return html;
	}

	@Override
	@Label
	public String toString() {
		return name;
	}

	@Structure(DataSet.LIST)
	private List<Folder> listChildren() {
		return ServicesLoader.get(ProjectService.class).listChildrenFolder(_id);
	}

	@Structure(DataSet.COUNT)
	private long countChildren() {
		return ServicesLoader.get(ProjectService.class).countChildrenFolder(_id);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectId get_id() {
		return _id;
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

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

}
