package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProductService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Product;
import com.bizvisionsoft.service.model.ProductBenchmark;
import com.mongodb.BasicDBObject;

public class ProductServiceImpl extends BasicServiceImpl implements ProductService {

	@Override
	public Product insert(Product product, String domain) {
		return insert(product, Product.class, domain);
	}

	@Override
	public long update(BasicDBObject fu, String domain) {
		return update(fu, Product.class, domain);
	}

	@Override
	public Product get(ObjectId _id, String domain) {
		return get(_id, Product.class, domain);
	}

	@Override
	public long count(BasicDBObject filter, String domain) {
		return count(filter, Product.class, domain);
	}

	@Override
	public List<Product> listProduct(BasicDBObject condition, String domain) {
		return createDataSet(condition, Product.class, domain);
	}

	@Override
	public List<Product> listProjectProduct(ObjectId project_id, String domain) {
		BasicDBObject condition = new Query().filter(new BasicDBObject("project_id", project_id)).bson();
		return listProduct(condition, domain);
	}

	@Override
	public long delete(ObjectId _id, String domain) {
		return delete(_id, Product.class, domain);
	}

	@Override
	public List<String> listProductSeries(String domain) {
		return Arrays.asList();
	}

	@Override
	public List<ProductBenchmark> projectProductBenchmarking(ObjectId project_id, String domain) {
		List<Bson> pipeline = Domain.getJQ(domain, "ͼ��-����-��Ʒ�Ա�").set("project_id", project_id).array();
		return c("product", domain).aggregate(pipeline, ProductBenchmark.class).into(new ArrayList<>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document productIncomeBenchMarkingChartData(ObjectId product_id,String domain){
//	 <��Ʒ���۶�ռ��> <�Ա��Ʒ���۶�ռ��> <ϵ��������Ʒ���۶�ռ��>

		Document data = c("product",domain).aggregate(Domain.getJQ(domain, "��ѯ-����-��Ʒ�Ա�").set("product_id", product_id).array()).first();
		JQ jq = Domain.getJQ(domain, "ͼ��-����-��Ʒ�Ա�");

		jq.set("ϵ��", data.getString("series")+"ϵ��");
		jq.set("��Ʒ", data.getString("name"));
		jq.set("�Ա��Ʒ", data.getString("bm_name"));
		jq.set("��Ʒ���۶�ռ��", data.get("income"));
		jq.set("�Ա��Ʒ���۶�ռ��", data.get("bm_income"));
		double v = data.getDouble("series_income")-data.getDouble("income")-data.getDouble("bm_income");
		jq.set("ϵ��������Ʒ���۶�ռ��", (double) Math.round(v * 10) / 10);
		

		List<Document> salesItem = (List<Document>) data.get("salesItem");
		List<Document> bm_salesItem = (List<Document>) data.get("bm_salesItem");
		List<Document> seriesSalesItem = (List<Document>) data.get("seriesSalesItem");

		int count = Math.max(salesItem.size(), bm_salesItem.size());
		List<String> month = new ArrayList<>();
		for (int i = 0; i < count; i++){
			month.add("" + (i + 1));
		}
		jq.set("�·�", month);

		ArrayList<Double> income = new ArrayList<Double>();
		ArrayList<Double> bm_income = new ArrayList<Double>();

		ArrayList<Double> profit = new ArrayList<Double>();
		ArrayList<Double> bm_profit = new ArrayList<Double>();

		ArrayList<Double> volumn = new ArrayList<Double>();
		ArrayList<Double> bm_volumn = new ArrayList<Double>();

		ArrayList<Double> growth = new ArrayList<Double>();
		ArrayList<Double> bm_growth = new ArrayList<Double>();
		ArrayList<Double> s_growth = new ArrayList<Double>();

		for (int i = 0; i < count; i++){
			if (salesItem.size() > i){
				Document itm = salesItem.get(i);

				Double _income = ((Number) itm.get("income")).doubleValue();
				income.add((double) Math.round(_income * 10) / 10);

				Double _volumn = ((Number) itm.get("volumn")).doubleValue();
				volumn.add((double) Math.round(_volumn));

				Double _profit = ((Number) itm.get("profit")).doubleValue();
				profit.add((double) Math.round(_profit * 10) / 10);

				if (i == 0){
					growth.add(0d);
				} else {
					double pre = ((Number) salesItem.get(i - 1).get("income")).doubleValue();
					if (pre == 0){
						growth.add(100d);
					} else {
						growth.add((double) Math.round((_income / pre - 1) * 1000) / 10);
					}
				}

			} else {
				income.add(0d);
				volumn.add(0d);
				profit.add(0d);
				growth.add(0d);
			}
		}
		jq.set("��Ʒ���۶�", income).set("��Ʒ������", volumn).set("��Ʒë��", profit).set("��Ʒ���۶������", growth);

		for (int i = 0; i < count; i++){
			if (bm_salesItem.size() > i){
				Document itm = bm_salesItem.get(i);

				Double _income = ((Number) itm.get("income")).doubleValue();
				bm_income.add((double) Math.round(_income * 10) / 10);

				Double _volumn = ((Number) itm.get("volumn")).doubleValue();
				bm_volumn.add((double) Math.round(_volumn));

				Double _profit = ((Number) itm.get("profit")).doubleValue();
				bm_profit.add((double) Math.round(_profit * 10) / 10);

				if (i == 0){
					bm_growth.add(0d);
				} else {
					double pre = ((Number) bm_salesItem.get(i - 1).get("income")).doubleValue();
					if (pre == 0){
						bm_growth.add(100d);
					} else {
						bm_growth.add((double) Math.round((_income / pre - 1) * 1000) / 10);
					}
				}

			} else {
				bm_income.add(0d);
				bm_volumn.add(0d);
				bm_profit.add(0d);
				bm_growth.add(0d);
			}
		}
		jq.set("�Ա��Ʒ���۶�", bm_income).set("�Ա��Ʒ������", bm_volumn).set("�Ա��Ʒë��", bm_profit).set("�Ա��Ʒ���۶������", bm_growth);

		for (int i = 0; i < count; i++){
			if (seriesSalesItem.size() > i){
				Document itm = seriesSalesItem.get(i);

				Double _income = ((Number) itm.get("income")).doubleValue();

				if (i == 0){
					s_growth.add(0d);
				} else {
					double pre = ((Number) seriesSalesItem.get(i - 1).get("income")).doubleValue();
					if (pre == 0){
						s_growth.add(100d);
					} else {
						s_growth.add((double) Math.round((_income / pre - 1) * 1000) / 10);
					}
				}

			} else {
				s_growth.add(0d);
			}
		}
		jq.set("ϵ�в�Ʒ���۶������", s_growth);
		
		return jq.doc();
	}

}
