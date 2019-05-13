package com.bizvisionsoft.pms.bpm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.window.Window;

import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.factory.assembly.EditorFactory;
import com.bizvisionsoft.bruicommons.factory.fields.SpinnerFieldFactory;
import com.bizvisionsoft.bruicommons.factory.fields.TextFieldFactory;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.PermissionService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.JSTools;
import com.bizvisionsoft.service.tools.ServiceHelper;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class BPMClient {

	private String domain;
	private BPMService service;

	// private static Logger logger = LoggerFactory.getLogger(BPMClient.class);

	public BPMClient(String domain) {
		this.domain = domain;
		service = Services.get(BPMService.class);
	}

	public Long startProcess(IBruiContext context, ProcessDefinition pd, String userId) {
		Document input = new Document();
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 0.设置默认参数
		input.put("launcher", userId);// 流程发起人

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.构造启动参数
		Optional.ofNullable(pd.getProperties()).ifPresent(input::putAll);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2.是否有表单，如有，打开
		String editor = pd.getEditor();
		if (Check.isAssigned(editor)) {
			Assembly assembly = ModelLoader.site.getAssemblyByName(editor);
			if (assembly == null)
				throw new RuntimeException("无法获得表单：" + editor);
			assembly.getFields().forEach(f -> {
				String fieldName = f.getName();// 如果定义了流程表单，流程表单中包含了该字段，给出默认值
				if ("proc_subject".equals(fieldName))
					input.put("proc_subject", pd.getName());// 流程名称
				if ("proc_desc".equals(fieldName))
					input.put("proc_desc", pd.getDescription());// 流程描述
			});

			if (!editInput(context, assembly, pd.getName(), input))
				return null;
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3.如有脚本，运行脚本
		executeJS(input, context.getContextParameterData(), pd.getScript());

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4.构造启动参数
		Document meta = new Document("userId", userId);
		// 添加流程附加属性
		meta.putAll(pd.getMetaInfo());
		// 添加编辑器录入的流程属性
		Arrays.asList("proc_subject", "proc_desc").forEach(keyword -> {
			String value = input.getString(keyword);
			if (value != null) {
				input.remove(keyword);
				meta.append(keyword, value);
			}
		});

		Document parameter = new Document().append("input", input).append("meta", meta);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. 启动流程
		return service.startProcess(parameter, pd.getBpmnId(), domain);

	}

	public Result startTask(long taskId, String userId) {
		return service.startTask(taskId, userId, domain);
	}

	public Result completeTask(IBruiContext context, long taskId, String userId) {
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1. 接受流程参数
		Document input = new Document();
		Optional.ofNullable(service.getProcessInstanceVariablesByTaskId(taskId, domain)).ifPresent(input::putAll);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2. 处理任务属性
		TaskDefinition td = service.getTaskDefinitionByTaskId(taskId, domain);
		if (td != null)
			Optional.ofNullable(td.getProperties()).ifPresent(input::putAll);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3. 前处理
		if (td != null)
			executeJS(input, context.getContextParameterData(), td.getiScript());

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. 打开编辑器表单
		String editor = td == null ? null : td.getEditor();
		if (Check.isAssigned(editor)) {// 如果定义了编辑器
			Assembly assembly = ModelLoader.site.getAssemblyByName(editor);
			if (assembly == null)
				return Result.error("无法获得表单：" + editor);
			if (!editInput(context, assembly, td.getName(), input))
				return Result.terminated();
		} else {// 没有定义编辑器
			Document node = service.getTaskNodeInfo(taskId, domain);
			if (node != null) {
				Document meta = (Document) node.get("meta");
				Document taskOutput = (Document) meta.get("DataOutputs");
				if (!taskOutput.isEmpty()) {// 没有输出的时候，无需表单
					String title = node.getString("name");
					String name = node.getString("taskName");
					EditorFactory e = new EditorFactory().name(name).title(title).size(EditorFactory.HARROW | EditorFactory.SHORT);
					taskOutput.entrySet().stream().map(BPMClient::createField).forEach(e::appendField);
					if (!editInput(context, e.get(), title, input))
						return Result.terminated();
				}
			}
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. 后处理
		if (td != null)
			executeJS(input, context.getContextParameterData(), td.getoScript());

		return service.completeTask(taskId, userId, input, domain);
	}

	public Result delegateTask(IBruiContext context, long taskId, String userId) {
		Assembly config = ModelLoader.site.getAssemblyByName("任务委托人选择器");
		Selector selector = new Selector(config, context).setTitle("请选择委托本任务的执行人");
		if (Window.OK != selector.open()) {
			return Result.terminated();
		} else {
			List<?> ret = (List<?>) selector.getResult();
			if (Check.isAssigned(ret)) {
				User user = (User) ret.get(0);
				String targetUserId = user.getUserId();
				if (userId.equals(targetUserId)) {
					return Result.terminated();
				}
				return service.delegateTask(taskId, userId, targetUserId, domain);
			} else {
				return Result.terminated();
			}
		}
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

	private static boolean editInput(IBruiContext context, Assembly assembly, String title, Document input) {
		Set<String> editorFields = new HashSet<>();
		editorFields.addAll(input.keySet());
		editorFields.addAll(assembly.getFields().stream().map(fi -> fi.getName()).collect(Collectors.toSet()));
		Editor<Document> editor = new Editor<Document>(assembly, context).setInput(true, input).setTitle(title).setEditable(true);
		if (Window.OK != editor.open()) {
			return false;
		} else {
			// 去除多余的字段
			Set<String> toRemove = input.keySet().stream().filter(s -> !editorFields.contains(s)).collect(Collectors.toSet());
			toRemove.forEach(s -> input.remove(s));
			return true;
		}
	}

}
