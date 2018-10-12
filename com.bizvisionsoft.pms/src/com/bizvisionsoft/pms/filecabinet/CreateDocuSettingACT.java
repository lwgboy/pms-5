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

	// �Ѿ���ģ�����Ŀ�ķֿ���û�б�Ҫ��Behavior���ã�����ע�����´���
	// @Behavior("����ĵ�/�����������ĵ�ģ������")
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
		Editor.open("�༭����ĵ�����", context, docu, (r, t) -> {
			((GridPart) context.getContent()).insert(Services.get(DocumentService.class).createDocumentSetting(t));
		});

	}

}
