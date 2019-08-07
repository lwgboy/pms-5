package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Part;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceconsumer.Services;

public class FolderDocSelector extends Part {

	public static void selectFolder(IBruiContext context, IFolder initialFolder, int selectionStyle) {
		Shell parentShell = Display.getCurrent().getActiveShell();
		FolderDocSelector selector = new FolderDocSelector(parentShell);
		selector.setParentContext(context).setTitle("ѡ��Ŀ¼").setSizeMode(false, true)
				.setExplorerStyle(VaultExplorer.ADDRESS_BAR | VaultExplorer.NAVIGATOR).setSelectionStyle(selectionStyle);
		selector.init(initialFolder);
		selector.open();
	}

	public static void selectDocument(IBruiContext context, IFolder initialFolder, int selectionStyle) {
		Shell parentShell = Display.getCurrent().getActiveShell();
		FolderDocSelector selector = new FolderDocSelector(parentShell);
		selector.setParentContext(context).setTitle("ѡ���ĵ�").setSizeMode(false, false).setExplorerStyle(VaultExplorer.ADDRESS_BAR
				| VaultExplorer.NAVIGATOR | VaultExplorer.FILETABLE | VaultExplorer.SEARCH_FILE | VaultExplorer.SEARCH_FOLDER)
				.setSelectionStyle(selectionStyle);
		selector.init(initialFolder);
		selector.open();
	}

	public static void selectFolder(IBruiContext context) {
		selectFolder(context, IFolder.Null, SWT.SINGLE);
	}

	public static void selectDocument(IBruiContext context) {
		selectDocument(context, IFolder.Null, SWT.MULTI);
	}

	private boolean isTiny;
	private boolean isSmall;
	private String title;
	private VaultExplorer explorer;
	private BruiService service;
	private String domain;
	private int explorerStyle;
	private BruiAssemblyContext context;
	private int selectionStyle;

	private Object result;

