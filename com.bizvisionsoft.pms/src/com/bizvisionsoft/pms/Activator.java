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
		// String id = ModelLoader.getLatestVersionEditorIdOfType("/pms/tmt/锥形簧文档编辑器");
		// TODO 根据加载的编辑器最后修改时间，停用formDef
		// ModelLoader.streamAssembly(Assembly.TYPE_EDITOR);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ModelLoader.removeModelEventListener(this::handleModelEvent);
	}

	private void handleModelEvent(ModelEvent event) {
		if (Arrays.asList(ModelEvent.UPGRADED, ModelEvent.MODIFIED, ModelEvent.DELETED).contains(event.code)) {
			// TODO 停用formDef
		}
	}
}
