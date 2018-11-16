package com.bizvisionsoft.pms.resource;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.model.Program;
import com.bizvisionsoft.service.model.Project;

public class ResourceChartASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Object input;

	@Init
	private void init() {
		input = context.getRootInput();
		if (input instanceof Project) {
			// ������Ŀ�е���Դ
		} else if (input instanceof Program) {
			// ������Ŀ���е���Դ
			// }else if(input instanceof Portfolio) {//������Ŀ��ϵ���Դ
		} else {// ������ǰ�û�������֯����Դ

		}
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		Composite content = Controls.handle(createBar(parent)).loc(SWT.LEFT | SWT.TOP | SWT.RIGHT, 48)
				.add(() -> Controls.contentPanel(parent).mLoc().layout(new FormLayout())).formLayout().bg(BruiColor.white).get();

		Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT, 0.25f).put(this::leftPane).formLayout()
				.addRight(() -> Controls.label(content, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM, 1))
				.addRight(() -> Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.RIGHT).put(this::rightPane).formLayout());

	}

	private void leftPane(Composite parent) {
		
	}

	private void rightPane(Composite parent) {

	}

	private StickerTitlebar createBar(Composite parent) {
		Action a = new Action();
		a.setName("������Ŀ���ļ���");
		a.setImage("/img/add_16_w.svg");
		a.setTooltips("������Ŀ���ļ���");
		a.setStyle("normal");

		Action b = new Action();
		b.setName("��ѯ");
		b.setImage("/img/search_w.svg");
		b.setTooltips("��ѯ��Ŀ�ĵ�");
		b.setStyle("info");

		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(a, b)).setActions(context.getAssembly().getActions())
				.setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			// if ("������Ŀ���ļ���".equals(((Action) l.data).getName())) {
			// if (createFolder(null)) {
			// folderPane.setViewerInput(getInput());
			// }
			// } else if ("��ѯ".equals(((Action) l.data).getName())) {
			// filePane.openQueryEditor();
			// }
		});
		return bar;
	}

}
