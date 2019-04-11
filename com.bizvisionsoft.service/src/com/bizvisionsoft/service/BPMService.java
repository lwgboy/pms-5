package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;

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

}
