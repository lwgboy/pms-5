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
			Selector.open("组织模板选择器", context, project, l -> {
				boolean overide = false;
				// 检查重复的角色
				OBSService obsService = Services.get(OBSService.class);
				boolean duplicated = obsService.isRoleNumberDuplicated(((OBSModule) l.get(0)).get_id(),
						project.get_id());
				if (duplicated && brui.confirm("添加组织模块", "存在编号相同的角色，请确认是否创建编号重复的角色。")) {
					overide = true;
				}
				// 添加模块
				List<OBSItem> obsModules = obsService.addOBSModule(((OBSModule) l.get(0)).get_id(),
						((OBSItem) o).get_id(), overide);
				TreePart tree = (TreePart) context.getContent();
				tree.addAll(o, obsModules);
				Layer.message("组织模块已添加到本项目团队。");
			});
		});

	}
}
