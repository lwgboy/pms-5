package com.bizvisionsoft.pms.bpm;

import java.util.List;

import org.bson.Document;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.serviceconsumer.Services;

public class TaskCardACT {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private BPMService service;

	public TaskCardACT() {
		service = Services.get(BPMService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document doc, @MethodParam(Execute.EVENT) Event e) {
		if ("nominate".equals(e.text)) {
			nominate(doc);
		} else if ("skip".equals(e.text)) {
			skip(doc);
		} else if ("exit".equals(e.text)) {
			exit(doc);
		} else if ("claim".equals(e.text)) {
			claim(doc);
		} else if ("start".equals(e.text)) {
			start(doc);
		} else if ("forward".equals(e.text)) {
			forward(doc);
		} else if ("suspend".equals(e.text)) {
			suspend(doc);
		} else if ("delegate".equals(e.text)) {
			delegate(doc);
		} else if ("complete".equals(e.text)) {
			complete(doc);
		} else if ("stop".equals(e.text)) {
			stop(doc);
		} else if ("resume".equals(e.text)) {
			resume(doc);
		}
	}

	private boolean resume(Document doc) {
		if (!br.confirm("继续任务", "继续执行任务，请确认。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.resumeTask(doc.getLong("_id"), userId);
	}

	private boolean stop(Document doc) {
		if (!br.confirm("停止执行", "停止执行后任务将被置于预留状态，请确认。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.stopTask(doc.getLong("_id"), userId);
	}

	private boolean complete(Document doc) {
		String userId = br.getCurrentUserId();
		Document parameters = new Document();// TODO
		service.completeTask(doc.getLong("_id"), userId, parameters);
		return true;
	}

	private boolean delegate(Document doc) {
		String userId = br.getCurrentUserId();
		String targetUserId = null;// TODO
		return service.delegateTask(doc.getLong("_id"), userId, targetUserId);
	}

	private boolean suspend(Document doc) {
		if (!br.confirm("暂停任务", "请确认暂停任务。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.suspendTask(doc.getLong("_id"), userId);
	}

	private boolean forward(Document doc) {
		String userId = br.getCurrentUserId();
		String targetUserId = null;// TODO
		return service.forwardTask(doc.getLong("_id"), userId, targetUserId);
	}

	private boolean start(Document doc) {
		if (!br.confirm("开始任务", "请确认开始任务。"))
			return false;
		String userId = br.getCurrentUserId();
		service.startTask(doc.getLong("_id"), userId);
		context.getParentContext().refresh(true);
		return true;
	}

	private boolean claim(Document doc) {
		if (!br.confirm("认领任务", "请确认认领任务。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.claimTask(doc.getLong("_id"), userId);
	}

	private boolean exit(Document doc) {
		if (!br.confirm("退出任务", "退出任务后，该将关闭任务不再执行，本操作不可回退，请确认。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.exitTask(doc.getLong("_id"), userId);
	}

	private boolean skip(Document doc) {
		if (!br.confirm("跳过任务", "跳过任务后，该将废弃任务不再执行，本操作不可回退，请确认。"))
			return false;
		String userId = br.getCurrentUserId();
		return service.skipTask(doc.getLong("_id"), userId);
	}

	private boolean nominate(Document doc) {
		String userId = br.getCurrentUserId();
		List<String> potentialOwnersUserId = null;// TODO
		return service.nominateTask(doc.getLong("_id"), userId, potentialOwnersUserId);
	}

}
