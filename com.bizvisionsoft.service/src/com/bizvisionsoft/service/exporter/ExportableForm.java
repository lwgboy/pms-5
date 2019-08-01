package com.bizvisionsoft.service.exporter;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.tools.Check;

public class ExportableForm implements JsonExternalizable {

	public ObjectId _id;

	public List<ExportableFormField> fields;

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

	public ExportableFormField findField(String fieldName) {
		return findField(fields, fieldName);
	}

	public ExportableFormField findField(List<ExportableFormField> fieldList, String fieldName) {
		if (Check.isNotAssigned(fieldList))
			return null;
		for (int i = 0; i < fieldList.size(); i++) {
			ExportableFormField f = fieldList.get(i);
			if (f.name.equals(fieldName) && !ExportableFormField.TYPE_PAGE.equals(f.type)) {
				return f;
			}
			f = findField(f.formFields, fieldName);
			if (f != null) {
				return f;
			}
		}
		return null;
	}

}
