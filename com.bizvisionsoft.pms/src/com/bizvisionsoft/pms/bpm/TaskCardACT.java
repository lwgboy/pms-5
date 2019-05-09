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
		if ("nominate".equals(e.text)) {// ָ��
			nominate(doc);
		} else if ("skip".equals(e.text)) {// ����
			skip(doc);
		} else if ("exit".equals(e.text)) {// �˳�
			exit(doc);
		} else if ("claim".equals(e.text)) {// ǩ��
			claim(doc);
		} else if ("start".equals(e.text)) {// ǩ��
			start(doc);
		} else if ("forward".equals(e.text)) {// �˻أ�����
			forward(doc);
		} else if ("suspend".equals(e.text)) {// ��ͣ
			suspend(doc);
		} else if ("delegate".equals(e.text)) {// ����
			delegate(doc);
		} else if ("complete".equals(e.text)) {// �ύ
			complete(doc);
		} else if ("stop".equals(e.text)) {// ֹͣ
			stop(doc);
		} else if ("resume".equals(e.text)) {// ����
			resume(doc);
		}
	}

	private boolean resume(Document doc) {
		if (!br.confirm("��������", "����ִ��������ȷ�ϡ�"))
			return false;
		String userId = br.getCurrentUserId();
		return service.resumeTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean stop(Document doc) {
		if (!br.confirm("ͣ������", "��ȷ��ͣ����������<br>" + getTaskDetailMsg(doc)))
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
		if (!br.confirm("��ͣ����", "��ȷ����ͣ��������<br>" + getTaskDetailMsg(doc)))
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
		if (!br.confirm("ǩ������", "��ȷ��ǩ�ղ���ʼ��������<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		service.startTask(doc.getLong("_id"), userId, br.getDomain());
		context.getParentContext().refresh(true);
		return true;
	}

	private String getTaskDetailMsg(Document doc) {
		return "���̣�" + doc.getString("process") + "<br>" + "����" + doc.getString("task");
	}

	private boolean claim(Document doc) {
		if (!br.confirm("ǩ������", "��ȷ��ǩ����������<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.claimTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean exit(Document doc) {
		if (!br.confirm("�˳�����", "�˳�����󣬸ý��ر�������ִ�У����������ɻ��ˣ���ȷ���˳���������<br>" + getTaskDetailMsg(doc)))
			return false;
		String userId = br.getCurrentUserId();
		return service.exitTask(doc.getLong("_id"), userId, br.getDomain());
	}

	private boolean skip(Document doc) {
		if (!br.confirm("��������", "��������󣬸ý�����������ִ�У����������ɻ��ˣ���ȷ��������������<br>" + getTaskDetailMsg(doc)))
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
