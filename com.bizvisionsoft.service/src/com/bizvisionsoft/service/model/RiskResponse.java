package com.bizvisionsoft.service.model;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
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

	@Behavior({ "�༭", "ɾ��" })
	@Exclude
	private boolean behavior = true;

	@ReadValue
	@WriteValue
	private String chargerId;


	@WriteValue("charger")
	private void setCharger(User charger) {
		this.chargerId = Optional.ofNullable(charger).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue({"charger","��Ŀ����Ӧ�Լƻ�/timeInf"})
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	/**
	 * �е���λ
	 */
	private ObjectId impUnit_id;

	@SetValue // ��ѯ��������
	@ReadValue // ������
	private String impUnitOrgFullName;

	public ObjectId getImpUnit_id() {
		return impUnit_id;
	}

	@WriteValue("impUnit") // �༭����
	public void setOrganization(Organization org) {
		this.impUnit_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue({"impUnit","��Ŀ����Ӧ�Լƻ�/costInf"}) // �༭����
	public Organization getOrganization() {
		return Optional.ofNullable(impUnit_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}

	@ReadValue({"action","��Ŀ����Ӧ�Լƻ�/result"})
	@WriteValue
	private String action;

	@ReadValue({"plan","��Ŀ����Ӧ�Լƻ�/title"})
	@WriteValue
	private String plan;

	@ReadValue
	@WriteValue
	private List<RemoteFile> attachment;

	public RiskResponse setType(String type) {
		this.type = type;
		return this;
	}

	public RiskResponse setRBSItem_id(ObjectId rbsItem_id) {
		this.rbsItem_id = rbsItem_id;
		return this;
	}

}