package com.bizvisionsoft.pms.project;

import org.bson.types.ObjectId;

import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.serviceconsumer.Services;

public class SwitchProjectPage {

	public static void openProject(IBruiService br, ObjectId _id) {
		Project pj = Services.get(ProjectService.class).get(_id);
		openProject(br,pj);
	}

	public static Project loadAndOpenProject(IBruiService br, Project project) {
		Project pj = Services.get(ProjectService.class).get(project.get_id());
		openProject(br,pj);
		return pj;
	}
	
	public static void openProject(IBruiService br, Project pj) {
		ObjectId project_id = pj.get_id();
		if (ProjectStatus.Created.equals(pj.getStatus())) {
			br.switchPage("项目首页（启动）", project_id.toHexString());
		} else if (ProjectStatus.Processing.equals(pj.getStatus())) {
			br.switchPage("项目首页（执行）", project_id.toHexString());
		} else if (ProjectStatus.Closing.equals(pj.getStatus())) {
			br.switchPage("项目首页（收尾）", project_id.toHexString());
		} else if (ProjectStatus.Closed.equals(pj.getStatus())) {
			br.switchPage("项目首页（关闭）", project_id.toHexString());
		}
	}
	

}
