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

	@Structure("选取PLM对象/list")
	public List<PLMObject> getChildren() {
		if (children == null) {
			loadChildren();
		}

		return children;
	}

	private void loadChildren() {
		try {
			if (TYPE_WORKSPACE == type) {
				children = Distribution.getPLMRootFolder(Arrays.asList(values.get("workspaceId").toString()));
			} else if (TYPE_FOLDER == type) {
				children = Distribution.getPLMFolder(Arrays.asList(values.get("id").toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Structure("选取PLM对象/count")
	public long countChildren() {
		if (children == null) {
			loadChildren();
		}
		return children.size();
	}
}
