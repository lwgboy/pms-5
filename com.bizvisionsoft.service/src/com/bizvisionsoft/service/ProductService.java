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
import com.bizvisionsoft.service.model.Product;
import com.bizvisionsoft.service.model.ProductBenchmark;
import com.mongodb.BasicDBObject;

@Path("/product")
public interface ProductService {

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ĿĿ���Ʒ/" + DataSet.INSERT)
	public Product insert(@MethodParam(MethodParam.OBJECT) Product product);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ĿĿ���Ʒ/" + DataSet.UPDATE)
	public long update(BasicDBObject filterAndUpdate);

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ĿĿ���Ʒ/" + DataSet.INPUT)
	public Product get(@PathParam("_id") @MethodParam("_id") ObjectId _id);

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Ʒѡ���б�/count")
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Ʒѡ���б�/list")
	public List<Product> listProduct(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/project_id/{project_id}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ĿĿ���Ʒ/" + DataSet.LIST)
	public List<Product> listProjectProduct(
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/series/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Ʒϵ��ѡ��/list")
	public List<String> listProductSeries();

	@DELETE
	@Path("/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ĿĿ���Ʒ/" + DataSet.DELETE)
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId id);

	@POST
	@Path("/project_id/{project_id}/skubenchmarking/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ƷSKU�Ա��������/list")
	public List<ProductBenchmark> projectProductBenchmarking(
			@PathParam("project_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId project_id);

	@POST
	@Path("/product_id/{product_id}/benchmarking/income/chart")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��һ��Ʒ�Ա����-���۶�/list")
	public Document productIncomeBenchMarkingChartData(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("product_id") ObjectId product_id);
	@POST
	@Path("/product_id/{product_id}/benchmarking/volumn/chart")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��һ��Ʒ�Ա����-������/list")
	public Document productVolumnBenchMarkingChartData(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("product_id") ObjectId product_id);

}
