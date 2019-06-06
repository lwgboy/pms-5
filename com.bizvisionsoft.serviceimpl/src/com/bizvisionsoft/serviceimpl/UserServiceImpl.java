package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.TraceInfo;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class UserServiceImpl extends BasicServiceImpl implements UserService {

	@Override
	public long update(BasicDBObject fu, String domain) {
		return update(fu, User.class, domain);
	}

	@Override
	public List<User> listConsigned(String userId, String domain) {
		List<User> ds = createDataSet(new BasicDBObject().append("skip", 0).append("filter", new BasicDBObject("consigner", userId)),
				domain);
		ds.forEach(user -> updateUserRole(user, domain));
		return ds;
	}

	@Override
	public long updatePassword(String newPassword, String userId) {
		MongoCollection<Document> c = c("user");
		Document d = c.find(new Document("userId", userId)).first();
		if (d == null)
			return 0;
		String psw = encryPassword(d.getObjectId("_id").toHexString(), newPassword);
		c.updateOne(new Document("userId", userId), new Document("$set", new Document("password", psw).append("changePSW", false)));
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public User check(String password, String userId) {

		MongoCollection<Document> c = c("user");
		Document userDoc = c.find(new Document("userId", userId)).first();
		if (userDoc == null)
			throw new ServiceException("账户无法通过验证");

		if (!logger.isDebugEnabled()) {
			try {
				String _psw = encryPassword(userDoc.getObjectId("_id").toHexString(), password);
				if (!userDoc.getString("password").equals(_psw)) {
					throw new ServiceException("账户无法通过验证");
				}
			} catch (Exception e) {
				throw new ServiceException("账户无法通过验证");
			}
		}

		String domain = userDoc.getString("domain");
		if (domain == null)
			throw new ServiceException("账户尚未注册应用");

		Document domainData = c("domain").find(new Document("_id", domain)).first();
		if (domainData == null)
			throw new ServiceException("账户尚未分配企业域");
		if (!domainData.getBoolean("activated", false))
			throw new ServiceException("您的企业账户尚未激活");

		User user;
		if (userDoc.getBoolean("admin", false)) {
			// 系统管理员账号,无需取用户数据
			user = new User().setDomain(userDoc.getString("domain")).setUserId(userDoc.getString("userId")).setAdmin(true)
					.setName(userDoc.getString("userId")).setBuzAdmin(userDoc.getBoolean("buzAdmin", false));
		} else {
			List<User> ds = createDataSet(
					new BasicDBObject().append("skip", 0).append("limit", 1).append("filter", new BasicDBObject("userId", userId)), domain);
			if (ds.size() == 0) {
				throw new ServiceException("账户尚未注册应用");
			}

			user = ds.get(0);
			updateUserRole(user, domain);
		}
		user.setDomain(domain);
		String site = userDoc.getString("site");
		List<String> siteList = (List<String>) domainData.get("site");
		if (site == null) {
			site = siteList.get(0);
		}
		user.setSite(site);
		user.setSiteList(siteList);
		return user;

	}

	@Override
	public long updateUserDefaultSite(String sitePath, String userId) {
		MongoCollection<Document> c = c("user");
		UpdateResult result = c.updateOne(new Document("userId", userId), new Document("$set", new Document("site", sitePath)));
		return result.getModifiedCount();
	}

	@Deprecated
	private void updateUserRole(User user, String domain) {

		// 获得user所在的各级组织 TODO 取组织的方法有待优化
		ObjectId org_id = user.getOrganizationId();
		List<String> orgIds = new ArrayList<String>();
		if (org_id != null) {
			Optional.ofNullable(c("organization", domain).find(new Document("_id", org_id)).first()).ifPresent(doc -> {
				while (doc != null) {
					orgIds.add(doc.getString("id"));
					doc = Optional.ofNullable(doc.getObjectId("parent_id"))
							.map(parentId -> c("organization", domain).find(new Document("_id", parentId)).first()).orElse(null);
				}
			});
		}

		ArrayList<String> functionPermissions = c(
				"funcPermission", domain)
						.distinct("role",
								new Document("$or",
										Arrays.asList(new Document("type", "组织").append("id", new Document("$in", orgIds)),
												new Document("type", "用户").append("id", user.getUserId()))),
								String.class)
						.into(new ArrayList<>());

		user.setRoles(functionPermissions);
	}

	@Override
	public User get(String userId, String domain) {
		List<User> ds = createDataSet(
				new BasicDBObject().append("skip", 0).append("limit", 1).append("filter", new BasicDBObject("userId", userId)), domain);
		if (ds.size() == 0) {
			throw new ServiceException("没有用户Id为" + userId + "的用户。");
		}
		return ds.get(0);
	}

	@Override
	public User insert(User user, String domain) {
		String userId = user.getUserId();

		if ("su".equals(userId))// TODO考虑检查表
			throw new ServiceException("禁用的用户Id");

		if (c("user").countDocuments(new Document("userId", userId)) != 0)
			throw new ServiceException("用户Id已被占用");

		if (userId.length() > 48)
			throw new ServiceException("用户Id不能操作48个字符");

		String psw = user.getPassword();
		Document setting = getSystemSetting("设置用户密码要求");
		String pswReq = setting.getString("passwordRequest");
		if (pswReq != null) {
			if (psw == null || (!psw.matches(pswReq))) {
				throw new ServiceException("不符合密码要求" + Check.option(setting.getString("desc")).orElse(""));
			}
		}

		// 加密密码
		ObjectId _id = new ObjectId();
		psw = encryPassword(_id.toHexString(), psw);

		c("user").insertOne(new Document("_id", _id).append("userId", userId).append("admin", false).append("buzAdmin", false)
				.append("password", psw).append("domain", domain).append("activated", true).append("changePSW", false));
		user = insert(user, User.class, domain);
		return user;
	}

	@Override
	public List<User> createDataSet(BasicDBObject condition, String domain) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(null, skip, limit, filter, sort, pipeline -> appendOrgFullName(pipeline, "org_id", "orgFullName"), User.class, domain);
	}

	@Override
	public long count(BasicDBObject filter, String domain) {
		return count(filter, User.class, domain);
	}

	@Override
	public List<User> createDataSet(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(null, skip, limit, filter, sort, pipeline -> appendOrgFullName(pipeline, "org_id", "orgFullName"), User.class, null);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, User.class, null);
	}

	@Override
	public long delete(ObjectId _id, String domain) {
		MongoCollection<Document> col = c("user", domain);
		Document doc = col.find(new BasicDBObject("_id", _id)).projection(new BasicDBObject("activated", 1).append("userId", 1)).first();
		if (doc.getBoolean("activated", false))
			throw new ServiceException("不能删除激活状态的用户。");

		String userId = doc.getString("userId");
		BasicDBObject filter = new BasicDBObject().append("managerId", userId);
		if (count(filter, Organization.class, domain) != 0)
			throw new ServiceException("不能删除在组织中担任管理者的用户。");

		// TODO 有角色的需要清除

		// 检查资源计划
		if (count(new BasicDBObject("usedHumanResId", userId), ResourcePlan.class, domain) != 0)
			throw new ServiceException("不能删除在项目中作为资源计划的用户。");
		// 检查资源用量
		if (count(new BasicDBObject("usedHumanResId", userId), ResourceActual.class, domain) != 0)
			throw new ServiceException("不能删除在项目中作为资源用量的用户。");

		// 检查项目团队成员
		if (count(new BasicDBObject("$or", Arrays.asList(new BasicDBObject("managerId", userId), new BasicDBObject("member", userId))),
				OBSItem.class, domain) != 0)
			throw new ServiceException("不能删除参与到项目中的用户。");

		// TODO 其他检查
		return delete(_id, User.class, domain);
	}

	@Override
	public List<User> createDeptUserDataSet(BasicDBObject condition, String userId, String domain) {
		// 获取我所管理的组织及其下级组织
		List<ObjectId> orgIds = c("organization", domain).distinct("_id", new Document("managerId", userId), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		orgIds = getDesentItems(orgIds, "organization", "parent_id", domain);

		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(new BasicDBObject());

		if (orgIds.size() > 0) {
			filter.append("org_id", new BasicDBObject("$in", orgIds));
			return query(null, null, null, filter, User.class, domain);
		} else
			return new ArrayList<User>();
	}

	@Override
	public long countDeptUser(BasicDBObject filter, String userId, String domain) {
		// 获取我所管理的组织及其下级组织
		List<ObjectId> orgIds = c("organization", domain).distinct("_id", new Document("managerId", userId), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		orgIds = getDesentItems(orgIds, "organization", "parent_id", domain);

		if (filter == null)
			filter = new BasicDBObject();

		if (orgIds.size() > 0) {
			filter.append("org_id", new BasicDBObject("$in", orgIds));
			return count(filter, domain);
		} else
			return 0;
	}

	@Override
	public void consign(String userId, String consignerId, String domain) {
		c("user", domain).updateOne(new Document("userId", userId), new Document("$set", new Document("consigner", consignerId)));
	}

	@Override
	public void disconsign(String userId, String domain) {
		c("user", domain).updateOne(new Document("userId", userId), new Document("$set", new Document("consigner", null)));
	}

	@Override
	public void trace(String userId, boolean trace, String domain) {
		c("user", domain).updateOne(new Document("userId", userId), new Document("$set", new Document("trace", trace)));
	}

	@Override
	public void insertTraceInfo(TraceInfo traceInfo, String domain) {
		insert(traceInfo, domain);
	}

	@Override
	public List<TraceInfo> listTraceInfo(BasicDBObject condition, String domain) {
		return createDataSet(condition, TraceInfo.class, domain);
	}

	@Override
	public long countTraceInfo(BasicDBObject filter, String domain) {
		return count(filter, TraceInfo.class, domain);
	}

	@Override
	public void requestChangePassword(List<String> userIds, String domain) {
		c("user", domain).updateMany(new Document("userId", new Document("$in", userIds)),
				new Document("$set", new Document("changePSW", true)));
	}

	@Override
	public void requestAllChangePassword(String domain) {
		c("user", domain).updateMany(new Document(), new Document("$set", new Document("changePSW", true)));
	}

	@Override
	public List<User> listDelegatableUsers(BasicDBObject condition, String userId, String domain) {
		// 先查询需要显示成员的组织。
		// 根据用户作为管理者的组织
		List<ObjectId> orgIds = getDelegateableOrgIds(userId, domain);
		List<Bson> pipeline = Domain.getJQ(domain, "查询-成员-组织含下级").set("ids", orgIds).array();
		appendConditionToPipeline(pipeline, condition);
		return c("organization", domain).aggregate(pipeline, User.class).into(new ArrayList<>());
	}

	private List<ObjectId> getDelegateableOrgIds(String userId, String domain) {
		List<ObjectId> orgIds = c("organization", domain).find(new Document("managerId", userId)).projection(new Document("_id", true))
				.map(d -> d.getObjectId("_id")).into(new ArrayList<>());
		// 如果用户具有 部门经理 和委派者角色，查询用户所在的直接部门
		boolean hasRolePermission = c("funcPermission", domain).countDocuments(
				new Document("id", userId).append("type", "用户").append("role", new Document("$in", Arrays.asList("部门经理", "委托者")))) > 0;
		if (hasRolePermission) {
			ObjectId org_id = c("user", domain).find(new Document("userId", userId)).projection(new Document("org_id", true)).first()
					.getObjectId("org_id");
			if (org_id != null) {
				orgIds.add(org_id);
			}
		}
		return orgIds;
	}

	@Override
	public long countDelegatableUsers(BasicDBObject filter, String userId, String domain) {
		List<ObjectId> orgIds = getDelegateableOrgIds(userId, domain);
		List<Bson> pipeline = Domain.getJQ(domain, "查询-成员-组织含下级").set("ids", orgIds).array();
		if (filter != null)
			pipeline.add(new BasicDBObject("$match", filter));
		pipeline.add(Aggregates.count("count"));
		Document d = c("organization", domain).aggregate(pipeline).first();
		if (d == null)
			return 0;
		return ((Number) d.get("count")).longValue();
	}

	@Override
	public void updatePassword() {
		c("user").find(new Document("scram", new Document("$ne", true))).forEach((Document d) -> {
			String userId = d.getString("userId");
			String salt = d.getObjectId("_id").toHexString();
			String psw = d.getString("password");
			String psw2 = encryPassword(salt, psw);
			c("user").updateOne(new Document("userId", userId), new Document("$set", new Document("password", psw2).append("scram", true)));
		});

	}


}