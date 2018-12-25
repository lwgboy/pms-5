package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("problem")
public class Problem {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String description;

	private OperationInfo creationInfo;

	@ReadValue("createOn")
	private Date readCreateOn() {
		return Optional.ofNullable(creationInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("createByInfo")
	private String readCreateBy() {
		return Optional.ofNullable(creationInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("createByConsignerInfo")
	private String readCreateByConsigner() {
		return Optional.ofNullable(creationInfo).map(c -> c.consignerName).orElse(null);
	}
	
	private OperationInfo initInfo;

	@ReadValue("initOn")
	private Date readInitOn() {
		return Optional.ofNullable(initInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("initByInfo")
	private String readInitBy() {
		return Optional.ofNullable(initInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("initByConsignerInfo")
	private String readInitByConsigner() {
		return Optional.ofNullable(initInfo).map(c -> c.consignerName).orElse(null);
	}
	
	private OperationInfo approveInfo;

	@ReadValue("approveOn")
	private Date readApproveOn() {
		return Optional.ofNullable(approveInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("approveByInfo")
	private String readApproveBy() {
		return Optional.ofNullable(approveInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("approveByConsignerInfo")
	private String readApproveByConsigner() {
		return Optional.ofNullable(approveInfo).map(c -> c.consignerName).orElse(null);
	}

	private OperationInfo closeInfo;

	@ReadValue({ "closeOn" })
	private Date readCloseOn() {
		return Optional.ofNullable(closeInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("closeByInfo")
	private String readCloseBy() {
		return Optional.ofNullable(closeInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("closeByConsignerInfo")
	private String readCloseByConsigner() {
		return Optional.ofNullable(closeInfo).map(c -> c.consignerName).orElse(null);
	}

	@ReadValue
	@WriteValue
	private Date receiveOn;
	
	@ReadValue
	@WriteValue
	private boolean custConfirm;
	
	@ReadValue
	@WriteValue
	private String custConfirmBy;

	@ReadValue
	@WriteValue
	private Date custConfirmOn;
	
	@ReadValue
	@WriteValue
	private Date custoConfirmRemark;
	
	@ReadValue
	@WriteValue
	private List<RemoteFile> primaryDocuments;
	
	@ReadValue
	@WriteValue
	private List<RemoteFile> attarchments;
	
	@ReadValue
	@WriteValue
	private String customerId;

	@ReadValue
	@WriteValue
	private String customerInfo;
	
	@ReadValue
	@WriteValue
	private String productId;

	@ReadValue
	@WriteValue
	private String productInfo;

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ���β���
	 */
	private ObjectId dept_id;

	@SetValue // ��ѯ��������
	@ReadValue // �����
	private String deptName;

	@WriteValue("dept") // �༭����
	public void setOrganization(Organization org) {
		this.dept_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("dept") // �༭����
	public Organization getOrganization() {
		return Optional.ofNullable(dept_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id)).orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue
	@WriteValue	
	private String remark;

	
}
