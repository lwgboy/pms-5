package com.bizvisionsoft.service;

import java.util.Date;
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
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.BaselineComparable;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SalesItem;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.Workspace;
import com.mongodb.BasicDBObject;

@Path("/project")
public interface ProjectService {

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Project insert(Project project);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long update(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/_id/{project_id}/id/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateProjectId(@PathParam("project_id") ObjectId _id, @PathParam("id") String id);

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Project get(@PathParam("_id") @MethodParam("_id") ObjectId _id);

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Project> list(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@GET
	@Path("/_id/{_id}/daterange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Date> getPlanDateRange(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/stage/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listStage(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/stage/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countStage(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/mystage/{userId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listMyStage(@PathParam("userId") String userId);

	@POST
	@Path("/mystage/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countMyStage(@PathParam("userId") String userId);

	@POST
	@Path("/command/start/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startProject(Command command);

	@POST
	@Path("/command/approve/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void approveProject(Command command);

	@POST
	@Path("/command/distribute/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> distributeProjectPlan(Command command);

	@POST
	@Path("/_id/{_id}/stockholder/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目干系人/list", "项目干系人（查看）/list" })
	public List<Stockholder> getStockholders(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@Path("/_id/{_id}/stockholder/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目干系人/count", "项目干系人（查看）/count" })
	public long countStockholders(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/stockholder")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Stockholder insertStockholder(Stockholder c);

	@PUT
	@Path("/stockholder")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目干系人/" + DataSet.UPDATE)
	public long updateStockholder(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/stockholder/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目干系人/" + DataSet.DELETE)
	public long deleteStockholder(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId id);

	@POST
	@Path("/pm/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Project> listManagedProjects(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/pm/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countManagedProjects(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的项目/list", "我的项目（首页小组件）/list", "我的项目选择列表/list" })
	public List<Project> listParticipatedProjects(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的项目/count", "我的项目（首页小组件）/count", "我的项目选择列表/count" })
	public long countParticipatedProjects(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/daily/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我报告的项目选择列表-日报/list" })
	public List<Project> listParticipatedProjectsInDaily(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/daily/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我报告的项目选择列表-日报/count" })
	public long countParticipatedProjectsInDaily(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/weekly/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我报告的项目选择列表-周报/list" })
	public List<Project> listParticipatedProjectsInWeekly(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/weekly/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我报告的项目选择列表-周报/count" })
	public long countParticipatedProjectsInWeekly(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/monthly/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我报告的项目选择列表-月报/list" })
	public List<Project> listParticipatedProjectsInMonthly(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/monthly/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我报告的项目选择列表-月报/count" })
	public long countParticipatedProjectsInMonthly(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@DELETE
	@Path("/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的项目/" + DataSet.DELETE, "所有项目/" + DataSet.DELETE })
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId id);

	@GET
	@Path("/workspace/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Workspace getWorkspace(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/command/finish/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishProject(Command command);

	@POST
	@Path("/_id/{_id}/schedule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Integer schedule(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/command/close/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> closeProject(Command command);

	@POST
	@Path("/_id/{_id}/news/{count}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<News> getRecentNews(@PathParam("_id") ObjectId _id, @PathParam("count") int count);

	@POST
	@Path("/salesItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public SalesItem insertSalesItem(SalesItem salesItem);

	@POST
	@Path("/_id/{_id}/baseline/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目基线/" + DataSet.LIST, "项目基线（查看）/" + DataSet.LIST })
	public List<Baseline> listBaseline(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/baseline/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目基线/" + DataSet.COUNT, "项目基线（查看）/" + DataSet.COUNT })
	public long countBaseline(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/baseline")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目基线/" + DataSet.INSERT)
	public Baseline createBaseline(Baseline baseline);

	@DELETE
	@Path("/baseline/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目基线/" + DataSet.DELETE)
	public long deleteBaseline(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/baseline/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目基线/" + DataSet.UPDATE)
	public long updateBaseline(BasicDBObject filterAndUpdate);

	@POST
	@Path("/_id/{_id}/projectchange/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目变更/" + DataSet.LIST, "项目变更（查看）/" + DataSet.LIST })
	public List<ProjectChange> listProjectChange(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/projectchange/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目变更/" + DataSet.COUNT, "项目变更（查看）/" + DataSet.COUNT })
	public long countProjectChange(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/projectchange/reviewer/{userId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "待审批的项目变更/" + DataSet.LIST })
	public List<ProjectChange> listReviewerProjectChange(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/projectchange/reviewer/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "待审批的项目变更/" + DataSet.COUNT, "审批项目变更/budget" })
	public long countReviewerProjectChange(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/projectchange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目变更/" + DataSet.INSERT)
	public ProjectChange createProjectChange(ProjectChange pc);

	@DELETE
	@Path("/projectchange/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目变更/" + DataSet.DELETE)
	public long deleteProjectChange(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/projectchange/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目变更/" + DataSet.UPDATE)
	public long updateProjectChange(BasicDBObject filterAndUpdate);

	@GET
	@Path("/projectchange/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ProjectChange getProjectChange(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/projectchange/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateProjectChange(ProjectChangeTask projectChangeTask, @PathParam("_id") ObjectId _id);

	@POST
	@Path("/projectchange/{_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "变更详情信息面板/list", "变更审核信息面板/list", "变更详情信息面板（查看）/list", "变更审核信息面板（查看）/list" })
	public List<ProjectChange> listProjectChangeInfo(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/submitprojectchange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> submitProjectChange(List<ObjectId> projectChangeIds);

	@POST
	@Path("/passprojectchange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> passProjectChange(ProjectChangeTask projectChangeTask);

	@POST
	@Path("/cancelprojectchange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> cancelProjectChange(ProjectChangeTask projectChangeTask);

	@POST
	@Path("baselinevomparable")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<BaselineComparable> getBaselineComparable(List<ObjectId> projectIds);

	@PUT
	@Path("/projectchange/{_id}/createcheck")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long checkCreateProjectChange(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/confirmprojectchange/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> confirmProjectChange(List<ObjectId> projectChangeIds, @PathParam("userId") String userId);

	@POST
	@Path("/managedby/{managerId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我管理的项目清单/list")
	public List<Project> listAdministratedProjects(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("managerId") @MethodParam(MethodParam.CURRENT_USER_ID) String managerId);

	@POST
	@Path("/managedby/{managerId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我管理的项目清单/count")
	public long countAdministratedProjects(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("managerId") @MethodParam(MethodParam.CURRENT_USER_ID) String managerId);

	@POST
	@Path("/all/userid/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "所有项目/list", "项目选择列表/list" })
	public List<Project> listAllProjects(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/all/userid/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "所有项目/count", "项目选择列表/count" })
	public long countAllProjects(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/addOBSModule/{module_id}/{parent_id}/{cover}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void addOBSModule(@PathParam("module_id") ObjectId module_id, @PathParam("parent_id") ObjectId parent_id,
			@PathParam("cover") boolean cover);

	@GET
	@Path("/isRoleNumberDuplicated /{module_id}/{scope_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean isRoleNumberDuplicated (@PathParam("module_id") ObjectId module_id,
			@PathParam("scope_id") ObjectId scope_id);

}
