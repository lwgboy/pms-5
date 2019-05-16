package com.awesometech.leoco.ds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import com.bizvisionsoft.serviceconsumer.Services;
import com.bizvisionsoft.sqldb.SqlQuery;
import com.mongodb.BasicDBObject;

public class OADataset {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	@DataSet("工作包-OA流程/" +DataSet.LIST)
	private List<Document> list() {
		Object[] data = (Object[]) context.getParentContext().getInput();
		TrackView view = (TrackView) data[1];
		@SuppressWarnings("unchecked")
		List<String> instList = (List<String>) view.getParameter("WF_INSTS");
		List<Document> result = new ArrayList<>();
		if (instList == null) {
			return result;
		}

		///////////////////////////////////////////////////////
		String sql = buildSql(instList);
		new SqlQuery("ecology").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}
	
	@DataSet("工作包-OA流程/" +DataSet.DELETE)
	private long delete(@MethodParam(MethodParam._ID) ObjectId _id, @MethodParam(MethodParam.OBJECT) Object selected) {
		Object[] data = (Object[]) context.getParentContext().getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) data[0];
		TrackView view = (TrackView) data[1];
		@SuppressWarnings("unchecked")
		List<String> instList = (List<String>) view.getParameter("WF_INSTS");
		
		Document row = (Document)selected;
		if(null != row && null != row.get("INST_ID").toString() && null != instList && instList.contains(row.get("INST_ID").toString())) {
			instList.remove(row.get("INST_ID").toString());
			Services.get(WorkService.class)
				.updateWork(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id",
							view.get_id()))
					.set(new BasicDBObject("workPackageSetting.$.parameter",
							new BasicDBObject("WF_INSTS", instList)))
					.bson(), br.getDomain());
			//////////////////////////////////////////////////
			// 刷新表格
			view.setParameter("WF_INSTS", instList);
			GridPart grid = (GridPart) context.getChildContextByAssemblyName("工作包-OA流程").getContent();
			grid.setViewerInput();
		}
		return 0l;
	}
	

	private String buildSql(List<String> instList) {
		StringBuffer sb = new StringBuffer();
		sb.append("select inst.id as inst_id,wf.type_name,wf.wf_name,inst.inst_name,inst.status,inst.create_date,inst.creater from  V_PMS_wf wf,V_PMS_wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
		sb.append(" and inst.id in ('" + StringUtils.join(instList.toArray(), "','") + "')");
		return sb.toString();
	}
	
	/***
	 * 备注：
	 * 此方法支持的界面操作为查询流程进行关联，核心是为了找到指定的某个流程，所以暂时未做分页，可以考虑只查出指定数量的数据。
	 * 另外开发时候使用的derby数据库的分页语法和sql server不一致，需要考虑开发的环境和生产环境差异问题
	 * 
	 * @param condition
	 * @return
	 */
	@DataSet("OA流程选择列表/" +DataSet.LIST)
	private List<Document> selectList(@MethodParam(MethodParam.CONDITION) BasicDBObject condition) {
		List<Document> result = new ArrayList<>();
		if(null == condition.get("filter")) {
			return result;
		}
		String sql = buildSelectSql(condition);
		new SqlQuery("ecology").sql(sql).forEach(d -> {
			result.add(d);
		});

		return result;
	}

	@DataSet("OA流程选择列表/" + DataSet.COUNT)
	public long countOASelectList(@MethodParam(MethodParam.FILTER) BasicDBObject filter) {
		String sql = countSelectSql(filter);
		long count = Long.valueOf((int) new SqlQuery("ecology").sql(sql).first().get(""));
		return count;
	}
	
	private String buildSelectSql( BasicDBObject condition) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("select * from (select *, ROW_NUMBER() OVER(Order by tb.create_date desc ) AS RowId from(");
		sb.append("select  inst.id as inst_id,wf.type_name,wf.wf_name,inst.inst_name,inst.status,inst.create_date,inst.creater from  V_PMS_wf wf,V_PMS_wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
		if(null != condition && null != condition.get("filter")) {
			BasicDBObject filter = (BasicDBObject)condition.get("filter");
			sb.append(filterToSQL(filter));
		}
		sb.append(") as tb) as tb2  where RowId between " + (condition.getInt("skip")+1) + " and "  + (condition.getInt("skip") + condition.getInt("limit")));
		return sb.toString();
	}
	
	private String countSelectSql(BasicDBObject condition) {
		StringBuffer sb = new StringBuffer();
		sb.append("select count(*) from  V_PMS_wf wf,V_PMS_wf_inst inst " );
		sb.append(" where wf.id = inst.wf_id ");
		if(null != condition) {
			sb.append(filterToSQL(condition));
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
				Iterator<Entry<String, Object>> it = dbo.entrySet().iterator();
				while(it.hasNext()) {
					Entry<String, Object> entry = it.next();
					String type = (String) entry.getKey();
					if(type.equals("$eq")) {
						String value = (String) entry.getValue();
						sb.append(" and " + fieldName + " = '" + value + "' ");
					}else if(type.equals("$not")) {
						//此处value对象为Pattern，考虑到实际使用情况可能很复杂也可能很简单，暂时使用not in
						String value = entry.getValue().toString();
						sb.append(" and " + fieldName + " not in ('" + value + "') ");
					}else if(type.equals("$ne")) {
						String value = (String) entry.getValue();
						sb.append(" and " + fieldName + " <> '" + value + "' ");
					}
					if(fieldName.equals("CREATE_DATE")) {
						Date date = (Date) entry.getValue();
						SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
						String dateStr = sf.format(date);
						if(type.equals("$gte")) {
							sb.append(" and " + fieldName + " >= '" + dateStr + "' ");
						}else if(type.equals("$lte")) {
							sb.append(" and " + fieldName + " <= '" + dateStr + "' ");
						}
					}
				}
				
			}else {
				String value = d.getValue().toString();
				sb.append(" and " + fieldName + " = '" + value + "' ");
			}
		});
		return sb.toString();
	}

}
