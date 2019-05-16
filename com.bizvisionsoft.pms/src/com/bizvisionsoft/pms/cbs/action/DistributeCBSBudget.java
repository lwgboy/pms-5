package com.bizvisionsoft.pms.cbs.action;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
/**
 * ȡ����CBS�ֽ⹦�ܣ��ò�����Ч
 * @author gdiyang
 * @date 2018/10/27
 *
 */
@Deprecated
public class DistributeCBSBudget {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			new Selector(br.getAssembly("�׶�ѡ����"), context).setInput(context.getRootInput()).setTitle("����Ԥ�㵽ָ���׶�")
					.open(r -> {
						// TODO ��CBS�ڵ�����ʾ���䵽�ĸ��׶�
						// TODO �׶�ѡ��������ʾ�������
						// TODO ������ЩԤ����Է���
						// TODO ȡ���׶ε�Ԥ�����
						Work workInfo = (Work) r.get(0);
						ObjectId cbs_id = workInfo.getCBS_id();
						if (cbs_id != null) {
							br.error( "����", "�Ѿ�Ϊ�ý׶η���Ԥ�㣬�޷��ٴη��䡣");
						} else {
							CBSItem cbsItem = Services.get(CBSService.class).allocateBudget(((CBSItem) parent).get_id(),
									workInfo.get_id(), workInfo.toString(), br.getDomain());
							// TODO ���󷵻�
							// TODO �ɹ���ʾ
							BudgetCBS grid = (BudgetCBS) context.getContent();
							grid.replaceItem(parent, cbsItem);
							grid.refresh(parent);
						}
					});

		});
	}

}
