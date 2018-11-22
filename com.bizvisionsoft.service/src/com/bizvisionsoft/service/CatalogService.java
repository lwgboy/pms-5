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
import com.bizvisionsoft.service.model.Catalog;

@Path("/catalog")
public interface CatalogService {

	/**
	 * 获取用户所在组织
	 * 
	 * @param userId
	 * @return
	 */
	@POST
	@Path("/res/org/{userId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "资源目录/" + DataSet.LIST })
	public List<Catalog> listRootCatalog(@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId);

	@POST
	@Path("/res/suborg/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "资源目录/" + DataSet.STRUCTURE_LIST })
	public List<Catalog> listSubCatalog(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/suborg/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "资源目录/" + DataSet.STRUCTURE_COUNT })
	public long countSubCatalog(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/chart/res/planAndUsage/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document createResourcePlanAndUserageChart(Document condition);

}
