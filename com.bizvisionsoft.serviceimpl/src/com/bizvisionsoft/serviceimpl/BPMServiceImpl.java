package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.kie.api.definition.process.Node;
import org.kie.api.internal.utils.BPM;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.process.CorrelationAwareProcessRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.common.query.JQ;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.StreamToolkit;
import com.bizvisionsoft.serviceimpl.renderer.ProcessTaskCardRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class BPMServiceImpl extends BasicServiceImpl implements BPMService {

	private static Logger logger = LoggerFactory.getLogger(BPMServiceImpl.class);

	private TaskService taskService() {
		return BPM.getDefaultRuntimeEngine().getTaskService();
	}

	private KieSession kie() {
		return BPM.getDefaultRuntimeEngine().getKieSession();
	}

	@Override
	public List<Document> listResources(BasicDBObject condition, String domain) {
		Stream<Document> stream = createResourceStream();
		return StreamToolkit.appendCondition(stream, condition).collect(Collectors.toList());
	}

	private Stream<Document> createResourceStream() {
		Stream<Document> stream = BPM.getKieBase().getProcesses().stream().map(p -> {
			// Document meta = new Document();
			// meta.putAll( );
			// Map<String, Object> meta = p.getMetaData();
			Document resource = new Document();
			Resource res = p.getResource();
			resource.append("sourcePath", res.getSourcePath())//
					.append("targetPath", res.getTargetPath())//
					.append("resourceType", res.getResourceType().getName());
			return new Document("id", p.getId())//
					.append("name", p.getName())//
					.append("namespace", p.getNamespace())//
					.append("version", p.getVersion())//
					.append("knowledgeType", p.getKnowledgeType().name())//
					.append("type", p.getType())//
					.append("packageName", p.getPackageName())//
					// .append("meta", meta)//
					.append("resource", resource);//
		});
		return stream;
	}

	@Override
	public long countResources(BasicDBObject filter, String domain) {
		Stream<Document> stream = createResourceStream();
		return StreamToolkit.appendFilter(stream, filter).count();
	}

	@Override
	public List<ProcessDefinition> listProcessDefinitionByFunctionRoles(BasicDBObject condition, String userId, String domain) {
		List<Bson> pipeline = new ArrayList<>();
		new JQ("查询授权用户的流程定义").set("userId", userId).appendTo(pipeline);
		appendConditionToPipeline(pipeline, condition);
		List<?> ids = c("processDefinition").aggregate(pipeline).map((Document d) -> d.getObjectId("_id")).into(new ArrayList<>());
		ArrayList<ProcessDefinition> result = c(ProcessDefinition.class).find(new Document("_id", new Document("$in", ids)))
				.into(new ArrayList<>());
		return result;
	}

	@Override
	public long countProcessDefinitionByFunctionRoles(BasicDBObject filter, String userId, String domain) {
		List<Bson> pipeline = new ArrayList<>();
		new JQ("查询授权用户的流程定义").appendTo(pipeline);
		Optional.ofNullable(filter).map(Aggregates::match).ifPresent(pipeline::add);
		return c("processDefinition").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long countTaskDefinitions(ObjectId _id, String domain) {
		ProcessDefinition pd = getProcessDefinition(_id, domain);
		RuleFlowProcess process = (RuleFlowProcess) BPM.getKieBase().getProcess(pd.getBpmnId());
		return Arrays.asList(process.getNodes()).stream().filter(n -> HumanTaskNode.class.isInstance(n)).count();
	}

	public Document getTaskNodeInfo(long taskId, String domain) {
		Task task = taskService().getTaskById(taskId);
		String taskName = task.getFormName();
		RuleFlowProcess process = (RuleFlowProcess) BPM.getKieBase().getProcess(task.getTaskData().getProcessId());
		return Arrays.asList(process.getNodes()).stream()
				.filter(n -> HumanTaskNode.class.isInstance(n) && taskName.equals(((HumanTaskNode) n).getWork().getParameter("TaskName")))
				.findFirst().map(this::getNodeInfo).orElse(null);
	}

	private Document getNodeInfo(Node n) {
		HumanTaskNode node = (HumanTaskNode) n;
		Document nodeData = new Document();
		nodeData.append("name", n.getName()).append("taskName", node.getWork().getParameter("TaskName"));
		Map<String, Object> meta = node.getMetaData();
		nodeData.append("meta", meta);
		return nodeData;
	}

	@Override
	public List<TaskDefinition> listTaskDefinitions(ObjectId _id, String domain) {
		String processId = getString("processDefinition", "bpmnId", _id);
		RuleFlowProcess process = (RuleFlowProcess) BPM.getKieBase().getProcess(processId);
		List<String> ids = new ArrayList<>();
		List<HumanTaskNode> nodes = new ArrayList<>();
		Arrays.asList(process.getNodes()).stream().filter(n -> HumanTaskNode.class.isInstance(n)).forEach(node -> {
			nodes.add((HumanTaskNode) node);
			ids.add(((HumanTaskNode) node).getUniqueId());
		});
		ArrayList<Document> docs = c("taskDefinition")
				.find(new Document("processDefinitionId", _id).append("nodeId", new Document("$in", ids))).into(new ArrayList<>());
		return nodes.stream().map(node -> {
			String nodeId = node.getUniqueId();
			String name = node.getName();
			String taskName = (String) node.getWork().getParameter("TaskName");
			Document data = docs.stream().filter(doc -> nodeId.equals(doc.get("nodeId"))).findFirst().orElse(
					new Document("nodeId", nodeId).append("taskName", taskName).append("name", name).append("processDefinitionId", _id));
			TaskDefinition td = new TaskDefinition();
			td.setData(data);
			return td;
		}).collect(Collectors.toList());
	}

	@Override
	public Long startProcess(Document parameters, String processId, String domain) {
		// 获取流程传入参数
		Document input = (Document) parameters.get("input");
		// 获取流程附加数据
		CorrelationKeyInfo ck = new CorrelationKeyInfo("meta", (Document) parameters.get("meta"));

		// // 启动流程
		CorrelationAwareProcessRuntime kies = (CorrelationAwareProcessRuntime) kie();
		ProcessInstance pi = kies.startProcess(processId, ck, input);

		long id = pi.getId();
		logger.info("启动流程：{}", pi);

		return id;
	}

	private List<Document> listTasksByPotentialOwnerUserIdAndStatus(BasicDBObject condition, String userId, String lang,
			List<String> status) {
		List<Bson> pipe = new ArrayList<>();
		pipe = pipelineTasksAssignedAsPotentialOwner(pipe, (BasicDBObject) condition.get("filter"), userId, status);
		BasicDBObject sort = Optional.ofNullable((BasicDBObject) condition.get("sort")).orElse(new BasicDBObject("_id", -1));
		pipe.add(Aggregates.sort(sort));
		Optional.ofNullable((Integer) condition.get("skip")).ifPresent(Aggregates::skip);
		Optional.ofNullable((Integer) condition.get("limit")).ifPresent(Aggregates::limit);
		return c("bpm_Task").aggregate(pipe).map(d -> ProcessTaskCardRenderer.renderTasksAssignedAsPotentialOwner(d, userId,lang))
				.into(new ArrayList<>());
	}

	private long countTasksByUserIdAndStatus(BasicDBObject filter, String userId, List<String> status) {
		List<Bson> pipe = new ArrayList<>();
		pipelineTasksAssignedAsPotentialOwner(pipe, filter, userId, status).add(Aggregates.count());
		return Optional.ofNullable(c("bpm_Task").aggregate(pipe).first()).map(c -> c.getLong("count")).orElse(0l);
	}

	@Override
	public List<Document> listTasksAssignedAsPotentialOwnerCard(BasicDBObject condition, String userId, String lang, String domain) {
		return listTasksByPotentialOwnerUserIdAndStatus(condition, userId, lang, Arrays.asList("Ready", "Reserved", "InProgress"));
	}

	@Override
	public long countTasksAssignedAsPotentialOwnerCard(BasicDBObject filter, String userId, String domain) {
		return countTasksByUserIdAndStatus(filter, userId, Arrays.asList("Ready", "Reserved", "InProgress"));
	}

	@Override
	public List<Document> listTasksClosedCard(BasicDBObject condition, String userId, String lang, String domain) {
		return listTasksByPotentialOwnerUserIdAndStatus(condition, userId, lang,
				Arrays.asList("Completed", "Failed", "Error", "Exited", "Obsolete"));
	}

	@Override
	public long countTasksClosedCard(BasicDBObject filter, String userId, String domain) {
		return countTasksByUserIdAndStatus(filter, userId, Arrays.asList("Completed", "Failed", "Error", "Exited", "Obsolete"));
	}

	private List<Bson> pipelineTasksAssignedAsPotentialOwner(List<Bson> pipe, BasicDBObject filter, String userId, List<String> status) {
		if (pipe == null)
			pipe = new ArrayList<>();
		new JQ("bpm/查询-用户和状态-任务").set("status", status).set("userId", userId).appendTo(pipe);
		if (filter != null && !filter.isEmpty())
			pipe.add(Aggregates.match(filter));
		return pipe;
	}

	private Result checkAction(TaskAction action, long taskId, String userId, String domain) {
		Document task = c("bpm_Task").find(new Document("_id", taskId)).first();
		if (task == null)
			return Result.notFoundError("id:" + taskId + "的任务不存在");
		String status = ((Document) task.get("taskData")).getString("status");
		String statusText = Formatter.getStatusText(status, "cn");
		String actualOwner = ((Document) task.get("taskData")).getString("actualOwner_id");
		// TODO 权限检查，另外考虑
		switch (action) {
		case start:
			if (!Arrays.asList(Status.Created.name(), Status.Ready.name()).contains(status) && actualOwner != null
					&& !userId.equals(actualOwner))
				return Result.notAllowedError("任务已分配至其他用户，已忽略任务启动操作。");
			// 状态检查
			if (!Arrays.asList(Status.Ready.name(), Status.Reserved.name()).contains(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务启动操作。");
			return null;
		case complete:
			// 状态检查
			if (!Status.InProgress.name().equals(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务提交操作。");
			return null;
		case delegate:
			if (!Arrays.asList(Status.Ready.name(), Status.Reserved.name(), Status.InProgress.name()).contains(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务委派操作。");
			return null;
		case stop:
			if (!Status.InProgress.name().equals(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务停办操作。");
			return null;
		case skip:
			if (!Arrays.asList(Status.Created.name(), Status.Ready.name(), Status.Reserved.name(), Status.InProgress.name())
					.contains(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务跳过操作。");
			return null;
		case claim:
			if (!Status.Ready.name().equals(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务签收操作。");
		case exit:
			if (!Arrays.asList(Status.Created.name(), Status.Ready.name(), Status.Reserved.name(), Status.InProgress.name())
					.contains(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务退出操作。");
			return null;
		case suspend:
			if (!Arrays.asList(Status.Ready.name(), Status.Reserved.name(), Status.InProgress.name()).contains(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务暂停操作。");
			return null;
		case resume:
			if (!Status.Suspended.name().equals(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务继续操作。");
		case nominate:
			if (!Status.Created.name().equals(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务继续操作。");
		case forward:
			if (!Arrays.asList(Status.Reserved.name(), Status.InProgress.name()).contains(status))
				return Result.notAllowedError("任务当前状态[" + statusText + "]，不允许执行任务改派操作。");
			return null;
		default:
			return null;
		}

	}

	@Override
	public Result resumeTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.resume, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().resume(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result stopTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.stop, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().stop(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result suspendTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.suspend, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().suspend(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result forwardTask(long taskId, String userId, String targetUserId, String domain) {
		try {
			Result result = checkAction(TaskAction.forward, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().forward(taskId, userId, targetUserId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result startTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.start, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().start(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result claimTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.claim, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().claim(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result exitTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.exit, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().exit(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result skipTask(long taskId, String userId, String domain) {
		try {
			Result result = checkAction(TaskAction.skip, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().skip(taskId, userId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result delegateTask(long taskId, String userId, String targetUserId, String domain) {
		try {
			Result result = checkAction(TaskAction.delegate, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().delegate(taskId, userId, targetUserId);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result completeTask(long taskId, String userId, Document parameters, String domain) {
		try {
			Result result = checkAction(TaskAction.complete, taskId, userId, domain);
			if (result != null)
				return result;
			taskService().complete(taskId, userId, parameters);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public Result nominateTask(long taskId, String userId, List<String> potentialOwnersUserId, String domain) {
		try {
			Result result = checkAction(TaskAction.nominate, taskId, userId, domain);
			if (result != null)
				return result;
			List<OrganizationalEntity> potentialOwners = null;// TODO
			taskService().nominate(taskId, userId, potentialOwners);
			return Result.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	@Override
	public ProcessDefinition getProcessDefinition(ObjectId _id, String domain) {
		return get(_id, ProcessDefinition.class);
	}

	@Override
	public List<ProcessDefinition> listProcessDefinitions(BasicDBObject condition, String domain) {
		return createDataSet(condition, ProcessDefinition.class);
	}

	@Override
	public long countProcessDefinitions(BasicDBObject filter, String domain) {
		return count(filter, ProcessDefinition.class);
	}

	@Override
	public long updateProcessDefinitions(BasicDBObject fu, String domain) {
		return update(fu, ProcessDefinition.class);
	}

	@Override
	public long updateTaskDefinitions(BasicDBObject fu, String domain) {
		return update(fu, TaskDefinition.class);
	}

	@Override
	public ProcessDefinition insertProcessDefinition(ProcessDefinition p, String domain) {
		return insert(p);
	}

	@Override
	public TaskDefinition insertTaskDefinition(TaskDefinition td, String domain) {
		return insert(td);
	}

	@Override
	public long deleteProcessDefinition(ObjectId _id, String domain) {
		c("taskDefinition").deleteMany(new Document("processDefinitionId", _id));
		return delete(_id, ProcessDefinition.class);
	}

	@Override
	public long deleteTaskDefinition(ObjectId _id, String domain) {
		return delete(_id, TaskDefinition.class);
	}

	@Override
	public TaskDefinition getTaskDefinitionByTaskId(long taskId, String domain) {
		List<Bson> pipe = new JQ("bpm/查询-任务-任务定义").set("taskId", taskId).array();
		return c("bpm_Task").aggregate(pipe, TaskDefinition.class).first();
	}

	@Override
	public Document getProcessInstanceVariablesByTaskId(long taskId, String domain) {
		long pid = getValue("bpm_Task", "taskData.processInstanceId", taskId, Long.class);
		return getProcessInstanceVariables(pid, domain);
	}

	@Override
	public Document getProcessInstanceVariables(long pid, String domain) {
		RuleFlowProcessInstance pi = (RuleFlowProcessInstance) kie().getProcessInstance(pid);
		Map<String, Object> v = pi.getVariables();
		Document doc = new Document();
		doc.putAll(v);
		return doc;
	}

}
