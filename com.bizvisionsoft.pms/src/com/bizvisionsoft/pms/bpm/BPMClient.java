package com.bizvisionsoft.pms.bpm;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.eclipse.jface.window.Window;

import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.factory.assembly.EditorFactory;
import com.bizvisionsoft.bruicommons.factory.fields.SpinnerFieldFactory;
import com.bizvisionsoft.bruicommons.factory.fields.TextFieldFactory;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.JSTools;
import com.bizvisionsoft.service.tools.ServiceHelper;
import com.bizvisionsoft.serviceconsumer.Services;

public class BPMClient {

	private String domain;

	// private static Logger logger = LoggerFactory.getLogger(BPMClient.class);

	public BPMClient(String domain) {
		this.domain = domain;
	}

	public Long startProcess(IBruiContext context, ProcessDefinition pd, String userId) {
		Document input = new Document();
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 0.����Ĭ�ϲ���
		input.put("launcher", userId);// ���̷�����

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.������������
		Optional.ofNullable(pd.getProperties()).ifPresent(input::putAll);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2.�Ƿ��б������У���
		String editor = pd.getEditor();
		if (Check.isAssigned(editor) && !editInput(context, editor, pd.getName(), input))
			return null;

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3.���нű������нű�
		executeJS(input, context.getContextParameterData(), pd.getScript());

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4.������������
		Document meta = new Document("userId", userId);
		meta.putAll(pd.getMetaInfo());
		Document parameter = new Document().append("input", input).append("meta", meta);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. ��������
		return Services.get(BPMService.class).startProcess(parameter, pd.getBpmnId(),domain);

	}

	public void completeTask(IBruiContext context, long taskId, String userId) {
		BPMService service = Services.get(BPMService.class);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1. �������̲���
		Document input = new Document();
		Optional.ofNullable(service.getProcessInstanceVariablesByTaskId(taskId,domain)).ifPresent(input::putAll);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2. ������������
		TaskDefinition td = service.getTaskDefinitionByTaskId(taskId,domain);
		if (td != null)
			Optional.ofNullable(td.getProperties()).ifPresent(input::putAll);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3. ǰ����
		if (td != null)
			executeJS(input, context.getContextParameterData(), td.getiScript());

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. �򿪱༭����
		String editor = td == null ? null : td.getEditor();
		if (Check.isAssigned(editor)) {// ��������˱༭��
			if (!editInput(context, editor, td.getName(), input))
				return;
		} else {// û�ж���༭��
			Document node = service.getTaskNodeInfo(taskId,domain);
			if (node != null) {
				Document meta = (Document) node.get("meta");
				Document taskOutput = (Document) meta.get("DataOutputs");
				if (!taskOutput.isEmpty()) {// û�������ʱ�������
					String title = node.getString("name");
					String name = node.getString("taskName");
					EditorFactory e = new EditorFactory().name(name).title(title).size(EditorFactory.HARROW|EditorFactory.SHORT);
//					Document taskInput = (Document) meta.get("DataInputs");
//					taskInput.entrySet().stream().map(BPMClient::createField).forEach(f -> {
//						f.setReadOnly(true);
//						e.appendField(f);
//					});
					taskOutput.entrySet().stream().map(BPMClient::createField).forEach(e::appendField);
					if (!editInput(context, e.get(), title, input))
						return;
				}
			}
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. ����
		if (td != null)
			executeJS(input, context.getContextParameterData(), td.getoScript());
		System.out.println();
		service.completeTask(taskId, userId, input,domain);
	}

	private static FormField createField(Entry<String, Object> t) {
		String fieldName = t.getKey();
		String fieldTitle = fieldName;
		Object fieldType = t.getValue();
		FormField field = null;
		if (String.class.getName().equals(fieldType)) {
			field = new TextFieldFactory().name(fieldName).text(fieldTitle).get();
		} else if (Integer.class.getName().equals(fieldType)) {
			field = new SpinnerFieldFactory().name(fieldName).text(fieldTitle).get();
		} else if (Long.class.getName().equals(fieldType)) {
			field = new SpinnerFieldFactory().name(fieldName).text(fieldTitle).get();
		} else if (Short.class.getName().equals(fieldType)) {
			field = new SpinnerFieldFactory().name(fieldName).text(fieldTitle).get();
		} else if (Double.class.getName().equals(fieldType)) {
			field = new SpinnerFieldFactory().name(fieldName).text(fieldTitle).get();
		} else if (Float.class.getName().equals(fieldType)) {
			field = new SpinnerFieldFactory().name(fieldName).text(fieldTitle).get();
		} else {
			field = new TextFieldFactory().name(fieldName).text(fieldTitle).get();
		}
		return field;
	}

	private static void executeJS(Document input, Document contextParameterData, String script) {
		if (Check.isAssigned(script)) {
			Document binding = new Document("input", input).append("context", contextParameterData).append("ServiceHelper",
					new ServiceHelper());
			JSTools.invoke(script, null, "input", binding, input, contextParameterData);
		}
	}

	private static boolean editInput(IBruiContext context, String editorName, String title, Document input) {
		Assembly assembly = ModelLoader.site.getAssemblyByName(editorName);
		if (assembly == null) {
			throw new RuntimeException("�޷���ñ���" + editorName);
		} else {
			return editInput(context, assembly, title, input);
		}
	}

	private static boolean editInput(IBruiContext context, Assembly assembly, String title, Document input) {
		Set<String> editorFields = new HashSet<>();
		editorFields.addAll(input.keySet());
		editorFields.addAll(assembly.getFields().stream().map(fi -> fi.getName()).collect(Collectors.toSet()));
		Editor<Document> editor = new Editor<Document>(assembly, context).setInput(true, input).setTitle(title).setEditable(true);
		if (Window.OK != editor.open()) {
			return false;
		} else {
			// ȥ��������ֶ�
			Set<String> toRemove = input.keySet().stream().filter(s -> !editorFields.contains(s)).collect(Collectors.toSet());
			toRemove.forEach(s -> input.remove(s));
			return true;
		}
	}

}
