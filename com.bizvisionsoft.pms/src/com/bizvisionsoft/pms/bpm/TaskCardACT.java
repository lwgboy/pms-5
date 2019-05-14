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
		if ("nominate".equals(e.text)) {// ָ��
			result = client.nominateTask(context, taskId, userId);
		} else if ("skip".equals(e.text)) {// ����
			if (br.confirm("��������", "��������󣬸ý�����������ִ�У����������ɻ��ˣ���ȷ��������������<br>" + taskMsg))
			result = client.skipTask(context, taskId, userId);
		} else if ("exit".equals(e.text)) {// �˳�
			if (br.confirm("�˳�����", "�˳�����󣬸ý��ر�������ִ�У����������ɻ��ˣ���ȷ���˳���������<br>" + taskMsg))
			result = client.exitTask(context, taskId, userId);
		} else if ("claim".equals(e.text)) {// ǩ��
			if (br.confirm("ǩ������", "��ȷ��ǩ����������<br>" + taskMsg))
				result = client.claimTask(context, taskId, userId);
		} else if ("start".equals(e.text)) {// ǩ��
			if (br.confirm("ǩ������", "��ȷ��ǩ�ղ���ʼ��������<br>" + taskMsg))
				result = client.startTask(taskId, userId);
		} else if ("forward".equals(e.text)) {// �˻أ�����
			result = client.forwardTask(context, taskId, userId);
		} else if ("suspend".equals(e.text)) {// ��ͣ
			if (br.confirm("��ͣ����", "��ȷ����ͣ��������<br>" + taskMsg))
			result = client.suspendTask(context, taskId, userId);
		} else if ("delegate".equals(e.text)) {// ����
			result = client.delegateTask(context, taskId, userId);
		} else if ("complete".equals(e.text)) {// �ύ
			result = client.completeTask(context, taskId, userId);
		} else if ("stop".equals(e.text)) {// ֹͣ
			if (br.confirm("ͣ������", "��ȷ��ͣ����������<br>" + taskMsg))
				result = client.stopTask(context, taskId, userId);
		} else if ("resume".equals(e.text)) {// ����
			if (br.confirm("��������", "����ִ��������ȷ�ϡ�"))
				result = client.resumeTask(context, taskId, userId);
		}

		handleResult(result);
	}

	private void handleResult(Result result) {
		if (result == null)
			return;
		if (result.type == Result.TYPE_ERROR) {// ���磬������Ĳ�������Ҫˢ��
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
		return "���̣�" + doc.getString("process") + "<br>" + "����" + doc.getString("task");
	}

}