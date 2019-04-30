package com.bizvisionsoft.pms.bpm;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.ProcessDefinition;

public class SelectAndLaunchProcess {

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		Selector.create("用户工作流定义选择器", context, null).setTitle("请选择将要发起的流程").open(list -> {
			if (list != null && list.size() > 0) {
				ProcessDefinition pd = (ProcessDefinition) list.get(0);
				Long id = BPMClient.startProcess(context, pd, userId);
				if (id != null)
					Layer.message("启动流程：<br>" + pd.getName());
				else
					Layer.message("启动流程已取消：<br>" + pd.getName());
			}
		});
	}

}
