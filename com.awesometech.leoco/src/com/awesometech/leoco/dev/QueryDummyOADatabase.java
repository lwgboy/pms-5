package com.awesometech.leoco.dev;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

public class QueryDummyOADatabase {

	
	@Inject
	private IBruiService brui;

	@Execute
	public void execute() {
		try {
			new SqlQuery("oa").sql("select * from wf_node").forEach(d->System.out.println(d));
			new SqlQuery("oa").sql("select * from wf_link").forEach(d->System.out.println(d));
			Layer.message("��ѯ��ɣ���鿴����̨");
		} catch (Exception e) {
			Layer.message(e.getMessage(), Layer.ICON_CANCEL);
		}
	}
}
