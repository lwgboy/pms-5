package com.bizvisionsoft.jz.workpackage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.PermissionUtil;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.UpdateWorkPackages;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.serviceconsumer.Services;

public class WorkPackagePlanASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Object work;

	private TrackView view;

	private GridPart grid;

	@CreateUI
	public void createUI(Composite parent) {
		Object[] input = (Object[]) context.getInput();
		work = input[0];
		view = (TrackView) input[1];

		parent.setLayout(new FormLayout());

		Action closeAction = new Action();
		closeAction.setName("close");
		closeAction.setImage("/img/close.svg");

		StickerTitlebar bar = new StickerTitlebar(parent, closeAction, null);
		String title;
		if (view == null) {
			title = "工作包：" + work;
		} else {
			title = view.toString() + "：" + work;
		}
		bar.setText(title);
		if (!(work instanceof Work) || ((Work) work).getActualFinish() == null) {
			// TODO 控制选取按钮显示
			final List<Action> actions = new ArrayList<Action>();
			Assembly assembly = context.getAssembly();
			List<Action> list = assembly.getActions();

			list = PermissionUtil.getPermitActions(brui.getCurrentUserInfo(), list, context.getRootInput());

			if (list != null) {
				list.forEach(action -> {
					if (!action.isObjectBehavier()) {
						actions.add(action);
					} else {
						if (work != null) {
							if (isAcceptableBehavior(action)) {
								actions.add(action);
							}
						}
					}
				});
			}

			bar.setActions(actions);
		}

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

		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				brui.closeCurrentContent();
			} else {
				UserSession.bruiToolkit().runAction(action, e, brui, context);
			}
		});

		createContent(content);
	}

	private boolean isAcceptableBehavior(Action action) {
		if (view == null) {
			if ("创建工作包".equals(action.getName())) {
				return true;
			}
		} else {
			String catagory = view.getCatagory();
			if ("研发".equals(catagory)) {
				if ("选取PLM对象".equals(action.getName())) {
					return true;
				}
			} else if ("采购".equals(catagory)) {
				if ("选取ERP计划".equals(action.getName())) {
					return true;
				}
			} else if ("生产".equals(catagory)) {
				if ("选取ERP计划".equals(action.getName())) {
					return true;
				}
			} else if ("质量".equals(catagory)) {

			}

			if ("更新执行情况".equals(action.getName())) {
				return true;
			}
		}
		return false;
	}

	private void createContent(Composite parent) {
		parent.setLayout(new FillLayout());
		BruiAssemblyContext gridContext;
		if (view == null) {
			gridContext = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("工作包-基本"))
					.setServices(brui).create().getContext();
		} else {
			gridContext = new AssemblyContainer(parent, context).setInput(view)
					.setAssembly(brui.getAssembly(view.getPackageAssembly())).setServices(brui).create().getContext();
		}
		grid = (GridPart) gridContext.getContent();
	}

	public void doCreate(Object parent, WorkPackage element) {
		grid.doCreate(parent, element);
	}

	public void insert(WorkPackage element) {
		WorkPackage workPackage = Services.get(WorkService.class).insertWorkPackage(element);
		grid.insert(workPackage);
	}

	public void updatePurchase(List<WorkPackage> workPackages) {
		List<WorkPackage> wps = Services.get(WorkService.class)
				.updatePurchaseWorkPackage(new UpdateWorkPackages().setWorkPackages(workPackages)
						.setWork_id(((Work) work).get_id()).setCatagory(view.getCatagory()).setName(view.getName()));
		grid.setViewerInput(wps);
	}

	public void updateProduction(List<WorkPackage> workPackages) {
		List<WorkPackage> wps = Services.get(WorkService.class)
				.updateProductionWorkPackage(new UpdateWorkPackages().setWorkPackages(workPackages)
						.setWork_id(((Work) work).get_id()).setCatagory(view.getCatagory()).setName(view.getName()));
		grid.setViewerInput(wps);
	}

	public void updatePLM(List<WorkPackage> workPackages, List<WorkPackageProgress> workPackageProgresss) {
		List<WorkPackage> wps = Services.get(WorkService.class)
				.updateDevelopmentWorkPackage(new UpdateWorkPackages().setWorkPackages(workPackages)
						.setWorkPackageProgress(workPackageProgresss).setWork_id(((Work) work).get_id())
						.setCatagory(view.getCatagory()).setName(view.getName()));
		grid.setViewerInput(wps);
	}

}
