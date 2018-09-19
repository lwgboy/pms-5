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
public class CreateDummyOADatabase {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute() {
		try {
			Connection conn = SqlDB.s.getConnection("ecology");
			Statement stmt = conn.createStatement();
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			try {
				stmt.executeUpdate("drop table V_PMS_wf_type");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_wf");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_wf_inst");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_wf_node");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_wf_link");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table V_PMS_wf_log");
			} catch (Exception e) {
			}
			
			//-------------流程分类表----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_type (id varchar(20), type_name varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_type (id,type_name) values ('0001','费用相关')");
			stmt.executeUpdate("insert into V_PMS_wf_type (id,type_name) values ('0002','服务支持')");
			stmt.executeUpdate("insert into V_PMS_wf_type (id,type_name) values ('0003','人事管理')");
			
			//-------------流程定义表----------------
			stmt.executeUpdate(
					"create table V_PMS_wf (id varchar(20), type_name varchar(20),wf_name varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF01','0001','出差申请')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF02','0001','借款申请')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF03','0002','电子邮箱申请')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF04','0003','用工需求')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF05','0003','职位调整')");
			
			//-------------流程实例表----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_inst (id varchar(20), wf_id varchar(20), cur_node_id varchar(20), status varchar(20), inst_name varchar(20), create_date varchar(20), creater varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('0000001','WF01','05,01','1','XXX出差香港申请','2018-09-01 22:11:12','XXX')");
			stmt.executeUpdate("insert into V_PMS_wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('0000002','WF01','10','1','XXX出差深圳申请','2018-02-07 08:13:00','XXX')");
			stmt.executeUpdate("insert into V_PMS_wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('0000003','WF01','01,08','1','XXX出差北京申请','2018-11-11 15:12:00','XXX')");
			
			//----------流程活动日志---------------
			stmt.executeUpdate(
					"create table V_PMS_wf_log (inst_id varchar(20), node_id varchar(20),node_name varchar(20),opr_dat varchar(20),operator varchar(20),lastname varchar(20),tgt varchar(20),comment varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000001','01','编制','2016-09-01 22:11:12','XXX','XX','02','编制完成')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000001','02','审核','2016-09-01 22:11:12','XXX','XX','03','完成')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000001','03','校核','2016-09-01 22:11:12','XXX','XX','04','完成')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000002','01','编制','2016-09-01 22:11:12','XXX','XX','02','编制完成')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000003','01','编制','2016-09-01 22:11:12','XXX','XX','02','编制完成')");
			
			
			//-------------流程节点定义表----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_node (wf_id varchar(20), id varchar(20), text varchar(20),foreground varchar(6),background varchar(6))");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','01','编制','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','02','审核','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','03','校核','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','04','批准','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','05','返工','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','06','质量检查','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','07','材料','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','08','复查','ffffff','b0120a')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','09','测试','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','10','模具开发','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','11','模具检查','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','12','试模','ffffff','455a64')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//-------------流程连线定义表----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_link (wf_id varchar(20),src varchar(20), tgt varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','01','02')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','01','03')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','02','04')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','03','04')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','04','05')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','05','02')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','05','03')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','04','06')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','06','07')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','06','08')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','07','09')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','06','09')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','09','10')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','10','11')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','11','10')");
			stmt.executeUpdate("insert into V_PMS_wf_link (wf_id,src,tgt) values ('WF01','11','12')");
			
			conn.commit();
			stmt.close();
			SqlDB.s.freeConnection("oa", conn);
			Layer.message("测试数据库创建完成");
		} catch (Exception e) {
			MessageDialog.openError(brui.getCurrentShell(), "测试数据库创建错误", e.getMessage());
		}
	}

}
