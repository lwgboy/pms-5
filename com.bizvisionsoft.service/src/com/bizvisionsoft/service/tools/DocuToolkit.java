package com.bizvisionsoft.service.tools;

import org.bson.Document;

public class DocuToolkit {
	/**
	 * �ѷ���
	 */
	public static final String STATUS_RELEASED_ID = "released";
	public static final String STATUS_RELEASED_TEXT = "�ѷ���";

	/**
	 * ������
	 */
	public static final String STATUS_WORKING_ID = "working";
	public static final String STATUS_WORKING_TEXT = "������";

	/**
	 * �����
	 */
	public static final String STATUS_APPROVING_ID = "approving";
	public static final String STATUS_APPROVING_TEXT = "�����";

	/**
	 * �ѷ���
	 */
	public static final String STATUS_DEPOSED_ID = "deposed";
	public static final String STATUS_DEPOSED_TEXT = "�ѷ���";

	public static final String TYPE_DOCUMENT = "pmdocument";
	public static final String TYPE_DOCUMENT_TEXT = "�ĵ�";

	public static final String TYPE_CAD = "pmcaddocument";
	public static final String TYPE_CAD_TEXT = "ͼֽ";

	public static final String TYPE_PART = "pmpart";
	public static final String TYPE_PART_TEXT = "�㲿��";

	public static final String TYPE_PRODUCT = "pmproduct";
	public static final String TYPE_PRODUCT_TEXT = "��Ʒ";

	public static final String TYPE_MATERIAL = "pmmaterial";
	public static final String TYPE_MATERIAL_TEXT = "ԭ����";

	public static final String TYPE_SUPPLYMENT = "pmsupplyment";
	public static final String TYPE_SUPPLYMENT_TEXT = "�͹���";

	public static final String TYPE_JIGTOOL = "pmjigtools";
	public static final String TYPE_JIGTOOL_TEXT = "��װ�о�";

	public static final String TYPE_PACKAGE = "pmpackage";
	public static final String TYPE_PACKAGE_TEXT = "��װ����";

	public static final String TYPE_FORM = "pmform";
	public static final String TYPE_FORM_TEXT = "��";

	public static final String[] DEFAULT_MAJOR_VID_SEQ = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
			"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public static void initVersionNumber(Document doc) {
		Object mvid = doc.get("major_vid");
		if (!(mvid instanceof String)) {
			String[] seq = DEFAULT_MAJOR_VID_SEQ;
			doc.put("major_vid", seq[0]);
		}
		Object svid = doc.get("svid");
		if (svid instanceof Integer) {
			doc.put("svid", new Integer(((Integer) svid).intValue() + 1));
		} else {
			doc.put("svid", new Integer(1));
		}
	}
}
