package com.bizvisionsoft.pms.problem.action;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProblemLifecycle {

	@Inject
	private IBruiService br;

	@Inject
	private String action;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context) {
		ObjectId _id = getProblemId(context);
		if ("start".equals(action)) {
			doStart(context, _id);
		} else if ("close".equals(action)) {
			doClose(context, _id);
		} else if ("cancel".equals(action)) {
			doCancel(context, _id);
		}
	}

	private void doCancel(BruiAssemblyContext context, ObjectId _id) {
		if (br.confirm("ȡ������", "��ȷ��ȡ����������")) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
					.set(new BasicDBObject("status", "��ȡ��").append("cancelInfo", br.operationInfo().encodeBson())).bson();
			if (Services.get(ProblemService.class).updateProblems(fu) > 0) {
				Layer.message("������ȡ��");
				refreshContentPart(context);
			}
		}
	}

	private void doClose(BruiAssemblyContext context, ObjectId _id) {
		if (br.confirm("�ر�����", "��ȷ�Ϲر����⡣")) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
					.set(new BasicDBObject("status", "�ѹر�").append("closeInfo", br.operationInfo().encodeBson())).bson();
			if (Services.get(ProblemService.class).updateProblems(fu) > 0) {
				Layer.message("�����ѹر�");
				refreshContentPart(context);
			}
		}
	}

	private void doStart(BruiAssemblyContext context, ObjectId _id) {
		if (br.confirm("����������", "��ȷ����������������")) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
					.set(new BasicDBObject("status", "�����").append("initInfo", br.operationInfo().encodeBson())).bson();
			if (Services.get(ProblemService.class).updateProblems(fu) > 0) {
				Layer.message("����������������");
				refreshContentPart(context);
			}
		}
	}

	private void refreshContentPart(BruiAssemblyContext context) {
		Object content = context.getContent();
		if (content instanceof GridPart) {
			((GridPart) content).remove(context.getFirstElement());
		} else if (content == null) {
			br.switchPage("�������", null);
		}
	}

	private ObjectId getProblemId(BruiAssemblyContext context) {
		Object element = context.getRootInput();
		if (element instanceof Problem) {
			return ((Problem) element).get_id();
		}
		element = context.getFirstElement();
		if (element instanceof Problem) {
			return ((Problem) element).get_id();
		}
		return null;
	}

}
