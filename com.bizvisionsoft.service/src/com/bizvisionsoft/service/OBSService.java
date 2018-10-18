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
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.service.model.ScopeRoleParameter;
import com.bizvisionsoft.service.model.User;
import com.mongodb.BasicDBObject;

@Path("/obs")
public interface OBSService {

	@POST
	@Path("/nextobsseq")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int nextOBSSeq(BasicDBObject condition);

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OBSItem insert(OBSItem item);

	@POST
	@Path("/scope/root/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目团队/list")
	public List<OBSItem> getScopeRootOBS(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id);

	@POST
	@Path("/scope/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "组织结构图/list", "组织结构图（查看）/list" })
	public List<OBSItem> getScopeOBS(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id);

	@GET
	@Path("/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OBSItem get(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目团队/" + DataSet.DELETE, "组织结构图/" + DataSet.DELETE })
	public void delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
	@Path("/{_id}/sub")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<OBSItem> getSubOBSItem(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/{_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSubOBSItem(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目团队/" + DataSet.UPDATE, "组织结构图/" + DataSet.UPDATE })
	public long update(BasicDBObject filterAndUpdate);

	@POST
	@Path("/member/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "团队成员/" + DataSet.LIST, "团队成员（查看）/" + DataSet.LIST })
	public List<User> getMember(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id);

	@POST
	@Path("/member/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "团队成员/" + DataSet.COUNT, "团队成员（查看）/" + DataSet.COUNT })
	public long countMember(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id);

	@GET
	@Path("/scope/id/{_id}/userId/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<String> getScopeRoleofUser(@PathParam("_id") ObjectId scope_id, @PathParam("userId") String userId);

	@POST
	@Path("/obsitemwarpper/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<OBSItemWarpper> getOBSItemWarpper(BasicDBObject condition, @PathParam("_id") ObjectId scope_id);

	/**
	 * 检查是否具有某个Scope下的角色
	 * 
	 * @param param
	 * @return
	 */
	@POST
	@Path("/scopeRole/check/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean checkScopeRole(ScopeRoleParameter param);

	@POST
	@Path("/addOBSModule/{module_id}/{parent_id}/{cover}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void addOBSModule(@PathParam("module_id") ObjectId module_id, @PathParam("parent_id") ObjectId parent_id,
			@PathParam("cover") boolean cover);

	@GET
	@Path("/isRoleNumberDuplicated /{module_id}/{scope_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean isRoleNumberDuplicated(@PathParam("module_id") ObjectId module_id,
			@PathParam("scope_id") ObjectId scope_id);
}