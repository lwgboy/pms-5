package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.service.model.Problem;

public class ExportProblemENReport {

	public Logger logger = LoggerFactory.getLogger(getClass());

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Problem problem = context.search_sele_root(Problem.class);

		if (problem == null)
			// TODO ��ʾ
			return;

		String _id = problem.get_id().toString();
		// TODO ʹ��JQ��ȡ
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
						+ "{ '$addFields' : {'how' : '$d2ProblemDesc.how', 'howmany' : '$d2ProblemDesc.howmany', 'what' : '$d2ProblemDesc.what', 'why' : '$d2ProblemDesc.why', 'when' : '$d2ProblemDesc.when', 'where' : '$d2ProblemDesc.where', 'who' : '$d2ProblemDesc.who','rootCauseDesc' : '$d4RootCauseDesc.rootCauseDesc', 'escapePoint' : '$d4RootCauseDesc.escapePoint', 'evidenceData' : '$d4RootCauseDesc.evidenceData','deptName' : '$org.name','icaConfirmedOn':'$icaConfirmed.date','icaConfirmedBy':'$icaConfirmed.userName','pcaApprovedOn':'$pcaApproved.date','pcaApprovedBy':'$pcaApproved.userName','icaConfirmedOn':'$icaConfirmed.date','pcaValidatedBy':'$pcaValidated.userName','pcaValidatedOn':'$pcaValidated.date','pcaConfirmedBy':'$pcaConfirmed.userName','pcaConfirmedOn':'$pcaConfirmed.date','closeDate' : '$closeInfo.date','closeBy' : '$closeInfo.userName','severityIndText': {'$concat':[{'$toString':'$severityInd.index'},'.','$severityInd.value','(','$severityInd.desc',')']},'freqIndText': {'$concat':[{'$toString':'$freqInd.index'},'.','$freqInd.value','(','$freqInd.text',')']},'detectionIndText': {'$concat':[{'$toString':'$detectionInd.index'},'.','$detectionInd.value','(','$detectionInd.desc',')']},'lostIndText': {'$concat':[{'$toString':'$lostInd.index'},'.','$lostInd.value','(','$lostInd.desc',')']}, 'incidenceIndText': {'$concat':[{'$toString':'$incidenceInd.index'},'.','$incidenceInd.value','(','$incidenceInd.desc',')']},'d5pcaCharger1name' : '$d5PCA.charger1_meta.name', 'd5pcaPlanStart1' : '$d5PCA.planStart1', 'd5pcaPlanFinish1' : '$d5PCA.planFinish1', 'd5pcaCharger2name' : '$d5PCA.charger2_meta.name', 'd5pcaPlanStart2' : '$d5PCA.planStart2', 'd5pcaPlanFinish2' : '$d5PCA.planFinish2'}}, "
						+ "{ '$project' : {'d2ProblemDesc' : false, 'd4RootCauseDesc' : false,'org' : false,'d5PCA' : false,}}]")
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
				.append("pipeline-d5PCA", "[ { '$match' :{'selected':true,'problem_id':{'$oid':'" + _id + "'}} }]")
				//
				.append("pipeline-d5PCA1",
						"[ { '$match' :{'selected':true,'problem_id':{'$oid':'" + _id + "'}} }, "
								+ "{ '$unwind' : {'path' : '$pca1', 'preserveNullAndEmptyArrays' : true}}]")
				//
				.append("pipeline-d5PCA2",
						"[ { '$match' :{'selected':true,'problem_id':{'$oid':'" + _id + "'}} }, "
								+ "{ '$unwind' : {'path' : '$pca2', 'preserveNullAndEmptyArrays' : true}}]")
				//
				.append("pipeline-d7Similar", "[{ '$match' : {'problem_id':{'$oid':'" + _id + "'}}}]")
				//
				.append("pipeline-ai-era", "[{$match:{'stage':'era','problem_id':{'$oid':'" + _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {$cond:['$finish','��','��']}}}]")
				//
				.append("pipeline-ai-ica", "[{$match:{'stage':'ica','problem_id':{'$oid':'" + _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {$cond:['$finish','��','��']}}}]")
				//
				.append("pipeline-ai-pca-make", "[{$match:{'stage':'pca', 'actionType':'make', 'problem_id':{'$oid':'"
						+ _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {$cond:['$finish','��','��']}}}]")
				//
				.append("pipeline-ai-pca-out", "[{$match:{'stage':'pca', 'actionType':'out','problem_id':{'$oid':'"
						+ _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {$cond:['$finish','��','��']}}}]")
				//
				.append("pipeline-ai-spa", "[{$match:{'stage':'spa','problem_id':{'$oid':'" + _id + "'}}},"
						+ "{ '$addFields' : {'title' : '$verification.title', 'comment' : '$verification.comment', 'verificationuser' : '$verification.user_meta.name', 'verificationdate' : '$verification.date', 'chargername' : '$charger_meta.name','finish': {$cond:['$finish','��','��']}}}]")
				//
				.append("pipeline-causeRelation-make",
						"[{ '$match' : {'type' : '�������-����', 'problem_id' : {'$oid':'" + _id
								+ "'}}},{'$sort':{'subject':1,'classifyCause.id':1,'_id':1}}]")
				//
				.append("pipeline-causeRelation-out",
						"[{ '$match' : {'type' : '�������-����', 'problem_id' : {'$oid':'" + _id
								+ "'}}},{'$sort':{'subject':1,'classifyCause.id':1,'_id':1}}]")
				//
				.append("pipeline-problem-attachment", "[{ '$match' : {'_id':{'$oid':'" + _id
						+ "'}}}, { '$unwind' : '$attarchments'}, "
						+ "{ '$addFields' : {'attarchment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$attarchments._id'},'&namespace=','$attarchments.namepace','&name=','$attarchments.name','&sid=rwt\">','$attarchments.name','</a>']}}}]")
				//
				.append("pipeline-problem-idrrept", "[{ '$match' : {'_id':{'$oid':'" + _id
						+ "'}}}, { '$unwind' :'$idrrept'}, "
						+ "{ '$addFields' : {'attarchment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$idrrept._id'},'&namespace=','$idrrept.namepace','&name=','$idrrept.name','&sid=rwt\">','$idrrept.name','</a>']}}}]")
				//
				.append("pipeline-ai-era-attachment", "[{$match:{'stage':'era','problem_id':{'$oid':'" + _id + "'}}}, "
						+ "{ '$unwind' : '$verification'}, { '$addFields' : {'verification' : '$verification.attachment'}}, "
						+ "{ '$unwind' : '$verification'}, { '$addFields' : {'attachment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$verification._id'},'&namespace=','$verification.namepace','&name=','$verification.name','&sid=rwt\">','$verification.name','</a>']}}}]")
				//
				.append("pipeline-ai-ica-attachment", "[{$match:{'stage':'ica','problem_id':{'$oid':'" + _id + "'}}}, "
						+ "{ '$unwind' : '$verification'}, { '$addFields' : {'verification' : '$verification.attachment'}}, "
						+ "{ '$unwind' : '$verification'}, { '$addFields' : {'attachment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$verification._id'},'&namespace=','$verification.namepace','&name=','$verification.name','&sid=rwt\">','$verification.name','</a>']}}}]")
				//
				.append("pipeline-ai-pca-make-attachment",
						"[{$match:{'stage':'pca','actionType':'make','problem_id':{'$oid':'" + _id + "'}}}, "
								+ "{ '$unwind' : '$verification'}, { '$addFields' : {'verification' : '$verification.attachment'}}, "
								+ "{ '$unwind' : '$verification'}, { '$addFields' : {'attachment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$verification._id'},'&namespace=','$verification.namepace','&name=','$verification.name','&sid=rwt\">','$verification.name','</a>']}}}]")
				//
				.append("pipeline-ai-pca-out-attachment",
						"[{$match:{'stage':'pca','actionType':'out','problem_id':{'$oid':'" + _id + "'}}}, "
								+ "{ '$unwind' : '$verification'}, { '$addFields' : {'verification' : '$verification.attachment'}}, "
								+ "{ '$unwind' : '$verification'}, { '$addFields' : {'attachment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$verification._id'},'&namespace=','$verification.namepace','&name=','$verification.name','&sid=rwt\">','$verification.name','</a>']}}}]")
				//
				.append("pipeline-ai-spa-attachment", "[{$match:{'stage':'spa','problem_id':{'$oid':'" + _id + "'}}}, "
						+ "{ '$unwind' : '$verification'}, { '$addFields' : {'verification' : '$verification.attachment'}}, "
						+ "{ '$unwind' : '$verification'}, { '$addFields' : {'attachment' : { '$concat' :['<a href=\"/bvs/fs?id=',{ '$toString' : '$verification._id'},'&namespace=','$verification.namepace','&name=','$verification.name','&sid=rwt\">','$verification.name','</a>']}}}]")
		//
		;

		JsonObject jo = new JsonObject().set("rptParam", rptParam.toJson()).set("template", "8DReport_en.rptdesign")
				.set("fileName", "ProblemReport").set("serverPath",
						"http://" + RWT.getRequest().getServerName() + ":" + RWT.getRequest().getServerPort());
		UserSession.bruiToolkit().downloadServerFile("topsreport", jo);
	}
}