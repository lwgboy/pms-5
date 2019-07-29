package com.bizvisionsoft.service.exporter;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;

public class ExportableForm implements JsonExternalizable{

	public ObjectId _id;
	
	public List<ExportableFormField> fields;

	public String fileName;

	public Boolean stdReportBreakByTabPage;

	public Boolean stdReportEditable;

	public Boolean stdReportExportTabPageTitle;

	public Integer stdReportFootMargin;

	public Integer stdReportHeaderMargin;

	public String stdReportPageMargin;

	public String stdReportPageSize;

	public String stdReportPaperOrientation;

	public String stdReportPaperType;

	public String stdReportTableBottomBorderColor;

	public Integer stdReportTableBottomBorderSize;

	public String stdReportTableBottomBorderType;

	public String stdReportTableInsideHBorderColor;

	public Integer stdReportTableInsideHBorderSize;

	public String stdReportTableInsideHBorderType;

	public String stdReportTableInsideVBorderColor;

	public Integer stdReportTableInsideVBorderSize;

	public String stdReportTableInsideVBorderType;

	public String stdReportTableInternalMargin;

	public String stdReportTableLeftBorderColor;

	public Integer stdReportTableLeftBorderSize;

	public String stdReportTableLeftBorderType;

	public String stdReportTableRightBorderColor;

	public Integer stdReportTableRightBorderSize;

	public String stdReportTableRightBorderType;

	public String stdReportTableTopBorderColor;

	public Integer stdReportTableTopBorderSize;

	public String stdReportTableTopBorderType;
	
	public String templateFilePath;
	
	public String stdPageTemplate;

	public Map<String,String> properties;


}
