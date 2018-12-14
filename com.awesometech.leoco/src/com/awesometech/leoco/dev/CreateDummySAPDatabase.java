package com.awesometech.leoco.dev;

import java.sql.Connection;
import java.sql.Statement;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.sqldb.SqlDB;

/**
 * 创建模拟SAP测试数据库的程序
 * 
 * @author hua
 *
 */
public class CreateDummySAPDatabase {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute() {
		try {
			Connection conn = SqlDB.s.getConnection("ecology");
			Statement stmt = conn.createStatement();

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建SO-->Product表
			try {
				stmt.executeUpdate("drop table V_PMS_SO");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_SO_PR");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_PR_PO");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_PO_IN");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_SO_PR_PO_IN");
			} catch (Exception e) {
			}

			stmt.executeUpdate(
					"create table V_PMS_SO (so_num varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float)");
			stmt.executeUpdate(
					"insert into V_PMS_SO (so_num, prt_num, prt_desc,unit,qty) values ('SO01','P01','产品A','PCS',10)");
			stmt.executeUpdate(
					"insert into V_PMS_SO (so_num, prt_num, prt_desc,unit,qty) values ('SO02','P02','产品B','PCS',60)");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建SO-->Pr表
			stmt.executeUpdate(
					"create table V_PMS_SO_PR (so_num varchar(20), pr_num varchar(20), pr_idx varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float,rdate varchar(20))");
			stmt.executeUpdate("insert into V_PMS_SO_PR (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO01','PR01-1','01','M01','外购件01','PCS',10,'2018-09-01 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_SO_PR (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO01','PR01-1','02','M02','外购件02','PCS',20,'2018-09-01 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_SO_PR (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO01','PR01-2','01','M03','外购件03','PCS',30,'2018-09-01 12:12:12')");

			stmt.executeUpdate("insert into V_PMS_SO_PR (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO02','PR02-1','01','M01','外购件01','PCS',10,'2018-09-01 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_SO_PR (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO02','PR02-1','02','M04','外购件04','PCS',40,'2018-10-01 12:12:12')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建Pr-->Po表
			stmt.executeUpdate(
					"create table V_PMS_PR_PO (pr_num varchar(20), pr_idx varchar(20), po_num varchar(20), po_idx varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float,edate varchar(20))");
			stmt.executeUpdate(
					"insert into V_PMS_PR_PO (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','01','PO01','01','M01','外购件01','PCS',3,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into V_PMS_PR_PO (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','01','PO01','02','M01','外购件01','PCS',3,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into V_PMS_PR_PO (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','01','PO02','01','M01','外购件01','PCS',4,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into V_PMS_PR_PO (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','02','PO03','01','M02','外购件02','PCS',19,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into V_PMS_PR_PO (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR02-1','02','PO04','01','M04','外购件04','PCS',40,'2018-09-01 12:12:12')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建Po-->Ai表
			stmt.executeUpdate(
					"create table V_PMS_PO_IN (po_num varchar(20), po_idx varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float,adate varchar(20))");
			stmt.executeUpdate("insert into V_PMS_PO_IN (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
					+ "('PO01','01','M01','外购件01','PCS',1,'2018-09-02 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_PO_IN (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
					+ "('PO01','01','M01','外购件01','PCS',2,'2018-09-03 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_PO_IN (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
					+ "('PO01','02','M01','外购件01','PCS',2,'2018-09-02 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_PO_IN (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
					+ "('PO02','01','M01','外购件01','PCS',4,'2018-09-02 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_PO_IN (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
					+ "('PO03','01','M02','外购件02','PCS',10,'2018-09-02 12:12:12')");
			stmt.executeUpdate("insert into V_PMS_PO_IN (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
					+ "('PO03','01','M02','外购件02','PCS',9,'2018-09-02 12:12:12')");

			// 创建Po-->Ai表
			stmt.executeUpdate(
					"create table V_PMS_SO_PR_PO_IN (so_qty float,pr_qty float,po_qty float,po_sumqty float,in_qty float,so_num nvarchar(20),so_num_lineno nvarchar(20),customerno nvarchar(20),so_prt_num nvarchar(20),so_prt_desc nvarchar(40),so_unit nvarchar(20),insno nvarchar(20),in_work nvarchar(20),in_adate nvarchar(20)(20),in_localtion nvarchar(20),in_unit nvarchar(20),pr_rdate nvarchar(20),po_num nvarchar(20),po_idx nvarchar(20),po_prt_num nvarchar(20),po_prt_desc nvarchar(40),po_edate nvarchar(20),pr_num nvarchar(20),pr_idx nvarchar(20),pr_prt_num nvarchar(20),pr_prt_desc nvarchar(40),pr_work nvarchar(20),pr_unit nvarchar(20))");
//			stmt.executeUpdate("insert into V_PMS_PO_IN (so_qty,pr_qty,po_qty,po_sumqty,in_qty,so_num,so_num_lineno,customerno,so_prt_num,so_prt_desc,so_unit,insno,in_work,in_adate,in_localtion,in_unit,pr_rdate,po_num,po_idx,po_prt_num,po_prt_desc,po_edate,pr_num,pr_idx,pr_prt_num,pr_prt_desc,pr_work,pr_unit) values "
//					+ "('PO01','01','M01','外购件01','PCS',1,'2018-09-02 12:12:12')");

			conn.commit();
			stmt.close();
			SqlDB.s.freeConnection("oa", conn);
			Layer.message("测试数据库创建完成");
		} catch (Exception e) {
			MessageDialog.openError(brui.getCurrentShell(), "测试数据库创建错误", e.getMessage());
		}
	}

}
