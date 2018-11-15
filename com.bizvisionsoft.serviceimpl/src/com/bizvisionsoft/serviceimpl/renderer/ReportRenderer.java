package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.model.WorkReport;

public class ReportRenderer {
	
	private WorkReport report;


	public static Document render(WorkReport report) {
		return new ReportRenderer(report).render();
	}

	public ReportRenderer(WorkReport report) {
		this.report = report;
	}


	private Document render() {
		int rowHeight = 100;
		StringBuffer sb = new StringBuffer();
		sb.append("aaaa");
		
		return new Document("height",rowHeight).append("html", sb.toString()).append("_id", report.get_id());
	}


}
