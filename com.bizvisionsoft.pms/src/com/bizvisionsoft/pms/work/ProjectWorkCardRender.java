package com.bizvisionsoft.pms.work;

import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;

public class ProjectWorkCardRender extends WorkCardRender {

	private String userId;

	@Init
	protected void init() {
		super.init();
		userId = getBruiService().getCurrentUserId();
	}

	@Override
	protected void showFirstRow(Work work, CardTheme theme, StringBuffer sb) {
		// ��ʾ�׶�ͼ�������
		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));
	}

	@Override
	protected void showButtons(Work work, CardTheme theme, StringBuffer sb, String label, String href) {
		// ���ݵ�ǰ�û��ж��Ƿ���ʾ������ť
		renderButtons(theme, sb, work, Check.equals(userId, work.getChargerId()), label, href);
	}

}
