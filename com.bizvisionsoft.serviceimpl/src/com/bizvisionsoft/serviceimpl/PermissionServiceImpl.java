package com.bizvisionsoft.serviceimpl;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.PermissionService;
import com.bizvisionsoft.service.model.FuncPermission;
import com.mongodb.BasicDBObject;

public class PermissionServiceImpl extends BasicServiceImpl implements PermissionService {

	@Override
	public FuncPermission insertFunctionPermission(FuncPermission fp) {
		return insert(fp);
	}

	@Override
	public long updateFunctionPermission(BasicDBObject fu) {
		return update(fu, FuncPermission.class);
	}

	@Override
	public List<FuncPermission> listFunctionPermission(BasicDBObject condition) {
		return createDataSet(condition, FuncPermission.class);
	}

	@Override
	public long countFunctionPermission(BasicDBObject filter) {
		return count(filter, FuncPermission.class);
	}

	@Override
	public long deleteFunctionPermission(ObjectId _id) {
		return delete(_id, FuncPermission.class);
	}

	@Override
	public FuncPermission getFunctionPermission(ObjectId _id) {
		return get(_id, FuncPermission.class);
	}

}
