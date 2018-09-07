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
			Connection conn = SqlDB.s.getConnection("oa");
			Statement stmt = conn.createStatement();
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			try {
				stmt.executeUpdate("drop table wf_type");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table wf");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table wf_inst");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table wf_log");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table wf_node");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table wf_link");
			} catch (Exception e) {
			}
			
			//-------------���̷����----------------
			stmt.executeUpdate(
					"create table wf_type (id varchar(20), type_name varchar(20))");
			stmt.executeUpdate("insert into wf_type (id,type_name) values ('0001','�������')");
			stmt.executeUpdate("insert into wf_type (id,type_name) values ('0002','����֧��')");
			stmt.executeUpdate("insert into wf_type (id,type_name) values ('0003','���¹���')");
			
			//-------------���̶����----------------
			stmt.executeUpdate(
					"create table wf (id varchar(20), type_name varchar(20),wf_name varchar(20))");
			stmt.executeUpdate("insert into wf (id,type_name,wf_name) values ('WF01','�������','��������')");
			stmt.executeUpdate("insert into wf (id,type_name,wf_name) values ('WF02','�������','�������')");
			stmt.executeUpdate("insert into wf (id,type_name,wf_name) values ('WF03','����֧��','������������')");
			stmt.executeUpdate("insert into wf (id,type_name,wf_name) values ('WF04','���¹���','�ù�����')");
			stmt.executeUpdate("insert into wf (id,type_name,wf_name) values ('WF05','���¹���','ְλ����')");
			
			//-------------����ʵ����----------------
			stmt.executeUpdate(
					"create table wf_inst (id varchar(20), wf_id varchar(20), cur_node_id varchar(20), status varchar(20), inst_name varchar(20), create_date varchar(20), creater varchar(20))");
			stmt.executeUpdate("insert into wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('IS01','WF01','05,01','1','XXX�����������','2016-09-01 22:11:12','XXX')");
			stmt.executeUpdate("insert into wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('IS02','WF01','10','1','XXX������������','2017-02-07 08:13:00','XXX')");
			stmt.executeUpdate("insert into wf_inst (id,wf_id,cur_node_id,status,inst_name,create_date,creater) values ('IS03','WF01','01,08','1','XXX���������','2018-11-11 15:12:00','XXX')");
			
			//----------���̻��־---------------
			stmt.executeUpdate(
					"create table wf_log (inst_id varchar(20), node_id varchar(20),node_name varchar(20),opr_dat varchar(20),operator varchar(20),lastname varchar(20),tgt varchar(20),comment varchar(20))");
			stmt.executeUpdate("insert into wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('IS01','01','����','2016-09-01 22:11:12','XXX','XX','02','�������')");
			stmt.executeUpdate("insert into wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('IS01','02','���','2016-09-01 22:11:12','XXX','XX','03','���')");
			stmt.executeUpdate("insert into wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('IS01','03','У��','2016-09-01 22:11:12','XXX','XX','04','���')");
			stmt.executeUpdate("insert into wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('IS02','01','����','2016-09-01 22:11:12','XXX','XX','02','�������')");
			stmt.executeUpdate("insert into wf_log (inst_id,node_id,node_name,opr_dat,operator,lastname,tgt,comment) values ('IS03','01','����','2016-09-01 22:11:12','XXX','XX','02','�������')");
			
			
			//-------------���̽ڵ㶨���----------------
			stmt.executeUpdate(
					"create table wf_node (wf_id varchar(20), id varchar(20), text varchar(20),foreground varchar(6),background varchar(6))");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','01','����','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','02','���','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','03','У��','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','04','��׼','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','05','����','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','06','�������','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','07','����','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','08','����','ffffff','b0120a')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','09','����','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','10','ģ�߿���','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','11','ģ�߼��','ffffff','455a64')");
			stmt.executeUpdate("insert into wf_node (wf_id,id,text,foreground,background) values ('WF01','12','��ģ','ffffff','455a64')");
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//-------------�������߶����----------------
			stmt.executeUpdate(
					"create table wf_link (wf_id varchar(20),src varchar(20), tgt varchar(20))");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','01','02')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','01','03')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','02','04')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','03','04')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','04','05')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','05','02')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','05','03')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','04','06')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','06','07')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','06','08')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','07','09')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','06','09')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','09','10')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','10','11')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','11','10')");
			stmt.executeUpdate("insert into wf_link (wf_id,src,tgt) values ('WF01','11','12')");
			
			conn.commit();
			stmt.close();
			SqlDB.s.freeConnection("oa", conn);
			Layer.message("�������ݿⴴ�����");
		} catch (Exception e) {
			MessageDialog.openError(brui.getCurrentShell(), "�������ݿⴴ������", e.getMessage());
		}
	}

}
