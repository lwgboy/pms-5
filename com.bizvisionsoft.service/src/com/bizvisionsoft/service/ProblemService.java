package com.bizvisionsoft.service;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.ClassifyCause;
import com.bizvisionsoft.service.model.ClassifyProblem;
import com.bizvisionsoft.service.model.ClassifyProblemLost;
import com.bizvisionsoft.service.model.FreqInd;
import com.bizvisionsoft.service.model.IncidenceInd;
import com.bizvisionsoft.service.model.LostInd;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.ProblemActionInfo;
import com.bizvisionsoft.service.model.ProblemActionLinkInfo;
import com.bizvisionsoft.service.model.ProblemCostItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SeverityInd;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;

@Path("/problem")
@Api("/problem")
public interface ProblemService {

	/**
	 * 添加或删除CTF团队成员
	 */
	public static final String ACTION_EDIT_TEAM = "editTeam";

	public static final String ACTION_DELETE_SIMILAR = "deleteSimilar";

	public static final String ACTION_EDIT_SIMILAR = "editSimilar";

	public static final String ACTION_CREATE = "create";

	public static final String ACTION_FINISH = "finish";

	public static final String ACTION_VERIFY = "verify";

	public static final String ACTION_DELETE = "delete";

	public static final String ACTION_READ = "read";

	public static final String ACTION_EDIT = "edit";

	public static final String ACTION_PCA_APPROVE = "pcaApproved";

	public static final String ACTION_ICA_CONFIRM = "icaConfirmed";

	public static final String ACTION_PCA_VALIDATE = "pcaValidated";

	public static final String ACTION_PCA_CONFIRM = "pcaConfirmed";

	public static final String ACTION_PROBLEM_START = "startProblem";

	public static final String ACTION_PROBLEM_CLOSE = "closeProblem";

	public static final String ACTION_PROBLEM_CANCEL = "cancelProblem";

	public static final String[] actionType = new String[] { "era", "ica", "pca", "spa", "lra" };

	public static final String[] actionName = new String[] { "紧急反应行动", "临时控制行动", "永久纠正措施", "系统性预防措施", "挽回损失和善后措施" };

	public static final String[] cftRoleText = new String[] { "组长", "设计", "工艺", "生产", "质量", "顾客代表", "ERA", "ICA", "PCA",
			"SPA", "LRA" };

	public static final String[] similarDegreeText = new String[] { "相同", "近似", "类似", "不同" };

