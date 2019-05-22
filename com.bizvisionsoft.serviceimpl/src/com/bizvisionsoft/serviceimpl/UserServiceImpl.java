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
	public long updatePassword(String userId, String newPassword) {
		MongoCollection<Document> c = hostCol("user");
		UpdateResult r = c.updateOne(new Document("userId", userId),
				new Document("$set", new Document("password", newPassword).append("changePSW", false)));
		return r.getModifiedCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public User check(String userId, String password) {
		BasicDBObject filter = new BasicDBObject("userId", userId);
		if (!logger.isDebugEnabled())
			filter.append("password", password);
		else
			logger.debug("�Ѻ���������֤��");

		MongoCollection<Document> c = hostCol("user");
		Document userDoc = c.find(filter).first();
		if (userDoc == null)
			throw new ServiceException("�˻��޷�ͨ����֤");

		String domain = userDoc.getString("domain");
		if (domain == null)
			throw new ServiceException("�˻���δע��Ӧ��");

		Document domainData = com.bizvisionsoft.service.common.Service.database.getCollection("domain").find(new Document("_id", domain))
				.first();
		if (domainData == null)
			throw new ServiceException("�˻���δ��������");
		if (!domainData.getBoolean("activated", false))
			throw new ServiceException("������ҵ�˻���δ����");

		User user;
		if (userDoc.getBoolean("admin", false)) {
			// ϵͳ����Ա�˺�,����ȡ�û�����
			user = new User().setDomain(userDoc.getString("domain")).setUserId(userDoc.getString("userId")).setAdmin(true)
					.setName(userDoc.getString("userId")).setBuzAdmin(userDoc.getBoolean("buzAdmin", false));
		} else {
			List<User> ds = createDataSet(
					new BasicDBObject().append("skip", 0).append("limit", 1).append("filter", new BasicDBObject("userId", userId)), domain);
			if (ds.size() == 0) {
				throw new ServiceException("�˻���δע��Ӧ��");
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
		MongoCollection<Document> c = hostCol("user");
		UpdateResult result = c.updateOne(new Document("userId", userId), new Document("$set", new Document("site", sitePath)));
		return result.getModifiedCount();
	}

	@Deprecated
	private void updateUserRole(User user, String domain) {

		// ���user���ڵĸ�����֯ TODO ȡ��֯�ķ����д��Ż�
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
										Arrays.asList(new Document("type", "��֯").append("id", new Document("$in", orgIds)),
												new Document("type", "�û�").append("id", user.getUserId()))),
								String.class)
						.into(new ArrayList<>());

		user.setRoles(functionPermissions);
	}

	@Override
	public User get(String userId, String domain) {
		List<User> ds = createDataSet(
				new BasicDBObject().append("skip", 0).append("limit", 1).append("filter", new BasicDBObject("userId", userId)), domain);
		if (ds.size() == 0) {
			throw new ServiceException("û���û�IdΪ" + userId + "���û���");
		}
		return ds.get(0);
	}

	@Override
	public User insert(User user, String domain) {
		// ����û����Ƿ��ظ�
		String userId = user.getUserId();
		if (hostCol("user").countDocuments(new Document("userId", userId)) != 0)
			throw new ServiceException("�û�Id�ѱ�ռ��");
		if (userId.length() > 48)
			throw new ServiceException("�û�Id���ܲ���48���ַ�");
		String psw = user.getPassword();
		Document setting = getSystemSetting("�����û�����Ҫ��");
		String pswReq = setting.getString("passwordRequest");
		if (pswReq != null) {
			if (psw == null || (!psw.matches(pswReq))) {
				throw new ServiceException("����������Ҫ��" + Check.option(setting.getString("desc")).orElse(""));
			}
		}
		hostCol("user").insertOne(new Document("userId", userId).append("admin", false).append("buzAdmin", false).append("password", psw)
				.append("domain", domain).append("activated", true).append("changePSW", false));
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
	public long delete(ObjectId _id, String domain) {
		MongoCollection<Document> col = c("user", domain);
		Document doc = col.find(new BasicDBObject("_id", _id)).projection(new BasicDBObject("activated", 1).append("userId", 1)).first();
		if (doc.getBoolean("activated", false))
			throw new ServiceException("����ɾ������״̬���û���");

		String userId = doc.getString("userId");
		BasicDBObject filter = new BasicDBObject().append("managerId", userId);
		if (count(filter, Organization.class, domain) != 0)
			throw new ServiceException("����ɾ������֯�е��ι����ߵ��û���");

		// TODO �н�ɫ����Ҫ���

		// �����Դ�ƻ�
		if (count(new BasicDBObject("usedHumanResId", userId), ResourcePlan.class, domain) != 0)
			throw new ServiceException("����ɾ������Ŀ����Ϊ��Դ�ƻ����û���");
		// �����Դ����
		if (count(new BasicDBObject("usedHumanResId", userId), ResourceActual.class, domain) != 0)
			throw new ServiceException("����ɾ������Ŀ����Ϊ��Դ�������û���");

		// �����Ŀ�Ŷӳ�Ա
		if (count(new BasicDBObject("$or", Arrays.asList(new BasicDBObject("managerId", userId), new BasicDBObject("member", userId))),
				OBSItem.class, domain) != 0)
			throw new ServiceException("����ɾ�����뵽��Ŀ�е��û���");

		// TODO �������
		return delete(_id, User.class, domain);
	}

	@Override
	public List<User> createDeptUserDataSet(BasicDBObject condition, String userId, String domain) {
		// ��ȡ�����������֯�����¼���֯
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
		// ��ȡ�����������֯�����¼���֯
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
		// �Ȳ�ѯ��Ҫ��ʾ��Ա����֯��
		// �����û���Ϊ�����ߵ���֯
		List<ObjectId> orgIds = getDelegateableOrgIds(userId, domain);
		List<Bson> pipeline = Domain.getJQ(domain, "��ѯ-��Ա-��֯���¼�").set("ids", orgIds).array();
		appendConditionToPipeline(pipeline, condition);
		return c("organization", domain).aggregate(pipeline, User.class).into(new ArrayList<>());
	}

	private List<ObjectId> getDelegateableOrgIds(String userId, String domain) {
		List<ObjectId> orgIds = c("organization", domain).find(new Document("managerId", userId)).projection(new Document("_id", true))
				.map(d -> d.getObjectId("_id")).into(new ArrayList<>());
		// ����û����� ���ž��� ��ί���߽�ɫ����ѯ�û����ڵ�ֱ�Ӳ���
		boolean hasRolePermission = c("funcPermission", domain).countDocuments(
				new Document("id", userId).append("type", "�û�").append("role", new Document("$in", Arrays.asList("���ž���", "ί����")))) > 0;
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
		List<Bson> pipeline = Domain.getJQ(domain, "��ѯ-��Ա-��֯���¼�").set("ids", orgIds).array();
		if (filter != null)
			pipeline.add(new BasicDBObject("$match", filter));
		pipeline.add(Aggregates.count("count"));
		Document d = c("organization", domain).aggregate(pipeline).first();
		if (d == null)
			return 0;
		return ((Number) d.get("count")).longValue();
	}

}