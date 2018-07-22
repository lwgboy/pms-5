package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class ProjectSetServiceImpl extends BasicServiceImpl implements ProjectSetService {

	@Override
	public ProjectSet insert(ProjectSet projectSet) {
		projectSet.setWorkOrder(generateWorkOrder(projectSet));
		return insert(projectSet, ProjectSet.class);
	}

	@Override
	public ProjectSet get(ObjectId _id) {
		return get(_id, ProjectSet.class);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, ProjectSet.class);
	}

	@Override
	public List<ProjectSet> createDataSet(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		return query(skip, limit, filter);
	}

	private List<ProjectSet> query(Integer skip, Integer limit, BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// TODO 补充pipeline

		List<ProjectSet> result = new ArrayList<ProjectSet>();
		c(ProjectSet.class).aggregate(pipeline).into(result);
		return result;

	}

	@Override
	@Deprecated
	public long delete(ObjectId _id) {
		// 如果有下级项目集不可被删除
		if (c(ProjectSet.class).count(new BasicDBObject("parent_id", _id)) > 0)
			throw new ServiceException("不允许删除有下级项目集的项目集记录");

		// 如果有项目引用了该项目集，不可删除
		if (c(Project.class).count(new BasicDBObject("projectSet_id", _id)) > 0)
			throw new ServiceException("不允许删除有下级项目的项目集记录");

		return delete(_id, ProjectSet.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ProjectSet.class);
	}

	@Override
	public List<ProjectSet> listFinishProjectSet(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		List<Bson> pipeline = new ArrayList<Bson>();
		if (filter != null)
			pipeline.add(new BasicDBObject("$match", filter));

		pipeline.addAll(Arrays.asList(
				new BasicDBObject("$lookup", new BasicDBObject("from", "project")
						.append("let", new BasicDBObject("projectSet_id", "$_id"))
						.append("pipeline", Arrays.asList(
								new BasicDBObject("$match",
										new BasicDBObject("$expr", new BasicDBObject("$and", Arrays.asList(
												new BasicDBObject("$eq",
														Arrays.asList("$projectSet_id", "$$projectSet_id")),
												new BasicDBObject("$eq",
														Arrays.asList(
																new BasicDBObject("$not",
																		Arrays.asList(new BasicDBObject("$eq",
																				Arrays.asList("$status",
																						ProjectStatus.Closed)))),
																false)))))),
								new BasicDBObject("$group",
										new BasicDBObject("_id", null).append("count",
												new BasicDBObject("$sum", 1.0)))))
						.append("as", "project")),
				new BasicDBObject("$unwind",
						new BasicDBObject("path", "$project").append("preserveNullAndEmptyArrays", true)),
				new BasicDBObject("$addFields", new BasicDBObject("projectCount", "$project.count")),
				new BasicDBObject("$match", new BasicDBObject("projectCount", new BasicDBObject("$gt", 0.0))),
				new BasicDBObject("$project", new BasicDBObject("project", false))));

		if (skip != null)
			pipeline.add(new BasicDBObject("$skip", skip));

		if (limit != null)
			pipeline.add(new BasicDBObject("$limit", limit));

		return c(ProjectSet.class).aggregate(pipeline).into(new ArrayList<ProjectSet>());
	}

	@Override
	public String generateWorkOrder(ProjectSet projectSet) {
		/**
		 * TODO 需要根据九洲定制
		 * 
		 * KG×-××-××-××××
		 * 
		 * 作号采用四级编码。
		 *
		 * 第1位至第2位为类型码：KG代表科研类项目；YG代表预研类项目；CG代表CBB项目。
		 *
		 * 第3位为承研部门：1代表识别事业部；2代表探测事业部；3代表空管公司；5代表通信与对抗事业部；6代表工程部；7代表预研部；8代表共性部。
		 *
		 * 第4位至第9位为立项顺序号，分为两部分：
		 * 1.第4位至第6位按流水号排序，每年从“-01”开始往后编排，如项目为子项目，则第4位至第6位为父项目立项顺序号；
		 * 2.第7位至第9位为子项目流水号，从项目中获取的子项目流水号，如不是子项目，则第7位至第9位为空。
		 * 
		 * 第10位至第14位为立项年份。
		 **/
		String catalog = projectSet.getCatalog();
		ObjectId impunit_id = projectSet.getImpUnit_id();

		String workOrder;
		if ("预研".equals(catalog)) {
			workOrder = "YG";
		} else if ("CBB".equals(catalog)) {
			workOrder = "CG";
		} else {
			workOrder = "KG";
		}
		int year = Calendar.getInstance().get(Calendar.YEAR);

		String orgNo = c("organization").distinct("id", new Document("_id", impunit_id), String.class).first();
		workOrder += orgNo;

		int index = generateCode(Generator.DEFAULT_NAME, "projectno" + year);
		workOrder += "-" + String.format("%02d", index);

		workOrder += "-" + String.format("%04d", year);

		return workOrder;
	}

}
