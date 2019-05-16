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
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.User;
import com.mongodb.BasicDBObject;

@Path("/org")
public interface OrganizationService {

	@POST
	@Path("/{domain}/")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.INSERT)
	public Organization insert(@MethodParam(MethodParam.OBJECT) Organization orgInfo,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/role/")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织角色/" + DataSet.INSERT)
	public Role insertRole(@MethodParam(MethodParam.OBJECT) Role role,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/{_id}")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	public Organization get(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/role/{_id}")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	public Role getRole(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	
	@GET
	@Path("/{domain}/managerId/{userId}/")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	public List<ObjectId> getManagedOrganizationsId(@PathParam("userId") String userId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.UPDATE)
	public long update(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/role/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织角色/" + DataSet.UPDATE)
	public long updateRole(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/root")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.LIST)
	public List<Organization> getRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.COUNT)
	public long countRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/sub/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Organization> getSub(@PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/sub/count/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSub(@PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/member/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<User> getMember(BasicDBObject condition, @PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/member/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countMember(BasicDBObject filter, @PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/roles/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Role> getRoles(BasicDBObject condition, @PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/roles/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countRoles(BasicDBObject filter, @PathParam("_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/roles/{_id}/users")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<User> queryUsersOfRole(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/roles/{_id}/users/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countUsersOfRole(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织选择器/" + DataSet.LIST)
	public List<Organization> createDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织选择器/" + DataSet.COUNT)
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/projectbuilder/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织选择器（项目承担单位）/" + DataSet.LIST)
	public List<Organization> listQualifiedContractor(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/projectbuilder/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织选择器（项目承担单位）/" + DataSet.COUNT)
	public long countQualifiedContractor(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.DELETE)
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/role/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织角色/" + DataSet.DELETE)
	public long deleteRole(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	
}