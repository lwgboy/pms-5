package com.bizvisionsoft.service.tools;

import org.bson.Document;

public class DocuToolkit {
	/**
	 * 已发布
	 */
	public static final String STATUS_RELEASED_ID = "released";
	public static final String STATUS_RELEASED_TEXT = "已发布";

	/**
	 * 工作中
	 */
	public static final String STATUS_WORKING_ID = "working";
	public static final String STATUS_WORKING_TEXT = "工作中";

	/**
	 * 审核中
	 */
	public static final String STATUS_APPROVING_ID = "approving";
	public static final String STATUS_APPROVING_TEXT = "审核中";

	/**
	 * 已废弃
	 */
	public static final String STATUS_DEPOSED_ID = "deposed";
	public static final String STATUS_DEPOSED_TEXT = "已废弃";

	public static final String TYPE_DOCUMENT = "pmdocument";
	public static final String TYPE_DOCUMENT_TEXT = "文档";

	public static final String TYPE_CAD = "pmcaddocument";
	public static final String TYPE_CAD_TEXT = "图纸";

	public static final String TYPE_PART = "pmpart";
	public static final String TYPE_PART_TEXT = "零部件";

	public static final String TYPE_PRODUCT = "pmproduct";
	public static final String TYPE_PRODUCT_TEXT = "成品";

	public static final String TYPE_MATERIAL = "pmmaterial";
	public static final String TYPE_MATERIAL_TEXT = "原材料";

	public static final String TYPE_SUPPLYMENT = "pmsupplyment";
	public static final String TYPE_SUPPLYMENT_TEXT = "客供件";

	public static final String TYPE_JIGTOOL = "pmjigtools";
	public static final String TYPE_JIGTOOL_TEXT = "工装夹具";

	public static final String TYPE_PACKAGE = "pmpackage";
	public static final String TYPE_PACKAGE_TEXT = "包装材料";

	public static final String TYPE_FORM = "pmform";
	public static final String TYPE_FORM_TEXT = "表单";

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
