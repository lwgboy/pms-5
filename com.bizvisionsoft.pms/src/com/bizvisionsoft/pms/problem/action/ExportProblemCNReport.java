package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.tools.WidgetHandler;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.ReportService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class ExportProblemCNReport {

	public Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private IBruiService br;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Problem problem = context.search_sele_root(Problem.class);

		if (problem == null)
			return;

		String _id = problem.get_id().toString();
		Document rptParam = new Document()
				//
				.append("pipeline-problem", "[{ '$match' : { '_id' : { '$oid' : '" + _id + "'}}}, "
						+ "{ '$lookup' : {'from' : 'd2ProblemDesc', 'localField' : '_id', 'foreignField' : '_id', 'as' : 'd2ProblemDesc'}}, "
						+ "{ '$unwind' : {'path' : '$d2ProblemDesc', 'preserveNullAndEmptyArrays' : true}}, "
						+ "{ '$lookup' : {'from' : 'd4RootCauseDesc', 'localField' : '_id', 'foreignField' : '_id', 'as' : 'd4RootCauseDesc'}}, "
						+ "{ '$unwind' : {'path' : '$d4RootCauseDesc', 'preserveNullAndEmptyArrays' : true}}, "
						+ "{ '$lookup' : {'from' : 'organization', 'localField' : 'dept_id', 'foreignField' : '_id', 'as' : 'org'}}, "
						+ "{ '$unwind' : {'path' : '$org', 'preserveNullAndEmptyArrays' : true}}, "
						+ "{ '$lookup' : {'from' : 'd5PCA', 'let' : {'problem_id' : '$_id'}, 'pipeline' : [{'$match' : {'$expr' : {'$and' : [{'$eq' : ['$$problem_id', '$problem_id']}, '$selected']}}}], 'as' : 'd5PCA'}}, "
						+ "{ '$unwind' : {'path' : '$d5PCA', 'preserveNullAndEmptyArrays' : true}}, "
						+ "{ '$addFields' : {'how' : '$d2ProblemDesc.how', 'howmany' : '$d2ProblemDesc.howmany', 'what' : '$d2ProblemDesc.what', 'why' : '$d2ProblemDesc.why', 'when' : '$d2ProblemDesc.when', 'where' : '$d2ProblemDesc.where', 'who' : '$d2ProblemDesc.who','rootCauseDesc' : '$d4RootCauseDesc.rootCauseDesc', 'escapePoint' : '$d4RootCauseDesc.escapePoint', 'evidenceData' : '$d4RootCauseDesc.evidenceData','deptName' : '$org.name','icaConfirmedOn':'$icaConfirmed.date','icaConfirmedBy':'$icaConfirmed.userName','pcaApprovedOn':'$pcaApproved.date','pcaApprovedBy':'$pcaApproved.userName','icaConfirmedOn':'$icaConfirmed.date','pcaValidatedBy':'$pcaValidated.userName','pcaValidatedOn':'$pcaValidated.date','pcaConfirmedBy':'$pcaConfirmed.userName','pcaConfirmedOn':'$pcaConfirmed.date','closeDate' : '$closeInfo.date','closeBy' : '$closeInfo.userName','severityIndText': {'$concat':[{'$toString':'$severityInd.index'},'.','$severityInd.value','(','$severityInd.desc',')']},'freqIndText': {'$concat':[{'$toString':'$freqInd.index'},'.','$freqInd.value','(','$freqInd.text',')']},'detectionIndText': {'$concat':[{'$toString':'$detectionInd.index'},'.','$detectionInd.value','(','$detectionInd.desc',')']},'lostIndText': {'$concat':[{'$toString':'$lostInd.index'},'.','$lostInd.value','(','$lostInd.desc',')']}, 'incidenceIndText': {'$concat':[{'$toString':'$incidenceInd.index'},'.','$incidenceInd.value','(','$incidenceInd.desc',')']},'d5pcaCharger1name' : '$d5PCA.charger1_meta.name', 'd5pcaPlanStart1' : '$d5PCA.planStart1', 'd5pcaPlanFinish1' : '$d5PCA.planFinish1', 'd5pcaCharger2name' : '$d5PCA.charger2_meta.name', 'd5pcaPlanStart2' : '$d5PCA.planStart2', 'd5pcaPlanFinish2' : '$d5PCA.planFinish2', 'd5pca1' : '$d5PCA.pca1', 'd5pca2' : '$d5PCA.pca2','d5pca1' : '$d5PCA.pca1.name', 'd5pca2' : '$d5PCA.pca2.name','idrrept': {'$map':{'input':'$idrrept', 'as' :'idrrept', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$idrrept._id'},'&namespace=','$$idrrept.namepace','&name=','$$idrrept.name','&sid=rwt\">','$$idrrept.name','</a>']}}},'attarchments' : {'$map':{'input':'$attarchments', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}}}}, "
						+ "{ '$project' : {'d2ProblemDesc' : false, 'd4RootCauseDesc' : false,'org' : false,'d5PCA' : false}}]")
				//
				.append("pipeline-d1cft",
						"[ { '$match' :{ 'problem_id' : { '$oid' : '" + _id + "'}} }, "
								+ "{ '$sort' : { 'role' : 1 } }]")
				//
				.append("pipeline-d2ProblemPhoto", "[{ '$match' : { 'problem_id' : {'$oid':'" + _id + "'}}},"
						+ "{ '$unwind' : {'path' : '$problemImg', 'preserveNullAndEmptyArrays' : true}}, "
						+ "{ '$addFields' : { 'img' : { '$concat' : [\"<img alt='' src='/bvs/fs?id=\" , { '$toString' : '$problemImg._id'},"
						+ "'&namespace=','$problemImg.namepace','&name=','$problemImg.name',\"&sid=rwt' style='width:200px' />\"]}}}]")
				//
				.append("pipeline-d7Similar", "[{ '$match' : {'problem_id':{'$oid':'" + _id
						+ "'}}},{ '$unwind' : { 'path' : '$id', 'preserveNullAndEmptyArrays' : true } }, "
						+ "{ '$addFields' : { 'ids' : { '$concat' : [ '$id.id', ' ', '$id.keyword' ] } } }, "
						+ "{ '$group' : { '_id' : '$_id', 'problem_id' : { '$first' : '$problem_id' }, 'similar' : { '$first' : '$similar' }, "
						+ "'desc' : { '$first' : '$desc' }, 'degree' : { '$first' : '$degree' }, 'prob' : { '$first' : '$prob' }, "
						+ "'id' : { '$addToSet' : '$ids' } } }]")
				//
				.append("pipeline-ai-era", "[{$match:{'stage':'era','problem_id':{'$oid':'" + _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {'$cond':['$finish','是','否']},'verificationAttachment': { '$concatArrays': [ {'$ifNull':[{ '$map':{ 'input':'$verification.attachment', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}, {'$ifNull':[{ '$map':{ 'input':'$attachments', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}]}}}]")
				//
				.append("pipeline-ai-ica", "[{$match:{'stage':'ica','problem_id':{'$oid':'" + _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {'$cond':['$finish','是','否']},'verificationAttachment': { '$concatArrays': [ {'$ifNull':[{ '$map':{ 'input':'$verification.attachment', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}, {'$ifNull':[{ '$map':{ 'input':'$attachments', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}]}}}]")
				//
				.append("pipeline-ai-pca-make", "[{$match:{'stage':'pca', 'actionType':'make', 'problem_id':{'$oid':'"
						+ _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {'$cond':['$finish','是','否']},'verificationAttachment': { '$concatArrays': [ {'$ifNull':[{ '$map':{ 'input':'$verification.attachment', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}, {'$ifNull':[{ '$map':{ 'input':'$attachments', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}]}}}]")
				//
				.append("pipeline-ai-pca-out", "[{$match:{'stage':'pca', 'actionType':'out','problem_id':{'$oid':'"
						+ _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {'$cond':['$finish','是','否']},'verificationAttachment': { '$concatArrays': [ {'$ifNull':[{ '$map':{ 'input':'$verification.attachment', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}, {'$ifNull':[{ '$map':{ 'input':'$attachments', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}]}}}]")
				//
				.append("pipeline-ai-spa", "[{$match:{'stage':'spa','problem_id':{'$oid':'" + _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {'$cond':['$finish','是','否']},'verificationAttachment': { '$concatArrays': [ {'$ifNull':[{ '$map':{ 'input':'$verification.attachment', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}, {'$ifNull':[{ '$map':{ 'input':'$attachments', 'as' :'attarchments', 'in':{ '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$$attarchments._id'},'&namespace=','$$attarchments.namepace','&name=','$$attarchments.name','&sid=rwt\">','$$attarchments.name','</a>']}}},[]]}]}}}]")
				//
				.append("pipeline-causeRelation-make", "[{ '$match' : {'type' : '因果分析-制造', 'problem_id' : {'$oid':'"
						+ _id
						+ "'}}},{'$sort':{'subject':1,'classifyCause.id':1,'_id':1}},{ '$addFields' : { 'classifyCausePath' : '$classifyCause.path' } }]")
				//
				.append("pipeline-causeRelation-out", "[{ '$match' : {'type' : '因果分析-流出', 'problem_id' : {'$oid':'"
						+ _id
						+ "'}}},{'$sort':{'subject':1,'classifyCause.id':1,'_id':1}},{ '$addFields' : { 'classifyCausePath' : '$classifyCause.path' } }]")
		//
		;

		JsonObject jo = new JsonObject().set("rptParam", rptParam.toJson()).set("template", "8DReport_cn.rptdesign")
				.set("outputType", "excel").set("fileName", "问题报告").set("domain", br.getDomain());
		UserSession.bruiToolkit().downloadServerFile("report", jo);
//		UserSession.bruiToolkit().transportServerFile("report", "问题报告.xlsx", jo);
	}
}
