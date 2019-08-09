package com.bizvisionsoft.service.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.mongocodex.codec.JsonExternalizable;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.DocuToolkit;

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

	@ReadValue("path")
	private String getPath() {
		String text = "";
		if (Check.isAssigned(paths)) {
			Collections.sort(paths);
			for (int i = 0; i < paths.size(); i++) {
				if (i != 0) {
					text += "\\";
				}
				text += paths.get(i).getName();
			}
		}
		return text;
	}

	@ReadValue
	@WriteValue
	@SetValue
	private List<VaultFolderDescriptor> paths;

	@ReadValue
	@WriteValue
	private String status;

	@ReadValue
	@WriteValue
	private String projectdesc;

	@ReadValue
	@WriteValue
	private List<String> projectworkorder;

	@ReadValue
	@WriteValue
	private String projectnumber;

	@ReadValue("statusText")
	private String getStatusText() {
		if (DocuToolkit.STATUS_APPROVING_ID.equals(status))
			return DocuToolkit.STATUS_APPROVING_TEXT;
		else if (DocuToolkit.STATUS_DEPOSED_ID.equals(status))
			return DocuToolkit.STATUS_DEPOSED_TEXT;
		else if (DocuToolkit.STATUS_RELEASED_ID.equals(status))
			return DocuToolkit.STATUS_RELEASED_TEXT;

		return DocuToolkit.STATUS_WORKING_TEXT;
	}

	@ReadValue("plmTypeText")
	private String getPlmTypeText() {
		if (DocuToolkit.TYPE_PRODUCT.equals(plmtype)) {
			return DocuToolkit.TYPE_PRODUCT_TEXT;
		} else if (DocuToolkit.TYPE_PART.equals(plmtype)) {
			return DocuToolkit.TYPE_PART_TEXT;
		} else if (DocuToolkit.TYPE_MATERIAL.equals(plmtype)) {
			return DocuToolkit.TYPE_MATERIAL_TEXT;
		} else if (DocuToolkit.TYPE_SUPPLYMENT.equals(plmtype)) {
			return DocuToolkit.TYPE_SUPPLYMENT_TEXT;
		} else if (DocuToolkit.TYPE_PACKAGE.equals(plmtype)) {
			return DocuToolkit.TYPE_PACKAGE_TEXT;
		} else if (DocuToolkit.TYPE_JIGTOOL.equals(plmtype)) {
			return DocuToolkit.TYPE_JIGTOOL_TEXT;
		} else if (DocuToolkit.TYPE_CAD.equals(plmtype)) {
			return DocuToolkit.TYPE_CAD_TEXT;
		} else if (DocuToolkit.TYPE_FORM.equals(plmtype)) {
			return DocuToolkit.TYPE_FORM_TEXT;
		}
		return DocuToolkit.TYPE_DOCUMENT_TEXT;
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
