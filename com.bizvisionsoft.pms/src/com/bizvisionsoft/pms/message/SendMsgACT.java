package com.bizvisionsoft.pms.message;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.NewMessage;
import com.bizvisionsoft.serviceconsumer.Services;

public class SendMsgACT {

	@Inject
	private BruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Editor.create("新消息", context, new NewMessage(), false).ok((r, o) -> {
			o.sender = br.getCurrentConsignerInfo();
			Services.get(CommonService.class).sendMessage(o);
			Layer.message("消息已发送");
		});
	}

}
