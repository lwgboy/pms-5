package com.bizvisionsoft.pms.filecabinet;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.DocuSetting;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateDocuSettingACT {

	@Inject
	private IBruiService brui;

	// 已经把模板和项目的分开，没有必要用Behavior设置，所以注释以下代码
	// @Behavior("输出文档/创建工作包文档模板设置")
	// private boolean behaviour(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT)
	// Object input) {
	// return input instanceof ProjectTemplate;
	// }

	@Execute
	public void execute(final @MethodParam(Execute.CONTEXT) IBruiContext context) {
		WorkPackage wp = (WorkPackage) context.getInput();
		DocuSetting docu = new DocuSetting()//
				.setWorkPackage_id(wp.get_id())//
				.setName(wp.description);
		Editor.open("编辑输出文档设置", context, docu, (r, t) -> {
			((GridPart) context.getContent()).insert(Services.get(DocumentService.class).createDocumentSetting(t));
		});

	}

}
