package com.bizvisionsoft.serviceimpl.update;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.serviceimpl.Service;
import com.mongodb.client.MongoCollection;

/**
 * 更新内容：
 * <p>
 * 1. 增加组织模板，在项目创建后，可直接通过套用组织模板的方式创建项目团队。
 * <p>
 * 2. 修改组织中项目承担单位字段名称为：qualifiedContractor。
 * <p>
 * 3. 增加报告管理，在其中可以查看我管理的项目的日报、周报和月报。
 * <p>
 * 4.
 * 增加项目角色：项目管理组（角色编号为：PMO），功能角色：供应链经理、制造经理、财务经理，并通过PMO角色对所有项目、采购管理、生产管理、成本管理和报告管理中显示的内容进行细分。
 * <p>
 * 所有项目：具有项目总监和项目管理员权限的账户可以访问，具有项目总监权限的用户在其中可以查看所有项目信息。项目管理员权限的账户只能看到其作为项目PMO团队成员的项目。
 * <p>
 * 生产管理：具有制造管理和制造经理权限的账户可以访问，具有制造管理权限的用户在其中可以查看所有项目的生产工作。制造经理权限的账户只能看到其作为项目PMO团队成员的的生产工作。
 * <p>
 * 成本管理：具有成本管理和财务经理权限的账户可以访问，具有成本管理权限的用户在其中可以查看所有项目的成本数据。财务经理权限的账户只能看到其作为项目PMO团队成员的项目成本数据。
 * <p>
 * 报告管理：具有项目总监和项目管理员权限的账户可以访问，具有项目总监权限的用户在其中可以查看所有项目的报告。项目管理员权限的账户只能看到其作为项目PMO团队成员的项目的报告。
 * <p>
 * 注： 更新该功能时，系统将自动在已创建的项目中添加PMO团队。该功能更新完成后，请在服务器端存放js查询的目类中添加以下三个文件：
 * <p>
 * 1.查询-项目PMO成员.js；
 * <p>
 * 2.追加-CBS-CBS叶子节点ID.js；
 * <p>
 * 3.追加-CBSScope-CBS叶子节点ID.js。
 * 
 * @author gdiyang
 *
 */
public class PMS0501_pmo implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void run() {
		// 1.添加词典（注意名称重复）
		insertDictionary("角色名称", "PMO", "项目管理组");
		insertDictionary("功能角色", "项目总监", "项目总监");
		insertDictionary("功能角色", "项目管理员", "项目管理员");
		insertDictionary("功能角色", "供应链管理", "供应链管理");
		insertDictionary("功能角色", "供应链经理", "供应链经理");
		insertDictionary("功能角色", "制造管理", "制造管理");
		insertDictionary("功能角色", "制造经理", "制造经理");
		insertDictionary("功能角色", "成本管理", "成本管理");
		insertDictionary("功能角色", "财务经理", "财务经理");

		// 2.修改组织里面的qualifiedContractor 原：projectBuilder
		c("organization").updateMany(new Document("projectBuilder", new Document("$ne", null)),
				new Document("$rename", new Document("projectBuilder", "qualifiedContractor")));
		logger.info("完成组织字段projectBuilder字段名修改，修改为qualifiedContractor。");

		// 3.历史项目添加PMO
		List<OBSItem> insertOBSItem = new ArrayList<OBSItem>();
		// 获取当前OBS团队中存在PMO的范围ID
		List<ObjectId> scope_ids = c("OBS").distinct("scope_id", new Document("roleId", "PMO"), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		// 获取需要创建PMO团队的项目，并构建需要插入到OBS中的PMO团队
		c("project").find(new Document("_id", new Document("$nin", scope_ids)))
				.projection(new Document("_id", true).append("obs_id", true)).forEach((Document doc) -> {
					insertOBSItem.add(new OBSItem().setIsRole(false).generateSeq().setRoleId("PMO").setName("项目管理组")
							.setScopeRoot(false).setParent_id(doc.getObjectId("obs_id"))
							.setScope_id(doc.getObjectId("_id")));
				});

		if (insertOBSItem.size() > 0)
			try {
				Service.col(OBSItem.class).insertMany(insertOBSItem);
				logger.info("完成项目添加PMO团队。");
			} catch (Exception e) {
				logger.info("为项目添加PMO团队时出现错误:"+e.getMessage());
			}
	}

	/**
	 * 在名称中创建角色
	 * 
	 * @param type
	 * @param id
	 * @param name
	 */
	private void insertDictionary(String type, String id, String name) {
		try {
			c("dictionary").insertOne(new Document("type", type).append("id", id).append("name", name));
			logger.info("完成 " + type + " " + id + " 的添加");
		} catch (Exception e) {
			logger.error("添加 " + type + " " + id + " 时出现错误:" + e.getMessage());
		}
	}

	private MongoCollection<Document> c(String name) {
		return Service.col(name);
	}

}
