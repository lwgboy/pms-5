package com.bizvisionsoft.jz;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;

public class PLMObject {

	public static int TYPE_WORKSPACE = 1;

	public static int TYPE_FOLDER = 2;

	public static int TYPE_PART = 3;

	public static int TYPE_EPM = 4;

	public static int TYPE_DOCUMENT = 5;

	public static int TYPE_PROCESS = 6;

	public static int TYPE_PURCHASE = 7;

	public static int TYPE_PRODUCTION = 8;

	public static int TYPE_MES = 9;

	public static int TYPE_QMS = 10;

	private Map<String, Object> values;

	public int type;

	private List<PLMObject> children;

	public void addValue(String name, String value) {
		if (values == null) {
			values = new HashMap<String, Object>();
		}
		values.put(name, value);
	}

	public Object getValue(String name) {
		return values.get(name);
	}

	@ReadValue("name")
	private String getName() {
		return values.get("name").toString();
	}

	@Structure("ѡȡPLM����/list")
	public List<PLMObject> getChildren() {
		if (children == null) {
			loadChildren();
		}

		return children;
	}

	private void loadChildren() {
		try {
			if (TYPE_WORKSPACE == type) {
				children = new Distribution().getPLMRootFolder(Arrays.asList(values.get("workspaceId").toString()));
			} else if (TYPE_FOLDER == type) {
				children = new Distribution().getPLMFolder(Arrays.asList(values.get("id").toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Structure("ѡȡPLM����/count")
	public long countChildren() {
		if (children == null) {
			loadChildren();
		}
		return children.size();
	}

	@ReadValue("��Ŀ��������/stage")
	public String getQMSStage() {
		return (String) values.get("stage");
	}

	@ReadValue("��Ŀ��������/dept")
	public String getQMSDept() {
		return (String) values.get("dept");
	}

	@ReadValue("��Ŀ��������/arms")
	public String getQMSArms() {
		return (String) values.get("arms");
	}

	@ReadValue("��Ŀ��������/phenomenon")
	public String getQMSPhenomenon() {
		return (String) values.get("phenomenon");
	}

	@ReadValue("��Ŀ��������/type")
	public String getQMSType() {
		return (String) values.get("type");
	}

	@ReadValue("��Ŀ��������/glqk")
	public String getQMSglqk() {
		return (String) values.get("glqk");
	}

	@ReadValue("��Ŀ��������/source")
	public String getQMSSsource() {
		return (String) values.get("stage");
	}

	@ReadValue("��Ŀ��������/finishDate")
	public String getQMSFinishDate() {
		return (String) values.get("finishDate");
	}

	@ReadValue("��Ŀ��������/jzcs")
	public String getQMSjzcs() {
		return (String) values.get("jzcs");
	}

}
