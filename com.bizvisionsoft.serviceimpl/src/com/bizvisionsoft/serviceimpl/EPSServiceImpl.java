package com.bizvisionsoft.serviceimpl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.EPSInfo;
import com.bizvisionsoft.service.model.EPSInvestmentAnalysis;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;

public class EPSServiceImpl extends BasicServiceImpl implements EPSService {

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, EPS.class);
	}

	@Override
	public EPS insert(EPS eps) {
		return insert(eps, EPS.class);
	}

	@Override
	public EPS get(ObjectId _id) {
		return get(_id, EPS.class);
	}

	@Override
	public List<EPS> getRootEPS() {
		return getSubEPS(null);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, EPS.class);
	}

	@Override
	public long delete(ObjectId _id) {
		// 检查有没有下级的EPS节点
		if (c(EPS.class).count(new Document("parent_id", _id)) > 0) {
			throw new ServiceException("不允许删除有下级节点的EPS记录");
		}
		// 检查有没有下级的项目集节点
		if (c(ProjectSet.class).count(new Document("eps_id", _id)) > 0) {
			throw new ServiceException("不允许删除有下级节点的EPS记录");
		}

		// 检查有没有下级的项目节点
		if (c(Project.class).count(new Document("eps_id", _id)) > 0) {
			throw new ServiceException("不允许删除有下级节点的EPS记录");
		}

		// TODO 即便下面没有节点同样也需要考虑是否有其他数据（比如，绩效等等）
		return delete(_id, EPS.class);
	}

	@Override
	public List<EPS> getSubEPS(ObjectId parent_id) {
		ArrayList<EPS> result = new ArrayList<EPS>();
		c(EPS.class).find(new Document("parent_id", parent_id)).sort(new Document("id", 1)).into(result);
		return result;
	}

	@Override
	public long countSubEPS(ObjectId _id) {
		return c(EPS.class).count(new Document("parent_id", _id));
	}

	@Override
	public long deleteProjectSet(ObjectId _id) {
		// 如果有下级项目集不可被删除
		if (c(ProjectSet.class).count(new Document("parent_id", _id)) > 0)
			throw new ServiceException("不允许删除有下级项目集的项目集记录");

		// 如果有项目引用了该项目集，不可删除
		if (c(Project.class).count(new Document("projectSet_id", _id)) > 0)
			throw new ServiceException("不允许删除有下级项目的项目集记录");

		return delete(_id, ProjectSet.class);
	}

	@Override
	public List<EPSInfo> listRootEPSInfo() {
		List<EPSInfo> result = new ArrayList<EPSInfo>();
		c("eps", EPSInfo.class).find(new Document("parent_id", null)).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_EPS));
		});
		return result;
	}

	@Override
	public long countRootEPSInfo() {
		return c("eps").count(new Document("parent_id", null));
	}

	@Override
	public List<EPSInfo> listSubEPSInfo(ObjectId _id) {
		List<EPSInfo> result = new ArrayList<EPSInfo>();
		c("eps", EPSInfo.class).find(new Document("parent_id", _id)).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_EPS));
		});
		c("projectSet", EPSInfo.class).find(new Document("eps_id", _id)).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_PROJECTSET));
		});
		c("projectSet", EPSInfo.class).find(new Document("parent_id", _id)).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_PROJECTSET));
		});
		List<? extends Bson> pipeline = new JQ("查询投资分析-Porject").set("match", new Document("eps_id", _id)).array();
		c("project", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_PROJECT));
		});
		pipeline = new JQ("查询投资分析-Porject").set("match", new Document("projectSet_id", _id)).array();
		c("project", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_PROJECT));
		});
		return result;
	}

	@Override
	public long countSubEPSInfo(ObjectId _id) {
		long count = c("eps").count(new Document("parent_id", _id));
		count += c("projectSet").count(new Document("eps_id", _id));
		count += c("projectSet").count(new Document("parent_id", _id));
		count += c("project").count(new Document("eps_id", _id));
		count += c("project").count(new Document("projectSet_id", _id));
		return count;
	}

	@Override
	public Document getMonthProfitIA(List<EPSInvestmentAnalysis> epsIAs, String year) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Integer.parseInt(year), 0, 1, 0, 0, 0);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Integer.parseInt(year), 11, 1, 0, 0, 0);
		return getProfitIA(epsIAs, year + "年 销售利润分析（万元）", cal1.getTime(), cal2.getTime());
	}

	@Override
	public Document getMonthCostIA(List<EPSInvestmentAnalysis> epsIAs, String year) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Integer.parseInt(year), 0, 1, 0, 0, 0);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Integer.parseInt(year), 11, 1, 0, 0, 0);
		return getCostIA(epsIAs, year + "年 资金投入分析（万元）", cal1.getTime(), cal2.getTime());
	}

	private Document getProfitIA(List<EPSInvestmentAnalysis> epsIAs, String title, Date startDate, Date endDate) {
		List<String> xAxis = createXAxis(null, startDate, endDate);
		List<Document> series = new ArrayList<Document>();
		createProfitSeries(series, epsIAs, startDate, endDate);
		List<String> legend = getLegend(epsIAs);

		Document option = new Document();
		option.append("title", new Document("text", title).append("x", "center"));
		option.append("grid",
				new Document("left", "3%").append("right", "5%").append("bottom", "6%").append("containLabel", true));

		if (legend != null && legend.size() > 0)
			option.append("legend", new Document("data", legend).append("bottom", 10).append("left", "center"));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data", xAxis)));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", series);

		return option;
	}

	private Document getCostIA(List<EPSInvestmentAnalysis> epsIAs, String title, Date startDate, Date endDate) {
		List<String> xAxis = createXAxis(null, startDate, endDate);
		List<Document> series = new ArrayList<Document>();
		createCostSeries(series, epsIAs, startDate, endDate);
		List<String> legend = getLegend(epsIAs);

		Document option = new Document();
		option.append("title", new Document("text", title).append("x", "center"));
		option.append("grid",
				new Document("left", "3%").append("right", "5%").append("bottom", "6%").append("containLabel", true));

		if (legend != null && legend.size() > 0)
			option.append("legend", new Document("data", legend).append("bottom", 10).append("left", "center"));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data", xAxis)));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", series);

		return option;
	}

	private List<String> getLegend(List<EPSInvestmentAnalysis> epsIAs) {
		if (epsIAs != null && epsIAs.size() > 0) {
			List<String> legend = new ArrayList<String>();
			epsIAs.forEach(epsIA -> {
				legend.add(epsIA.name);
			});
			return legend;
		}
		return null;
	}

	private List<String> createXAxis(List<String> xAxis, Date startDate, Date endDate) {
		if (xAxis == null)
			xAxis = new ArrayList<String>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M");
		xAxis.add(sdf.format(startDate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		while (cal.getTime().before(endDate)) {
			cal.add(Calendar.MONTH, 1);
			xAxis.add(sdf.format(cal.getTime()));
		}
		return xAxis;
	}

	@SuppressWarnings("unchecked")
	private void createCostSeries(List<Document> series, List<EPSInvestmentAnalysis> epsIAs, Date startDate,
			Date endDate) {
		Map<String, Double> mapKeys = new TreeMap<String, Double>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		mapKeys.put(sdf.format(startDate), 0d);

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		while (cal.getTime().before(endDate)) {
			cal.add(Calendar.MONTH, 1);
			mapKeys.put(sdf.format(cal.getTime()), 0d);
		}
		if (epsIAs != null && epsIAs.size() > 0) {
			epsIAs.forEach(epsIA -> {
				Map<String, Double> map = new TreeMap<String, Double>(mapKeys);
				AggregateIterable<Document> aggregate;
				if (epsIA.project_ids != null) {
					aggregate = c("project").aggregate(new JQ("查询投资分析-Porject")
							.set("match", new Document("_id", new Document("$in", epsIA.project_ids))).array());

					aggregate.forEach((Document doc) -> {
						Object obj = doc.get("cbsSubjects");
						if (obj != null && obj instanceof List) {
							((List<Document>) obj).forEach(cbsSubject -> {
								Double d = map.get(cbsSubject.getString("id"));
								if (d != null) {
									Object cost = cbsSubject.get("cost");
									if (cost != null) {
										d += ((Number) cost).doubleValue();
										map.put(cbsSubject.getString("id"), d);
									}
								}
							});
						}
					});

					Document costDoc = new Document();
					series.add(costDoc);

					costDoc.append("name", epsIA.name);
					costDoc.append("type", "bar");
					costDoc.append("label",
							new Document("normal", new Document("show", true).append("position", "inside")));
					List<Object> data = new ArrayList<Object>();
					for (Double d : map.values()) {
						data.add(getStringValue(d));
					}
					costDoc.append("data", data);
				}
			});
		} else {
			Map<String, Double> map = new TreeMap<String, Double>(mapKeys);
			c("project").aggregate(new JQ("查询投资分析-Porject").set("match", new Document()).array())
					.forEach((Document doc) -> {
						Object obj = doc.get("cbsSubjects");
						if (obj != null && obj instanceof List) {
							((List<Document>) obj).forEach(cbsSubject -> {
								Double d = map.get(cbsSubject.getString("id"));
								if (d != null) {
									Object cost = cbsSubject.get("cost");
									if (cost != null) {
										d += ((Number) cost).doubleValue();
										map.put(cbsSubject.getString("id"), d);
									}
								}
							});
						}
					});

			Document costDoc = new Document();
			series.add(costDoc);

			costDoc.append("name", "资金投入");
			costDoc.append("type", "bar");
			costDoc.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
			List<Object> data = new ArrayList<Object>();
			for (Double d : map.values()) {
				data.add(getStringValue(d));
			}
			costDoc.append("data", data);
		}
	}

	@SuppressWarnings("unchecked")
	private void createProfitSeries(List<Document> series, List<EPSInvestmentAnalysis> epsIAs, Date startDate,
			Date endDate) {
		Map<String, Double> mapKeys = new TreeMap<String, Double>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		mapKeys.put(sdf.format(startDate), 0d);

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		while (cal.getTime().before(endDate)) {
			cal.add(Calendar.MONTH, 1);
			mapKeys.put(sdf.format(cal.getTime()), 0d);
		}
		if (epsIAs != null && epsIAs.size() > 0) {
			epsIAs.forEach(epsIA -> {
				Map<String, Double> map = new TreeMap<String, Double>(mapKeys);
				AggregateIterable<Document> aggregate;
				if (epsIA.project_ids != null) {
					aggregate = c("project").aggregate(new JQ("查询投资分析-Porject")
							.set("match", new Document("_id", new Document("$in", epsIA.project_ids))).array());

					aggregate.forEach((Document doc) -> {
						Object obj = doc.get("salesItems");
						if (obj != null && obj instanceof List) {
							((List<Document>) obj).forEach(salesItem -> {
								Double d = map.get(salesItem.getString("period"));
								if (d != null) {
									Object profit = salesItem.get("profit");
									if (profit != null) {
										d += ((Number) profit).doubleValue();
										map.put(salesItem.getString("period"), d);
									}
								}
							});
						}
					});

					Document profitDoc = new Document();
					series.add(profitDoc);

					profitDoc.append("name", epsIA.name);
					profitDoc.append("type", "bar");
					profitDoc.append("label",
							new Document("normal", new Document("show", true).append("position", "inside")));
					List<Object> data = new ArrayList<Object>();
					for (Double d : map.values()) {
						data.add(getStringValue(d));
					}
					profitDoc.append("data", data);
				}
			});
		} else {
			Map<String, Double> map = new TreeMap<String, Double>(mapKeys);
			c("project").aggregate(new JQ("查询投资分析-Porject").set("match", new Document()).array())
					.forEach((Document doc) -> {
						Object obj = doc.get("salesItems");
						if (obj != null && obj instanceof List) {
							((List<Document>) obj).forEach(salesItem -> {
								Double d = map.get(salesItem.getString("period"));
								if (d != null) {
									Object profit = salesItem.get("profit");
									if (profit != null) {
										d += ((Number) profit).doubleValue();
										map.put(salesItem.getString("period"), d);
									}
								}
							});
						}
					});

			Document profitDoc = new Document();
			series.add(profitDoc);

			profitDoc.append("name", "销售利润");
			profitDoc.append("type", "bar");
			profitDoc.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
			List<Object> data = new ArrayList<Object>();
			for (Double d : map.values()) {
				data.add(getStringValue(d));
			}
			profitDoc.append("data", data);

		}
	}

	private String getStringValue(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			if (d != 0d) {
				return new DecimalFormat("0.0").format(d);
			}
		}
		return "";
	}

	@Override
	public List<ObjectId> getSubProjectId(ObjectId _id) {
		List<ObjectId> epsIds = getDesentItems(Arrays.asList(_id), "eps", "parent_id");

		List<ObjectId> projectIds = c("project")
				.distinct("_id", new Document("eps_id", new Document("$in", epsIds)), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		List<ObjectId> projectSetIds = c("projectSet")
				.distinct("_id", new Document("eps_id", new Document("$in", epsIds)), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		if (projectSetIds.size() > 0) {
			projectSetIds = getDesentItems(projectSetIds, "projectSet", "parent_id");

			projectIds.addAll(c("project")
					.distinct("_id", new Document("projectSet_id", new Document("$in", projectSetIds)), ObjectId.class)
					.into(new ArrayList<ObjectId>()));
		}
		projectIds = getDesentItems(projectIds, "project", "parentProject_id");
		return projectIds;
	}

}
