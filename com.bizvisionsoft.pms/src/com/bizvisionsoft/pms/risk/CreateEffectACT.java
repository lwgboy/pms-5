package com.bizvisionsoft.pms.risk;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateEffectACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		RiskEffect re = new RiskEffect().setProject_id(((Project) context.getRootInput()).get_id());
		context.selected(c -> {
			re.setRBSItem_id(((RBSItem) c).get_id());
			boolean positive = false;
			String title = "";
			if ("�������Ӱ��Ĺ���".equals(((Action) event.data).getName())) {
				positive = true;
				title = "����Ӱ��";
			} else if ("��Ӹ���Ӱ��Ĺ���".equals(((Action) event.data).getName())) {
				positive = false;
				title = "����Ӱ��";
			}
			re.setPositive(positive);
			Editor.create("����Ӱ��༭��", context, re, false).setTitle(title).ok((r, o) -> {
				o = Services.get(RiskService.class).addRiskEffect(o);
			});
		});
	}

}
