package com.bizvisionsoft.service;

import java.util.Date;
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
import com.bizvisionsoft.service.model.Result;
import com.mongodb.BasicDBObject;

@Path("/cbs")
public interface CBSService {

	@GET
	@Path("/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSItem get(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/scope/root/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "CBS/list", "CBS£¨²é¿´£©/list" })
	public List<CBSItem> getScopeRoot(
			@PathParam("_id") @ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id);

	@POST
	@Path("/{_id}/subcbs/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSItem> getSubCBSItems(@PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/{_id}/subcbs/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSubCBSItems(@PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/{_id}/subject/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSSubject> getCBSSubject(@PathParam("_id") ObjectId cbs_id);

	@POST
	@Path("/{_id}/subject/{number}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSSubject> getCBSSubjectByNumber(@PathParam("_id") ObjectId cbs_id,
			@PathParam("number") String number);

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
	@Path("/subject/budget/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSSubject upsertCBSSubjectBudget(CBSSubject o);

	@PUT
	@Path("/subject/cost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSSubject upsertCBSSubjectCost(CBSSubject o);

	@PUT
	@Path("/_id/{_id}/allocate/{scope_id}/{scopename}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSItem allocateBudget(@PathParam("_id") ObjectId _id, @PathParam("scope_id") ObjectId scope_id,
			@PathParam("scopename") String scopename);

	@PUT
	@Path("/_id/{_id}/unallocate/{parent_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CBSItem unallocateBudget(@PathParam("_id") ObjectId _id, @PathParam("parent_id") ObjectId parent_id);

	@PUT
	@Path("/_id/{_id}/calculation/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result calculationBudget(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/_id/{_id}/addcbsbystage/{project_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSItem> addCBSItemByStage(@PathParam("_id") ObjectId _id,
			@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/projectcost/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CBSItem> listProjectCost(BasicDBObject condition);

	@POST
	@Path("/projectcost/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countProjectCost(BasicDBObject filter);

	@GET
	@Path("/settlementdate/{scope_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Date getSettlementDate(@PathParam("scope_id") ObjectId scope_id);

	@PUT
	@Path("/submitcost/{scope_id}/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> submitCBSSubjectCost(@PathParam("scope_id")ObjectId scope_id,@PathParam("id") String id);

}