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
	 * ��ȡ�û�������֯
	 * 
	 * @param userId
	 * @return
	 */
	@POST
	@Path("/res/selector/root/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.LIST })
	public List<Catalog> listResRoot(@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId);

	@POST
	@Path("/res/selector/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.STRUCTURE_LIST })
	public List<Catalog> listResStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/selector/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.STRUCTURE_COUNT })
	public long countResStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.CHART })
	public Document createResChart(@MethodParam(MethodParam.CONDITION) Document condition);

	@POST
	@Path("/res/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/" + DataSet.DEFAULT })
	public Document createDefaultOption();

}
