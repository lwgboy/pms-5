package com.bizvisionsoft.pms.problem.assembly;

import org.bson.Document;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.chart.ECharts;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class CausalAnalysis {

	public Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private IBruiService br;

	@Inject
	private String type;

	@Inject
	private BruiAssemblyContext context;

	private ECharts chart;

	private GridTreeViewer tree;

	private Problem problem;

	private ProblemService service;

	@Init
	private void init() {
		problem = context.getRootInput(Problem.class, false);
		service = Services.get(ProblemService.class);
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		Composite content = Controls.contentPanel(parent).mLoc().formLayout().bg(BruiColor.white).get();

		// ������д�����������
		Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT, 0.25f).formLayout().put(this::leftPane)
				// �������ұ߻�һ���ָ���
				.addRight(() -> Controls.label(content, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM, 1))
				// ���ߵ��ұ���ͼ���������ң�
				.addRight(() -> Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.RIGHT).formLayout().put(this::rightPane));

	}

	private void leftPane(Composite parent) {
		parent.setLayout(new FormLayout());
		StickerTitlebar bar = Controls.handle(new StickerTitlebar(parent, null, null)).loc(SWT.TOP | SWT.LEFT | SWT.RIGHT, 48).get();
		bar.setText(type);
		tree = new GridTreeViewer(parent, SWT.V_SCROLL);
		tree.getGrid().setLinesVisible(false);
		tree.getGrid().setData(RWT.CUSTOM_VARIANT, "board");
		tree.setAutoExpandLevel(GridTreeViewer.ALL_LEVELS);
		GridViewerColumn textColumn = new GridViewerColumn(tree, SWT.NONE);
		textColumn.setLabelProvider(new CauseLabelProvider(type));
		GridViewerColumn btnColumn = new GridViewerColumn(tree, SWT.NONE);
		btnColumn.getColumn().setWidth(36);
		btnColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return "<a target='_rwt' href='add' style='margin:6px;' class='layui-icon layui-icon-add-1 layui-btn layui-btn-primary layui-btn-xs'></a>";
				} else {
					StringBuffer s = new StringBuffer();
					s.append(
							"<div style='height:96px;display:flex;flex-direction:column;justify-content:space-evenly;align-items:center;'>");
					s.append(
							"<div><a target='_rwt' href='add'  class='layui-icon layui-icon-add-1 layui-btn layui-btn-primary layui-btn-xs'/></div>");
					s.append(
							"<div><a target='_rwt' href='edit'  class='layui-icon layui-icon-edit layui-btn layui-btn-primary layui-btn-xs'/></div>");
					s.append(
							"<div><a target='_rwt' href='del'  class='layui-icon layui-icon-close layui-btn layui-btn-primary layui-btn-xs'/></div>");
					s.append("</div>");
					return s.toString();
				}
			}
		});

		tree.setContentProvider(new CauseContentProvider(problem, type));
		tree.setInput(ProblemService.CauseSubject);
		Controls.handle(tree.getControl()).markup().loc(SWT.LEFT | SWT.RIGHT | SWT.BOTTOM).top(bar)
				.listen(SWT.Selection, this::showTreeMemu)
				.listen(SWT.Resize, e -> textColumn.getColumn().setWidth(tree.getGrid().getBounds().width - 36));
	}

	public void showTreeMemu(Event event) {
		Object element = event.item.getData();
		if ("add".equals(event.text)) {
			if (element instanceof String) {// ���
				createCauseItem((String) element);
			} else if (element instanceof CauseConsequence) {
				createCauseItem((CauseConsequence) element);
			}
		} else if ("edit".equals(event.text)) {
			editCauseItem((CauseConsequence) element);
		} else if ("del".equals(event.text)) {
			Object parent = ((GridItem) event.item).getParentItem().getData();
			delCauseItem(parent, (CauseConsequence) element);
		}
	}

	private void delCauseItem(Object parent, CauseConsequence element) {
		if (br.confirm("ɾ��", "��ȷ��ɾ�������ϵ��" + element)) {
			service.deleteCauseConsequence(element.get_id());
			tree.refresh(parent, false);
			refreshChart();
		}
	}

	private void createCauseItem(CauseConsequence parent) {
		CauseConsequence cc = new CauseConsequence().setProblem_id(problem.get_id()).setType(type).setSubject(parent.getSubject())
				.setParent_id(parent.get_id());
		Editor.open("���ر༭��", context, cc, true, (r, t) -> {
			t = service.insertCauseConsequence(t);
			tree.refresh(parent, false);
			refreshChart();
			tree.expandAll();
		});
	}

	private void editCauseItem(CauseConsequence cc) {
		Editor.open("���ر༭��", context, cc, false, (r, t) -> {
			r.remove("_id");
			FilterAndUpdate fu = new FilterAndUpdate().filter(new BasicDBObject("_id", t.get_id())).set(r);
			service.updateCauseConsequence(fu.bson());
			tree.update(AUtil.simpleCopy(t, cc), null);
			refreshChart();
		});
	}

	private void createCauseItem(String subject) {
		CauseConsequence cc = new CauseConsequence().setProblem_id(problem.get_id()).setType(type).setSubject(subject);
		Editor.open("���ر༭��", context, cc, true, (r, t) -> {
			t = service.insertCauseConsequence(t);
			tree.refresh(subject, false);
			refreshChart();
		});
	}

	private void rightPane(Composite parent) {
		chart = Controls.handle(new ECharts(parent, SWT.NONE)).loc().get();
		refreshChart();
	}

	private void refreshChart() {
		try {
			Document chartData = service.getCauseConsequence(problem.get_id(), type);
			JsonObject option = JsonObject.readFrom(chartData.toJson());
			chart.setOption(option);
		} catch (Exception e) {
			String message = e.getMessage();
			logger.error(message);
			Layer.error(message);
		}
	}
}
