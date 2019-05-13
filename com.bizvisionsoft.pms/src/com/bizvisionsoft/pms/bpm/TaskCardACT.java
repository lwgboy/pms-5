package com.bizvisionsoft.pms.bpm;

import java.util.List;

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
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class TaskCardACT {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private BPMService service;

	private BPMClient client;

	private static Logger logger = LoggerFactory.getLogger(TaskCardACT.class);

	public TaskCardACT() {
	}

	@Init
	public void init() {
		service = Services.get(BPMService.class);
		client = new BPMClient(br.getDomain());
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document doc, @MethodParam(Execute.EVENT) Event e) {
		String userId = br.getCurrentUserId();
		String domain = br.getDomain();
		String taskMsg = getTaskDetailMsg(doc);
		long taskId = doc.getLong("_id");

		Result result = null;
		if ("nominate".equals(e.text)) {// ָ��
			List<String> potentialOwnersUserId = null;// TODO
			result = service.nominateTask(taskId, userId, potentialOwnersUserId, domain);
		} else if ("skip".equals(e.text)) {// ����
			if (br.confirm("��������", "��������󣬸ý�����������ִ�У����������ɻ��ˣ���ȷ��������������<br>" + taskMsg))
				result = service.skipTask(taskId, userId, domain);
		} else if ("exit".equals(e.text)) {// �˳�
			if (br.confirm("�˳�����", "�˳�����󣬸ý��ر�������ִ�У����������ɻ��ˣ���ȷ���˳���������<br>" + taskMsg))
				result = service.exitTask(taskId, userId, domain);
		} else if ("claim".equals(e.text)) {// ǩ��
			if (br.confirm("ǩ������", "��ȷ��ǩ����������<br>" + taskMsg))
				result = service.claimTask(taskId, userId, domain);
		} else if ("start".equals(e.text)) {// ǩ��
			if (br.confirm("ǩ������", "��ȷ��ǩ�ղ���ʼ��������<br>" + taskMsg))
				result = client.startTask(taskId, userId);
		} else if ("forward".equals(e.text)) {// �˻أ�����
			String targetUserId = null;// TODO
			result = service.forwardTask(taskId, userId, targetUserId, domain);
		} else if ("suspend".equals(e.text)) {// ��ͣ
			if (br.confirm("��ͣ����", "��ȷ����ͣ��������<br>" + taskMsg))
				result = service.suspendTask(taskId, userId, domain);
		} else if ("delegate".equals(e.text)) {// ����
			result = client.delegateTask(context,taskId, userId);
		} else if ("complete".equals(e.text)) {// �ύ
			result = client.completeTask(context, taskId, userId);
		} else if ("stop".equals(e.text)) {// ֹͣ
			if (br.confirm("ͣ������", "��ȷ��ͣ����������<br>" + taskMsg))
				result = service.stopTask(taskId, userId, domain);
		} else if ("resume".equals(e.text)) {// ����
			if (br.confirm("��������", "����ִ��������ȷ�ϡ�"))
				result = service.resumeTask(taskId, userId, domain);
		}

		handleResult(result);
	}

	private void handleResult(Result result) {
		if (result == null)
			return;
		if (result.type == Result.TYPE_ERROR) {
			Layer.message(result.message);
			logger.error(result.message);
		} else if (result.type == Result.TYPE_INFO) {
			logger.info(result.message);
		} else if (result.type == Result.TYPE_WARNING) {
			logger.warn(result.message);
		}
		context.refresh(true);
	}

	private String getTaskDetailMsg(Document doc) {
		return "���̣�" + doc.getString("process") + "<br>" + "����" + doc.getString("task");
	}

}