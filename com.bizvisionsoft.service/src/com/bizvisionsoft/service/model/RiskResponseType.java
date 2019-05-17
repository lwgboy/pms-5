package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

public class RiskResponseType {

	private ObjectId _id;

	private static final String TYPE_PREVENT = "Ԥ����ʩ";

	private static final String TYPE_RESPONSE = "Ӧ�Դ�ʩ";

	private static final String TYPE_EMERGENCY = "Ӧ��Ԥ��";

	public ObjectId get_id() {
		return _id;
	}

	private String type;

	@Behavior("��Ŀ����Ӧ�Լƻ�/���")
	private boolean action = true;
	
	public String domain;

	@ReadValue("title")
	private String getTitle() {
		if (TYPE_PREVENT.equals(type)) {
			return "<span class='layui-badge layui-bg-green'>" + type + "</span>";
		} else if (TYPE_RESPONSE.equals(type)) {
			return "<span class='layui-badge layui-bg-blue'>" + type + "</span>";
		} else if (TYPE_EMERGENCY.equals(type)) {
			return "<span class='layui-badge'>" + type + "</span>";
		}
		return "";
	}

	public RiskResponseType set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public RiskResponseType setType(String type) {
		this.type = type;
		return this;
	}

	public static RiskResponseType pervent(ObjectId _id,String domain) {
		return new RiskResponseType().setType(TYPE_PREVENT).set_id(_id);
	}

	public static RiskResponseType response(ObjectId _id,String domain) {
		return new RiskResponseType().setType(TYPE_RESPONSE).set_id(_id);
	}

	public static RiskResponseType emergency(ObjectId _id,String domain) {
		return new RiskResponseType().setType(TYPE_EMERGENCY).set_id(_id);
	}

	public String getType() {
		return type;
	}

	@Structure({ "��Ŀ����Ӧ�Լƻ�/list", "��Ŀ����Ӧ�Լƻ����鿴��/list" })
	private List<RiskResponse> listRiskResponse() {
		return ServicesLoader.get(RiskService.class)
				.listRiskResponse(new Query().filter(new BasicDBObject("rbsItem_id", _id).append("type", type)).bson(), domain);
	}

	@Structure({ "��Ŀ����Ӧ�Լƻ�/count", "��Ŀ����Ӧ�Լƻ����鿴��/count" })
	private long countRiskResponse() {
		return ServicesLoader.get(RiskService.class)
				.countRiskResponse(new BasicDBObject("rbsItem_id", _id).append("type", type), domain);
	}

	@ReadValue({ "��Ŀ����Ӧ�Լƻ�/costInf", "��Ŀ����Ӧ�Լƻ����鿴��/costInf" })
	public String getOrganization() {
		return "���ε�λ";
	}

	@ReadValue({ "��Ŀ����Ӧ�Լƻ�/timeInf", "��Ŀ����Ӧ�Լƻ����鿴��/costInf" })
	public String getCharger() {
		return "������";
	}

	@ReadValue({ "��Ŀ����Ӧ�Լƻ�/result", "��Ŀ����Ӧ�Լƻ����鿴��/result" })
	public String getHeaderResult() {
		return "���üƻ�";
	}
}
