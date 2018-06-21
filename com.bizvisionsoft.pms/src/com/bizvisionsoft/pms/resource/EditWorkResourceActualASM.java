package com.bizvisionsoft.pms.resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourceAssignment;
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

	private WorkService workService;

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
				addAction.setText("添加资源计划");
			else
				addAction.setText("添加资源用量");
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
			barText = "资源计划用量 ";
		else
			barText = "资源实际用量 ";
		bar.setText(barText);

		workService = Services.get(WorkService.class);
		start = Calendar.getInstance();
		start.setTime(rt.getFrom());
		end = Calendar.getInstance();
		end.setTime(rt.getTo());
		//
		createViewer(content);
		doRefresh();
	}

	private void doRefresh() {
		resource = workService.getResource(rt);
		viewer.setInput(resource);
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
			addResource("人力资源选择器");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("设备设施选择器");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("资源类型选择器");
			return false;
		}).open();
	}

	private void addResource(String editorId) {
		Selector.open(editorId, context, null, l -> {
			List<ResourceAssignment> resas = new ArrayList<ResourceAssignment>();
			if (ResourceTransfer.TYPE_PLAN == rt.getType()) {
				l.forEach(o -> {
					rt.getWorkIds().forEach(
							work_id -> resas.add(new ResourceAssignment().setTypedResource(o).setWork_id(work_id)));
				});
				workService.addResourcePlan(resas);
			} else if (ResourceTransfer.TYPE_ACTUAL == rt.getType()) {
				l.forEach(o -> {
					rt.getWorkIds().forEach(work_id -> {
						ResourceAssignment ra = new ResourceAssignment().setTypedResource(o).setWork_id(work_id);
						ra.from = start.getTime();
						ra.to = end.getTime();
						resas.add(ra);
					});
				});
				workService.addResourceActual(resas);
			}
			doRefresh();
		});
	}

	private void updateQty(String text, Object data) {
		String dialogTitle = "";
		String dialogMessage = "";
		if (rt.getType() == ResourceTransfer.TYPE_PLAN) {
			dialogTitle = "填写资源计划用量";
		} else if (rt.getType() == ResourceTransfer.TYPE_ACTUAL) {
			dialogTitle = "填写资源实际用量";
		}
		if (text.startsWith("Basic")) {
			dialogMessage = "请填写资源标准用量";
		} else if (text.startsWith("OverTime")) {
			dialogMessage = "请填写资源加班用量";
		}
		InputDialog id = new InputDialog(brui.getCurrentShell(), dialogTitle, dialogMessage, null, t -> {
			if (t.trim().isEmpty())
				return "请输入资源用量";
			try {
				Double.parseDouble(t);
			} catch (Exception e) {
				return "输入的类型错误";
			}
			return null;
		});
		if (InputDialog.OK == id.open()) {
			double qty = Double.parseDouble(id.getValue());
			if (text.indexOf("-") > 0) {
				if (data == null)
					return;

				Date period = null;
				try {
					period = new SimpleDateFormat("yyyyMMdd").parse(text.split("-")[1]);
					if (period != null && rt.getType() == ResourceTransfer.TYPE_PLAN) {
						ResourcePlan rp = new ResourcePlan();
						Document doc = (Document) data;
						rp.setWork_id(doc.getObjectId("work_id"));
						rp.setUsedEquipResId(doc.getString("usedEquipResId"));
						rp.setUsedHumanResId(doc.getString("usedHumanResId"));
						rp.setUsedTypedResId(doc.getString("usedTypedResId"));
						rp.setResTypeId(doc.getObjectId("resTypeId"));
						rp.setId(period);
						if (text.startsWith("Basic")) {
							rp.setPlanBasicQty(qty);
						} else if (text.startsWith("OverTime")) {
							rp.setPlanOverTimeQty(qty);
						}
						workService.insertResourcePlan(rp);
					} else if (period != null && rt.getType() == ResourceTransfer.TYPE_ACTUAL) {
						ResourceActual ra = new ResourceActual();
						Document doc = (Document) data;
						ra.setWork_id(doc.getObjectId("work_id"));
						ra.setUsedEquipResId(doc.getString("usedEquipResId"));
						ra.setUsedHumanResId(doc.getString("usedHumanResId"));
						ra.setUsedTypedResId(doc.getString("usedTypedResId"));
						ra.setResTypeId(doc.getObjectId("resTypeId"));
						ra.setId(period);
						if (text.startsWith("Basic")) {
							ra.setActualBasicQty(qty);
						} else if (text.startsWith("OverTime")) {
							ra.setActualOverTimeQty(qty);
						}
						workService.insertResourceActual(ra);
					}
				} catch (ParseException e) {
				}

			} else {
				ObjectId _id = null;
				String key = "";
				if (text.startsWith("Basic")) {
					_id = new ObjectId(text.replace("Basic", ""));
					key += "BasicQty";
				} else if (text.startsWith("OverTime")) {
					_id = new ObjectId(text.replace("OverTime", ""));
					key += "OverTimeQty";
				}
				if (_id != null && rt.getType() == ResourceTransfer.TYPE_PLAN) {
					workService.updateResourcePlan(new FilterAndUpdate().filter(new Document("_id", _id))
							.set(new Document("plan" + key, qty)).bson());
				} else if (_id != null && rt.getType() == ResourceTransfer.TYPE_ACTUAL) {
					workService.updateResourceActual(new FilterAndUpdate().filter(new Document("_id", _id))
							.set(new Document("actual" + key, qty)).bson());
				}

			}
			doRefresh();
		}
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

		if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_ONERESOURCE) {
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
		}

		if (rt.getShowType() == ResourceTransfer.SHOWTYPE_MULTIWORK_MULTIRESOURCE
				|| rt.getShowType() == ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE) {
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
			if (l.text != null)
				updateQty(l.text, l.item.getData());
		});

	}

	private void createDateColumn(Date now) {
		String name = Util.getFormatText(now, null, locale);
		GridColumnGroup grp = new GridColumnGroup(viewer.getGrid(), SWT.CENTER);

		grp.setData("name", name);
		grp.setText(name);
		grp.setExpanded(true);

		GridColumn col = new GridColumn(grp, SWT.CENTER);
		col.setText("标准");
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
		col.setText("加班");
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
							return "<a href='" + key + doc.get("_id").toString()
									+ "' target='_rwt' style='width: 100%;'>"
									+ ("0.0".equals(text)
											? ("<button class='layui-btn layui-btn-xs layui-btn-primary' style='bottom:0px;right:0px;'>"
													+ "<i class='layui-icon  layui-icon-edit'></i></button>")
											: text)
									+ "</a>";
						}
					}

				}
				return "<a href='" + key + "-" + id
						+ "' target='_rwt' style='width: 100%;'><button class='layui-btn layui-btn-xs layui-btn-primary' style='bottom:0px;right:0px;'>"
						+ "<i class='layui-icon  layui-icon-edit'></i></button></a>";
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
