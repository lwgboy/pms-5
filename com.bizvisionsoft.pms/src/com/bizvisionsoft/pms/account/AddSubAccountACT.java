package com.bizvisionsoft.pms.account;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.AccountItem;

/**
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
public class AddSubAccountACT {

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			Object input;
			if (parent instanceof AccountIncome) {
				input = new AccountIncome().setParentId(((AccountIncome) parent).getId());
			} else if (parent instanceof AccountItem) {
				input = new AccountItem().setParentId(((AccountItem) parent).getId());
			} else {
				return;
			}
			Editor.create("财务科目编辑器", context, input, true).ok((r, o) -> {
				GridPart grid = (GridPart) context.getContent();
				grid.doCreateSubItem(parent, o);
			});

		});
	}

}
