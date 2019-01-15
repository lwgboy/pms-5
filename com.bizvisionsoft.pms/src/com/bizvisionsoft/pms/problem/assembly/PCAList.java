package com.bizvisionsoft.pms.problem.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.SpinnerCellEditor;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Columns;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class PCAList {

	private class PCAListContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object index) {
			return (Object[]) index;
		}

		@Override
		public Object[] getChildren(Object index) {
			if (decisionCriteria == null || decisionCriteria.isEmpty())
				return new Object[0];
			if (((Integer) index) == 1)
				return Optional.ofNullable((List<?>) decisionCriteria.get("givens")).map(d -> d.toArray()).orElse(new Object[0]);
			if (((Integer) index) == 2)
				return Optional.ofNullable((List<?>) decisionCriteria.get("wants")).map(d -> d.toArray()).orElse(new Object[0]);
			return new Object[0];
		}

		@Override
		public Object getParent(Object index) {
			return null;
		}

		@Override
		public boolean hasChildren(Object index) {
			return index instanceof Integer && ((Integer) index) > 0;
		}

	}

	public Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private Problem problem;

	private ProblemService service;

	private GridTreeViewer viewer;

	private Document decisionCriteria;

	private String language;

	private String[] items;

	private List<Document> pcaList;

	@Init
	protected void init() {
		language = RWT.getLocale().getLanguage();
		problem = context.getRootInput(Problem.class, false);
		service = Services.get(ProblemService.class);
		decisionCriteria = service.getD5DecisionCriteria(problem.get_id());
		if(decisionCriteria!=null) {
			items = new String[] {decisionCriteria.getString("endResult"), "强制要求", "期望目标" };
		}else {
			items = new String[] { "强制要求", "期望目标" };
		}
		pcaList = service.listD5PCA(problem.get_id(), language);
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		Action action1 = new ActionFactory().normalStyle().forceText("目标和准则").name("editCriteria").get();
		Action action2 = new ActionFactory().normalStyle().forceText("创建方案").name("createSolution").get();

		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(action1, action2));
		Controls.handle(bar).setText("选择永久纠正措施方案").height(48).left().top().right().select(e -> {
			if ("createSolution".equals(((Action) e.data).getName())) {
				handleCreatePCA();
			} else if ("editCriteria".equals(((Action) e.data).getName())) {
				handleEditCriteria();
			}
		}).add(() -> Controls.handle(createGrid(parent)).mLoc().layout(new FillLayout())).get();
	}

	private Grid createGrid(Composite parent) {
		viewer = new GridTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setAutoExpandLevel(GridTreeViewer.ALL_LEVELS);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setHideIndentionImage(true);
		grid.setAutoHeight(true);
		Controls.handle(grid).markup();

		Columns.create(viewer).setWidth(320).setText("目标/决策项").setLabelProvider(this.labelTitleColumn());
		Columns.create(viewer).setWidth(48).setText("权重").setAlignment(SWT.CENTER).setLabelProvider(this::labelWeightColumn);

		for (int i = 0; i < pcaList.size(); i++) {
			Document pca = pcaList.get(i);
			createPCAColumn(pca);
		}

		viewer.setContentProvider(new PCAListContentProvider());
		viewer.setInput(new Integer[] { 0, 1, 2 });
		return grid;
	}

	private void createPCAColumn(Document pca) {
		GridColumn col = Columns.create(viewer).setWidth(240).setEditingSupport(this.editingSupport(pca))
				.setLabelProvider(this.labelPCAColumn(pca)).getColumn();
		col.setWordWrap(true);
		UserSession.bruiToolkit().enableMarkup(col);
		col.setData("pca", pca);
		String name = pca.getString("name");
		if (pca.getBoolean("selected", false)) {
			col.setText(getPCAColumnHeaderText("<b>[已选择]" + name + "</b>"));
		} else {
			col.setText(getPCAColumnHeaderText(name));
		}
		col.addListener(SWT.Selection, event -> {
			Document p = (Document) ((GridColumn) event.widget).getData("pca");
			ActionMenu m = new ActionMenu(br);
			List<Action> actions = new ArrayList<>();
			actions.add(new ActionFactory().text("选择方案").normalStyle().exec((r, t) -> {
				if (br.confirm("选择PCA", "请确认选择以下方案作为永久纠正措施。<br>" + p.getString("name"))) {
					handleSelectPCA(p);
				}
			}).get());

			actions.add(new ActionFactory().text("编辑方案").normalStyle().exec((r, t) -> {
				handleEditPCA(p);
			}).get());

			actions.add(new ActionFactory().text("删除方案").warningStyle().exec((r, t) -> {
				if (br.confirm("删除PCA", "请确认删除以下方案。<br>" + p.getString("name"))) {
					handleDeletePCA(p);
				}
			}).get());

			m.setActions(actions).open();
		});
	}

	private String getPCAColumnHeaderText(String name) {
		return "<div style='display:flex;justify-content:space-between;'><div class='brui_text_line' style='flex-shrink:1'>" + name
				+ "</div><i class='layui-icon layui-icon-triangle-d' style='flex-shrink:0'></i></div>";
	}

	private EditingSupport editingSupport(Document pca) {
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				Document param = (Document) element;
				if (param.get("weight") == null) {// 强制的
					handleUpdatePCAParamterValue(pca, param, value, "givens");
				} else {
					handleUpdatePCAParamterValue(pca, param, value, "wants");
				}
			}

			@Override
			protected Object getValue(Object element) {
				Document param = (Document) element;
				if (param.get("weight") == null) {// 强制的
					return Boolean.TRUE.equals(getPCAGivensValue(pca, param));
				} else {
					return Optional.ofNullable(getPCAWantsValue(pca, param)).map(i -> i).orElse(0);
				}
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				Document param = (Document) element;
				if (param.get("weight") == null) {// 强制的
					return new CheckboxCellEditor(viewer.getGrid());
				} else {
					return new SpinnerCellEditor(viewer.getGrid()).setMaximum(10).setMinimum(0).setIncrement(1).setPageIncrement(2)
							.setDigits(0);
				}
			}

			@Override
			protected boolean canEdit(Object element) {
				return element instanceof Document;
			}
		};
	}

	private CellLabelProvider labelPCAColumn(Document pca) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Document) {
					Document param = (Document) element;
					if (param.get("weight") == null) {// 强制的
						Boolean value = getPCAGivensValue(pca, param);
						if (value == null) {
							return "";
						} else if (value) {
							return "<i class='layui-icon layui-icon-ok'></i>";
						} else {
							return "<i class='layui-icon layui-icon-close'></i>";
						}
					} else {
						return Optional.ofNullable(getPCAWantsValue(pca, param)).map(i -> "" + i).orElse("");
					}
				} else if (element instanceof Integer) {
					if (((Integer) element).intValue() == 0) {
						return getPCAItemText(pca);
					} else {
						return "";
					}
				}
				return "";
			}

		};
	}

	private String getPCAItemText(Document pca) {
		StringBuffer sb = new StringBuffer();
		List<?> list = (List<?>) pca.get("pca1");
		if (list != null) {
			sb.append("<div class='layui-text'>");
			sb.append("<div>问题产生纠正措施</div>");
			sb.append("<ul style='margin-left: 4px;padding:4px 0px 4px 16px;'>");
			for (int i = 0; i < list.size(); i++) {
				Document x = (Document) list.get(i);
				sb.append("<li style='magin:0px'>" + x.getString("name") + "</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}
		list = (List<?>) pca.get("pca2");
		if (list != null) {
			sb.append("<div class='layui-text'>");
			sb.append("<div>问题流出纠正措施</div>");
			sb.append("<ul style='margin-left: 4px;padding:4px 0px 4px 16px;'>");
			for (int i = 0; i < list.size(); i++) {
				Document x = (Document) list.get(i);
				sb.append("<li style='magin:0px'>" + x.getString("name") + "</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}

		return sb.toString();
	}

	private Boolean getPCAGivensValue(Document pca, Document nameParameter) {
		String name = nameParameter.getString("name");
		List<?> list = (List<?>) pca.get("givens");
		if (list != null) {
			Document first = (Document) list.stream().filter(d -> name.equals(((Document) d).getString("name"))).findFirst().orElse(null);
			if (first != null) {
				return first.getBoolean("value");
			}
		}
		return null;
	}

	private Integer getPCAWantsValue(Document pca, Document nameParameter) {
		String name = nameParameter.getString("name");
		List<?> list = (List<?>) pca.get("wants");
		if (list != null) {
			Document first = (Document) list.stream().filter(d -> name.equals(((Document) d).getString("name"))).findFirst().orElse(null);
			if (first != null) {
				return first.getInteger("value");
			}
		}
		return null;
	}

	private CellLabelProvider labelTitleColumn() {
		return new GridColumnLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object elem = cell.getElement();
				GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
				if (elem instanceof Document) {
					String name = ((Document) elem).getString("name");
					String weight = ((Document) elem).getString("weight");
					cell.setText(name);
					if (weight == null) {
						gridItem.setColumnSpan(0, 1);
					}
				} else if (elem instanceof Integer) {
					cell.setText(items[(Integer) elem]);
					if ((Integer) elem > 0) {
						gridItem.setColumnSpan(0, 1 + pcaList.size());
						gridItem.setBackground(BruiColors.getColor(BruiColor.Grey_50));
					} else {
						gridItem.setColumnSpan(0, 1);
					}
				}
			}
		};
	}

	private String labelWeightColumn(Object elem) {
		if (elem instanceof Document)
			return ((Document) elem).getString("weight");
		return "";
	}

	private void handleCreatePCA() {
		Editor.open("D5-PCA方案-编辑器", context, new Document(), (r, t) -> {
			t.append("problem_id", problem.get_id()).append("_id", new ObjectId());
			service.insertD5PCA(t, language);
			pcaList.add(t);
			createPCAColumn(t);
			viewer.refresh();
		});
	}

	private void handleEditCriteria() {
		Document d = service.getD5DecisionCriteria(problem.get_id());
		boolean insert = d == null;
		if (insert) {
			d = new Document();
		}
		Editor.create("D5-目标和准则-编辑器", context, d, true).ok((r, t) -> {
			if (insert) {
				t.append("_id", problem.get_id());
				service.insertD5DecisionCriteria(t, RWT.getLocale().getLanguage());
			} else {
				r.remove("_id");
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r).bson();
				service.updateD5DecisionCriteria(fu, RWT.getLocale().getLanguage());
			}
			viewer.refresh();
		});
	}

	private void handleEditPCA(Document pca) {
		Editor.open("D5-PCA方案-编辑器", context, pca, (r, t) -> {
			r.remove("_id");
			FilterAndUpdate fu = new FilterAndUpdate().filter(new BasicDBObject("_id", pca.get("_id"))).set(r);
			service.updateD5PCA(fu.bson(), language);
			pca.clear();
			pca.putAll(t);
			viewer.refresh();
		});
	}

	private void handleDeletePCA(Document pca) {
		service.deleteD5PCA(pca.getObjectId("_id"), language);
		Arrays.asList(viewer.getGrid().getColumns()).stream().filter(c -> pca == c.getData("pca")).findFirst().ifPresent(c -> c.dispose());
	}

	@SuppressWarnings({ "unchecked" })
	private void handleUpdatePCAParamterValue(Document pca, Document param, Object value, String fieldName) {
		List<Document> givens = (List<Document>) pca.get(fieldName);
		if (givens == null) {
			givens = new ArrayList<>();
			pca.put(fieldName, givens);
		}
		String name = param.getString("name");
		boolean find = false;
		for (int i = 0; i < givens.size(); i++) {
			Document d = (Document) givens.get(i);
			if (name.equals(d.get("name"))) {
				Object oldValue = d.get("value");
				if (Check.equals(oldValue, value)) {
					return;
				}
				d.append("value", value);
				find = true;
				break;
			}
		}
		if (!find)
			givens.add(new Document("name", name).append("value", value));

		BasicDBObject set = new BasicDBObject();
		set.putAll(pca);
		set.remove("_id");
		FilterAndUpdate fu = new FilterAndUpdate().filter(new BasicDBObject("_id", pca.get("_id"))).set(set);
		service.updateD5PCA(fu.bson(), language);
		viewer.update(param, null);
	}

	private void handleSelectPCA(Document pca) {
		Object _id = pca.get("_id");
		FilterAndUpdate fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("selected", true));
		service.updateD5PCA(fu.bson(), language);
		fu = new FilterAndUpdate().filter(new BasicDBObject("problem_id", problem.get_id()).append("_id", new BasicDBObject("$ne", _id)))
				.set(new BasicDBObject("selected", false));
		service.updateD5PCA(fu.bson(), language);

		Arrays.asList(viewer.getGrid().getColumns()).stream().filter(c -> c.getData("pca") != null).forEach(e -> {
			Document otherPCA = (Document) e.getData("pca");
			String name = otherPCA.getString("name");
			if (_id.equals(otherPCA.getObjectId("_id"))) {
				otherPCA.put("selected", true);
				e.setText(getPCAColumnHeaderText("<b>[已选择]" + name + "</b>"));
			} else {
				otherPCA.put("selected", false);
				e.setText(getPCAColumnHeaderText(name));
			}
		});
	}

}
