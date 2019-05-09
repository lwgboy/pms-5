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
import org.kie.api.task.model.Task;
import org.kie.internal.process.CorrelationAwareProcessRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.common.query.JQ;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.service.tools.StreamToolkit;
import com.bizvisionsoft.serviceimpl.renderer.ProcessTaskCardRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class BPMServiceImpl extends BasicServiceImpl implements BPMService {

	private static Logger logger = LoggerFactory.getLogger(BPMServiceImpl.class);

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

	private List<Document> listTasksByPotentialOwnerUserIdAndStatus(BasicDBObject condition, String userId, String lang, List<String> status) {
		List<Bson> pipe = new ArrayList<>();
		pipe = pipelineTasksAssignedAsPotentialOwner(pipe, (BasicDBObject) condition.get("filter"), userId, status);
		BasicDBObject sort = Optional.ofNullable((BasicDBObject) condition.get("sort")).orElse(new BasicDBObject("_id", -1));
		pipe.add(Aggregates.sort(sort));
		Optional.ofNullable((Integer) condition.get("skip")).ifPresent(Aggregates::skip);
		Optional.ofNullable((Integer) condition.get("limit")).ifPresent(Aggregates::limit);
		return c("bpm_Task").aggregate(pipe).map(d -> ProcessTaskCardRenderer.renderTasksAssignedAsPotentialOwner(d, lang))
				.into(new ArrayList<>());
	}

	private long countTasksByUserIdAndStatus(BasicDBObject filter, String userId, List<String> status) {
		List<Bson> pipe = new ArrayList<>();
		pipelineTasksAssignedAsPotentialOwner(pipe, filter, userId, status).add(Aggregates.count());
		return Optional.ofNullable(c("bpm_Task").aggregate(pipe).first()).map(c -> c.getLong("count")).orElse(0l);
	}

	@Override
	public List<Document> listTasksAssignedAsPotentialOwnerCard(BasicDBObject condition, String userId, String lang, String domain) {
		return listTasksByPotentialOwnerUserIdAndStatus(condition, userId, lang, Arrays.asList( "Ready", "Reserved"));
	}

	@Override
	public long countTasksAssignedAsPotentialOwnerCard(BasicDBObject filter, String userId, String domain) {
		return countTasksByUserIdAndStatus(filter, userId, Arrays.asList( "Ready", "Reserved"));
	}

	@Override
	public List<Document> listTasksInProgressCard(BasicDBObject condition, String userId, String lang, String domain) {
		return listTasksByPotentialOwnerUserIdAndStatus(condition, userId, lang, Arrays.asList("InProgress"));
	}

	@Override
	public long countTasksInProgressCard(BasicDBObject filter, String userId, String domain) {
		return countTasksByUserIdAndStatus(filter, userId, Arrays.asList("InProgress"));
	}

	@Override
	public List<Document> listTasksSuspendedCard(BasicDBObject condition, String userId, String lang, String domain) {
		return listTasksByPotentialOwnerUserIdAndStatus(condition, userId, lang, Arrays.asList("Suspended"));
	}

	@Override
	public long countTasksSuspendedCard(BasicDBObject filter, String userId, String domain) {
		return countTasksByUserIdAndStatus(filter, userId, Arrays.asList("Suspended"));
	}

	@Override
	public List<Document> listTasksClosedCard(BasicDBObject condition, String userId, String lang, String domain) {
		return listTasksByPotentialOwnerUserIdAndStatus(condition, userId, lang, Arrays.asList("Completed", "Failed", "Error", "Exited", "Obsolete"));
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

	@Override
	public boolean resumeTask(long taskId, String userId, String domain) {
		taskService().resume(taskId, userId);
		return true;
	}

	@Override
	public boolean stopTask(long taskId, String userId, String domain) {
		taskService().stop(taskId, userId);
		return true;
	}

	@Override
	public boolean suspendTask(long taskId, String userId, String domain) {
		taskService().suspend(taskId, userId);
		return true;
	}

	@Override
	public boolean forwardTask(long taskId, String userId, String targetUserId, String domain) {
		taskService().forward(taskId, userId, targetUserId);
		return true;
	}

	@Override
	public boolean startTask(long taskId, String userId, String domain) {
		taskService().start(taskId, userId);
		return true;
	}

	@Override
	public boolean claimTask(long taskId, String userId, String domain) {
		taskService().claim(taskId, userId);
		return true;
	}

	@Override
	public boolean exitTask(long taskId, String userId, String domain) {
		taskService().exit(taskId, userId);
		return true;
	}

	@Override
	public boolean skipTask(long taskId, String userId, String domain) {
		taskService().skip(taskId, userId);
		return true;
	}

	private TaskService taskService() {
		return BPM.getDefaultRuntimeEngine().getTaskService();
	}

	private KieSession kie() {
		return BPM.getDefaultRuntimeEngine().getKieSession();
	}

	@Override
	public boolean delegateTask(long taskId, String userId, String targetUserId, String domain) {
		taskService().delegate(taskId, userId, targetUserId);
		return true;
	}

	@Override
	public boolean completeTask(long taskId, String userId, Document parameters, String domain) {
		taskService().complete(taskId, userId, parameters);
		return true;
	}

	@Override
	public boolean nominateTask(long taskId, String userId, List<String> potentialOwnersUserId, String domain) {
		List<OrganizationalEntity> potentialOwners = null;// TODO
		taskService().nominate(taskId, userId, potentialOwners);
		return true;
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
