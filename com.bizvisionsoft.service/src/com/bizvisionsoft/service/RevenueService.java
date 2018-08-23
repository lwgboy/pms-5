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

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.model.RevenueForecastItem;
import com.bizvisionsoft.service.model.RevenueRealizeItem;

@Path("/revenue")
public interface RevenueService {

	@GET
	@Path("/forecast/{scope_id}/type")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String getRevenueForecastType(@PathParam("scope_id") ObjectId scope_id);

	@GET
	@Path("/forecast/{scope_id}/forwardIndex")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int getForwardRevenueForecastIndex(@PathParam("scope_id") ObjectId scope_id);

	@PUT
	@Path("/forecast/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateRevenueForecastItem(RevenueForecastItem rfi);

	@GET
	@Path("/forecast/{scope_id}/{subject}/{type}/{index}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public double getRevenueForecastAmount(@PathParam("scope_id") ObjectId scope_id,
			@PathParam("subject") String subject, @PathParam("type") String type, @PathParam("index") int index);

	@POST
	@Path("/forecast/{scope_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<RevenueForecastItem> listRevenueForecast(@PathParam("scope_id") ObjectId scope_id);

	@DELETE
	@Path("/forecast/{scope_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void clearRevenueForecast(@PathParam("scope_id") ObjectId scope_id);

	@POST
	@Path("/realize/{scope_id}/groupByPeriod")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> groupRevenueRealizeAmountByPeriod(ObjectId scope_id);

	@GET
	@Path("/realize/{scope_id}/{subject}/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public double getRevenueRealizeAmount(@PathParam("scope_id") ObjectId scope_id,
			@PathParam("subject") String subject, @PathParam("index") String id);

	@PUT
	@Path("/realize/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void updateRevenueRealizeItem(RevenueRealizeItem item);

	@GET
	@Path("/realize/{scope_id}/period")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<String> getRevenueRealizePeriod(@PathParam("scope_id") ObjectId scope_id);

}
