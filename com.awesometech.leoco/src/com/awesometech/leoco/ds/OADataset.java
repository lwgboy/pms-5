package com.awesometech.leoco.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.bizvisionsoft.sqldb.SqlQuery;
import com.mongodb.BasicDBObject;

public class OADataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet("������-OA����/" +DataSet.LIST)
	private List<Document> list() {
		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView) data[1];
		List instList = (List) view.getParameter("WF_INSTS");
		List<Document> result = new ArrayList<>();
		if (instList == null) {
			return result;
		}

		///////////////////////////////////////////////////////
		String sql = buildSql(instList);
		new SqlQuery("oa").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}
	
	@DataSet("������-OA����/" +DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id, @MethodParam(MethodParam.OBJECT) Object selected) {
		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView) data[1];
		List instList = (List) view.getParameter("WF_INSTS");
		
		Document row = (Document)selected;
		if(null != row && null != row.getString("INST_ID") && null != instList && instList.contains(row.getString("INST_ID"))) {
			instList.remove(row.getString("INST_ID"));
			Services.get(WorkService.class)
				.updateWork(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id",
							view.get_id()))
					.set(new BasicDBObject("workPackageSetting.$.parameter",
							new BasicDBObject("WF_INSTS", instList)))
					.bson());
			//////////////////////////////////////////////////
			// ˢ�±��
			//	tv.setParameter("so_num", so_num);
			GridPart grid = (GridPart) context.getChildContextByAssemblyName("������-OA����").getContent();
			grid.setViewerInput();
		}
		return 0l;
	}
	

	private String buildSql(List instList) {
		StringBuffer sb = new StringBuffer();
		sb.append("select inst.id as inst_id,wf.type_name,wf.wf_name,inst.inst_name,inst.status,inst.create_date,inst.creater from  wf wf,wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
		sb.append(" and inst.id in ('" + StringUtils.join(instList.toArray(), "','") + "')");
		return sb.toString();
	}
	
	/***
	 * ��ע��
	 * �˷���֧�ֵĽ������Ϊ��ѯ���̽��й�����������Ϊ���ҵ�ָ����ĳ�����̣�������ʱδ����ҳ�����Կ���ֻ���ָ�����������ݡ�
	 * ���⿪��ʱ��ʹ�õ�derby���ݿ�ķ�ҳ�﷨��sql server��һ�£���Ҫ���ǿ����Ļ���������������������
	 * 
	 * @param condition
	 * @return
	 */
	@DataSet("OA����ѡ���б�/" +DataSet.LIST)
	private List<Document> selectList(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		List<Document> result = new ArrayList<>();
		if(null == condition.get("filter")) {
			return result;
		}
		String sql = buildSelectSql(condition);
		new SqlQuery("oa").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}

	@DataSet("OA����ѡ���б�/" + DataSet.COUNT)
	public long countOASelectList(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		
		return 100L;
	}
	
	private String buildSelectSql( BasicDBObject condition) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("select  inst.id as inst_id,wf.type_name,wf.wf_name,inst.inst_name,inst.status,inst.create_date,inst.creater from  wf wf,wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
		if(null != condition && null != condition.get("filter")) {
			BasicDBObject filter = (BasicDBObject)condition.get("filter");
			sb.append(filterToSQL(filter));
		}
		return sb.toString();
	}

	private String filterToSQL(BasicDBObject filter) {
		StringBuffer sb = new StringBuffer();
		filter.entrySet().forEach(d->{
			String fieldName = d.getKey();
			if(fieldName.equals("INST_ID")) {
				fieldName = "inst.id";
			}
			if(d.getValue().getClass().getSimpleName().equals("Pattern")) {
				Pattern pattern = (Pattern)d.getValue();
				String value = pattern.pattern();
				if(value.startsWith("^")) {
					value = value.substring(1);
					sb.append(" and " + fieldName + " like '" + value + "%' ");
				}else if(value.endsWith("$")) {
					value = value.substring(0, value.length() - 1);
					sb.append(" and " + fieldName + " like '%" + value + "' ");
				}else {
					sb.append(" and " + fieldName + " like '%" + value + "%' ");
				}
			}else if(d.getValue().getClass().getSimpleName().equals("BasicDBObject")) {
				BasicDBObject dbo =  (BasicDBObject)d.getValue();
				Entry<String, Object> entry = dbo.entrySet().iterator().next();
				String type = (String) entry.getKey();
				if(type.equals("$eq")) {
					String value = (String) entry.getValue();
					sb.append(" and " + fieldName + " = '" + value + "' ");
				}else if(type.equals("$not")) {
					//�˴�value����ΪPattern�����ǵ�ʵ��ʹ��������ܸܺ���Ҳ���ܼܺ򵥣���ʱʹ��not in
					String value = entry.getValue().toString();
					sb.append(" and " + fieldName + " not in ('" + value + "') ");
				}else if(type.equals("$ne")) {
					String value = (String) entry.getValue();
					sb.append(" and " + fieldName + " <> '" + value + "' ");
				}
			}else {
				String value = d.getValue().toString();
				sb.append(" and " + fieldName + " = '" + value + "' ");
			}
		});
		return sb.toString();
	}

}
