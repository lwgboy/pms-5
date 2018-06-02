package com.bizvisionsoft.pms.project.dataset;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectSchedule {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@Init
	private void init() {
	}

	@DataSet("�׶�ѡ�����б�/list")
	private List<Work> listStage(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id) {
		return Services.get(ProjectService.class).listStage(parent_id);
	}

	@DataSet("�׶�ѡ�����б�/count")
	private long countStage(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id) {
		return Services.get(ProjectService.class).countStage(parent_id);
	}

	@DataSet({ "��Ŀ���ȼƻ���/list", "��Ŀ���ȼƻ����鿴��/list", "���ȼƻ��ͼ��/list" })
	private List<Work> listRootTask(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (input instanceof Project) {
			return Services.get(WorkService.class).listProjectRootTask(((Project) input).get_id());
		} else if (input instanceof Work) {
			return Services.get(WorkService.class).listChildren(((Work) input).get_id());
		} else {
			// TODO ��������
			return new ArrayList<Work>();
		}
	}

	@DataSet({ "��Ŀ���ȼƻ���/count", "��Ŀ���ȼƻ����鿴��/count", "���ȼƻ��ͼ��/count" })
	private long countRootTask(@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (input instanceof Project) {
			return Services.get(WorkService.class).countProjectRootTask(((Project) input).get_id());
		} else if (input instanceof Work) {
			return Services.get(WorkService.class).countChildren(((Work) input).get_id());
		} else {
			// TODO ��������
			return 0l;
		}

	}

	@DataSet("��Ŀ���ȼƻ���/" + DataSet.UPDATE)
	private long update(BasicDBObject filterAndUpdate) {
		return Services.get(WorkService.class).updateWork(filterAndUpdate);
	}

}
