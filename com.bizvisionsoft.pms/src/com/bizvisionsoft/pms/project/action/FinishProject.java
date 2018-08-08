package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.ResultHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class FinishProject {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		Shell shell = brui.getCurrentShell();
		boolean ok = MessageDialog.openConfirm(shell, "项目收尾", "请确认收尾项目" + project
				+ "。<br/>系统将标记当前时间为项目完工日期。<br/>收尾中的项目将不能修改计划，增加新的工作或项目变更。<br/>收尾期间仍可进行项目财务结算，整理项目文档。");
		if (!ok) {
			return;
		}

		/////////////////////////////////////////////////////////////////////////////
		// 
		final ObjectId id = project.get_id();
		ProjectService service = Services.get(ProjectService.class);
		
		ResultHandler.run(ICommand.Finish_Project, "项目收尾完成", "项目收尾失败",
				() -> service.finishProject(brui.command(id, new Date(), ICommand.Finish_Project)),//
				() -> service.finishProject(brui.command(id, new Date(), ICommand.Finish_Project_Ignore_Warrning)), //
				code -> brui.switchPage("项目首页（收尾）", id.toHexString()));

	}

}
