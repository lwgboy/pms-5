package com.bizvisionsoft.pms.project.dataset;

import java.util.List;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;

public class ReadonlyGantt {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private IWBSScope workScope;

	@Init
	private void init() {
		workScope = (IWBSScope) context.getRootInput();
	}

	@DataSet({ "��Ŀ����ͼ/data", "��Ŀ����ͼ���ޱ��鿴��/data" })
	public List<Work> data() {
		return workScope.createGanttTaskDataSet();
	}

	@DataSet({ "��Ŀ����ͼ/links", "��Ŀ����ͼ���ޱ��鿴��/links" })
	public List<WorkLink> links() {
		return workScope.createGanttLinkDataSet();
	}
}
