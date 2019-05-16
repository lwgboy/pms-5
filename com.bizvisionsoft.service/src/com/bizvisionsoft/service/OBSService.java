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
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.ScopeRoleParameter;
import com.bizvisionsoft.service.model.User;
import com.mongodb.BasicDBObject;

@Path("/obs")
public interface OBSService {

	@POST
	@Path("/{domain}/nextobsseq")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int nextOBSSeq(BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OBSItem insert(OBSItem item,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/scope/root/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目团队/list")
	public List<OBSItem> getScopeRootOBS(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/scope/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "组织结构图/list", "组织结构图（查看）/list" })
	public List<OBSItem> getScopeOBS(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId scope_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OBSItem get(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目团队/" + DataSet.DELETE, "组织结构图/" + DataSet.DELETE })
	public void delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/{_id}/sub")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<OBSItem> getSubOBSItem(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/{_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSubOBSItem(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目团队/" + DataSet.UPDATE, "组织结构图/" + DataSet.UPDATE })
	public long update(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/member/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "团队成员/" + DataSet.LIST, "团队成员（查看）/" + DataSet.LIST })
	public List<User> getMember(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/member/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "团队成员/" + DataSet.COUNT, "团队成员（查看）/" + DataSet.COUNT })
	public long countMember(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/member/AllSubOBSItem/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<String> getAllSubOBSItemMember(@PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/scope/id/{_id}/userId/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<String> getScopeRoleofUser(@PathParam("_id") ObjectId scope_id, @PathParam("userId") String userId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/scope/id/{_id}/{roleId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<OBSItem> getScopeRoleofRoleId(@PathParam("_id") ObjectId scope_id, @PathParam("roleId") String roleId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsitemwarpper/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<OBSItemWarpper> getOBSItemWarpper(BasicDBObject condition, @PathParam("_id") ObjectId scope_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsitemwarpper/{_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countOBSItemWarpper(BasicDBObject filter, @PathParam("_id") ObjectId scope_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/**
	 * 检查是否具有某个Scope下的角色
	 * 
	 * @param param
	 * @return
	 */
	@POST
	@Path("/{domain}/scopeRole/check/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean checkScopeRole(ScopeRoleParameter param,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/addOBSModule/{module_id}/{parent_id}/{cover}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<OBSItem> addOBSModule(@PathParam("module_id") ObjectId module_id, @PathParam("parent_id") ObjectId parent_id,
			@PathParam("cover") boolean cover,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/isRoleNumberDuplicated /{module_id}/{scope_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean isRoleNumberDuplicated(@PathParam("module_id") ObjectId module_id, @PathParam("scope_id") ObjectId scope_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/deleteProjectMemberCheck/{type}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> deleteProjectMemberCheck(@PathParam("_id") ObjectId _id, @PathParam("type") String type,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/removeUnStartWorkUser/{currentId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void removeUnStartWorkUser(OBSItem obsItem, @PathParam("currentId") String currentId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
}