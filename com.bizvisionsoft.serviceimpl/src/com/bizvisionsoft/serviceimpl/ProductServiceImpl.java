package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProductService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Product;
import com.bizvisionsoft.service.model.ProductBenchmark;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;

public class ProductServiceImpl extends BasicServiceImpl implements ProductService {

	@Override
	public Product insert(Product product) {
		return insert(product, Product.class);
	}

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, Product.class);
	}

	@Override
	public Product get(ObjectId _id) {
		return get(_id, Product.class);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, Product.class);
	}

	@Override
	public List<Product> listProduct(BasicDBObject condition) {
		return createDataSet(condition, Product.class);
	}

	@Override
	public List<Product> listProjectProduct(ObjectId project_id) {
		BasicDBObject condition = new Query().filter(new BasicDBObject("project_id", project_id)).bson();
		return listProduct(condition);
	}

	@Override
	public long delete(ObjectId _id) {
		return delete(_id, Product.class);
	}

	@Override
	public List<String> listProductSeries() {
		return Arrays.asList("超级飞侠", "巴啦啦小魔仙", "爆裂飞车", "火力少年王", "巨神战击队", "铠甲勇士", "飓风战魂");
	}

	@Override
	public List<ProductBenchmark> projectProductBenchmarking(ObjectId project_id) {
		List<Bson> pipeline = new JQ("SKU销售对标").set("project_id", project_id).array();
		return c("product").aggregate(pipeline, ProductBenchmark.class).into(new ArrayList<>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document productIncomeBenchMarkingChartData(ObjectId product_id) {
		Document data = c("product").aggregate(new JQ("单一产品对比分析取数").set("product_id", product_id).array()).first();
		String prodname = data.getString("name");
		String bmname = data.getString("bm_name");

		List<Document> salesItem = (List<Document>) data.get("salesItem");
		List<Document> bm_salesItem = (List<Document>) data.get("bm_salesItem");
		List<Document> seriesSalesItem = (List<Document>) data.get("seriesSalesItem");
		int count = Math.max(salesItem.size(), bm_salesItem.size());
		List<String> month = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			month.add("" + (i + 1));
		}
		ArrayList<Double> product1 = new ArrayList<Double>();
		ArrayList<Double> bm1 = new ArrayList<Double>();
		ArrayList<Double> product2 = new ArrayList<Double>();
		ArrayList<Double> bm2 = new ArrayList<Double>();

		for (int i = 0; i < count; i++) {
			if (salesItem.size() > i) {
				Double d = salesItem.get(i).getDouble("income");
				product1.add((double) Math.round(d * 10) / 10);
				d = ((Number) salesItem.get(i).get("volumn")).doubleValue();
				product2.add((double) Math.round(d * 10) / 10);
			} else {
				product1.add(0d);
				product2.add(0d);
			}
		}

		for (int i = 0; i < count; i++) {
			if (bm_salesItem.size() > i) {
				Double d = bm_salesItem.get(i).getDouble("income");
				bm1.add((double) Math.round(d * 10) / 10);
				d = ((Number) bm_salesItem.get(i).get("volumn")).doubleValue();
				bm2.add((double) Math.round(d * 10) / 10);
			} else {
				bm1.add(0d);
				bm2.add(0d);
			}
		}

		return new JQ("单一产品对标分析图表").set("title", "销售额逐月对比").set("y", "销售额").set("month", month).set("product", product1)
				.set("bm", bm1).set("prodname", prodname).set("bmname", bmname).doc();
	}

	@Override
	public Document productVolumnBenchMarkingChartData(ObjectId product_id) {
		Document data = c("product").aggregate(new JQ("单一产品对比分析取数").set("product_id", product_id).array()).first();
		String prodname = data.getString("name");
		String bmname = data.getString("bm_name");

		List<Document> salesItem = (List<Document>) data.get("salesItem");
		List<Document> bm_salesItem = (List<Document>) data.get("bm_salesItem");
		List<Document> seriesSalesItem = (List<Document>) data.get("seriesSalesItem");
		int count = Math.max(salesItem.size(), bm_salesItem.size());
		List<String> month = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			month.add("" + (i + 1));
		}
		ArrayList<Double> product1 = new ArrayList<Double>();
		ArrayList<Double> bm1 = new ArrayList<Double>();
		ArrayList<Double> product2 = new ArrayList<Double>();
		ArrayList<Double> bm2 = new ArrayList<Double>();

		for (int i = 0; i < count; i++) {
			if (salesItem.size() > i) {
				Double d = salesItem.get(i).getDouble("income");
				product1.add((double) Math.round(d * 10) / 10);
				d = ((Number) salesItem.get(i).get("volumn")).doubleValue();
				product2.add((double) Math.round(d * 10) / 10);
			} else {
				product1.add(0d);
				product2.add(0d);
			}
		}

		for (int i = 0; i < count; i++) {
			if (bm_salesItem.size() > i) {
				Double d = bm_salesItem.get(i).getDouble("income");
				bm1.add((double) Math.round(d * 10) / 10);
				d = ((Number) bm_salesItem.get(i).get("volumn")).doubleValue();
				bm2.add((double) Math.round(d * 10) / 10);
			} else {
				bm1.add(0d);
				bm2.add(0d);
			}
		}

		return new JQ("单一产品对标分析图表").set("title", "销售量逐月对比").set("y", "销售量").set("month", month).set("product", product2)
				.set("bm", bm2).set("prodname", prodname).set("bmname", bmname).doc();
	}

}
