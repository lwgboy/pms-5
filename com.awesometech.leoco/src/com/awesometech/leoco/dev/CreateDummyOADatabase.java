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
			// ����SO-->Product��
			try {
				stmt.executeUpdate("drop table wf_node");
			} catch (Exception e) {
			}
			try {
				stmt.executeUpdate("drop table wf_link");
			} catch (Exception e) {
			}
			

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
			Layer.message("�������ݿⴴ�����");
		} catch (Exception e) {
			MessageDialog.openError(brui.getCurrentShell(), "�������ݿⴴ������", e.getMessage());
		}
	}

}
