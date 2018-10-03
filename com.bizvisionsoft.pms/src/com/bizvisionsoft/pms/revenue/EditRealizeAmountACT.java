package com.bizvisionsoft.pms.revenue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.tools.Checker;

public class EditRealizeAmountACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(t -> {
			Editor.open("编辑收益实现", context, new Document(), (r, d) -> {
				try {
					Date _index = r.getDate("index");
					String _amount = r.getString("amount");
					double amount = Double.parseDouble(_amount);
					Checker.instanceThen(context.getContent(), RealizeASM.class,
							a -> a.update(((AccountIncome) t), new SimpleDateFormat("yyyyMM").format(_index), amount));
				} catch (Exception e) {
					Layer.message("更新失败。", Layer.ICON_CANCEL);
				}

			});
		});
	}

}
