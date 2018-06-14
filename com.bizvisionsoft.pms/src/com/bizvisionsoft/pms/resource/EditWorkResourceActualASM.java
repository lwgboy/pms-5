package com.bizvisionsoft.pms.resource;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
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
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkReportAssignment;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditWorkResourceActualASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Calendar start;

	private Calendar end;

	private GridTableViewer viewer;

	private List<WorkResourcePlanDetail> works;

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		Action closeAction = new Action();
		closeAction.setName("close");
		closeAction.setImage("/img/close.svg");

		Action addAction = new Action();
		addAction.setName("add");
		addAction.setText("�����Դ����");
		addAction.setForceText(true);
		addAction.setStyle("normal");

		StickerTitlebar bar = new StickerTitlebar(parent, closeAction, Arrays.asList(addAction))
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
		fd.left = new FormAttachment(0, 12);
		fd.top = new FormAttachment(bar, 12);
		fd.right = new FormAttachment(100, -12);
		fd.bottom = new FormAttachment(100, -12);
		content.setLayout(new FillLayout(SWT.VERTICAL));

		ResourceAssignment ra = (ResourceAssignment) context.getInput();

		bar.setText("��Դʵ������ ");

		// ȡ����ʼ�����ʱ��

		WorkService service = Services.get(WorkService.class);
		Work work = service.getWork(ra.work_id);
		start = Calendar.getInstance();
		start.setTime(ra.from);
		end = Calendar.getInstance();
		end.setTime(ra.to);
		//
		createViewer(content);
		//
		// TODO ��װ�ɷ���
		// works = service.listConflictWorks(ra.work_id);
		// viewer.setInput(works);
	}

	private void allocateResource() {
		// ��ʾ��Դѡ���
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("������Դ");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("�豸��Դ");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("��Դ����");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// ����menu
		new ActionMenu(brui).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("��������-���������Դ�༭��");
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("��������-����豸��Դ�༭��");
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("��������-�����Դ���ͱ༭��");
			return false;
		}).open();
	}

	private void addResource(String editorId) {
		// TODO

	}

	private void createViewer(Composite parent) {
		viewer = new GridTableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);

		Column c = new Column();
		c.setName("resId");
		c.setText("��Դ���");
		c.setWidth(160);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("type");
		c.setText("��Դ����");
		c.setWidth(160);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("name");
		c.setText("����");
		c.setWidth(320);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(start.getTimeInMillis());
		createDateColumn(now.getTime());
		while (now.before(end)) {
			now.add(Calendar.DATE, 1);
			createDateColumn(now.getTime());
		}

		viewer.setContentProvider(ArrayContentProvider.getInstance());

	}

	private void createDateColumn(Date now) {
		GridColumn col = new GridColumn(viewer.getGrid(), SWT.CENTER);
		col.setText(new SimpleDateFormat("M/d").format(now.getTime()));
		col.setWidth(48);
		col.setMoveable(false);
		col.setResizeable(false);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// ĳ�����幤ʱ
				return "";
			}
		});
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
				// TODO
				if ("resId".equals(name)) {
					return "";
				} else if ("type".equals(name)) {
					return "";
				} else if ("name".equals(name)) {
					return "";
				}
				return "";
			}
		});

	}
}
