package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("trackView")
public class TrackView {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String catagory;

	public String getCatagory() {
		return catagory;
	}

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue("text")
	public String getName() {
		return name;
	}

	@ReadValue
	@WriteValue
	private String viewAssembly;

	public String getViewAssembly() {
		return viewAssembly;
	}

	@ReadValue
	@WriteValue
	private String packageAssembly;

	public String getPackageAssembly() {
		return packageAssembly;
	}

	@ReadValue
	@WriteValue
	private String editAssembly;

	public String getEditAssembly() {
		return editAssembly;
	}

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "视图和工作包";

	@SetValue("children")
	private List<Work> children;

	@Override
	@Label
	public String toString() {
		return catagory + "/" + name;
	}

	@Structure({ "研发进度监控/" + DataSet.LIST, "采购计划监控/" + DataSet.LIST, "采购计划监控（项目管理）/" + DataSet.LIST,
			"生产计划监控/" + DataSet.LIST, "生产计划监控（项目管理）/" + DataSet.LIST, "检验进度监控/" + DataSet.LIST })
	public List<Work> listWorkList() {
		return children;
	}

	@Structure({ "研发进度监控/" + DataSet.COUNT, "采购计划监控/" + DataSet.COUNT, "采购计划监控（项目管理）/" + DataSet.COUNT,
			"生产计划监控（项目管理）/" + DataSet.COUNT, "生产计划监控/" + DataSet.COUNT, "检验进度监控/" + DataSet.COUNT })
	public long countWorkList() {
		return children.size();
	}

	@Behavior("打开工作包")
	private boolean behaviourOpenWorkPackage() {
		return false;
	}
}
