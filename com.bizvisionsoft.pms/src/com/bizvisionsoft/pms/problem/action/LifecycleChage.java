package com.bizvisionsoft.pms.problem.action;

import java.util.Optional;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class LifecycleChage {

	/**
	 * ICA, ע�����Ⲣ������ڽ������ PCA, ������ڽ����������δʵʩLong Term Corrective Action developed
	 * but not implemented. pcaCplt imp, ʵʩ���ڽ����������ܲ���֤Ч��Long Term Corrective Action
	 * implemented. Monitoring/evaluating effectiveness. implCplt
	 * iv���ڽ��������֤��Ч���᰸Long Term Corrective Action confirmed effective. valdCplt
	 */
	@Inject
	private String stage;

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.ACTION) Action a) {
		if (Check.isAssigned(stage)) {
			String msg = Optional.ofNullable(a.getText()).orElse(a.getName());
			if (br.confirm("ȷ��", "��ȷ��ִ�в���<br>" + msg)) {
				Problem pb = context.getRootInput(Problem.class, false);
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", pb.get_id()))
						.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
				Services.get(ProblemService.class).updateProblems(fu);
				Layer.message("�����" + msg);
			}
		} else {
			// TODO stageû��ע�����
		}
	}

}
