package com.bizvisionsoft.pms.formdef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.tools.Check;
import com.mongodb.BasicDBObject;

public class FormDefAssemblyFieldDS {
	@Inject
	private BruiAssemblyContext context;

	private List<BasicDBObject> input;

	@SuppressWarnings("unchecked")
	@Init
	private void init() {
		input = new ArrayList<BasicDBObject>();
		Map<String, String> map = (Map<String, String>) context.getRootInput();
		if (Check.isAssigned(map))
			for (String key : map.keySet())
				input.add(new BasicDBObject("name", key).append("text", map.get(key)));

	}

	@DataSet(DataSet.LIST)
	private List<BasicDBObject> getField(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		return BsonTools.appendCondition(input.stream(), condition).collect(Collectors.toList());
	}

	@DataSet(DataSet.COUNT)
	private long countField(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		return BsonTools.appendFilter(input.stream(), filter).count();
	}

}
