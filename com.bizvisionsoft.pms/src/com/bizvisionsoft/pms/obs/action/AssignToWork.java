package com.bizvisionsoft.pms.obs.action;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class AssignToWork {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (input instanceof Project) {
			ObjectId project_id = ((Project) input).get_id();
			// 检查是否存在需要覆盖原有工作分配的工作
			if (Services.get(WorkService.class).checkCoverWork(project_id)) {
				// 存在时，则提示询问是否覆盖原有的工作分配
				IDialogConstants constants = IDialogConstants.get();
				MessageDialog d = new MessageDialog(br.getCurrentShell(), "分配工作", null,
						"请确认是否将角色的担任者分配到工作的负责人和指派者，并覆盖原有的工作分配", MessageDialog.QUESTION_WITH_CANCEL,
						new String[] { constants.CANCEL_LABEL, constants.NO_LABEL, constants.YES_LABEL }, 2);
				d.buttonStyle = MessageDialog.getButtonStyle(MessageDialog.QUESTION_WITH_CANCEL);

				// 获取用户操作按钮索引
				int open = d.open();
				// 选择取消时,直接结束操作
				if (open == 0) {
					return;
				}
				// 选择"是"时,设置需要覆盖已经分配的工作.
				boolean cover = false;
				if (open == 2) {
					cover = true;
				}

				// 调用服务,为工作分配人员
				Services.get(WorkService.class).assignRoleToProject(project_id, cover);
				Layer.message("已完成角色的工作分配");
			} else if (br.confirm("分配工作", "请确认将角色的担任者分配到工作的负责人和指派者。")) {
				// 否则，提示询问是否需要分配负责人和指派者
				Services.get(WorkService.class).assignRoleToProject(project_id, false);
				Layer.message("已完成角色的工作分配");
			}
		} else if (input instanceof Work) {
			// TODO 原阶段团队分配工作功能，如不在增加阶段团队功能则去掉。
			if (br.confirm("分配工作", "请确认将角色的担任者分配到工作的负责人和指派者。")) {
				Services.get(WorkService.class).assignRoleToStage(((Work) input).get_id());
				Layer.message("已完成角色的工作分配");
			}
		}
	}
}
