package com.bizvisionsoft.service.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("resourceType")
public class ResourceType {

	public static final String TYPE_HR = "人力资源";
	public static final String TYPE_ER = "设备设施";

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/** 编号 Y **/
	@ReadValue
	@WriteValue
	private String id;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "资源类型";

	/** 资源类别名称 25 Y **/
	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String type;

	@ReadValue
	@WriteValue
	private String description;

	/** 计价方式 10 Y **/
	@WriteValue
	@ReadValue("资源类型编辑器/pricingModel")
	private String pricingModel;

	/** 标准费率(元) Y **/
	@ReadValue
	private double basicRate;

	/** 加班费率(元) Y **/
	@ReadValue
	private double overtimeRate;

	@WriteValue("资源类型编辑器/basicRate")
	private void set_basicRate(String _basicRate) {
		try {
			this.basicRate = Double.parseDouble(_basicRate);
		} catch (Exception e) {
			throw new RuntimeException("标准费率字段只能输入数值");
		}
	}

	@WriteValue("资源类型编辑器/overtimeRate")
	private void set_overtimeRate(String _overtimeRate) {
		try {
			this.overtimeRate = Double.parseDouble(_overtimeRate);
		} catch (Exception e) {
			throw new RuntimeException("加班费率字段只能输入数值");
		}
	}

	@ReadValue("资源类型/pricingModel")
	private String getPricingModelText() {
		if ("byHour".equals(pricingModel)) {
			return "按每小时计费";
		} else if ("byTimes".equals(pricingModel)) {
			return "按使用次数计费";
		}
		return "";
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public String getType() {
		return type;
	}

	public ObjectId get_id() {
		return _id;
	}

	@Structure("list")
	public List<?> getResource() {
		if (TYPE_HR.equals(type))
			return ServicesLoader.get(UserService.class).createDataSet(new Query().filter(new BasicDBObject("resourceType_id",_id)).bson());
		else
			return ServicesLoader.get(CommonService.class).getERResources(_id);

	}
	
	@Structure("count")
	public long countResource() {
		if (TYPE_HR.equals(type))
			return ServicesLoader.get(UserService.class).count(new BasicDBObject("resourceType_id",_id));
		else
			return ServicesLoader.get(CommonService.class).countERResources(_id);

	}
	
	@Behavior({"资源类型/添加资源","资源类型/编辑资源类型","资源类型/删除资源类型"})
	public boolean enabledBehavior = true;
}
