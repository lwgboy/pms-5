package com.awesometech.leoco.action;

import org.bson.Document;
import org.eclipse.jface.dialogs.InputDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceconsumer.Services;
import com.bizvisionsoft.sqldb.SqlQuery;
import com.mongodb.BasicDBObject;

public class SelectSO {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		String catagory = tv.getCatagory();
		if ("�ɹ�".equals(catagory)) {
			InputDialog id = new InputDialog(br.getCurrentShell(), "����׷������", "����������������ƷSO���", null,
					t -> t.trim().isEmpty() ? "����Ϊ��" : null);
			if (InputDialog.OK == id.open()) {
				//////////////////////////////////////////
				// ��ѯSO
				String so_num = id.getValue();
				Document doc = new SqlQuery("ecology").sql("select * from V_PMS_SO where so_num='" + so_num + "'").first();
				if (doc != null) {
					String prt_desc = doc.get("PRT_DESC").toString();
					String prt_num = doc.get("PRT_NUM").toString();
					boolean ok = br.confirm("����׷������",
							"SO��" + so_num + "���������²�Ʒ��<br>" + prt_desc + " [" + prt_num + "]<br>��ȷ�ϡ�");
					if (ok) {
						Services.get(WorkService.class)
								.updateWork(new FilterAndUpdate()
										.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id",
												tv.get_id()))
										.set(new BasicDBObject("workPackageSetting.$.parameter",
												new BasicDBObject("so_num", so_num)))
										.bson());
						//////////////////////////////////////////////////
						// ˢ�±��
						tv.setParameter("so_num", so_num);
						GridPart grid = (GridPart) context.getChildContextByAssemblyName("������-�ɹ�").getContent();
						grid.setViewerInput();
					}
				} else {
					Layer.message("�޷���ȡ���" + so_num + "��Ӧ��Ʒ��������������롣", Layer.ICON_CANCEL);
				}
			}
		}
	}

}
