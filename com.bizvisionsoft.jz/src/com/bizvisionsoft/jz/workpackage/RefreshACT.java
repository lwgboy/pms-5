package com.bizvisionsoft.jz.workpackage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.jz.Distribution;
import com.bizvisionsoft.jz.PLMObject;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class RefreshACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		String catagory = tv.getCatagory();
		try {
			WorkPackagePlanASM wpp = (WorkPackagePlanASM) context.getContent();
			Distribution distribution = new Distribution();
			if ("采购".equals(catagory)) {
				List<PLMObject> erpPurchases = distribution
						.getERPPurchase(Arrays.asList((String) tv.getParameter("trackWorkOrder")));
				List<WorkPackage> workPackages = new ArrayList<WorkPackage>();

				erpPurchases.forEach(plmObject -> {
					WorkPackage wp = WorkPackage.newInstance(work, tv);
					wp.matId = (String) ((PLMObject) plmObject).getValue("MATNR");
					wp.matDesc = (String) ((PLMObject) plmObject).getValue("MAKTX");
					wp.planQty = (double) ((PLMObject) plmObject).getValue("BDMNG");
					wp.completeQty = (double) ((PLMObject) plmObject).getValue("ZMENG");
					wp.unit = (String) ((PLMObject) plmObject).getValue("MEINS");
					// ((PLMObject) plmObject).getValue("WERKS");
					// ((PLMObject) plmObject).getValue("EBELN");
					// ((PLMObject) plmObject).getValue("EBELP");
					// ((PLMObject) plmObject).getValue("WEMPF");
					// ((PLMObject) plmObject).getValue("AUFNR");
					// ((PLMObject) plmObject).getValue("ERDAT");
					// ((PLMObject) plmObject).getValue("DISPO");
					// ((PLMObject) plmObject).getValue("ABLAD");
					// ((PLMObject) plmObject).getValue("ZMENG2");
					// ((PLMObject) plmObject).getValue("ZMENG3");
					workPackages.add(wp);
				});
				wpp.updatePurchase(workPackages);

			} else if ("生产".equals(catagory)) {
				Map<String, String> productions = new HashMap<String, String>();
				productions.put((String) tv.getParameter("trackWorkOrder"),
						(String) tv.getParameter("trackMaterielId"));
				List<PLMObject> erpProduction = distribution.getERPProduction(productions);

				List<PLMObject> mes = distribution.getMES(productions);
				List<WorkPackage> workPackages = new ArrayList<WorkPackage>();

				List<WorkPackageProgress> workPackageProgresss = new ArrayList<WorkPackageProgress>();

				erpProduction.forEach(plmObject -> {
					WorkPackage erp = WorkPackage.newInstance(work, tv);

					ObjectId _id = new ObjectId();
					erp.set_id(_id);
					erp.matId = (String) ((PLMObject) plmObject).getValue("MATNR");
					erp.matDesc = (String) ((PLMObject) plmObject).getValue("MAKTX");
					erp.planQty = (double) ((PLMObject) plmObject).getValue("BDMNG");
					erp.completeQty = (double) ((PLMObject) plmObject).getValue("ENMNG");
					String workOrder = (String) ((PLMObject) plmObject).getValue("ABLAD");

					// ((PLMObject) plmObject).getValue("ZNUM");
					// ((PLMObject) plmObject).getValue("WERKS");
					// ((PLMObject) plmObject).getValue("WEMPF");
					// ((PLMObject) plmObject).getValue("AUFNR");
					// ((PLMObject) plmObject).getValue("KTEXT");
					// ((PLMObject) plmObject).getValue("ZNUM1");
					// ((PLMObject) plmObject).getValue("SORTF");
					// ((PLMObject) plmObject).getValue("TXT_FEVOR");
					// ((PLMObject) plmObject).getValue("DSNAM");
					// ((PLMObject) plmObject).getValue("ZSTAT");
					workPackages.add(erp);

					mes.forEach(mesObject -> {
						if (workOrder.equals(mesObject.getValue("id"))
								&& erp.matId.equals(mesObject.getValue("materialNo"))) {
							erp.unit = (String) mesObject.getValue("unit");
							WorkPackageProgress wp = new WorkPackageProgress();
							wp.updateTime = new Date();
							wp.description = (String) mesObject.getValue("status");
							wp.qty = (double) mesObject.getValue("qty");
							workPackageProgresss.add(wp);
						}
					});
				});
				wpp.updateProduction(workPackages, workPackageProgresss);
			} else if ("研发".equals(catagory)) {
				List<WorkPackage> workPackages = Services.get(WorkService.class)
						.listWorkPackage(new Query().filter(new BasicDBObject("work_id", work.get_id())
								.append("catagory", catagory).append("name", tv.getName())).bson());
				List<String> objectIds = new ArrayList<String>();
				workPackages.forEach(wp -> {
					objectIds.add(wp.id + "|" + wp.verNo);
				});
				List<PLMObject> plmObjectInfo = distribution.getPLMObjectInfo(objectIds);

				List<PLMObject> plmObjectProcesss = distribution.getPLMObjectProcess(objectIds);

				workPackages.clear();

				plmObjectInfo.forEach(plmObject -> {
					workPackages.forEach(wp -> {
						if (wp.id.equals((String) plmObject.getValue("id"))
								&& wp.verNo.equals((String) plmObject.getValue("majorVerNo"))) {
							wp.description = (String) plmObject.getValue("name");
							wp.planStatus = true;
							wp.documentType = (String) plmObject.getValue("type");
							wp.completeStatus = plmObject.getValue("status") != null;
							// ((PLMObject) plmObject).getValue("security");
							// ((PLMObject) plmObject).getValue("stage");
							// ((PLMObject) plmObject).getValue("createdBy");
							// ((PLMObject) plmObject).getValue("createDate");
						}
					});
				});

				List<WorkPackageProgress> workPackageProgresss = new ArrayList<WorkPackageProgress>();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				plmObjectProcesss.forEach(plmObjectProcess -> {
					workPackages.forEach(workPackage -> {
						String processName = (String) plmObjectProcess.getValue("processName");
						if (processName.startsWith(workPackage.id + "/" + workPackage.verNo)) {
							WorkPackageProgress wp = new WorkPackageProgress();
							wp.setPackage_id(workPackage.get_id());
							try {
								wp.updateTime = df.parse((String) plmObjectProcess.getValue("endDate"));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							wp.charger = (String) plmObjectProcess.getValue("charger");
							wp.completeStatus = (String) plmObjectProcess.getValue("name");
							wp.description = (String) plmObjectProcess.getValue("remark");
							// plmObjectProcess.getValue("objectNo");
							// plmObjectProcess.getValue("processName");
							// plmObjectProcess.getValue("processId");
							// plmObjectProcess.getValue("startDate");
							// plmObjectProcess.getValue("id");
							workPackageProgresss.add(wp);
						}
					});
				});
				wpp.updatePLM(workPackages, workPackageProgresss);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
