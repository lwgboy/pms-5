package com.bizvisionsoft.pms.obs;

import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.TreePart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddOBSModuleACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();
		context.selected(o -> {
			Selector.open("��֯ģ��ѡ����", context, project, l -> {
				boolean overide = false;
				// ����ظ��Ľ�ɫ
				OBSService obsService = Services.get(OBSService.class);
				boolean duplicated = obsService.isRoleNumberDuplicated(((OBSModule) l.get(0)).get_id(),
						project.get_id());
				if (duplicated && brui.confirm("�����֯ģ��", "���ڱ����ͬ�Ľ�ɫ����ȷ���Ƿ񴴽�����ظ��Ľ�ɫ��")) {
					overide = true;
				}
				// ���ģ��
				List<OBSItem> obsModules = obsService.addOBSModule(((OBSModule) l.get(0)).get_id(),
						((OBSItem) o).get_id(), overide);
				TreePart tree = (TreePart) context.getContent();
				tree.addAll(o, obsModules);
				Layer.message("��֯ģ������ӵ�����Ŀ�Ŷӡ�");
			});
		});

	}
}
