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
	 * 在父容器下创建地址栏
	 * 
	 * @param parent
	 *            父容器
	 * @param path
	 *            当前的路径
	 * @param actionGroups
	 *            按钮组
	 * @param authority
	 *            检查权限的代码
	 */
	public AddressBar(Composite parent, IFolder[] path, List<List<Action>> actionGroups, Function<Action, Boolean> authority) {
		super(parent, SWT.NONE);
		this.authority = authority;
		Controls.handle(this).rwt(BruiToolkit.CSS_BAR_TITLE).bg(BruiColor.Grey_50).formLayout().get();
		// 创建左侧按钮
		Action action = VaultActions.create(VaultActions.uplevel, true, false);
		Controls<Button> btn = createToolitem(this, action, false).loc(SWT.TOP | SWT.BOTTOM).left(0, 4)// 向上级文件夹
				.listen(SWT.Selection, e -> handlerEvent(SWT.Modify, this.path.length - 2, action));// 改变当前目录
		Label lead = createToolitemSeperator().left(btn.get()).get();

		// 创建右侧按钮组
		Control left = null;
		for (int i = actionGroups.size() - 1; i >= 0; i--) {
			List<Action> group = actionGroups.get(i);
			for (int j = group.size() - 1; j >= 0; j--) {
				Action a = group.get(j);
				btn = createToolitem(this, a, true);
				if (btn != null)
					// 按钮点击事件
					left = btn.loc(SWT.TOP | SWT.BOTTOM).right(left)
							.listen(SWT.Selection, e -> handlerEvent(SWT.Selection, this.path.length - 1, a)).get();
			}

			if (left != null)
				left = createToolitemSeperator().right(left).get();
		}

		// 刷新
		Action refreshAction = VaultActions.create(VaultActions.refresh, true, false);
		;
		btn = createToolitem(this, refreshAction, false).loc(SWT.TOP | SWT.BOTTOM).right(left)//
				// 刷新事件 等同于将当前的目录重新选择一次
				.listen(SWT.Selection, e -> handlerEvent(SWT.Modify, this.path.length - 1, refreshAction));

		Label end = createToolitemSeperator().right(btn.get()).get();

		// 创建地址栏
		addressBar = Controls.contentPanel(this).bg(BruiColor.White).loc(SWT.TOP | SWT.BOTTOM).left(lead).right(end);
		setPath(path);
	}

	/**
	 * 改变当前的目录
	 * 
	 * @param path
	 */
	public void setPath(IFolder[] path) {
		if (Arrays.equals(this.path, path))
			return;

		// 清空
		Composite parent = addressBar.get();
		Stream.of(parent.getChildren()).forEach(c -> c.dispose());

		if (path != null && path.length > 0) {
			// 显示路径
			Composite panel = Controls.comp(parent).formLayout().height(BAR_HEIGHT).get();
			Control left = null;
			for (int i = 0; i < path.length; i++) {
				int folderIndex = i;

				Action action = new ActionFactory().text(path[i].getName()).get();
				left = createToolitem(panel, action, false).loc(SWT.TOP | SWT.BOTTOM).left(left)//
						.listen(SWT.Selection, e -> handlerEvent(SWT.Modify, folderIndex, action))// 改变当前目录
						.get();

				left = Controls.label(panel, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).left(left).get();
			}

			caculatePathItemBounds(parent, panel);
			parent.addListener(SWT.Resize, e -> caculatePathItemBounds(parent, panel));
		}

		this.path = path;

		// 触发文件夹改变的事件
		handlerEvent(SWT.SetData, path.length - 1, null);

		// 设置向上一级目录的按钮状态
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
		// 根据index得到path
		IFolder[] newPath = new IFolder[lvl + 1];
		System.arraycopy(path, 0, newPath, 0, lvl + 1);

		PathActionEvent event = new PathActionEvent(listenerType, action, newPath);
		event.doit = listenerType == SWT.Modify;

		Stream.of(getListeners(listenerType)).forEach(l -> l.handleEvent(event));

		if (event.doit) // 可以改变目录
			setPath(newPath);
	}

	/**
	 * 改变工具栏按钮状态
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
	 * 如果检查权限，但权限没有验证通过 返回null
	 * 
	 * @param bar
	 * @param action
	 * @param checkAuthority
	 * @return
	 */
	private Controls<Button> createToolitem(Composite bar, Action action, boolean checkAuthority) {
		if (checkAuthority && authority != null && !authority.apply(action))
			// 如果检查权限，但权限没有验证通过
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
	 * @return 返回当前的路径
	 */
	public IFolder[] getCurrentPath() {
		return path;
	}

}
