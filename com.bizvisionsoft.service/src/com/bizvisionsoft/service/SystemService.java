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
	@Path("/{domain}/{req}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public ServerInfo getServerInfo(@PathParam("req") String req);

	@POST
	@Path("/{domain}/dump/notes/{note}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String mongodbDump(@PathParam("note") String note,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/backup/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("管理备份/" + DataSet.LIST)
	public List<Backup> getBackups(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/backup/note/id/{id}/note/{note}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateBackupNote(@PathParam("id") String id, @PathParam("note") String note,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/backup/id/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean deleteBackup(@PathParam("id") String id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/restore/id/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean restoreFromBackup(@PathParam("id") String id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/clientSetting/{clientId}/{name}/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String getClientSetting(@PathParam("userId") String userId, @PathParam("clientId") String clientId,
			@PathParam("name") String name,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/clientSetting/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateClientSetting(Document setting,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/clientSetting/{clientId}/{name}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteClientSetting(@PathParam("clientId") String clientId, @PathParam("name") String name,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/clientSetting/{clientId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteClientSetting(@PathParam("clientId") String clientId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/clientSetting/{clientId}/{name}/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void deleteClientSetting(@PathParam("userId") String userId, @PathParam("clientId") String clientId,
			@PathParam("name") String name,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/updateSystem/{versionNumber}/{packageCode}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateSystem(@PathParam("versionNumber") String versionNumber, @PathParam("packageCode") String packageCode,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/createindex/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void createIndex(@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@POST
	@Path("/{domain}/valueRule/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.LIST)
	public List<ValueRule> listValueRule(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/valueRule/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.COUNT)
	public long countValueRule(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/valueRule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.INSERT)
	public ValueRule insertValueRule(@MethodParam(MethodParam.OBJECT) ValueRule valueRule,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/valueRule/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.DELETE)
	public long deleteValueRule(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/valueRule/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值生成规则/" + DataSet.UPDATE)
	public long updateValueRule(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@POST
	@Path("/{domain}/valueRule/{_id}/segment/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.LIST)
	public List<ValueRuleSegment> listValueRuleSegment(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId rule_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/valueRule/{_id}/segment/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.COUNT)
	public long countValueRuleSegment(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("_id") @MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) ObjectId rule_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/valueRuleSegment/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ValueRuleSegment insertValueRuleSegment(ValueRuleSegment vrs,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/valueRuleSegment/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.DELETE)
	public long deleteValueRuleSegment(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/valueRuleSegment/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("值规则段/" + DataSet.UPDATE)
	public long updateValueRuleSegment(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/valueRuleSegment/maxIndex/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getMaxSegmentIndex(ObjectId rule_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

}