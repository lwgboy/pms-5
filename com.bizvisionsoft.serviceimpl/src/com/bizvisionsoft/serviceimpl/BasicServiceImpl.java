package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.Service;
import com.bizvisionsoft.service.dps.EmailSender;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.RSACoder;
import com.bizvisionsoft.serviceimpl.commons.EmailClient;
import com.bizvisionsoft.serviceimpl.commons.EmailClientBuilder;
import com.bizvisionsoft.serviceimpl.commons.NamedAccount;
import com.bizvisionsoft.serviceimpl.scheduling.Consequence;
import com.bizvisionsoft.serviceimpl.scheduling.Graphic;
import com.bizvisionsoft.serviceimpl.scheduling.Relation;
import com.bizvisionsoft.serviceimpl.scheduling.Risk;
import com.bizvisionsoft.serviceimpl.scheduling.Route;
import com.bizvisionsoft.serviceimpl.scheduling.Task;
import com.hankcs.hanlp.RestrictTextRankKeyword;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.UnwindOptions;

public class BasicServiceImpl extends DataServiceImpl {

	public static String CHECKIN_SETTING_NAME = "项目计划提交设置";

	public static String START_SETTING_NAME = "项目启动设置";

	public static String CLOSE_SETTING_NAME = "项目关闭设置";

	protected static List<String> PROJECT_SETTING_NAMES = Arrays.asList(CHECKIN_SETTING_NAME, START_SETTING_NAME, CLOSE_SETTING_NAME);

	public Logger logger = LoggerFactory.getLogger(getClass());

