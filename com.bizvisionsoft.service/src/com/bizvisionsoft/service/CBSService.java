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

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.CBSSubject;
import com.mongodb.BasicDBObject;

@Path("/cbs")
public interface CBSService {

	@GET
	@Path("/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSItem get(@PathParam("_id") ObjectId _id);

	
	@GET
	@Path("/scope/root/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("CBS/list")
	public List<CBSItem> getScopeRoot(
			@PathParam("_id") @ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id);

	@GET
	@Path("/{_id}/subcbs/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSItem> getSubCBSItems(@PathParam("_id") ObjectId parent_id);

	@GET
	@Path("/{_id}/subcbs/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSubCBSItems(@PathParam("_id") ObjectId parent_id);

	@GET
	@Path("/{_id}/subject/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSSubject> getSubjectBudget(@PathParam("_id") ObjectId cbs_id);

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSItem insertCBSItem(CBSItem o);
	
	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSItem> createDataSet(BasicDBObject filter);

	
	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.DELETE)
	public void delete(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);
	
	@PUT
	@Path("/period/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ObjectId updateCBSPeriodBudget(CBSPeriod o);


	@PUT
	@Path("/subject/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSSubject upsertCBSSubjectBudget(CBSSubject o);


	@PUT
	@Path("/_id/{_id}/allocate/{work_id}/{workname}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void allocateBudget(@PathParam("_id") ObjectId _id, @PathParam("work_id") ObjectId work_id, @PathParam("workname") String workname);

	

}