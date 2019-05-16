package com.bizvisionsoft.pms.cbs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.pms.cbs.assembly.BudgetSubject;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;

/**
 * Ԥ��༭��ĿԤ��ʱʹ�ã���Ӧ�÷ϳ�
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
public class EditCBSSubjectBudget {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			AccountItem account = (AccountItem) parent;
			CBSSubject period;
			Object cbs = context.getInput();
			if (cbs == null) {
				ICBSScope rootInput = (ICBSScope) context.getRootInput();
				period = new CBSSubject().setCBSItem_id(rootInput.getCBS_id()).setSubjectNumber(account.getId());
			} else if (cbs instanceof Work || cbs instanceof Project) {
				period = new CBSSubject().setCBSItem_id(((ICBSScope) cbs).getCBS_id())
						.setSubjectNumber(account.getId());
			} else {
				period = new CBSSubject().setCBSItem_id(((CBSItem) cbs).get_id()).setSubjectNumber(account.getId());
			}

			Check.instanceThen(context.getRootInput(), ICBSScope.class, r -> period.setRange(r.getCBSRange()));

			Editor.create("�ڼ�Ԥ��༭��", context, period, true).setTitle("�༭��Ŀ�ڼ�Ԥ��").ok((r, o) -> {
				BudgetSubject grid = (BudgetSubject) context.getContent();
				grid.updateCBSSubjectAmount(o);
			});

		});
	}

}
