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
		// 创建左侧按钮
		Action action = new ActionFactory().img("/img/line_up.svg").disImg("/img/line_up_disable.svg").tooltips("上级目录").id(ACTION_UP).get();
		Controls<Button> btn = createToolitem(this, action, false).loc(SWT.TOP | SWT.BOTTOM).left(0, 4)// 向上级文件夹
				.listen(SWT.Selection, e -> folderChanged(this.path.length - 2, action));// 改变当前目录
		Label lead = Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).left(btn.get()).get();

		// 创建右侧按钮组
		Control left = null;
		for (int i = actionGroups.size() - 1; i >= 0; i--) {
			List<Action> group = actionGroups.get(i);
			for (int j = group.size() - 1; j >= 0; j--) {
				Action itmAct = group.get(j);
				btn = createToolitem(this, itmAct, true);
				if (btn != null) {
					// 按钮点击事件
					left = btn.loc(SWT.TOP | SWT.BOTTOM).right(left).listen(SWT.Selection, e -> handleEvent(SWT.Selection, itmAct)).get();
				}
			}
			if (left != null)
				left = Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).right(left).get();
		}

		// 刷新
		Action refreshAction = new ActionFactory().img("/img/line_refresh.svg").tooltips("刷新").id(ACTION_REFRESH).get();
		btn = createToolitem(this, refreshAction, false).loc(SWT.TOP | SWT.BOTTOM).right(left)//
				// 刷新事件 等同于将当前的目录重新选择一次
				.listen(SWT.Selection, e -> handleEvent(SWT.Modify, refreshAction));
		Label end = Controls.label(this, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM).right(btn.get()).get();

		// 创建地址栏
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

		// 清空
		Composite parent = addressBar.get();
		Stream.of(parent.getChildren()).forEach(c -> c.dispose());

		if (path != null && path.length > 0) {
			// 显示路径
			Composite panel = Controls.comp(parent).formLayout().height(32).get();
			Control left = null;
			for (int i = 0; i < path.length; i++) {
				int folderIndex = i;

				Action action = new ActionFactory().text(path[i].getName()).get();
				left = createToolitem(panel, action, false).loc(SWT.TOP | SWT.BOTTOM).left(left)//
						.listen(SWT.Selection, e -> folderChanged(folderIndex, action))// 改变当前目录
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

		// 触发文件夹改变的事件
		Stream.of(getListeners(SWT.SetData)).forEach(l -> l.handleEvent(createEvent(SWT.SetData, null)));

		// 设置向上一级目录的按钮状态
		updateUplevelBtnStatus();

	}

	private void folderChanged(int index, Action action) {
		// 根据index得到path
		IFolder[] newPath = new IFolder[index + 1];
		System.arraycopy(path, 0, newPath, 0, index + 1);

		ActionEvent event = createEvent(SWT.Modify, action);
		event.path = newPath;
		event.doit = true;

		Stream.of(getListeners(SWT.Modify)).forEach(l -> l.handleEvent(event));

		if (event.doit) {// 可以改变目录
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
	 * 如果检查权限，但权限没有验证通过 返回null
	 * 
	 * @param bar
	 * @param action
	 * @param checkAuthority
	 * @return
	 */
	private Controls<Button> createToolitem(Composite bar, Action action, boolean checkAuthority) {
		if (checkAuthority && !hasAuthority(action)) // 如果检查权限，但权限没有验证通过
			return null;

		String style = Check.option(action.getStyle()).orElse("compact");

		Controls<Button> button = Controls.button(bar).rwt(style).setImageText(action.getImage(), action.getText(), 16, 32)
				.tooltips(action.getTooltips()).setData("action", action);
		button.listen(SWT.Dispose, e -> controlHolder.remove(button));//
		controlHolder.add(button);
		return button;
	}

	/**
	 * 判断是否具有该操作的权限
	 * 
	 * @param action
	 * @return
	 */
	private boolean hasAuthority(Action action) {
		// TODO Auto-generated method stub
		return true;
	}

}
