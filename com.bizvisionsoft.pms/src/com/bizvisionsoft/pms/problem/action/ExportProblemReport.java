package com.bizvisionsoft.pms.problem.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bson.Document;
import org.eclipse.rap.rwt.RWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.FileTools;
import com.bizvisionsoft.serviceconsumer.Services;

public class ExportProblemReport {

	public Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private String template;

	@Inject
	private String fileName;

	@Inject
	private Document rptParam;

	@Inject
	private IBruiService br;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Problem problem = context.search_sele_root(Problem.class);

		if (problem == null)
			return;

		try {
			InputStream is = Services.get(ProblemService.class).createReportAndGetDownloadPath(rptParam, problem.get_id(), template,
					fileName, RWT.getRequest().getServerName(), RWT.getRequest().getServerPort(), br.getDomain());
			File folder = br.createSessionTemplateDirectory();
			String filePath = folder.getPath() + "/" + fileName + ".zip";
			OutputStream os = null;
			try {
				os = new FileOutputStream(new File(filePath));
				byte[] bytes = new byte[1024];
				int c;
				while ((c = is.read(bytes)) != -1) {
					os.write(bytes, 0, c);
				}
			} catch (IOException e) {
				logger.error("报表存储文件失败。", e);
			} finally {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			}
			UserSession.bruiToolkit().downloadLocalFile(filePath);
		} catch (IOException e) {
			logger.error("报表生成出错。", e);
		}
	}
}
