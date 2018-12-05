package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.Backup;
import com.bizvisionsoft.service.model.ServerInfo;
import com.mongodb.BasicDBObject;

@Path("/sysman")
public interface SystemService {

	@GET
	@Path("/{req}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public ServerInfo getServerInfo(@PathParam("req") String req);

	@POST
	@Path("/dump/notes/{note}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String mongodbDump(@PathParam("note") String note);

	@POST
	@Path("/backup/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("管理备份/" + DataSet.LIST)
	public List<Backup> getBackups();

	@PUT
	@Path("/backup/note/id/{id}/note/{note}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateBackupNote(@PathParam("id") String id, @PathParam("note") String note);

	@DELETE
	@Path("/backup/id/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean deleteBackup(@PathParam("id") String id);

	@POST
	@Path("/restore/id/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean restoreFromBackup(@PathParam("id") String id);

	@GET
	@Path("/clientSetting/{clientId}/{name}/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String getClientSetting(@PathParam("userId") String userId, @PathParam("clientId") String clientId,
			@PathParam("name") String name);

	@PUT
	@Path("/clientSetting/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateClientSetting(Document setting);

	@DELETE
	@Path("/clientSetting/{clientId}/{name}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteClientSetting(@PathParam("clientId") String clientId, @PathParam("name") String name);

	@DELETE
	@Path("/clientSetting/{clientId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteClientSetting(@PathParam("clientId") String clientId);

	@DELETE
	@Path("/clientSetting/{clientId}/{name}/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteClientSetting(@PathParam("userId") String userId, @PathParam("clientId") String clientId,
			@PathParam("name") String name);

	@POST
	@Path("/updateSystem/{versionNumber}/{packageCode}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateSystem(@PathParam("versionNumber") String versionNumber, @PathParam("packageCode") String packageCode);

	@POST
	@Path("/createindex/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void createIndex();

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@POST
	@Path("/valueRule/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.LIST)
	public List<ValueRule> listValueRule(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/valueRule/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.COUNT)
	public long countValueRule(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/valueRule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.INSERT)
	public ValueRule insertValueRule(@MethodParam(MethodParam.OBJECT) ValueRule valueRule);

	@DELETE
	@Path("/valueRule/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.DELETE)
	public long deleteValueRule(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/valueRule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.UPDATE)
	public long updateValueRule(BasicDBObject filterAndUpdate);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/valueRule/{_id}/segment/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.LIST)
	public List<ValueRuleSegment> listValueRuleSegment(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId rule_id);

	@POST
	@Path("/valueRule/{_id}/segment/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.COUNT)
	public long countValueRuleSegment(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId rule_id);

	@POST
	@Path("/valueRuleSegment/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ValueRuleSegment insertValueRuleSegment(ValueRuleSegment vrs);
	
	@DELETE
	@Path("/valueRuleSegment/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.DELETE)
	public long deleteValueRuleSegment(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@PUT
	@Path("/valueRuleSegment/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.UPDATE)
	public long updateValueRuleSegment(BasicDBObject filterAndUpdate);

	@POST
	@Path("/valueRuleSegment/maxIndex/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getMaxSegmentIndex(ObjectId rule_id);

}