package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RBSType;
import com.mongodb.BasicDBObject;

public class RiskServiceImpl extends BasicServiceImpl implements RiskService {

	@Override
	public List<RBSType> getRBSType() {
		return c(RBSType.class).find().into(new ArrayList<RBSType>());
	}

	@Override
	public RBSType insertRBSType(RBSType item) {
		return insert(item, RBSType.class);
	}

	@Override
	public long deleteRBSType(ObjectId _id) {
		return delete(_id, RBSType.class);
	}

	@Override
	public long updateRBSType(BasicDBObject fu) {
		return update(fu, RBSType.class);
	}

	@Override
	public List<RBSItem> listRBSItem(BasicDBObject condition) {
		return createDataSet(condition, RBSItem.class);
	}

	@Override
	public long countRBSItem(BasicDBObject filter) {
		return c("rbsItem").count(filter);
	}

	@Override
	public RBSItem insertRBSItem(RBSItem item) {
		//
		ObjectId pj_id = item.getProject_id();
		String typeId = item.getRbsType().getId();
		int idx = nextRBSItemIndex(pj_id,typeId);
		item.setIndex(idx);
		return insert(item, RBSItem.class);
	}

	public int nextRBSItemIndex(ObjectId project_id, String type_id) {
		Document doc = c("rbsItem").find(new BasicDBObject("project_id", project_id).append("rbsType.id", type_id))
				.sort(new BasicDBObject("index", -1)).projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

}
