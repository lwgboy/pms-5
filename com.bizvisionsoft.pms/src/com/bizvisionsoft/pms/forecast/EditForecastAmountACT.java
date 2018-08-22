package com.bizvisionsoft.pms.forecast;

import org.bson.Document;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.model.AccountIncome;

public class EditForecastAmountACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(t -> {
			Editor.open("�༭����Ԥ��", context, new Document(), (r, d) -> {
				try {
					String _index = r.getString("index");
					String _amount = r.getString("amount");
					int index = Integer.parseInt(_index) - 1;
					double amount = Double.parseDouble(_amount);
					Util.ifInstanceThen(context.getContent(), RevenueForecastASM.class,
							a -> a.update(((AccountIncome) t), index, amount));
				} catch (Exception e) {
					Layer.message("����ʧ�ܡ�", Layer.ICON_CANCEL);
				}

			});
		});
	}

}
