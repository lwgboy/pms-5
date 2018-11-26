package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.Catalog;

@Path("/catalog")
public interface CatalogService {

	@POST
	@Path("/res/selector/org/root/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.LIST })
	public List<Catalog> listResOrgRoot(@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId);

	@POST
	@Path("/res/selector/org/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.STRUCTURE_LIST, "EPS��Դͼ��/" + DataSet.STRUCTURE_LIST, "��Ŀ��Դͼ��/" + DataSet.STRUCTURE_LIST })
	public List<Catalog> listResOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/selector/org/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.STRUCTURE_COUNT, "EPS��Դͼ��/" + DataSet.STRUCTURE_COUNT, "��Ŀ��Դͼ��/" + DataSet.STRUCTURE_LIST })
	public long countResOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/selector/eps/root/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "EPS��Դͼ��/" + DataSet.LIST })
	public List<Catalog> listResEPSRoot();

	@POST
	@Path("/res/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.CHART, "EPS��Դͼ��/" + DataSet.CHART, "��Ŀ��Դͼ��/" + DataSet.CHART })
	public Document createResChart(@MethodParam(MethodParam.CONDITION) Document condition);

	@POST
	@Path("/res/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.DEFAULT, "EPS��Դͼ��/" + DataSet.DEFAULT, "��Ŀ��Դͼ��/" + DataSet.DEFAULT })
	public Document createDefaultOption();

	@POST
	@Path("/res/selector/project/root/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��Ŀ��Դͼ��/" + DataSet.LIST })
	public List<Catalog> listResProjectRoot(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId _id);

}
