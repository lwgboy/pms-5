package com.bizvisionsoft.pms.problem.assembly;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
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
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Columns;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

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

	private String[] items = new String[] { "","负责人","计划完成", "强制要求", "期望目标" };

	private List<Document> pcaList;

	@Init
	protected void init() {
		language = RWT.getLocale().getLanguage();
		problem = context.getRootInput(Problem.class, false);
		service = Services.get(ProblemService.class);
		decisionCriteria = service.getD5DecisionCriteria(problem.get_id());
		items[0] = decisionCriteria.getString("endResult");
		pcaList = service.listPCA(problem.get_id());
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		Action action = new ActionFactory().normalStyle().forceText("创建方案").name("create").get();
		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(action));
		Controls.handle(bar).setText("永久纠正措施候选方案").height(48).left().top().right().select(this::handleAction)
				.add(() -> Controls.handle(createGrid(parent)).mLoc().layout(new FillLayout())).get();
	}

	private void handleAction(Event e) {
		if ("create".equals(((Action) e.data).getName())) {
			Editor.open("D5-PCA-编辑器", context, new Document(), (r, t) -> {
				t.append("problem_id", problem.get_id()).append("_id", new ObjectId());
				service.insertD5PCA(t, language);
				pcaList.add(t);
				createPCAColumn(t);
				viewer.refresh();
			});
		}
	}

	private Grid createGrid(Composite parent) {
		viewer = new GridTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setAutoExpandLevel(GridTreeViewer.ALL_LEVELS);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setHideIndentionImage(true);
		grid.setAutoHeight(true);
		Controls.handle(grid).markup();

		Columns.create(viewer).setWidth(320).setText("目标/决策项").setLabelProvider(this.getTitleRowText());
		Columns.create(viewer).setWidth(48).setText("权重").setAlignment(SWT.CENTER).setLabelProvider(this::getWeightRowText);

		for (int i = 0; i < pcaList.size(); i++) {
			Document pca = pcaList.get(i);
			createPCAColumn(pca);
		}

		viewer.setContentProvider(new PCAListContentProvider());
		viewer.setInput(new Integer[] { 0, 1, 2 });
		return grid;
	}

	private void createPCAColumn(Document pca) {
		Columns.create(viewer).setWidth(240).setText("方案：" + pca.getString("name")).setLabelProvider(this.getPCAText(pca)).getColumn()
				.setWordWrap(true);
	}

	private CellLabelProvider getPCAText(Document document) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Document) {
					String name = ((Document) element).getString("name");
					return getPCAParameterText(document, name, "givens", "wants");
				} else if (element instanceof Integer) {
					if (((Integer) element).intValue() == 0) {
						return getPCAItemText(document);
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
			sb.append("<div>杜绝问题产生</div>");
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
			sb.append("<div>防止问题流出</div>");
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

	private String getPCAParameterText(Document pca, String name, String... fields) {
		for (int i = 0; i < fields.length; i++) {
			List<?> list = (List<?>) pca.get(fields[i]);
			if (list != null) {
				Document first = (Document) list.stream().filter(d -> name.equals(((Document) d).getString("name"))).findFirst()
						.orElse(null);
				if (first != null) {
					return Optional.ofNullable(first.get("value")).map(f -> "" + f).orElse("");
				}
			}
		}
		return "";
	}

	private CellLabelProvider getTitleRowText() {
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
					} else {
						gridItem.setColumnSpan(0, 1);
					}
				}
			}
		};
	}

	private String getWeightRowText(Object elem) {
		if (elem instanceof Document)
			return ((Document) elem).getString("weight");
		return "";
	}

}
