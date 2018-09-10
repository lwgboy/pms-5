package com.awesometech.leoco.dev;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

/**
 * ����ģ��SAP�������ݿ�ĳ���
 * 
 * @author hua
 *
 */
public class QueryDummySAPDatabase {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute() {
		try {
			new SqlQuery("ecology").sql("select * from V_PMS_PO_IN").forEach(d->System.out.println(d));
			new SqlQuery("ecology").sql("select * from V_PMS_SO_PR").forEach(d->System.out.println(d));
			new SqlQuery("ecology").sql("select * from V_PMS_SO").forEach(d->System.out.println(d));
			new SqlQuery("ecology").sql("select * from V_PMS_PR_PO").forEach(d->System.out.println(d));
			Layer.message("��ѯ��ɣ���鿴����̨");
		} catch (Exception e) {
			Layer.message(e.getMessage(), Layer.ICON_CANCEL);
		}
	}

}
