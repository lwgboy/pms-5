package com.bizvisionsoft.pms.resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.bson.Document;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
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
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourcePlan;
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

	private Locale locale;

	private List<Document> resource;

	private ResourceTransfer rt;

	@CreateUI
	public void createUI(Composite parent) {
		locale = RWT.getLocale();
		rt = (ResourceTransfer) context.getInput();

		parent.setLayout(new FormLayout());

		Action closeAction = new Action();
		closeAction.setName("close");
		closeAction.setImage("/img/close.svg");

		List<Action> rightActions = null;
		if (rt.isCanAdd()) {
			rightActions = new ArrayList<Action>();
			Action addAction = new Action();
			addAction.setName("add");
			if (ResourceTransfer.TYPE_PLAN == rt.getType())
				addAction.setText("�����Դ�ƻ�");
			else
				addAction.setText("�����Դ����");
			addAction.setForceText(true);
			addAction.setStyle("normal");
			rightActions.add(addAction);
		}

		StickerTitlebar bar = new StickerTitlebar(parent, closeAction, rightActions)
				.setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				brui.closeCurrentContent();
			} else if ("add".equals(action.getName())) {
				allocateResource();
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
		String barText;
		if (ResourceTransfer.TYPE_PLAN == rt.getType())
			barText = "��Դ�ƻ����� ";
		else
			barText = "��Դʵ������ ";
		bar.setText(barText);

		resource = Services.get(WorkService.class).getResource(rt);
		start = Calendar.getInstance();
		start.setTime(rt.getFrom());
		end = Calendar.getInstance();
		end.setTime(rt.getTo());
		//
		createViewer(content);

		viewer.setInput(resource);
	}

	private void allocateResource() {
		// ��ʾ��Դѡ���
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("������Դ");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("�豸��Դ");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("��Դ����");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// ����menu
		new ActionMenu(brui).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("������Դѡ����");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("�豸��ʩѡ����");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("��Դ����ѡ����");
			return false;
		}).open();
	}

	private void addResource(String editorId) {
		// TODO
		Selector.open(editorId, context, null, l -> {
			// List<ResourceAssignment> resa = new ArrayList<ResourceAssignment>();
			// l.forEach(o -> resa.add(new
			// ResourceAssignment().setTypedResource(o).setWork_id(work.get_id())));
			// Services.get(WorkService.class).addResourcePlan(resa);
			//
			// grid.setViewerInput(Services.get(WorkService.class).listResourcePlan(work.get_id()));
		});
	}

	private void createViewer(Composite parent) {
		viewer = new GridTableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE) {
			grid.setData(RWT.FIXED_COLUMNS, 5);
		} else if (rt.getShowType() == ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE) {
			grid.setData(RWT.FIXED_COLUMNS, 3);
		} else if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE) {
			grid.setData(RWT.FIXED_COLUMNS, 4);
		}
		Column c;
		if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE
				|| rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE) {
			c = new Column();
			c.setName("workName");
			c.setText("��������");
			c.setWidth(160);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("projectName");
			c.setText("��Ŀ����");
			c.setWidth(160);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);
		}

		if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE) {
			c = new Column();
			c.setName("startDate");
			c.setText("��ʼ");
			c.setWidth(96);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("endDate");
			c.setText("���");
			c.setWidth(96);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);
		}

		if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE
				|| rt.getShowType() == ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE) {
			c = new Column();
			c.setName("resId");
			c.setText("��Դ���");
			c.setWidth(120);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("type");
			c.setText("��Դ����");
			c.setWidth(120);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);

			c = new Column();
			c.setName("name");
			c.setText("����");
			c.setWidth(120);
			c.setAlignment(SWT.LEFT);
			c.setMoveable(false);
			c.setResizeable(true);
			createTitleColumn(c);
		}
		Calendar now = Calendar.getInstance();
		now.setTime(start.getTime());
		createDateColumn(now.getTime());
		while (now.before(end)) {
			now.add(Calendar.DATE, 1);
			createDateColumn(now.getTime());
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
		viewer.getGrid().addListener(SWT.Selection, l -> {
			Object data = l.item.getData();
			System.out.println(l.text);
		});

	}

	private void createDateColumn(Date now) {
		String name = Util.getFormatText(now, null, locale);
		GridColumnGroup grp = new GridColumnGroup(viewer.getGrid(), SWT.CENTER);

		grp.setData("name", name);
		grp.setText(name);
		grp.setExpanded(true);

		GridColumn col = new GridColumn(grp, SWT.CENTER);
		col.setText("��׼");
		col.setData("name", "Basic");
		col.setWidth(48);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(now, "Basic"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "OverTime");
		col.setText("�Ӱ�");
		col.setWidth(48);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(now, "OverTime"));
	}

	@SuppressWarnings("unchecked")
	private ColumnLabelProvider getColumnLabelProvider(Date now, String key) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String id = sdf.format(now);

		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object obj = ((Document) element).get("resource");
				if (obj instanceof List) {
					List<Document> list = (List<Document>) obj;
					for (Document doc : list) {
						if (id.equals(sdf.format(doc.get("id")))) {
							Object value;
							if (ResourceTransfer.TYPE_PLAN == rt.getType()) {
								value = doc.get("plan" + key + "Qty");
							} else {
								value = doc.get("actual" + key + "Qty");
							}
							String text = Util.getFormatText(value, null, locale);
							return "0.0".equals(text) ? "" : text;
						}
					}

				}
				return "";
			}

			@Override
			public Color getBackground(Object element) {
				if (rt.isCheckTime()) {
					double workTime = 0;
					Iterator<Document> iter = resource.iterator();
					while (iter.hasNext()) {
						Object obj = iter.next().get("resource");
						if (obj instanceof List) {
							List<Document> list = (List<Document>) obj;
							for (Document doc : list) {
								if (id.equals(sdf.format(doc.get("id")))) {
									Object value;
									if (ResourceTransfer.TYPE_PLAN == rt.getType())
										value = doc.get("plan" + key + "Qty");
									else
										value = doc.get("actual" + key + "Qty");

									if (value instanceof Number) {
										workTime += ((Number) value).doubleValue();
									}
								}
							}
						}
					}
					if ("Basic".equals(key)) {
						return workTime > 8 ? BruiColors.getColor(BruiColor.Red_400) : null;
					} else if ("OverTime".equals(key)) {
						return workTime > 16 ? BruiColors.getColor(BruiColor.Red_400) : null;
					}
				}
				return null;
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
				Object value;
				if ("startDate".equals(name)) {
					if (((Document) element).get("actualStart") != null) {
						value = ((Document) element).get("actualStart");
					} else {
						value = ((Document) element).get("planStart");
					}
				} else if ("endDate".equals(name)) {
					if (((Document) element).get("actualFinish") != null) {
						value = ((Document) element).get("actualFinish");
					} else {
						value = ((Document) element).get("planFinish");
					}
				} else {
					value = ((Document) element).get(name);
				}
				return Util.getFormatText(value, null, locale);
			}
		});

	}
}
