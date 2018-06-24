package com.bizvisionsoft.pms.project.dataset;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProjectSchedule {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@Init
	private void init() {
	}

	@DataSet("阶段选择器列表/list")
	private List<Work> listStage(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id) {
		return Services.get(ProjectService.class).listStage(parent_id);
	}

	@DataSet("阶段选择器列表/count")
	private long countStage(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id) {
		return Services.get(ProjectService.class).countStage(parent_id);
	}

	@DataSet({ "项目WBS/list", "进度计划和监控/list" , "进度计划和监控（查看）/list" , "进度计划/list","进度计划（查看）/list"})
	private List<Work> listRootTask(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (input instanceof Project) {
			return Services.get(WorkService.class).listProjectRootTask(((Project) input).get_id());
		} else if (input instanceof Work) {
			return Services.get(WorkService.class).listChildren(((Work) input).get_id());
		} else {
			// TODO 其他类型
			return new ArrayList<Work>();
		}
	}

	@DataSet({ "项目WBS/count", "进度计划和监控/count","进度计划和监控（查看）/count","进度计划/count","进度计划（查看）/count" })
	private long countRootTask(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (input instanceof Project) {
			return Services.get(WorkService.class).countProjectRootTask(((Project) input).get_id());
		} else if (input instanceof Work) {
			return Services.get(WorkService.class).countChildren(((Work) input).get_id());
		} else {
			// TODO 其他类型
			return 0l;
		}

	}

}
