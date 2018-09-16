package com.awesometech.leoco.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

public class QueryDummyOADatabase {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private IBruiService brui;

	@Execute
	public void execute() {
		try {
			new SqlQuery("ecology").sql("select * from V_PMS_wf_type").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_wf").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_wf_inst").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_wf_node").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_wf_link").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_wf_log").forEach(d->logger.debug(d.toString()));
			Layer.message("查询完成，请查看控制台");
		} catch (Exception e) {
			Layer.message(e.getMessage(), Layer.ICON_CANCEL);
		}
	}
}
