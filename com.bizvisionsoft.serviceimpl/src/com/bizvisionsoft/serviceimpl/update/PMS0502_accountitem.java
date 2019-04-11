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
		// 1.重新创建数据库索引
		new SystemServiceImpl().createIndex();

		// 2更新费用类科目
		// 2.1 更新parentId，清除掉parent_id
		// 获取所有需要更新的parent_id
		List<ObjectId> parent_ids = c("accountItem")
				.distinct("parent_id", new Document("parent_id", new Document("$ne", null)), ObjectId.class)
				.into(new ArrayList<>());
		// 获取parent_id对应的id
		c("accountItem").find(new Document("_id", new Document("$in", parent_ids)))
				.projection(new Document("_id", true).append("id", true)).forEach((Document doc) -> {
					// 更新parentId，清除掉parent_id
					c("accountItem").updateMany(new Document("parent_id", doc.get("_id")),
							new Document("$set", new Document("parentId", doc.get("id"))).append("$unset",
									new Document("parent_id", true)));
				});
		logger.info("完成accountItem关系字段parent_id到关系字段parentId的切换。");
		// 2.2生成subAccounts
		updateSubAccounts(new Document("parentId", null));
		logger.info("完成accountItem中subAccounts字段的更新。");

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
