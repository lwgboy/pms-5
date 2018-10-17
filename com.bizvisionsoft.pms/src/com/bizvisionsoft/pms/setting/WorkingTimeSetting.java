package com.bizvisionsoft.pms.setting;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkingTimeSetting {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Document setting = Services.get(CommonService.class).getSetting("����ʱ������");
		if (setting == null) {
			setting = new Document("name", "����ʱ������");
		}
		Editor.open("����ʱ������", context, setting, true, (d, r) -> {
			Services.get(CommonService.class).updateSetting(r);
		});
	}
	
}
