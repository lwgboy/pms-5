package com.bizvisionsoft.jz.project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.jz.Distribution;
import com.bizvisionsoft.jz.PLMObject;
import com.bizvisionsoft.pms.work.assembly.WorkPackagePlan;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class Refresh {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		String catagory = tv.getCatagory();
		try {
			WorkPackagePlan wpp = (WorkPackagePlan) context.getContent();
			if ("采购".equals(catagory)) {
				String trackWorkOrder = tv.getTrackWorkOrder();
				List<PLMObject> erpPurchases = new Distribution().getERPPurchase(Arrays.asList(trackWorkOrder));
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
				String trackMaterielId = tv.getTrackMaterielId();
				String trackWorkOrder = tv.getTrackWorkOrder();
				Map<String, String> productions = new HashMap<String, String>();
				productions.put(trackWorkOrder, trackMaterielId);
				List<PLMObject> erpProduction = new Distribution().getERPProduction(productions);
				List<WorkPackage> workPackages = new ArrayList<WorkPackage>();

				erpProduction.forEach(plmObject -> {
					WorkPackage wp = WorkPackage.newInstance(work, tv);

					wp.matId = (String) ((PLMObject) plmObject).getValue("MATNR");
					wp.matDesc = (String) ((PLMObject) plmObject).getValue("MAKTX");
					wp.planQty = (double) ((PLMObject) plmObject).getValue("BDMNG");
					wp.completeQty = (double) ((PLMObject) plmObject).getValue("ENMNG");

					// ((PLMObject) plmObject).getValue("ZNUM");
					// ((PLMObject) plmObject).getValue("WERKS");
					// ((PLMObject) plmObject).getValue("WEMPF");
					// ((PLMObject) plmObject).getValue("ABLAD");
					// ((PLMObject) plmObject).getValue("AUFNR");
					// ((PLMObject) plmObject).getValue("KTEXT");
					// ((PLMObject) plmObject).getValue("ZNUM1");
					// ((PLMObject) plmObject).getValue("SORTF");
					// ((PLMObject) plmObject).getValue("TXT_FEVOR");
					// ((PLMObject) plmObject).getValue("DSNAM");
					// ((PLMObject) plmObject).getValue("ZSTAT");
					workPackages.add(wp);
				});
				wpp.updateProduction(workPackages);
			} else if ("研发".equals(catagory)) {
				List<WorkPackage> workPackages = Services.get(WorkService.class)
						.listWorkPackage(new Query().filter(new BasicDBObject("work_id", work.get_id())
								.append("catagory", catagory).append("name", tv.getName())).bson());
				List<String> objectIds = new ArrayList<String>();
				workPackages.forEach(wp -> {
					objectIds.add(wp.id + "|" + wp.verNo);
				});
				List<PLMObject> plmObjectInfo = new Distribution().getPLMObjectInfo(objectIds);

				List<PLMObject> plmObjectProcesss = new Distribution().getPLMObjectProcess(objectIds);

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
