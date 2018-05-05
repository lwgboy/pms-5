package com.bizvisionsoft.service;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.Certificate;
import com.bizvisionsoft.service.model.Dictionary;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.ResourceType;
import com.mongodb.BasicDBObject;

@Path("/common")
public interface CommonService {

	@POST
	@Path("/cert/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.LIST)
	public List<Certificate> getCertificates();

	@POST
	@Path("/cert/names/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格选择器列表/" + DataSet.LIST)
	public List<String> getCertificateNames();

	@POST
	@Path("/cert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.INSERT)
	public Certificate insertCertificate(@ServiceParam(ServiceParam.OBJECT) Certificate cert);

	@DELETE
	@Path("/cert/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.DELETE)
	public long deleteCertificate(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/cert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.UPDATE)
	public long updateCertificate(BasicDBObject filterAndUpdate);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/restype/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.LIST)
	public List<ResourceType> getResourceType();

	@POST
	@Path("/restype/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.INSERT)
	public ResourceType insertResourceType(@ServiceParam(ServiceParam.OBJECT) ResourceType resourceType);

	@DELETE
	@Path("/restype/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.DELETE)
	public long deleteResourceType(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/restype/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.UPDATE)
	public long updateResourceType(BasicDBObject filterAndUpdate);

	@GET
	@Path("/restype/{_id}/er")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Equipment> getERResources(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/restype/{_id}/er/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countERResources(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/restype/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ResourceType getResourceType(@PathParam("_id") ObjectId _id);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/euip/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "设备设施/" + DataSet.LIST, "设备设施选择表格/" + DataSet.LIST })
	public List<Equipment> getEquipments();

	@POST
	@Path("/euip/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("设备设施/" + DataSet.INSERT)
	public Equipment insertEquipment(@ServiceParam(ServiceParam.OBJECT) Equipment cert);

	@DELETE
	@Path("/euip/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("设备设施/" + DataSet.DELETE)
	public long deleteEquipment(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/euip/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("设备设施/" + DataSet.UPDATE)
	public long updateEquipment(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/cal/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.LIST)
	public List<Calendar> getCalendars();

	@POST
	@Path("/cal/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.INSERT)
	public Calendar insertCalendar(@ServiceParam(ServiceParam.OBJECT) Calendar obj);

	@DELETE
	@Path("/cal/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.DELETE)
	public long deleteCalendar(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@POST
	@Path("/cal/{_id}/wt/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void addCalendarWorktime(BasicDBObject r, @PathParam("_id") ObjectId _cal_id);

	@PUT
	@Path("/cal/wt/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateCalendarWorkTime(BasicDBObject r);

	@PUT
	@Path("/cal/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.UPDATE)
	public long updateCalendar(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/cal/wt/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteCalendarWorkTime(@PathParam("_id") ObjectId _id);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/dict/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.LIST)
	public List<Dictionary> getDictionary();

	@POST
	@Path("/dict/{type}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Map<String,String> getDictionary(@PathParam("type") String type);
	
	@POST
	@Path("/dict/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.INSERT)
	public Dictionary insertResourceType(@ServiceParam(ServiceParam.OBJECT) Dictionary resourceType);

	@DELETE
	@Path("/dict/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.DELETE)
	public long deleteDictionary(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/dict/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.UPDATE)
	public long updateDictionary(BasicDBObject filterAndUpdate);
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@GET
	@Path("/accountitem/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "财务科目设置/" + DataSet.LIST})
	public List<AccountItem> getAccoutItemRoot();
	
	@GET
	@Path("/accountitem/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutItemRoot();
	
	@GET
	@Path("/accountitem/parent/{_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<AccountItem> getAccoutItem(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	
	@GET
	@Path("/accountitem/parent/{_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutItem(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/accountitem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<AccountItem> queryAccountItem(BasicDBObject filter);
	
	@POST
	@Path("/accountitem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "财务科目设置/" + DataSet.INSERT})
	public AccountItem insertAccountItem(@ServiceParam(ServiceParam.OBJECT) AccountItem ai);

	@DELETE
	@Path("/accountitem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "财务科目设置/" + DataSet.DELETE})
	public long deleteAccountItem(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/accountitem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "财务科目设置/" + DataSet.UPDATE})
	public long updateAccountItem(BasicDBObject filterAndUpdate);


}
