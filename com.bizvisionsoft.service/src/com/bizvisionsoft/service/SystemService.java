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

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.service.model.Backup;
import com.bizvisionsoft.service.model.ServerInfo;

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

}