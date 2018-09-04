package com.bizvisionsoft.jz.workpackage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceconsumer.Services;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.BasicDBObject;

public class AddERPPlanACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		String catagory = tv.getCatagory();
		if ("采购".equals(catagory)) {
			InputDialog id = new InputDialog(bruiService.getCurrentShell(), "采购计划工作令", "请填写采购计划的工作令", null, t -> {
				return t.trim().isEmpty() ? "请填写采购计划的工作令" : null;
			});
			if (InputDialog.OK == id.open()) {
				Services.get(WorkService.class).updateWork(new FilterAndUpdate()
						.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id", tv.get_id()))
						.set(new BasicDBObject("workPackageSetting.$.parameter",
								new HashMap<String, Object>().put("trackWorkOrder", id.getValue())))
						.bson());

				new RefreshACT().execute(context);
			}
		} else if ("生产".equals(catagory)) {
			MultiInputDialog mid = new MultiInputDialog(bruiService.getCurrentShell(), "选取生产计划", "请填写生产计划的工作令",
					"请填写生产跟踪物料号", (a, b) -> {
						if (a.trim().isEmpty())
							return "请填写采购计划的工作令";

						if (b.trim().isEmpty())
							return "请填写生产跟踪物料号";

						return null;
					});
			if (InputDialog.OK == mid.open()) {
				String[] values = mid.getValues();
				Map<String, Object> set = new LinkedTreeMap<String, Object>();
				set.put("trackWorkOrder", values[0]);
				set.put("trackMaterielId", values[1]);

				Services.get(WorkService.class).updateWork(new FilterAndUpdate()
						.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id", tv.get_id()))
						.set(new BasicDBObject("workPackageSetting.$.parameter", set)).bson());

				new RefreshACT().execute(context);
			}
		}

		// 更新计划及执行情况
	}

	private class MultiInputDialog extends Dialog {
		/**
		 * The title of the dialog.
		 */
		private String title;

		/**
		 * The message to display, or <code>null</code> if none.
		 */
		private String message;

		/**
		 * The message to display, or <code>null</code> if none.
		 */
		private String message1;

		/**
		 * The input validator, or <code>null</code> if none.
		 */
		private BiFunction<String, String, String> validator;

		/**
		 * Ok button widget.
		 */
		private Button okButton;

		/**
		 * Input text widget.
		 */
		private Text text;

		/**
		 * Input text widget.
		 */
		private Text text1;

		/**
		 * Error message label widget.
		 */
		private Text errorMessageText;

		/**
		 * Error message string.
		 */
		private String errorMessage;

		private int textStyle = SWT.SINGLE | SWT.BORDER;

		private boolean multiline;

		/**
		 * Creates an input dialog with OK and Cancel buttons. Note that the dialog will
		 * have no visual representation (no widgets) until it is told to open.
		 * <p>
		 * Note that the <code>open</code> method blocks for input dialogs.
		 * </p>
		 * 
		 * @param parentShell
		 *            the parent shell, or <code>null</code> to create a top-level shell
		 * @param dialogTitle
		 *            the dialog title, or <code>null</code> if none
		 * @param dialogMessage
		 *            the dialog message, or <code>null</code> if none
		 * @param initialValue
		 *            the initial input value, or <code>null</code> if none (equivalent
		 *            to the empty string)
		 * @param validator
		 *            an input validator, or <code>null</code> if none
		 */
		public MultiInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String dialogMessage1,
				BiFunction<String, String, String> validator) {
			super(parentShell);
			this.title = dialogTitle;
			message = dialogMessage;
			message1 = dialogMessage1;
			this.validator = validator;
		}

		/*
		 * (non-Javadoc) Method declared on Dialog.
		 */
		protected void buttonPressed(int buttonId) {
			super.buttonPressed(buttonId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (title != null) {
				shell.setText(title);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.
		 * widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.get().CANCEL_LABEL, false)
					.setData(RWT.CUSTOM_VARIANT, "warning");
			// do this here because setting the text will set enablement on the ok
			// button
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.get().OK_LABEL, true);
			okButton.setData(RWT.CUSTOM_VARIANT, "normal");
			text.setFocus();
			validateInput();
		}

		/*
		 * (non-Javadoc) Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite
			Composite composite = (Composite) super.createDialogArea(parent);
			// create message
			if (message != null) {
				Label label = new Label(composite, SWT.WRAP);
				label.setText(message);
				GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
				data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
				label.setLayoutData(data);
				label.setFont(parent.getFont());
			}
			text = new Text(composite, getInputTextStyle());
			GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			if (multiline) {
				layoutData.heightHint = 160;
			}
			text.setLayoutData(layoutData);
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});

			if (message1 != null) {
				Label label = new Label(composite, SWT.WRAP);
				label.setText(message1);
				GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
				data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
				label.setLayoutData(data);
				label.setFont(parent.getFont());
			}
			text1 = new Text(composite, getInputTextStyle());
			layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			if (multiline) {
				layoutData.heightHint = 160;
			}
			text1.setLayoutData(layoutData);
			text1.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
			errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
			errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
			errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			// Set the error message text
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
			setErrorMessage(errorMessage);

			applyDialogFont(composite);
			return composite;
		}

		public String[] getValues() {
			return new String[] { text.getText(), text1.getText() };
		}

		/**
		 * Validates the input.
		 * <p>
		 * The default implementation of this framework method delegates the request to
		 * the supplied input validator object; if it finds the input invalid, the error
		 * message is displayed in the dialog's message line. This hook method is called
		 * whenever the text changes in the input field.
		 * </p>
		 */
		protected void validateInput() {
			String errorMessage = null;
			if (validator != null) {
				errorMessage = validator.apply(text.getText(), text1.getText());
			}
			// Bug 16256: important not to treat "" (blank error) the same as null
			// (no error)
			setErrorMessage(errorMessage);
		}

		/**
		 * Sets or clears the error message. If not <code>null</code>, the OK button is
		 * disabled.
		 * 
		 * @param errorMessage
		 *            the error message, or <code>null</code> to clear
		 */
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			if (errorMessageText != null && !errorMessageText.isDisposed()) {
				errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
				// Disable the error message text control if there is no error, or
				// no error text (empty or whitespace only). Hide it also to avoid
				// color change.
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
				boolean hasError = errorMessage != null
						&& (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
				errorMessageText.setEnabled(hasError);
				errorMessageText.setVisible(hasError);
				errorMessageText.getParent().update();
				// Access the ok button by id, in case clients have overridden button creation.
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
				Control button = getButton(IDialogConstants.OK_ID);
				if (button != null) {
					button.setEnabled(errorMessage == null);
				}
			}
		}

		/**
		 * Returns the style bits that should be used for the input text field. Defaults
		 * to a single line entry. Subclasses may override.
		 * 
		 * @return the integer style bits that should be used when creating the input
		 *         text
		 * 
		 * @since 1.1
		 */
		protected int getInputTextStyle() {
			return textStyle;
		}

	}
}
