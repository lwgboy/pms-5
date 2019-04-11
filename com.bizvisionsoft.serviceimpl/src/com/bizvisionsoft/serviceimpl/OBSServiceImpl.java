package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.common.query.JQ;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.ScopeRoleParameter;
import com.bizvisionsoft.service.model.User;
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
		// TODO 如果启用阶段团队，此处获取scopeRoot出现错误。
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

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllSubOBSItemMember(ObjectId obs_id) {
		Set<String> result = new HashSet<String>();
		lookupDesentItems(Arrays.asList(obs_id), "obs", "parent_id", true).forEach((Document d) -> {
			Object managerId = d.get("managerId");
			if (managerId != null)
				result.add((String) managerId);

			Object member = d.get("member");
			if (member instanceof List)
				result.addAll((Collection<? extends String>) member);
		});
		return new ArrayList<>(result);
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
		Document doc = c("obs").find(condition).sort(new BasicDBObject("seq", -1)).projection(new BasicDBObject("seq", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("seq", 0)).orElse(0) + 1;
	}

	@Override
	public List<String> getScopeRoleofUser(ObjectId scope_id, String userId) {
		ArrayList<String> result = c("obs")
				.distinct("roleId",
						new Document("scope_id", scope_id).append("$or",
								Arrays.asList(new Document("managerId", userId), new Document("member", userId))),
						String.class)
				.into(new ArrayList<>());
		if (c("obs").countDocuments(new Document("scope_id", scope_id).append("member", userId)) != 0) {
			result.add("member");
		}
		return result;
	}

	/**
	 * 根据角色ID获取范围内的角色（包括团队）
	 */
	@Override
	public List<OBSItem> getScopeRoleofRoleId(ObjectId scope_id, String roleId) {
		return query(new BasicDBObject("scope_id", scope_id).append("roleId", roleId));
	}

	/**
	 * 根据角色ID获取范围内的团队
	 * 
	 * @param scope_id
	 * @param roleId
	 * @return
	 */
	public List<OBSItem> getScopeTeamofRoleId(ObjectId scope_id, String roleId) {
		return query(new BasicDBObject("scope_id", scope_id).append("roleId", roleId).append("isRole", false));
	}

	@Override
	public List<OBSItemWarpper> getOBSItemWarpper(BasicDBObject condition, ObjectId scope_id) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		List<Bson> pipeline = getOBSItemWarpperPipeline(scope_id, filter);

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c("obs", OBSItemWarpper.class).aggregate(pipeline).into(new ArrayList<OBSItemWarpper>());
	}

	private List<Bson> getOBSItemWarpperPipeline(ObjectId scope_id, BasicDBObject filter) {
		List<ObjectId> obsIds = c("obs").distinct("_id", new Document("scope_id", scope_id), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		obsIds = getDesentOBSItem(obsIds);
		JQ jq = new JQ("查询-成员-OBS").set("match", new Document("_id", new Document("$in", obsIds)));

		if (filter != null)
			jq.set("filter", filter);
		else
			jq.set("filter", new Document());

		return jq.array();
	}

	@Override
	public long countOBSItemWarpper(BasicDBObject filter, ObjectId scope_id) {
		List<Bson> pipeline = getOBSItemWarpperPipeline(scope_id, filter);
		return c("obs").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public boolean checkScopeRole(ScopeRoleParameter param) {
		return c(OBSItem.class).countDocuments(new Document("scope_id", new Document("$in", param.scopes)).append("managerId", param.userId)
				.append("roleId", new Document("$in", param.roles))) > 0;
	}

	@Override
	public List<OBSItem> addOBSModule(ObjectId module_id, ObjectId obsParent_id, boolean cover) {
		// 准备模板对象和新对象之间的id对应表
		Map<ObjectId, ObjectId> idMap = new HashMap<ObjectId, ObjectId>();
		// 保存准备插入数据库的记录
		List<Document> tobeInsert = new ArrayList<>();
		// 项目的OBS_ID
		Document parent = c("obs").find(new Document("_id", obsParent_id)).first();
		ObjectId scope_id = parent.getObjectId("scope_id");

		// 得到重复角色
		List<String> repeatRole = getRepeatRole(module_id, scope_id);

		// 创建组织结构
		c("obsInTemplate").find(new Document("scope_id", module_id)).sort(new Document("_id", 1)).forEach((Document doc) -> {
			// 建立项目团队上下级关系
			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id == null) {
				idMap.put(doc.getObjectId("_id"), obsParent_id);
			} else {
				ObjectId newParent_id = idMap.get(parent_id);
				if (newParent_id != null) {
					ObjectId _id = new ObjectId();
					idMap.put(doc.getObjectId("_id"), _id);
					doc.append("_id", _id).append("scope_id", scope_id).append("parent_id", newParent_id);
					String roleId = doc.getString("roleId");
					if (!cover && repeatRole.contains(roleId)) {
						return;
					}
					tobeInsert.add(doc);
				}
			}

		});
		List<OBSItem> result = new ArrayList<OBSItem>();
		// 插入项目团队
		if (!tobeInsert.isEmpty()) {
			c("obs").insertMany(tobeInsert);
			// 通过查询获取OBSItem
			Collection<ObjectId> ids = idMap.values();
			ids.remove(obsParent_id);
			result = query(new BasicDBObject("_id", new BasicDBObject("$in", ids)));
		}

		return result;
	}

	/**
	 * 检查模板中的角色在scope范围中是否重复
	 */
	@Override
	public boolean isRoleNumberDuplicated(ObjectId module_id, ObjectId scope_id) {
		List<String> repeatRole = getRepeatRole(module_id, scope_id);
		return repeatRole.size() > 0;
	}

	/**
	 * 得到重复角色
	 * 
	 * @param module_id
	 * @param scope_id
	 * @return
	 */
	private List<String> getRepeatRole(ObjectId module_id, ObjectId scope_id) {
		// 使用JS进行查询，获取scope范围内存在与组织模板中的角色id。
		List<String> result = new ArrayList<String>();
		c("obs").aggregate(new JQ("查询-OBS-组织模板中重复的角色").set("scope_id", scope_id).set("module_id", module_id).array())
				.forEach((Document doc) -> {
					result.add(doc.getString("roleId"));
				});
		return result;
	}

	/**
	 * 从项目团队中移除人员检查
	 */
	@Override
	public List<Result> deleteProjectMemberCheck(ObjectId _id, String type) {
		List<Result> results = new ArrayList<Result>();
		OBSItem obsItem = get(_id);
		ObjectId scope_id = obsItem.getScope_id();
		Set<String> userIds = new HashSet<String>();
		List<ObjectId> obs_id = new ArrayList<ObjectId>();
		if (type.startsWith("appointmentobsitem")) {
			obs_id.add(_id);
			userIds.add(obsItem.getManagerId());
		} else if (type.startsWith("editobsitem")) {
			obs_id.add(_id);
			userIds.add(obsItem.getManagerId());
		} else if (type.startsWith("removeobsitemmember")) {
			obs_id.add(_id);
			userIds.add(type.replace("removeobsitemmember@", ""));
		} else if (type.startsWith("deleteobsitem")) {
			lookupDesentItems(Arrays.asList(_id), "obs", "parent_id", true).forEach((Document d) -> {
				obs_id.add(d.getObjectId("_id"));
				Object managerId = d.get("managerId");
				if (managerId != null)
					userIds.add((String) managerId);

				Object member = d.get("member");
				if (member instanceof List)
					userIds.addAll((List<String>) member);
			});
		}
		// 判断人员是否在都在项目团队中
		c("obs").find(new Document("scope_id", scope_id).append("_id", new Document("$nin", obs_id)).append("$or", Arrays
				.asList(new Document("managerId", new Document("$in", userIds)), new Document("member", new Document("$in", userIds)))))
				.forEach((Document d) -> {
					Object managerId = d.get("managerId");
					if (managerId != null)
						userIds.remove((String) managerId);

					Object member = d.get("member");
					if (member instanceof List)
						userIds.removeAll((List<String>) member);
				});
		if (userIds.size() > 0) {
			// TODO 没考虑可以建立EPS团队的情况
			List<OBSItem> rootOBS = getScopeRootOBS(scope_id);
			if (rootOBS.size() > 0) {
				ObjectId project_id = rootOBS.get(0).getScope_id();
				Map<String, Set<String>> error = new HashMap<String, Set<String>>();
				Map<String, Set<String>> warning = new HashMap<String, Set<String>>();
				// 检查进行中的工作
				c("work").find(
						new Document("project_id", project_id).append("actualFinish", null).append("actualStart", new Document("$ne", null))
								.append("$or", Arrays.asList(new Document("chargerId", new Document("$in", userIds)),
										new Document("assignerId", new Document("$in", userIds)))))
						.forEach((Document d) -> {
							String userid;
							userid = d.getString("chargerId");
							if (!userIds.contains(userid))
								userid = d.getString("assignerId");
							String userName = getUserName(userid);
							Set<String> fullname = error.get(userName);
							if (fullname == null) {
								fullname = new HashSet<String>();
								error.put(userName, fullname);
							}
							fullname.add(d.getString("fullName"));

						});
				// 检查工作区进行中的工作
				c("workspace").find(
						new Document("project_id", project_id).append("actualFinish", null).append("actualStart", new Document("$ne", null))
								.append("$or", Arrays.asList(new Document("chargerId", new Document("$in", userIds)),
										new Document("assignerId", new Document("$in", userIds)))))
						.forEach((Document d) -> {
							String userid;
							userid = d.getString("chargerId");
							if (!userIds.contains(userid))
								userid = d.getString("assignerId");
							String userName = getUserName(userid);
							Set<String> fullname = error.get(userName);
							if (fullname == null) {
								fullname = new HashSet<String>();
								error.put(userName, fullname);
							}
							fullname.add(d.getString("fullName"));
						});

				// 检查未开始的工作
				c("work").find(new Document("project_id", project_id).append("actualStart", null).append("$or", Arrays.asList(
						new Document("chargerId", new Document("$in", userIds)), new Document("assignerId", new Document("$in", userIds)))))
						.forEach((Document d) -> {
							String userid;
							userid = d.getString("chargerId");
							if (!userIds.contains(userid))
								userid = d.getString("assignerId");
							String userName = getUserName(userid);
							Set<String> fullname = warning.get(userName);
							if (fullname == null) {
								fullname = new HashSet<String>();
								warning.put(userName, fullname);
							}
							fullname.add(d.getString("fullName"));

						});

				// 检查工作区未开始的工作
				c("workspace").find(new Document("project_id", project_id).append("actualStart", null).append("$or", Arrays.asList(
						new Document("chargerId", new Document("$in", userIds)), new Document("assignerId", new Document("$in", userIds)))))
						.forEach((Document d) -> {
							String userid;
							userid = d.getString("chargerId");
							if (!userIds.contains(userid))
								userid = d.getString("assignerId");
							String userName = getUserName(userid);
							Set<String> fullname = warning.get(userName);
							if (fullname == null) {
								fullname = new HashSet<String>();
								warning.put(userName, fullname);
							}
							fullname.add(d.getString("fullName"));
						});
				error.forEach((u, f) -> {
					f.forEach(w -> {
						results.add(Result.error(u + " 参与的工作：" + w + " 需要移交。"));
					});
				});
				warning.forEach((u, f) -> {
					f.forEach(w -> {
						results.add(Result.warning("从项目组中移除:" + u + " ，将取消工作区中他作为工作：" + w + " 参与者的任命。")
								.setResultDate(new BasicDBObject("userIds", userIds)));
					});
				});
			}
		}
		return results;
	}

	@Override
	public void removeUnStartWorkUser(OBSItem obsItem, String currentId) {
		List<String> userIds = getAllSubOBSItemMember(obsItem.get_id());
		new WorkServiceImpl().removeUnStartWorkUser(userIds, obsItem.getScope_id(), currentId);
	}

}
