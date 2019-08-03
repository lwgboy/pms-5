package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.BruiToolkit;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.tools.Check;

public class AddressBar extends Composite {

	public class ActionEvent extends Event {
		public IFolder[] path;
		public Action action;
	}

	private static final String ACTION_REFRESH = "refresh";
	private static final String ACTION_UP = "up";
	private IFolder[] path;
	private Controls<Composite> addressBar;
	private List<Controls<Button>> controlHolder = new ArrayList<>();

	/**
	 * 
	 * @param parent
	 * @param path
	 * @param actionGroups
	 */
	public AddressBar(Composite parent, IFolder[] path, List<List<Action>> actionGroups) {
		super(parent, SWT.NONE);
		Controls.handle(this).rwt(BruiToolkit.CSS_BAR_TITLE).bg(BruiColor.Grey_50).formLayout().get();
		// ������ఴť
		Action action = new ActionFactory().img("/img/line_up.svg").disImg("/img/line_up_disable.svg").tooltips("�ϼ�Ŀ¼").id(ACTION_UP).get();
		Controls<Button> btn = createToolitem(this, action, false).loc(SWT.TOP | SWT.BOTTOM).left(0, 4)// ���ϼ��ļ���
				.listen(SWT.Selection, e -> folderChanged(this.path.length - 2, action));// �ı䵱ǰĿ¼
		Label lead = Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).left(btn.get()).get();

		// �����Ҳఴť��
		Control left = null;
		for (int i = actionGroups.size() - 1; i >= 0; i--) {
			List<Action> group = actionGroups.get(i);
			for (int j = group.size() - 1; j >= 0; j--) {
				Action itmAct = group.get(j);
				btn = createToolitem(this, itmAct, true);
				if (btn != null) {
					// ��ť����¼�
					left = btn.loc(SWT.TOP | SWT.BOTTOM).right(left).listen(SWT.Selection, e -> handleEvent(SWT.Selection, itmAct)).get();
				}
			}
			if (left != null)
				left = Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).right(left).get();
		}

		// ˢ��
		Action refreshAction = new ActionFactory().img("/img/line_refresh.svg").tooltips("ˢ��").id(ACTION_REFRESH).get();
		btn = createToolitem(this, refreshAction, false).loc(SWT.TOP | SWT.BOTTOM).right(left)//
				// ˢ���¼� ��ͬ�ڽ���ǰ��Ŀ¼����ѡ��һ��
				.listen(SWT.Selection, e -> handleEvent(SWT.Modify, refreshAction));
		Label end = Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).right(btn.get()).get();

		// ������ַ��
		addressBar = Controls.contentPanel(this).bg(BruiColor.White).loc(SWT.TOP | SWT.BOTTOM).left(lead).right(end);
		setPath(path);
	}

	private ActionEvent handleEvent(int listenerType, Action action) {
		ActionEvent event = createEvent(listenerType, action);
		Stream.of(getListeners(listenerType)).forEach(l -> l.handleEvent(event));
		return event;
	}

	public void setPath(IFolder[] path) {
		if (Arrays.equals(this.path, path))
			return;

		// ���
		Composite parent = addressBar.get();
		Stream.of(parent.getChildren()).forEach(c -> c.dispose());

		if (path != null && path.length > 0) {
			// ��ʾ·��
			Composite panel = Controls.comp(parent).formLayout().height(32).get();
			Control left = null;
			for (int i = 0; i < path.length; i++) {
				int folderIndex = i;

				Action action = new ActionFactory().text(path[i].getName()).get();
				left = createToolitem(panel, action, false).loc(SWT.TOP | SWT.BOTTOM).left(left)//
						.listen(SWT.Selection, e -> folderChanged(folderIndex, action))// �ı䵱ǰĿ¼
						.get();

				left = Controls.label(panel, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).left(left).get();
			}
			panel.setLocation(0, 0);
			final Point panelSize = panel.computeSize(SWT.DEFAULT, 32);
			panel.setSize(panelSize);
			parent.addListener(SWT.Resize, e -> {
				int offset = parent.getSize().x - panelSize.x - 8;
				if (offset < 0) {
					panel.setLocation(offset, 0);
				}
			});
		}

		this.path = path;

		// �����ļ��иı���¼�
		Stream.of(getListeners(SWT.SetData)).forEach(l -> l.handleEvent(createEvent(SWT.SetData, null)));

		// ��������һ��Ŀ¼�İ�ť״̬
		updateUplevelBtnStatus();

	}

	private void folderChanged(int index, Action action) {
		// ����index�õ�path
		IFolder[] newPath = new IFolder[index + 1];
		System.arraycopy(path, 0, newPath, 0, index + 1);

		ActionEvent event = createEvent(SWT.Modify, action);
		event.path = newPath;
		event.doit = true;

		Stream.of(getListeners(SWT.Modify)).forEach(l -> l.handleEvent(event));

		if (event.doit) {// ���Ըı�Ŀ¼
			setPath(newPath);
		}
	}

	private ActionEvent createEvent(int eventCode, Action action) {
		ActionEvent event = new ActionEvent();
		event.type = eventCode;
		event.path = path;
		event.action = action;
		return event;
	}

	private void updateUplevelBtnStatus() {
		for (int i = 0; i < controlHolder.size(); i++) {
			Controls<Button> c = controlHolder.get(i);
			Action a = (Action) c.get().getData("action");
			if (ACTION_UP.equals(a.getId())) {
				if (path.length > 1) {
					c.setImageText(a.getImage(), a.getText(), 16, 32);
				} else {
					c.setImageText(a.getImageDisabled(), a.getText(), 16, 32);
				}
				c.get().setEnabled(path.length > 1);
				return;
			}
		}
	}

	/**
	 * ������Ȩ�ޣ���Ȩ��û����֤ͨ�� ����null
	 * 
	 * @param bar
	 * @param action
	 * @param checkAuthority
	 * @return
	 */
	private Controls<Button> createToolitem(Composite bar, Action action, boolean checkAuthority) {
		if (checkAuthority && !hasAuthority(action)) // ������Ȩ�ޣ���Ȩ��û����֤ͨ��
			return null;

		String style = Check.option(action.getStyle()).orElse("compact");

		Controls<Button> button = Controls.button(bar).rwt(style).setImageText(action.getImage(), action.getText(), 16, 32)
				.tooltips(action.getTooltips()).setData("action", action);
		button.listen(SWT.Dispose, e -> controlHolder.remove(button));//
		controlHolder.add(button);
		return button;
	}

	/**
	 * �ж��Ƿ���иò�����Ȩ��
	 * 
	 * @param action
	 * @return
	 */
	private boolean hasAuthority(Action action) {
		// TODO Auto-generated method stub
		return true;
	}

}
