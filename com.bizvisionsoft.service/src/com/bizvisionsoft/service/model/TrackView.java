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
	private String typeName = "��ͼ�͹�����";

	@SetValue("children")
	private List<Work> children;

	@Override
	@Label
	public String toString() {
		return catagory + "/" + name;
	}

	@Structure({ "�з����ȼ��/" + DataSet.LIST, "�ɹ��ƻ����/" + DataSet.LIST, "�ɹ��ƻ���أ���Ŀ����/" + DataSet.LIST,
			"�����ƻ����/" + DataSet.LIST, "�����ƻ���أ���Ŀ����/" + DataSet.LIST, "������ȼ��/" + DataSet.LIST })
	public List<Work> listWorkList() {
		return children;
	}

	@Structure({ "�з����ȼ��/" + DataSet.COUNT, "�ɹ��ƻ����/" + DataSet.COUNT, "�ɹ��ƻ���أ���Ŀ����/" + DataSet.COUNT,
			"�����ƻ���أ���Ŀ����/" + DataSet.COUNT, "�����ƻ����/" + DataSet.COUNT, "������ȼ��/" + DataSet.COUNT })
	public long countWorkList() {
		return children.size();
	}

	@Behavior("�򿪹�����")
	private boolean behaviourOpenWorkPackage() {
		return false;
	}
}
