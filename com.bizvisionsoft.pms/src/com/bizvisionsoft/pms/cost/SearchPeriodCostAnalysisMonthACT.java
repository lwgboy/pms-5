package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.mongodb.BasicDBObject;

public class SearchPeriodCostAnalysisMonthACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		Editor<Document> editor = Editor.create("�ɱ�������ѯ", context, new Document(), false).setTitle("�ɱ���ɷ�������ѯ");
		if (Window.OK == editor.open()) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			BasicDBObject dbo = (BasicDBObject) editor.getResult();
			String startPeriod = getPeriod(dbo.getDate("date1"));
			String endPeriod = getPeriod(dbo.getDate("date2"));

			PeriodCostCompositionAnalysisASM content = (PeriodCostCompositionAnalysisASM) context
					.getChildContextByAssemblyName("�ɱ���ɷ������-�ڼ�").getContent();

			Object input = context.getInput();
			ObjectId scope_id = null;
			// �ӳɱ��������Ŀ�ɱ�����ʱ������contextInputΪCBSItem
			if (input instanceof CBSItem) {
				scope_id = ((CBSItem) input).getScope_id();

			} else {
				// ��Ŀ���׶δ���Ŀ�ɱ�����ʱ���contextRootInput��ȡ����Ĳ���
				if (input == null) {
					input = context.getRootInput();
				}
				if (input != null) {
					ICBSScope cbsScope = (ICBSScope) input;
					scope_id = cbsScope.getScope_id();
				}
			}

			content.setOption(startPeriod, endPeriod, scope_id);
		}

	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		result += String.format("%02d", period.get(java.util.Calendar.MONTH) + 1);
		return result;
	}
}
