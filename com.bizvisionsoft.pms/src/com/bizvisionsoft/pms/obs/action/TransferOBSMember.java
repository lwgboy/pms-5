package com.bizvisionsoft.pms.obs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;

public class TransferOBSMember {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object rootInput, @MethodParam(Execute.CONTEXT_INPUT_OBJECT) Object input,
			@MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (br.confirm("�ƽ�", "��ȷ�Ͻ� " + em + " �Ĺ��������ƽ���")) {
			// ������Ŀ�Ŷӳ�Աѡ������ѡ���ƽ���Ա
			new Selector(br.getAssembly("��Ŀ�Ŷ�ѡ����.selectorassy"), context).setInput(rootInput).setTitle("ѡ�����ƽ���").open(l -> {
				Services.get(WorkService.class).transferWorkUser(((OBSItem) input).getScope_id(), ((User) em).getUserId(),
						((OBSItemWarpper) l.get(0)).getUserId(), br.getCurrentUserId(), br.getDomain());
			});
		}
	}
}
