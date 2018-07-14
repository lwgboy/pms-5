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
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.Certificate;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Dictionary;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.TrackView;
import com.mongodb.BasicDBObject;

@Path("/common")
public interface CommonService {

	@GET
	@Path("/workTag/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("������ǩ/" + DataSet.LIST)
	public List<String> listWorkTag();

	@POST
	@Path("/msg/userId/{userId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Ϣ�ռ���/" + DataSet.LIST)
	public List<Message> listMessage(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/msg/userId/{userId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Ϣ�ռ���/" + DataSet.COUNT)
	public long countMessage(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userId") String userId);

	@POST
	@Path("/cert/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ִҵ�ʸ��б�/" + DataSet.LIST)
	public List<Certificate> getCertificates(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/cert/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ִҵ�ʸ��б�/" + DataSet.COUNT)
	public long countCertificate(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/cert/names/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ִҵ�ʸ�ѡ�����б�/" + DataSet.LIST)
	public List<String> getCertificateNames();

	@POST
	@Path("/cert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ִҵ�ʸ��б�/" + DataSet.INSERT)
	public Certificate insertCertificate(@MethodParam(MethodParam.OBJECT) Certificate cert);

	@DELETE
	@Path("/cert/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ִҵ�ʸ��б�/" + DataSet.DELETE)
	public long deleteCertificate(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/cert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("ִҵ�ʸ��б�/" + DataSet.UPDATE)
	public long updateCertificate(BasicDBObject filterAndUpdate);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/restype/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Դ����/" + DataSet.LIST)
	public List<ResourceType> getResourceType();

	@POST
	@Path("/restype/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Դ����/" + DataSet.INSERT)
	public ResourceType insertResourceType(@MethodParam(MethodParam.OBJECT) ResourceType resourceType);

	@DELETE
	@Path("/restype/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Դ����/" + DataSet.DELETE)
	public long deleteResourceType(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/restype/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��Դ����/" + DataSet.UPDATE)
	public long updateResourceType(BasicDBObject filterAndUpdate);

	@POST
	@Path("/restype/{_id}/er")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Equipment> getERResources(@PathParam("_id") ObjectId _id);

	@POST
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
	@DataSet({ "�豸��ʩ/" + DataSet.LIST, "�豸��ʩѡ����/" + DataSet.LIST })
	public List<Equipment> getEquipments();

	@POST
	@Path("/euip/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�豸��ʩ/" + DataSet.INSERT)
	public Equipment insertEquipment(@MethodParam(MethodParam.OBJECT) Equipment cert);

	@DELETE
	@Path("/euip/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�豸��ʩ/" + DataSet.DELETE)
	public long deleteEquipment(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/euip/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�豸��ʩ/" + DataSet.UPDATE)
	public long updateEquipment(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/cal/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��������/" + DataSet.LIST, "��������ѡ����/" + DataSet.LIST })
	public List<Calendar> getCalendars();

	@GET
	@Path("/cal/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Calendar getCalendar(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/cal/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��������/" + DataSet.INSERT)
	public Calendar insertCalendar(@MethodParam(MethodParam.OBJECT) Calendar obj);

	@DELETE
	@Path("/cal/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��������/" + DataSet.DELETE)
	public long deleteCalendar(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

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
	@DataSet("��������/" + DataSet.UPDATE)
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
	@DataSet("�����ֵ�/" + DataSet.LIST)
	public List<Dictionary> getDictionary();

	@POST
	@Path("/dict/{type}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Map<String, String> getDictionary(@PathParam("type") String type);

	@POST
	@Path("/dict/idname/{type}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Map<String, String> getDictionaryIdNamePair(@PathParam("type") String type);

	@POST
	@Path("/dict/{type}/{valueField}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<String> listDictionary(@PathParam("type") String type, @PathParam("valueField") String valueField);

	@POST
	@Path("/dict/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����ֵ�/" + DataSet.INSERT)
	public Dictionary insertResourceType(@MethodParam(MethodParam.OBJECT) Dictionary resourceType);

	@DELETE
	@Path("/dict/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����ֵ�/" + DataSet.DELETE)
	public long deleteDictionary(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/dict/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("�����ֵ�/" + DataSet.UPDATE)
	public long updateDictionary(BasicDBObject filterAndUpdate);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/accountitem/root/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����Ŀ����/" + DataSet.LIST })
	public List<AccountItem> getAccoutItemRoot();

	@POST
	@Path("/accountitem/root/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countAccoutItemRoot();

	@POST
	@Path("/accountitem/parent/{_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<AccountItem> getAccoutItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
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
	@DataSet({ "�����Ŀ����/" + DataSet.INSERT })
	public AccountItem insertAccountItem(@MethodParam(MethodParam.OBJECT) AccountItem ai);

	@DELETE
	@Path("/accountitem/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����Ŀ����/" + DataSet.DELETE })
	public long deleteAccountItem(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/accountitem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "�����Ŀ����/" + DataSet.UPDATE })
	public long updateAccountItem(BasicDBObject filterAndUpdate);

	@POST
	@Path("/gencode/{name}/{key}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int generateCode(@PathParam("name") String name, @PathParam("key") String key);

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/track/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "��ͼ�͹������б�/" + DataSet.LIST, "��ͼ�͹�����ѡ����/" + DataSet.LIST })
	public List<TrackView> getTrackView();

	@POST
	@Path("/track/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ͼ�͹������б�/" + DataSet.INSERT)
	public TrackView insertTrackView(@MethodParam(MethodParam.OBJECT) TrackView trackView);

	@DELETE
	@Path("/track/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ͼ�͹������б�/" + DataSet.DELETE)
	public long deleteTrackView(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/track/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("��ͼ�͹������б�/" + DataSet.UPDATE)
	public long updateTrackView(BasicDBObject filterAndUpdate);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@GET
	@Path("/currentcbsperiod")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Date getCurrentCBSPeriod();

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/changeprocess/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("������/" + DataSet.LIST)
	public List<ChangeProcess> createChangeProcessDataSet();

	@GET
	@Path("/changeprocess/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("������/" + DataSet.INPUT)
	public ChangeProcess getChangeProcess(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@POST
	@Path("/changeprocess/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("������/" + DataSet.INSERT)
	public ChangeProcess insertChangeProcess(@MethodParam(MethodParam.OBJECT) ChangeProcess changeProcess);

	@DELETE
	@Path("/changeprocess/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("������/" + DataSet.DELETE)
	public long deleteChangeProcess(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/changeprocess/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("������/" + DataSet.UPDATE)
	public long updateChangeProcess(BasicDBObject filterAndUpdate);

	@POST
	@Path("/createindex/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void createIndex();

	@POST
	@Path("/strudata/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> listStructuredData(BasicDBObject query);

	@POST
	@Path("/strudata/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void insertStructuredData(List<Document> result);

	@PUT
	@Path("/strudata/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateStructuredData(BasicDBObject fu);

}
