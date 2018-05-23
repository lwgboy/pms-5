package com.bizvisionsoft.demo.rsclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.timeline.TimeLine;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class TimeLineDemo {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	private Composite content;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		createTimeLine(parent);
	}

	private void createTimeLine(Composite parent) {
		TimeLine tl = new TimeLine(parent, SWT.BORDER);
		tl.append("3��17������", "������ɽṹ��ѧ���飬���������㼼�����Ҫ���й���������ѳ������鱨�档");
		tl.append("8��17��", "��ͻ�Ҫ��ı仯���漰��Aģ��ദ�з����ģ�����������������з�������躷�����Ŀ�����Ԥ����Ŀ������30�졣");
		tl.append("8��18��", "�����з���ɡ�");
		tl.append("8��20��", "������´���Aģ��ṹ�з���Bģ��ṹ�з��ȹ����ļƻ���");
		tl.append("8��22��", "����������������Ҫ������������ɡ�");
		tl.append("9��24��", "������ύ��Aģ�����յ��з��ɹ��");
		tl.append("10��9��", "Aģ���з�����������ɡ�");
		tl.append("11��20��", "����躷�����Aģ����з���������");
	}


}
