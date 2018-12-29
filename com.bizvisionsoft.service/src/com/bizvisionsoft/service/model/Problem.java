package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("problem")
public class Problem {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "问题";

	@Exclude
	public static final String StatusCreated = "已创建";

	@Exclude
	public static final String StatusCanceled = "已取消";

	@Exclude
	public static final String StatusSolving = "解决中";

	@Exclude
	public static final String StatusClosed = "已关闭";

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String id;
	
	@Label(Label.ID_LABEL)
	private String getIdLabel() {
		return id +" ["+ status+"]";
	}
	
	@ReadValue
	@WriteValue
	private String status;

	@ReadValue
	@WriteValue
	@Label(Label.NAME_LABEL)
	private String name;
	
	@ReadValue
	@WriteValue
	private String custId;

	@ReadValue
	@WriteValue
	private String custInfo;

	@ReadValue
	@WriteValue
	private String partNum;
	
	@ReadValue
	@WriteValue
	private String partVer;
	
	@ReadValue
	@WriteValue
	private String partName;

	@ReadValue
	@WriteValue
	private String lotNum;

	@ReadValue
	@WriteValue
	private Date distDate;
	
	@ReadValue
	@WriteValue
	private String distQty;
	
	@ReadValue
	@WriteValue
	private Date issueDate;
	
	@ReadValue
	@WriteValue
	private String issueBy;
	
	@ReadValue
	@WriteValue
	private String initiatedFrom;
	
	@ReadValue
	@WriteValue
	private List<RemoteFile> idrrept;
	
	@ReadValue
	@WriteValue
	private List<RemoteFile> attarchments;
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 责任部门
	 */
	private ObjectId dept_id;

	@SetValue // 查询服务设置
	@ReadValue // 表格用
	private String deptName;

	@WriteValue("dept") // 编辑器用
	public void setOrganization(Organization org) {
		this.dept_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("dept") // 编辑器用
	public Organization getOrganization() {
		return Optional.ofNullable(dept_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id)).orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	private OperationInfo creationInfo;

	public Problem setCreationInfo(OperationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	/**
	 * 创建
	 * @return
	 */
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

	/**
	 * 启动8D
	 */
	@ReadValue
	@WriteValue
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

	/**
	 * 批准
	 */
	@ReadValue
	@WriteValue
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

	/**
	 * 关闭
	 */
	@ReadValue
	@WriteValue
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

	@Override
	@Label
	public String toString() {
		return name+(id == null ? "" : (" ["+id+"]"));
	}


	public Problem setStatus(String status) {
		this.status = status;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

}
