package com.bizvisionsoft.pms.bpm;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.tools.Check;

public class SelectAndLaunchProcess {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		Selector.create("�û�����������ѡ����.selectorassy", context, null).setTitle("��ѡ��Ҫ���������").open(list -> {
			if (Check.isAssigned(list)) {
				ProcessDefinition pd = (ProcessDefinition) list.get(0);
				Long id = new BPMClient(br.getDomain()).startProcess(context, pd, userId);
				if (id != null) {
					Check.instanceThen(context.getContent(), IQueryEnable.class, s -> s.doRefresh());
					Layer.message("�������̣�" + pd.getName());
				} else {
					Layer.message("����������ȡ����" + pd.getName());
				}
			}
		});
	}

}
