package com.bizvisionsoft.pms.project.dataset;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Export;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.exporter.MPPExporter;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;

import net.sf.mpxj.Duration;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;

public class ReadonlyGantt {
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private IWBSScope workScope;

	@Init
	private void init() {
		workScope = (IWBSScope) context.getRootInput();
	}

	@DataSet("data")
	public List<Work> data() {
		return workScope.createGanttTaskDataSet();
	}

	@DataSet("links")
	public List<WorkLink> links() {
		return workScope.createGanttLinkDataSet();
	}

	@Export(Export.DEFAULT)
	public void export() {
		try {
			new MPPExporter<Work, WorkLink>().setTasks(data()).setLinks(links())
					.setProjectName(workScope.getProjectName()).setTaskConvertor((w, t, m) -> {
						m.put(w.get_id(), t);
						t.setName(w.getText());
						t.setNotes(w.getFullName());
						Date planStart = w.getPlanStart();
						t.setStart(planStart);
						Date planFinish = w.getPlanFinish();
						t.setFinish(planFinish);
						t.setDuration(Duration.getInstance(w.getPlanDuration(), TimeUnit.DAYS));
						ObjectId parent_id = w.getParent_id();
						if (parent_id != null) {
							Task parentTask = m.get(parent_id);
							parentTask.addChildTask(t);
						}
					}).setLinkConvertor((w, taskMap) -> {
						String type = w.getType();
						RelationType rt;
						if ("FF".equals(type)) {
							rt = RelationType.FINISH_FINISH;
						} else if ("SS".equals(type)) {
							rt = RelationType.START_START;
						} else if ("SF".equals(type)) {
							rt = RelationType.START_FINISH;
						} else {
							rt = RelationType.FINISH_START;
						}
						ObjectId sourceId = w.getSourceId();
						ObjectId targetId = w.getTargetId();
						Task src = taskMap.get(sourceId);
						Task tgt = taskMap.get(targetId);
						tgt.addPredecessor(src, rt, Duration.getInstance(w.getLag(), TimeUnit.DAYS));
					}).export();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
