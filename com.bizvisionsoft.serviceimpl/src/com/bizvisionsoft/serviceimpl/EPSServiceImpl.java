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
		// �����û���¼���EPS�ڵ�
		if (c(EPS.class).count(new Document("parent_id", _id)) > 0) {
			throw new ServiceException("������ɾ�����¼��ڵ��EPS��¼");
		}
		// �����û���¼�����Ŀ���ڵ�
		if (c(ProjectSet.class).count(new Document("eps_id", _id)) > 0) {
			throw new ServiceException("������ɾ�����¼��ڵ��EPS��¼");
		}

		// �����û���¼�����Ŀ�ڵ�
		if (c(Project.class).count(new Document("eps_id", _id)) > 0) {
			throw new ServiceException("������ɾ�����¼��ڵ��EPS��¼");
		}

		// TODO ��������û�нڵ�ͬ��Ҳ��Ҫ�����Ƿ����������ݣ����磬��Ч�ȵȣ�
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
		// ������¼���Ŀ�����ɱ�ɾ��
		if (c(ProjectSet.class).count(new Document("parent_id", _id)) > 0)
			throw new ServiceException("������ɾ�����¼���Ŀ������Ŀ����¼");

		// �������Ŀ�����˸���Ŀ��������ɾ��
		if (c(Project.class).count(new Document("projectSet_id", _id)) > 0)
			throw new ServiceException("������ɾ�����¼���Ŀ����Ŀ����¼");

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
		List<? extends Bson> pipeline = new JQ("��ѯͶ�ʷ���-Porject").set("match", new Document("eps_id", _id)).array();
		c("project", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			result.add(epsInfo.setType(EPSInfo.TYPE_PROJECT));
		});
		pipeline = new JQ("��ѯͶ�ʷ���-Porject").set("match", new Document("projectSet_id", _id)).array();
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
		List<? extends Bson> pipeline = new JQ("��ѯͶ�ʷ���-Ͷ�ʻر����·���").array();

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		Document pieDoc = new Document();
		pieDoc.append("type", "pie");
		pieDoc.append("center", Arrays.asList("83%", "53%"));
		pieDoc.append("radius", "28%");
		pieDoc.append("label", new Document("normal", new Document("formatter", "{b|{b}��{c}��Ԫ} {per|{d}%}").append(
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
			profitDoc.append("stack", "�ر�");
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
		profitDoc.append("name", "�������� �ϼ�");
		profitDoc.append("type", "bar");
		profitDoc.append("stack", "�ر�");
		profitDoc.append("label", new Document("normal", new Document("show", true).append("position", "insideBottom")
				.append("textStyle", new Document("color", "#000"))));
		profitDoc.append("itemStyle", new Document("normal", new Document("color", "rgba(128, 128, 128, 0)")));
		List<String> profitData = new ArrayList<String>();
		for (int i = 1; i < 13; i++) {
			profitData.add(getStringValue(totalProfits.get(year + String.format("%02d", i))));
		}

		profitDoc.append("data", profitData);

		Document option = new Document();
		option.append("title", new Document("text", year + "�� ���������������Ԫ��").append("x", "center"));
//		option.append("tooltip", new Document("trigger", "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", data1).append("orient", "vertical").append("left", "right"));
		option.append("grid",
				new Document("left", "3%").append("right", "35%").append("bottom", "6%").append("containLabel", true));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1��", " 2��", " 3��", " 4��", " 5��", " 6��", " 7��", " 8��", " 9��", "10��", "11��", "12��"))));
		option.append("yAxis", Arrays.asList(new Document("type", "value")));

		option.append("series", data2);
		return option;
	}

	@Override
	public Document getMonthCostIA(String year) {
		List<? extends Bson> pipeline = new JQ("��ѯͶ�ʷ���-Ͷ�ʻر����·���").array();

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		Document pieDoc = new Document();
		pieDoc.append("type", "pie");
		pieDoc.append("center", Arrays.asList("83%", "53%"));
		pieDoc.append("radius", "28%");
		pieDoc.append("label", new Document("normal", new Document("formatter", "{b|{b}��{c}��Ԫ} {per|{d}%}").append(
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
			costDoc.append("stack", "Ͷ��");
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
		costDoc.append("name", "�ʽ�Ͷ�� �ϼ�");
		costDoc.append("type", "bar");
		costDoc.append("stack", "Ͷ��");
		costDoc.append("label", new Document("normal", new Document("show", true).append("position", "insideBottom")
				.append("textStyle", new Document("color", "#000"))));
		costDoc.append("itemStyle", new Document("normal", new Document("color", "rgba(128, 128, 128, 0)")));
		List<String> costData = new ArrayList<String>();
		for (int i = 1; i < 13; i++) {
			costData.add(getStringValue(totalCosts.get(year + String.format("%02d", i))));
		}

		costDoc.append("data", costData);

		Document option = new Document();
		option.append("title", new Document("text", year + "�� �ʽ�Ͷ���������Ԫ��").append("x", "center"));
//		option.append("tooltip", new Document("trigger", "axis").append("axisPointer", new Document("type", "shadow")));

		option.append("legend", new Document("data", data1).append("orient", "vertical").append("left", "right"));
		option.append("grid",
				new Document("left", "3%").append("right", "35%").append("bottom", "6%").append("containLabel", true));

		option.append("xAxis", Arrays.asList(new Document("type", "category").append("data",
				Arrays.asList(" 1��", " 2��", " 3��", " 4��", " 5��", " 6��", " 7��", " 8��", " 9��", "10��", "11��", "12��"))));
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
