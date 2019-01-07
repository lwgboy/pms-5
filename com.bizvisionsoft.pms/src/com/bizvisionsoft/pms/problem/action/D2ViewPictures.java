package com.bizvisionsoft.pms.problem.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.Document;

import com.bizivisionsoft.widgets.tools.WidgetHandler;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Problem;

public class D2ViewPictures {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		String filter = "";
		try {
			filter = URLEncoder.encode(new Document("problem_id", problem.get_id()).toJson(), "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		String url = "bvs/imgf?c=d2ProblemPhoto&filter="+ filter + "&f=problemImg&alt=problemImgDesc";
		WidgetHandler.getHandler().viewPicture(url);
	}
}
