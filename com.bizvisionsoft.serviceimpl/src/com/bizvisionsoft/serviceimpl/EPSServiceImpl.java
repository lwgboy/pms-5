package com.bizvisionsoft.serviceimpl;

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

}
