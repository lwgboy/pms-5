package com.bizvisionsoft.pms.filecabinet;

import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;

public class SetDocuTemplateOfPackageACT {

	@Inject
	private IBruiService brui;

	@Behavior("����ĵ�/�����ĵ�ģ��")
	private boolean behaviour(@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		return input instanceof ProjectTemplate;
	}

	@Execute
	public void execute(final @MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object root = context.getRootInput();
		WorkPackage wp = (WorkPackage) context.getInput();
		Selector.open("��Ŀģ���ļ���ѡ��", context, root, em -> {
			Docu docu = new Docu()//
					.setCreationInfo(brui.operationInfo())//
					.addWorkPackageId(wp.get_id())//
					.setFolder_id(((FolderInTemplate) em.get(0)).get_id())//
					.setName(wp.description);
			Editor.open("ʹ���ĵ�ģ��༭��", context, docu, (r, t) -> {
				((GridPart) context.getContent()).insert(Services.get(DocumentService.class).createDocument(t));
			});
		});
	}

}
