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
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.User;
import com.mongodb.BasicDBObject;

@Path("/org")
public interface OrganizationService {

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.INSERT)
	public Organization insert(@ServiceParam(ServiceParam.OBJECT) Organization orgInfo);

	@POST
	@Path("/role/")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织角色/" + DataSet.INSERT)
	public Role insertRole(@ServiceParam(ServiceParam.OBJECT) Role role);

	@GET
	@Path("/{_id}")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	public Organization get(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/role/{_id}")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	public Role getRole(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.UPDATE)
	public long update(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/role/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织角色/" + DataSet.UPDATE)
	public long updateRole(BasicDBObject filterAndUpdate);

	@GET
	@Path("/root")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.LIST)
	public List<Organization> getRoot();

	@GET
	@Path("/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.COUNT)
	public long countRoot();

	@GET
	@Path("/sub/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Organization> getSub(@PathParam("_id") ObjectId parent_id);

	@GET
	@Path("/sub/count/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSub(@PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/member/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<User> getMember(BasicDBObject condition, @PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/member/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countMember(BasicDBObject filter, @PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/roles/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Role> getRoles(BasicDBObject condition, @PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/roles/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countRoles(BasicDBObject filter, @PathParam("_id") ObjectId parent_id);

	@POST
	@Path("/roles/{_id}/users")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<User> queryUsersOfRole(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/roles/{_id}/users/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countUsersOfRole(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织选择器/" + DataSet.LIST)
	public List<Organization> createDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织选择器/" + DataSet.COUNT)
	public long count(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter);

	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织管理/" + DataSet.DELETE)
	public long delete(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@DELETE
	@Path("/role/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织角色/" + DataSet.DELETE)
	public long deleteRole(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

}