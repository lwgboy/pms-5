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

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.DetectionInd;
import com.bizvisionsoft.service.model.QuanlityInfInd;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RBSType;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.service.model.RiskScore;
import com.bizvisionsoft.service.model.RiskUrgencyInd;
import com.mongodb.BasicDBObject;

@Path("/risk")
public interface RiskService {

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/type/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.LIST)
	public List<RBSType> listRBSType();

	@POST
	@Path("/type/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.INSERT)
	public RBSType insertRBSType(@ServiceParam(ServiceParam.OBJECT) RBSType item);

	@DELETE
	@Path("/type/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.DELETE)
	public long deleteRBSType(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/type/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("RBS类别/" + DataSet.UPDATE)
	public long updateRBSType(BasicDBObject filterAndUpdate);

	@POST
	@Path("/rbs/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<RBSItem> listRBSItem(BasicDBObject condition);

	@POST
	@Path("/rbs/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countRBSItem(BasicDBObject filter);

	@POST
	@Path("/rbs/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public RBSItem insertRBSItem(RBSItem item);

	@DELETE
	@Path("/rbs/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteRBSItem(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/rbs/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateRBSItem(BasicDBObject filterAndUpdate);

	@POST
	@Path("/effect/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public RiskEffect addRiskEffect(RiskEffect re);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/urgInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险临近性指标设置/" + DataSet.LIST)
	public List<RiskUrgencyInd> listRiskUrgencyInd();

	@POST
	@Path("/urgInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险临近性指标设置/" + DataSet.INSERT)
	public RiskUrgencyInd insertRiskUrgencyInd(@ServiceParam(ServiceParam.OBJECT) RiskUrgencyInd item);

	@DELETE
	@Path("/urgInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险临近性指标设置/" + DataSet.DELETE)
	public long deleteRiskUrgencyInd(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/urgInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险临近性指标设置/" + DataSet.UPDATE)
	public long updateRiskUrgencyInd(BasicDBObject filterAndUpdate);

	@GET
	@Path("/urgInds/{days}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String getUrgencyText(@PathParam("days") long days);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/qltyInfInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("质量影响级别/" + DataSet.LIST)
	public List<QuanlityInfInd> listRiskQuanlityInfInd();

	@POST
	@Path("/qltyInfInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("质量影响级别/" + DataSet.INSERT)
	public QuanlityInfInd insertRiskQuanlityInfInd(@ServiceParam(ServiceParam.OBJECT) QuanlityInfInd item);

	@DELETE
	@Path("/qltyInfInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("质量影响级别/" + DataSet.DELETE)
	public long deleteRiskQuanlityInfInd(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/qltyInfInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("质量影响级别/" + DataSet.UPDATE)
	public long updateRiskQuanlityInfInd(BasicDBObject filterAndUpdate);
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/detectInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("可探测性级别/" + DataSet.LIST)
	public List<DetectionInd> listRiskDetectionInd();

	@POST
	@Path("/detectInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("可探测性级别/" + DataSet.INSERT)
	public DetectionInd insertRiskDetectionInd(@ServiceParam(ServiceParam.OBJECT) DetectionInd item);

	@DELETE
	@Path("/detectInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("可探测性级别/" + DataSet.DELETE)
	public long deleteRiskDetectionInd(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/detectInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("可探测性级别/" + DataSet.UPDATE)
	public long updateRiskDetectionInd(BasicDBObject filterAndUpdate);
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/scoreInds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险评分标准/" + DataSet.LIST)
	public List<RiskScore> listRiskScoreInd();

	@POST
	@Path("/scoreInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险评分标准/" + DataSet.INSERT)
	public RiskScore insertRiskScoreInd(@ServiceParam(ServiceParam.OBJECT) RiskScore item);

	@DELETE
	@Path("/scoreInds/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险评分标准/" + DataSet.DELETE)
	public long deleteRiskScoreInd(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@PUT
	@Path("/scoreInds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("风险评分标准/" + DataSet.UPDATE)
	public long updateRiskScoreInd(BasicDBObject filterAndUpdate);

}
