package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IFolder;

public abstract class VaultExplorer {

	private static final Logger logger = LoggerFactory.getLogger(VaultExplorer.class);

	private IBruiService br;

	private BruiAssemblyContext context;

	protected GridPart folderPane;

	protected GridPart filePane;

	private AddressBar addressBar;

	private IFolder currentFolder;

	public VaultExplorer() {
	}

	public VaultExplorer setContext(BruiAssemblyContext context) {
		this.context = context;
		return this;
	}

	public VaultExplorer setBruiService(IBruiService brui) {
		this.br = brui;
		return this;
	}

	protected void init() {
		currentFolder = getCurrentFolder();
		context.setInput(currentFolder);
	}

	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		// ��������������
		Composite bar = Controls.handle(createAddressBar(parent)).loc(SWT.LEFT | SWT.TOP | SWT.RIGHT).height(32).get();

		Composite navigator = Controls.handle(createNaviPane(parent)).loc(SWT.LEFT | SWT.BOTTOM, .25f).top(bar).formLayout().get();

		Controls.handle(createFilePane(parent)).loc(SWT.RIGHT | SWT.BOTTOM).left(navigator, 1).top(bar).formLayout().get();
	}

	/**
	 * ����ʵ�ֱ����������ظ�Ŀ¼�嵥
	 * 
	 * @return
	 */
	public abstract IFolder getCurrentFolder();

	/**
	 * ��������������
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createAddressBar(Composite parent) {
		List<List<Action>> actions = createToolbarActions();
		
		IFolder[] path = getPath(currentFolder);
		
		addressBar = new AddressBar(parent, path, actions, this::checkAuthority);
		addressBar.addListener(SWT.SetData, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
		});

		addressBar.addListener(SWT.Modify, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
			e.doit = true;// ��������Ϊtrue,���ܸı��ַ����Ĭ��Ϊtrue, ��ĳЩ�ļ��н�ֹ����ʱ��������Ϊfalse;
		});

		addressBar.addListener(SWT.Selection, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ���������¼�: " + ae.action + ", ·����" + msg);
		});
		return addressBar;
	}

	protected abstract IFolder[] getPath(IFolder folder) ;

	private List<List<Action>> createToolbarActions() {
		List<List<Action>> result = new ArrayList<>();

		List<Action> actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.createSubFolder, true, true));
		actions.add(VaultActions.create(VaultActions.createDocument, true, true));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.findSubFolder, true, true));
		actions.add(VaultActions.create(VaultActions.findDocuments, true, true));
		actions.add(VaultActions.create(VaultActions.search, true, false));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.sortDocuments, true, false));
		actions.add(VaultActions.create(VaultActions.addFavour, true, false));
		actions.add(VaultActions.create(VaultActions.setFolderProperties, true, false));
		result.add(actions);
		return result;
	}

	/**
	 * �����ļ�pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFilePane(Composite parent) {
		// �����ļ����
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault/���Ͽ��ļ��б�.gridassy"))
				.setServices(br).create();

		filePane = (GridPart) right.getContext().getContent();
		// // �����ļ���ѡ��ʱ��������ѡ����޸��ļ������ѯ
		// folderPane.getViewer()
		// .addPostSelectionChangedListener(e -> selectFolderQueryFile((IFolder)
		// e.getStructuredSelection().getFirstElement()));
		// selectFolderQueryFile(null);
		return right.getContainer();
	}

	/**
	 * �����ļ���pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createNaviPane(Composite parent) {
		// �����ļ������
		AssemblyContainer left = new AssemblyContainer(parent, context)//
				.setAssembly(getNavigatorAssembly())//
				.setServices(br)//
				.setInput(currentFolder)//
				.create();

		return left.getContainer();
	}

	protected abstract Assembly getNavigatorAssembly() ;

	/**
	 * // TODO ���Ȩ��
	 * 
	 * @param a
	 * @return
	 */
	private boolean checkAuthority(Action a) {
		return true;
	}
}
