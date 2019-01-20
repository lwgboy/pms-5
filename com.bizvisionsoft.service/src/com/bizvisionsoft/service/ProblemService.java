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
	@DataSet("�����嵥���Ѵ�����/delete")
	public long deleteProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
	@Path("/item/{status}/{userid}/count/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����嵥���Ѵ�����/count", "�����嵥������У�/count", "�����嵥���ѹرգ�/count", "�����嵥����ȡ����/count", "�Ѵ������⿴��/count" })
	public long countProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@PUT
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����嵥���Ѵ�����/update", "�����嵥������У�/update" })
	public long updateProblems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);

	@POST
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����嵥���Ѵ�����/insert")
	public Problem insertProblem(@MethodParam(MethodParam.OBJECT) Problem p);

	@POST
	@Path("/item/{status}/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����嵥���Ѵ�����/list", "�����嵥������У�/list", "�����嵥���ѹرգ�/list", "�����嵥����ȡ����/list" })
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
	@DataSet("D0�����ʼ��¼��Ϣ���/list")
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
	@DataSet("D2��������5W2H��Ϣ���/list")
	public Document getD2ProblemDesc(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@POST
	@Path("/_id/{_id}/d2/photo/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2�ֳ���Ƭ���/list")
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
	// @DataSet({ "D0������Ӧ�ж�/list", "D0������Ӧ�ж����/list" })
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
	@DataSet({ "D1�๦��С��/list", "D1�๦��С����/list" })
	public List<Document> listD1(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/_id/{_id}/d2/cards/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2��������/list")
	public List<Document> listD2(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d4/cards/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D4����ԭ�����/list")
	public List<Document> listD4(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d5/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D5���ô�ʩ/list")
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
	@DataSet("D7����ϵͳ���/list")
	public List<Document> listD7Similar(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/_id/{_id}/d7/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7ϵͳԤ��/list")
	public List<Document> listD7(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	
	@POST
	@Path("/_id/{_id}/d8/{render}/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8�رս᰸/list", "D8�����ѵ���/list" })
	public List<Document> listD8(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang, @MethodParam("render") @PathParam("render") String render);

	@POST
	@Path("/item/{status}/{userid}/card/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�Ѵ������⿴��/list", "��������⿴��/list", "�ѹر����⿴��/list", "��ȡ�����⿴��/list" })
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
	@DataSet({ "�����Եȼ�/" + DataSet.LIST })
	public List<SeverityInd> listSeverityInd();

	@POST
	@Path("/serverityInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����Եȼ�/" + DataSet.INSERT)
	public SeverityInd insertSeverityInd(@MethodParam(MethodParam.OBJECT) SeverityInd item);

	@DELETE
	@Path("/serverityInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����Եȼ�/" + DataSet.DELETE)
	public long deleteSeverityInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/serverityInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����Եȼ�/" + DataSet.UPDATE)
	public long updateSeverityInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/lostInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��ʧ�ȼ�/" + DataSet.LIST })
	public List<LostInd> listLostInd();

	@POST
	@Path("/lostInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ʧ�ȼ�/" + DataSet.INSERT)
	public LostInd insertLostInd(@MethodParam(MethodParam.OBJECT) LostInd item);

	@DELETE
	@Path("/lostInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ʧ�ȼ�/" + DataSet.DELETE)
	public long deleteLostInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/lostInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ʧ�ȼ�/" + DataSet.UPDATE)
	public long updateLostInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/freqInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "Ƶ�ȵȼ�/" + DataSet.LIST })
	public List<FreqInd> listFreqInd();

	@POST
	@Path("/freqInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("Ƶ�ȵȼ�/" + DataSet.INSERT)
	public FreqInd insertFreqInd(@MethodParam(MethodParam.OBJECT) FreqInd item);

	@DELETE
	@Path("/freqInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("Ƶ�ȵȼ�/" + DataSet.DELETE)
	public long deleteFreqInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/freqInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("Ƶ�ȵȼ�/" + DataSet.UPDATE)
	public long updateFreqInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/incidenceInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "Ӱ�췶Χ�ȼ�/" + DataSet.LIST })
	public List<IncidenceInd> listIncidenceInd();

	@POST
	@Path("/incidenceInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("Ӱ�췶Χ�ȼ�/" + DataSet.INSERT)
	public IncidenceInd insertIncidenceInd(@MethodParam(MethodParam.OBJECT) IncidenceInd item);

	@DELETE
	@Path("/incidenceInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("Ӱ�췶Χ�ȼ�/" + DataSet.DELETE)
	public long deleteIncidenceInd(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/incidenceInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("Ӱ�췶Χ�ȼ�/" + DataSet.UPDATE)
	public long updateIncidenceInd(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/classifyProblemLost/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ�����/" + DataSet.LIST })
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
	@DataSet({ "����ɱ�����/" + DataSet.INSERT })
	public ClassifyProblemLost insertClassifyProblemLost(@MethodParam(MethodParam.OBJECT) ClassifyProblemLost ai);

	@DELETE
	@Path("/classifyProblemLost/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ�����/" + DataSet.DELETE })
	public long deleteClassifyProblemLost(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/classifyProblemLost/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ�����/" + DataSet.UPDATE })
	public long updateClassifyProblemLost(BasicDBObject filterAndUpdate);

	@POST
	@Path("/classifyProblem/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�������/" + DataSet.LIST })
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
	@DataSet({ "�������/" + DataSet.INSERT })
	public ClassifyProblem insertClassifyProblem(@MethodParam(MethodParam.OBJECT) ClassifyProblem ai);

	@DELETE
	@Path("/classifyProblem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�������/" + DataSet.DELETE })
	public long deleteClassifyProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/classifyProblem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�������/" + DataSet.UPDATE })
	public long updateClassifyProblem(BasicDBObject filterAndUpdate);

	@POST
	@Path("/classifyCause/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "ԭ�����/" + DataSet.LIST })
	public List<ClassifyCause> rootClassifyCause();

	@POST
	@Path("/classifyCause/root/selector/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "ԭ����ࣨѡ�����ã�/" + DataSet.LIST })
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
	@DataSet({ "ԭ�����/" + DataSet.INSERT })
	public ClassifyCause insertClassifyCause(@MethodParam(MethodParam.OBJECT) ClassifyCause ai);

	@DELETE
	@Path("/classifyCause/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "ԭ�����/" + DataSet.DELETE })
	public long deleteClassifyCause(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/classifyCause/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "ԭ�����/" + DataSet.UPDATE })
	public long updateClassifyCause(BasicDBObject filterAndUpdate);

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����ɱ���
	@DELETE
	@Path("/costItem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8����ɱ���Ŀ���/delete")
	public long deleteCostItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
	@Path("/{_id}/costItem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8����ɱ���Ŀ���/list" })
	public List<ProblemCostItem> listCostItems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@POST
	@Path("/{_id}/costItem/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8����ɱ���Ŀ���/count" })
	public long countCostItems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@PUT
	@Path("/costItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "D8����ɱ���Ŀ���/update" })
	public long updateCostItems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);

	@POST
	@Path("/{_id}/costItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8����ɱ���Ŀ���/insert")
	public ProblemCostItem insertCostItem(@MethodParam(MethodParam.OBJECT) ProblemCostItem p,
			@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@POST
	@Path("/{_id}/chart/periodCost")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8����ɱ��ڼ�������/list")
	public Document periodCostChart(@PathParam("_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ͨ���ж�
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
	@DataSet({ "D0������Ӧ�ж�/list", "D0������Ӧ�ж����/list", //
			"D3��ʱ�����ж�/list", "D3��ʱ�����ж����/list", //
			"D6ִ�к�ȷ��/list", "D6ִ�к�ȷ�����þ�����ʩ���/list", //
			"D7ϵͳ��Ԥ����ʩ���/list",  //
			"D8��ʧ��ش�ʩ���/list" })
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
	// �ж�Ԥ��
	@POST
	@Path("/_id/{_id}/{stage}/advisableplan/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�ж�Ԥ��ѡ���б�/list")
	public List<Document> listAdvisablePlan(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage
			);

	@POST
	@Path("/_id/{_id}/{stage}/advisableplan/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�ж�Ԥ��ѡ���б�/count")
	public long countAdvisablePlan(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("problem_id") @PathParam("_id") ObjectId problem_id,
			@MethodParam("stage") @PathParam("stage") String stage);

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɱ����� ����ɱ����ɱ�������ȡ
	@POST
	@Path("/cost/selector/root/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ����ɱ�������ȡ/list","����ɱ������������ȡ/list","����ɱ�������ԭ�������ȡ/list" })
	public List<Catalog> listClassifyRoot();

	@POST
	@Path("/cost/selector/classifycost/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ����ɱ�������ȡ/slist" })
	public List<Catalog> listClassifyCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/classifycost/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ����ɱ�������ȡ/scount" })
	public long countClassifyCostStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/cost/option/default")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ����ɱ�������ȡ/default","����ɱ������������ȡ/default","����ɱ������β��ŷ�����ȡ/default","����ɱ�������ԭ�������ȡ/default" })
	public Document defaultClassifyCostOption();
	
	@POST
	@Path("/cost/classifycost/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ����ɱ�������ȡ/chart" })
	public Document createClassifyCostChart(@MethodParam(MethodParam.CONDITION) Document condition);

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɱ����� ����ɱ������������ȡ

	@POST
	@Path("/cost/selector/classifyproblem/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������������ȡ/slist" })
	public List<Catalog> listClassifyProblemStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/classifyproblem/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������������ȡ/scount" })
	public long countClassifyProblemStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/cost/classifyproblem/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������������ȡ/chart" })
	public Document createClassifyProblemChart(@MethodParam(MethodParam.CONDITION) Document condition);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɱ����� ����ɱ������β��ŷ�����ȡ
	
	@POST
	@Path("/cost/selector/root/org")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������β��ŷ�����ȡ/list" })
	public List<Catalog> listOrgRoot();

	@POST
	@Path("/cost/selector/dept/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������β��ŷ�����ȡ/slist" })
	public List<Catalog> listOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/dept/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������β��ŷ�����ȡ/scount" })
	public long countOrgStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/res/dept/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ������β��ŷ�����ȡ/chart" })
	public Document createClassifyDeptChart(@MethodParam(MethodParam.CONDITION) Document condition);
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ɱ����� ����ɱ���ԭ�������ȡ

	@POST
	@Path("/cost/selector/cause/structure/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ�������ԭ�������ȡ/slist" })
	public List<Catalog> listClassifyCauseStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);

	@POST
	@Path("/cost/selector/cause/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ�������ԭ�������ȡ/scount" })
	public long countClassifyCauseStructure(@MethodParam(MethodParam.OBJECT) Catalog parent);
	
	@POST
	@Path("/res/cause/chart/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "����ɱ�������ԭ�������ȡ/chart" })
	public Document createClassifyCauseChart(@MethodParam(MethodParam.CONDITION) Document condition);
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �����ۺϷ��� 
	@POST
	@Path("/cost/classifyproblem/bar")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "������ʧ���������ͼ��/list" })
	public Document createCostClassifyByProblemChart();
	
	@POST
	@Path("/count/classifyproblem/bar")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����������������ͼ��/list" })
	public Document createCountClassifyByProblemChart();
	
	@POST
	@Path("/cost/classifyCause/pie")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "������ʧ��ԭ�����ͼ��/list" })
	public Document createCostClassifyByCauseChart();
	
	@POST
	@Path("/count/classifyCause/pie")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "������ʧ�����ŷ���ͼ��/list" })
	public Document createCostClassifyByDeptChart();

	@POST
	@Path("/spa/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ϵͳ��Ԥ����ʩ���/list")
	public List<Document> listSPA(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/spa/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ϵͳ��Ԥ����ʩ���/count")
	public long countSPA(@MethodParam(MethodParam.FILTER) BasicDBObject filter);


}
