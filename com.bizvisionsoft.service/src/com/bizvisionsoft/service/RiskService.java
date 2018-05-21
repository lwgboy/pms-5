package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.RBSType;
import com.mongodb.BasicDBObject;

@Path("/risk")
public interface RiskService {
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/type/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.LIST)
	public List<RBSType> getRBSItem();

	@POST
	@Path("/type/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.INSERT)
	public RBSType insertRBSItem(@ServiceParam(ServiceParam.OBJECT) RBSType item);

	@DELETE
	@Path("/type/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.DELETE)
	public long deleteRBSItem(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/type/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.UPDATE)
	public long updateRBSItem(BasicDBObject filterAndUpdate);

}
