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
 * ����ģ��SAP�������ݿ�ĳ���
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
			
			//-------------���̷����----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_type (id varchar(20), type_name varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_type (id,type_name) values ('0001','�������')");
			stmt.executeUpdate("insert into V_PMS_wf_type (id,type_name) values ('0002','����֧��')");
			stmt.executeUpdate("insert into V_PMS_wf_type (id,type_name) values ('0003','���¹���')");
			
			//-------------���̶����----------------
			stmt.executeUpdate(
					"create table V_PMS_wf (id varchar(20), type_name varchar(20),wf_name varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF01','0001','��������')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF02','0001','�������')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF03','0002','������������')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF04','0003','�ù�����')");
			stmt.executeUpdate("insert into V_PMS_wf (id,type_name,wf_name) values ('WF05','0003','ְλ����')");
			
			//-------------����ʵ����----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_inst (id varchar(20), wf_id varchar(20), cur_node_id varchar(20), status varchar(20), inst_name varchar(20), create_date varchar(20), creater varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('0000001','WF01','05,01','1','XXX�����������','2018-09-01 22:11:12','XXX')");
			stmt.executeUpdate("insert into V_PMS_wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('0000002','WF01','10','1','XXX������������','2018-02-07 08:13:00','XXX')");
			stmt.executeUpdate("insert into V_PMS_wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('0000003','WF01','01,08','1','XXX���������','2018-11-11 15:12:00','XXX')");
			
			//----------���̻��־---------------
			stmt.executeUpdate(
					"create table V_PMS_wf_log (inst_id varchar(20), node_id varchar(20),node_name varchar(20),opr_dat varchar(20),operator varchar(20),lastname varchar(20),tgt varchar(20),comment varchar(20))");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000001','01','����','2016-09-01 22:11:12','XXX','XX','02','�������')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000001','02','���','2016-09-01 22:11:12','XXX','XX','03','���')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000001','03','У��','2016-09-01 22:11:12','XXX','XX','04','���')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000002','01','����','2016-09-01 22:11:12','XXX','XX','02','�������')");
			stmt.executeUpdate("insert into V_PMS_wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('0000003','01','����','2016-09-01 22:11:12','XXX','XX','02','�������')");
			
			
			//-------------���̽ڵ㶨���----------------
			stmt.executeUpdate(
					"create table V_PMS_wf_node (wf_id varchar(20), id varchar(20), text varchar(20),foreground varchar(6),background varchar(6))");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','01','����','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','02','���','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','03','У��','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','04','��׼','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','05','����','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','06','�������','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','07','����','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','08','����','ffffff','b0120a')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','09','����','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','10','ģ�߿���','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','11','ģ�߼��','ffffff','455a64')");
			stmt.executeUpdate("insert into V_PMS_wf_node (wf_id,id,text,foreground,background) values ('WF01','12','��ģ','ffffff','455a64')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//-------------�������߶����----------------
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
			Layer.message("�������ݿⴴ�����");
		} catch (Exception e) {
			MessageDialog.openError(brui.getCurrentShell(), "�������ݿⴴ������", e.getMessage());
		}
	}

}
