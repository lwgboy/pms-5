package com.awesometech.leoco.dev;

import org.bson.Document;
import org.eclipse.rap.json.JsonObject;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.bruiengine.service.UserSession;

public class ReportExample2 {

	@Execute
	public void execute() {
		//报表参数为json格式字符串
		String rptParam = new Document("pipeline", "[{'$match':{'project_id':{'$oid':'5b988ee1a183c92e0c105ede'}}},"
				+ " {'$lookup' : {'from' : 'project', 'localField' : 'project_id','foreignField' : '_id', 'as' : 'project'}}, "
				+ "{ '$unwind' : {'path' : '$project'}}]").toJson();
		
		JsonObject postParam = new JsonObject()//
				.set("rptParam", rptParam)//指定到报表参数
				.set("template", "simple_parameter2.rptdesign")//模板文件名
				.set("outputType", "pdf")//生成PDF文件
				.set("fileName", "example2.pdf");//下载用的文件名
		
		UserSession.bruiToolkit().downloadServerFile("report", postParam);//下载
	}

}
