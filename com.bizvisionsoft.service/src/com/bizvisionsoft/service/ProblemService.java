package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
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
import com.bizvisionsoft.service.model.Problem;
import com.mongodb.BasicDBObject;

@Path("/problem")
public interface ProblemService {

	@POST
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/insert")
	public Problem insertProblem(@MethodParam(MethodParam.OBJECT) Problem p);

	@POST
	@Path("/item/{status}/{userid}/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/list")
	public List<Problem> listProblems(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/item/{status}/{userid}/count/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/count")
	public long countProblems(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam("status") @PathParam("status") String status,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

	@POST
	@Path("/item/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/delete")
	public long deleteProblem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("问题清单/update")
	public long updateProblems(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu);

	@POST
	@Path("/_id/{_id}/d1/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D1多功能小组/list")
	public List<Document> listD1(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d1/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D1多功能小组/count")
//	public long countD1(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);
//
	@POST
	@Path("/d1/item/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD1Item(Document d1, @PathParam("lang") String lang);

	@POST
	@Path("/_id/{_id}/d2/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D2问题描述/list")
	public List<Document> listD2(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d2/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D2问题描述/count")
//	public long countD2(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);

	@GET
	@Path("/_id/{_id}/d2/desc")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getD2ProblemDesc(@PathParam("_id") ObjectId problem_id);
	
	@POST
	@Path("/d2/pd/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document updateD2ProblemDesc(Document d, @MethodParam(MethodParam.LANG) @PathParam("lang") String lang);
	
	@POST
	@Path("/d2/photo/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertD2ProblemPhoto(Document t, @PathParam("lang") String lang);
	
	@POST
	@Path("/_id/{_id}/d3/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D3临时措施/list")
	public List<Document> listD3(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d3/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D3临时措施/count")
//	public long countD3(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);
//
	@POST
	@Path("/_id/{_id}/d4/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D4根本原因分析/list")
	public List<Document> listD4(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d4/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D4根本原因分析/count")
//	public long countD4(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);
//
	@POST
	@Path("/_id/{_id}/d5/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D5永久措施/list")
	public List<Document> listD5(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d5/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D5永久措施/count")
//	public long countD5(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);
//
	@POST
	@Path("/_id/{_id}/d6/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D6执行和确认/list")
	public List<Document> listD6(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d6/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D6执行和确认/count")
//	public long countD6(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);
//
	@POST
	@Path("/_id/{_id}/d7/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D7系统预防/list")
	public List<Document> listD7(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d7/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D7系统预防/count")
//	public long countD7(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);
//
	@POST
	@Path("/_id/{_id}/d8/ds/{lang}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("D8关闭结案/list")
	public List<Document> listD8(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id,
			@MethodParam(MethodParam.LANG) @PathParam("lang") String lang);

//	@POST
//	@Path("/_id/{_id}/d8/count/")
//	@Consumes("application/json; charset=UTF-8")
//	@Produces("application/json; charset=UTF-8")
//	@DataSet("D8关闭结案/count")
//	public long countD8(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
//			@PathParam("_id") @MethodParam(MethodParam.PAGE_CONTEXT_INPUT_OBJECT_ID) ObjectId problem_id);


}
