package com.bizvisionsoft.pms.bpm;

import org.bson.Document;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Result;

public class TaskCardACT {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;


	private BPMClient client;

	private static Logger logger = LoggerFactory.getLogger(TaskCardACT.class);

	public TaskCardACT() {
	}

	@Init
	public void init() {
		client = new BPMClient(br.getDomain());
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document doc, @MethodParam(Execute.EVENT) Event e) {
		String userId = br.getCurrentUserId();
		String taskMsg = getTaskDetailMsg(doc);
		long taskId = doc.getLong("_id");

		Result result = null;
		if ("nominate".equals(e.text)) {// 指派
			result = client.nominateTask(context, taskId, userId);
		} else if ("skip".equals(e.text)) {// 跳过
			if (br.confirm("跳过任务", "跳过任务后，该将废弃任务不再执行，本操作不可回退，请确认跳过以下任务：<br>" + taskMsg))
			result = client.skipTask(context, taskId, userId);
		} else if ("exit".equals(e.text)) {// 退出
			if (br.confirm("退出任务", "退出任务后，该将关闭任务不再执行，本操作不可回退，请确认退出以下任务：<br>" + taskMsg))
			result = client.exitTask(context, taskId, userId);
		} else if ("claim".equals(e.text)) {// 签收
			if (br.confirm("签收任务", "请确认签收以下任务：<br>" + taskMsg))
				result = client.claimTask(context, taskId, userId);
		} else if ("start".equals(e.text)) {// 签收
			if (br.confirm("签收任务", "请确认签收并开始以下任务：<br>" + taskMsg))
				result = client.startTask(taskId, userId);
		} else if ("forward".equals(e.text)) {// 退回，撤回
			result = client.forwardTask(context, taskId, userId);
		} else if ("suspend".equals(e.text)) {// 暂停
			if (br.confirm("暂停任务", "请确认暂停以下任务：<br>" + taskMsg))
			result = client.suspendTask(context, taskId, userId);
		} else if ("delegate".equals(e.text)) {// 代办
			result = client.delegateTask(context, taskId, userId);
		} else if ("complete".equals(e.text)) {// 提交
			result = client.completeTask(context, taskId, userId);
		} else if ("stop".equals(e.text)) {// 停止
			if (br.confirm("停办任务", "请确认停办以下任务：<br>" + taskMsg))
				result = client.stopTask(context, taskId, userId);
		} else if ("resume".equals(e.text)) {// 继续
			if (br.confirm("继续任务", "继续执行任务，请确认。"))
				result = client.resumeTask(context, taskId, userId);
		}

		handleResult(result);
	}

	private void handleResult(Result result) {
		if (result == null)
			return;
		if (result.type == Result.TYPE_ERROR) {// 例如，不允许的操作，需要刷新
			Layer.message(result.message);
			logger.error(result.message);
			context.refresh(true);
		} else if (result.type == Result.TYPE_INFO) {
			logger.info(result.message);
		} else if (result.type == Result.TYPE_WARNING) {
			logger.warn(result.message);
		}
	}

	private String getTaskDetailMsg(Document doc) {
		return "流程：" + doc.getString("process") + "<br>" + "任务：" + doc.getString("task");
	}

}