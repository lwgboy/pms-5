package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;

@PersistenceCollection("docu")
public class DocuDescriptor implements JsonExternalizable {

	public String domain;

	/** ฑ๊สถ Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String documentnumber;

	@ReadValue
	@WriteValue
	private String vid;

	@ReadValue
	@WriteValue
	private String desc;

	@ReadValue
	@WriteValue
	private ObjectId folder_id;

	@ReadValue
	@WriteValue
	private String phase;

	@ReadValue
	@WriteValue
	private String security;

	@ReadValue
	@WriteValue
	private String owner;

	@ReadValue
	@WriteValue
	private String plmtype;

	@ReadValue
	@WriteValue
	private String createBy;

	@ReadValue
	@WriteValue
	private Date createOn;

	@ReadValue
	@WriteValue
	private String path;

	@ReadValue
	@WriteValue
	private String status;

	@ReadValue("statusText")
	private String getStatusText() {
		if (DocuStatus.STATUS_APPROVING_ID.equals(status))
			return DocuStatus.STATUS_APPROVING_TEXT;
		else if (DocuStatus.STATUS_DEPOSED_ID.endsWith(status))
			return DocuStatus.STATUS_DEPOSED_TEXT;
		else if (DocuStatus.STATUS_RELEASED_ID.endsWith(status))
			return DocuStatus.STATUS_RELEASED_TEXT;

		return DocuStatus.STATUS_WORKING_TEXT;
	}

	@ReadValue("plmTypeText")
	private String getPlmTypeText() {
		if (DocuType.TYPE_PRODUCT.equals(plmtype)) {
			return DocuType.TYPE_PRODUCT_TEXT;
		} else if (DocuType.TYPE_PART.equals(plmtype)) {
			return DocuType.TYPE_PART_TEXT;
		} else if (DocuType.TYPE_MATERIAL.equals(plmtype)) {
			return DocuType.TYPE_MATERIAL_TEXT;
		} else if (DocuType.TYPE_SUPPLYMENT.equals(plmtype)) {
			return DocuType.TYPE_SUPPLYMENT_TEXT;
		} else if (DocuType.TYPE_PACKAGE.equals(plmtype)) {
			return DocuType.TYPE_PACKAGE_TEXT;
		} else if (DocuType.TYPE_JIGTOOL.equals(plmtype)) {
			return DocuType.TYPE_JIGTOOL_TEXT;
		} else if (DocuType.TYPE_CAD.equals(plmtype)) {
			return DocuType.TYPE_CAD_TEXT;
		} else if (DocuType.TYPE_FORM.equals(plmtype)) {
			return DocuType.TYPE_FORM_TEXT;
		}
		return DocuType.TYPE_DOCUMENT_TEXT;
	}

	@ReadValue("securityText")
	private String getSecurityText() {
		if ("a".equals(security)) {
			return "I";
		} else if ("b".equals(security)) {
			return "II";
		} else if ("c".equals(security)) {
			return "III";
		} else if ("d".equals(security)) {
			return "IV";
		} else if ("e".equals(security)) {
			return "V";
		}
		return "";
	}
}
