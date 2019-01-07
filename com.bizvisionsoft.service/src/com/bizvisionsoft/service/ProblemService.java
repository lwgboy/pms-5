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
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.Result;
import com.mongodb.BasicDBObject;

@Path("/problem")
public interface ProblemService {

	public final String[] CauseSubject = { "人", "设备", "材料", "环境", "方法", "测量" };

	@POST
	@Path("/cc/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countCauseConsequences(BasicDBObject filter);

	@POST
	@Path("/item/{status}/{userid}/count/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/count")
	public long countProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

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
	@Path("/d3/ica/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD3ICA(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d5/pca/_id/{_id}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD5PCA(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@DELETE
	@Path("/d6/ivpca/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD6IVPCA(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d7/simi/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD7Similar(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d7/spa/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD7SPA(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/d8/exp/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteD8Exp(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/delete")
	public long deleteProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Problem get(@PathParam("_id") @MethodParam("_id") ObjectId _id);

	@GET
	@Path("/chart/problem_id/{problem_id}/type/{type}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getCauseConsequence(@PathParam("problem_id") ObjectId problem_id, @PathParam("type") String type);

	@GET
	@Path("/_id/{_id}/d2/desc")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD2ProblemDesc(@PathParam("_id") ObjectId problem_id);

	@GET
	@Path("/_id/{_id}/d3/ica")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD3ICA(@PathParam("_id") ObjectId _id);

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
	@Path("/_id/{_id}/d6/ivpca")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD6IVPCA(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/d7/simi")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD7Similar(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/d7/spa")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD7SPA(@PathParam("_id") ObjectId _id);

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
	@Path("/d1/item/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD1Item(Document d1, @PathParam("lang") String lang);

	@POST
	@Path("/d2/photo/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD2ProblemPhoto(Document t, @PathParam("lang") String lang);

	@POST
	@Path("/d3/ica/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD3ICA(Document t, @PathParam("lang") String lang);

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
	@Path("/d6/ivpca/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD6IVPCA(Document t, @PathParam("lang") String language);

	@POST
	@Path("/d7/spa/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD7PreventAction(Document t, @PathParam("lang") String lang);

	@POST
	@Path("/d7/ss/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD7SimilarSituation(Document t, @PathParam("lang") String lang);

	@POST
	@Path("/d8/exp/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD8Experience(Document t, @PathParam("lang") String lang);

	@POST
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/insert")
	public Problem insertProblem(@MethodParam(MethodParam.OBJECT) Problem p);

	@POST
	@Path("/cc/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<CauseConsequence> listCauseConsequences(BasicDBObject filter);

	@POST
	@Path("/_id/{_id}/d1/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D1多功能小组/list")
	public List<Document> listD1(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d2/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2问题描述/list")
	public List<Document> listD2(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d3/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D3临时措施/list")
	public List<Document> listD3(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d4/ds/{lang}")
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
	@Path("/_id/{_id}/d6/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D6执行和确认/list")
	public List<Document> listD6(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d7/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7系统预防/list")
	public List<Document> listD7(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d8/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8关闭结案/list")
	public List<Document> listD8(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d5/pca/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> listPCA(@PathParam("_id") ObjectId problem_id);

	@POST
	@Path("/item/{status}/{userid}/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/list")
	public List<Problem> listProblems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
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
	@Path("/d3/ica/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD3ICA(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

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
	@Path("/d6/pca/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD6IVPCA(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/d7/simi/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD7Similar(Document t, @PathParam("lang") String lang);

	@PUT
	@Path("/d7/spa/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD7SPA(Document t, @PathParam("lang") String lang);

	@PUT
	@Path("/d8/exp/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD8Exp(Document t, @PathParam("lang") String lang);

	@PUT
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/update")
	public long updateProblems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);

}
