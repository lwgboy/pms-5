package com.bizvisionsoft.pms.formdef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.service.tools.Check;

public class FormDefAssemblyFieldDS {
	@Inject
	private BruiAssemblyContext context;

	private List<Document> input;

	@SuppressWarnings("unchecked")
	@Init
	private void init() {
		input = new ArrayList<Document>();
		Map<String, String> map = (Map<String, String>) context.getRootInput();
		if (Check.isAssigned(map))
			for (String key : map.keySet())
				input.add(new Document("name", key).append("text", map.get(key)));

	}

	@DataSet(DataSet.LIST)
	private List<Document> getField() {
		return input;
	}

	@DataSet(DataSet.COUNT)
	private long countField() {
		return input.size();
	}

}
