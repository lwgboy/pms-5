package com.bizvisionsoft.pms.obs;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.IOBSScope;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.mongodb.BasicDBObject;

//TODO ÐÞ¸ÄÎªService
public class OBSItemDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private ObjectId obsScope_id;

	@Init
	private void init() {
		IOBSScope rootInput = (IOBSScope) context.getRootInput();
		obsScope_id = rootInput.getScope_id();
	}

	@DataSet(DataSet.LIST)
	public List<OBSItemWarpper> listOBSItem(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		return ServicesLoader.get(OBSService.class).getOBSItemWarpper(condition, obsScope_id, br.getDomain());
	}
	

	@DataSet(DataSet.COUNT)
	public long countOBSItem(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		return ServicesLoader.get(OBSService.class).countOBSItemWarpper(filter, obsScope_id, br.getDomain());
	}

}
