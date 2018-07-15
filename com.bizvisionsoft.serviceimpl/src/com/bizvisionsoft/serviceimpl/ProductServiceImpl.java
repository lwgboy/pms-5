package com.bizvisionsoft.serviceimpl;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ProductService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.Product;
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
		return Arrays.asList("超级飞侠","巴啦啦小魔仙","爆裂飞车","火力少年王","巨神战击队","铠甲勇士","飓风战魂");
	}

}
