package com.bizvisionsoft.pms;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		// TODO ע��༭���޸ĸ���
		// ModelLoader.addAssemblyCreateListener(lis);
		// ModelLoader.addAssemblyDeleteListener(lis);
		// ModelLoader.addAssemblyModifiyListener(lis);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO ȡ���༭������
		// ModelLoader.removeAssemblyCreateListener(lis);
		// ModelLoader.removeAssemblyDeleteListener(lis);
		// ModelLoader.removeAssemblyModifiyListener(lis);

	}

}
