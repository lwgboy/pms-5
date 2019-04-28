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
		if (!br.confirm("��������", "����ִ��������ȷ�ϡ�"))
			return false;
		String userId = br.getCurrentUserId();
		return service.resumeTask(doc.getLong("_id"), userId);
	}

	private boolean stop(Document doc) {
		if (!br.confirm("ִֹͣ��", "ִֹͣ�к����񽫱�����Ԥ��״̬����ȷ�ϡ�"))
			return false;
		String userId = br.getCurrentUserId();
		return service.stopTask(doc.getLong("_id"), userId);
	}

	private boolean complete(Document doc) {
		long taskId = doc.getLong("_id");
		// 1. �������̲���
		Document input = new Document();
		Document processVariables = service.getProcessInstanceVariablesByTaskId(taskId);
		if (processVariables != null)
			input.putAll(processVariables);

		// 2. ������������
		TaskDefinition td = service.getTaskDefinitionByTaskId(taskId);
		if (td != null) {
			Document properties = td.getProperties();
			if (properties != null)
				input.putAll(properties);

			// 3. ǰ����
			if (!executeJS(input, td.getiScript()))
				return false;

			// 4. �򿪱༭����
			if (!editInput(properties, td.getEditor(), td.getName(), input))
				return false;

			// 5. ����
			if (!executeJS(input, td.getoScript()))
				return false;
		}

		service.completeTask(taskId, br.getCurrentUserId(), input);
		return true;
	}

	private boolean editInput(Document properties, String editorName, String title, Document input) {
		if (Check.isAssigned(editorName)) {
			Assembly assembly = ModelLoader.site.getAssemblyByName(editorName);
			if (assembly == null) {
				Layer.error("�޷�����������" + editorName);
				return false;
			} else {
				Set<String> editorFields = assembly.getFields().stream().map(fi -> fi.getName()).collect(Collectors.toSet());
				Editor<Document> editor = new Editor<Document>(assembly, context).setInput(true, input).setTitle(title).setEditable(true);
				if (Window.OK != editor.open()) {
					return false;
				} else {
					// ȥ��������ֶ�
					Set<String> toRemove = input.keySet().stream()
							.filter(s -> ((properties == null) || !properties.containsKey(s)) && !editorFields.contains(s))
							.collect(Collectors.toSet());
					toRemove.forEach(s -> input.remove(s));
					return true;
				}
			}
		} else {
			return false;
		}
	}

	private boolean executeJS(Document input, String script) {
		if (Check.isAssigned(script)) {
			try {
				Document binding = new Document("input", input).append("context", context.getContextParameterData()).append("ServiceHelper",
						new ServiceHelper());
				JSTools.invoke(script, null, "input", binding, input, context.getContextParameterData());
				return true;
			} catch (Exception e) {
				logger.error("ִ�нű�����", e);
				Layer.error("ִ�нű�����");
				return false;
			}
		} else {
			return true;
		}
	}

	private boolean delegate(Document doc) {
		String userId = br.getCurrentUserId();
		String targetUserId = null;// TODO
		return service.delegateTask(doc.getLong("_id"), userId, targetUserId);
	}

	private boolean suspend(Document doc) {
		if (!br.confirm("��ͣ����", "��ȷ����ͣ����"))
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
		if (!br.confirm("��ʼ����", "��ȷ�Ͽ�ʼ����"))
			return false;
		String userId = br.getCurrentUserId();
		service.startTask(doc.getLong("_id"), userId);
		context.getParentContext().refresh(true);
		return true;
	}

	private boolean claim(Document doc) {
		if (!br.confirm("��������", "��ȷ����������"))
			return false;
		String userId = br.getCurrentUserId();
		return service.claimTask(doc.getLong("_id"), userId);
	}

	private boolean exit(Document doc) {
		if (!br.confirm("�˳�����", "�˳�����󣬸ý��ر�������ִ�У����������ɻ��ˣ���ȷ�ϡ�"))
			return false;
		String userId = br.getCurrentUserId();
		return service.exitTask(doc.getLong("_id"), userId);
	}

	private boolean skip(Document doc) {
		if (!br.confirm("��������", "��������󣬸ý�����������ִ�У����������ɻ��ˣ���ȷ�ϡ�"))
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
