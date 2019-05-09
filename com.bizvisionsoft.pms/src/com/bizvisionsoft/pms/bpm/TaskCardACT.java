package com.bizvisionsoft.pms.bpm;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.JSTools;
import com.bizvisionsoft.service.tools.ServiceHelper;
import com.bizvisionsoft.serviceconsumer.Services;

public class TaskCardACT {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private BPMService service;

	private static Logger logger = LoggerFactory.getLogger(TaskCardACT.class);

	public TaskCardACT() {
		service = Services.get(BPMService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document doc, @MethodParam(Execute.EVENT) Event e) {
		if ("nominate".equals(e.text)) {// 指派
			nominate(doc);
		} else if ("skip".equals(e.text)) {// 跳过
			skip(doc);
		} else if ("exit".equals(e.text)) {// 退出
			exit(doc);
		} else if ("claim".equals(e.text)) {// 签收
			claim(doc);
		} else if ("start".equals(e.text)) {// 签收
			start(doc);
		} else if ("forward".equals(e.text)) {// 退回，撤回
			forward(doc);
		} else if ("suspend".equals(e.text)) {// 暂停
			suspend(doc);
		} else if ("delegate".equals(e.text)) {// 代办
			delegate(doc);
		} else if ("complete".equals(e.text)) {// 提交
			complete(doc);
		} else if ("stop".equals(e.text)) {// 停止
			stop(doc);
		} else if ("resume".equals(e.text)) {// 继续
			resume(doc);
		}
	}

	private boolean resume(Document doc) {
		if (!br.confirm("继续任务", "继续执行任务，请确认。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.resumeTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean stop(Document doc) {
		if (!br.confirm("停办任务", "请确认停办以下任务：<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.stopTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean complete(Document doc) {
		long taskId = doc.getLong("_id");
		new BPMClient(br.getDomain()).completeTask(context, taskId, br.getCurrentUserId());
		return true;
	}

	private boolean delegate(Document doc) {
		String userId = br.getCurrentUserId();
		String targetUserId = null;// TODO
		return service.delegateTask(doc.getLong("_id"), userId, targetUserId, br.getDomain());
	}

	private boolean suspend(Document doc) {
		if (!br.confirm("暂停任务", "请确认暂停以下任务：<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.suspendTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean forward(Document doc) {
		String userId = br.getCurrentUserId();
		String targetUserId = null;// TODO
		return service.forwardTask(doc.getLong("_id"), userId, targetUserId, br.getDomain());
	}

	private boolean start(Document doc) {
		if (!br.confirm("签收任务", "请确认签收并开始以下任务：<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		service.startTask(doc.getLong("_id"), userId, br.getDomain());
		context.getParentContext().refresh(true);
		return true;
	}

	private String getTaskDetailMsg(Document doc) {
		return "流程：" + doc.getString("process") + "<br>" + "任务：" + doc.getString("task");
	}

	private boolean claim(Document doc) {
		if (!br.confirm("签收任务", "请确认签收以下任务：<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.claimTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean exit(Document doc) {
		if (!br.confirm("退出任务", "退出任务后，该将关闭任务不再执行，本操作不可回退，请确认退出以下任务：<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.exitTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean skip(Document doc) {
		if (!br.confirm("跳过任务", "跳过任务后，该将废弃任务不再执行，本操作不可回退，请确认跳过以下任务：<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.skipTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean nominate(Document doc) {
		String userId = br.getCurrentUserId();
		List<String> potentialOwnersUserId = null;// TODO
		return service.nominateTask(doc.getLong("_id"), userId, potentialOwnersUserId, br.getDomain());
	}

}
