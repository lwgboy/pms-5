package com.bizvisionsoft.pms.resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.serviceconsumer.Services;

public class SingleResourceConflictSolutionASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Calendar start;

	private Calendar end;

	private GridTableViewer viewer;

	private List<WorkResourcePlanDetail> works;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = new StickerTitlebar(parent, null, null)
				.setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				brui.closeCurrentContent();
			}
		});
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 12);
		fd.top = new FormAttachment(bar, 12);
		fd.right = new FormAttachment(100, -12);
		fd.bottom = new FormAttachment(100, -12);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		ResourcePlan rp = (ResourcePlan) context.getInput();
		bar.setText("解决资源冲突 - " + rp);

		// 取出开始和完成时间

		WorkService service = Services.get(WorkService.class);
		Work work = service.getWork(rp.getWork_id());
		start = Calendar.getInstance();
		start.setTime(Optional.ofNullable(work.getActualStart()).orElse(work.getPlanStart()));
		end = Calendar.getInstance();
		end.setTime(work.getPlanFinish());

		createViewer(content);

		// TODO 封装成方法
		works = service.listConflictWorks(rp);
		viewer.setInput(works);
	}

	private void createViewer(Composite parent) {
		viewer = new GridTableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);

		Column c = new Column();
		c.setName("project");
		c.setText("项目");
		c.setWidth(240);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("work");
		c.setText("工作");
		c.setWidth(240);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("startDate");
		c.setText("开始");
		c.setWidth(96);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("endDate");
		c.setText("完成");
		c.setWidth(96);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(start.getTimeInMillis());
		while (now.before(end)) {
			createDateColumn(now.getTime());
			now.add(Calendar.DATE, 1);
		}

		viewer.setContentProvider(ArrayContentProvider.getInstance());

	}

	private void createDateColumn(Date now) {
		GridColumn col = new GridColumn(viewer.getGrid(), SWT.CENTER);
		col.setText(new SimpleDateFormat("M/d").format(now.getTime()));
		col.setWidth(48);
		col.setMoveable(false);
		col.setResizeable(false);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Optional.ofNullable(((WorkResourcePlanDetail) element).getWorks(now)).map(d -> "" + d)
						.orElse("");
			}

			@Override
			public Color getBackground(Object element) {
				double workTime = 0;
				Iterator<WorkResourcePlanDetail> iter = works.iterator();
				while (iter.hasNext())
					workTime += Optional.ofNullable(iter.next().getWorks(now)).orElse(0d);
				return workTime > 8 ? BruiColors.getColor(BruiColor.Red_400) : null;
			}
		});
	}

	private void createTitleColumn(Column c) {
		GridColumn col = new GridColumn(viewer.getGrid(), SWT.NONE);
		col.setText(c.getText());
		col.setWidth(c.getWidth());
		col.setMoveable(c.isMoveable());
		col.setResizeable(c.isResizeable());

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String name = c.getName();
				if ("project".equals(name)) {
					return ((WorkResourcePlanDetail) element).projectName;
				} else if ("work".equals(name)) {
					return ((WorkResourcePlanDetail) element).name;
				} else if ("startDate".equals(name)) {
					Date startDate;
					if (((WorkResourcePlanDetail) element).actualStart != null) {
						startDate = ((WorkResourcePlanDetail) element).actualStart;
					} else {
						startDate = ((WorkResourcePlanDetail) element).planStart;
					}
					return new SimpleDateFormat("yyyy-MM-dd").format(startDate);
				} else if ("endDate".equals(name)) {
					Date endDate;
					if (((WorkResourcePlanDetail) element).actualFinish != null) {
						endDate = ((WorkResourcePlanDetail) element).actualFinish;
					} else {
						endDate = ((WorkResourcePlanDetail) element).planFinish;
					}
					return new SimpleDateFormat("yyyy-MM-dd").format(endDate);
				}
				return "";
			}
		});

	}
}
