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
	public Document getMonthInvestmentAnalysis(String year) {
		List<? extends Bson> pipeline = new JQ("��ѯͶ�ʷ���-Ͷ�ʻر����·���").array();

		List<String> data1 = new ArrayList<String>();
		List<Document> data2 = new ArrayList<Document>();
		c("eps", EPSInfo.class).aggregate(pipeline).forEach((EPSInfo epsInfo) -> {
			String name = epsInfo.getName();
			data1.add(name);
			Document costDoc = new Document();
			data2.add(costDoc);
			costDoc.append("name", name);
			costDoc.append("type", "bar");
			costDoc.append("stack", "Ͷ��");
			costDoc.append("label", new Document("normal", new Document("show", true).append("position", "inside")));
			Document profitDoc = new Document();
			data2.add(profitDoc);
			profitDoc.append("name", name);
			profitDoc.append("type", "bar");
			profitDoc.append("stack", "�ر�");
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
		return getBarChart(year + "�� ����EPSͶ�ʻر���������Ԫ��", data1, data2);
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
