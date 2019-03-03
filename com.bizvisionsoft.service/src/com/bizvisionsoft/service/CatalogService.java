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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/catalog")
@Api(value = "/catalog")
public interface CatalogService {

	@POST
	@Path("/root/selector/org/{userId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/list" })
	@ApiOperation(value = "��ȡ����֯Ŀ¼", response = Catalog.class, responseContainer = "List")
	public List<Catalog> listOrgRoot(@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId);

	@POST
	@Path("/root/selector/eps/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "EPS��Դͼ��/list", "EPSԤ��ɱ�ͼ��/list" })
	@ApiOperation(value = "��ȡ��EPSĿ¼", response = Catalog.class, responseContainer = "List")
	public List<Catalog> listEPSRoot();

	@POST
	@Path("/res/selector/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/slist", "EPS��Դͼ��/slist", "��Ŀ��Դͼ��/slist" })
	@ApiOperation(value = "��ȡ��ԴĿ¼���¼�Ŀ¼", response = Catalog.class, responseContainer = "List")
	public List<Catalog> listResStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/selector/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/scount", "EPS��Դͼ��/scount", "��Ŀ��Դͼ��/scount" })
	@ApiOperation(value = "��ȡ��ԴĿ¼���¼�Ŀ¼������", response = Long.class)
	public long countResStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/res/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/chart", "EPS��Դͼ��/chart", "��Ŀ��Դͼ��/chart" })
	@ApiOperation(value = "��������������Դͼ������", response = Document.class)
	public Document createResChart(@MethodParam(MethodParam.CONDITION) Document condition);

	@POST
	@Path("/budgetNCost/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "EPSԤ��ɱ�ͼ��/chart", "��ĿԤ��ɱ�ͼ��/chart" })
	@ApiOperation(value = "������������Ԥ��ɱ�ͼ������", response = Document.class)
	public Document createBudgetNCostChart(@MethodParam(MethodParam.CONDITION) Document condition);

	@POST
	@Path("/budgetNCost/selector/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "EPSԤ��ɱ�ͼ��/slist", "��ĿԤ��ɱ�ͼ��/slist" })
	@ApiOperation(value = "��ȡԤ��ɱ�Ŀ¼���¼�Ŀ¼", response = Catalog.class, responseContainer = "List")
	public List<Catalog> listBudgetNCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/budgetNCost/selector/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "EPSԤ��ɱ�ͼ��/scount", "��ĿԤ��ɱ�ͼ��/scount" })
	@ApiOperation(value = "��ȡԤ��ɱ�Ŀ¼���¼�Ŀ¼����", response = Long.class)
	public long countBudgetNCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/budgetcost/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "EPSԤ��ɱ�ͼ��/default", "��ĿԤ��ɱ�ͼ��/default" })
	@ApiOperation(value = "����Ĭ�ϵ�Ԥ��ɱ�ͼ����������", response = Document.class)
	public Document createDefaultBudgetNCostOption();

	@POST
	@Path("/res/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��֯��Դͼ��/default", "EPS��Դͼ��/default", "��Ŀ��Դͼ��/default" })
	@ApiOperation(value = "����Ĭ�ϵ���Դͼ����������", response = Document.class)
	public Document createDefaultResOption();

	@POST
	@Path("/res/selector/project/root/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��Ŀ��Դͼ��/list", "��ĿԤ��ɱ�ͼ��/list" })
	@ApiOperation(value = "��ȡ��Ŀ��Ϊ��Ŀ¼", response = Catalog.class, responseContainer = "List")
	public List<Catalog> listResProjectRoot(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId _id);

}
