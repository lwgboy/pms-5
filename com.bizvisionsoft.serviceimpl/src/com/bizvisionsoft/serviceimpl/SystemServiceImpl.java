package com.bizvisionsoft.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.SystemService;
import com.bizvisionsoft.service.ValueRule;
import com.bizvisionsoft.service.ValueRuleSegment;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.common.Service;
import com.bizvisionsoft.service.model.Backup;
import com.bizvisionsoft.service.model.ServerInfo;
import com.bizvisionsoft.service.tools.FileTools;
import com.bizvisionsoft.serviceimpl.commons.EmailClient;
import com.bizvisionsoft.serviceimpl.commons.EmailClientBuilder;
import com.bizvisionsoft.serviceimpl.commons.NamedAccount;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.mongotools.MongoDBBackup;
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
	public String mongodbDump(String note, String domain) {
		ServerAddress addr = Service.getDatabaseServerList().get(0);
		String host = addr.getHost();
		int port = addr.getPort();

		String dbName = domain;
		String dumpPath = Domain.getDumpFolder(domain);
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
	public List<Backup> getBackups(String domain) {
		List<Backup> result = new ArrayList<>();
		File folder = new File(Domain.getDumpFolder(domain));
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
	public void updateBackupNote(String id, String text, String domain) {
		File[] files = new File(Domain.getDumpFolder(domain)).listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			try {
				FileTools.writeFile(text, files[0].getPath() + "/notes.txt", "utf-8");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean deleteBackup(String id, String domain) {
		File[] files = new File(Domain.getDumpFolder(domain)).listFiles(f -> f.isDirectory() && id.equals(f.getName()));
		if (files != null && files.length > 0) {
			return FileTools.deleteFolder(files[0].getPath());
		}
		return false;
	}

	@Override
	public boolean restoreFromBackup(String id, String domain) {
		ServerAddress addr = Service.getDatabaseServerList().get(0);
		String host = addr.getHost();
		int port = addr.getPort();
		String dbName = domain;
		String path = Service.mongoDbBinPath;
		String archive;
		File[] files = new File(Domain.getDumpFolder(domain)).listFiles(f -> f.isDirectory() && id.equals(f.getName()));
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
	public String getClientSetting(String userId, String clientId, String name, String domain) {
		Document doc = c("clientSetting", domain).find(new Document("userId", userId).append("clientId", clientId).append("name", name))
				.first();
		if (doc != null) {
			return doc.getString("value");
		} else {
			return "";
		}
	}

	@Override
	public void updateClientSetting(Document setting, String domain) {
		Document query = new Document();
		query.putAll(setting);
		query.remove("value");

		long cnt = c("clientSetting", domain).countDocuments(query);
		if (cnt == 0) {
			c("clientSetting", domain).insertOne(setting);
		} else {
			c("clientSetting", domain).updateOne(query, new Document("$set", setting));
		}
	}

	@Override
	public void deleteClientSetting(String clientId, String name, String domain) {
		c("clientSetting", domain).deleteMany(new Document("clientId", clientId).append("name", name));
	}

	@Override
	public void deleteClientSetting(String clientId, String domain) {
		c("clientSetting", domain).deleteMany(new Document("clientId", clientId));
	}

	@Override
	public void deleteClientSetting(String userId, String clientId, String name, String domain) {
		c("clientSetting", domain).deleteMany(new Document("userId", userId).append("clientId", clientId).append("name", name));
	}

	@Override
	public void updateSystem(String versionNumber, String packageCode, String domain) {
	}

	@Override
	public void createIndex(String domain) {
		// 移到 systemservice中
		Domain.getDatabase(domain).listCollectionNames().forEach((String col) -> {
			c(col, domain).dropIndexes();
		});

		createUniqueIndex("accountItem", new Document("id", 1), "id", domain);
		createIndex("accountItem", new Document("parent_id", 1), "parent", domain);
		createIndex("accountItem", new Document("subAccounts", 1), "subAccounts", domain);

		createIndex("baseline", new Document("project_id", 1), "project", domain);
		createIndex("baseline", new Document("creationDate", -1), "date", domain);// 按时间倒序

		createIndex("baselineWork", new Document("project_id", 1), "project", domain);
		createIndex("baselineWork", new Document("wbsCode", 1), "wbs", domain);
		createIndex("baselineWork", new Document("index", 1), "index", domain);
		createIndex("baselineWork", new Document("baseline_id", 1), "baseline", domain);
		createIndex("baselineWork", new Document("parent_id", 1), "parent", domain);

		createIndex("baselineWorkLinks", new Document("project_id", 1), "project", domain);
		createIndex("baselineWorkLinks", new Document("source", 1), "source", domain);
		createIndex("baselineWorkLinks", new Document("target", 1), "target", domain);
		createIndex("baselineWorkLinks", new Document("baseline_id", 1), "baseline", domain);

		createIndex("cbs", new Document("scope_id", 1), "scope", domain);
		createIndex("cbs", new Document("parent_id", 1), "parent", domain);
		createUniqueIndex("cbs", new Document("id", 1).append("scope_id", 1), "id_scope", domain);

		createIndex("cbsPeriod", new Document("cbsItem_id", 1), "item", domain);
		createIndex("cbsPeriod", new Document("id", 1), "id", domain);
		createUniqueIndex("cbsPeriod", new Document("cbsItem_id", 1).append("id", 1), "id_cbsitem", domain);

		createIndex("cbsSubject", new Document("cbsItem_id", 1), "item", domain);
		createIndex("cbsSubject", new Document("id", 1), "id", domain);
		createIndex("cbsSubject", new Document("subjectNumber", 1), "subject", domain);
		createIndex("cbsSubject", new Document("id", 1).append("type", 1), "id_type", domain);
		createUniqueIndex("cbsSubject", new Document("cbsItem_id", 1).append("id", 1).append("subjectNumber", 1), "id_item_subject",
				domain);

		createIndex("docu", new Document("folder_id", 1), "folder", domain);
		createIndex("docu", new Document("tag", 1), "tag", domain);
		createIndex("docu", new Document("category", 1), "category", domain);
		createIndex("docu", new Document("name", 1), "name", domain);
		createIndex("docu", new Document("id", 1), "id", domain);

		createUniqueIndex("eps", new Document("id", 1), "id", domain);

		createIndex("equipment", new Document("org_id", 1), "org", domain);
		createIndex("equipment", new Document("id", 1), "id", domain);
		createIndex("equipment", new Document("resourceType_id", 1), "resourceType", domain);

		createIndex("folder", new Document("project_id", 1), "project", domain);
		createIndex("folder", new Document("parent_id", 1), "parent", domain);
		createUniqueIndex("folder", new Document("name", 1).append("project_id", 1).append("parent_id", 1), "name_project", domain);

		createIndex("funcPermission", new Document("id", 1).append("type", 1), "id_type", domain);

		createIndex("message", new Document("receiver", 1), "receiver", domain);
		createIndex("message", new Document("sendDate", 1), "sendDate", domain);
		createIndex("message", new Document("subject", 1), "subject", domain);

		createIndex("monteCarloSimulate", new Document("project_id", 1), "project", domain);

		createIndex("obs", new Document("scope_id", 1), "scope", domain);
		createIndex("obs", new Document("roleId", 1), "role", domain);
		createIndex("obs", new Document("managerId", 1), "manager", domain);
		createIndex("obs", new Document("parent_id", 1), "parent", domain);
		createIndex("obs", new Document("seq", 1), "seq", domain);
		// OBS允许出现重复的索引
		// createUniqueIndex("obs", new Document("roleId", 1).append("scope_id", 1),
		// "role_scope",domain);

		createIndex("obsInTemplate", new Document("scope_id", 1), "scope", domain);
		createIndex("obsInTemplate", new Document("roleId", 1), "role", domain);
		createIndex("obsInTemplate", new Document("parent_id", 1), "parent", domain);
		createIndex("obsInTemplate", new Document("seq", 1), "seq", domain);
		// OBS允许出现重复的索引
		// createUniqueIndex("obsInTemplate", new Document("roleId",
		// 1).append("scope_id", 1), "role_scope",domain);

		createUniqueIndex("organization", new Document("id", 1), "id", domain);
		createIndex("organization", new Document("managerId", 1), "manager", domain);
		createIndex("organization", new Document("parent_id", 1), "parent", domain);

		createIndex("project", new Document("pmId", 1), "pm", domain);
		createIndex("project", new Document("cbs_id", 1), "cbs", domain);
		// createIndex("project", new Document("workOrder", 1), "workOrder",domain);
		createIndex("project", new Document("obs_id", 1), "obs", domain);
		createIndex("project", new Document("planStart", 1), "planStart", domain);
		createIndex("project", new Document("planFinish", -1), "planFinish", domain);
		createIndex("project", new Document("actualStart", 1), "actualStart", domain);
		createIndex("project", new Document("actualFinish", 1), "actualFinish", domain);
		createIndex("project", new Document("eps_id", 1), "eps", domain);
		createUniqueIndex("project", new Document("id", 1), new IndexOptions().name("id").unique(true).sparse(true), domain);

		createIndex("projectChange", new Document("project_id", 1), "project", domain);
		createIndex("projectChange", new Document("applicantDate", -1), "date", domain);

		// createIndex("program", new Document("workOrder", 1), "workOrder",domain);
		createIndex("program", new Document("eps_id", 1), "eps", domain);

		createIndex("rbsItem", new Document("project_id", 1), "project", domain);
		createIndex("rbsItem", new Document("parent_id", 1), "parent", domain);
		createIndex("rbsItem", new Document("index", 1), "index", domain);

		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1), "equip", domain);
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1), "type", domain);
		createIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1), "hr", domain);
		createIndex("resourceActual", new Document("id", 1), "id", domain);
		createUniqueIndex("resourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1), "resource", domain);

		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1), "equip", domain);
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1), "type", domain);
		createIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1), "hr", domain);
		createIndex("resourcePlan", new Document("id", 1), "id", domain);
		createUniqueIndex("resourcePlan", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
				.append("usedEquipResId", 1).append("usedTypedResId", 1).append("id", 1), "resource", domain);

		createUniqueIndex("resourceType", new Document("id", 1), "id", domain);

		createIndex("riskEffect", new Document("project_id", 1), "project", domain);
		createIndex("riskEffect", new Document("rbsItem_id", 1), "item", domain);

		createIndex("salesItem", new Document("period", 1), "period", domain);
		createIndex("salesItem", new Document("project_id", 1), "project", domain);

		createIndex("stockholder", new Document("project_id", 1), "project", domain);

		createUniqueIndex("user", new Document("userId", 1), "user", domain);
		createIndex("user", new Document("org_id", 1), "org", domain);
		createIndex("user", new Document("resourceType_id", 1), "resource", domain);

		createIndex("work", new Document("project_id", 1), "project", domain);
		createIndex("work", new Document("planStart", 1), "planStart", domain);
		createIndex("work", new Document("planFinish", 1), "planFinish", domain);
		createIndex("work", new Document("actualStart", 1), "actualStart", domain);
		createIndex("work", new Document("actualFinish", 1), "actualFinish", domain);
		createIndex("work", new Document("wbsCode", 1), "wbs", domain);
		createIndex("work", new Document("index", 1), "index", domain);
		createIndex("work", new Document("parent_id", 1), "parent", domain);
		createIndex("work", new Document("chargerId", 1), "charger", domain);
		createIndex("work", new Document("assignerId", 1), "assigner", domain);
		createIndex("work", new Document("manageLevel", 1), "manageLevel", domain);

		createIndex("workInTemplate", new Document("wbsCode", 1), "wbs", domain);
		createIndex("workInTemplate", new Document("index", 1), "index", domain);
		createIndex("workInTemplate", new Document("parent_id", 1), "parent", domain);
		createIndex("workInTemplate", new Document("template_id", 1), "template", domain);

		createIndex("workPackage", new Document("work_id", 1), "work", domain);

		createIndex("workPackageProgress", new Document("package_id", 1), "package", domain);
		createIndex("workPackageProgress", new Document("time", -1), "time", domain);

		createIndex("workReport", new Document("project_id", 1).append("type", 1), "type_project", domain);
		createIndex("workReport", new Document("period", 1), "period", domain);
		createIndex("workReport", new Document("reporter", 1).append("type", 1), "type_reporter", domain);

		createIndex("workReportItem", new Document("report_id", 1), "report", domain);
		createIndex("workReportItem", new Document("project_id", 1), "project", domain);

		createIndex("workReportResourceActual",
				new Document("workReportItem_id", 1).append("work_id", 1).append("resTypeId", 1).append("usedEquipResId", 1), "equip",
				domain);
		createIndex("workReportResourceActual",
				new Document("workReportItem_id", 1).append("work_id", 1).append("resTypeId", 1).append("usedTypedResId", 1), "type",
				domain);
		createIndex("workReportResourceActual",
				new Document("workReportItem_id", 1).append("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1), "hr", domain);
		createIndex("workReportResourceActual", new Document("id", 1), "id", domain);
		createUniqueIndex(
				"workReportResourceActual", new Document("work_id", 1).append("resTypeId", 1).append("usedHumanResId", 1)
						.append("usedEquipResId", 1).append("usedTypedResId", 1).append("workReportItemId", 1).append("id", 1),
				"resource", domain);

		// createUniqueIndex("resourcePlanInTemplate", new Document("work_id",
		// 1).append("resTypeId", 1)
		// .append("usedHumanResId", 1).append("usedEquipResId",
		// 1).append("usedTypedResId", 1), "resource",domain);

		createIndex("worklinks", new Document("project_id", 1), "project", domain);
		createIndex("worklinks", new Document("space_id", 1), "space", domain);

		createIndex("workspace", new Document("project_id", 1), "project", domain);
		createIndex("workspace", new Document("wbsCode", 1), "wbs", domain);
		createIndex("workspace", new Document("index", 1), "index", domain);
		createIndex("workspace", new Document("parent_id", 1), "parent", domain);
		createIndex("workspace", new Document("space_id", 1), "space", domain);

		createIndex("traceInfo", new Document("date", -1), "date", domain);

		createUniqueIndex("folderInTemplate", new Document("name", 1).append("template_id", 1).append("parent_id", 1), "name_template",
				domain);

		createUniqueIndex("dictionary", new Document("type", 1).append("id", 1), "type", domain);

		createIndex("accountIncome", new Document("parentId", 1), "parentId", domain);
		createIndex("accountIncome", new Document("subAccounts", 1), "subAccounts", domain);
		createUniqueIndex("accountIncome", new Document("id", 1), "id", domain);

		createIndex("projectTemplate", new Document("id", 1), "id", domain);
		createUniqueIndex("projectTemplate", new Document("id", 1).append("module", 1), "module", domain);

		createUniqueIndex("obsModule", new Document("id", 1), "id", domain);
		// yangjun 2018/10/26
		createIndex("setting", new Document("name", 1), "name", domain);
		createIndex("clientSetting", new Document("userId", 1).append("clientId", 1).append("name", 1), "userClientName", domain);

		createIndex("problem", new Document("id", -1), "id", domain);

		// D1CFT
		createIndex("d1CFT", new Document("problem_id", 1), "problem_id", domain);
		createUniqueIndex("d1CFT", new Document("problem_id", 1).append("userId", 1), "userId", domain);
	}

	private void createUniqueIndex(String collectionName, final Document keys, IndexOptions indexOptions, String domain) {
		try {
			c(collectionName, domain).createIndex(keys, indexOptions);
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建唯一性索引错误。" + e.getMessage());
		}
	}

	private void createIndex(String collectionName, final Document keys, String name, String domain) {
		try {
			c(collectionName, domain).createIndex(keys, new IndexOptions().name(name));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建索引错误。" + e.getMessage());
		}
	}

	private void createUniqueIndex(String collectionName, final Document keys, String name, String domain) {
		try {
			c(collectionName, domain).createIndex(keys, new IndexOptions().name(name).unique(true));
		} catch (Exception e) {
			throw new ServiceException("集合：" + collectionName + "创建唯一性索引错误。" + e.getMessage());
		}
	}

	@Override
	public List<ValueRule> listValueRule(BasicDBObject condition, String domain) {
		return createDataSet(condition, ValueRule.class, domain);
	}

	@Override
	public long countValueRule(BasicDBObject filter, String domain) {
		return count(filter, ValueRule.class, domain);
	}

	@Override
	public ValueRule insertValueRule(ValueRule valueRule, String domain) {
		return insert(valueRule, domain);
	}

	@Override
	public long deleteValueRule(ObjectId _id, String domain) {
		return delete(_id, ValueRule.class, domain);
	}

	@Override
	public long updateValueRule(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, ValueRule.class, domain);
	}

	@Override
	public List<ValueRuleSegment> listValueRuleSegment(BasicDBObject cond, ObjectId rule_id, String domain) {
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

		return createDataSet(cond, ValueRuleSegment.class, domain);
	}

	@Override
	public long countValueRuleSegment(BasicDBObject filter, ObjectId rule_id, String domain) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("rule_id", rule_id);

		return count(filter, ValueRuleSegment.class, domain);
	}

	@Override
	public ValueRuleSegment insertValueRuleSegment(ValueRuleSegment vrs, String domain) {
		return insert(vrs, domain);
	}

	@Override
	public Document getMaxSegmentIndex(ObjectId rule_id, String domain) {
		List<Bson> pipe = Arrays.asList(Aggregates.match(new Document("rule_id", rule_id)), new Document("$group", new Document("_id", null)
				.append("index", new Document("$max", "$index")).append("executeSequance", new Document("$max", "$executeSequance"))));
		return c("valueRuleSegment", domain).aggregate(pipe).first();
	}

	@Override
	public long deleteValueRuleSegment(ObjectId _id, String domain) {
		return delete(_id, ValueRuleSegment.class, domain);
	}

	@Override
	public long updateValueRuleSegment(BasicDBObject filterAndUpdate, String domain) {
		return update(filterAndUpdate, ValueRuleSegment.class, domain);
	}

	@Override
	public void requestDomain(Document data) {
		ObjectId id = new ObjectId();
		// TODO 检查用户名是否会重复
		hostCol("request").insertOne(data.append("_id", id).append("activated", false));

		String receiverAddress = data.getString("email");
		String subject = "欢迎注册WisPlanner账户";
		String from = "WisPlanner";

		String request = data.getString("request");
		String content = "请点击以下链接验证您的邮箱地址<br>";
		content += request + "bvs/verify?r=" + id;

		Document setting = getSystemSetting("邮件设置");
		String smtpHost = setting.getString("smtpHost");
		int smtpPort = Integer.parseInt(setting.getString("smtpPort"));
		boolean smtpUseSSL = Boolean.TRUE.equals(setting.get("smtpUseSSL"));
		String senderPassword = setting.getString("senderPassword");
		String senderAddress = setting.getString("senderAddress");

		EmailClient client = new EmailClientBuilder(EmailClientBuilder.HTML)//
				.setSenderAddress(senderAddress)//
				.setSenderPassword(senderPassword)//
				.setSmtpHost(smtpHost)//
				.setSmtpPort(smtpPort)//
				.setSmtpUseSSL(smtpUseSSL)//
				.setCharset("GB2312")//
				.build();
		try {
			client.setSubject(subject)//
					.setMessage(content)//
					.setFrom(new NamedAccount(from, senderAddress))//
					.addCc(new NamedAccount(receiverAddress))//
					.send();
			return;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}

	@Override
	public Document createDomainFromRequest(ObjectId _id) {
		Document result = hostCol("request").findOneAndUpdate(new Document("_id", _id).append("activated", false),
				new Document("$set", new Document("activated", true)));
		if (result != null) {
			int code = generateCode("ids", "domain");
			String domain = "bvs_" + code;
			String domainRoot = Service.serverConfigRootPath + "/" + domain;
			String schemeRoot = Service.serverConfigRootPath + "/scheme/" + result.getString("scheme");

			// 创建domain
			List<String> sites = ((List<?>) result.get("site")).stream().map(s -> {
				File file = new File((String) s);
				return file.getParentFile().getParent() + "/site/" + domain + "." + file.getName();
			}).collect(Collectors.toList());

			Document domainData = new Document("_id", domain).append("activated", false).append("rootPath", domainRoot).append("site",
					sites);
			hostCol("domain").insertOne(domainData);
			// 复制配置文件
			try {

				FileUtils.copyDirectoryToDirectory(new File(schemeRoot + "/bpmn"), new File(domainRoot));
				FileUtils.copyDirectoryToDirectory(new File(schemeRoot + "/query"), new File(domainRoot));
				FileUtils.copyDirectoryToDirectory(new File(schemeRoot + "/rptdesign"), new File(domainRoot));
				new File(domainRoot + "/dump").mkdirs();
			} catch (IOException e) {
				logger.error("复制scheme出错", e);
				return new Document();
			}
			// 启动域
			new Domain(domainData).start();
			// 装入基础业务数据
			if (result.getBoolean("loadBasicData", false)) {
				ServerAddress addr = Service.getDatabaseServerList().get(0);
				String host = addr.getHost();
				int port = addr.getPort();
				String dbName = domain;
				String path = Service.mongoDbBinPath;
				String archive;
				File[] files = new File(schemeRoot + "/dump").listFiles(f -> f.isDirectory() && "basic".equals(f.getName()));
				if (files != null && files.length > 0) {
					archive = files[0].getPath() + "/bvs_std";
					new MongoDBBackup.Builder().runtime(Runtime.getRuntime()).path(path).host(host).port(port).dbName(dbName)
							.archive(archive).build().restore();
				}

				// 创建根组织
				c("organization", domain)
						.insertOne(new Document("name", result.getString("company")).append("fullName", result.getString("company"))
								.append("id", domain).append("type", "公司").append("externalType", "内部"));

			}
			// 插入超级用户
			hostCol("user").insertOne(new Document("userId", result.getString("email")).append("admin", true).append("buzAdmin", true)
					.append("password", result.getString("psw")).append("domain", domain).append("activated", true)
					.append("changePSW", false));

			return domainData;
		}
		return new Document();
	}

	@Override
	public List<Document> listScheme() {
		return Arrays.asList(new File(Service.serverConfigRootPath + "/scheme").listFiles()).stream().map(f -> {
			try {
				String text = FileTools.readFile(f.getPath() + "/note.txt", "utf-8");
				return new JQ().doc(text);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new Document();
		}).collect(Collectors.toList());
	}

	@Override
	public boolean checkRequest(ObjectId _id) {
		return hostCol("request").countDocuments(new Document("_id", _id).append("activated", false)) == 1;
	}

}
