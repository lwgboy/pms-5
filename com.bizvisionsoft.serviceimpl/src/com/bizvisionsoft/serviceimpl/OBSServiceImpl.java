package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.service.model.ScopeRoleParameter;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class OBSServiceImpl extends BasicServiceImpl implements OBSService {

	/**
	 * db.getCollection('project').aggregate([
	 * {$lookup:{"from":"obs","localField":"obs_id","foreignField":"_id","as":"obs"}},
	 * {$project:{"obs":true,"_id":false}},
	 * 
	 * {$replaceRoot: { newRoot: {$mergeObjects: [ { $arrayElemAt: [ "$obs", 0 ] },
	 * "$$ROOT" ] } }}, {$project:{"obs":false}} ])
	 */
	@Override
	public List<OBSItem> getScopeRootOBS(ObjectId scope_id) {
		return query(new BasicDBObject("scope_id", scope_id).append("scopeRoot", true));
	}

	private List<OBSItem> query(BasicDBObject match) {
		ArrayList<OBSItem> result = new ArrayList<OBSItem>();
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(match));

		appendUserInfoAndHeadPic(pipeline, "managerId", "managerInfo", "managerHeadPic");
		appendSortBy(pipeline, "seq", 1);
		c(OBSItem.class).aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public List<OBSItem> getSubOBSItem(ObjectId _id) {
		return query(new BasicDBObject("parent_id", _id));
	}

	@Override
	public List<OBSItem> getScopeOBS(ObjectId scope_id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", scope_id), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		List<ObjectId> ids = getDesentOBSItem(parentIds);
		return query(new BasicDBObject("_id", new BasicDBObject("$in", ids)));
	}

	private List<ObjectId> getDesentOBSItem(List<ObjectId> ids) {
		return getDesentItems(ids, "obs", "parent_id");
	}

	@Override
	public long countSubOBSItem(ObjectId _id) {
		return c(OBSItem.class).countDocuments(new BasicDBObject("parent_id", _id));
	}

	@Override
	public OBSItem insert(OBSItem obsItem) {
		return insert(obsItem, OBSItem.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, OBSItem.class);
	}

	@Override
	public OBSItem get(ObjectId _id) {
		return get(_id, OBSItem.class);
	}

	@Override
	public void delete(ObjectId _id) {
		// TODO Auto-generated method stub
		// 级联删除下级节点
		List<ObjectId> desentItems = getDesentItems(Arrays.asList(_id), "obs", "parent_id");
		c(OBSItem.class).deleteMany(new Document("_id", new Document("$in", desentItems)));
		// 不能删除的情况
		// delete(_id, OBSItem.class);
	}

	@Override
	public List<User> getMember(BasicDBObject condition, ObjectId obs_id) {
		List<String> userIds = getMemberUserId(obs_id);
		if (userIds.isEmpty()) {
			return new ArrayList<User>();
		} else {
			BasicDBObject filter = (BasicDBObject) condition.get("filter");
			if (filter == null) {
				filter = new BasicDBObject();
				condition.append("filter", filter);
			}
			if (!filter.containsField("userId")) {
				filter.append("userId", new BasicDBObject("$in", userIds));
			}
			return new UserServiceImpl().createDataSet(condition);
		}
	}

	private ArrayList<String> getMemberUserId(ObjectId obs_id) {
		ArrayList<String> result = new ArrayList<String>();
		c("obs").distinct("member", new BasicDBObject("_id", obs_id), String.class).into(result);
		return result;
	}

	@Override
	public long countMember(BasicDBObject filter, ObjectId obs_id) {
		List<String> userIds = getMemberUserId(obs_id);
		if (userIds == null) {
			return 0;
		} else {
			if (filter == null) {
				filter = new BasicDBObject();
			}
			if (!filter.containsField("userId")) {
				filter.append("userId", new BasicDBObject("$in", userIds));
			}
			return new UserServiceImpl().count(filter);
		}
	}

	@Override
	public int nextOBSSeq(BasicDBObject condition) {
		Document doc = c("obs").find(condition).sort(new BasicDBObject("seq", -1))
				.projection(new BasicDBObject("seq", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("seq", 0)).orElse(0) + 1;
	}

	@Override
	public List<String> getScopeRoleofUser(ObjectId scope_id, String userId) {
		ArrayList<String> result = c("obs").distinct("roleId",
				new Document("scope_id", scope_id).append("$or",
						Arrays.asList(new Document("managerId", userId), new Document("member", userId))),
				String.class).into(new ArrayList<>());
		if (c("obs").countDocuments(new Document("scope_id", scope_id).append("member", userId)) != 0) {
			result.add("member");
		}
		return result;
	}

	@Override
	public List<OBSItemWarpper> getOBSItemWarpper(BasicDBObject condition, ObjectId scope_id) {
		List<ObjectId> obsIds = c("obs").distinct("_id", new Document("scope_id", scope_id), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		obsIds = getDesentOBSItem(obsIds);
		JQ jq = new JQ("查询-成员-OBS").set("match", new Document("_id", new Document("$in", obsIds)));

		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			jq.set("filter", filter);
		else
			jq.set("filter", new Document());

		List<Bson> pipeline = jq.array();

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c("obs", OBSItemWarpper.class).aggregate(pipeline).into(new ArrayList<OBSItemWarpper>());
	}

	@Override
	public boolean checkScopeRole(ScopeRoleParameter param) {
		return c(OBSItem.class).countDocuments(new Document("scope_id", new Document("$in", param.scopes))
				.append("managerId", param.userId).append("roleId", new Document("$in", param.roles))) > 0;
	}

}
