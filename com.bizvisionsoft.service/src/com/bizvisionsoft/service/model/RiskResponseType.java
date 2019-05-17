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

	private static final String TYPE_PREVENT = "预防措施";

	private static final String TYPE_RESPONSE = "应对措施";

	private static final String TYPE_EMERGENCY = "应急预案";

	public ObjectId get_id() {
		return _id;
	}

	private String type;

	@Behavior("项目风险应对计划/添加")
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

	@Structure({ "项目风险应对计划/list", "项目风险应对计划（查看）/list" })
	private List<RiskResponse> listRiskResponse() {
		return ServicesLoader.get(RiskService.class)
				.listRiskResponse(new Query().filter(new BasicDBObject("rbsItem_id", _id).append("type", type)).bson(), domain);
	}

	@Structure({ "项目风险应对计划/count", "项目风险应对计划（查看）/count" })
	private long countRiskResponse() {
		return ServicesLoader.get(RiskService.class)
				.countRiskResponse(new BasicDBObject("rbsItem_id", _id).append("type", type), domain);
	}

	@ReadValue({ "项目风险应对计划/costInf", "项目风险应对计划（查看）/costInf" })
	public String getOrganization() {
		return "责任单位";
	}

	@ReadValue({ "项目风险应对计划/timeInf", "项目风险应对计划（查看）/costInf" })
	public String getCharger() {
		return "责任人";
	}

	@ReadValue({ "项目风险应对计划/result", "项目风险应对计划（查看）/result" })
	public String getHeaderResult() {
		return "处置计划";
	}
}
