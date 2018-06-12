package com.bizvisionsoft.pms.workreport;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.WorkReport;
import com.mongodb.BasicDBObject;

public class EditWorkReportInfoACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {

		WorkReport input = (WorkReport) context.getInput();
		Editor.open("工作报告说明信息编辑器", context, input, (r, i) -> {
			ServicesLoader.get(WorkService.class).updateWorkReport(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", i.get_id())).set(Util.getBson((WorkReport) i, "_id")).bson());
			AUtil.simpleCopy(i, input);
			// TODO 刷新
			// IBruiContext bruiContext = context.getChildContextByName("工作报告说明信息面板");
			// InfopadPart content = (InfopadPart) bruiContext.getContent();
		});
	}
}
