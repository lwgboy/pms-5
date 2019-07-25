package com.bizvisionsoft.service.exporter;

/**
 * ����Ҫ��ExportableFormField���浽���ݿ⣬�ʽ������ƶ����ӿ���
 * 
 * @author gdiya
 *
 */
public interface IExportableFormField {

	public final static String TYPE_TEXT = "�����ı���";

	public final static String TYPE_SPINNER = "���������";

	public static final String TYPE_COMBO = "����ѡ���";

	public static final String TYPE_RADIO = "��ѡ��";

	public static final String TYPE_CHECK = "��ѡ��";

	public static final String TYPE_MULTI_CHECK = "���ѡ��";

	public static final String TYPE_DATETIME = "����ʱ��ѡ��";

	public static final String TYPE_DATETIME_RANGE = "����ʱ�䷶Χѡ��";

	public static final String TYPE_SELECTION = "����ѡ���";

	public static final String TYPE_MULTI_SELECTION = "�������ѡ���";

	public static final String TYPE_TABLE = "���";

	public static final String TYPE_FILE = "�ļ�ѡ���";

	public static final String TYPE_MULTI_FILE = "����ļ�ѡ���";

	public static final String TYPE_IMAGE_FILE = "ͼƬѡ���";

	public static final String TYPE_MULTI_IMAGE_FILE = "���ͼƬѡ���";

	public static final String TYPE_TEXT_RANGE = "��ֵ��Χ����";

	public static final String TYPE_TEXT_MULTILINE = "�����ı���";

	public static final String TYPE_TEXT_HTML = "HTML�ı���";

	public final static String TYPE_INLINE = "��";

	public final static String TYPE_PAGE = "��ǩҳ";

	public final static String TYPE_PAGE_HTML = "��ǩҳ��HTML�༭��";

	public final static String TYPE_PAGE_NOTE = "��ǩҳ���ı��༭��";

	public static final String TYPE_LABEL = "�ı���ǩ";

	public static final String TYPE_LABEL_MULTILINE = "�����ı���ǩ";

	public static final String TYPE_BANNER = "���";

	public static final String TYPE_SORT_TEXT = "�����ֶ�";

	public static final String RADIO_STYLE_CLASSIC = "��ͳ";

	public static final String RADIO_STYLE_SEGMENT = "����ֶΣ�Ĭ�ϣ�";

	public static final String RADIO_STYLE_VERTICAL = "����";

	public static final String DATE_TYPE_YEAR = "year";

	public static final String DATE_TYPE_MONTH = "month";

	public static final String DATE_TYPE_DATE = "date";

	public static final String DATE_TYPE_TIME = "time";

	public static final String DATE_TYPE_DATETIME = "datetime";

	public static final String DATE_TYPE_YEAR_MONTH = "yearMonth";
}
