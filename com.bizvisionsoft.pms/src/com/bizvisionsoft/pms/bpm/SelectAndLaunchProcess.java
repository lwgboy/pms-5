package com.bizvisionsoft.pms.bpm;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.eclipse.jface.window.Window;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.JSTools;
import com.bizvisionsoft.service.tools.ServiceHelper;
import com.bizvisionsoft.serviceconsumer.Services;

public class SelectAndLaunchProcess {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.CURRENT_USER_ID) String userId) {
		Selector.create("�û�����������ѡ����", context, null).setTitle("��ѡ��Ҫ���������").open(list -> {
			if (list != null && list.size() > 0) {
				ProcessDefinition pd = (ProcessDefinition) list.get(0);
				launch(pd, context, userId);
			}
		});
	}

	private void launch(ProcessDefinition pd, IBruiContext context, String userId) {
		String processFullName = pd.getName();

		Document input = new Document();
		// 1.������������
		Document properties = pd.getProperties();
		Optional.ofNullable(properties).ifPresent(input::putAll);
		// 2.�Ƿ��б������У���
		String editorName = pd.getEditor();
		if (Check.isAssigned(editorName)) {
			Assembly assembly = ModelLoader.site.getAssemblyByName(editorName);
			if (assembly == null) {
				Layer.error("�޷�������̱���" + editorName);
				return;
			}
			Set<String> editorFields = assembly.getFields().stream().map(fi -> fi.getName()).collect(Collectors.toSet());
			Editor<Document> editor = new Editor<Document>(assembly, context).setInput(true, input).setTitle("��������-" + processFullName)
					.setEditable(true);
			if (Window.OK != editor.open()) {
				return;
			} else {
				// ȥ��������ֶ�
				Set<String> toRemove = input.keySet().stream()
						.filter(s -> ((properties == null) || !properties.containsKey(s)) && !editorFields.contains(s))
						.collect(Collectors.toSet());
				toRemove.forEach(s -> input.remove(s));
			}
		}
		// 3.���нű������нű�
		String script = pd.getScript();
		String function = pd.getFunction();
		if (Check.isAssigned(script)) {
			Document binding = new Document("input", input).append("context", context.getContextParameterData()).append("ServiceHelper",
					new ServiceHelper());
			JSTools.invoke(script, function, "input", binding, input, context.getContextParameterData());
		}
		Document parameter = new Document().append("input", input).append("meta", pd.getMetaInfo()).append("creationInfo",
				br.operationInfo().encodeDocument());
		Long id = Services.get(BPMService.class).startProcess(parameter, pd.getBpmnId());
		if (id != null)
			Layer.message("����������<br>" + processFullName, Layer.ICON_INFO);
		else
			Layer.message("��������ʧ��<br>" + processFullName, Layer.ICON_ERROR);
	}

}
