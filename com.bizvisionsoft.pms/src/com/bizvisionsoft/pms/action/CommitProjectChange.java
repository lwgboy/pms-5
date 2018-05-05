package com.bizvisionsoft.pms.action;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class CommitProjectChange {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute() {
		Shell currentShell = bruiService.getCurrentShell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(currentShell);
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Counting from one to 20...", 20);
					for (int i = 1; !monitor.isCanceled() && i <= 20; i++) {
						monitor.worked(1);
						Thread.sleep(100);
					}
					monitor.done();
				}
			});
		} catch (Exception e) {
			MessageDialog.openError(currentShell, "Error", e.getMessage());
		}
	}

}