	protected void appendOrgFullName(List<Bson> pipeline, String inputField, String outputField) {
		String tempField = "_org_" + inputField;

		pipeline.add(Aggregates.lookup("organization", inputField, "_id", tempField));

		pipeline.add(Aggregates.unwind("$" + tempField, new UnwindOptions().preserveNullAndEmptyArrays(true)));

		pipeline.add(Aggregates.addFields(new Field<String>(outputField, "$" + tempField + ".fullName")));

		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));//
	}

	protected void appendStage(List<Bson> pipeline, String inputField, String outputField) {
		pipeline.add(Aggregates.lookup("work", inputField, "_id", outputField));

		pipeline.add(Aggregates.unwind("$" + outputField, new UnwindOptions().preserveNullAndEmptyArrays(true)));
	}

	protected void appendUserInfo(List<Bson> pipeline, String useIdField, String userInfoField, String domain) {
		appendUserInfo(pipeline, useIdField, userInfoField, userInfoField + "_meta", domain);
	}

	protected void appendUserInfo(List<Bson> pipeline, String useIdField, String userInfoField, String userMetaField, String domain) {
		pipeline.addAll(Domain.getJQ(domain, "追加-用户")//
				.set("$chargerId", "$" + useIdField)//
				.set("chargerInfo_meta", userMetaField)//
				.set("$chargerInfo_meta", "$" + userMetaField)//
				.set("chargerInfo_meta.org_id", userMetaField + ".org_id")//
				.set("chargerInfo", userInfoField)//
				.set("$chargerInfo_meta.name", "$" + userMetaField + ".name")//
				.set("chargerInfo_meta.orgInfo", userMetaField + ".orgInfo")//
				.array());
	}

	protected void appendUserInfoAndHeadPic(List<Bson> pipeline, String useIdField, String userInfoField, String headPicField) {
		String tempField = "_user_" + useIdField;

		pipeline.add(Aggregates.lookup("user", useIdField, "userId", tempField));

		pipeline.add(Aggregates.unwind("$" + tempField, new UnwindOptions().preserveNullAndEmptyArrays(true)));

		pipeline.add(Aggregates.addFields(
				// info字段 去掉userid显示
				// new Field<BasicDBObject>(userInfoField,
				// new BasicDBObject("$concat",
				// new String[] { "$" + tempField + ".name", " [", "$" + tempField + ".userId",
				// "]" })),
				new Field<String>(userInfoField, "$" + tempField + ".name"),
				// headPics字段
				new Field<BasicDBObject>(headPicField,
						new BasicDBObject("$arrayElemAt", new Object[] { "$" + tempField + ".headPics", 0 }))));

		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));//
	}

	protected void appendWork(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("work", "work_id", "_id", "work"));
		pipeline.add(Aggregates.unwind("$work"));
	}

	protected void appendProject(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
	}

	@Deprecated
	protected List<Bson> getOBSRootPipline(ObjectId project_id) {
		List<Bson> pipeline = new ArrayList<Bson>();

		String tempField = "_obs" + project_id;
		pipeline.add(Aggregates.match(new BasicDBObject("_id", project_id)));
		pipeline.add(Aggregates.lookup("obs", "obs_id", "_id", tempField));
		pipeline.add(Aggregates.project(new BasicDBObject(tempField, true).append("_id", false)));
		pipeline.add(Aggregates.replaceRoot(new BasicDBObject("$mergeObjects",
				new Object[] { new BasicDBObject("$arrayElemAt", new Object[] { "$" + tempField, 0 }), "$$ROOT" })));
		pipeline.add(Aggregates.project(new BasicDBObject(tempField, false)));

		return pipeline;
	}

	public double getWorkingHoursPerDay(ObjectId resTypeId, String domain) {
		// List<? extends Bson> pipeline = Arrays.asList(
		// new Document("$lookup",
		// new Document("from", "resourceType").append("localField", "_id")
		// .append("foreignField", "cal_id").append("as", "resourceType")),
		// new Document("$unwind", "$resourceType"),
		// new Document("$addFields", new Document("resTypeId", "$resourceType._id")),
		// new Document("$project", new Document("resourceType", false)),
		// new Document("$match", new Document("resTypeId", resTypeId)),
		// new Document("$project", new Document("works", true)));

		List<? extends Bson> pipeline = Domain.getJQ(domain, "查询-日历-资源类-每日工时").set("resTypeId", resTypeId).array();
		return Optional.ofNullable(c("calendar", domain).aggregate(pipeline).first()).map(d -> d.getDouble("basicWorks"))
				.map(w -> w.doubleValue()).orElse(0d);
	}

	public boolean checkDayIsWorkingDay(Calendar cal, ObjectId resTypeId, String domain) {

		// List<? extends Bson> pipeline = Arrays
		// .asList(new Document("$lookup",
		// new Document("from", "resourceType").append("localField", "_id")
		// .append("foreignField", "cal_id").append("as", "resourceType")),
		// new Document("$unwind", "$resourceType"),
		// new Document("$addFields", new Document("resTypeId", "$resourceType._id")),
		// new Document("$project", new Document("resourceType", false)),
		// new Document("$match", new Document("resTypeId", resId)), new
		// Document("$unwind", "$workTime"),
		// new Document("$addFields", new Document("date", "$workTime.date")
		// .append("workingDay", "$workTime.workingDay").append("day",
		// "$workTime.day")),
		// new Document("$project", new Document("workTime",
		// false)),
		// new Document()
		// .append("$unwind", new Document("path",
		// "$day").append("preserveNullAndEmptyArrays",
		// true)),
		// new Document("$addFields", new Document().append("workdate",
		// new Document("$cond",
		// Arrays.asList(new Document("$eq", Arrays.asList("$date", cal.getTime())),
		// true,
		// false)))
		// .append("workday", new Document("$eq", Arrays.asList("$day",
		// getDateWeek(cal))))),
		// new Document("$project",
		// new Document("workdate", true).append("workday", true).append("workingDay",
		// true)),
		// new Document("$match",
		// new Document("$or",
		// Arrays.asList(new Document("workdate", true), new Document("workday",
		// true)))),
		// new Document("$sort", new Document("workdate", -1).append("workingDay",
		// -1)));

		List<? extends Bson> pipeline = Domain.getJQ(domain, "查询-日历-资源类-工作日").set("resTypeId", resTypeId)
				.set("week", Formatter.getDateWeek(cal)).set("date", cal.getTime()).array();

		Document doc = c("calendar", domain).aggregate(pipeline).first();
		if (doc != null) {
			boolean workdate = doc.getBoolean("workdate", false);
			boolean workday = doc.getBoolean("workday", false);
			boolean workingDay = doc.getBoolean("workingDay", false);
			if (workdate) {
				return workingDay;
			} else {
				return workday;
			}
		}
		return false;
	}

	protected boolean sendMessage(String subject, String content, String sender, String receiver, String url, String domain) {
		sendMessage(Message.newInstance(subject, content, sender, receiver, url), domain);
		return true;
	}

	protected boolean sendMessage(String subject, String content, String sender, List<String> receivers, String url, String domain) {
		List<Message> toBeInsert = new ArrayList<>();
		new HashSet<String>(receivers).forEach(r -> toBeInsert.add(Message.newInstance(subject, content, sender, r, url)));
		return sendMessages(toBeInsert, domain);
	}

	protected boolean sendMessages(List<Message> toBeInsert, String domain) {
		if (Check.isNotAssigned(toBeInsert))
			return false;
		c(Message.class, domain).insertMany(toBeInsert);
		Document setting = getSystemSetting("邮件设置", domain);
		if (setting != null && Boolean.TRUE.equals(setting.get("emailNotice"))) {
			toBeInsert.forEach(m -> sendEmail(m, "系统", setting, domain));
		}
		return true;
	}

	protected boolean sendMessage(Message message, String domain) {
		c(Message.class, domain).insertOne(message);
		Document setting = getSystemSetting("邮件设置", domain);
		if (setting != null && Boolean.TRUE.equals(setting.get("emailNotice"))) {
			sendEmail(message, "系统", setting, domain);
		}
		return true;
	}

	protected void convertGraphic(ArrayList<Document> works, ArrayList<Document> links, final ArrayList<Task> tasks,
			final ArrayList<Route> routes) {
		works.forEach(doc -> createGraphicTaskNode(tasks, doc));
		works.forEach(doc -> setGraphicTaskParent(tasks, doc));
		links.forEach(doc -> createGrphicRoute(routes, tasks, doc));
	}

	protected void convertGraphicWithRisks(ArrayList<Document> works, ArrayList<Document> links, ArrayList<Document> riskEffect,
			final ArrayList<Task> tasks, final ArrayList<Route> routes, final ArrayList<Risk> risks) {
		convertGraphic(works, links, tasks, routes);
		riskEffect.forEach(doc -> createGraphicRiskNode(tasks, risks, doc));
		riskEffect.forEach(doc -> setGraphicRiskParent(risks, doc));
	}

	private Route createGrphicRoute(ArrayList<Route> routes, ArrayList<Task> tasks, Document doc) {
		String end1Id = doc.getObjectId("source").toHexString();
		String end2Id = doc.getObjectId("target").toHexString();
		Task end1 = tasks.stream().filter(t -> end1Id.equals(t.getId())).findFirst().orElse(null);
		Task end2 = tasks.stream().filter(t -> end2Id.equals(t.getId())).findFirst().orElse(null);
		if (end1 != null && end2 != null) {
			String _type = doc.getString("type");
			int type = Relation.FTS;
			if ("FF".equals(_type)) {
				type = Relation.FTF;
			} else if ("SF".equals(_type)) {
				type = Relation.STF;
			} else if ("SS".equals(_type)) {
				type = Relation.STS;
			}
			float interval = Optional.ofNullable((Number) doc.get("lag")).map(l -> l.floatValue()).orElse(0f);
			Relation rel = new Relation(type, interval);
			Route route = new Route(end1, end2, rel);
			routes.add(route);
			return route;
		}
		return null;
	}

	private void setGraphicTaskParent(final ArrayList<Task> tasks, final Document doc) {
		Optional.ofNullable(doc.getObjectId("parent_id")).map(_id -> _id.toHexString()).ifPresent(parentId -> {
			String id = doc.getObjectId("_id").toHexString();
			Task subTask = tasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
			Task parentTask = tasks.stream().filter(t -> parentId.equals(t.getId())).findFirst().orElse(null);
			if (subTask != null && parentTask != null) {
				parentTask.addSubTask(subTask);
			}
		});
	}

	private void setGraphicRiskParent(ArrayList<Risk> risks, Document doc) {
		Optional.ofNullable(doc.getObjectId("parent_id")).map(_id -> _id.toHexString()).ifPresent(parentId -> {
			String id = doc.getObjectId("_id").toHexString();
			Risk subRisk = risks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
			Risk parentRisk = risks.stream().filter(t -> parentId.equals(t.getId())).findFirst().orElse(null);
			if (subRisk != null && parentRisk != null) {
				parentRisk.addSecondaryRisks(subRisk);
			}
		});
	}

	private void createGraphicRiskNode(ArrayList<Task> tasks, ArrayList<Risk> risks, final Document doc) {
		String id = doc.getObjectId("_id").toHexString();
		double prob = doc.getDouble("probability");// 取出的数字需要除100
		List<?> riskEffects = (List<?>) doc.get("riskEffect");
		List<Consequence> cons = new ArrayList<>();
		if (riskEffects != null && !riskEffects.isEmpty()) {
			for (int i = 0; i < riskEffects.size(); i++) {
				Document rf = (Document) riskEffects.get(i);
				Integer timeInf = rf.getInteger("timeInf");
				if (timeInf != null) {
					Task task = tasks.stream().filter(t -> t.getId().equals(rf.getObjectId("work_id").toHexString())).findFirst()
							.orElse(null);
					if (task != null) {
						cons.add(new Consequence(task, timeInf.intValue()));
					}
				}
			}
		}
		if (cons.size() > 0) {
			risks.add(new Risk(id, (float) prob / 100, cons.toArray(new Consequence[0])));
		}
	}

	private Task createGraphicTaskNode(ArrayList<Task> tasks, Document doc) {
		String id = doc.getObjectId("_id").toHexString();
		Date aStart = doc.getDate("actualStart");
		Date aFinish = doc.getDate("actualFinish");
		Date pStart = doc.getDate("planStart");
		Date pFinish = doc.getDate("planFinish");
		Date now = new Date();
		/////////////////////////////////
		// 只考虑完成时取实际工期
		// 未完成时取计划工期
		long duration;
		if (aFinish != null) {// 如果工作已经完成，工期为实际完成-实际开始
			duration = aFinish.getTime() - aStart.getTime();
		} else if (aStart != null) {// 如果工作已开始
			if (now.after(pFinish)) {// 如果超过完成时间还未完成，视作可能立即完成
				duration = now.getTime() - aStart.getTime() + aStart.getTime() - pStart.getTime();
			} else {
				duration = pFinish.getTime() - pStart.getTime() + aStart.getTime() - pStart.getTime();
			}
		} else if (now.after(pStart)) {
			duration = pFinish.getTime() - pStart.getTime() + now.getTime() - pStart.getTime();
		} else {
			duration = pFinish.getTime() - pStart.getTime();
		}

		Task task = new Task(id, duration / (1000 * 60 * 60 * 24));
		task.setName(doc.getString("name"));
		tasks.add(task);
		return task;
	}

	protected void setupStartDate(Graphic gh, ArrayList<Document> works, Date pjStart, ArrayList<Task> tasks) {
		gh.setStartDate(pjStart);
		gh.getStartRoute().forEach(r -> {
			String id = r.end2.getId();
			ObjectId _id = new ObjectId(id);
			Document doc = works.stream().filter(d -> _id.equals(d.getObjectId("_id"))).findFirst().get();
			Date aStart = doc.getDate("actualStart");
			Date pStart = doc.getDate("planStart");
			gh.setStartDate(id, aStart == null ? pStart : aStart);
		});
	}

	/**
	 * 获得userId 管理的项目
	 * 
	 * @param condition
	 * @param userId
	 * @param domain
	 * @return
	 */
	protected List<ObjectId> getAdministratedProjects(String userId, String domain) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(new Document("$match",
				new Document("status", new Document("$in", Arrays.asList(ProjectStatus.Processing, ProjectStatus.Closing)))));

		// 当前用户具有项目总监权限时显示全部，不显示全部时，加载PMO团队查询
		if (!checkUserRoles(userId, Role.SYS_ROLE_PD_ID, domain)) {
			appendQueryUserInProjectPMO(pipeline, userId, "$_id", domain);
		}

		return c("project", domain).aggregate(pipeline).map(d -> d.getObjectId("_id")).into(new ArrayList<>());
		// return c("project",domain).distinct("_id", new Document("status", "进行中"),
		// ObjectId.class).into(new ArrayList<>());
	}

	/**
	 * 添加获取项目时，只获取当前用户在项目PMO团队中的项目的查询
	 * 
	 * @param pipeline
	 * @param userid
	 * @param domain
	 */
	protected void appendQueryUserInProjectPMO(List<Bson> pipeline, String userid, String scopeIdName, String domain) {
		pipeline.addAll(Domain.getJQ(domain, "查询-项目PMO成员").set("scopeIdName", scopeIdName).set("userId", userid).array());
	}

	public Integer schedule(ObjectId _id, String domain) {
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 锁定
		Document pj = c("project", domain).find(new Document("_id", _id)).first();
		if (pj.getBoolean("backgroundScheduling", false)) {
			return -1;
		}
		c("project", domain).updateOne(new Document("_id", _id), new Document("$set", new Document("backgroundScheduling", true)));

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 前处理：构造图
		ArrayList<Document> works = c("work", domain).find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> links = c("worklinks", domain).find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Task> tasks = new ArrayList<Task>();
		ArrayList<Route> routes = new ArrayList<Route>();
		convertGraphic(works, links, tasks, routes);
		Graphic gh = new Graphic(tasks, routes);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 前处理：初始化日期
		Date start = pj.getDate("planStart");
		Date end = pj.getDate("planFinish");
		setupStartDate(gh, works, start, tasks);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 排程计算
		gh.schedule();

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 后处理：检查项目是否超期
		int warningLevel = 999;
		Document scheduleEst = null;
		// 0级预警检查
		float overTime = gh.getT() - ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
		scheduleEst = new Document("date", new Date()).append("overdue", (int) overTime).append("finish", gh.getFinishDate())
				.append("duration", (int) gh.getT());

		if (overTime > 0) {
			warningLevel = 0;// 0级预警，项目可能超期。
			scheduleEst.append("msg", "项目预计超期");
		}
		for (int i = 0; i < works.size(); i++) {
			Document doc = works.get(i);
			Date planFinish = doc.getDate("planFinish");
			Date estFinish = gh.getTaskEFDate((doc.getObjectId("_id").toHexString()));
			Task task = gh.getTask((doc.getObjectId("_id").toHexString()));
			long overdue = ((estFinish.getTime() - planFinish.getTime()) / (1000 * 60 * 60 * 24));
			Document update = new Document("date", new Date()).append("duration", (int) task.getD().floatValue())
					.append("overdue", (int) overdue).append("finish", estFinish);

			if ("1".equals(doc.getString("manageLevel")) && overdue > 0) {
				warningLevel = warningLevel > 1 ? 1 : warningLevel;
				if (scheduleEst.get("msg") == null) {
					scheduleEst.append("msg", "一级工作预计超期");
				}
			}

			if ("2".equals(doc.getString("manageLevel")) && overdue > 0) {
				warningLevel = warningLevel > 2 ? 2 : warningLevel;
				if (scheduleEst.get("msg") == null) {
					scheduleEst.append("msg", "二级工作预计超期");
				}
			}

			if (update == null) {
				update = new Document();
			}
			update.append("tf", (double) task.getTF()).append("ff", (double) task.getFF());
			c("work", domain).updateOne(new Document("_id", doc.getObjectId("_id")),
					new Document("$set", new Document("scheduleEst", update)));
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 后处理：保存排程结果，解除锁定
		c("project", domain).updateOne(new Document("_id", _id), new Document("$set",
				new Document("overdueIndex", warningLevel).append("scheduleEst", scheduleEst).append("backgroundScheduling", false)));

		return warningLevel;
	}

	public Document getSystemSetting(String name, String domain) {
		return c("setting", domain).find(new Document("name", name)).first();
	}

	public Document getSystemSetting(String name) {
		return Service.database.getCollection("setting").find(new Document("name", name)).first();
	}

	public Object getSystemSetting(String name, String parameter, String domain) {
		return Optional.ofNullable(getSystemSetting(name, domain)).map(d -> d.get(parameter)).orElse(null);
	}

	protected Document getScopeSetting(ObjectId _id, String settingName, String domain) {
		return getSystemSetting(settingName + "@" + _id, domain);
	}

	@SuppressWarnings("unchecked")
	protected static <T> T getSettingValue(Document setting, String key, T defaultValue) {
		return (T) Optional.ofNullable(setting).map(s -> s.get(key)).orElse(defaultValue);
	}

	private boolean sendEmail(Message m, String from, Document setting, String domain) {
		Service.run(() -> {
			String subject = m.getSubject();
			String content = m.getContent();
			String userId = m.getReceiver();
			Document user = c("user", domain).find(new Document("userId", userId)).first();
			if (user == null)
				return;

			String receiverAddress = user.getString("email");
			if (Check.isNotAssigned(receiverAddress)) {
				logger.error("邮件未能发送，原因是没有邮箱地址，用户：" + user);
				return;
			}

			if (logger.isDebugEnabled()) {
				logger.warn("调试模式启动下，只发送到系统设置的接受账号");
				receiverAddress = (String) getSystemSetting("测试邮件接收账户", "testEmail", domain);
				if (Check.isNotAssigned(receiverAddress)) {
					logger.error("邮件未能发送，原因是没有设置系统参数：测试邮件接收账户/testEmail");
					return;
				}
			}

			if (Boolean.TRUE.equals(setting.get("dps"))) {
				EmailSender sender = Service.get(EmailSender.class);
				if (sender != null) {
					try {
						sender.send("BizVision PMS5", receiverAddress, subject, content, from);
						return;
					} catch (Exception e) {
						logger.error("DPS 发送邮件错误。", e);
					}
				} else {
					logger.error("发送邮件失败。");
				}
				return;
			} else {
				String smtpHost = setting.getString("smtpHost");
				int smtpPort;
				try {
					smtpPort = Integer.parseInt(setting.getString("smtpPort"));
				} catch (Exception e) {
					logger.error("smtp端口号配置错误。", e);
					return;
				}
				Boolean smtpUseSSL = Boolean.TRUE.equals(setting.get("smtpUseSSL"));
				String senderPassword = setting.getString("senderPassword");
				String senderAddress = setting.getString("senderAddress");

				try {
					EmailClient client = new EmailClientBuilder(EmailClientBuilder.SIMPLE)//
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
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return;
				}
			}
		});
		return true;
	}

	/**
	 * 检查当前用户是否具有某些角色
	 * 
	 * @param userid
	 *            用户编号
	 * @param roles
	 *            角色
	 * @param domain
	 * @return
	 */
	protected boolean checkUserRoles(String userid, List<String> roles, String domain) {
		// 检查当前用户是否需要显示全部信息
		return c("funcPermission", domain)
				.countDocuments(new BasicDBObject("id", userid).append("role", new BasicDBObject("$in", roles))) > 0;
	}

	/**
	 * 检查当前用户是否具有某角色
	 * 
	 * @param userid
	 *            用户编号
	 * @param role
	 *            角色
	 * @param domain
	 * @return
	 */
	protected boolean checkUserRoles(String userid, String role, String domain) {
		return checkUserRoles(userid, Arrays.asList(role), domain);
	}

	/**
	 * 获取用户名称
	 * 
	 * @param userId
	 * @param domain
	 * @return
	 */
	protected String getUserName(String userId, String domain) {
		return c("user", domain).distinct("name", new Document("userId", userId), String.class).first();
	}

	/**
	 * 提取文本关键字，写入到关键字字段
	 * 
	 * @param t,
	 *            待处理的对象
	 * @param keywordField,
	 *            保存关键字的字段名
	 * @param fields，提取文本的字段名
	 */
	protected List<String> extractKeywords(Document t, int size, String... fields) {
		StringBuffer text = new StringBuffer();
		for (String f : fields) {
			Optional.ofNullable(t.get(f)).ifPresent(text::append);
		}
		String document = text.toString();
		document = Formatter.removeHtmlTag(document);
		if (document.length() == 0)
			return new ArrayList<>();
		RestrictTextRankKeyword trk = new RestrictTextRankKeyword();
		return trk.getKeywords(document, size);
	}

	protected String encryPassword(String salt, String psw) {
		try {
			byte[] data = RSACoder.encryptSHA((salt + psw).getBytes());
			String psw2 = Formatter.bytes2HexString(data);
			return psw2;
		} catch (Exception e) {
			return null;
		}
	}

}
