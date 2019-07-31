package com.bizvisionsoft.service;

import java.util.Date;
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

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.Certificate;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Dictionary;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.NewMessage;
import com.bizvisionsoft.service.model.RefDef;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.VaultFolder;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;

@Path("/common")
@Api("/common")
public interface CommonService {

	@GET
	@Path("/{domain}/workTag/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作标签/" + DataSet.LIST)
	public List<String> listWorkTag(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/msg/userId/{userId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("消息收件箱/" + DataSet.LIST)
	public List<Message> listMessage(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/msg/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Message getMessage(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/msg/userId/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("消息收件箱/" + DataSet.COUNT)
	public long countMessage(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/msg/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("消息收件箱/" + DataSet.UPDATE)
	public long updateMessage(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/unread/msg/userId/{userId}/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("未读消息/" + DataSet.LIST)
	public List<Document> listUnreadMessage(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/unread/msg/userId/{userId}/card/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("未读消息/" + DataSet.COUNT)
	public long countUnreadMessage(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cert/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.LIST)
	public List<Certificate> getCertificates(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cert/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.COUNT)
	public long countCertificate(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cert/names/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格选择器列表/" + DataSet.LIST)
	public List<String> getCertificateNames(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.INSERT)
	public Certificate insertCertificate(@MethodParam(MethodParam.OBJECT) Certificate cert,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/cert/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.DELETE)
	public long deleteCertificate(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/cert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("执业资格列表/" + DataSet.UPDATE)
	public long updateCertificate(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/restype/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.LIST)
	public List<ResourceType> getResourceType(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/restype/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.INSERT)
	public ResourceType insertResourceType(@MethodParam(MethodParam.OBJECT) ResourceType resourceType,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/restype/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.DELETE)
	public long deleteResourceType(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/restype/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资源类型/" + DataSet.UPDATE)
	public long updateResourceType(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/restype/{_id}/er")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Equipment> getERResources(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/restype/{_id}/er/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countERResources(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/restype/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ResourceType getResourceType(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/euip/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "设备设施/" + DataSet.LIST, "设备设施选择表格/" + DataSet.LIST })
	public List<Equipment> getEquipments(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/euip/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("设备设施/" + DataSet.INSERT)
	public Equipment insertEquipment(@MethodParam(MethodParam.OBJECT) Equipment cert,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/euip/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("设备设施/" + DataSet.DELETE)
	public long deleteEquipment(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/euip/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("设备设施/" + DataSet.UPDATE)
	public long updateEquipment(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/cal/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "工作日历/" + DataSet.LIST, "工作日历选择表格/" + DataSet.LIST })
	public List<Calendar> getCalendars(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/cal/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Calendar getCalendar(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cal/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.INSERT)
	public Calendar insertCalendar(@MethodParam(MethodParam.OBJECT) Calendar obj,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/cal/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.DELETE)
	public long deleteCalendar(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/cal/{_id}/wt/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void addCalendarWorktime(BasicDBObject r, @PathParam("_id") ObjectId _cal_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/cal/wt/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateCalendarWorkTime(BasicDBObject r, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/cal/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作日历/" + DataSet.UPDATE)
	public long updateCalendar(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/cal/wt/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteCalendarWorkTime(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/dict/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.LIST)
	public List<Dictionary> getDictionary(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/dict/{type}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Map<String, String> getDictionary(@PathParam("type") String type,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/dict/idname/{type}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Map<String, String> getDictionaryIdNamePair(@PathParam("type") String type,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/dict/{type}/{valueField}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<String> listDictionary(@PathParam("type") String type, @PathParam("valueField") String valueField,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/dict/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.INSERT)
	public Dictionary insertResourceType(@MethodParam(MethodParam.OBJECT) Dictionary resourceType,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/dict/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.DELETE)
	public long deleteDictionary(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/dict/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.UPDATE)
	public long updateDictionary(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/dict/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("名称字典/" + DataSet.COUNT)
	public long countDictionary(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/projectrole/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目角色选择器/" + DataSet.LIST)
	public List<Dictionary> getProjectRole(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/projectrole/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目角色选择器/" + DataSet.COUNT)
	public long countProjectRole(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/projectrole/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Dictionary getProjectRole(@PathParam("id") String id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/accountItem/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "费用类科目/" + DataSet.LIST })
	public List<AccountItem> getAccoutItemRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountIncome/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "损益类科目/" + DataSet.LIST })
	public List<AccountIncome> getAccoutIncomeRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountItem/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutItemRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountIncome/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutIncomeRoot(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountItem/parent/{id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<AccountItem> getAccoutItem(@PathParam("id") String id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountItem/parent/{id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutItem(@PathParam("id") String id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountIncome/parent/{id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutIncome(@PathParam("id") String id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountItem/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<AccountItem> queryAccountItem(BasicDBObject filter, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountIncome/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<AccountIncome> queryAccountIncome(BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "费用类科目/" + DataSet.INSERT })
	public AccountItem insertAccountItem(@MethodParam(MethodParam.OBJECT) AccountItem ai,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accountIncome/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "损益类科目/" + DataSet.INSERT })
	public AccountIncome insertAccountIncome(@MethodParam(MethodParam.OBJECT) AccountIncome ai,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/accountItem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "费用类科目/" + DataSet.DELETE })
	public long deleteAccountItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/accountIncome/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "损益类科目/" + DataSet.DELETE })
	public long deleteAccountIncome(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/accountItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "费用类科目/" + DataSet.UPDATE })
	public long updateAccountItem(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/accountIncome/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "损益类科目/" + DataSet.UPDATE })
	public long updateAccountIncome(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/accoutItem/hasParentIds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> getAllAccoutItemsHasParentIds(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/gencode/{name}/{key}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int generateCode(@PathParam("name") String name, @PathParam("key") String key,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/track/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "视图和工作包列表/" + DataSet.LIST, "视图和工作包选择器/" + DataSet.LIST })
	public List<TrackView> listTrackView(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/track/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "视图和工作包列表/" + DataSet.COUNT, "视图和工作包选择器/" + DataSet.COUNT })
	public long countTrackView(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/track/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("视图和工作包列表/" + DataSet.INSERT)
	public TrackView insertTrackView(@MethodParam(MethodParam.OBJECT) TrackView trackView,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/track/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("视图和工作包列表/" + DataSet.DELETE)
	public long deleteTrackView(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/track/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("视图和工作包列表/" + DataSet.UPDATE)
	public long updateTrackView(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@GET
	@Path("/{domain}/currentcbsperiod")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Date getCurrentCBSPeriod(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/changeprocess/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("变更审核/" + DataSet.LIST)
	public List<ChangeProcess> createChangeProcessDataSet(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/changeprocess/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("变更审核/" + DataSet.INPUT)
	public ChangeProcess getChangeProcess(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/changeprocess/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("变更审核/" + DataSet.INSERT)
	public ChangeProcess insertChangeProcess(@MethodParam(MethodParam.OBJECT) ChangeProcess changeProcess,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/changeprocess/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("变更审核/" + DataSet.DELETE)
	public long deleteChangeProcess(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/changeprocess/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("变更审核/" + DataSet.UPDATE)
	public long updateChangeProcess(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/strudata/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> listStructuredData(BasicDBObject query, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/strudata/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertStructuredData(List<Document> result, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/strudata/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateStructuredData(BasicDBObject fu, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/newMessage/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void sendMessage(NewMessage msg, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/userId/{userId}/budget/mywork")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作/budget")
	public boolean hasSomethingNewOfMyWork(@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/tools/syncOrgFullName")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void syncOrgFullName(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/setting/{name}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getSetting(@PathParam("name") String name, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/setting/{name}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getSetting(@PathParam("name") String name);

	@PUT
	@Path("/{domain}/setting/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateSetting(Document setting, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/setting/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateSetting(Document r);

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/funcrole/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "功能角色选择器/" + DataSet.LIST })
	public List<Dictionary> listFunctionRoles(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/funcrole/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "功能角色选择器/" + DataSet.COUNT })
	public long countFunctionRoles(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/container/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "资料库设置/" + DataSet.LIST })
	public List<VaultFolder> listContainer(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/container/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "资料库设置/" + DataSet.COUNT })
	public long countContainer(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/container/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资料库设置/" + DataSet.DELETE)
	public long deleteContainer(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/container/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资料库设置/" + DataSet.UPDATE)
	public long updateContainer(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/container/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "资料库设置/" + DataSet.INSERT })
	public VaultFolder insertContainer(@MethodParam(MethodParam.OBJECT) VaultFolder vf,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/container/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public VaultFolder getVaultFolder(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/formDef/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("表单定义/" + DataSet.LIST)
	public List<FormDef> listFormDef(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/formDef/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("表单定义/" + DataSet.COUNT)
	public long countFormDef(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/containerFormDef/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资料库表单定义/" + DataSet.LIST)
	public List<Document> listNameOfFormDef(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/containerFormDef/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("资料库表单定义/" + DataSet.COUNT)
	public long countNameOfFormDef(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/formDef/selector/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "表单定义选择表格/" + DataSet.LIST })
	public List<FormDef> listFormDefSelector(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) VaultFolder folder,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/formDef/selector/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "表单定义选择表格/" + DataSet.COUNT })
	public long countFormDefSelector(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT) VaultFolder folder,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/formDef/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteFormDef(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/formDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateFormDef(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/formDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "表单定义/" + DataSet.INSERT })
	public FormDef insertFormDef(@MethodParam(MethodParam.OBJECT) FormDef formDef,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/formDef/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public FormDef getFormDef(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/upgradeFormDef/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public FormDef upgradeFormDef(@PathParam("_id") ObjectId _id, @MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/exportDocRule/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ExportDocRule> listExportDocRule(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/exportDocRule/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countExportDocRule(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/exportDocRule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ExportDocRule insertExportDocRule(@MethodParam(MethodParam.OBJECT) ExportDocRule exportDocRule,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/exportDocRule/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteExportDocRule(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/exportDocRule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateExportDocRule(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/exportDocRule/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ExportDocRule getExportDocRule(@PathParam("_id") ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/refDef/ds/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("参照定义/" + DataSet.LIST)
	public List<RefDef> getRefDef(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId parent_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/refDef/count/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("参照定义/" + DataSet.COUNT)
	public long countRefDef(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId parent_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/refDef/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("参照定义/" + DataSet.DELETE)
	public long deleteRefDef(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/refDef/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("参照定义/" + DataSet.UPDATE)
	public long updateRefDef(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/refDef/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("参照定义/" + DataSet.INSERT)
	public RefDef insertRefDef(@MethodParam(MethodParam.OBJECT) RefDef refDef,
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId parent_id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
