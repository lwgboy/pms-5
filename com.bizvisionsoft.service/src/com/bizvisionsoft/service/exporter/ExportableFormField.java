package com.bizvisionsoft.service.exporter;

import java.util.List;

public class ExportableFormField {
	
	public final static String TYPE_TEXT = "单行文本框";

	public final static String TYPE_SPINNER = "数字输入框";

	public static final String TYPE_COMBO = "下拉选择框";

	public static final String TYPE_RADIO = "单选框";

	public static final String TYPE_CHECK = "复选框";

	public static final String TYPE_MULTI_CHECK = "多项勾选框";

	public static final String TYPE_DATETIME = "日期时间选择";

	public static final String TYPE_DATETIME_RANGE = "日期时间范围选择";

	public static final String TYPE_SELECTION = "对象选择框";

	public static final String TYPE_MULTI_SELECTION = "多个对象选择框";

	public static final String TYPE_TABLE = "表格";

	public static final String TYPE_FILE = "文件选择框";

	public static final String TYPE_MULTI_FILE = "多个文件选择框";

	public static final String TYPE_IMAGE_FILE = "图片选择框";

	public static final String TYPE_MULTI_IMAGE_FILE = "多个图片选择框";
	
	public static final String TYPE_TEXT_RANGE = "数值范围输入";

	public static final String TYPE_TEXT_MULTILINE = "多行文本框";

	public static final String TYPE_TEXT_HTML = "HTML文本框";

	public final static String TYPE_INLINE = "行";

	public final static String TYPE_PAGE = "标签页";

	public final static String TYPE_PAGE_HTML = "标签页（HTML编辑）";

	public final static String TYPE_PAGE_NOTE = "标签页（文本编辑）";
	
	public static final String TYPE_LABEL = "文本标签";

	public static final String TYPE_LABEL_MULTILINE = "多行文本标签";

	public static final String TYPE_BANNER = "横幅";

	public static final String TYPE_SORT_TEXT = "排序字段";
	
	public static final String RADIO_STYLE_CLASSIC = "传统";

	public static final String RADIO_STYLE_SEGMENT = "横向分段（默认）";

	public static final String RADIO_STYLE_VERTICAL = "纵向";
	
	public static final String DATE_TYPE_YEAR = "year";

	public static final String DATE_TYPE_MONTH = "month";

	public static final String DATE_TYPE_DATE = "date";

	public static final String DATE_TYPE_TIME = "time";

	public static final String DATE_TYPE_DATETIME = "datetime";

	public static final String DATE_TYPE_YEAR_MONTH = "yearMonth";
	
	public List<ExportableFormField> formFields;
	
	public String radioStyle;
	
	public String stdReportFieldBackgound;

	public Boolean stdReportFieldBold;

	public String stdReportFieldBottomBorderColor;

	public Integer stdReportFieldBottomBorderSize;

	public String stdReportFieldBottomBorderType;

	public String stdReportFieldCellMargin;

	public String stdReportFieldFontFamily;

	public Integer stdReportFieldFontSize;

	public String stdReportFieldForegound;

	public String stdReportFieldHorizontalAlign;

	public String stdReportFieldInsideHBorderColor;

	public Integer stdReportFieldInsideHBorderSize;

	public String stdReportFieldInsideHBorderType;

	public String stdReportFieldInsideVBorderColor;

	public Integer stdReportFieldInsideVBorderSize;

	public String stdReportFieldInsideVBorderType;

	public String stdReportFieldLeftBorderColor;

	public Integer stdReportFieldLeftBorderSize;

	public String stdReportFieldLeftBorderType;
	
	public String stdReportFieldRightBorderColor;

	public Integer stdReportFieldRightBorderSize;

	public String stdReportFieldRightBorderType;
	
	public String stdReportFieldTL2BRBorderColor;

	public Integer stdReportFieldTL2BRBorderSize;

	public String stdReportFieldTL2BRBorderType;

	public String stdReportFieldTopBorderColor;

	public Integer stdReportFieldTopBorderSize;

	public String stdReportFieldTopBorderType;

	public String stdReportFieldTR2BLBorderColor;

	public Integer stdReportFieldTR2BLBorderSize;

	public String stdReportFieldTR2BLBorderType;

	public String stdReportFieldVerticalAlign;

	public Integer stdReportFieldWidth;

	public String stdReportLabelBackgound;

	public Boolean stdReportLabelBold;

	public String stdReportLabelBottomBorderColor;
	
	public Integer stdReportLabelBottomBorderSize;
	
	public String stdReportLabelBottomBorderType;

	public String stdReportLabelCellMargin;
	
	public String stdReportLabelFontFamily;
	
	public Integer stdReportLabelFontSize;
	
	public String stdReportLabelForegound;
	
	public Boolean stdReportLabelHide;
	
	public String stdReportLabelHorizontalAlign;
	
	public String stdReportLabelInsideHBorderColor;
	
	public Integer stdReportLabelInsideHBorderSize;
	
	public String stdReportLabelInsideHBorderType;
	
	public String stdReportLabelInsideVBorderColor;
	
	public Integer stdReportLabelInsideVBorderSize;
	
	public String stdReportLabelInsideVBorderType;
	
	public String stdReportLabelLeftBorderColor;
	
	public Integer stdReportLabelLeftBorderSize;
	
	public String stdReportLabelLeftBorderType;
	
	public String stdReportLabelRightBorderColor;

	public Integer stdReportLabelRightBorderSize;

	public String stdReportLabelRightBorderType;
	
	public String stdReportLabelText;
	
	public String stdReportLabelTL2BRBorderColor;
	
	public Integer stdReportLabelTL2BRBorderSize;

	public String stdReportLabelTL2BRBorderType;

	public String stdReportLabelTopBorderColor;

	public Integer stdReportLabelTopBorderSize;

	public String stdReportLabelTopBorderType;

	public String stdReportLabelTR2BLBorderColor;

	public Integer stdReportLabelTR2BLBorderSize;

	public String stdReportLabelTR2BLBorderType;

	public String stdReportLabelVerticalAlign;

	public Integer stdReportLabelWidth;

	public String text;

	public String type;

	public String name;

}
