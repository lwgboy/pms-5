package com.bizvisionsoft.pms.problem.action;

import java.util.Arrays;

import org.bson.types.ObjectId;

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
		ProblemService service = Services.get(ProblemService.class);
		Problem pb = context.getRootInput(Problem.class, false);
		ObjectId _id = pb.get_id();
		pb = service.get(_id);

		if ("icaConfirmed".equals(stage)) {
			// ���ICA�Ѿ�ȷ�ϣ���ʾ
			if (pb.getIcaConfirmed() != null) {
				Layer.message("��ʱ�����ж�����Ч����ȷ��");
			} else {
				// ���ICAδ����֤����Ҫ��ʾ
				BasicDBObject filter = new BasicDBObject("problem_id", _id).append("$or", Arrays.asList(
						new BasicDBObject("verification", null), new BasicDBObject("verification.title", new BasicDBObject("$ne", "����֤"))));
				boolean verified = service.countActions(filter, "ica") == 0;
				filter = new BasicDBObject("problem_id", _id).append("finish", new BasicDBObject("$ne", true));
				boolean finished = service.countActions(filter, "ica") == 0;
				String msg = "��ȷ�ϣ���ʱ�����ж��Ƿ�����Ч�����˿ͣ������ڲ��˿ͣ���������Ӱ�죿";
				if (!finished) {
					msg = "��ʱ�����ж�<span style='color:red'>��δȫ�����</span><br><br>" + msg;
				}
				if (!verified) {
					msg = "��ʱ�����ж�<span style='color:red'>��δȫ����֤</span><br>" + msg;
				}
				if (br.confirm("ȷ����ʱ�����ж���Ч", msg)) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblems(fu);
					Layer.message("��ʱ�����ж�����Ч����ȷ��");
				} else {
					Layer.message("��ʱ�����ж�����Ч��ȷ����ȡ��");
				}
			}
		} else if ("pcaApproved".equals(stage)) {
			if (pb.getPcaApproved() != null) {
				Layer.message("���þ�����ʩ����׼");
			} else {
				if (br.confirm("��׼���þ�����ʩ", "��ȷ�ϣ���׼ʵʩ���þ�����ʩ��")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblems(fu);
					Layer.message("���þ�����ʩ����׼");
				} else {
					Layer.message("���þ�����ʩ��׼��ȡ��");
				}
			}
		} else if ("pcaValidated".equals(stage)) {
			if (pb.getPcaApproved() == null) {
				Layer.error("���þ�����ʩ��δ�õ���׼");
				return;
			}
			if (pb.getPcaValidated() != null) {
				Layer.message("���þ�����ʩ��ʵʩЧ����ͨ����֤");
			} else {
				if (br.confirm("��֤���þ�����ʩʵʩЧ��", "��ȷ�ϣ����þ�����ʩ��ʵʩЧ����ͨ����֤")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblems(fu);
					Layer.message("���þ�����ʩ��ʵʩЧ����ͨ����֤");
				} else {
					Layer.message("���þ�����ʩ��ʵʩЧ����֤��ȡ��");
				}
			}
		} else if ("pcaConfirmed".equals(stage)) {
			if (pb.getPcaValidated() == null) {
				Layer.error("���þ�����ʩ��ʵʩЧ����δ�����֤");
				return;
			}
			// ���PCA�Ѿ�ȷ�ϣ���ʾ
			if (pb.getPcaConfirmed() != null) {
				Layer.message("���þ�����ʩ����Ч����ȷ��");
			} else {
				// ���ICAδ����֤����Ҫ��ʾ
				BasicDBObject filter = new BasicDBObject("problem_id", _id).append("$or", Arrays.asList(
						new BasicDBObject("verification", null), new BasicDBObject("verification.title", new BasicDBObject("$ne", "����֤"))));
				boolean verified = service.countActions(filter, "pca") == 0;
				filter = new BasicDBObject("problem_id", _id).append("finish", new BasicDBObject("$ne", true));
				boolean finished = service.countActions(filter, "pca") == 0;
				String msg = "��ȷ�ϣ����þ�����ʩ��Ч�������ѱ���ȫ������";
				if (!finished) {
					msg = "���þ�����ʩ<span style='color:red'>��δȫ�����</span><br><br>" + msg;
				}
				if (!verified) {
					msg = "���þ�����ʩ<span style='color:red'>��δȫ����֤</span><br>" + msg;
				}
				if (br.confirm("ȷ�����þ�����ʩ��Ч", msg)) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblems(fu);
					Layer.message("���þ�����ʩ����Ч����ȷ��");
				} else {
					Layer.message("���þ�����ʩ����Ч��ȷ����ȡ��");
				}
			}
		}

	}

}
