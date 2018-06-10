package com.bizvisionsoft.serviceimpl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
	public Document getMonthInvestmentAnalysis(String year) {
		List<? extends Bson> pipeline = new JQ("查询投资分析-投资回报按月分析").array();

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		c("eps", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			String name = epsInfo.getName();
			data1.add(name);
			Document costDoc = new Document();
			data2.add(costDoc);
			costDoc.append("name", name);
			costDoc.append("type", "bar");
			costDoc.append("stack", "投资");
			costDoc.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
			Document profitDoc = new Document();
			data2.add(profitDoc);
			profitDoc.append("name", name);
			profitDoc.append("type", "bar");
			profitDoc.append("stack", "回报");
			profitDoc.append("itemStyle", new Document("opacity", 0.5));
			profitDoc.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
			List<Object> cost = new ArrayList<Object>();
			List<Object> profit = new ArrayList<Object>();
			costDoc.append("data", cost);
			profitDoc.append("data", profit);
			for (int i = 0; i < 12; i++) {
				String month = String.format("%02d", i + 1);
				cost.add(getDoubleValue(epsInfo.getCost(year + month)));
				profit.add(getDoubleValue(epsInfo.getProfit(year + month)));
			}

		});
		return getBarChart(year + "年 各月EPS投资回报分析（万元）", data1, data2);
	}

	private String getDoubleValue(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();
			if (d != 0d) {
				return new DecimalFormat("#.0").format(d);
			}
		}
		return null;
	}

}
