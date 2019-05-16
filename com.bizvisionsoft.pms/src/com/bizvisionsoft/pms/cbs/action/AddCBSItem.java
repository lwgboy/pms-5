package com.bizvisionsoft.pms.cbs.action;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.model.CBSItem;
/**
 * ȡ����CBS�ɷֽ⹦�ܣ��ò�����ʱ��Ч��
 * @author gdiyang
 * @date 2018/10/27
 *
 */
@Deprecated
public class AddCBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			Editor.create("�ɱ���༭��", context, CBSItem.getInstance((CBSItem) parent), true).setTitle("�������")
					.ok((r, o) -> {
						BudgetCBS cbsGrid = (BudgetCBS) context.getContent();
						AUtil.simpleCopy(cbsGrid, o);
						cbsGrid.addCBSItem((CBSItem) parent, o);
					});

		});
	}
}
