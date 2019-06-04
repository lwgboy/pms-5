package com.bizvisionsoft.pms.workreport;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.WorkReport;
import com.mongodb.BasicDBObject;

public class EditWorkReportInfoACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {

		WorkReport input = (WorkReport) context.getInput();
		Editor.open("工作报告编辑器.editorassy", context, input, (r, i) -> {
			ServicesLoader.get(WorkReportService.class).update(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", i.get_id())).set(BsonTools.getBasicDBObject((WorkReport) i, "_id")).bson(), br.getDomain());
			AUtil.simpleCopy(i, input);
			((InfopadPart) context.getContent()).reload();
		});
	}
}
