package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.SelectionValidation;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.mongodb.BasicDBObject;

@PersistenceCollection("problem")
public class Problem {

	@ReadValue(ReadValue.TYPE)
	@Exclude
	public static final String typeName = "问题";

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
		return id + " [" + status + "]";
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

	public String domain;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 责任部门
	 */
	private List<ObjectId> depts_id;

	@SetValue // 查询服务设置
	@ReadValue // 表格用
	private List<String> deptNames;

	@ReadValue // 表格用
	public String getDeptName() {
		if (Check.isAssigned(deptNames)) {
			return deptNames.get(0);
		}
		return null;
	}

	@SetValue // 查询服务设置
	public void setDeptName(String deptName) {
		if (this.deptNames == null)
			this.deptNames = new ArrayList<String>();
		if (deptName != null)
			this.deptNames.add(deptName);
	}

	@WriteValue("dept") // 编辑器用
	public void setOrganization(Organization org) {
		if (this.depts_id == null)
			this.depts_id = new ArrayList<ObjectId>();
		if (org != null)
			this.depts_id.add(org.get_id());
	}

	@ReadValue("dept") // 编辑器用
	public Organization getOrganization() {
		if (Check.isAssigned(depts_id)) {
			return ServicesLoader.get(OrganizationService.class).get(depts_id.get(0), domain);
		}
		return null;
	}

	@WriteValue("depts") // 编辑器用
	public void setOrganizations(List<Organization> org) {
		if (org != null && org.size() > 0) {
			this.depts_id = new ArrayList<ObjectId>();
			org.forEach(o -> this.depts_id.add(o.get_id()));
		}
	}

