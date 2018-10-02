package com.awesometech.leoco.dev;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.bruiengine.service.UserSession;

public class ReportExample2 {

	@Execute
	public void execute() {
		//�������Ϊjson��ʽ�ַ���
		String rptParam = new Document("pipeline", "[{'$match':{'project_id':{'$oid':'5b988ee1a183c92e0c105ede'}}},"
				+ " {'$lookup' : {'from' : 'project', 'localField' : 'project_id','foreignField' : '_id', 'as' : 'project'}}, "
				+ "{ '$unwind' : {'path' : '$project'}}]").toJson();
		
		JsonObject postParam = new JsonObject()//
				.set("rptParam", rptParam)//ָ�����������
				.set("template", "simple_parameter2.rptdesign")//ģ���ļ���
				.set("outputType", "pdf")//����PDF�ļ�
				.set("fileName", "example2.pdf");//�����õ��ļ���
		
		UserSession.bruiToolkit().downloadServerFile("report", postParam);//����
	}

}
