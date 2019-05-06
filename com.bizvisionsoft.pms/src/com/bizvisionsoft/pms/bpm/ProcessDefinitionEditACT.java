package com.bizvisionsoft.pms.bpm;

import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridItem;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.BPMService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ProcessDefinition;
import com.bizvisionsoft.service.model.TaskDefinition;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProcessDefinitionEditACT {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object elem) {
		if (elem instanceof ProcessDefinition) {
			editProcessDefinition((ProcessDefinition)elem);
		} else if (elem instanceof TaskDefinition) {
			ObjectId _id = ((TaskDefinition) elem).get_id();
			if (_id == null) {
				createTaskDefinition((TaskDefinition)elem);
			}else {
				editTaskDefinition((TaskDefinition)elem);
			}
		}
	}

	private void editTaskDefinition(TaskDefinition td) {
		IStructuredDataPart grid = (IStructuredDataPart) context.getContent();
		Editor.create("������༭��", context, td, false).setEditable(true).setTitle("�༭������"+td.toString()).ok((r,o)->{
			Object _id = r.get("_id");
			r.removeField("_id");
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(r).bson();
			Services.get(BPMService.class).updateTaskDefinitions(fu,br.getDomain());
			grid.replaceItem(td, o);
		});
	}

	private void createTaskDefinition(TaskDefinition td) {
		GridPart grid = (GridPart) context.getContent();
		Editor.create("������༭��", context, td, false).setEditable(true).setTitle("����������"+td.toString()).ok((r,o)->{
			Services.get(BPMService.class).insertTaskDefinition(o,br.getDomain());
			GridTreeViewer viewer = grid.getViewer();
			GridItem item = (GridItem) viewer.testFindItem(td);
			Object parentItem = item.getParentItem().getData();
			viewer.refresh(parentItem, true);
		});
	}

	private void editProcessDefinition(ProcessDefinition pd) {
		IStructuredDataPart grid = (IStructuredDataPart) context.getContent();
		Editor.create("����������༭��", context, pd, false).setEditable(true).setTitle("�༭������"+pd.toString()).ok((r,o)->{
			grid.doModify(pd, o, r);
		});
	}
}