package com.bizvisionsoft.pms.work.assembly;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
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
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;

public class WorkPackagePlan {

	@Inject
	private IBruiService br;

	@Inject
	private BruiAssemblyContext context;

	private IWorkPackageMaster work;

	private TrackView view;

	private GridPart grid;

	private Assembly targetAssembly;

	@CreateUI
	public void createUI(Composite parent) {
		Object[] input = (Object[]) context.getInput();
		work = (IWorkPackageMaster) input[0];
		view = (TrackView) input[1];

		parent.setLayout(new FormLayout());

		Action closeAction = new Action();
		closeAction.setName("close");
		closeAction.setImage("/img/close.svg");

		StickerTitlebar bar = new StickerTitlebar(parent, closeAction, null);
		String title = Optional.ofNullable(view).map(v -> v.toString() + ": " + work).orElse("工作包: " + work);
		bar.setText(title);

		targetAssembly = Optional.ofNullable(view).map(v -> v.getPackageAssembly()).map(a -> br.getAssembly(a))
				.orElse(br.getAssembly("工作包-基本.gridassy"));

		bar.setActions(UserSession.bruiToolkit().getAcceptedActions(targetAssembly, br.getCurrentUserInfo(), context));

		Controls.handle(bar).height(48).left().top().right();

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout()).get();

		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("close".equals(action.getName())) {
				br.closeCurrentContent();
			} else {
				UserSession.bruiToolkit().runAction(action, e, br, context);
			}
		});

		grid = (GridPart) new AssemblyContainer(content, context).setInput(view).setAssembly(targetAssembly).setServices(br).create()
				.getContext().getContent();
	}

	public void doCreate() {
		String editorId = Optional.ofNullable(view).map(t -> t.getEditAssembly()).orElse("编辑工作包-基本.editorassy");
		Editor.open(editorId, context, WorkPackage.newInstance(work, view, br.getDomain()), (r, o) -> {
			grid.doCreate(null, o);
		});
	}

}
