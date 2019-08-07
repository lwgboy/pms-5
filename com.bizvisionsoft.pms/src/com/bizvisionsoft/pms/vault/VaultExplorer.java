package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.bson.Document;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.BruiQueryEngine;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.BruiToolkit;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.UniversalDataService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public abstract class VaultExplorer {

	private static final Logger logger = LoggerFactory.getLogger(VaultExplorer.class);

	private IBruiService br;

	private BruiAssemblyContext context;

	private BruiAssemblyContext contextNavi;

	protected GridPart filePane;

	private AddressBar addressBar;

	private Composite navigator;

	private Composite fileSearchResultTable;

	private Composite folderSearchResultTable;

	private Composite fileTable;

	protected static final int ADDRESS_BAR = 1 << 2;
	protected static final int NAVIGATOR = 1 << 3;
	protected static final int FILETABLE = 1 << 4;
	protected static final int SEARCH_FOLDER = 1 << 5;
	protected static final int SEARCH_FILE = 1 << 6;

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
		context.setInput(getInitialFolder());
	}

	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		if ((ADDRESS_BAR & getStyle()) != 0)
			addressBar = Controls.handle(createAddressBar(parent)).loc(SWT.LEFT | SWT.TOP | SWT.RIGHT).height(32).get();

		if ((NAVIGATOR & getStyle()) != 0) {
			Controls<Composite> controls = Controls.handle(createNaviPane(parent)).loc(SWT.LEFT | SWT.BOTTOM).top(addressBar);
			if (((FILETABLE | SEARCH_FOLDER | SEARCH_FILE) & getStyle()) != 0) {
				controls.right(0.25f);
			} else {
				controls.right();
			}
			navigator = controls.get();
		}

		if ((FILETABLE & getStyle()) != 0)
			fileTable = Controls.handle(createFilePane(parent)).loc(SWT.RIGHT | SWT.BOTTOM).top(addressBar).left(navigator).formLayout()
					.get();

		if ((SEARCH_FOLDER & getStyle()) != 0)
			folderSearchResultTable = Controls.handle(createSearchPane(parent)).loc(SWT.RIGHT | SWT.BOTTOM).top(addressBar).left(navigator)
					.formLayout().get();

		if ((SEARCH_FILE & getStyle()) != 0)
			fileSearchResultTable = Controls.handle(createSearchPane(parent)).loc(SWT.RIGHT | SWT.BOTTOM).top(addressBar).left(navigator)
					.formLayout().get();

	}

	protected int getStyle() {
		return ADDRESS_BAR | NAVIGATOR | FILETABLE;
	}

	public static void main(String[] args) {
		System.out.println(((SEARCH_FILE | FILETABLE) & (ADDRESS_BAR | NAVIGATOR | FILETABLE)) != 0);
	}

	private Composite createSearchPane(Composite parent) {
		return null;
	}

	/**
	 * ����ʵ�ֱ����������ظ�Ŀ¼�嵥
	 * 
	 * @return
	 */
	public abstract IFolder getInitialFolder();

	/**
	 * ��������������
	 * 
	 * @param parent
	 * @return
	 */
	private AddressBar createAddressBar(Composite parent) {
		List<List<Action>> actions = createToolbarActions();

		IFolder[] path = getPath(context.getInput(IFolder.class, true));

		AddressBar addressBar = new AddressBar(parent, path, actions, this::checkActionAuthorityOfCurrentFolder);
		addressBar.addListener(SWT.SetData, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
		});

		addressBar.addListener(SWT.Modify, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			e.doit = doPathModified(ae.path);// ��������Ϊtrue,���ܸı��ַ����Ĭ��Ϊtrue, ��ĳЩ�ļ��н�ֹ����ʱ��������Ϊfalse;
		});

		addressBar.addListener(SWT.Selection, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			handlerEvent(ae.path, ae.action);
			logger.debug("��ַ���������¼�: " + ae.action + ", ·����" + msg);
		});
		return addressBar;
	}

	private void handlerEvent(IFolder[] path, Action action) {
		IFolder folder = getOpenFolder(path);
		if (VaultActions.createSubFolder.name().equals(action.getName())) {
			if (folder == null)
				Layer.error("��ѡ��Ҫ����Ŀ¼��Ŀ¼��");
			else if (canCreateSubFolder(folder))
				doCreateSubFolder(folder);
			else
				Layer.error("��ǰĿ¼��ֹ����Ŀ¼��");
		} else if (VaultActions.createDocument.name().equals(action.getName())) {
			if (canCreateDocument(folder))
				doCreateDocument(folder);
			else
				Layer.error("��ǰĿ¼��ֹ�����ĵ���");
		} else if (VaultActions.findDocuments.name().equals(action.getName())) {
			// TODO �����޶�����ǰ�ļ���
			if (folder != null)
				openFileQueryEditor(new BasicDBObject("parent_id", folder.get_id()));
			else
				openFileQueryEditor(null);
		} else if (VaultActions.search.name().equals(action.getName())) {
			openFileQueryEditor(null);
		} else if (VaultActions.sortDocuments.name().equals(action.getName())) {
			filePane.openSortEditor();
		} else if (VaultActions.addFavour.name().equals(action.getName())) {

		} else if (VaultActions.setFolderProperties.name().equals(action.getName())) {

		}
		logger.debug("��ַ���������¼�: " + action);
	}

	private void openFileQueryEditor(BasicDBObject defQuery) {
		Assembly config = (Assembly) filePane.getConfig().clone();
		config.getActions().clear();
		IBruiContext context = filePane.getContext();
		IBruiService bruiService = filePane.getBruiService();
		Assembly c = (Assembly) AUtil.simpleCopy(config, new Assembly());
		c.setType(Assembly.TYPE_EDITOR);
		String title = Stream.of(c.getStickerTitle(), c.getTitle(), c.getName()).filter(Check::isAssigned).findFirst().map(t -> " - " + t)
				.orElse("");
		c.setTitle("��ѯ" + title);

		c.setSmallEditor(true);
		c.setTinyEditor(true);

		String bundleId = config.getQueryBuilderBundle();
		String classId = config.getQueryBuilderClass();
		Object input;
		if (Check.isAssigned(bundleId, classId)) {
			input = BruiQueryEngine.create(bundleId, classId, bruiService, context).getTarget();
		} else {
			input = new Document();
		}

		Editor.create(c, context, input, true).ok((r, t) -> {
			if (defQuery != null)
				r.putAll(defQuery.toMap());
			filePane.doQuery(r);
		});

	}

	private boolean canCreateDocument(IFolder folder) {
		return folder != null && checkFolderAuthority(folder, VaultActions.createDocument.name());
	}

	private void doCreateDocument(IFolder folder) {
		Selector.open("/vault/������ѡ����.selectorassy", context, (VaultFolder) folder.getContainer(), l -> {
			// TODO
			Document doc = (Document) l.get(0);
			String name = doc.getString("name");

			Editor.open(name, context, new Document(), (r, t) -> {
				UniversalCommand command = new UniversalCommand().setTargetClassName(Docu.class.getName())
						.addParameter(MethodParam.OBJECT, t).setTargetCollection("docu");
				UniversalResult ur = Services.get(UniversalDataService.class).insert(command, br.getDomain());
				filePane.insert(ur.getValue());
			});
		});
	}

	private boolean canCreateSubFolder(IFolder folder) {
		return folder != null && checkFolderAuthority(folder, VaultActions.createSubFolder.name());
	}

	private void doCreateSubFolder(IFolder folder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "����Ŀ¼", "Ŀ¼����", null, t -> {
			return t.trim().isEmpty() ? "����������" : null;
		});
		if (InputDialog.OK == id.open()) {
			String domain = br.getDomain();
			if (folder instanceof VaultFolder) {
				VaultFolder vaultFolder = (VaultFolder) folder;
				VaultFolder vf = vaultFolder.getSubFolderInstance(domain);
				vf.setDesc(id.getValue());
				vf = Services.get(DocumentService.class).insertFolder(vf, domain);
				Layer.message("Ŀ¼�����ɹ���");
				GridPart navi = (GridPart) contextNavi.getContent();
				navi.insert(vf);
			}
		}
	}

	private IFolder getOpenFolder(IFolder[] path) {
		if (Check.isAssigned(path)) {
			IFolder folder = path[path.length - 1];
			logger.debug("��ַ���������¼�����ǰĿ¼" + folder);
			return folder;
		}
		return null;
	}

	protected abstract IFolder[] getPath(IFolder folder);

	private List<List<Action>> createToolbarActions() {
		List<List<Action>> result = new ArrayList<>();

		List<Action> actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.createSubFolder, true, true));
		actions.add(VaultActions.create(VaultActions.createDocument, true, true));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.findFolder, true, true));
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

	private Composite createNaviPane(Composite parent) {
		Composite naviPane = Controls.comp(parent).formLayout().get();
		Control bar = Controls.handle(createNaviToolbarPane(naviPane)).loc(SWT.TOP | SWT.LEFT | SWT.RIGHT, 35).get();
		Controls.handle(createFolderPane(naviPane)).loc(SWT.LEFT | SWT.RIGHT | SWT.BOTTOM).top(bar);
		return naviPane;
	}

	private Control createNaviToolbarPane(Composite parent) {
		Composite bar = Controls.comp(parent).rwt(BruiToolkit.CSS_BAR_TITLE).bg(BruiColor.Grey_50).formLayout().get();
		Text text = Controls.text(bar).margin(2).mLoc().get();
		text.addListener(SWT.KeyDown, e -> {
			if (e.keyCode == 13) {
				queryFolder(text.getText());
			}
		});
		text.setMessage("����Ŀ¼");
		Controls.button(bar).rwt("compact").setImageText(VaultActions.search.getImg(), null, 16, 32).margin(2)
				.mLoc(SWT.TOP | SWT.BOTTOM | SWT.RIGHT).width(32).above(null).listen(SWT.MouseDown, e -> queryFolder(text.getText())).get();
		return bar;
	}

	private void queryFolder(String text) {
		GridPart navi = (GridPart) contextNavi.getContent();
		BasicDBObject query = new BasicDBObject("desc", Pattern.compile(text, Pattern.CASE_INSENSITIVE));
		navi.doQuery(query);
		logger.debug("����Ŀ¼��" + text);
	}

	/**
	 * �����ļ���pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFolderPane(Composite parent) {
		// �����ļ������
		Assembly assy = getNavigatorAssembly();
		// �������ʽ�޸�
		assy.setGridPageControlStyle("SHORT");
		// �������
		AssemblyContainer ac = new AssemblyContainer(parent, context)//
				.setAssembly(assy)//
				.setServices(br)//
				.setInput(context.getInput(IFolder.class, true))//
				.create();
		contextNavi = ac.getContext();

		((GridPart) contextNavi.getContent()).getViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				IFolder input = (IFolder) selection.getFirstElement();
				if (checkFolderAuthority(input, VaultActions.openFolder.label())) {
					doSetCurrentFolder(input);
				}
				logger.debug("doubleClick:" + input);
			}
		});

		return ac.getContainer();
	}

	protected abstract Assembly getNavigatorAssembly();

	/**
	 * 
	 * @param a
	 * @return
	 */
	private boolean checkActionAuthorityOfCurrentFolder(Action a) {
		return checkFolderAuthority(context.getInput(IFolder.class, false), a.getName());
	}

	private boolean checkFolderAuthority(IFolder input, String actionName) {
		// TODO ���Ȩ��
		return true;
	}

	private boolean doPathModified(IFolder[] path) {
		IFolder folder;
		if (Check.isNotAssigned(path)) {
			folder = getInitialFolder();
		} else {
			folder = path[path.length - 1];
		}
		context.setInput(folder);
		contextNavi.setInput(folder);
		GridPart navi = (GridPart) contextNavi.getContent();
		navi.doRefresh();

		// TODO ���Ȩ�ޣ��Ƿ���������Ŀ¼
		return true;
	}

	private void doSetCurrentFolder(IFolder folder) {
		context.setInput(folder);
		contextNavi.setInput(folder);
		IFolder[] path = getPath(folder);
		addressBar.setPath(path);
		GridPart navi = (GridPart) contextNavi.getContent();
		navi.doRefresh();
		// TODO ���������ĵ�input����

	}

	private void doDeleteFolder(IFolder folder) {
		try {
			// TODO �������쳣�����û��������ɾ���������½��ļ��в�����ɺ󣬽��в��ԣ�
			Services.get(DocumentService.class).deleteVaultFolder(folder.get_id(), br.getDomain());
			Layer.message("�ļ�����ɾ����");
			GridPart navi = (GridPart) contextNavi.getContent();
			navi.remove(folder);
			logger.debug("doDeleteFolder:" + folder);
		} catch (Exception e) {
			Layer.error(e);
		}
	}

	private void doMoveFolder(IFolder folder) {
		// TODO ����ѡ���ļ���
		Selector.open("", context, null, l -> {
			try {
				// TODO δ����
				Services.get(DocumentService.class).moveVaultFolder(folder.get_id(), ((IFolder) l.get(0)).get_id(), br.getDomain());
				Layer.message("�ļ������ƶ���");
				GridPart navi = (GridPart) contextNavi.getContent();
				navi.remove(folder);
				logger.debug("doMoveFolder:" + folder);
			} catch (Exception e) {
				Layer.error(e);
			}
		});

	}

	private void doRenameFolder(IFolder folder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "�ļ��и���", "�µ�����", null, t -> {
			return t.trim().isEmpty() ? "����������" : null;
		});
		if (InputDialog.OK == id.open()) {
			String name = id.getValue();
			try {
				Services.get(DocumentService.class).renameVaultFolder(folder.get_id(), name, br.getDomain());
				Layer.message("�ļ�������������");
				folder.setName(name);
				GridPart navi = (GridPart) contextNavi.getContent();
				navi.refresh(folder);
				logger.debug("doRenameFolder:" + folder);
			} catch (Exception e) {
				Layer.error("����ͬ�����ļ��С�");
			}
		}
	}

	public void handleAction(IFolder folder, Action action) {
		boolean authorized = checkFolderAuthority(folder, action.getName());
		if (!authorized)
			return;

		String name = action.getName();
		if (VaultActions.openFolder.name().equals(name)) {
			doSetCurrentFolder(folder);
		} else if (VaultActions.deleteFolder.name().equals(name)) {
			doDeleteFolder(folder);
		} else if (VaultActions.moveFolder.name().equals(name)) {
			doMoveFolder(folder);
		} else if (VaultActions.renameFolder.name().equals(name)) {
			doRenameFolder(folder);
		}

	}

	public boolean enableOpenFolder(IFolder folder) {

		return checkFolderAuthority(folder, VaultActions.openFolder.name());
	}

	public boolean enableMoveFolder(IFolder folder) {
		IFolder[] path = addressBar.getCurrentPath();
		if (path.length == 0) {
			return false;
		}

		return checkFolderAuthority(folder, VaultActions.moveFolder.name());
	}

	public boolean enableDeleteFolder(IFolder folder) {
		IFolder[] path = addressBar.getCurrentPath();
		if (path.length == 0) {
			return false;
		}
		return checkFolderAuthority(folder, VaultActions.deleteFolder.name());
	}

	public boolean enableRenameFolder(IFolder folder) {
		IFolder[] path = addressBar.getCurrentPath();
		if (path.length == 0) {
			return false;
		}
		return checkFolderAuthority(folder, VaultActions.renameFolder.name());
	}
}
