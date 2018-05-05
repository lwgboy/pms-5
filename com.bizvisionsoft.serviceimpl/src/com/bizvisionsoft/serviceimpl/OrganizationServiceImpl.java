package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class OrganizationServiceImpl extends BasicServiceImpl implements OrganizationService {

	@Override
	public Organization insert(Organization orgInfo) {
		return insert(orgInfo, Organization.class);
	}

	@Override
	public Organization get(ObjectId _id) {
		return get(_id, Organization.class);
	}

	@Override
	public List<Organization> createDataSet(BasicDBObject condition) {
		return createDataSet(condition, Organization.class);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, Organization.class);
	}

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, Organization.class);
	}

	@Override
	public List<Organization> getRoot() {
		return getOrganizations(new BasicDBObject("parent_id", null).append("project_id", null));
	}

	@Override
	public long countRoot() {
		return countOrganizations(new BasicDBObject("parent_id", null).append("project_id", null));
	}

	/**
	 * 
	 * db.getCollection('organization').aggregate( [
	 * {"$lookup":{"from":"user","localField":"managerId","foreignField":"userId","as":"user"}}
	 * , {"$unwind":{"path":"$user","preserveNullAndEmptyArrays":true}} ,
	 * {"$addFields":{"managerInfo":{"$concat":["$user.name","
	 * [","$user.userId","]"]}}} , {"$project":{"user":0}} ])
	 * 
	 */
	@Override
	public List<Organization> getSub(ObjectId parent_id) {
		return getOrganizations(new BasicDBObject("parent_id", parent_id));
	}

	private List<Organization> getOrganizations(BasicDBObject match) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(match));

		appendUserInfo(pipeline,"managerId","managerInfo");

		List<Organization> result = new ArrayList<Organization>();
		c(Organization.class).aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public long countSub(ObjectId parent_id) {
		return countOrganizations(new BasicDBObject("parent_id", parent_id));
	}

	private long countOrganizations(BasicDBObject match) {
		return c(Organization.class).count(match);
	}

	public long countMember(ObjectId _id) {
		return c(User.class).count(new BasicDBObject("org_id", _id));
	}

	@Override
	public long delete(ObjectId _id) {
		// 检查
		if (countSub(_id) > 0)
			throw new ServiceException("不允许删除有下级的组织");

		if (countMember(_id) > 0)
			throw new ServiceException("不允许删除有成员的组织");

		// TODO 完整性问题
		return delete(_id, Organization.class);
	}

	@Override
	public List<User> getMember(BasicDBObject condition, ObjectId org_id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("org_id", org_id);
		return createDataSet(condition, User.class);
	}

	@Override
	public long countMember(BasicDBObject filter, ObjectId org_id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("org_id", org_id);
		return count(filter, User.class);
	}

	@Override
	public List<Role> getRoles(BasicDBObject condition, ObjectId org_id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("org_id", org_id);
		return createDataSet(condition, Role.class);
	}

	@Override
	public long countRoles(BasicDBObject filter, ObjectId org_id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("org_id", org_id);
		return count(filter, Role.class);
	}

	@Override
	public Role insertRole(Role role) {
		return insert(role, Role.class);
	}

	@Override
	public Role getRole(ObjectId _id) {
		return get(_id, Role.class);
	}

	@Override
	public long updateRole(BasicDBObject fu) {
		return update(fu, Role.class);
	}

	@Override
	public long deleteRole(ObjectId _id) {
		return delete(_id, Role.class);
	}

	/**
	 * db.getCollection('role').aggregate( [
	 * {$match:{_id:ObjectId("5ad1366585e0fb292c30cb26")}},
	 * {$unwind:{"path":"$users"}},
	 * {$lookup:{"from":"user","localField":"users","foreignField":"userId","as":"user"}},
	 * {$project:{"user":1,"_id":0}},
	 * 
	 * {$replaceRoot: { newRoot: { $mergeObjects: [ { $arrayElemAt: [ "$user", 0 ]},
	 * "$$ROOT" ] } }},
	 * 
	 * {$project:{"user":0}} ] )
	 */
	@Override
	public List<User> queryUsersOfRole(ObjectId _id) {
		List<Bson> pipeline = new ArrayList<>();

		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));

		pipeline.add(Aggregates.unwind("$users"));

		pipeline.add(Aggregates.lookup("user", "users", "userId", "user"));

		pipeline.add(Aggregates.project(new BasicDBObject("user", true).append("_id", false)));

		pipeline.add(Aggregates.replaceRoot(new BasicDBObject("$mergeObjects",
				new Object[] { new BasicDBObject("$arrayElemAt", new Object[] { "$user", 0 }), "$$ROOT" })));

		pipeline.add(Aggregates.project(new BasicDBObject("user", false)));
		
		pipeline.add(Aggregates.sort(new BasicDBObject("userId",1)));

		List<User> result = new ArrayList<User>();
		c(Role.class).aggregate(pipeline, User.class).into(result);
		return result;
	}

	@Override
	public long countUsersOfRole(ObjectId _id) {
		Document doc = c("role").find(new BasicDBObject("_id", _id)).first();
		if (doc == null) {
			throw new ServiceException("没有指定id的角色");
		}
		Object users = doc.get("users");
		if (users instanceof List<?>)
			return ((List<?>) users).size();
		return 0;
	}

}
