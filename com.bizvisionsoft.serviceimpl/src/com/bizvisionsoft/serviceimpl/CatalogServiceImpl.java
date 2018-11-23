package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CatalogService;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.Equipment;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.Function;
import com.mongodb.client.model.Aggregates;

public class CatalogServiceImpl extends BasicServiceImpl implements CatalogService {

	@Override
	public Document createDefaultOption() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.set(year, 0, 1, 0, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MINUTE, -1);
		Date end = cal.getTime();
		return new Document("dateRange", Arrays.asList(start, end)).append("dateType", "月").append("seriesType", "汇总")
				.append("dataType", new ArrayList<String>(Arrays.asList("计划", "实际"))).append("showData", "叠加")
				.append("aggregateType", "不累计");
	}

	/**
	 * 该人员所属的组织
	 */
	@Override
	public List<Catalog> listResOrgRoot(String userId) {
		List<Bson> pipeline = Arrays.asList(Aggregates.match(new Document("userId", userId)),
				Aggregates.lookup("organization", "org_id", "_id", "org"), Aggregates.unwind("$org"));
		return c("user").aggregate(pipeline).map(d -> (Document) d.get("org")).map(CatalogMapper::org).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listResOrgStructure(Catalog parent) {
		List<Catalog> result = new ArrayList<Catalog>();
		if (typeEquals(parent, Organization.class)) {
			listSubOrg(parent._id, result);
			listResourceType(parent._id, "user", result);
			listResourceType(parent._id, "equipment", result);
		} else if (typeEquals(parent, ResourceType.class)) {
			ObjectId resourceType_id = parent._id;
			Object org_id = parent.meta.get("org_id");
			listHRResource(org_id, resourceType_id, CatalogMapper::user, result);
			listEQResource(org_id, resourceType_id, CatalogMapper::equipment, result);
		} else if (typeEquals(parent, EPS.class)) {
			listSubEPS(parent._id, result);
			listProject(parent._id, result);
		} else if (typeEquals(parent, Project.class)) {
			// TODO 列出阶段
			// TODO 列出资源类别
		}
		return result;
	}

	@Override
	public long countResOrgStructure(Catalog parent) {
		// 如果parent是组织，获取下级组织和资源类型
		long count = 0;
		if (typeEquals(parent, Organization.class)) {
			count += countSubOrg(parent._id);
			count += countResourceType(parent._id, "user");
			count += countResourceType(parent._id, "equipment");
		} else if (typeEquals(parent, ResourceType.class)) {
			ObjectId resourceType_id = parent._id;
			Object org_id = parent.meta.get("org_id");
			count += countResource(org_id, resourceType_id, "user");
			count += countResource(org_id, resourceType_id, "equipment");
		} else if (typeEquals(parent, EPS.class)) {
			count += countSubEPS(parent._id);
			count += countProject(parent._id);
		} else if (typeEquals(parent, Project.class)) {
			// TODO 列出资源类别
		}
		return count;
	}

	private long countResourceType(ObjectId org_id, String collection) {
		List<Bson> p = new ArrayList<>();
		p.addAll(new JQ("查询-组织-资源类型").set("org_id", org_id).array());
		p.add(Aggregates.count());
		return Optional.ofNullable(c(collection).aggregate(p).first()).map(m -> ((Number) m.get("count")).longValue()).orElse(0l);
	}

	private List<Catalog> listResourceType(ObjectId org_id, String collection, List<Catalog> into) {
		List<Bson> p = new JQ("查询-组织-资源类型").set("org_id", org_id).array();
		return c(collection).aggregate(p)
				.map(d -> ((Document) d.get("resourceType")).append("org_id", ((Document) d.get("_id")).get("org_id")))
				.map(CatalogMapper::resourceType).into(into);
	}

	private long countSubOrg(ObjectId org_id) {
		return c("organization").countDocuments(new Document("parent_id", org_id));
	}

	private long countSubEPS(ObjectId eps_id) {
		return c("eps").countDocuments(new Document("parent_id", eps_id));
	}

	private long countProject(ObjectId eps_id) {
		return c("project").countDocuments(new Document("eps_id", eps_id));
	}

	private List<Catalog> listSubOrg(ObjectId org_id, List<Catalog> into) {
		return c("organization").find(new Document("parent_id", org_id)).map(CatalogMapper::org).into(into);
	}

	private List<Catalog> listSubEPS(ObjectId parent_id, List<Catalog> into) {
		return c("eps").find(new Document("parent_id", parent_id)).map(CatalogMapper::eps).into(into);
	}

	private List<Catalog> listProject(ObjectId eps_id, List<Catalog> into) {
		return c("project").find(new Document("eps_id", eps_id)).map(CatalogMapper::project).into(into);
	}

	private <T> List<T> listHRResource(Object org_id, ObjectId resourceType_id, Function<Document, T> map, List<T> into) {
		return c("user").find(new Document("org_id", org_id).append("resourceType_id", resourceType_id)).map(map).into(into);
	}

	private <T> List<T> listEQResource(Object org_id, ObjectId resourceType_id, Function<Document, T> map, List<T> into) {
		return c("equipment").find(new Document("org_id", org_id).append("resourceType_id", resourceType_id)).map(map).into(into);
	}

	private long countResource(Object org_id, ObjectId resourceType_id, String collection) {
		return c(collection).countDocuments(new Document("org_id", org_id).append("resourceType_id", resourceType_id));
	}

	private boolean typeEquals(Catalog c, Class<?> clas) {
		return clas.getName().equals(c.type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document createResChart(Document condition) {

		checkResChartOption(condition);

		List<Document> series = new ArrayList<Document>();

		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		String dateType = option.getString("dateType");
		List<String> dataType = (List<String>) option.get("dataType");
		String seriesType = option.getString("seriesType");
		String showData = option.getString("showData");
		String aggregateType = option.getString("aggregateType");

		// 并根据时间构建查询语句
		Document match = new Document();
		Date start = (Date) dateRange.get(0);
		Date end = (Date) dateRange.get(1);
		match.append("$and", Arrays.asList(new Document("id", new Document("$gte", start)), new Document("id", new Document("$lte", end))));

		// 根据时间类型和起止时间，构建group的_id和xAxis数据
		Document group_id = new Document();
		List<String> xAxisData = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		Document markLineData = new Document();

		SimpleDateFormat sdf;
		if ("日".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			// TODO 需要根据所选资源的资源日历进行设置
			markLineData.append("data", "");
		} else if ("月".equals(dateType)) {
			group_id.append("$dateToString", new Document("format", "%Y-%m").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy-MM");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.MONTH, 1);
			}
		} else {
			group_id.append("$dateToString", new Document("format", "%Y").append("date", "$id"));
			sdf = new SimpleDateFormat("yyyy");
			while (!cal.getTime().after(end)) {
				xAxisData.add(sdf.format(cal.getTime()));
				cal.add(Calendar.YEAR, 1);
			}
		}
		List<Double> totalPlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalPlanAggAmountData = new ArrayList<Double>();
		List<Double> basicPlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicPlanAggAmountData = new ArrayList<Double>();
		List<Double> overTimePlanAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimePlanAggAmountData = new ArrayList<Double>();

		List<Double> totalActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> totalActualAggAmountData = new ArrayList<Double>();
		List<Double> basicActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> basicActualAggAmountData = new ArrayList<Double>();
		List<Double> overTimeActualAggWorkTimeData = new ArrayList<Double>();
		List<Double> overTimeActualAggAmountData = new ArrayList<Double>();

		List<String> legendData = new ArrayList<String>();
		// 根据系列类型构建series
		if ("并列".equals(seriesType)) {
			((List<Document>) condition.get("input")).forEach((Document doc) -> {
				match.append("resource_id", new Document("$in", addChildResource(doc)));
				appendSeries(series, dataType, showData, aggregateType, match, group_id, xAxisData, markLineData, totalPlanAggWorkTimeData,
						totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData, legendData,
						doc.getString("label"));
			});
		} else {
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			match.append("resource_id", new Document("$in", resourceIds));
			appendSeries(series, dataType, showData, aggregateType, match, group_id, xAxisData, markLineData, totalPlanAggWorkTimeData,
					totalPlanAggAmountData, basicPlanAggWorkTimeData, basicPlanAggAmountData, overTimePlanAggWorkTimeData,
					overTimePlanAggAmountData, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
					basicActualAggAmountData, overTimeActualAggWorkTimeData, overTimeActualAggAmountData, legendData, "");

		}
		// 根据累计值类型构建series
		if ("总计".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				series.add(new JQ("图表-资源图表-累计工时").set("name", "累计计划工时").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "累计计划工时")
						.set("data", totalPlanAggWorkTimeData).doc());
				legendData.add("累计计划工时");
				series.add(new JQ("图表-资源图表-累计金额").set("name", "累计计划金额").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "累计计划金额")
						.set("data", totalPlanAggAmountData).doc());
				legendData.add("累计计划金额");
			}
			if (dataType.contains("实际")) {
				series.add(
						new JQ("图表-资源图表-累计工时").set("name", "累计实际工时").set("color1", "rgba(0,150,136,1)").set("color2", "rgba(0,150,136,0.7)")
								.set("color3", "rgba(0,150,136,0)").set("stack", "累计实际工时").set("data", totalActualAggWorkTimeData).doc());
				legendData.add("累计实际工时");
				series.add(
						new JQ("图表-资源图表-累计金额").set("name", "累计实际金额").set("color1", "rgba(0,150,136,1)").set("color2", "rgba(0,150,136,0.7)")
								.set("color3", "rgba(0,150,136,0)").set("stack", "累计实际金额").set("data", totalActualAggAmountData).doc());
				legendData.add("累计实际金额");
			}
		} else if ("标准加班".equals(aggregateType)) {
			if (dataType.contains("计划")) {
				series.add(new JQ("图表-资源图表-累计工时").set("name", "累计计划标准工时").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "累计计划工时")
						.set("data", basicPlanAggWorkTimeData).doc());
				legendData.add("累计计划标准工时");
				series.add(new JQ("图表-资源图表-累计金额").set("name", "累计计划标准金额").set("color1", "rgba(95,184,120,1)")
						.set("color2", "rgba(95,184,120,0.7)").set("color3", "rgba(95,184,120,0)").set("stack", "累计计划金额")
						.set("data", basicPlanAggAmountData).doc());
				legendData.add("累计计划标准金额");
				series.add(new JQ("图表-资源图表-累计工时").set("name", "累计计划加班工时").set("color1", "rgba(255,87,34,1)")
						.set("color2", "rgba(255,87,34,0.7)").set("color3", "rgba(255,87,34,0)").set("stack", "累计计划工时")
						.set("data", overTimePlanAggWorkTimeData).doc());
				legendData.add("累计计划加班工时");
				series.add(new JQ("图表-资源图表-累计金额").set("name", "累计计划加班金额").set("color1", "rgba(255,87,34,1)")
						.set("color2", "rgba(255,87,34,0.7)").set("color3", "rgba(255,87,34,0)").set("stack", "累计计划金额")
						.set("data", overTimePlanAggAmountData).doc());
				legendData.add("累计计划加班金额");
			}
			if (dataType.contains("实际")) {
				series.add(new JQ("图表-资源图表-累计工时").set("name", "累计实际标准工时").set("color1", "rgba(0,150,136,1)")
						.set("color2", "rgba(0,150,136,0.7)").set("color3", "rgba(0,150,136,0)").set("stack", "累计实际工时")
						.set("data", basicActualAggWorkTimeData).doc());
				legendData.add("累计实际标准工时");
				series.add(new JQ("图表-资源图表-累计金额").set("name", "累计实际标准金额").set("color1", "rgba(0,150,136,1)")
						.set("color2", "rgba(0,150,136,0.7)").set("color3", "rgba(0,150,136,0)").set("stack", "累计实际金额")
						.set("data", basicActualAggAmountData).doc());
				legendData.add("累计实际标准金额");
				series.add(new JQ("图表-资源图表-累计工时").set("name", "累计实际加班工时").set("color1", "rgba(255,184,0,1)")
						.set("color2", "rgba(255,184,0,0.7)").set("color3", "rgba(255,184,0,0)").set("stack", "累计实际工时")
						.set("data", overTimeActualAggWorkTimeData).doc());
				legendData.add("累计实际加班工时");
				series.add(new JQ("图表-资源图表-累计金额").set("name", "累计实际加班金额").set("color1", "rgba(255,184,0,1)")
						.set("color2", "rgba(255,184,0,0.7)").set("color3", "rgba(255,184,0,0)").set("stack", "累计实际金额")
						.set("data", overTimeActualAggAmountData).doc());
				legendData.add("累计实际加班金额");
			}
		}

		// 生成资源图表
		// TODO legendData和series的顺序需要调整
		JQ jq = new JQ("图表-资源图表").set("title", "").set("legendData", legendData).set("xAxisData", xAxisData).set("series", series);
		return jq.doc();
	}

	/**
	 * 检查图表条件，抛出错误
	 * 
	 * @param condition
	 */
	private void checkResChartOption(Document condition) {
		Object option = condition.get("option");
		if (option instanceof Document) {
			Object dateRange = ((Document) option).get("dateRange");
			if (dateRange instanceof List<?>) {
				if (((List<?>) dateRange).size() == 2) {
					Object d0 = ((List<?>) dateRange).get(0);
					Object d1 = ((List<?>) dateRange).get(1);
					if (!(d0 instanceof Date) || !(d1 instanceof Date) || !((Date) d0).before((Date) d1)) {
						throw new ServiceException("日期范围数据不合法");
					}
				} else {
					throw new ServiceException("日期范围数据不合法");
				}
			} else {
				throw new ServiceException("日期范围类型错误");
			}
		} else {
			throw new ServiceException("选项类型错误");
		}
	}

	/**
	 * 构建series
	 * 
	 * @param series
	 * @param dataType
	 * @param showData
	 * @param aggregateType
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @param markLineData
	 * @param totalPlanAggWorkTimeData
	 * @param totalPlanAggAmountData
	 * @param basicPlanAggWorkTimeData
	 * @param basicPlanAggAmountData
	 * @param overTimePlanAggWorkTimeData
	 * @param overTimePlanAggAmountData
	 * @param totalActualAggWorkTimeData
	 * @param totalActualAggAmountData
	 * @param basicActualAggWorkTimeData
	 * @param basicActualAggAmountData
	 * @param overTimeActualAggWorkTimeData
	 * @param overTimeActualAggAmountData
	 * @param legendData
	 * @param label
	 */
	@SuppressWarnings("unchecked")
	private void appendSeries(List<Document> series, List<String> dataType, String showData, String aggregateType, Document match,
			Document group_id, List<String> xAxisData, Document markLineData, List<Double> totalPlanAggWorkTimeData,
			List<Double> totalPlanAggAmountData, List<Double> basicPlanAggWorkTimeData, List<Double> basicPlanAggAmountData,
			List<Double> overTimePlanAggWorkTimeData, List<Double> overTimePlanAggAmountData, List<Double> totalActualAggWorkTimeData,
			List<Double> totalActualAggAmountData, List<Double> basicActualAggWorkTimeData, List<Double> basicActualAggAmountData,
			List<Double> overTimeActualAggWorkTimeData, List<Double> overTimeActualAggAmountData, List<String> legendData, String label) {
		if (dataType.contains("计划")) {
			// 获取计划资源数据
			Object[] resourceData = getResourceData("resourcePlan", "$planBasicQty", "$planOverTimeQty", match, group_id, xAxisData);
			if ("标准".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划标准" + "工时").set("stack", label + "计划工时").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划标准" + "金额").set("stack", label + "计划金额").set("data", resourceData[3])
						.doc());
				legendData.add(label + "计划标准" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData,
						basicPlanAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
			} else if ("加班".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划加班" + "工时").set("stack", label + "计划工时").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划加班" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划加班" + "金额").set("stack", label + "计划金额").set("data", resourceData[4])
						.doc());
				legendData.add(label + "计划加班" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else if ("汇总".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划" + "工时").set("stack", label + "计划工时").set("data", resourceData[2])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划" + "金额").set("stack", label + "计划金额").set("data", resourceData[5])
						.doc());
				legendData.add(label + "计划" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData,
						basicPlanAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);

				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划标准" + "工时").set("stack", label + "计划工时").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划标准" + "金额").set("stack", label + "计划金额").set("data", resourceData[3])
						.doc());
				legendData.add(label + "计划标准" + "金额");

				series.add(new JQ("图表-资源图表-工时").set("name", label + "计划加班" + "工时").set("stack", label + "计划工时").set("data", resourceData[1])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "计划加班" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "计划加班" + "金额").set("stack", label + "计划金额").set("data", resourceData[4])
						.doc());
				legendData.add(label + "计划加班" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, basicPlanAggWorkTimeData,
						basicPlanAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
				appendAggregateData(aggregateType, totalPlanAggWorkTimeData, totalPlanAggAmountData, overTimePlanAggWorkTimeData,
						overTimePlanAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			}
		}
		if (dataType.contains("实际")) {
			// 获取实际资源数据
			Object[] resourceData = getResourceData("resourceActual", "$actualBasicQty", "$actualOverTimeQty", match, group_id, xAxisData);
			if ("标准".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "实际标准" + "工时").set("stack", label + "实际工时").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "实际标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "实际标准" + "金额").set("stack", label + "实际金额").set("data", resourceData[3])
						.doc());
				legendData.add(label + "实际标准" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
			} else if ("加班".equals(showData)) {
				if (markLineData.get("data") != null && resourceData[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", resourceData[6]).append("name", label + "标记线")));

				series.add(new JQ("图表-资源图表-工时").set("name", label + "实际加班" + "工时").set("stack", label + "实际工时").set("data", resourceData[1])
						.set("markLineData", markLineData).doc());
				legendData.add(label + "实际加班" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "实际加班" + "金额").set("stack", label + "实际金额").set("data", resourceData[4])
						.doc());
				legendData.add(label + "实际加班" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData,
						overTimeActualAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else if ("汇总".equals(showData)) {
				series.add(new JQ("图表-资源图表-工时").set("name", label + "实际" + "工时").set("stack", label + "实际工时").set("data", resourceData[2])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "实际" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "实际" + "金额").set("stack", label + "实际金额").set("data", resourceData[5])
						.doc());
				legendData.add(label + "实际" + "金额");

				// 计算累计值
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);

				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData,
						overTimeActualAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			} else {
				if (markLineData.get("data") != null && resourceData[6] != null)
					markLineData.put("data", Arrays.asList(new Document("yAxis", resourceData[6]).append("name", label + "标记线")));

				series.add(new JQ("图表-资源图表-工时").set("name", label + "实际标准" + "工时").set("stack", label + "实际工时").set("data", resourceData[0])
						.set("markLineData", new Document()).doc());
				legendData.add(label + "实际标准" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "实际标准" + "金额").set("stack", label + "实际金额").set("data", resourceData[3])
						.doc());
				legendData.add(label + "实际标准" + "金额");

				series.add(new JQ("图表-资源图表-工时").set("name", label + "实际加班" + "工时").set("stack", label + "实际工时").set("data", resourceData[1])
						.set("markLineData", markLineData).doc());
				legendData.add(label + "实际加班" + "工时");
				series.add(new JQ("图表-资源图表-金额").set("name", label + "实际加班" + "金额").set("stack", label + "实际金额").set("data", resourceData[4])
						.doc());
				legendData.add(label + "实际加班" + "金额");
				// 计算累计值
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, basicActualAggWorkTimeData,
						basicActualAggAmountData, (List<Double>) resourceData[0], (List<Double>) resourceData[3]);
				appendAggregateData(aggregateType, totalActualAggWorkTimeData, totalActualAggAmountData, overTimeActualAggWorkTimeData,
						overTimeActualAggAmountData, (List<Double>) resourceData[1], (List<Double>) resourceData[4]);
			}

		}

	}

	/**
	 * 根据累计值类型，计算累计值
	 * 
	 * @param aggregateType
	 * @param totalAggWorkTimeData
	 * @param totalAggAmountData
	 * @param aggWorkTimeData
	 * @param aggAmountData
	 * @param resourceWorkTimeData
	 * @param resourceAmountData
	 */
	private void appendAggregateData(String aggregateType, List<Double> totalAggWorkTimeData, List<Double> totalAggAmountData,
			List<Double> aggWorkTimeData, List<Double> aggAmountData, List<Double> resourceWorkTimeData, List<Double> resourceAmountData) {
		if ("总计".equals(aggregateType)) {
			addAggData(totalAggWorkTimeData, totalAggAmountData, resourceWorkTimeData, resourceAmountData);
		} else if ("标准加班".equals(aggregateType)) {
			addAggData(aggWorkTimeData, aggAmountData, resourceWorkTimeData, resourceAmountData);
		}
	}

	/**
	 * 计算工时和金额的累计值
	 * 
	 * @param aggWorkTimeDatas
	 * @param aggAmountDatas
	 * @param resourceWorkTimeData
	 * @param resourceAmountData
	 */
	private void addAggData(List<Double> aggWorkTimeDatas, List<Double> aggAmountDatas, List<Double> resourceWorkTimeData,
			List<Double> resourceAmountData) {
		Double wd = null;
		Double ad = null;
		if (aggWorkTimeDatas.size() > 0) {
			for (int i = 0; i < aggWorkTimeDatas.size(); i++) {
				Double d = resourceWorkTimeData.get(i);
				if (d != null && wd != null)
					wd += d;
				else if (d != null && wd == null)
					wd = d;

				Double aggWorkTimeData = aggWorkTimeDatas.get(i);
				if (aggWorkTimeData != null && wd != null)
					aggWorkTimeData += wd;
				else if (aggWorkTimeData == null && wd != null)
					aggWorkTimeData = wd;

				aggWorkTimeDatas.set(i, aggWorkTimeData);

				d = resourceAmountData.get(i);
				if (d != null && ad != null)
					ad += d;
				else if (d != null && ad == null)
					ad = d;

				Double aggAmountData = aggAmountDatas.get(i);
				if (aggAmountData != null && ad != null)
					aggAmountData += ad;
				else if (aggAmountData == null && ad != null)
					aggAmountData = ad;

				aggAmountDatas.set(i, aggAmountData);
			}
		} else {
			for (Double d : resourceWorkTimeData) {
				if (d != null && wd != null)
					wd += d;
				else if (d != null && wd == null)
					wd = d;
				aggWorkTimeDatas.add(wd);
			}
			for (Double d : resourceAmountData) {
				if (d != null && ad != null)
					ad += d;
				else if (d != null && ad == null)
					ad = d;
				aggAmountDatas.add(ad);
			}
		}
	}

	/**
	 * 查询资源图表数据，并根据xAxisData构建返回值
	 * 
	 * @param collectionName
	 * @param basicQtyName
	 * @param overTimeQtyName
	 * @param match
	 * @param group_id
	 * @param xAxisData
	 * @return 元素1：标准数量，元素2：加班数量，元素3：数量合计，元素4：标准金额，元素5：加班金额，元素6：金额合计，元素7：标准工时.
	 */
	private Object[] getResourceData(String collectionName, String basicQtyName, String overTimeQtyName, Document match, Document group_id,
			List<String> xAxisData) {
		HashMap<String, Double> basicQty = new HashMap<String, Double>();
		HashMap<String, Double> overTimeQty = new HashMap<String, Double>();
		HashMap<String, Double> totalQty = new HashMap<String, Double>();
		HashMap<String, Double> basicAmount = new HashMap<String, Double>();
		HashMap<String, Double> overTimeAmount = new HashMap<String, Double>();
		HashMap<String, Double> totalAmount = new HashMap<String, Double>();
		List<Double> works = new ArrayList<Double>();
		// 查询获取数据
		c(collectionName).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
				.set("match", match).set("group_id", group_id).array()).forEach((Document doc) -> {
					basicQty.put(doc.getString("_id"), doc.getDouble("basicQty"));
					overTimeQty.put(doc.getString("_id"), doc.getDouble("overTimeQty"));
					totalQty.put(doc.getString("_id"), doc.getDouble("totalQty"));
					basicAmount.put(doc.getString("_id"), doc.getDouble("basicAmount"));
					overTimeAmount.put(doc.getString("_id"), doc.getDouble("overTimeAmount"));
					totalAmount.put(doc.getString("_id"), doc.getDouble("totalAmount"));
					works.add(doc.getDouble("basicWorks"));
				});
		List<Double> basicQtys = new ArrayList<Double>();
		List<Double> overTimeQtys = new ArrayList<Double>();
		List<Double> totalQtys = new ArrayList<Double>();
		List<Double> basicAmounts = new ArrayList<Double>();
		List<Double> overTimeAmounts = new ArrayList<Double>();
		List<Double> totalAmounts = new ArrayList<Double>();

		// 根据xAxisData构建返回值
		for (String xAxis : xAxisData) {
			basicQtys.add(basicQty.get(xAxis));
			overTimeQtys.add(overTimeQty.get(xAxis));
			totalQtys.add(totalQty.get(xAxis));

			basicAmounts.add(basicAmount.get(xAxis) == null ? null : (basicAmount.get(xAxis) / 10000));
			overTimeAmounts.add(overTimeAmount.get(xAxis) == null ? null : (overTimeAmount.get(xAxis) / 10000));
			totalAmounts.add(totalAmount.get(xAxis) == null ? null : (totalAmount.get(xAxis) / 10000));
		}

		return new Object[] { basicQtys, overTimeQtys, totalQtys, basicAmounts, overTimeAmounts, totalAmounts,
				Check.isAssigned(works) ? works.get(0) : null };
	}

	/**
	 * 根据传入的查询的数据，获取其从属其的资源id
	 * 
	 * @param doc
	 * @return
	 */
	private List<ObjectId> addChildResource(Document doc) {
		List<ObjectId> result = new ArrayList<>();
		String type = doc.getString("type");
		if (ResourceType.class.getName().equals(type)) {
			ObjectId resType_id = doc.getObjectId("_id");
			Object org_id = ((Document) doc.get("meta")).getObjectId("org_id");
			listEQResource(org_id, resType_id, d -> d.getObjectId("_id"), result);
			listHRResource(org_id, resType_id, d -> d.getObjectId("_id"), result);
		} else if (Organization.class.getName().equals(type)) {
			List<Bson> pipe = new ArrayList<>();
			pipe.add(Aggregates.match(new Document("_id", doc.get("_id"))));
			pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "organization").set("startWith", "$_id").set("connectFromField", "_id")
					.set("connectToField", "parent_id").array());
			pipe.addAll(new JQ("追加-组织下资源").array());

			debugPipeline(pipe);
			c("organization").aggregate(pipe).map(d -> d.getObjectId("_id")).into(result);
		} else if (User.class.getName().equals(type) || Equipment.class.getName().equals(type)) {
			result.add(doc.getObjectId("_id"));
		}
		doc.put("childResourceIds", result);
		return result;
	}

	@Override
	public List<Catalog> listResEPSRoot() {
		return c("eps").find(new Document("parent_id", null)).map(CatalogMapper::eps).into(new ArrayList<>());
	}

}
