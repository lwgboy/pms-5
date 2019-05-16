package com.bizvisionsoft.serviceimpl;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.PermissionService;
import com.bizvisionsoft.service.model.FuncPermission;
import com.mongodb.BasicDBObject;

public class PermissionServiceImpl extends BasicServiceImpl implements PermissionService {

	@Override
	public FuncPermission insertFunctionPermission(FuncPermission fp,String domain){
		return insert(fp,domain);
	}

	@Override
	public long updateFunctionPermission(BasicDBObject fu,String domain){
		return update(fu, FuncPermission.class,domain);
	}

	@Override
	public List<FuncPermission> listFunctionPermission(BasicDBObject condition,String domain){
		return createDataSet(condition, FuncPermission.class,domain);
	}

	@Override
	public long countFunctionPermission(BasicDBObject filter,String domain){
		return count(filter, FuncPermission.class,domain);
	}

	@Override
	public long deleteFunctionPermission(ObjectId _id,String domain){
		return delete(_id, FuncPermission.class,domain);
	}

	@Override
	public FuncPermission getFunctionPermission(ObjectId _id,String domain){
		return get(_id, FuncPermission.class,domain);
	}

}
