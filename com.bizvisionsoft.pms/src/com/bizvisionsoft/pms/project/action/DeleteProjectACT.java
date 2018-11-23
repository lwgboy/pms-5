package com.bizvisionsoft.pms.project.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectScheduleInfo;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class DeleteProjectACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {

		context.selected(se -> {
			Project project;
			if (se instanceof ProjectScheduleInfo)
				project = ((ProjectScheduleInfo) se).getProject();
			else
				project = (Project) se;
			String message = Optional.ofNullable(AUtil.readTypeAndLabel(project)).map(m -> "请确认将要删除 " + m).orElse("请确认将要删除选择的记录。");
			if (br.confirm("删除", message)) {
				Services.get(ProjectService.class).delete(project.get_id());
				Check.instanceThen(context.getContent(), GridPart.class, grid -> grid.remove(se));
				Layer.message(Optional.ofNullable(AUtil.readLabel(project)).map(m -> "已删除 " + m).orElse("已删除"));
			}
		});
	}
}
