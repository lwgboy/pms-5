package com.bizvisionsoft.pms.workreport;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.WorkReport;

public class OpenWorkReportACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(s -> {
			br.openContent(br.getAssembly("±¨¸æÏêÇé"), s, e -> {
				if (s instanceof WorkReport) {
					WorkReport o = ServicesLoader.get(WorkReportService.class).getWorkReport(((WorkReport) s).get_id(), br.getDomain());
					GridPart viewer = (GridPart) context.getContent();
					AUtil.simpleCopy(o, s);
					viewer.update(s);
				}
			});
		});
	}
}
