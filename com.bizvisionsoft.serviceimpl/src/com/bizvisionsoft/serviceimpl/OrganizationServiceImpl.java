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
	public Organization insert(Organization orgInfo,String domain){
		return insert(orgInfo, Organization.class,domain);
	}

	@Override
	public Organization get(ObjectId _id,String domain){
		return get(_id, Organization.class,domain);
	}

	@Override
	public List<Organization> createDataSet(BasicDBObject condition,String domain){
		return createDataSet(condition, Organization.class,domain);
	}

	@Override
	public long count(BasicDBObject filter,String domain){
		return count(filter, Organization.class,domain);
	}

	@Override
	public long update(BasicDBObject fu,String domain){
		return update(fu, Organization.class,domain);
	}

	@Override
	public List<Organization> getRoot(String domain){
		return getOrganizations(new BasicDBObject("parent_id", null).append("project_id", null), domain);
	}

	@Override
	public long countRoot(String domain){
		return countOrganizations(new BasicDBObject("parent_id", null).append("project_id", null), domain);
	}

	@Override
	public List<Organization> getSub(ObjectId parent_id,String domain){
		return getOrganizations(new BasicDBObject("parent_id", parent_id), domain);
	}

	private List<Organization> getOrganizations(BasicDBObject match,String domain){
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(match));

		appendUserInfo(pipeline, "managerId", "managerInfo", domain);

		List<Organization> result = new ArrayList<Organization>();
		c(Organization.class,domain).aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public long countSub(ObjectId parent_id,String domain){
		return countOrganizations(new BasicDBObject("parent_id", parent_id), domain);
	}

	private long countOrganizations(BasicDBObject match,String domain){
		return c(Organization.class,domain).countDocuments(match);
	}

	public long countMember(ObjectId _id,String domain){
		return c(User.class,domain).countDocuments(new BasicDBObject("org_id", _id));
	}

	@Override
	public long delete(ObjectId _id,String domain){
		// 检查
		if (countSub(_id, domain) > 0)
			throw new ServiceException("不允许删除有下级的组织");

		if (countMember(_id, domain) > 0)
			throw new ServiceException("不允许删除有成员的组织");

		// TODO 完整性问题
		return delete(_id, "organization",domain);
	}

	@Override
	public List<User> getMember(BasicDBObject condition, ObjectId org_id,String domain){
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null){
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("org_id", org_id);
		return createDataSet(condition, User.class,domain);
	}

	@Override
	public long countMember(BasicDBObject filter, ObjectId org_id,String domain){
		if (filter == null){
			filter = new BasicDBObject();
		}
		filter.put("org_id", org_id);
		return count(filter, User.class,domain);
	}

	@Override
	public List<Role> getRoles(BasicDBObject condition, ObjectId org_id,String domain){
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null){
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("org_id", org_id);
		return createDataSet(condition, Role.class,domain);
	}

	@Override
	public long countRoles(BasicDBObject filter, ObjectId org_id,String domain){
		if (filter == null){
			filter = new BasicDBObject();
		}
		filter.put("org_id", org_id);
		return count(filter, Role.class,domain);
	}

	@Override
	public Role insertRole(Role role,String domain){
		return insert(role, Role.class,domain);
	}

	@Override
	public Role getRole(ObjectId _id,String domain){
		return get(_id, Role.class,domain);
	}

	@Override
	public long updateRole(BasicDBObject fu,String domain){
		return update(fu, Role.class,domain);
	}

	@Override
	public long deleteRole(ObjectId _id,String domain){
		return delete(_id, Role.class,domain);
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
	public List<User> queryUsersOfRole(ObjectId _id,String domain){
		List<Bson> pipeline = new ArrayList<>();

		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));

		pipeline.add(Aggregates.unwind("$users"));

		pipeline.add(Aggregates.lookup("user", "users", "userId", "user"));

		pipeline.add(Aggregates.project(new BasicDBObject("user", true).append("_id", false)));

		pipeline.add(Aggregates.replaceRoot(new BasicDBObject("$mergeObjects",
				new Object[] { new BasicDBObject("$arrayElemAt", new Object[] { "$user", 0 }), "$$ROOT" })));

		pipeline.add(Aggregates.project(new BasicDBObject("user", false)));

		pipeline.add(Aggregates.sort(new BasicDBObject("userId", 1)));

		List<User> result = new ArrayList<User>();
		c(Role.class,domain).aggregate(pipeline, User.class).into(result);
		return result;
	}

	@Override
	public long countUsersOfRole(ObjectId _id,String domain){
		Document doc = c("role",domain).find(new BasicDBObject("_id", _id)).first();
		if (doc == null){
			throw new ServiceException("没有指定id的角色");
		}
		Object users = doc.get("users");
		if (users instanceof List<?>)
			return ((List<?>) users).size();
		return 0;
	}

	@Override
	public List<Organization> listQualifiedContractor(BasicDBObject condition,String domain){
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null){
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("qualifiedContractor", true);
		return createDataSet(condition, Organization.class,domain);
	}

	@Override
	public long countQualifiedContractor(BasicDBObject filter,String domain){
		if (filter == null)
			filter = new BasicDBObject();
		filter.append("qualifiedContractor", true);
		return count(filter, Organization.class,domain);
	}

	@Override
	public List<ObjectId> getManagedOrganizationsId(String userId,String domain){
		return c("organization",domain).find(new Document("managerId", userId)).projection(new Document("_id", true))
				.map(d -> d.getObjectId("_id")).into(new ArrayList<>());
	}

}
