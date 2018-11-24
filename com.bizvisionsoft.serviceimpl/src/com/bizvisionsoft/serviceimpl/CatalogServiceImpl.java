package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.CatalogRenderer;
import com.mongodb.client.MongoIterable;
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
			c("organization").find(new Document("parent_id", parent._id)).map(CatalogMapper::org).into(result);
			listResourceType(parent._id, "user", result);
			listResourceType(parent._id, "equipment", result);
		} else if (typeEquals(parent, ResourceType.class)) {
			Object _id = parent.meta.get("org_id");
			if (_id != null) {
				listOrgResource(_id, parent._id, result);
			}
			_id = parent.meta.get("project_id");
			if (_id != null) {
				listProjectResource(_id, parent._id, result);
			}
			_id = parent.meta.get("stage_id");
			if (_id != null) {
				listStageResource(_id, parent._id, result);
			}
		} else if (typeEquals(parent, EPS.class)) {
			c("eps").find(new Document("parent_id", parent._id)).map(CatalogMapper::eps).into(result);
			c("project").find(new Document("eps_id", parent._id)).map(CatalogMapper::project).into(result);
		} else if (typeEquals(parent, Project.class)) {
			if (parent.meta.getBoolean("stageEnable", false)) {
				c("work").find(new Document("project_id", parent._id).append("stage", true)).map(CatalogMapper::work).into(result);
			} else {// 项目下的资源类型
				List<Bson> pipe = new ArrayList<>();
				pipe.add(Aggregates.match(new Document("project_id", parent._id)));
				pipe.addAll(new JQ("查询-工作-资源类别").array());
				c("work").aggregate(pipe).map(CatalogMapper::resourceType).map(c -> {
					c.meta.append("project_id", parent._id);// 补充项目Id
					return c;
				}).into(result);
			}
		} else if (typeEquals(parent, Work.class)) {
			List<Bson> pipe = new ArrayList<>();
			pipe.add(Aggregates.match(new Document("_id", parent._id)));
			pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "work").set("startWith", "$_id").set("connectFromField", "_id")
					.set("connectToField", "parent_id").array());
			pipe.addAll(new JQ("查询-工作-资源类别").array());
			c("work").aggregate(pipe).map(CatalogMapper::resourceType).map(c -> {
				c.meta.append("stage_id", parent._id);// 补充阶段Id
				return c;
			}).into(result);
		}
		return result;
	}

	private void listStageResource(Object stage_id, ObjectId type_id, List<Catalog> result) {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(Aggregates.match(new Document("_id", stage_id)));
		pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "work").set("startWith", "$_id").set("connectFromField", "_id")
				.set("connectToField", "parent_id").array());
		MongoIterable<Catalog> iter = iterateWorkResource(stage_id, type_id, pipe).map(d -> {
			d.meta.append("stage_id", stage_id);
			return d;
		});
		debugPipeline(pipe);
		iter.into(result);
	}

	private void listProjectResource(Object project_id, ObjectId type_id, List<Catalog> result) {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(Aggregates.match(new Document("project_id", project_id)));
		iterateWorkResource(project_id, type_id, pipe).map(d -> {
			d.meta.append("project_id", project_id);
			return d;
		}).into(result);
	}

	private MongoIterable<Catalog> iterateWorkResource(Object project_id, ObjectId type_id, List<Bson> pipe) {
		pipe.addAll(new JQ("查询-工作-资源类别-资源").set("resTypeId", type_id).array());
		pipe.addAll(new JQ("追加-资源id-Lookup资源").array());
		return c("work").aggregate(pipe).map(d -> {
			Document doc = (Document) d.get("hr");
			if (doc != null) {
				return CatalogMapper.user(doc);
			}
			doc = (Document) d.get("eq");
			if (doc != null) {
				return CatalogMapper.equipment(doc);
			}
			doc = (Document) d.get("ty");
			if (doc != null) {
				return CatalogMapper.typedResource(doc);
			}
			return new Catalog();
		});
	}

	private void listOrgResource(Object org_id, ObjectId resourceType_id, List<Catalog> result) {
		Document condition = new Document("org_id", org_id).append("resourceType_id", resourceType_id);
		c("user").find(condition).map(CatalogMapper::user).into(result);
		c("equipment").find(condition).map(CatalogMapper::equipment).into(result);
	}

	@Override
	public long countResOrgStructure(Catalog parent) {
		// 如果parent是组织，获取下级组织和资源类型
		long count = 0;
		if (typeEquals(parent, Organization.class)) {
			count += c("organization").countDocuments(new Document("parent_id", parent._id));
			count += countResourceType(parent._id, "user");
			count += countResourceType(parent._id, "equipment");
		} else if (typeEquals(parent, ResourceType.class)) {
			Object _id = parent.meta.get("org_id");
			if (_id != null) {// 查询组织下类型的资源
				Document cond = new Document("org_id", _id).append("resourceType_id", parent._id);
				count += c("user").countDocuments(cond);
				count += c("equipment").countDocuments(cond);
			} else {
				_id = parent.meta.get("project_id");
				if (_id != null) {// 查询项目下类型的资源
					List<Bson> pipe = new ArrayList<>();
					pipe.add(Aggregates.match(new Document("project_id", _id)));
					pipe.addAll(new JQ("查询-工作-资源类别-资源").set("resTypeId", parent._id).array());
					pipe.add(Aggregates.count());
					count += Optional.ofNullable(c("work").aggregate(pipe).first()).map(f -> ((Number) f.get("count")).intValue())
							.orElse(0);
				} else {
					_id = parent.meta.get("stage_id");// 查询阶段下类型的资源
					if (_id != null) {
						List<Bson> pipe = new ArrayList<>();
						pipe.add(Aggregates.match(new Document("_id", _id)));
						pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "work").set("startWith", "$_id").set("connectFromField", "_id")
								.set("connectToField", "parent_id").array());
						pipe.addAll(new JQ("查询-工作-资源类别-资源").set("resTypeId", parent._id).array());
						pipe.add(Aggregates.count());
						count += Optional.ofNullable(c("work").aggregate(pipe).first()).map(f -> ((Number) f.get("count")).intValue())
								.orElse(0);
					}
				}
			}
		} else if (typeEquals(parent, EPS.class)) {
			count += c("eps").countDocuments(new Document("parent_id", parent._id));
			count += c("project").countDocuments(new Document("eps_id", parent._id));
		} else if (typeEquals(parent, Project.class)) {
			if (parent.meta.getBoolean("stageEnable", false)) {
				count += c("work").countDocuments(new Document("project_id", parent._id).append("stage", true));
			} else {
				List<Bson> pipe = new ArrayList<>();
				pipe.add(Aggregates.match(new Document("project_id", parent._id)));
				pipe.addAll(new JQ("查询-工作-资源类别").array());
				pipe.add(Aggregates.count());
				count += Optional.ofNullable(c("work").aggregate(pipe).first()).map(f -> ((Number) f.get("count")).intValue()).orElse(0);
			}
		} else if (typeEquals(parent, Work.class)) {
			List<Bson> pipe = new ArrayList<>();
			pipe.add(Aggregates.match(new Document("_id", parent._id)));
			pipe.addAll(new JQ("查询-通用-下级迭代取出-含本级").set("from", "work").set("startWith", "$_id").set("connectFromField", "_id")
					.set("connectToField", "parent_id").array());
			pipe.addAll(new JQ("查询-工作-资源类别").array());
			pipe.add(Aggregates.count());
			debugPipeline(pipe);
			count += Optional.ofNullable(c("work").aggregate(pipe).first()).map(f -> ((Number) f.get("count")).intValue()).orElse(0);
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

	private boolean typeEquals(Catalog c, Class<?> clas) {
		return clas.getName().equals(c.type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document createResChart(Document condition) {
		checkResChartOption(condition);

		// 构建Catalog的渲染器
		CatalogRenderer cr = new CatalogRenderer((Document) condition.get("option"));

		// 根据设置获取所选数据的资源数据
		List<Document> input;
		if ("并列".equals(cr.getSeriesType())) {
			// 系列类型为并列时，需要单独为每个传入的数据加载其资源
			input = ((List<Document>) condition.get("input")).stream().map((Document doc) -> {
				addChildResource(doc);
				if (cr.getDataType().contains("计划")) {
					// 获取计划资源数据
					getResourceData("plan", cr, doc);
				}
				if (cr.getDataType().contains("实际")) {
					// 获取实际资源数据
					getResourceData("actual", cr, doc);
				}
				return doc;
			}).collect(Collectors.toList());
		} else {
			// 系列类型为合并时，将所选数据的所有资源进行合并处理。
			List<ObjectId> resourceIds = new ArrayList<>();
			((List<Document>) condition.get("input")).forEach((Document doc) -> resourceIds.addAll(addChildResource(doc)));
			Document doc = new Document("childResourceIds", resourceIds).append("label", "");
			if (cr.getDataType().contains("计划")) {
				// 获取计划资源数据
				getResourceData("plan", cr, doc);
			}
			if (cr.getDataType().contains("实际")) {
				// 获取实际资源数据
				getResourceData("actual", cr, doc);
			}
			input = Arrays.asList(doc);
		}
		// 生成echart的option
		return cr.render(input);
	}

	/**
	 * 根据传入的key获取资源数据
	 * 
	 * @param keyName
	 *            包括两种类型“plan”和“actual”：“plan”时，获取资源计划，；“actual”时，获取资源用量
	 * @param cr
	 *            Catalog渲染器，需要从中获取配置中的起止时间及日期显示方式。
	 * @param doc
	 *            doc中必须包含childResourceIds字段,类型为（{@code List<ObjectId>}
	 *            ），该字段中包含从属doc的资源id集合
	 * @return 传入的 doc ,doc中将会添加key = keyName，value = Document的数据。value中包括7个参数，分别是：
	 *         <p>
	 *         basicQty：标准数量；
	 *         <p>
	 *         overTimeQty：加班数量；
	 *         <p>
	 *         totalQty：总数；
	 *         <p>
	 *         basicAmount：标准金额；
	 *         <p>
	 *         overTimeAmount：加班金额；
	 *         <p>
	 *         totalAmount：总金额；
	 *         <p>
	 *         works:每日标准工时
	 */
	private Document getResourceData(String keyName, CatalogRenderer cr, Document doc) {
		String collectionName, basicQtyName, overTimeQtyName;
		if ("plan".equals(keyName)) {
			collectionName = "resourcePlan";
			basicQtyName = "$planBasicQty";
			overTimeQtyName = "$planOverTimeQty";
		} else {
			collectionName = "resourceActual";
			basicQtyName = "$actualBasicQty";
			overTimeQtyName = "$actualOverTimeQty";
		}

		Map<String, Double> basicQty = new HashMap<String, Double>();
		Map<String, Double> overTimeQty = new HashMap<String, Double>();
		Map<String, Double> totalQty = new HashMap<String, Double>();
		Map<String, Double> basicAmount = new HashMap<String, Double>();
		Map<String, Double> overTimeAmount = new HashMap<String, Double>();
		Map<String, Double> totalAmount = new HashMap<String, Double>();
		List<Double> works = new ArrayList<Double>();
		// 查询获取数据
		c(collectionName).aggregate(new JQ("查询-资源图表").set("$basicQty", basicQtyName).set("$overTimeQty", overTimeQtyName)
				.set("match",
						new Document("$and",
								Arrays.asList(new Document("id", new Document("$gte", cr.getStart())),
										new Document("id", new Document("$lte", cr.getEnd())))).append("resource_id",
												new Document("$in", doc.get("childResourceIds"))))
				.set("group_id", new Document().append("$dateToString", new Document("format", cr.getDBDateFormat()).append("date", "$id")))
				.array()).forEach((Document d) -> {
					basicQty.put(d.getString("_id"), d.getDouble("basicQty"));
					overTimeQty.put(d.getString("_id"), d.getDouble("overTimeQty"));
					totalQty.put(d.getString("_id"), d.getDouble("totalQty"));
					basicAmount.put(d.getString("_id"), d.getDouble("basicAmount"));
					overTimeAmount.put(d.getString("_id"), d.getDouble("overTimeAmount"));
					totalAmount.put(d.getString("_id"), d.getDouble("totalAmount"));
					works.add(d.getDouble("basicWorks"));
				});
		List<Double> basicQtys = new ArrayList<Double>();
		List<Double> overTimeQtys = new ArrayList<Double>();
		List<Double> totalQtys = new ArrayList<Double>();
		List<Double> basicAmounts = new ArrayList<Double>();
		List<Double> overTimeAmounts = new ArrayList<Double>();
		List<Double> totalAmounts = new ArrayList<Double>();

		// 根据xAxisData构建返回值
		for (String xAxis : cr.getxAxisData()) {
			basicQtys.add(basicQty.get(xAxis));
			overTimeQtys.add(overTimeQty.get(xAxis));
			totalQtys.add(totalQty.get(xAxis));

			basicAmounts.add(basicAmount.get(xAxis));
			overTimeAmounts.add(overTimeAmount.get(xAxis));
			totalAmounts.add(totalAmount.get(xAxis));
		}

		doc.append(keyName,
				new Document("basicQty", basicQtys).append("overTimeQty", overTimeQtys).append("totalQty", totalQtys)
						.append("basicAmount", basicAmounts).append("overTimeAmount", overTimeAmounts).append("totalAmount", totalAmounts)
						.append("works", works.isEmpty() ? null : works.get(0)));

		return doc;
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
	 * 根据传入的查询的数据，获取其从属其的资源id
	 * 
	 * @param doc
	 * @return
	 */
	private List<ObjectId> addChildResource(Document doc) {
		List<ObjectId> result = new ArrayList<>();
		String type = doc.getString("type");
		if (ResourceType.class.getName().equals(type)) {
			Document condition = new Document("org_id", ((Document) doc.get("meta")).get("org_id")).append("resourceType_id",
					doc.get("_id"));
			c("user").find(condition).map(d -> d.getObjectId("_id")).into(result);
			c("equipment").find(condition).map(d -> d.getObjectId("_id")).into(result);
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
