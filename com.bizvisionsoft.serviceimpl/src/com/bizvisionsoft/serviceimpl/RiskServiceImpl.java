package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RBSType;
import com.mongodb.BasicDBObject;

public class RiskServiceImpl extends BasicServiceImpl implements RiskService {

	@Override
	public List<RBSType> getRBSItem() {
		return c(RBSType.class).find().into(new ArrayList<RBSType>());
	}

	@Override
	public RBSType insertRBSItem(RBSType item) {
		return insert(item, RBSType.class);
	}

	@Override
	public long deleteRBSItem(ObjectId _id) {
		return delete(_id, RBSType.class);
	}

	@Override
	public long updateRBSItem(BasicDBObject fu) {
		return update(fu, RBSType.class);
	}

	@Override
	public List<RBSItem> listRBSItem(BasicDBObject condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countRBSItem(BasicDBObject filter) {
		return c("rbsItem").count(filter);
	}

}