	@ReadValue("depts") // 编辑器用
	public List<Organization> getOrganizations() {
		return Optional.ofNullable(depts_id)
				.map(_id -> ServicesLoader.get(OrganizationService.class).createDataSet(
						new Query().filter(new BasicDBObject("_id", new BasicDBObject("$in", depts_id))).bson(),
						domain))
				.orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	@Label
	public String toString() {
		return name + (id == null ? "" : (" [" + id + "]"));
	}

	public Problem setStatus(String status) {
		this.status = status;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public String getName() {
		return name;
	}

	@ReadValue
	@WriteValue
	private SeverityInd severityInd;

	@ReadValue
	@WriteValue
	private Date latestTimeReq;

	@ReadValue
	@WriteValue
	private LostInd lostInd;

	@ReadValue
	@WriteValue
	private IncidenceInd incidenceInd;

	@ReadValue
	@WriteValue
	private FreqInd freqInd;

	@ReadValue
	@WriteValue
	private DetectionInd detectionInd;

	@ReadValue
	@WriteValue
	private List<ClassifyProblem> classifyProblems;

	@ReadValue("classifyProblem")
	private ClassifyProblem getClassifyProblem() {
		if (Check.isAssigned(classifyProblems)) {
			return classifyProblems.get(0);
		}
		return null;
	}

	@WriteValue("classifyProblem")
	private void setClassifyProblems(ClassifyProblem cp) {
		if (this.classifyProblems == null)
			this.classifyProblems = new ArrayList<ClassifyProblem>();

		if (cp != null)
			this.classifyProblems.add(cp);
	}

	@ReadValue
	@WriteValue
	private RiskUrgencyInd urgencyInd;

	@ReadValue
	@SetValue
	private CostItem cost;

	@ReadValue("cost.summary")
	private double getCostSummary() {
		return Optional.ofNullable(cost).map(c -> c.summary).orElse(0d);
	}

	@ReadValue("severityIndInfo")
	private String getSeverityIndInfo() {
		return Optional.ofNullable(severityInd).map(s -> MetaInfoWarpper.warpper(getIndIndex(s.index), s.toString()))
				.orElse("");
	}

	private String getIndIndex(int index) {
		if (index >= 6)
			return "<span class='layui-badge layui-bg-red'>" + index + "</span>";
		if (index >= 3)
			return "<span class='layui-badge layui-bg-orange'>" + index + "</span>";
		return "<span class='layui-badge layui-bg-blue'>" + index + "</span>";
	}

	@ReadValue("urgencyIndInfo")
	private String getUrgencyIndInfo() {
		return Optional.ofNullable(urgencyInd).map(s -> MetaInfoWarpper.warpper(getIndIndex(s.index), s.toString()))
				.orElse("");
	}

	@ReadValue("incidenceIndInfo")
	private String getIncidenceIndInfo() {
		return Optional.ofNullable(incidenceInd).map(s -> MetaInfoWarpper.warpper(getIndIndex(s.index), s.toString()))
				.orElse("");
	}

	@ReadValue("lostIndInfo")
	private String getLostIndIndInfo() {
		return Optional.ofNullable(lostInd).map(s -> MetaInfoWarpper.warpper(getIndIndex(s.index), s.toString()))
				.orElse("");
	}

	@ReadValue("freqIndInfo")
	private String getFreqIndInfo() {
		return Optional.ofNullable(freqInd).map(s -> MetaInfoWarpper.warpper(getIndIndex(s.index), s.toString()))
				.orElse("");
	}

	@ReadValue("detectionIndInfo")
	private String getDetectionIndInfo() {
		return Optional.ofNullable(detectionInd).map(s -> MetaInfoWarpper.warpper(getIndIndex(s.index), s.toString()))
				.orElse("");
	}

	@SelectionValidation("classifyProblem")
	private boolean selectable(@MethodParam(MethodParam.OBJECT) ClassifyProblem elem) {
		return elem.isLeaf;
	}

	public String getStatus() {
		return status;
	}

	@ReadValue
	@WriteValue
	private OperationInfo creationInfo;

	public Problem setCreationInfo(OperationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	@ReadValue("createOn")
	private Date readCreateOn() {
		return Optional.ofNullable(creationInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("createBy")
	private String readCreateBy() {
		return Optional.ofNullable(creationInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue
	@WriteValue
	private OperationInfo initInfo;

	@ReadValue("initOn")
	private Date readInitOn() {
		return Optional.ofNullable(initInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("initBy")
	private String readInitBy() {
		return Optional.ofNullable(initInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue
	@WriteValue
	private OperationInfo icaConfirmed;

	public OperationInfo getIcaConfirmed() {
		return icaConfirmed;
	}

	@ReadValue({ "icaConfirmedOn" })
	private Date icaConfirmedOn() {
		return Optional.ofNullable(icaConfirmed).map(c -> c.date).orElse(null);
	}

	@ReadValue("icaConfirmedBy")
	private String icaConfirmedBy() {
		return Optional.ofNullable(icaConfirmed).map(c -> c.userName).orElse(null);
	}

	@ReadValue
	@WriteValue
	private OperationInfo pcaApproved;

	public OperationInfo getPcaApproved() {
		return pcaApproved;
	}

	@ReadValue({ "pcaApprovedOn" })
	private Date pcaApprovedOn() {
		return Optional.ofNullable(pcaApproved).map(c -> c.date).orElse(null);
	}

	@ReadValue("pcaApprovedBy")
	private String pcaApprovedBy() {
		return Optional.ofNullable(pcaApproved).map(c -> c.userName).orElse(null);
	}

	@ReadValue
	@WriteValue
	private OperationInfo pcaValidated;

	public OperationInfo getPcaValidated() {
		return pcaValidated;
	}

	@ReadValue({ "pcaValidatedOn" })
	private Date pcaValidatedOn() {
		return Optional.ofNullable(pcaValidated).map(c -> c.date).orElse(null);
	}

	@ReadValue("pcaValidatedBy")
	private String pcaValidatedBy() {
		return Optional.ofNullable(pcaValidated).map(c -> c.userName).orElse(null);
	}

	@ReadValue
	@WriteValue
	private OperationInfo pcaConfirmed;

	public OperationInfo getPcaConfirmed() {
		return pcaConfirmed;
	}

	@ReadValue({ "pcaConfirmedOn" })
	private Date pcaConfirmedOn() {
		return Optional.ofNullable(pcaConfirmed).map(c -> c.date).orElse(null);
	}

	@ReadValue("pcaConfirmedBy")
	private String pcaConfirmedBy() {
		return Optional.ofNullable(pcaConfirmed).map(c -> c.userName).orElse(null);
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

	@ReadValue("closeBy")
	private String readCloseBy() {
		return Optional.ofNullable(closeInfo).map(c -> c.userName).orElse(null);
	}

	/**
	 * 取消
	 */
	@ReadValue
	@WriteValue
	private OperationInfo cancelInfo;

	@ReadValue({ "cancelOn" })
	private Date readCancelOn() {
		return Optional.ofNullable(cancelInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("cancelBy")
	private String readCancelBy() {
		return Optional.ofNullable(cancelInfo).map(c -> c.userName).orElse(null);
	}

	public OperationInfo getCreationInfo() {
		return creationInfo;
	}

	public boolean isSolving() {
		return Problem.StatusSolving.equals(status);
	}
	
	@ReadValue
	@WriteValue
	private boolean needReport;
	

	@ReadValue
	@WriteValue
	private List<RemoteFile> meetingattachs;
	
	@ReadValue
	@WriteValue
	private List<RemoteFile> reportattachs;
	
	/**
	 * 客诉类型
	 */
	@ReadValue
	@WriteValue
	private String complaintType;
	
	/**
	 * 出货单号
	 */
	@ReadValue
	@WriteValue
	private String shippingNumber;
	
	/**
	 * 出货方式
	 */
	@ReadValue
	@WriteValue
	private String shippingMode;
	
	/**
	 * 退货数量
	 */
	@ReadValue
	@WriteValue
	private String quantityReturned;
	
	/**
	 * 不良率
	 */
	@ReadValue
	@WriteValue
	private String defectiveRate;

	/**
	 * 补退货类型
	 */
	@ReadValue
	@WriteValue
	private String typeOfrefund;

	/**
	 * 补货PO
	 */
	@ReadValue
	@WriteValue
	private String replenishmentPO;
	
	/**
	 * 流程ID
	 */
	@ReadValue
	@WriteValue
	private String flowID;
	
	/**
	 * 预留boolean字段1：
	 * 信维：Shipment Inspection/Agents Release(出货前/第三方检查）
	 */
	@ReadValue
	@WriteValue
	private boolean boolean1;
	
	/**
	 * 预留boolean字段2：
	 * 信维：Reliability Test（可靠性测试）
	 */
	@ReadValue
	@WriteValue
	private boolean boolean2;
	
	/**
	 * 预留date字段1：
	 * 信维：实际回复时间
	 */
	@ReadValue
	@WriteValue
	private Date date1;
	
	/**
	 * 客户ID（从organization表中获取的）
	 */
	private ObjectId customer_id;
	
	@WriteValue("customerId") // 编辑器用
	public void setCustomer(Organization org) {
		this.customer_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("customerId") // 编辑器用
	public Organization getCustomer() {
		return Optional.ofNullable(customer_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id, domain)).orElse(null);
	}
}
