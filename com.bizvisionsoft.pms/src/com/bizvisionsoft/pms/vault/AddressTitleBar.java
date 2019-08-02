package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.BruiToolkit;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.tools.Check;

public class AddressTitleBar extends Composite implements ISelectionProvider {

	private BruiToolkit toolkit;
	private IBruiService service;
	private IBruiContext context;
	private List<IFolder> path;
	private List<Action> rightActions;
	private List<Button> buttons;

	private ISelection selection;
	private ListenerList<ISelectionChangedListener> listeners;

	public AddressTitleBar(Composite parent, List<Action> rightActions, IBruiService service, IBruiContext context) {
		super(parent, SWT.NONE);
		this.path = new ArrayList<IFolder>();
		this.rightActions = rightActions;
		this.service = service;
		this.context = context;
		toolkit = UserSession.bruiToolkit();
		buttons = new ArrayList<Button>();

		GridLayout layout = new GridLayout(2, false);
		setLayout(layout);
		setBackground(BruiColors.getColor(BruiColor.Grey_50));
		setHtmlAttribute("class", "brui_toolbar_s");
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 8;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginLeft = 0;

		createAddressBar();

		createToolbar();
	}

	private void createAddressBar() {
		Composite parent = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		parent.setLayout(layout);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginLeft = 0;

		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		parent.setLayoutData(gd);

		Button returnBtn = createActionUI(parent,
				new ActionFactory().img("/img/left_w.svg").infoStyle().exec((s, c) -> showParentFolder()).get());
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.heightHint = 32;
		gd.widthHint = 32;
		returnBtn.setLayoutData(gd);

		Composite folderPathCrumb = createFolderPathCrumb(parent);
		gd = new GridData(SWT.LEFT, SWT.TOP, true, false);
		gd.heightHint = 32;
		folderPathCrumb.setLayoutData(gd);

		Button refreshBtn = createActionUI(parent,
				new ActionFactory().img("/img/refresh_w.svg").infoStyle().exec((s, c) -> refresh()).get());

		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.heightHint = 32;
		gd.widthHint = 32;
		refreshBtn.setLayoutData(gd);

	}

	private void showParentFolder() {
		int index = buttons.size() - 1;
		if (index != -1) {
			buttons.get(index).dispose();
			buttons.remove(index);
			path.remove(index);
		}
		layout();
		if (index > 0)
			selectFolder(path.get(index - 1));
		else
			selectFolder(null);
	}

	private void refresh() {
		fireSelectionsChanged(new SelectionChangedEvent(this, selection));
	}

	protected Composite createFolderPathCrumb(Composite parent) {
		Composite folderPathPane = new Composite(parent, SWT.NONE);
		folderPathPane.setLayout(new FormLayout());

		Button left = createFolderPathBar(folderPathPane, null, "/img/cabinet_c.svg", null, (s, c) -> showFolder(null));

		if (Check.isAssigned(path))
			for (IFolder folder : path) {
				left = createFolderPathBar(folderPathPane, left, null, folder, (s, c) -> showFolder(folder));
				buttons.add(left);
			}

		return folderPathPane;
	}

	private void showFolder(IFolder folder) {
		removeFolderPathBar(folder);
		// TODO
	}

	private void removeFolderPathBar(IFolder folder) {
		if (folder != null) {
			int index = path.indexOf(folder);
			if (index != -1)
				for (int i = index + 1; i < buttons.size(); i++) {
					buttons.get(i).dispose();
					buttons.remove(i);
					path.remove(i);
					i--;
				}
		} else {
			buttons.clear();
			path.clear();
		}
		layout();
		selectFolder(folder);
	}

	private void selectFolder(IFolder folder) {
		if (folder != null) {
			this.selection = new StructuredSelection(new Object[] { folder });
		} else {
			this.selection = StructuredSelection.EMPTY;
		}
		fireSelectionsChanged(new SelectionChangedEvent(this, selection));
	}

	private void fireSelectionsChanged(SelectionChangedEvent event) {
		if (listeners != null) {
			Object[] lis = listeners.getListeners();
			for (int i = 0; i < lis.length; i++) {
				((ISelectionChangedListener) lis[i]).selectionChanged(event);
			}
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listeners == null) {
			listeners = new ListenerList<ISelectionChangedListener>();
		}
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return selection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public void setSelection(ISelection selection) {

	}

	private Button createFolderPathBar(Composite parent, Control left, String img, IFolder folder, BiConsumer<Object, Object> exec) {
		Button btn = createActionUI(parent,
				new ActionFactory().img(img).text(Optional.ofNullable(folder).map(f -> f.getName()).orElse(null)).exec(exec).get());
		// TODO 增加Button去左右内边距，没有边框的的样式

		FormData fd = new FormData();
		btn.setLayoutData(fd);
		fd.top = new FormAttachment();
		if (left != null)
			fd.left = new FormAttachment(left);
		else
			fd.left = new FormAttachment();
		fd.bottom = new FormAttachment(100);

		return btn;
	}

	private void createToolbar() {
		Composite parent = new Composite(this, SWT.NONE);
		parent.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.marginHeight = 0;
		rl.spacing = 8;
		rl.marginWidth = 0;
		rl.wrap = false;
		rl.fill = true;
		rl.marginBottom = 0;
		rl.marginTop = 0;
		rl.marginLeft = 0;
		rl.marginRight = 0;
		parent.setLayout(rl);

		if (rightActions != null) {
			setActions(parent, rightActions);
		}
	}

	private void setActions(Composite parent, List<Action> actions) {
		Optional.ofNullable(actions).ifPresent(as -> as.forEach(a -> {
			Button btn = createActionUI(parent, a);
			RowData rd = new RowData();
			rd.height = 32;
			btn.setLayoutData(rd);
		}));
	}

	private Button createActionUI(Composite parent, Action a) {
		Button btn;
		btn = toolkit.createButton(parent, a, "compact");
		btn.addListener(SWT.Selection, e -> {
			toolkit.runAction(a, e, service, context);
		});
		return btn;
	}

}
