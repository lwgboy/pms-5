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
import com.bizvisionsoft.service.model.EPS;
import com.mongodb.BasicDBObject;

@Path("/eps")
public interface EPSService {

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public EPS get(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/eps")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<EPS> getSubEPS(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/root")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("list")
	public List<EPS> getRootEPS();

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("count")
	public long count(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/_id/{_id}/count/eps")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSubEPS(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("EPS管理/" + DataSet.DELETE)
	public long delete(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId get_id);

	@DELETE
	@Path("/projectset/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("EPS浏览/" + DataSet.DELETE)
	public long deleteProjectSet(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId get_id);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long update(BasicDBObject filterAndUpdate);

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("EPS管理/" + DataSet.INSERT)
	public EPS insert(@ServiceParam(ServiceParam.OBJECT) EPS eps);

}
