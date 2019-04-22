package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/bpm/")
@Api("/bpm/")
public interface BPMService {

	@POST
	@Path("/resource/bpmn/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "��ѯ����������", response = Document.class, responseContainer = "List")
	@DataSet("BPMN��Դ�б�/" + DataSet.LIST)
	public List<Document> listResources();

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/resource/bpmn/authorized/{userId}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�û������������б�/" + DataSet.LIST })
	public List<ProcessDefinition> listFunctionRoles(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/resource/bpmn/authorized/{userId}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�û������������б�/" + DataSet.COUNT })
	public long countFunctionRoles(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/process/start/{processId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Long startProcess(Document parameter, @PathParam("processId") String processId);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/userId/{userId}/planned/card/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�ҵ����������ѷ��䣩/list")
	public List<Document> listTaskCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userId,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/userId/{userId}/planned/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�ҵ����������ѷ��䣩/count")
	public long countTaskCard(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);
}
