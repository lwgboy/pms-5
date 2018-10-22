package com.bizvisionsoft.pms.projecttemplate;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WBSModule;

public class OpenProjectTemplateACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof WBSModule) {
				brui.openContent(brui.getAssembly("��Ŀģ�����ͼ"), em);
			} else if (em instanceof OBSModule) {
				// ��ǰѡ��Ϊ��֯ģ��ʱ����OBSģ����֯�ṹͼ������֯ģ��ı༭
				brui.openContent(brui.getAssembly("OBSģ����֯�ṹͼ"), em, e -> {
					OBSModule obs = ServicesLoader.get(ProjectTemplateService.class)
							.getOBSModule(((OBSModule) em).get_id());
					GridPart viewer = (GridPart) context.getContent();
					AUtil.simpleCopy(obs, em);
					viewer.refresh(em);
				});
			} else {
				brui.switchPage("��Ŀģ��", ((ProjectTemplate) em).get_id().toHexString());
			}
		});
	}

}
