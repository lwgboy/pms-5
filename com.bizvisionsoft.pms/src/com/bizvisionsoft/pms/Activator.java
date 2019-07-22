package com.bizvisionsoft.pms;

import java.util.Arrays;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.bizvisionsoft.bruicommons.ModelEvent;
import com.bizvisionsoft.bruicommons.ModelLoader;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		ModelLoader.addModelEventListener(this::handleModelEvent);
		// String id = ModelLoader.getLatestVersionEditorIdOfType("/pms/tmt/×¶ÐÎ»ÉÎÄµµ±à¼­Æ÷");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ModelLoader.removeModelEventListener(this::handleModelEvent);
	}

	private void handleModelEvent(ModelEvent event) {
		if (Arrays.asList(ModelEvent.UPGRADED, ModelEvent.MODIFIED, ModelEvent.DELETED).contains(event.code)) {
			// TODO Í£ÓÃformDef
		}
	}

}
