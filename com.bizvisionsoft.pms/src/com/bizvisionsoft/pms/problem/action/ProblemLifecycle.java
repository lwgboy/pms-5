package com.bizvisionsoft.pms.problem.action;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
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
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		ObjectId _id = getProblemId(context);
		if ("start".equals(action)) {
			doStart(context, _id, userId);
		} else if ("close".equals(action)) {
			doClose(context, _id, userId);
		} else if ("cancel".equals(action)) {
			doCancel(context, _id, userId);
		}
	}

	private void doCancel(BruiAssemblyContext context, ObjectId _id, String userId) {
		if (br.confirm("ȡ������", "��ȷ��ȡ����������")) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
					.set(new BasicDBObject("status", "��ȡ��").append("cancelInfo", br.operationInfo().encodeBson())).bson();
			if (Services.get(ProblemService.class).updateProblemsLifecycle(fu,"canceled") > 0) {
				Layer.message("������ȡ��");
				refreshContentPart(context);
			}
		}
	}

	private void doClose(BruiAssemblyContext context, ObjectId _id, String userId) {
		if (!Services.get(ProblemService.class).hasPrivate(_id, ProblemService.ACTION_PROBLEM_CLOSE, userId)) {
			Layer.error("��Ҫ�Ŷ��鳤�����ⴴ����ִ������ر�");
			return;
		}
		if (br.confirm("�ر�����", "��ȷ�Ϲر����⡣")) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
					.set(new BasicDBObject("status", "�ѹر�").append("closeInfo", br.operationInfo().encodeBson())).bson();
			if (Services.get(ProblemService.class).updateProblemsLifecycle(fu,"closed") > 0) {
				Layer.message("�����ѹر�");
				refreshContentPart(context);
			}
		}
	}

	private void doStart(BruiAssemblyContext context, ObjectId _id, String userId) {
		if (br.confirm("����������", "��ȷ����������������")) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
					.set(new BasicDBObject("status", "�����").append("initInfo", br.operationInfo().encodeBson())).bson();
			if (Services.get(ProblemService.class).updateProblemsLifecycle(fu,"started") > 0) {
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

	private ObjectId getProblemId(IBruiContext context) {
		if (context == null) {
			return null;
		}
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

	@Behavior({ "�ر�", "ȡ��" })
	private boolean enableCancel(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object element,
			@MethodParam(Execute.CURRENT_USER_ID) String userId) {
		if (element instanceof Problem) {
			return Services.get(ProblemService.class).hasPrivate(((Problem) element).get_id(), ProblemService.ACTION_PROBLEM_CLOSE, userId);
		}
		return false;
	}

}
