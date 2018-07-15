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
	@DataSet("项目目标产品/" + DataSet.INSERT)
	public Product insert(@MethodParam(MethodParam.OBJECT) Product product);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目目标产品/" + DataSet.UPDATE)
	public long update(BasicDBObject filterAndUpdate);

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目目标产品/" + DataSet.INPUT)
	public Product get(@PathParam("_id") @MethodParam("_id") ObjectId _id);

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("产品选择列表/count")
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("产品选择列表/list")
	public List<Product> listProduct(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/project_id/{project_id}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目目标产品/" + DataSet.LIST)
	public List<Product> listProjectProduct(
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/series/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("产品系列选择/list")
	public List<String> listProductSeries();

	@DELETE
	@Path("/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目目标产品/" + DataSet.DELETE)
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId id);

	@POST
	@Path("/project_id/{project_id}/skubenchmarking/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("产品SKU对标分析评估/list")
	public List<ProductBenchmark> projectProductBenchmarking(
			@PathParam("project_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId project_id);

	@POST
	@Path("/product_id/{product_id}/benchmarking/income/chart")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("单一产品对标分析-销售额/list")
	public Document productIncomeBenchMarkingChartData(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("product_id") ObjectId product_id);
	@POST
	@Path("/product_id/{product_id}/benchmarking/volumn/chart")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("单一产品对标分析-销售量/list")
	public Document productVolumnBenchMarkingChartData(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("product_id") ObjectId product_id);

}
