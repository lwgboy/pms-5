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
		Selector.create("用户工作流定义选择器", context, null).setTitle("请选择将要发起的流程").open(list -> {
			if (list != null && list.size() > 0) {
				ProcessDefinition pd = (ProcessDefinition) list.get(0);
				launch(pd, context, userId);
			}
		});
	}

	private void launch(ProcessDefinition pd, IBruiContext context, String userId) {
		String processFullName = pd.getName();

		Document input = new Document();
		// 1.构造启动参数
		Document properties = pd.getProperties();
		Optional.ofNullable(properties).ifPresent(input::putAll);
		// 2.是否有表单，如有，打开
		String editorName = pd.getEditor();
		if (Check.isAssigned(editorName)) {
			Assembly assembly = ModelLoader.site.getAssemblyByName(editorName);
			if (assembly == null) {
				Layer.error("无法获得流程表单：" + editorName);
				return;
			}
			Set<String> editorFields = assembly.getFields().stream().map(fi -> fi.getName()).collect(Collectors.toSet());
			Editor<Document> editor = new Editor<Document>(assembly, context).setInput(true, input).setTitle("发起流程-" + processFullName)
					.setEditable(true);
			if (Window.OK != editor.open()) {
				return;
			} else {
				// 去除多余的字段
				Set<String> toRemove = input.keySet().stream()
						.filter(s -> ((properties == null) || !properties.containsKey(s)) && !editorFields.contains(s))
						.collect(Collectors.toSet());
				toRemove.forEach(s -> input.remove(s));
			}
		}
		// 3.如有脚本，运行脚本
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
			Layer.message("流程已启动<br>" + processFullName, Layer.ICON_INFO);
		else
			Layer.message("流程启动失败<br>" + processFullName, Layer.ICON_ERROR);
	}

}
