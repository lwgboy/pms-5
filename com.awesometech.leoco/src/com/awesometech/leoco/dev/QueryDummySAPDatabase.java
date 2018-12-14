package com.awesometech.leoco.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlQuery;

/**
 * 创建模拟SAP测试数据库的程序
 * 
 * @author hua
 *
 */
public class QueryDummySAPDatabase {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private IBruiService brui;

	@Execute
	public void execute() {
		try {
			new SqlQuery("ecology").sql("select * from V_PMS_PO_IN").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_SO_PR").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_SO").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_PR_PO").forEach(d->logger.debug(d.toString()));
			new SqlQuery("ecology").sql("select * from V_PMS_SO_PR_PO_IN").forEach(d->logger.debug(d.toString()));
			Layer.message("查询完成，请查看控制台");
		} catch (Exception e) {
			Layer.message(e.getMessage(), Layer.ICON_CANCEL);
		}
	}

}
