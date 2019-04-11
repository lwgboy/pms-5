package com.bizvisionsoft.serviceimpl.update;
import com.bizvisionsoft.service.common.Service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.serviceimpl.SystemServiceImpl;
import com.mongodb.client.MongoCollection;

public class PMS0502_accountitem implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void run() {
		// 1.���´������ݿ�����
		new SystemServiceImpl().createIndex();

		// 2���·������Ŀ
		// 2.1 ����parentId�������parent_id
		// ��ȡ������Ҫ���µ�parent_id
		List<ObjectId> parent_ids = c("accountItem")
				.distinct("parent_id", new Document("parent_id", new Document("$ne", null)), ObjectId.class)
				.into(new ArrayList<>());
		// ��ȡparent_id��Ӧ��id
		c("accountItem").find(new Document("_id", new Document("$in", parent_ids)))
				.projection(new Document("_id", true).append("id", true)).forEach((Document doc) -> {
					// ����parentId�������parent_id
					c("accountItem").updateMany(new Document("parent_id", doc.get("_id")),
							new Document("$set", new Document("parentId", doc.get("id"))).append("$unset",
									new Document("parent_id", true)));
				});
		logger.info("���accountItem��ϵ�ֶ�parent_id����ϵ�ֶ�parentId���л���");
		// 2.2����subAccounts
		updateSubAccounts(new Document("parentId", null));
		logger.info("���accountItem��subAccounts�ֶεĸ��¡�");

	}

	private List<Object> updateSubAccounts(Document filter) {
		List<Object> result = new ArrayList<Object>();
		c("accountItem").find(filter).projection(new Document("id", true)).forEach((Document doc) -> {
			Object id = doc.get("id");
			List<Object> subAccounts = updateSubAccounts(new Document("parentId", id));
			if (subAccounts.size() > 0) {
				c("accountItem").updateMany(new Document("id", id),
						new Document("$set", new Document("subAccounts", subAccounts)));
				result.addAll(subAccounts);
			}
			result.add(id);
		});
		return result;
	}

	private MongoCollection<Document> c(String name) {
		return Service.col(name);
	}

}