	public FolderDocSelector(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.TITLE | SWT.RESIZE | SWT.ON_TOP | SWT.MAX | SWT.APPLICATION_MODAL);
	}

	public FolderDocSelector setParentContext(IBruiContext parentContext) {
		context = UserSession.newAssemblyContext().setParent(parentContext);
		return this;
	}

	public FolderDocSelector setSizeMode(boolean isTiny, boolean isSmall) {
		this.isTiny = isTiny;
		this.isSmall = isSmall;
		return this;
	}

	public FolderDocSelector setTitle(String title) {
		this.title = title;
		return this;
	}

	public FolderDocSelector setExplorerStyle(int explorerStyle) {
		this.explorerStyle = explorerStyle;
		return this;
	}

	public FolderDocSelector setSelectionStyle(int selectionStyle) {
		this.selectionStyle = selectionStyle;
		return this;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(title);
		super.configureShell(newShell);
	}

	@Override
	protected Point getInitialSize() {
		Rectangle disb = Display.getCurrent().getBounds();

		int width = isSmall ? 820 : 1200;// ��С���
		int height = isTiny ? 480 : 800;// ��С�߶�

		// �����ȴ�����Ļ��ȣ���Ϊ��Ļ���
		width = width > disb.width ? disb.width : width;
		// ����߶ȴ�����Ļ�߶ȣ���Ϊ��Ļ�߶�
		height = height > disb.height ? disb.height : height;

		return new Point(width, height);
	}

	private void init(IFolder initialFolder) {
		service = new BruiService(this);
		domain = service.getDomain();
		explorer = new VaultExplorer() {

			@Override
			protected IFolder[] getPath(IFolder folder) {
				if (IFolder.Null.equals(folder)) {
					return new IFolder[0];
				} else {
					List<VaultFolder> result = Services.get(DocumentService.class).getPath(folder.get_id(), domain);
					return result.toArray(new IFolder[0]);
				}
			}

			@Override
			protected Assembly getFileTableAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/�ļ��б�.gridassy").clone();
				assy.getActions().clear();
				assy.getRowActions().clear();
				if ((VaultExplorer.FILETABLE & explorerStyle) != 0) {// ����ѡ���ļ���ʱ�����ļ��ϲ��й�ѡ��
					assy.setCheckOn(true);
				}
				return assy;
			}

			@Override
			protected Assembly getNavigatorAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/Ŀ¼����.gridassy").clone();
				assy.getActions().clear();
				assy.getRowActions().clear();
				if ((VaultExplorer.FILETABLE & explorerStyle) == 0) {// ����ѡ��Ŀ¼��ʱ����Ŀ¼�ϲ��й�ѡ��
					assy.setCheckOn(true);
				}
				return assy;
			}

			@Override
			public IFolder getInitialFolder() {
				return initialFolder;
			}

			@Override
			protected int getStyle() {
				return explorerStyle;
			}

			@Override
			protected List<List<Action>> createToolbarActions() {
				ArrayList<List<Action>> result = new ArrayList<>();
				if ((VaultExplorer.SEARCH_FOLDER & explorerStyle) != 0) {
					List<Action> row = new ArrayList<>();
					result.add(row);
					row.add(VaultActions.create(VaultActions.findFolder, true, false));
				}

				if ((VaultExplorer.SEARCH_FILE & explorerStyle) != 0) {
					List<Action> row;
					if (result.isEmpty()) {
						row = new ArrayList<>();
						result.add(row);
					} else {
						row = result.get(0);
					}
					row.add(VaultActions.create(VaultActions.findDocuments, true, false));
					row.add(VaultActions.create(VaultActions.search, true, false));
				}

				return result;
			}
		};
		explorer.setBruiService(service).setContext(context);
		explorer.init();
	}

	@Override
	protected void createContents(Composite parent) {
		parent.setLayout(new FormLayout());
		Composite content = Controls.comp(parent).loc(SWT.TOP | SWT.LEFT | SWT.RIGHT).bottom(100, -42).bg(BruiColor.Grey_200).get();
		// ����������
		explorer.createUI(content);
		// ������Ť��
		
		Controls.label(parent,SWT.SEPARATOR|SWT.HORIZONTAL).loc(SWT.LEFT|SWT.RIGHT).top(content);
		Composite bar = Controls.comp(parent).loc(SWT.LEFT | SWT.RIGHT | SWT.BOTTOM).top(content, 1).get();
		createButtons(bar);
	}

	private void createButtons(Composite parent) {
		FormLayout layout = new FormLayout();
		layout.marginHeight = 4;
		layout.marginWidth = 4;
		layout.spacing = 4;
		parent.setLayout(layout);
		// TODO�˴����ְ�
		Controls<Button> btn = Controls.button(parent).rwt(Controls.CSS_NORMAL).setText("ȷ��").loc(SWT.RIGHT | SWT.TOP | SWT.BOTTOM)
				.width(120).listen(SWT.Selection, e -> okPressed());

		// ȡ����ť
		if (((SWT.SINGLE | SWT.MULTI) & selectionStyle) != 0) {
			Controls.button(parent).rwt(Controls.CSS_WARNING).setText("ȡ��").loc(SWT.TOP | SWT.BOTTOM).right(btn.get()).width(120).listen(SWT.Selection,
					e -> cancelPressed());
		}

		// ȫѡ��ȡ��ȫѡ
		if ((SWT.MULTI & selectionStyle) != 0) {
			btn = Controls.button(parent).rwt(Controls.CSS_INFO).setText("ȫ��ѡ��").loc(SWT.LEFT | SWT.TOP | SWT.BOTTOM).width(120).listen(SWT.Selection,
					e -> selectAllPressed());
			Controls.button(parent).rwt(Controls.CSS_INFO).setText("ȡ��ѡ��").loc(SWT.TOP | SWT.BOTTOM).left(btn.get()).width(120)	.listen(SWT.Selection,
					e -> unSelectAllPressed());
		}
	}

	private void unSelectAllPressed() {
		// TODO Auto-generated method stub

	}

	private void selectAllPressed() {
		// TODO Auto-generated method stub

	}

	private void cancelPressed() {
		setReturnCode(Window.CANCEL);
		close();
	}

	private void okPressed() {
		// TODO Auto-generated method stub
		setReturnCode(Window.OK);
		close();
	}

	public Object getResult() {
		return result;
	}

}
