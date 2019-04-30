package com.bizvisionsoft.pms.bpm;

import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProcessDefinitionDeleteACT {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object elem) {
		if (!br.confirm("É¾³ý", "É¾³ý" + elem + "£¬ÇëÈ·ÈÏ"))
			return;
		if (elem instanceof ProcessDefinition) {
			Services.get(BPMService.class).deleteProcessDefinition(((ProcessDefinition) elem).get_id());
			GridPart grid = (GridPart) context.getContent();
			grid.remove(elem);
		} else if (elem instanceof TaskDefinition) {
			ObjectId _id = ((TaskDefinition) elem).get_id();
			if (_id != null) {
				Services.get(BPMService.class).deleteTaskDefinition(((TaskDefinition) elem).get_id());
				GridPart grid = (GridPart) context.getContent();
				GridTreeViewer viewer = grid.getViewer();
				GridItem item = (GridItem) viewer.testFindItem(elem);
				Object parentItem = item.getParentItem().getData();
				viewer.refresh(parentItem, true);
			}
		}
		Layer.message("ÒÑÉ¾³ý");
	}
}