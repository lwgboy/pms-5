package com.bizvisionsoft.pms.resource;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
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
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditWorkResourceActualASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Calendar start;

	private Calendar end;

	private GridTableViewer viewer;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		Action closeAction = new Action();
		closeAction.setName("close");
		closeAction.setImage("/img/close.svg");

		// Action addAction = new Action();
		// addAction.setName("add");
		// addAction.setText("添加资源用量");
		// addAction.setForceText(true);
		// addAction.setStyle("normal");

		StickerTitlebar bar = new StickerTitlebar(parent, closeAction, null)
				.setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				brui.closeCurrentContent();
				// } else if ("add".equals(action.getName())) {
				// allocateResource();
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
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		ResourceTransfer ra = (ResourceTransfer) context.getInput();
		String barText;
		if (ResourceTransfer.TYPE_PLAN == ra.getType())
			barText = "资源计划用量 ";
		else
			barText = "资源实际用量 ";
		bar.setText(barText);

		// 取出开始和完成时间

		List<Document> resource = Services.get(WorkService.class).getResource(ra);
		start = Calendar.getInstance();
		start.setTime(ra.getFrom());
		end = Calendar.getInstance();
		end.setTime(ra.getTo());
		//
		createViewer(content, ra.getShowType(), ra.getType());

		viewer.setInput(resource);
		//
		// TODO 封装成方法
		// works = service.listConflictWorks(ra.work_id);
		// viewer.setInput(works);
	}

	private void allocateResource() {
		// 显示资源选择框
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("人力资源");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("设备资源");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("资源类型");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// 弹出menu
		new ActionMenu(brui).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("工作报告-添加人力资源编辑器");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("工作报告-添加设备资源编辑器");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("工作报告-添加资源类型编辑器");
			return false;
		}).open();
	}

	private void addResource(String editorId) {
		// TODO

	}

	private void createViewer(Composite parent, int showType, int type) {
		viewer = new GridTableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		if (showType == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE) {
			grid.setData(RWT.FIXED_COLUMNS, 5);
		} else if (showType == ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE) {
			grid.setData(RWT.FIXED_COLUMNS, 3);
		} else if (showType == ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE) {
			grid.setData(RWT.FIXED_COLUMNS, 2);
		}
		Column c;
		if (showType == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE
				|| showType == ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE) {
			c = new Column();
			c.setName("workName");
			c.setText("工作名称");
			c.setWidth(160);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("projectName");
			c.setText("项目名称");
			c.setWidth(160);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);
		}

		if (showType == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE
				|| showType == ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE) {
			c = new Column();
			c.setName("resId");
			c.setText("资源编号");
			c.setWidth(120);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("type");
			c.setText("资源类型");
			c.setWidth(120);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("name");
			c.setText("名称");
			c.setWidth(120);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);
		}
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(start.getTimeInMillis());
		createDateColumn(now.getTime(), type);
		while (now.before(end)) {
			now.add(Calendar.DATE, 1);
			createDateColumn(now.getTime(), type);
		}

		c = new Column();
		c.setName("");
		c.setText("");
		c.setWidth(0);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		viewer.setContentProvider(ArrayContentProvider.getInstance());

	}

	private void createDateColumn(Date now, int type) {
		String name = new SimpleDateFormat("yyyy/M/d").format(now);
		GridColumnGroup grp = new GridColumnGroup(viewer.getGrid(), SWT.CENTER);

		grp.setData("name", name);
		grp.setText(name);
		grp.setExpanded(true);

		GridColumn col = new GridColumn(grp, SWT.CENTER);
		col.setText("标准用量");
		col.setWidth(80);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(now, "Basic", type));

		col = new GridColumn(grp, SWT.CENTER);
		col.setText("加班用量");
		col.setWidth(80);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(now, "OverTime", type));
	}

	@SuppressWarnings("unchecked")
	private ColumnLabelProvider getColumnLabelProvider(Date now, String key, int type) {
		String id = new SimpleDateFormat("yyyyMMdd").format(now);

		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object obj = ((Document) element).get("resource");
				if (obj instanceof List) {
					List<Document> list = (List<Document>) obj;
					for (Document doc : list) {
						if (id.equals(doc.get("id"))) {
							Object value;
							if (ResourceTransfer.TYPE_PLAN == type) {
								value = doc.get("plan" + key + "Qty");
							} else {
								value = doc.get("actual" + key + "Qty");
							}
							if (value instanceof Number) {
								double d = ((Number) value).doubleValue();
								if (d != 0d) {
									return new DecimalFormat("0.0").format(d);
								}
							}
							return "";
						}
					}

				}
				return "";
			}
		};
		return labelProvider;
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
				// TODO
				return "" + ((Document) element).get(name);
				// if ("resId".equals(name)) {
				// return "";
				// } else if ("type".equals(name)) {
				// return "";
				// } else if ("name".equals(name)) {
				// return "";
				// } else if ("workName".equals(name)) {
				// return "";
				// } else if ("projectName".equals(name)) {
				// return "";
				// }
				// return "";
			}
		});

	}
}
