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
import com.bizvisionsoft.service.model.ProblemCostItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SeverityInd;
import com.mongodb.BasicDBObject;

@Path("/problem")
public interface ProblemService {

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Problem get(@PathParam("_id") @MethodParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/cost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getSummaryCost(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单（已创建）/delete")
	public long deleteProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
	@Path("/item/{status}/{userid}/count/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建）/count", "问题清单（解决中）/count", "问题清单（已关闭）/count", "问题清单（已取消）/count", "已创建问题看板/count" })
	public long countProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建）/update", "问题清单（解决中）/update" })
	public long updateProblems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);

	@POST
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单（已创建）/insert")
	public Problem insertProblem(@MethodParam(MethodParam.OBJECT) Problem p);

	@POST
	@Path("/item/{status}/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题清单（已创建）/list", "问题清单（解决中）/list", "问题清单（已关闭）/list", "问题清单（已取消）/list" })
	public List<Problem> listProblems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/cc/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countCauseConsequences(BasicDBObject filter);

	@DELETE
	@Path("/cc/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteCauseConsequence(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d1/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD1CFT(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d2/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD2ProblemPhotos(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d5/pca/_id/{_id}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD5PCA(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@DELETE
	@Path("/d7/simi/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD7Similar(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d8/exp/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD8Exp(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D0问题初始记录信息面板/list")
	public Problem info(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId _id,
			@PathParam("lang") @MethodParam(MethodParam.LANG) String lang);

	@GET
	@Path("/chart/problem_id/{problem_id}/type/{type}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getCauseConsequence(@PathParam("problem_id") ObjectId problem_id, @PathParam("type") String type);

	@GET
	@Path("/_id/{_id}/d2/desc")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2问题描述5W2H信息面板/list")
	public Document getD2ProblemDesc(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@POST
	@Path("/_id/{_id}/d2/photo/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2现场照片表格/list")
	public List<Document> listD2ProblemPhotos(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@GET
	@Path("/_id/{_id}/d4/rootCauseDesc")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD4RootCauseDesc(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/d5/criteria")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD5DecisionCriteria(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/d7/simi")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD7Similar(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/d8/exp")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD8Exp(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/d3/confirm/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> icaConfirm(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/cc/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CauseConsequence insertCauseConsequence(CauseConsequence cc);

	@POST
	@Path("/d1/item/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD1Item(Document d1, @PathParam("lang") String lang, @PathParam("render") String render);

	@POST
	@Path("/d2/photo/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD2ProblemPhoto(Document t, @PathParam("lang") String lang, @PathParam("render") String render);

	@POST
	@Path("/d4/rootCauseDesc/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD4RootCauseDesc(Document t, @PathParam("lang") String language);

	@POST
	@Path("/d5/criteria/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD5DecisionCriteria(Document t, @PathParam("lang") String language);

	@POST
	@Path("/d5/pca/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertD5PCA(Document t, @PathParam("lang") String language);

	@POST
	@Path("/d7/ss/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD7Similar(Document t, @PathParam("lang") String lang, @PathParam("render") String render);

	@POST
	@Path("/d8/exp/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD8Experience(Document t, @PathParam("lang") String lang, @PathParam("render") String render);

	@POST
	@Path("/cc/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CauseConsequence> listCauseConsequences(BasicDBObject filter);

	// @POST
	// @Path("/_id/{_id}/d0/{render}/{lang}")
	// @Consumes("application/json; charset=UTF-8")
	// @Produces("application/json; charset=UTF-8")
	// @DataSet({ "D0紧急反应行动/list", "D0紧急反应行动表格/list" })
	// public List<Document> listD0(@MethodParam(MethodParam.CONDITION)
	// BasicDBObject condition,
	// @PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID)
	// ObjectId problem_id,
	// @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
	// @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/_id/{_id}/d1/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D1多功能小组/list", "D1多功能小组表格/list" })
	public List<Document> listD1(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/_id/{_id}/d2/cards/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2问题描述/list")
	public List<Document> listD2(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d4/cards/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D4根本原因分析/list")
	public List<Document> listD4(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d5/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D5永久措施/list")
	public List<Document> listD5(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d5/pca/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> listD5PCA(@PathParam("_id") ObjectId problem_id, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d7/ss/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7类似系统表格/list")
	public List<Document> listD7Similar(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/_id/{_id}/d7/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7系统预防/list")
	public List<Document> listD7(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	
	@POST
	@Path("/_id/{_id}/d8/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8关闭结案/list", "D8经验教训表格/list" })
	public List<Document> listD8(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/item/{status}/{userid}/card/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "已创建问题看板/list", "解决中问题看板/list", "已关闭问题看板/list", "已取消问题看板/list" })
	public List<Document> listProblemsCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/cc/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateCauseConsequence(BasicDBObject fu);

	@POST
	@Path("/d2/pd/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD2ProblemDesc(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/d4/rootCauseDesc/{lang}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD4RootCauseDesc(BasicDBObject fu, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/d5/criteria/{lang}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD5DecisionCriteria(BasicDBObject fu, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/d5/pca/{lang}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateD5PCA(BasicDBObject fu, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/d7/simi/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD7Similar(Document t, @PathParam("lang") String lang, @PathParam("render") String render);

	@PUT
	@Path("/d8/exp/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD8Exp(Document t, @PathParam("lang") String lang, @PathParam("render") String render);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/serverityInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "严重性等级/" + DataSet.LIST })
	public List<SeverityInd> listSeverityInd();

	@POST
	@Path("/serverityInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("严重性等级/" + DataSet.INSERT)
	public SeverityInd insertSeverityInd(@MethodParam(MethodParam.OBJECT) SeverityInd item);

	@DELETE
	@Path("/serverityInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("严重性等级/" + DataSet.DELETE)
	public long deleteSeverityInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/serverityInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("严重性等级/" + DataSet.UPDATE)
	public long updateSeverityInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/lostInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "损失等级/" + DataSet.LIST })
	public List<LostInd> listLostInd();

	@POST
	@Path("/lostInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("损失等级/" + DataSet.INSERT)
	public LostInd insertLostInd(@MethodParam(MethodParam.OBJECT) LostInd item);

	@DELETE
	@Path("/lostInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("损失等级/" + DataSet.DELETE)
	public long deleteLostInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/lostInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("损失等级/" + DataSet.UPDATE)
	public long updateLostInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/freqInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "频度等级/" + DataSet.LIST })
	public List<FreqInd> listFreqInd();

	@POST
	@Path("/freqInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("频度等级/" + DataSet.INSERT)
	public FreqInd insertFreqInd(@MethodParam(MethodParam.OBJECT) FreqInd item);

	@DELETE
	@Path("/freqInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("频度等级/" + DataSet.DELETE)
	public long deleteFreqInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/freqInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("频度等级/" + DataSet.UPDATE)
	public long updateFreqInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/incidenceInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "影响范围等级/" + DataSet.LIST })
	public List<IncidenceInd> listIncidenceInd();

	@POST
	@Path("/incidenceInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("影响范围等级/" + DataSet.INSERT)
	public IncidenceInd insertIncidenceInd(@MethodParam(MethodParam.OBJECT) IncidenceInd item);

	@DELETE
	@Path("/incidenceInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("影响范围等级/" + DataSet.DELETE)
	public long deleteIncidenceInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/incidenceInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("影响范围等级/" + DataSet.UPDATE)
	public long updateIncidenceInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/classifyProblemLost/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.LIST })
	public List<ClassifyProblemLost> rootClassifyProblemLost();

	@POST
	@Path("/classifyProblemLost/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ClassifyProblemLost> listClassifyProblemLost(BasicDBObject filter);

	@POST
	@Path("/classifyProblemLost/parent/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countClassifyProblemLost(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/classifyProblemLost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.INSERT })
	public ClassifyProblemLost insertClassifyProblemLost(@MethodParam(MethodParam.OBJECT) ClassifyProblemLost ai);

	@DELETE
	@Path("/classifyProblemLost/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.DELETE })
	public long deleteClassifyProblemLost(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/classifyProblemLost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本分类/" + DataSet.UPDATE })
	public long updateClassifyProblemLost(BasicDBObject filterAndUpdate);

	@POST
	@Path("/classifyProblem/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.LIST })
	public List<ClassifyProblem> rootClassifyProblem();

	@POST
	@Path("/classifyProblem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ClassifyProblem> listClassifyProblem(BasicDBObject filter);

	@POST
	@Path("/classifyProblem/parent/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countClassifyProblem(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/classifyProblem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.INSERT })
	public ClassifyProblem insertClassifyProblem(@MethodParam(MethodParam.OBJECT) ClassifyProblem ai);

	@DELETE
	@Path("/classifyProblem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.DELETE })
	public long deleteClassifyProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/classifyProblem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题分类/" + DataSet.UPDATE })
	public long updateClassifyProblem(BasicDBObject filterAndUpdate);

	@POST
	@Path("/classifyCause/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.LIST })
	public List<ClassifyCause> rootClassifyCause();

	@POST
	@Path("/classifyCause/root/selector/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类（选择器用）/" + DataSet.LIST })
	public List<ClassifyCause> rootClassifyCauseSelector(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) CauseConsequence cc);

	@POST
	@Path("/classifyCause/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ClassifyCause> listClassifyCause(BasicDBObject filter);

	@POST
	@Path("/classifyCause/parent/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countClassifyCause(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/classifyCause/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.INSERT })
	public ClassifyCause insertClassifyCause(@MethodParam(MethodParam.OBJECT) ClassifyCause ai);

	@DELETE
	@Path("/classifyCause/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.DELETE })
	public long deleteClassifyCause(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/classifyCause/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "原因分类/" + DataSet.UPDATE })
	public long updateClassifyCause(BasicDBObject filterAndUpdate);

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题成本项
	@DELETE
	@Path("/costItem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8问题成本账目表格/delete")
	public long deleteCostItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
	@Path("/{_id}/costItem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8问题成本账目表格/list" })
	public List<ProblemCostItem> listCostItems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@POST
	@Path("/{_id}/costItem/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8问题成本账目表格/count" })
	public long countCostItems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@PUT
	@Path("/costItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8问题成本账目表格/update" })
	public long updateCostItems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);

	@POST
	@Path("/{_id}/costItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8问题成本账目表格/insert")
	public ProblemCostItem insertCostItem(@MethodParam(MethodParam.OBJECT) ProblemCostItem p,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@POST
	@Path("/{_id}/chart/periodCost")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8问题成本期间分类汇总/list")
	public Document periodCostChart(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 通用行动
	@POST
	@Path("/_id/{_id}/{stage}/action/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertAction(Document t, @PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage, @PathParam("lang") String lang, @PathParam("render") String render);

	@DELETE
	@Path("/action/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteAction(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/action/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getAction(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/{stage}/actions/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D0紧急反应行动/list", "D0紧急反应行动表格/list", //
			"D3临时控制行动/list", "D3临时控制行动表格/list", //
			"D6执行和确认/list", "D6执行和确认永久纠正措施表格/list", //
			"D7系统性预防措施表格/list",  //
			"D8损失挽回措施表格/list" })
	public List<Document> listActions(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@MethodParam("render") @PathParam("render") String render);

	@PUT
	@Path("/action/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateAction(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang,
			@PathParam("render") String render);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 行动预案
	@POST
	@Path("/_id/{_id}/{stage}/advisableplan/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("行动预案选择列表/list")
	public List<Document> listAdvisablePlan(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage
			);

	@POST
	@Path("/_id/{_id}/{stage}/advisableplan/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("行动预案选择列表/count")
	public long countAdvisablePlan(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage);

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按成本分类钻取
	@POST
	@Path("/cost/selector/root/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/list","问题成本按问题分类钻取/list","问题成本按问题原因分类钻取/list" })
	public List<Catalog> listClassifyRoot();

	@POST
	@Path("/cost/selector/classifycost/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/slist" })
	public List<Catalog> listClassifyCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/classifycost/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/scount" })
	public long countClassifyCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/cost/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/default","问题成本按问题分类钻取/default","问题成本按责任部门分类钻取/default","问题成本按问题原因分类钻取/default" })
	public Document defaultClassifyCostOption();
	
	@POST
	@Path("/cost/classifycost/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按成本分类钻取/chart" })
	public Document createClassifyCostChart(@MethodParam(MethodParam.CONDITION) Document condition);

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按问题分类钻取

	@POST
	@Path("/cost/selector/classifyproblem/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题分类钻取/slist" })
	public List<Catalog> listClassifyProblemStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/classifyproblem/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题分类钻取/scount" })
	public long countClassifyProblemStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/cost/classifyproblem/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题分类钻取/chart" })
	public Document createClassifyProblemChart(@MethodParam(MethodParam.CONDITION) Document condition);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按责任部门分类钻取
	
	@POST
	@Path("/cost/selector/root/org")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/list" })
	public List<Catalog> listOrgRoot();

	@POST
	@Path("/cost/selector/dept/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/slist" })
	public List<Catalog> listOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/dept/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/scount" })
	public long countOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/res/dept/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按责任部门分类钻取/chart" })
	public Document createClassifyDeptChart(@MethodParam(MethodParam.CONDITION) Document condition);
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 成本分析 问题成本按原因分类钻取

	@POST
	@Path("/cost/selector/cause/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题原因分类钻取/slist" })
	public List<Catalog> listClassifyCauseStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/cause/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题原因分类钻取/scount" })
	public long countClassifyCauseStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/res/cause/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题成本按问题原因分类钻取/chart" })
	public Document createClassifyCauseChart(@MethodParam(MethodParam.CONDITION) Document condition);
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 问题综合分析 
	@POST
	@Path("/cost/classifyproblem/bar")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题损失按问题分类图表/list" })
	public Document createCostClassifyByProblemChart();
	
	@POST
	@Path("/count/classifyproblem/bar")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题数量按问题分类图表/list" })
	public Document createCountClassifyByProblemChart();
	
	@POST
	@Path("/cost/classifyCause/pie")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题损失按原因分类图表/list" })
	public Document createCostClassifyByCauseChart();
	
	@POST
	@Path("/count/classifyCause/pie")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "问题损失按部门分类图表/list" })
	public Document createCostClassifyByDeptChart();

	@POST
	@Path("/spa/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("系统性预防措施表格/list")
	public List<Document> listSPA(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/spa/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("系统性预防措施表格/count")
	public long countSPA(@MethodParam(MethodParam.FILTER) BasicDBObject filter);


}
