package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;

public class UserServiceImpl extends BasicServiceImpl implements UserService {

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, User.class);
	}

	@Override
	public User check(String userId, String password) {
		User user = c(User.class).find(new BasicDBObject("userId", userId).append("password", password))
				.first();
		if (user != null && user.isActivated()) {
			return user;
		}
		throw new ServiceException("账户无法通过验证");
	}

	@Override
	public User get(String userId) {
		List<User> ds = createDataSet(new BasicDBObject().append("skip", 0).append("limit", 1).append("filter",
				new BasicDBObject("userId", userId)));
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

		appendOrgFullName(pipeline,"org_id","orgFullName");

		List<User> result = new ArrayList<User>();
		c(User.class).aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, User.class);
	}

	@Override
	public long delete(ObjectId _id) {
		MongoCollection<Document> col = c("user");
		Document doc = col.find(new BasicDBObject("_id", _id))
				.projection(new BasicDBObject("activated", 1).append("userId", 1)).first();
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

}