package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/bpm/")
@Api("/bpm/")
public interface BPMService {

	@POST
	@Path("/{domain}/resource/bpmn/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义", response = Document.class, responseContainer = "List")
	@DataSet("BPMN资源列表/" + DataSet.LIST)
	public List<Document> listResources(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/resource/bpmn/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义数量", response = Long.class)
	@DataSet({ "BPMN资源列表/" + DataSet.COUNT })
	public long countResources(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@GET
	@Path("/{domain}/processDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "获得工作流定义", response = ProcessDefinition.class)
	@DataSet(DataSet.INPUT)
	public ProcessDefinition getProcessDefinition(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/processDef/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义", response = ProcessDefinition.class, responseContainer = "List")
	@DataSet({ "工作流定义列表/list" })
	public List<ProcessDefinition> listProcessDefinitions(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/processDef/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义数量", response = Long.class)
	@DataSet({ "工作流定义列表/count" })
	public long countProcessDefinitions(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/processDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "更新工作流定义", response = Long.class)
	@DataSet({ "工作流定义列表/update" })
	public long updateProcessDefinitions(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/taskDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "更新任务定义", response = Long.class)
	public long updateTaskDefinitions(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/processDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "插入工作流定义", response = Long.class)
	@DataSet("工作流定义列表/insert")
	public ProcessDefinition insertProcessDefinition(@MethodParam(MethodParam.OBJECT) ProcessDefinition p,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/taskDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "插入任务定义", response = Long.class)
	public TaskDefinition insertTaskDefinition(TaskDefinition td, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/processDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "删除工作流定义", response = Long.class)
	@DataSet("工作流定义列表/delete")
	public long deleteProcessDefinition(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/taskDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "删除任务定义", response = Long.class)
	public long deleteTaskDefinition(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/processDef/{_id}/taskDef/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义下的人工任务数量", response = Long.class)
	public long countTaskDefinitions(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/processDef/{_id}/taskDef/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询工作流定义下的人工任务", response = TaskDefinition.class, responseContainer = "List")
	public List<TaskDefinition> listTaskDefinitions(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/taskDef/taskId/{taskId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "根据任务id查询任务定义", response = TaskDefinition.class)
	public TaskDefinition getTaskDefinitionByTaskId(@PathParam("taskId") long taskId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/processDef/authorized/{userId}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询该用户有权启动的工作流定义", response = ProcessDefinition.class, responseContainer = "List")
	@DataSet("用户工作流定义列表/list")
	public List<ProcessDefinition> listProcessDefinitionByFunctionRoles(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/processDef/authorized/{userId}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询该用户有权启动的工作流定义数量", response = Long.class)
	@DataSet("用户工作流定义列表/count")
	public long countProcessDefinitionByFunctionRoles(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/process/start/{processId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "启动工作流", response = Long.class)
	public Long startProcess(Document parameter, @PathParam("processId") String processId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/task/assigned/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务(已创建、准备中和已持有)", response = Document.class, responseContainer = "List")
	@DataSet("我的流程任务（待处理）/list")
	public List<Document> listTasksAssignedAsPotentialOwnerCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/assigned/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务数量(已创建、准备中和已持有)", response = Long.class)
	@DataSet("我的流程任务（待处理）/count")
	public long countTasksAssignedAsPotentialOwnerCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/wip/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务(进行中)", response = Document.class, responseContainer = "List")
	@DataSet("我的流程任务（进行中）/list")
	public List<Document> listTasksInProgressCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/wip/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务数量(进行中)", response = Long.class)
	@DataSet("我的流程任务（进行中）/count")
	public long countTasksInProgressCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/closed/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务(已关闭：完成、失败、错误、退出、废弃)", response = Document.class, responseContainer = "List")
	@DataSet("我的流程任务（已关闭）/list")
	public List<Document> listTasksClosedCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/closed/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务数量(已关闭：完成、失败、错误、退出、废弃)", response = Long.class)
	@DataSet("我的流程任务（已关闭）/count")
	public long countTasksClosedCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/suspended/{userId}/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务(已暂停)", response = Document.class, responseContainer = "List")
	@DataSet("我的流程任务（已暂停）/list")
	public List<Document> listTasksSuspendedCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/task/suspended/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "查询用户的流程任务数量(已暂停)", response = Long.class)
	@DataSet("我的流程任务（已暂停）/count")
	public long countTasksSuspendedCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/resume/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "继续暂停的任务", response = Boolean.class)
	public boolean resumeTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/stop/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "停止任务", response = Boolean.class)
	public boolean stopTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/suspend/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "暂停任务", response = Boolean.class)
	public boolean suspendTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/forward/{userId}/{targetUserId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "退回任务", response = Boolean.class)
	public boolean forwardTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@PathParam("targetUserId") String targetUserId, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/start/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "开始任务", response = Boolean.class)
	public boolean startTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/claim/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "认领任务", response = Boolean.class)
	public boolean claimTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/exit/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "退出任务", response = Boolean.class)
	public boolean exitTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/skip/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "跳过任务", response = Boolean.class)
	public boolean skipTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/delegate/{userId}/{targetUserId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "委托任务", response = Boolean.class)
	public boolean delegateTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId,
			@PathParam("targetUserId") String targetUserId, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/complete/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "完成任务", response = Boolean.class)
	public boolean completeTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId, Document parameters,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/task/{taskId}/nominate/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "指派任务", response = Boolean.class)
	public boolean nominateTask(@PathParam("taskId") long taskId, @PathParam("userId") String userId, List<String> potentialOwnersUserId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/task/{taskId}/processInstance/vars/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "获得流程实例的变量", response = Document.class)
	public Document getProcessInstanceVariablesByTaskId(@PathParam("taskId") long taskId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/processInstance/{processInstanceId}/vars/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "获得流程实例的变量", response = Document.class)
	public Document getProcessInstanceVariables(@PathParam("processInstanceId") long processInstanceId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/task/{taskId}/nodeInfo/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "获得任务的输入和输出数据", response = Document.class)
	public Document getTaskNodeInfo(@PathParam("taskId") long taskId, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

}
