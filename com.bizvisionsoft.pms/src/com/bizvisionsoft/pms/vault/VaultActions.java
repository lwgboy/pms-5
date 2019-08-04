package com.bizvisionsoft.pms.vault;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;

/**
 * 统一管理action
 * 
 * @author hua
 *
 */
public enum VaultActions {

	createSubFolder("新建目录", "在当前目录下创建子目录", "/img/line_newFolder.svg", "/img/line_newFolder_disable.svg"),

	createDocument("新建文档", "在当前目录下创建文档", "/img/line_newDoc.svg", "/img/line_newDoc_disable.svg"), //

	findSubFolder("查找目录", "在当前目录下查找子目录", "/img/line_searchFolder.svg"), //

	findDocuments("查找文档", "在当前目录下查找文档", "/img/line_searchDoc.svg"), //

	search("搜索", "在资料库中搜索文档", "/img/line_search.svg"), //

	sortDocuments("排序", "对当前目录下的文档进行排序", "/img/line_sort.svg"), //

	addFavour("收藏", "将当前目录添加到收藏夹", "/img/line_star.svg"), //

	setFolderProperties("属性", "设置目录属性", "/img/line_setting.svg"), //

	refresh("刷新", "刷新当前目录的子目录和文档", "/img/line_refresh.svg"), //

	uplevel("上一级", "返回上一级目录", "/img/line_up.svg","/img/line_up_disable.svg");

	private String label;

	private String desc;

	private String img;

	private String disImg;

	private VaultActions(String label, String desc, String img, String disImg) {
		this.label = label;
		this.desc = desc;
		this.img = img;
		this.disImg = disImg;
	}

	private VaultActions(String label, String desc, String img) {
		this.label = label;
		this.desc = desc;
		this.img = img;
	}

	public String desc() {
		return desc;
	}

	public String label() {
		return label;
	}

	public String getImg() {
		return img;
	}

	public String getDisImg() {
		return disImg;
	}

	public static Action create(VaultActions va, boolean withImg, boolean withText) {
		ActionFactory f = new ActionFactory().id(va.name()).tooltips(va.desc);
		if (withImg)
			f.img(va.img).disImg(va.disImg);
		if (withText)
			f.text(va.label);
		return f.get();
	}
}
