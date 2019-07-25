package com.bizvisionsoft.service.exporter;

/**
 * 因需要把ExportableFormField保存到数据库，故将常量移动到接口中
 * 
 * @author gdiya
 *
 */
public interface IExportableFormField {

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
}
