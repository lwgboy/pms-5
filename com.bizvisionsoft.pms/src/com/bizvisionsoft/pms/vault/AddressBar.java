package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.BruiToolkit;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.tools.Check;

public class AddressBar extends Composite {

	private static final int BAR_HEIGHT = 32;

	private static final int IMG_SIZE = 16;

	private IFolder[] path;

	private Controls<Composite> addressBar;

	private List<Controls<Button>> controlHolder = new ArrayList<>();

	private Function<Action, Boolean> authority;

	/**
	 * 
	 * �ڸ������´�����ַ��
	 * 
	 * @param parent
	 *            ������
	 * @param path
	 *            ��ǰ��·��
	 * @param actionGroups
	 *            ��ť��
	 * @param authority
	 *            ���Ȩ�޵Ĵ���
	 */
	public AddressBar(Composite parent, IFolder[] path, List<List<Action>> actionGroups, Function<Action, Boolean> authority) {
		super(parent, SWT.NONE);
		this.authority = authority;
		Controls.handle(this).rwt(BruiToolkit.CSS_BAR_TITLE).bg(BruiColor.Grey_50).formLayout().get();
		// ������ఴť
		Action action = VaultActions.create(VaultActions.uplevel, true, false);
		Controls<Button> btn = createToolitem(this, action, false).loc(SWT.TOP | SWT.BOTTOM).left(0, 4)// ���ϼ��ļ���
				.listen(SWT.Selection, e -> handlerEvent(SWT.Modify, this.path.length - 2, action));// �ı䵱ǰĿ¼
		Label lead = createToolitemSeperator().left(btn.get()).get();

		// �����Ҳఴť��
		Control left = null;
		for (int i = actionGroups.size() - 1; i >= 0; i--) {
			List<Action> group = actionGroups.get(i);
			for (int j = group.size() - 1; j >= 0; j--) {
				Action a = group.get(j);
				btn = createToolitem(this, a, true);
				if (btn != null)
					// ��ť����¼�
					left = btn.loc(SWT.TOP | SWT.BOTTOM).right(left)
							.listen(SWT.Selection, e -> handlerEvent(SWT.Selection, this.path.length - 1, a)).get();
			}

			if (left != null)
				left = createToolitemSeperator().right(left).get();
		}

		// ˢ��
		Action refreshAction = VaultActions.create(VaultActions.refresh, true, false);
		;
		btn = createToolitem(this, refreshAction, false).loc(SWT.TOP | SWT.BOTTOM).right(left)//
				// ˢ���¼� ��ͬ�ڽ���ǰ��Ŀ¼����ѡ��һ��
				.listen(SWT.Selection, e -> handlerEvent(SWT.Modify, this.path.length - 1, refreshAction));

		Label end = createToolitemSeperator().right(btn.get()).get();

		// ������ַ��
		addressBar = Controls.contentPanel(this).bg(BruiColor.White).loc(SWT.TOP | SWT.BOTTOM).left(lead).right(end);
		setPath(path);
	}

	/**
	 * �ı䵱ǰ��Ŀ¼
	 * 
	 * @param path
	 */
	public void setPath(IFolder[] path) {
		if (Arrays.equals(this.path, path))
			return;

		// ���
		Composite parent = addressBar.get();
		Stream.of(parent.getChildren()).forEach(c -> c.dispose());

		if (path != null && path.length > 0) {
			// ��ʾ·��
			Composite panel = Controls.comp(parent).formLayout().height(BAR_HEIGHT).get();
			Control left = null;
			for (int i = 0; i < path.length; i++) {
				int folderIndex = i;

				Action action = new ActionFactory().text(path[i].getName()).get();
				left = createToolitem(panel, action, false).loc(SWT.TOP | SWT.BOTTOM).left(left)//
						.listen(SWT.Selection, e -> handlerEvent(SWT.Modify, folderIndex, action))// �ı䵱ǰĿ¼
						.get();

				left = Controls.label(panel, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).left(left).get();
			}

			caculatePathItemBounds(parent, panel);
			parent.addListener(SWT.Resize, e -> caculatePathItemBounds(parent, panel));
		}

		this.path = path;

		// �����ļ��иı���¼�
		handlerEvent(SWT.SetData, path.length - 1, null);

		// ��������һ��Ŀ¼�İ�ť״̬
		updateToolitemStatus(VaultActions.uplevel.name(), b -> path.length > 0);
		parent.layout();
	}

	private void caculatePathItemBounds(Composite parent, Composite panel) {
		Point panelSize = panel.computeSize(SWT.DEFAULT, BAR_HEIGHT);
		panel.setSize(panelSize);
		Point parentSize = parent.getSize();
		int offset = 0;
		if (parentSize.x != 0) {
			int _offset = parentSize.x - panelSize.x - 8;
			if (_offset < 0) {
				offset = _offset;
			}
		}
		panel.setLocation(offset, 0);
	}

	private void handlerEvent(int listenerType, int lvl, Action action) {
		// ����index�õ�path
		IFolder[] newPath = new IFolder[lvl + 1];
		System.arraycopy(path, 0, newPath, 0, lvl + 1);

		PathActionEvent event = new PathActionEvent(listenerType, action, newPath);
		event.doit = listenerType == SWT.Modify;

		Stream.of(getListeners(listenerType)).forEach(l -> l.handleEvent(event));

		if (event.doit) // ���Ըı�Ŀ¼
			setPath(newPath);
	}

	/**
	 * �ı乤������ť״̬
	 * 
	 * @param actionId
	 * @param func
	 */
	public void updateToolitemStatus(String actionId, Function<Controls<Button>, Boolean> func) {
		for (int i = 0; i < controlHolder.size(); i++) {
			Controls<Button> c = controlHolder.get(i);
			Action a = (Action) c.get().getData("action");
			if (actionId.equals(a.getId())) {
				boolean result = func != null && Boolean.TRUE.equals(func.apply(c));
				String img = result ? a.getImage() : a.getImageDisabled();
				c.setImageText(img, a.getText(), IMG_SIZE, BAR_HEIGHT);
				c.get().setEnabled(result);
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
		if (checkAuthority && authority != null && !authority.apply(action))
			// ������Ȩ�ޣ���Ȩ��û����֤ͨ��
			return null;

		String style = Check.option(action.getStyle()).orElse("compact");

		Controls<Button> button = Controls.button(bar).rwt(style).setImageText(action.getImage(), action.getText(), IMG_SIZE, BAR_HEIGHT)
				.tooltips(action.getTooltips()).setData("action", action);
		button.listen(SWT.Dispose, e -> controlHolder.remove(button));//
		controlHolder.add(button);
		return button;
	}

	private Controls<Label> createToolitemSeperator() {
		return Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM);
	}

	/**
	 * 
	 * @return ���ص�ǰ��·��
	 */
	public IFolder[] getCurrentPath() {
		return path;
	}

}
