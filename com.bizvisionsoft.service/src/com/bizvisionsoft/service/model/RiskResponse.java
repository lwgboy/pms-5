package com.bizvisionsoft.service.model;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

@PersistenceCollection("riskResponse")
public class RiskResponse {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String type;

	@ReadValue
	@WriteValue
	private ObjectId rbsItem_id;

	@Behavior({ "编辑", "删除" })
	@Exclude
	private boolean behavior = true;

	@ReadValue
	@WriteValue
	private String chargerId;
	
	@Exclude
	public String domain;

	@WriteValue("charger")
	private void setCharger(User charger) {
		this.chargerId = Optional.ofNullable(charger).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue({ "charger", "项目风险应对计划/timeInf", "项目风险应对计划（查看）/timeInf" })
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id, domain)).orElse(null);
	}

	/**
	 * 承担单位
	 */
	private ObjectId impUnit_id;

	@SetValue // 查询服务设置
	@ReadValue // 表格用
	private String impUnitOrgFullName;

	public ObjectId getImpUnit_id() {
		return impUnit_id;
	}

	@WriteValue("impUnit") // 编辑器用
	public void setOrganization(Organization org) {
		this.impUnit_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue({ "impUnit", "项目风险应对计划/costInf", "项目风险应对计划（查看）/costInf" }) // 编辑器用
	public Organization getOrganization() {
		return Optional.ofNullable(impUnit_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id, domain))
				.orElse(null);
	}

	@ReadValue({ "action", "项目风险应对计划/result", "项目风险应对计划（查看）/result" })
	@WriteValue
	private String action;

	@ReadValue({ "plan", "项目风险应对计划/title", "项目风险应对计划（查看）/title" })
	@WriteValue
	private String plan;

	@ReadValue
	@WriteValue
	private List<RemoteFile> attachment;

	public ObjectId get_id() {
		return _id;
	}
	
	public RiskResponse setType(String type) {
		this.type = type;
		return this;
	}
	
	public String getType() {
		return type;
	}

	public RiskResponse setRBSItem_id(ObjectId rbsItem_id) {
		this.rbsItem_id = rbsItem_id;
		return this;
	}

	@Override
	@Label
	public String toString() {
		return plan;
	}

}
