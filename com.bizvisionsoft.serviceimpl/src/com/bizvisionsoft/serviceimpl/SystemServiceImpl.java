package com.bizvisionsoft.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import com.bizvisionsoft.service.common.Service;

import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.ValueRule;
import com.bizvisionsoft.service.ValueRuleSegment;
import com.bizvisionsoft.service.model.Backup;
import com.bizvisionsoft.service.model.ServerInfo;
import com.bizvisionsoft.service.tools.FileTools;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.mongotools.MongoDBBackup;
import com.bizvisionsoft.serviceimpl.update.PMS0501_pmo;
import com.bizvisionsoft.serviceimpl.update.PMS0502_accountitem;
import com.mongodb.BasicDBObject;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.IndexOptions;

public class SystemServiceImpl extends BasicServiceImpl implements SystemService {

	@Override
	public ServerInfo getServerInfo(String req) {
		return new ServerInfo(req).setHostMessage("Hello " + req).seDebugEnabled(logger.isDebugEnabled());

	}

	@Override
	public String mongodbDump(String note) {
		ServerAddress addr = Service.getDatabaseHost();
		String host = addr.getHost();
		int port = addr.getPort();
		String dbName = Service.db().getName();
		String dumpPath = Service.dumpFolder.getPath();
		String path = Service.mongoDbBinPath;
		String result = new MongoDBBackup.Builder().runtime(Runtime.getRuntime()).path(path).host(host).port(port).dbName(dbName)
				.archive(dumpPath + "\\").build().dump();
		try {
			FileTools.writeFile(note, result + "/notes.txt", "utf-8");
		} catch (IOException e) {
			throw new ServiceException(e.getMessage());
		}

		return result;
	}

