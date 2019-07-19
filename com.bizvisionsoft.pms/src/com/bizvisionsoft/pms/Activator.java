package com.bizvisionsoft.pms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.pms.formdef.action.FormDefTool;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private static List<Consumer<String>> accs = new ArrayList<Consumer<String>>();
	private static List<Consumer<String>> amcs = new ArrayList<Consumer<String>>();

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		// TODO ×¢²á±à¼­Æ÷ÐÞ¸Ä¸ú×Ù
		registerCreateConsumer(FormDefTool::doCreateAssembly);
		registerModifiyConsumer(FormDefTool::doModifiyAssembly);

	}

	private void registerModifiyConsumer(Consumer<String> acm) {
		amcs.add(acm);
		ModelLoader.addAssemblyModifiyListener(acm);

	}

	private void registerCreateConsumer(Consumer<String> acc) {
		accs.add(acc);
		ModelLoader.addAssemblyCreateListener(acc);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO È¡Ïû±à¼­Æ÷¸ú×Ù
		accs.forEach(acc -> ModelLoader.removeAssemblyCreateListener(acc));

		amcs.forEach(acm -> ModelLoader.removeAssemblyModifiyListener(acm));

	}

}
