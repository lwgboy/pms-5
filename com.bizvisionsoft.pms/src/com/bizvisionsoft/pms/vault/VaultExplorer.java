package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;

public abstract class VaultExplorer {

	private static final Logger logger = LoggerFactory.getLogger(VaultExplorer.class);

	private IBruiService br;

	private BruiAssemblyContext context;

	protected GridPart folderPane;

	protected GridPart filePane;

	private IFolder[] currentPath;

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

	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		// ��������������
		Composite bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 32;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).get();

		// ��������ļ���pane
		Composite left = createFolderPane(content);

		Label sep = new Label(content, SWT.SEPARATOR | SWT.VERTICAL);

		// �����Ҳ��ļ�pane
		Composite right = createFilePane(content);

		fd = new FormData();
		left.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(20);
		fd.bottom = new FormAttachment(100);

		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment(left);
		fd.top = new FormAttachment();
		fd.bottom = new FormAttachment(100);
		fd.width = 1;

		fd = new FormData();
		right.setLayoutData(fd);
		fd.left = new FormAttachment(sep);
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
	}

	/**
	 * ��������������
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createBar(Composite parent) {
		List<List<Action>> actions = createToolbarActions();
		currentPath = createDemoPath();
		Composite bar = new AddressBar(parent, currentPath, actions);
		bar.addListener(SWT.SetData, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
		});

		bar.addListener(SWT.Modify, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
			e.doit = true;// ��������Ϊtrue,���ܸı��ַ����Ĭ��Ϊtrue, ��ĳЩ�ļ��н�ֹ����ʱ��������Ϊfalse;
		});

		bar.addListener(SWT.Selection, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ���������¼�: " + ae.action + ", ·����" + msg);
		});
		return bar;
	}

	private IFolder[] createDemoPath() {
		ArrayList<IFolder> path = new ArrayList<IFolder>();
		VaultFolder folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0001");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0���ļ���0000002");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000003");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00004");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000005");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00006");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000007");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0008");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00009");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00���ļ���000010");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000011");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000012");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000013");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0014");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000015");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000016");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000���ļ���0017");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000018");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX000���ļ���00019");
		path.add(folder);

		return path.toArray(new IFolder[0]);
	}

	private List<List<Action>> createToolbarActions() {

		List<List<Action>> result = new ArrayList<>();
		// TODO Ȩ�޿��ƣ���������
		// TODO ����

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
		// ȫ�ֲ���

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
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault\\���Ͽ��ļ��б�.gridassy"))
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
	private Composite createFolderPane(Composite parent) {
		// �����ļ������
		AssemblyContainer left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault\\���Ͽ��ļ���.gridassy"))
				.setServices(br).create();

		folderPane = (GridPart) left.getContext().getContent();
		// ����ļ���ѡ��������������open��ť�������ļ��в�����
		// folderPane.getViewer().getControl().addListener(SWT.Selection, e -> {
		// if ("open/".equals(e.text)) {
		// IFolder parentFolder = (IFolder) e.item.getData();
		// folderPane.getViewer().setSelection(new StructuredSelection(parentFolder));
		// showMenu(parentFolder);
		// }
		// });
		// ������˫������¼���˫���󣬴򿪸��ļ��У���ʾ���µ��ļ���
		// folderPane.getViewer().getControl().addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseDoubleClick(MouseEvent e) {
		// IStructuredSelection s = (IStructuredSelection)
		// folderPane.getViewer().getSelection();
		// IFolder parentFolder = Optional.ofNullable((IFolder)
		// s.getFirstElement()).orElse(null);
		// selectFolderQueryFolder(parentFolder);
		// }
		// });

		// ��ѯ�ļ��У�Ĭ��û���κ�ѡ��
		// selectFolderQueryFolder(null);
		return left.getContainer();
	}
}
