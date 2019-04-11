package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.CatalogService;
import com.bizvisionsoft.service.common.query.JQ;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ResourceType;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceimpl.renderer.BudgetNCostChartRenderer;
//github.com/sgewuhan/pms.git
import com.bizvisionsoft.serviceimpl.renderer.ResourceChartRenderer;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;

public class CatalogServiceImpl extends BasicServiceImpl implements CatalogService {

	@Override
	public Document createDefaultResOption() {
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

	@Override
	public Document createDefaultBudgetNCostOption() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.set(year, 0, 1, 0, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MINUTE, -1);
		Date end = cal.getTime();
		return new Document("dateRange", Arrays.asList(start, end)).append("dateType", "月").append("seriesType", "汇总")
				.append("dataType", new ArrayList<String>(Arrays.asList("预算", "成本"))).append("aggregate", false)
				.append("showPercentage", false);
	}

	/**
	 * 该人员所属的组织
	 */
	@Override
	public List<Catalog> listOrgRoot(String userId) {
		List<Bson> pipeline = Arrays.asList(Aggregates.match(new Document("userId", userId)),
				Aggregates.lookup("organization", "org_id", "_id", "org"), Aggregates.unwind("$org"));
		return c("user").aggregate(pipeline).map(d -> (Document) d.get("org")).map(CatalogMapper::org).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listResStructure(Catalog parent) {
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
					c.match.append("project_id", parent._id);// 补充项目Id
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
				c.match.append("stage_id", parent._id);// 补充阶段Id
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
			d.match.append("stage_id", stage_id);
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
			d.match.append("project_id", project_id);
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
			doc = (Document) d.get("tp");
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
	public long countResStructure(Catalog parent) {
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

	@Override
	public Document createResChart(Document condition) {
		return new ResourceChartRenderer(condition).render();
	}

	@Override
	public Document createBudgetNCostChart(Document condition) {
		return new BudgetNCostChartRenderer(condition).render();
	}

	@Override
	public List<Catalog> listEPSRoot() {
		return c("eps").find(new Document("parent_id", null)).map(CatalogMapper::eps).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listResProjectRoot(ObjectId _id) {
		return c("project").find(new Document("_id", _id)).map(CatalogMapper::project).into(new ArrayList<>());
	}

	@Override
	public List<Catalog> listBudgetNCostStructure(Catalog parent) {
		List<Catalog> result = new ArrayList<Catalog>();
		if (typeEquals(parent, EPS.class)) {
			c("eps").find(new Document("parent_id", parent._id)).map(CatalogMapper::eps).into(result);
			c("project").find(new Document("eps_id", parent._id)).map(CatalogMapper::project).into(result);
		} else if (typeEquals(parent, Project.class)) {
			c("accountItem").find(new Document("parentId", null)).sort(new Document("id", 1)).map(d -> d.append("project_id", parent._id))
					.map(CatalogMapper::accountItem).map(c -> {
						c.match.append("project_id", parent._id);// 补充项目Id
						return c;
					}).into(result);
		} else if (typeEquals(parent, AccountItem.class)) {
			c("accountItem").find(new Document("parentId", parent.meta.get("id"))).sort(new Document("id", 1))
					.map(d -> d.append("project_id", parent.meta.get("project_id"))).map(CatalogMapper::accountItem).map(c -> {
						c.match.append("project_id", parent.meta.get("project_id"));// 补充项目Id
						return c;
					}).into(result);
		}
		return result;
	}

	@Override
	public long countBudgetNCostStructure(Catalog parent) {
		long count = 0;
		if (typeEquals(parent, EPS.class)) {
			count += c("eps").countDocuments(new Document("parent_id", parent._id));
			count += c("project").countDocuments(new Document("eps_id", parent._id));
		} else if (typeEquals(parent, Project.class)) {
			count += c("accountItem").countDocuments(new Document("parentId", null));
		} else if (typeEquals(parent, AccountItem.class)) {
			count += c("accountItem").countDocuments(new Document("parentId", parent.meta.get("id")));
		}
		return count;
	}

}
