package com.bizvisionsoft.pms.problem.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class GeneratePCA {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_CONTENT) GridPart grid) {
		String lang = RWT.getLocale().getLanguage();
		ObjectId problem_id = problem.get_id();

		ProblemService service = Services.get(ProblemService.class);
		Document d5 = service.listD5PCA(problem_id, lang).stream().filter(d -> d.getBoolean("selected", false)).findFirst().orElse(null);
		if (d5 == null) {
			Layer.error("没有可供选择的永久纠正措施方案");
			return;
		}
		List<Document> actions = new ArrayList<>();

		
		// pca1
		List<?> pcaList = (List<?>) d5.get("pca1");
		Document charger_meta = (Document) d5.get("charger1_meta");
		String charger = d5.getString("charger1");
		Date planStart = d5.getDate("planStart1");
		Date planFinish = d5.getDate("planFinish1");
		String actionType = "make";
		for (int i = 0; i < pcaList.size(); i++) {
			String name = ((Document) pcaList.get(i)).getString("name");
			Document action = new Document("action", name)//
					.append("charger_meta", charger_meta)//
					.append("charger", charger)//
					.append("planStart", planStart)//
					.append("planFinish", planFinish)//
					.append("stage", "pca")//
					.append("objective", "")//
					.append("problem_id", problem_id)//
					.append("index", actions.size())
					.append("actionType", actionType);
			actions.add(action);
		}
		
		pcaList = (List<?>) d5.get("pca2");
		charger_meta = (Document) d5.get("charger2_meta");
		charger = d5.getString("charger2");
		planStart = d5.getDate("planStart2");
		planFinish = d5.getDate("planFinish2");
		actionType = "out";
		for (int i = 0; i < pcaList.size(); i++) {
			String name = ((Document) pcaList.get(i)).getString("name");
			Document action = new Document("action", name)//
					.append("charger_meta", charger_meta)//
					.append("charger", charger)//
					.append("planStart", planStart)//
					.append("planFinish", planFinish)//
					.append("stage", "pca")//
					.append("objective", "")//
					.append("problem_id", problem_id)//
					.append("index", actions.size())
					.append("actionType", actionType);
			actions.add(action);
		}
		service.insertActions(actions);
		grid.doRefresh();
	}
	
	@Behavior({"启动紧急反应行动","中止紧急反应行动"})
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		if(!"解决中".equals(problem.getStatus()))
			return false;
		return true;
	}


}
