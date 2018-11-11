package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.TraceInfo;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class UserServiceImpl extends BasicServiceImpl implements UserService {

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, User.class);
	}

	@Override
	public List<User> listConsigned(String userId) {
		List<User> ds = createDataSet(new BasicDBObject().append("skip", 0).append("filter", new BasicDBObject("consigner", userId)));
		ds.forEach(user -> updateRole(user));
		return ds;
	}

	@Override
	public long updatePassword(String userId, String newPassword) {
		UpdateResult r = c("user").updateOne(new Document("userId", userId),
				new Document("$set", new Document("password", newPassword).append("changePSW", false)));
		return r.getModifiedCount();
	}

	@Override
	public User check(String userId, String password) {
		BasicDBObject filter = new BasicDBObject("userId", userId);
		if (!logger.isDebugEnabled()) {
			filter.append("password", password);
		} else {
			logger.debug("已忽略密码验证。");
		}

		List<User> ds = createDataSet(new BasicDBObject().append("skip", 0).append("limit", 1).append("filter", filter));
		if (ds.size() == 0) {
			throw new ServiceException("账户无法通过验证");
		}

		User user = ds.get(0);

		updateRole(user);

		return user;

	}

	private void updateRole(User user) {
		// 获得user所在的各级组织
		ObjectId org_id = user.getOrganizationId();
		List<String> orgIds = new ArrayList<String>();
		if (org_id != null) {
			Optional.ofNullable(c("organization").find(new Document("_id", org_id)).first()).ifPresent(doc -> {
				while (doc != null) {
					orgIds.add(doc.getString("id"));
					doc = Optional.ofNullable(doc.getObjectId("parent_id"))
							.map(parentId -> c("organization").find(new Document("_id", parentId)).first()).orElse(null);
				}
			});
		}

		ArrayList<String> functionPermissions = c(
				"funcPermission")
						.distinct("role",
								new Document("$or",
										Arrays.asList(new Document("type", "组织").append("id", new Document("$in", orgIds)),
												new Document("type", "用户").append("id", user.getUserId()))),
								String.class)
						.into(new ArrayList<>());

		user.setRoles(functionPermissions);
	}

	@Override
	public User get(String userId) {
		List<User> ds = createDataSet(
				new BasicDBObject().append("skip", 0).append("limit", 1).append("filter", new BasicDBObject("userId", userId)));
		if (ds.size() == 0) {
			throw new ServiceException("没有用户Id为" + userId + "的用户。");
		}
		return ds.get(0);
	}

	@Override
	public User insert(User user) {
		return insert(user, User.class);
	}

	@Override
	public List<User> createDataSet(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		return query(skip, limit, filter);
	}

	/**
	 * db.getCollection('account').aggregate([ {$lookup:{
	 * "from":"organization","localField":"org_id","foreignField":"_id","as":"org"}},
	 * {$unwind:{"path":"$org",preserveNullAndEmptyArrays:true}},
	 * {$addFields:{"orgFullName":"$org.fullName"}}, {$project:{"org":0}} ])
	 * 
	 * @param skip
	 * @param limit
	 * @param filter
	 * @return
	 */

	private List<User> query(Integer skip, Integer limit, BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendOrgFullName(pipeline, "org_id", "orgFullName");

		return c(User.class).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, User.class);
	}

	@Override
	public long delete(ObjectId _id) {
		MongoCollection<Document> col = c("user");
		Document doc = col.find(new BasicDBObject("_id", _id)).projection(new BasicDBObject("activated", 1).append("userId", 1)).first();
		if (doc.getBoolean("activated", false))
			throw new ServiceException("不能删除激活状态的用户。");

		String userId = doc.getString("userId");
		BasicDBObject filter = new BasicDBObject().append("managerId", userId);
		if (count(filter, Organization.class) != 0)
			throw new ServiceException("不能删除在组织中担任管理者的用户。");

		// TODO 有角色的需要清除

		// TODO 其他检查
		return delete(_id, User.class);
	}

	@Override
	public List<User> createDeptUserDataSet(BasicDBObject condition, String userId) {
		// 获取我所管理的组织及其下级组织
		List<ObjectId> orgIds = c("organization").distinct("_id", new Document("managerId", userId), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		orgIds = getDesentItems(orgIds, "organization", "parent_id");

		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(new BasicDBObject());

		if (orgIds.size() > 0) {
			filter.append("org_id", new BasicDBObject("$in", orgIds));
			return query(null, null, filter);
		} else
			return new ArrayList<User>();
	}

	@Override
	public long countDeptUser(BasicDBObject filter, String userId) {
		// 获取我所管理的组织及其下级组织
		List<ObjectId> orgIds = c("organization").distinct("_id", new Document("managerId", userId), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		orgIds = getDesentItems(orgIds, "organization", "parent_id");

		if (filter == null)
			filter = new BasicDBObject();

		if (orgIds.size() > 0) {
			filter.append("org_id", new BasicDBObject("$in", orgIds));
			return count(filter);
		} else
			return 0;
	}

	@Override
	public void consign(String userId, String consignerId) {
		c("user").updateOne(new Document("userId", userId), new Document("$set", new Document("consigner", consignerId)));
	}

	@Override
	public void disconsign(String userId) {
		c("user").updateOne(new Document("userId", userId), new Document("$set", new Document("consigner", null)));
	}

	@Override
	public void trace(String userId, boolean trace) {
		c("user").updateOne(new Document("userId", userId), new Document("$set", new Document("trace", trace)));
	}

	@Override
	public void insertTraceInfo(TraceInfo traceInfo) {
		insert(traceInfo);
	}

	@Override
	public List<TraceInfo> listTraceInfo(BasicDBObject condition) {
		return createDataSet(condition, TraceInfo.class);
	}

	@Override
	public long countTraceInfo(BasicDBObject filter) {
		return count(filter, TraceInfo.class);
	}

	@Override
	public void requestChangePassword(List<String> userIds) {
		c("user").updateMany(new Document("userId", new Document("$in", userIds)), new Document("$set", new Document("changePSW", true)));
	}

	@Override
	public void requestAllChangePassword() {
		c("user").updateMany(new Document(), new Document("$set", new Document("changePSW", true)));
	}

}