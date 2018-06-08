package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.mongodb.BasicDBObject;

public class SearchMonthCostAnalysisYearACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		Editor<Document> editor = Editor.create("��ĿԤ��ɱ��Աȷ�������ѯ", context, new Document(), false)
				.setTitle("Ԥ��ɱ����·�������ѯ");
		if (Window.OK == editor.open()) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			BasicDBObject dbo = (BasicDBObject) editor.getResult();
			String startPeriod = getPeriod(dbo.getDate("date1"));
			MonthCostCompositionAnalysisASM content = (MonthCostCompositionAnalysisASM) context
					.getChildContextByAssemblyName("Ԥ��ɱ����·������").getContent();
			content.setOption(startPeriod);
		}

	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		return result;
	}
}