	@GET
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Problem get(@PathParam("_id") @MethodParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/cost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getSummaryCost(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单（已创建）/delete")
	public long deleteProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/item/{status}/{userid}/count/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建）/count", "问题清单（解决中）/count", "问题清单（已关闭）/count", "问题清单（已取消）/count", "已创建问题看板/count" })
	public long countProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建）/" + DataSet.UPDATE, "问题清单（解决中）/" + DataSet.UPDATE , "D8会议纪要和最终报告/update" })
	public long updateProblems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/item/{msgCode}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateProblemsLifecycle(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu,
			@PathParam("msgCode") String msgCode, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单（已创建）/insert")
	public Problem insertProblem(@MethodParam(MethodParam.OBJECT) Problem p,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/item/{status}/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建）/list", "问题清单（解决中）/list", "问题清单（已关闭）/list", "问题清单（已取消）/list" })
	public List<Problem> listProblems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/allitem/{status}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建-all）/list", "问题清单（解决中-all）/list", "问题清单（已关闭-all）/list", "问题清单（已取消-all）/list" })
	public List<Problem> listAllProblems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/allitem/{status}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建-all）/" + DataSet.COUNT, "问题清单（解决中-all）/" + DataSet.COUNT, "问题清单（已关闭-all）/" + DataSet.COUNT,
			"问题清单（已取消-all）/" + DataSet.COUNT })
	public long countAllProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/uncancelitem/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（问题数据库）/list" })
	public List<Problem> listUnCancelProblems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/uncancelitem/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（问题数据库）/" + DataSet.COUNT })
	public long countUnCancelProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cc/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countCauseConsequences(BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/cc/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteCauseConsequence(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/d1/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD1CFT(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/d2/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD2ProblemPhotos(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/d4/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD4Verify(@PathParam("_id") ObjectId _id, 
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	
	@DELETE
	@Path("/{domain}/d5/pca/_id/{_id}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD5PCA(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/d7/simi/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD7Similar(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/d8/exp/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD8Exp(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({"D0问题初始记录信息面板/list", "D8会议纪要和最终报告/list"})
	public Problem info(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId _id,
			@PathParam("lang") @MethodParam(MethodParam.LANG) String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/chart/problem_id/{problem_id}/type/{type}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getCauseConsequence(@PathParam("problem_id") ObjectId problem_id, @PathParam("type") String type,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/d2/desc")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2问题描述5W2H信息面板/list")
	public Document getD2ProblemDesc(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d2/photo/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2现场照片表格/list")
	public List<Document> listD2ProblemPhotos(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/d4/rootCauseDesc")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD4RootCauseDesc(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d4/verify/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D4原因分析验证记录表格/list")
	public List<Document> listD4Verify(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	
	@GET
	@Path("/{domain}/_id/{_id}/d4/verify")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD4Verify(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/d5/criteria")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD5DecisionCriteria(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/d7/simi")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD7Similar(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}/d8/exp")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD8Exp(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d3/confirm/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> icaConfirm(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cc/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CauseConsequence insertCauseConsequence(CauseConsequence cc,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/ccs/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertCauseConsequences(List<CauseConsequence> cc,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/ccs/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateCauseConsequences(List<CauseConsequence> cc,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d1/item/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD1Item(Document d1, @PathParam("lang") String lang, @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d1/items")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD1Items(List<Document> d1, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d1/items")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD1Items(List<Document> d1, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d2/photo/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD2ProblemPhoto(Document t, @PathParam("lang") String lang, @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d2/photos")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD2ProblemPhotos(List<Document> t,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d2/photos")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD2ProblemPhotos(List<Document> t,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d4/rootCauseDesc/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD4RootCauseDesc(Document t, @PathParam("lang") String language,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d4/verify/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD4Verify(Document t, @PathParam("lang") String lang,
			@PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d4/verify/{render}/{lang}/update")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD4Verify(Document t, @PathParam("lang") String lang,
			@PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	
	@POST
	@Path("/{domain}/d5/criteria/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD5DecisionCriteria(Document t, @PathParam("lang") String language,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d5/pca/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD5PCA(Document t, @PathParam("lang") String language,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d7/ss/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD7Similar(Document t, @PathParam("lang") String lang, @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d7/sss")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD7Similars(List<Document> t, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d7/sss")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD7Similars(List<Document> t, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d8/exp/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD8Experience(Document t, @PathParam("lang") String lang, @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	
	@POST
	@Path("/{domain}/d8/exp")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD8Experiences(List<Document> t, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cc/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CauseConsequence> listCauseConsequences(BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	// @POST
	// @Path("/{domain}/_id/{_id}/d0/{render}/{lang}")
	// @Consumes("application/json; charset=UTF-8")
	// @Produces("application/json; charset=UTF-8")
	// @DataSet({ "D0紧急反应行动/list", "D0紧急反应行动表格/list" })
	// public List<Document> listD0(@MethodParam(MethodParam.CONDITION)
	// BasicDBObject condition,
	// @PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID)
	// ObjectId problem_id,
	// @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
	// @MethodParam("render") @PathParam("render") String
	// render,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d0init/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D0问题初始记录/list" })
	public List<Document> listD0Init(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d1/{userid}/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D1多功能小组/list", "D1多功能小组表格/list" })
	public List<Document> listD1(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d2/cards/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2问题描述/list")
	public List<Document> listD2(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d4/cards/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D4根本原因分析/list")
	public List<Document> listD4(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d5/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D5永久措施/list")
	public List<Document> listD5(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d5/pca/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> listD5PCA(@PathParam("_id") ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d7/ss/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7类似系统表格/list")
	public List<Document> listD7Similar(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d7/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7系统预防/list")
	public List<Document> listD7(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/d8/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8关闭结案/list", "D8经验教训表格/list" })
	public List<Document> listD8(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/exp/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题经验库表格/list" })
	public List<Document> listExp(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/exp/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题经验库表格/count" })
	public long countExp(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/item/{status}/{userid}/card/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "已创建问题看板/list", "解决中问题看板/list", "已关闭问题看板/list", "已取消问题看板/list" })
	public List<Document> listProblemsCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/itemtodo/{status}/{userid}/card/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "待办问题看板/list" })
	public List<Document> listProblemsToDo(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	
	@PUT
	@Path("/{domain}/cc/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateCauseConsequence(BasicDBObject fu,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/d2/pd/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD2ProblemDesc(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d4/rootCauseDesc/{lang}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD4RootCauseDesc(BasicDBObject fu, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d5/criteria/{lang}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD5DecisionCriteria(BasicDBObject fu,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d5/pca/{lang}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD5PCA(BasicDBObject fu, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d7/simi/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD7Similar(Document t, @PathParam("lang") String lang, @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/d8/exp/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD8Exp(Document t, @PathParam("lang") String lang, @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	

	@PUT
	@Path("/{domain}/d8/exp")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD8Exps(List<Document> t, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/serverityInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "严重性等级/" + DataSet.LIST })
	public List<SeverityInd> listSeverityInd(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/serverityInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("严重性等级/" + DataSet.INSERT)
	public SeverityInd insertSeverityInd(@MethodParam(MethodParam.OBJECT) SeverityInd item,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/serverityInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("严重性等级/" + DataSet.DELETE)
	public long deleteSeverityInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/serverityInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("严重性等级/" + DataSet.UPDATE)
	public long updateSeverityInd(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/lostInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "损失等级/" + DataSet.LIST })
	public List<LostInd> listLostInd(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/lostInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("损失等级/" + DataSet.INSERT)
	public LostInd insertLostInd(@MethodParam(MethodParam.OBJECT) LostInd item,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/lostInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("损失等级/" + DataSet.DELETE)
	public long deleteLostInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/lostInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("损失等级/" + DataSet.UPDATE)
	public long updateLostInd(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/freqInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "频度等级/" + DataSet.LIST })
	public List<FreqInd> listFreqInd(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/freqInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("频度等级/" + DataSet.INSERT)
	public FreqInd insertFreqInd(@MethodParam(MethodParam.OBJECT) FreqInd item,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/freqInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("频度等级/" + DataSet.DELETE)
	public long deleteFreqInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/freqInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("频度等级/" + DataSet.UPDATE)
	public long updateFreqInd(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/incidenceInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "影响范围等级/" + DataSet.LIST })
	public List<IncidenceInd> listIncidenceInd(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/incidenceInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("影响范围等级/" + DataSet.INSERT)
	public IncidenceInd insertIncidenceInd(@MethodParam(MethodParam.OBJECT) IncidenceInd item,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/incidenceInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("影响范围等级/" + DataSet.DELETE)
	public long deleteIncidenceInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/incidenceInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("影响范围等级/" + DataSet.UPDATE)
	public long updateIncidenceInd(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/classifyProblemLost/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.LIST })
	public List<ClassifyProblemLost> rootClassifyProblemLost(
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblemLost/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ClassifyProblemLost> listClassifyProblemLost(BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblemLost/parent/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countClassifyProblemLost(@PathParam("parent_id") ObjectId parent_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblemLost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.INSERT })
	public ClassifyProblemLost insertClassifyProblemLost(@MethodParam(MethodParam.OBJECT) ClassifyProblemLost ai,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/classifyProblemLost/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.DELETE })
	public long deleteClassifyProblemLost(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/classifyProblemLost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.UPDATE })
	public long updateClassifyProblemLost(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblem/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.LIST, "问题分类（多选显示）/" + DataSet.LIST, "问题分类选择表格（查询用）/list" })
	public List<ClassifyProblem> rootClassifyProblem(
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ClassifyProblem> listClassifyProblem(BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblem/parent/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countClassifyProblem(@PathParam("parent_id") ObjectId parent_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.INSERT })
	public ClassifyProblem insertClassifyProblem(@MethodParam(MethodParam.OBJECT) ClassifyProblem ai,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/classifyProblem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.DELETE })
	public long deleteClassifyProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/classifyProblem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.UPDATE })
	public long updateClassifyProblem(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyCause/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.LIST })
	public List<ClassifyCause> rootClassifyCause(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyCause/root/selector/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类（选择器用）/" + DataSet.LIST })
	public List<ClassifyCause> rootClassifyCauseSelector(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) CauseConsequence cc,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyCause/document/root/selector/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类（整体创建选择器用）/" + DataSet.LIST })
	public List<ClassifyCause> rootClassifyCauseSelector(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) Document doc,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyCause/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ClassifyCause> listClassifyCause(BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyCause/parent/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countClassifyCause(@PathParam("parent_id") ObjectId parent_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyCause/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.INSERT })
	public ClassifyCause insertClassifyCause(@MethodParam(MethodParam.OBJECT) ClassifyCause ai,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/classifyCause/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.DELETE })
	public long deleteClassifyCause(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/classifyCause/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.UPDATE })
	public long updateClassifyCause(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题成本项
	@DELETE
	@Path("/{domain}/costItem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8问题成本账目表格/delete")
	public long deleteCostItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/{_id}/costItem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8问题成本账目表格/list" })
	public List<ProblemCostItem> listCostItems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/{_id}/costItem/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8问题成本账目表格/count" })
	public long countCostItems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/costItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8问题成本账目表格/" + DataSet.UPDATE })
	public long updateCostItems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/{_id}/costItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8问题成本账目表格/insert")
	public ProblemCostItem insertCostItem(@MethodParam(MethodParam.OBJECT) ProblemCostItem p,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/{_id}/chart/periodCost")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8问题成本期间分类汇总/list")
	public Document periodCostChart(
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 通用行动
	@POST
	@Path("/{domain}/_id/{_id}/{stage}/action/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertAction(Document t,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage, @PathParam("lang") String lang,
			@PathParam("render") String render, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/actions/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertActions(List<Document> actions,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/actions/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateActions(List<Document> actions,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/action/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteAction(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/action/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getAction(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/{stage}/actions/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D0紧急反应行动/list", "D0紧急反应行动表格/list", //
			"D3临时控制行动/list", "D3临时控制行动表格/list", //
			"D6执行和确认/list", "D6执行和确认永久纠正措施表格/list", //
			"D7系统性预防措施表格/list", //
			"D8损失挽回措施表格/list" })
	public List<Document> listActions(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/action/{msgType}/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateAction(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@PathParam("render") String render, @PathParam("msgType") String msgType,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 行动预案
	@POST
	@Path("/{domain}/_id/{_id}/{stage}/advisableplan/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("行动预案选择列表/list")
	public List<Document> listAdvisablePlan(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/{stage}/advisableplan/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("行动预案选择列表/count")
	public long countAdvisablePlan(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 目标和决策准则选择器
	@POST
	@Path("/{domain}/_id/{_id}/criteriaTemplate/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("目标和决策准则选择表格/list")
	public List<Document> listCriteriaTemplate(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/criteriaTemplate/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("目标和决策准则选择表格/count")
	public long countCriteriaTemplate(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按成本分类钻取
	@POST
	@Path("/{domain}/cost/selector/root/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/list", "问题成本按问题分类钻取/list", "问题成本按问题原因分类钻取/list" })
	public List<Catalog> listClassifyRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/selector/classifycost/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/slist" })
	public List<Catalog> listClassifyCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/selector/classifycost/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/scount" })
	public long countClassifyCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/default", "问题成本按问题分类钻取/default", "问题成本按责任部门分类钻取/default", "问题成本按问题原因分类钻取/default" })
	public Document defaultClassifyCostOption(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/classifycost/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/chart" })
	public Document createClassifyCostChart(@MethodParam(MethodParam.CONDITION) Document condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按问题分类钻取

	@POST
	@Path("/{domain}/cost/selector/classifyproblem/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题分类钻取/slist", "问题智能分析/slist" })
	public List<Catalog> listClassifyProblemStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/selector/classifyproblem/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题分类钻取/scount", "问题智能分析/scount" })
	public long countClassifyProblemStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/classifyproblem/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题分类钻取/chart" })
	public Document createClassifyProblemChart(@MethodParam(MethodParam.CONDITION) Document condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按责任部门分类钻取

	@POST
	@Path("/{domain}/cost/selector/root/org")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/list" })
	public List<Catalog> listOrgRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/selector/dept/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/slist" })
	public List<Catalog> listOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/selector/dept/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/scount" })
	public long countOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/res/dept/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/chart" })
	public Document createClassifyDeptChart(@MethodParam(MethodParam.CONDITION) Document condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按原因分类钻取

	@POST
	@Path("/{domain}/cost/selector/cause/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题原因分类钻取/slist" })
	public List<Catalog> listClassifyCauseStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/selector/cause/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题原因分类钻取/scount" })
	public long countClassifyCauseStructure(@MethodParam(MethodParam.OBJECT) Catalog parent,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/res/cause/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题原因分类钻取/chart" })
	public Document createClassifyCauseChart(@MethodParam(MethodParam.CONDITION) Document condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题的智能分析
	@POST
	@Path("/{domain}/_id/{_id}/anlysis/root/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题智能分析/list" })
	public List<Catalog> listProblemAnlysisRoot(
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/anlysis/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题智能分析/default" })
	public Document defaultProblemAnlysisOption(
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId problem_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/anlysis/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题智能分析/chart" })
	public Document createProblemAnlysisChart(@MethodParam(MethodParam.CONDITION) Document condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题综合分析
	@POST
	@Path("/{domain}/cost/classifyproblem/bar")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题损失按问题分类条形图/list" })
	public Document createCostClassifyByProblemChart(
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/count/classifyproblem/bar")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题数量按问题分类条形图/list" })
	public Document createCountClassifyByProblemChart(
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cost/classifyCause/pie")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题损失按原因分类饼图/list" })
	public Document createCostClassifyByCauseChart(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/count/classifyCause/pie")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题损失按部门分类饼图/list" })
	public Document createCostClassifyByDeptChart(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/classifyProblem/classifyCause/graph")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题原因因果关系/list" })
	public Document createCauseProblemChart(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/actions/{stage}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("系统性预防措施表格/list")
	public List<Document> listActions(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("stage") @PathParam("stage") String stage,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/actions/{stage}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("系统性预防措施表格/count")
	public long countActions(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("stage") @PathParam("stage") String stage,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/actions/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ProblemActionInfo> listGanttActions(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/links/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ProblemActionLinkInfo> listGanttActionLinks(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/**
	 * 权限
	 * 
	 * @param _id
	 *            问题_id
	 * @param action
	 *            操作代码
	 * @param userId
	 *            用户名
	 * @return 是否有权限
	 */
	@GET
	@Path("/{domain}/_id/{_id}/action/{action}/userId/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean hasPrivate(@PathParam("_id") ObjectId _id, @PathParam("action") String action,
			@PathParam("userId") String userId, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/loadcauseanalysis")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document loadCauseAnalysis(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/createreport/{template}/{fileName}/{serverName}/{serverPath}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public InputStream createReportAndGetDownloadPath(Document rptParam, @PathParam("_id") ObjectId _id,
			@PathParam("template") String template, @PathParam("fileName") String fileName,
			@PathParam("serverName") String serverName, @PathParam("serverPath") int serverPath,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

}