	@Override
	public List<Backup> getBackups() {
		List<Backup> result = new ArrayList<>();
		File folder = Service.dumpFolder;
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				try {
					String name = files[i].getName();
					Backup backup = new Backup().setDate(new Date(Long.parseLong(name)));
					backup.setId(name);
					File[] note = files[i].listFiles(f -> f.getName().equals("notes.txt"));
					if (note != null && note.length > 0) {
						String text = FileTools.readFile(note[0].getPath(), "utf-8");
						backup.setNotes(text);
					}
					result.add(backup);
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

	@Override
	public void updateBackupNote(String id, String text) {
		File[] files = Service.dumpFolder.listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			try {
				FileTools.writeFile(text, files[0].getPath() + "/notes.txt", "utf-8");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean deleteBackup(String id) {
		File[] files = Service.dumpFolder.listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			return FileTools.deleteFolder(files[0].getPath());
		}
		return false;
	}

	@Override
	public boolean restoreFromBackup(String id) {
		ServerAddress addr = Service.getDatabaseHost();
		String host = addr.getHost();
		int port = addr.getPort();
		String dbName = Service.db().getName();
		String path = Service.mongoDbBinPath;
		String archive;
		File[] files = Service.dumpFolder.listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			archive = files[0].getPath() + "\\" + dbName;
		} else {
			return false;
		}

		new MongoDBBackup.Builder().runtime(Runtime.getRuntime()).path(path).host(host).port(port).dbName(dbName).archive(archive).build()
				.restore();
		return true;
	}

	@Override
	public String getClientSetting(String userId, String clientId, String name) {
		Document doc = c("clientSetting").find(new Document("userId", userId).append("clientId", clientId).append("name", name)).first();
		if (doc != null) {
			return doc.getString("value");
		} else {
			return "";
		}
	}

	@Override
	public void updateClientSetting(Document setting) {
		Document query = new Document();
		query.putAll(setting);
		query.remove("value");

		long cnt = c("clientSetting").countDocuments(query);
		if (cnt == 0) {
			c("clientSetting").insertOne(setting);
		} else {
			c("clientSetting").updateOne(query, new Document("$set", setting));
		}
	}

	@Override
	public void deleteClientSetting(String clientId, String name) {
		c("clientSetting").deleteMany(new Document("clientId", clientId).append("name", name));
	}

	@Override
	public void deleteClientSetting(String clientId) {
		c("clientSetting").deleteMany(new Document("clientId", clientId));
	}

	@Override
	public void deleteClientSetting(String userId, String clientId, String name) {
		c("clientSetting").deleteMany(new Document("userId", userId).append("clientId", clientId).append("name", name));
	}

	@Override
	public void updateSystem(String versionNumber, String packageCode) {
		if ("5.1M1".equals(versionNumber) && "PMO".equals(packageCode))
			new PMS0501_pmo().run();
		if ("5.1M2".equals(versionNumber) && "accountitem".equals(packageCode))
			new PMS0502_accountitem().run();
	}

	@Override
	public void createIndex() {
		// 移到 systemservice中
		Service.db().listCollectionNames().forEach((String col) -> {
			c(col).dropIndexes();
		});

		createUniqueIndex("accountItem", new Document("id", 1), "id");
		createIndex("accountItem", new Document("parent_id", 1), "parent");
		createIndex("accountItem", new Document("subAccounts", 1), "subAccounts");

		createIndex("baseline", new Document("project_id", 1), "project");
		createIndex("baseline", new Document("creationDate", -1), "date");// 按时间倒序

		createIndex("baselineWork", new Document("project_id", 1), "project");
		createIndex("baselineWork", new Document("wbsCode", 1), "wbs");
		createIndex("baselineWork", new Document("index", 1), "index");
		createIndex("baselineWork", new Document("baseline_id", 1), "baseline");
		createIndex("baselineWork", new Document("parent_id", 1), "parent");

		createIndex("baselineWorkLinks", new Document("project_id", 1), "project");
		createIndex("baselineWorkLinks", new Document("source", 1), "source");
		createIndex("baselineWorkLinks", new Document("target", 1), "target");
		createIndex("baselineWorkLinks", new Document("baseline_id", 1), "baseline");

		createIndex("cbs", new Document("scope_id", 1), "scope");
		createIndex("cbs", new Document("parent_id", 1), "parent");
		createUniqueIndex("cbs", new Document("id", 1).append("scope_id", 1), "id_scope");

		createIndex("cbsPeriod", new Document("cbsItem_id", 1), "item");
		createIndex("cbsPeriod", new Document("id", 1), "id");
		createUniqueIndex("cbsPeriod", new Document("cbsItem_id", 1).append("id", 1), "id_cbsitem");

		createIndex("cbsSubject", new Document("cbsItem_id", 1), "item");
		createIndex("cbsSubject", new Document("id", 1), "id");
		createIndex("cbsSubject", new Document("subjectNumber", 1), "subject");
		createIndex("cbsSubject", new Document("id", 1).append("type", 1), "id_type");
		createUniqueIndex("cbsSubject", new Document("cbsItem_id", 1).append("id", 1).append("subjectNumber", 1), "id_item_subject");

		createIndex("docu", new Document("folder_id", 1), "folder");
		createIndex("docu", new Document("tag", 1), "tag");
		createIndex("docu", new Document("category", 1), "category");
		createIndex("docu", new Document("name", 1), "name");
		createIndex("docu", new Document("id", 1), "id");

		createUniqueIndex("eps", new Document("id", 1), "id");

		createIndex("equipment", new Document("org_id", 1), "org");
		createIndex("equipment", new Document("id", 1), "id");
		createIndex("equipment", new Document("resourceType_id", 1), "resourceType");

		createIndex("folder", new Document("project_id", 1), "project");
		createIndex("folder", new Document("parent_id", 1), "parent");
		createUniqueIndex("folder", new Document("name", 1).append("project_id", 1).append("parent_id", 1), "name_project");

		createIndex("funcPermission", new Document("id", 1).append("type", 1), "id_type");

		createIndex("message", new Document("receiver", 1), "receiver");
		createIndex("message", new Document("sendDate", 1), "sendDate");
		createIndex("message", new Document("subject", 1), "subject");

		createIndex("monteCarloSimulate", new Document("project_id", 1), "project");

		createIndex("obs", new Document("scope_id", 1), "scope");
		createIndex("obs", new Document("roleId", 1), "role");
		createIndex("obs", new Document("managerId", 1), "manager");
		createIndex("obs", new Document("parent_id", 1), "parent");
		createIndex("obs", new Document("seq", 1), "seq");
		// OBS允许出现重复的索引
		// createUniqueIndex("obs", new Document("roleId", 1).append("scope_id", 1),
		// "role_scope");

		createIndex("obsInTemplate", new Document("scope_id", 1), "scope");
		createIndex("obsInTemplate", new Document("roleId", 1), "role");
		createIndex("obsInTemplate", new Document("parent_id", 1), "parent");
		createIndex("obsInTemplate", new Document("seq", 1), "seq");
		// OBS允许出现重复的索引
		// createUniqueIndex("obsInTemplate", new Document("roleId",
		// 1).append("scope_id", 1), "role_scope");

		createUniqueIndex("organization", new Document("id", 1), "id");
		createIndex("organization", new Document("managerId", 1), "manager");
		createIndex("organization", new Document("parent_id", 1), "parent");

		createIndex("project", new Document("pmId", 1), "pm");
		createIndex("project", new Document("cbs_id", 1), "cbs");
		// createIndex("project", new Document("workOrder", 1), "workOrder");
		createIndex("project", new Document("obs_id", 1), "obs");
		createIndex("project", new Document("planStart", 1), "planStart");
		createIndex("project", new Document("planFinish", -1), "planFinish");
		createIndex("project", new Document("actualStart", 1), "actualStart");
		createIndex("project", new Document("actualFinish", 1), "actualFinish");
		createIndex("project", new Document("eps_id", 1), "eps");
		createUniqueIndex("project", new Document("id", 1), new IndexOptions().name("id").unique(true).sparse(true));

		createIndex("projectChange", new Document("project_id", 1), "project");
		createIndex("projectChange", new Document("applicantDate", -1), "date");

		// createIndex("program", new Document("workOrder", 1), "workOrder");
		createIndex("program", new Document("eps_id", 1), "eps");

		createIndex("rbsItem", new Document("project_id", 1), "project");
		createIndex("rbsItem", new Document("parent_id", 1), "parent");
		createIndex("rbsItem", new Document("index", 1), "index");

		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1), "equip");
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1), "type");
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1), "hr");
		createIndex("resourceActual", new Document("id", 1), "id");
		createUniqueIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1), "resource");

		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1), "equip");
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1), "type");
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1), "hr");
		createIndex("resourcePlan", new Document("id", 1), "id");
		createUniqueIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1), "resource");

		createUniqueIndex("resourceType", new Document("id", 1), "id");

		createIndex("riskEffect", new Document("project_id", 1), "project");
		createIndex("riskEffect", new Document("rbsItem_id", 1), "item");

		createIndex("salesItem", new Document("period", 1), "period");
		createIndex("salesItem", new Document("project_id", 1), "project");

		createIndex("stockholder", new Document("project_id", 1), "project");

		createUniqueIndex("user", new Document("userId", 1), "user");
		createIndex("user", new Document("org_id", 1), "org");
		createIndex("user", new Document("resourceType_id", 1), "resource");

		createIndex("work", new Document("project_id", 1), "project");
		createIndex("work", new Document("planStart", 1), "planStart");
		createIndex("work", new Document("planFinish", 1), "planFinish");
		createIndex("work", new Document("actualStart", 1), "actualStart");
		createIndex("work", new Document("actualFinish", 1), "actualFinish");
		createIndex("work", new Document("wbsCode", 1), "wbs");
		createIndex("work", new Document("index", 1), "index");
		createIndex("work", new Document("parent_id", 1), "parent");
		createIndex("work", new Document("chargerId", 1), "charger");
		createIndex("work", new Document("assignerId", 1), "assigner");
		createIndex("work", new Document("manageLevel", 1), "manageLevel");

		createIndex("workInTemplate", new Document("wbsCode", 1), "wbs");
		createIndex("workInTemplate", new Document("index", 1), "index");
		createIndex("workInTemplate", new Document("parent_id", 1), "parent");
		createIndex("workInTemplate", new Document("template_id", 1), "template");

		createIndex("workPackage", new Document("work_id", 1), "work");

		createIndex("workPackageProgress", new Document("package_id", 1), "package");
		createIndex("workPackageProgress", new Document("time", -1), "time");

		createIndex("workReport", new Document("project_id", 1).append("type", 1), "type_project");
		createIndex("workReport", new Document("period", 1), "period");
		createIndex("workReport", new Document("reporter", 1).append("type", 1), "type_reporter");

		createIndex("workReportItem", new Document("report_id", 1), "report");
		createIndex("workReportItem", new Document("project_id", 1), "project");

		createIndex("workReportResourceActual",
				new Document("workReportItem_id", 1).append("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1), "equip");
		createIndex("workReportResourceActual",
				new Document("workReportItem_id", 1).append("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1), "type");
		createIndex("workReportResourceActual",
				new Document("workReportItem_id", 1).append("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1), "hr");
		createIndex("workReportResourceActual", new Document("id", 1), "id");
		createUniqueIndex("workReportResourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("workReportItemId", 1).append("id", 1), "resource");

		// createUniqueIndex("resourcePlanInTemplate", new Document("work_id",
		// 1).append("resTypeId", 1)
		// .append("usedHumanResId", 1).append("usedEquipResId",
		// 1).append("usedTypedResId", 1), "resource");

		createIndex("worklinks", new Document("project_id", 1), "project");
		createIndex("worklinks", new Document("space_id", 1), "space");

		createIndex("workspace", new Document("project_id", 1), "project");
		createIndex("workspace", new Document("wbsCode", 1), "wbs");
		createIndex("workspace", new Document("index", 1), "index");
		createIndex("workspace", new Document("parent_id", 1), "parent");
		createIndex("workspace", new Document("space_id", 1), "space");

		createIndex("traceInfo", new Document("date", -1), "date");

		createUniqueIndex("folderInTemplate", new Document("name", 1).append("template_id", 1).append("parent_id", 1), "name_template");

		createUniqueIndex("dictionary", new Document("type", 1).append("id", 1), "type");

		createIndex("accountIncome", new Document("parentId", 1), "parentId");
		createIndex("accountIncome", new Document("subAccounts", 1), "subAccounts");
		createUniqueIndex("accountIncome", new Document("id", 1), "id");

		createIndex("projectTemplate", new Document("id", 1), "id");
		createUniqueIndex("projectTemplate", new Document("id", 1).append("module", 1), "module");

		createUniqueIndex("obsModule", new Document("id", 1), "id");
		// yangjun 2018/10/26
		createIndex("setting", new Document("name", 1), "name");
		createIndex("clientSetting", new Document("userId", 1).append("clientId", 1).append("name", 1), "userClientName");

		createIndex("problem", new Document("id", -1), "id");

		// D1CFT
		createIndex("d1CFT", new Document("problem_id", 1), "problem_id");
		createUniqueIndex("d1CFT", new Document("problem_id", 1).append("userId", 1), "userId");
	}

	private void createUniqueIndex(String collectionName, final Document keys, IndexOptions indexOptions) {
		try {
			c(collectionName).createIndex(keys, indexOptions);
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建唯一性索引错误。" + e.getMessage());
		}
	}

	private void createIndex(String collectionName, final Document keys, String name) {
		try {
			c(collectionName).createIndex(keys, new IndexOptions().name(name));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建索引错误。" + e.getMessage());
		}
	}

	private void createUniqueIndex(String collectionName, final Document keys, String name) {
		try {
			c(collectionName).createIndex(keys, new IndexOptions().name(name).unique(true));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建唯一性索引错误。" + e.getMessage());
		}
	}

	@Override
	public List<ValueRule> listValueRule(BasicDBObject condition) {
		return createDataSet(condition, ValueRule.class);
	}

	@Override
	public long countValueRule(BasicDBObject filter) {
		return count(filter, ValueRule.class);
	}

	@Override
	public ValueRule insertValueRule(ValueRule valueRule) {
		return insert(valueRule);
	}

	@Override
	public long deleteValueRule(ObjectId _id) {
		return delete(_id, ValueRule.class);
	}

	@Override
	public long updateValueRule(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ValueRule.class);
	}

	@Override
	public List<ValueRuleSegment> listValueRuleSegment(BasicDBObject cond, ObjectId rule_id) {
		BasicDBObject filter = (BasicDBObject) cond.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			cond.put("filter", filter);
		}
		filter.put("rule_id", rule_id);

		BasicDBObject sort = (BasicDBObject) cond.get("sort");
		if (sort == null || sort.isEmpty()) {
			sort = new BasicDBObject("index", 1);
		}

		return createDataSet(cond, ValueRuleSegment.class);
	}

	@Override
	public long countValueRuleSegment(BasicDBObject filter, ObjectId rule_id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("rule_id", rule_id);

		return count(filter, ValueRuleSegment.class);
	}

	@Override
	public ValueRuleSegment insertValueRuleSegment(ValueRuleSegment vrs) {
		return insert(vrs);
	}

	@Override
	public Document getMaxSegmentIndex(ObjectId rule_id) {
		List<Bson> pipe = Arrays.asList(Aggregates.match(new Document("rule_id", rule_id)), new Document("$group", new Document("_id", null)
				.append("index", new Document("$max", "$index")).append("executeSequance", new Document("$max", "$executeSequance"))));
		return c("valueRuleSegment").aggregate(pipe).first();
	}

	@Override
	public long deleteValueRuleSegment(ObjectId _id) {
		return delete(_id, ValueRuleSegment.class);
	}

	@Override
	public long updateValueRuleSegment(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ValueRuleSegment.class);
	}

}
