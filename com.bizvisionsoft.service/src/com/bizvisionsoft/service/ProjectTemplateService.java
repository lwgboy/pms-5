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
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlanInTemplate;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WBSModule;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.service.model.WorkspaceGanttData;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;

@Path("/projectTemplate")
@Api("/projectTemplate")
public interface ProjectTemplateService {

	@POST
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板管理/" + DataSet.INSERT)
	public ProjectTemplate insertProjectTemplate(@MethodParam(MethodParam.OBJECT) ProjectTemplate project,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板管理/" + DataSet.DELETE, "WBS模块/" + DataSet.DELETE })
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板/" + DataSet.INPUT)
	public ProjectTemplate get(@PathParam("_id") @MethodParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板管理/" + DataSet.COUNT)
	public long countProjectTemplate(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板管理/" + DataSet.UPDATE, "WBS模块/" + DataSet.UPDATE })
	public long update(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板管理/" + DataSet.LIST)
	public List<ProjectTemplate> listProjectTemplate(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("wbsModule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("WBS模块/" + DataSet.INSERT)
	public WBSModule insertWBSModule(@MethodParam(MethodParam.OBJECT) WBSModule wbsModule,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/wbsmodule/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("WBS模块/" + DataSet.COUNT)
	public long countWBSModule(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/wbsmodule/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("WBS模块/" + DataSet.LIST)
	public List<WBSModule> listWBSModule(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/work/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkInTemplate getWorkInTemplate(@PathParam("_id") ObjectId work_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/works/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkInTemplate> listWorks(@PathParam("_id") ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/links/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLinkInTemplate> listLinks(@PathParam("_id") ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/work/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkInTemplate insertWork(WorkInTemplate work,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/work/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板WBS/" + DataSet.UPDATE, "项目模板WBS（分配角色）/" + DataSet.UPDATE })
	public long updateWork(BasicDBObject bson,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/work/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWork(@PathParam("_id") ObjectId work_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLinkInTemplate insertLink(WorkLinkInTemplate link,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateLink(BasicDBObject bson,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteLink(@PathParam("_id") ObjectId link_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/nextwbsidx")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int nextWBSIndex(BasicDBObject append,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/nextobsseq")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int nextOBSSeq(BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/id/{_id}/obs/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板组织结构图/list")
	public List<OBSInTemplate> getOBSTemplate(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/obs/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板组织结构图/" + DataSet.DELETE, "OBS模板组织结构图/" + DataSet.DELETE })
	public void deleteOBSItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/id/{_id}/obs/role/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("OBS模板节点选择器/list")
	public List<OBSInTemplate> getOBSRoleTemplate(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/id/{_id}/hasOBS")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean hasOBS(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/id/{_id}/obs/root")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OBSInTemplate createRootOBS(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obs/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OBSInTemplate insertOBSItem(OBSInTemplate t,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/obs/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板组织结构图/" + DataSet.UPDATE, "OBS模板组织结构图/" + DataSet.UPDATE })
	public long updateOBSItem(BasicDBObject fu,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/wbs/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板WBS/" + DataSet.LIST, "项目模板WBS（分配角色）/" + DataSet.LIST })
	public List<WorkInTemplate> listWBSRoot(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/wbs/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目模板WBS/" + DataSet.COUNT, "项目模板WBS（分配角色）/" + DataSet.COUNT })
	public long countWBSRoot(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/parent_id/{parent_id}/wbs/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkInTemplate> listWBSChildren(@PathParam("parent_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/parent_id/{parent_id}/wbs/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWBSChildren(@PathParam("parent_id") ObjectId parent_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/resourceplan/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourcePlanInTemplate> addResourcePlan(List<ResourceAssignment> resas,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/resourceplan/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateResourcePlan(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}/resourceplan/hr/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteHumanResourcePlan(@PathParam("_id") ObjectId work_id, @PathParam("resId") String hrResId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}/resourceplan/eq/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteEquipmentResourcePlan(@PathParam("_id") ObjectId work_id, @PathParam("resId") String eqResId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}/resourceplan/ty/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteTypedResourcePlan(@PathParam("_id") ObjectId work_id, @PathParam("resId") String tyResId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/resourceplan/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourcePlanInTemplate> listResourcePlan(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/_id/{_id}/enabled/{enabled}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void setEnabled(@PathParam("_id") ObjectId _id, @PathParam("enabled") boolean enabled,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/_id/{_id}/project_id/{project_id}/checkoutBy/{checkoutBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void useTemplate(@PathParam("_id") ObjectId _id, @PathParam("project_id") ObjectId project_id,
			@PathParam("checkoutBy") String checkoutBy,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/ganttdata/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result updateGanttData(WorkspaceGanttData ganttData,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/enabledtemplate/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板选择器列表/" + DataSet.COUNT)
	public long countEnabledTemplate(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/enabledtemplate/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目模板选择器列表/" + DataSet.LIST)
	public List<ProjectTemplate> createEnabledTemplateDataSet(
			@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织模板/" + DataSet.INSERT)
	public OBSModule insertOBSModule(@MethodParam(MethodParam.OBJECT) OBSModule obsModule,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织模板/" + DataSet.COUNT)
	public long countOBSModule(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织模板/" + DataSet.LIST)
	public List<OBSModule> listOBSModule(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/obsModule/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "组织模板/" + DataSet.DELETE })
	public long deleteOBSModule(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/obsModule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "组织模板/" + DataSet.UPDATE })
	public long updateOBSModule(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/id/{_id}/obsModule/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("OBS模板组织结构图/" + DataSet.LIST)
	public List<OBSInTemplate> getOBSInTemplateByModule(
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId module_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/id/{_id}/obsModule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织模板/" + DataSet.INPUT)
	public OBSModule getOBSModule(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/useOBSModule/_id/{_id}/project_id/{project_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void useOBSModule(@PathParam("_id") ObjectId _id, @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/project_id/{project_id}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织模板选择器列表/" + DataSet.COUNT)
	public long countOBSModuleSelector(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("project_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/project_id/{project_id}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("组织模板选择器列表/" + DataSet.LIST)
	public List<OBSModule> listOBSModuleSelector(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("project_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/template_id/{template_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void templateOBSSaveAsOBSModule(OBSModule obsModule, @PathParam("template_id") ObjectId template_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/obsModule/project_id/{project_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void projectOBSSaveAsOBSModule(OBSModule obsModule, @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
}
