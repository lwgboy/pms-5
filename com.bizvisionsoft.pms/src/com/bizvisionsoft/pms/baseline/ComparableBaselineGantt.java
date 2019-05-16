package com.bizvisionsoft.pms.baseline;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.WorkLink;

public class ComparableBaselineGantt {
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private List<ObjectId> projectIds;

	@Init
	private void init() {
		projectIds = new ArrayList<ObjectId>();
		ObjectId[] input = (ObjectId[]) context.getInput();
		projectIds.add(input[0]);
		projectIds.add(input[1]);
		
		
	}

	@DataSet("data")
	public List<?> data() {
		return ServicesLoader.get(ProjectService.class).getBaselineComparable(projectIds,br.getDomain());
	}

	@DataSet("links")
	public List<?> links() {
		return new ArrayList<WorkLink>();
	}
}
