package com.bizvisionsoft.pms.cbs.assembly;

import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.serviceconsumer.Services;

public class CostSubject extends CBSSubjectGrid {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;


	@Init
	public void init() {
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(br);
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	protected Double getAmount(CBSSubject u) {
		return u.getCost();
	}

	@Override
	protected CBSSubject getUpsertedCBSSubject(CBSSubject subject) {
		return Services.get(CBSService.class).upsertCBSSubjectCost(subject,br.getDomain());
	}

	@Override
	protected void setAmount(CBSSubject subject, double amount) {
		subject.setCost(amount);
	}

	@Override
	protected String getEditbindingAction() {
		return "±à¼­";
	}
}
