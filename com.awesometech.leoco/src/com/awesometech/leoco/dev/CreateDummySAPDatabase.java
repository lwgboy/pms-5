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
			Connection conn = SqlDB.s.getConnection("erp");
			Statement stmt = conn.createStatement();
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建SO-->Product表
			try {
				stmt.executeUpdate("drop table so");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table so_pr");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table pr_po");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table po_ai");
			} catch (Exception e) {
			}

			stmt.executeUpdate(
					"create table so (so_num varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float)");
			stmt.executeUpdate("insert into so (so_num, prt_num, prt_desc,unit,qty) values ('SO01','P01','产品A','PCS',10)");
			stmt.executeUpdate("insert into so (so_num, prt_num, prt_desc,unit,qty) values ('SO02','P02','产品B','PCS',60)");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建SO-->Pr表
			stmt.executeUpdate(
					"create table so_pr (so_num varchar(20), pr_num varchar(20), pr_idx varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float,rdate varchar(20))");
			stmt.executeUpdate("insert into so_pr (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO01','PR01-1','01','M01','外购件01','PCS',10,'2018-09-01 12:12:12')");
			stmt.executeUpdate("insert into so_pr (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO01','PR01-1','02','M02','外购件02','PCS',20,'2018-09-01 12:12:12')");
			stmt.executeUpdate("insert into so_pr (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO01','PR01-2','01','M03','外购件03','PCS',30,'2018-09-01 12:12:12')");

			stmt.executeUpdate("insert into so_pr (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO02','PR02-1','01','M01','外购件01','PCS',10,'2018-09-01 12:12:12')");
			stmt.executeUpdate("insert into so_pr (so_num,pr_num,pr_idx,prt_num,prt_desc,unit, qty,rdate) values "
					+ "('SO02','PR02-1','02','M04','外购件04','PCS',40,'2018-10-01 12:12:12')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建Pr-->Po表
			stmt.executeUpdate(
					"create table pr_po (pr_num varchar(20), pr_idx varchar(20), po_num varchar(20), po_idx varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float,edate varchar(20))");
			stmt.executeUpdate(
					"insert into pr_po (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','01','PO01','01','M01','外购件01','PCS',3,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into pr_po (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','01','PO01','02','M01','外购件01','PCS',3,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into pr_po (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','01','PO02','01','M01','外购件01','PCS',4,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into pr_po (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR01-1','02','PO03','01','M02','外购件02','PCS',19,'2018-09-01 12:12:12')");
			stmt.executeUpdate(
					"insert into pr_po (pr_num,pr_idx,po_num,po_idx, prt_num,prt_desc,unit,qty,edate) values "
							+ "('PR02-1','02','PO04','01','M04','外购件04','PCS',40,'2018-09-01 12:12:12')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 创建Po-->Ai表
			stmt.executeUpdate(
					"create table po_ai (po_num varchar(20), po_idx varchar(20), prt_num varchar(20), prt_desc varchar(20),unit varchar(20), qty float,adate varchar(20))");
			stmt.executeUpdate(
					"insert into po_ai (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
							+ "('PO01','01','M01','外购件01','PCS',1,'2018-09-02 12:12:12')");
			stmt.executeUpdate(
					"insert into po_ai (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
							+ "('PO01','01','M01','外购件01','PCS',2,'2018-09-03 12:12:12')");
			stmt.executeUpdate(
					"insert into po_ai (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
							+ "('PO01','02','M01','外购件01','PCS',2,'2018-09-02 12:12:12')");
			stmt.executeUpdate(
					"insert into po_ai (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
							+ "('PO02','01','M01','外购件01','PCS',4,'2018-09-02 12:12:12')");
			stmt.executeUpdate(
					"insert into po_ai (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
							+ "('PO03','01','M02','外购件02','PCS',10,'2018-09-02 12:12:12')");
			stmt.executeUpdate(
					"insert into po_ai (po_num,po_idx, prt_num,prt_desc,unit,qty,adate) values "
							+ "('PO03','01','M02','外购件02','PCS',9,'2018-09-02 12:12:12')");
			
			conn.commit();
			stmt.close();
			SqlDB.s.freeConnection("oa", conn);
			Layer.message("测试数据库创建完成");
		} catch (Exception e) {
			MessageDialog.openError(brui.getCurrentShell(), "测试数据库创建错误", e.getMessage());
		}
	}

}
