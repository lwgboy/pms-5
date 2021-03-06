package com.bizvisionsoft.pms.problem.action;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;

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
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class LifecycleChage {

	/**
	 * ICA, 注册问题并提出短期解决方案 PCA, 提出长期解决方案但并未实施Long Term Corrective Action developed
	 * but not implemented. pcaCplt imp, 实施长期解决方案，监管并验证效果Long Term Corrective Action
	 * implemented. Monitoring/evaluating effectiveness. implCplt
	 * iv长期解决方案验证有效。结案Long Term Corrective Action confirmed effective. valdCplt
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
		pb = service.get(_id, br.getDomain());
		User user = br.getCurrentUserInfo();
		if (!pb.isSolving()) {
			Layer.error("问题已关闭，无法执行该操作");
			return;
		}
		
		if ("icaConfirmed".equals(stage)) {
			if (!service.hasPrivate(_id, ProblemService.ACTION_ICA_CONFIRM, user.getUserId(), br.getDomain())) {
				Layer.error("临时控制行动有效性确认需由团队组长或顾客代表执行");
				return;
			}

			// 如果ICA已经确认，提示
			if (pb.getIcaConfirmed() != null) {
				Layer.message("临时控制行动的有效性已确认");
			} else {
				// 如果没有ICA 跳过
				BasicDBObject filter = new BasicDBObject("problem_id", _id);
				boolean noICA = service.countActions(filter, "ica", br.getDomain()) == 0;
				if (noICA) {
					Layer.error("没有临时控制行动");
					return;
				}
				// 如果ICA未经验证，需要提示
				filter = new BasicDBObject("problem_id", _id).append("$or",
						Arrays.asList(new BasicDBObject("verification", null),
								new BasicDBObject("verification.title", new BasicDBObject("$ne", "已验证"))));
				boolean verified = service.countActions(filter, "ica", br.getDomain()) == 0;
				filter = new BasicDBObject("problem_id", _id).append("finish", new BasicDBObject("$ne", true));
				boolean finished = service.countActions(filter, "ica", br.getDomain()) == 0;
				String msg = "请确认：临时控制行动是否能有效保护顾客（包括内部顾客）不受问题影响？";
				if (!finished) {
					msg = "临时控制行动<span style='color:red'>尚未全部完成</span><br><br>" + msg;
				}
				if (!verified) {
					msg = "临时控制行动<span style='color:red'>尚未全部验证</span><br>" + msg;
				}
				if (br.confirm("确认临时控制行动有效", msg)) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblemsLifecycle(fu, stage, br.getDomain());
					Layer.message("临时控制行动的有效性已确认");
				} else {
					Layer.message("临时控制行动的有效性确认已取消");
				}
			}
		} else if ("pcaApproved".equals(stage)) {
			if (!service.hasPrivate(_id, ProblemService.ACTION_PCA_APPROVE, user.getUserId(), br.getDomain())) {
				Layer.error("永久纠正措施的批准需由团队组长执行");
				return;
			}

			List<Document> selected = service.listD5(new BasicDBObject("selected", true), _id,
					RWT.getLocale().getLanguage(), br.getDomain());
			if (selected.size() == 0) {
				Layer.error("尚未选定将要实施的永久纠正措施");
				return;
			}
			if (pb.getPcaApproved() != null) {
				Layer.message("永久纠正措施已批准");
			} else {
				if (br.confirm("批准永久纠正措施", "请确认：批准实施永久纠正措施？")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblemsLifecycle(fu, stage, br.getDomain());
					Layer.message("永久纠正措施已批准");
				} else {
					Layer.message("永久纠正措施批准已取消");
				}
			}
		} else if ("pcaValidated".equals(stage)) {
			if (!service.hasPrivate(_id, ProblemService.ACTION_PCA_VALIDATE, user.getUserId(), br.getDomain())) {
				Layer.error("永久纠正措施的实施效果验证需由团队组长执行");
				return;
			}

			if (pb.getPcaApproved() == null) {
				Layer.error("永久纠正措施尚未得到批准");
				return;
			}
			if (pb.getPcaValidated() != null) {
				Layer.message("永久纠正措施的实施效果已通过验证");
			} else {
				if (br.confirm("验证永久纠正措施实施效果", "请确认：永久纠正措施的实施效果已通过验证")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblemsLifecycle(fu, stage, br.getDomain());
					Layer.message("永久纠正措施的实施效果已通过验证");
				} else {
					Layer.message("永久纠正措施的实施效果验证已取消");
				}
			}
		} else if ("pcaConfirmed".equals(stage)) {
			if (!service.hasPrivate(_id, ProblemService.ACTION_PCA_CONFIRM, user.getUserId(), br.getDomain())) {
				Layer.error("永久纠正措施的有效性确认由团队组长或顾客代表执行");
				return;
			}

			if (pb.getPcaValidated() == null) {
				Layer.error("永久纠正措施的实施效果尚未获得验证");
				return;
			}
			// 如果PCA已经确认，提示
			if (pb.getPcaConfirmed() != null) {
				Layer.message("永久纠正措施的有效性已确认");
			} else {
				// 如果ICA未经验证，需要提示
				BasicDBObject filter = new BasicDBObject("problem_id", _id).append("$or",
						Arrays.asList(new BasicDBObject("verification", null),
								new BasicDBObject("verification.title", new BasicDBObject("$ne", "已验证"))));
				boolean verified = service.countActions(filter, "pca", br.getDomain()) == 0;
				filter = new BasicDBObject("problem_id", _id).append("finish", new BasicDBObject("$ne", true));
				boolean finished = service.countActions(filter, "pca", br.getDomain()) == 0;
				String msg = "请确认：永久纠正措施有效，问题已被完全消除？";
				if (!finished) {
					msg = "永久纠正措施<span style='color:red'>尚未全部完成</span><br><br>" + msg;
				}
				if (!verified) {
					msg = "永久纠正措施<span style='color:red'>尚未全部验证</span><br>" + msg;
				}
				if (br.confirm("确认永久纠正措施有效", msg)) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
							.set(new BasicDBObject(stage, br.operationInfo().encodeBson())).bson();
					Services.get(ProblemService.class).updateProblemsLifecycle(fu, stage, br.getDomain());
					Layer.message("永久纠正措施的有效性已确认");
				} else {
					Layer.message("永久纠正措施的有效性确认已取消");
				}
			}
		}

	}

}
