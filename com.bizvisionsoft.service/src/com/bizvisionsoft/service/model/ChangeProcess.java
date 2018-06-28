package com.bizvisionsoft.service.model;

import java.util.Map;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadOptions;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("changeProcess")
public class ChangeProcess {

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	@WriteValue
	@ReadValue
	@Persistence
	private String taskName;

	private String projectOBSId;

	private String projectOBSName;

	@ReadOptions("projectOBS")
	public Map<String, String> getSystemOBSRole() {
		return ServicesLoader.get(CommonService.class).getDictionary("½ÇÉ«Ãû³Æ");
	}

	@WriteValue("projectOBS")
	public void writeProjectOBS(String projectOBSInfo) {
		if (projectOBSInfo != null) {
			projectOBSId = projectOBSInfo.split("#")[0];
			projectOBSName = projectOBSInfo.split("#")[1];
		}
	}

	@ReadValue("projectOBS")
	public String readProjectOBS() {
		if (projectOBSId != null) {
			return projectOBSId + "#" + projectOBSName;
		}
		return "";
	}

	@ReadValue("projectOBSInfo")
	public String getProjectOBSInfo() {
		if (projectOBSId != null) {
			return projectOBSName + "[" + projectOBSId + "]";
		}
		return "";
	}

	public String getProjectOBSId() {
		return projectOBSId;
	}
	
	@Label
	public String toString() {
		return taskName;
	}
}
