package com.bizvisionsoft.pms.bpm;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.ProcessDefinition;

public class SelectAndLaunchProcess {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		Selector.create("�û�����������ѡ����", context, null).setTitle("��ѡ��Ҫ���������").open(list -> {
			if (list != null && list.size() > 0) {
				ProcessDefinition pd = (ProcessDefinition) list.get(0);
				launch(pd);
			}
		});
	}

	private void launch(ProcessDefinition pd) {
		// TODO Auto-generated method stub

	}

}
