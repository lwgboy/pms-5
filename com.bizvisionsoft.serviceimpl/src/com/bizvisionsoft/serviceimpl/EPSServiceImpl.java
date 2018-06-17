package com.bizvisionsoft.serviceimpl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.EPSInfo;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;

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
	public Document getMonthProfitIA(String year) {
		List<? extends Bson> pipeline = new JQ("查询投资分析-投资回报按月分析").array();

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		Document pieDoc = new Document();
		pieDoc.append("type", "pie");
		pieDoc.append("center", Arrays.asList("83%", "53%"));
		pieDoc.append("radius", "28%");
		pieDoc.append("label", new Document("normal", new Document("formatter", "{b|{b}：{c}万元} {per|{d}%}").append(
				"rich",
				new Document("b", new Document("color", "#747474").append("lineHeight", 22).append("align", "center"))
						.append("hr",
								new Document("color", "#aaa").append("width", "100%").append("borderWidth", 0.5)
										.append("height", 0))
						.append("per", new Document("color", "#eee").append("backgroundColor", "#334455")
								.append("padding", Arrays.asList(2, 4)).append("borderRadius", 2)))));
		Map<String, Double> totalProfits = new HashMap<String, Double>();

		List<Document> pieDatas = new ArrayList<Document>();
		pieDoc.append("data", pieDatas);
		c("eps", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			String name = epsInfo.getName();
			data1.add(name);
			Document profitDoc = new Document();
			data2.add(profitDoc);
			profitDoc.append("name", name);
			profitDoc.append("type", "bar");
			profitDoc.append("stack", "回报");
			profitDoc.append("label", new Document("normal", new Document("show", false).append("position", "inside")));
			List<Object> profitData = new ArrayList<Object>();
			profitDoc.append("data", profitData);
			double pieValue = 0d;
			for (int i = 0; i < 12; i++) {
				String month = String.format("%02d", i + 1);
				double profit = epsInfo.getProfit(year + month);
				pieValue += profit;
				profitData.add(getStringValue(profit));

				Double totalProfit = totalProfits.get(year + month);
				if (totalProfit == null) {
					totalProfit = profit;
				} else {
					totalProfit += profit;
				}
				totalProfits.put(year + month, totalProfit);
			}
			Document pieData = new Document();
			pieData.append("name", name).append("value", getStringValue(pieValue));
			pieDatas.add(pieData);

		});
		data2.add(pieDoc);
		

		Document profitDoc = new Document();
		data2.add(profitDoc);
		profitDoc.append("name", "销售利润 合计");
		profitDoc.append("type", "bar");
		profitDoc.append("stack", "回报");
		profitDoc.append("label", new Document("normal", new Document("show", true).append("position", "insideBottom")
				.append("textStyle", new Document("color", "#000"))));
		profitDoc.append("itemStyle", new Document("normal", new Document("color", "rgba(128, 128, 128, 0)")));
		List<String> profitData = new ArrayList<String>();
		for (int i = 1; i < 13; i++) {
			profitData.add(getStringValue(totalProfits.get(year + String.format("%02d", i))));
		}

		profitDoc.append("data", profitData);

		Document option = new Document();
		option.append("title", new Document("text", year + "年 销售利润分析（万元）").append("x", "center"));
//		option.append("tooltip", new Document("trigger", "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", data1).append("orient", "vertical").append("left", "right"));
		option.append("grid",
				new Document("left", "3%").append("right", "35%").append("bottom", "6%").append("containLabel", true));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", data2);
		return option;
	}

	@Override
	public Document getMonthCostIA(String year) {
		List<? extends Bson> pipeline = new JQ("查询投资分析-投资回报按月分析").array();

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		Document pieDoc = new Document();
		pieDoc.append("type", "pie");
		pieDoc.append("center", Arrays.asList("83%", "53%"));
		pieDoc.append("radius", "28%");
		pieDoc.append("label", new Document("normal", new Document("formatter", "{b|{b}：{c}万元} {per|{d}%}").append(
				"rich",
				new Document("b", new Document("color", "#747474").append("lineHeight", 22).append("align", "center"))
						.append("hr",
								new Document("color", "#aaa").append("width", "100%").append("borderWidth", 0.5)
										.append("height", 0))
						.append("per", new Document("color", "#eee").append("backgroundColor", "#334455")
								.append("padding", Arrays.asList(2, 4)).append("borderRadius", 2)))));
		Map<String, Double> totalCosts = new HashMap<String, Double>();
		
		List<Document> pieDatas = new ArrayList<Document>();
		pieDoc.append("data", pieDatas);
		c("eps", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			String name = epsInfo.getName();
			data1.add(name);
			Document costDoc = new Document();
			data2.add(costDoc);
			costDoc.append("name", name);
			costDoc.append("type", "bar");
			costDoc.append("stack", "投资");
			costDoc.append("label", new Document("normal", new Document("show", false).append("position", "inside")));
			List<Object> costData = new ArrayList<Object>();
			costDoc.append("data", costData);
			double pieValue = 0d;
			for (int i = 0; i < 12; i++) {
				String month = String.format("%02d", i + 1);
				double cost = epsInfo.getCost(year + month);
				pieValue += cost;
				costData.add(getStringValue(cost));
				

				Double totalCost = totalCosts.get(year + month);
				if (totalCost == null) {
					totalCost = cost;
				} else {
					totalCost += cost;
				}
				totalCosts.put(year + month, totalCost);
			}
			Document pieData = new Document();
			pieData.append("name", name).append("value", getStringValue(pieValue));
			pieDatas.add(pieData);

		});
		data2.add(pieDoc);
		

		Document costDoc = new Document();
		data2.add(costDoc);
		costDoc.append("name", "资金投入 合计");
		costDoc.append("type", "bar");
		costDoc.append("stack", "投资");
		costDoc.append("label", new Document("normal", new Document("show", true).append("position", "insideBottom")
				.append("textStyle", new Document("color", "#000"))));
		costDoc.append("itemStyle", new Document("normal", new Document("color", "rgba(128, 128, 128, 0)")));
		List<String> costData = new ArrayList<String>();
		for (int i = 1; i < 13; i++) {
			costData.add(getStringValue(totalCosts.get(year + String.format("%02d", i))));
		}

		costDoc.append("data", costData);

		Document option = new Document();
		option.append("title", new Document("text", year + "年 资金投入分析（万元）").append("x", "center"));
//		option.append("tooltip", new Document("trigger", "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", data1).append("orient", "vertical").append("left", "right"));
		option.append("grid",
				new Document("left", "3%").append("right", "35%").append("bottom", "6%").append("containLabel", true));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1月", " 2月", " 3月", " 4月", " 5月", " 6月", " 7月", " 8月", " 9月", "10月", "11月", "12月"))));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", data2);
		return option;
	}


	private String getStringValue(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			if (d != 0d) {
				return new DecimalFormat("0.0").format(d);
			}
		}
		return null;
	}

}
