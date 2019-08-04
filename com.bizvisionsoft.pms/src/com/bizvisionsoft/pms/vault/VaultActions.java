package com.bizvisionsoft.pms.vault;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;

/**
 * ͳһ����action
 * 
 * @author hua
 *
 */
public enum VaultActions {

	createSubFolder("�½�Ŀ¼", "�ڵ�ǰĿ¼�´�����Ŀ¼", "/img/line_newFolder.svg", "/img/line_newFolder_disable.svg"),

	createDocument("�½��ĵ�", "�ڵ�ǰĿ¼�´����ĵ�", "/img/line_newDoc.svg", "/img/line_newDoc_disable.svg"), //

	findSubFolder("����Ŀ¼", "�ڵ�ǰĿ¼�²�����Ŀ¼", "/img/line_searchFolder.svg"), //

	findDocuments("�����ĵ�", "�ڵ�ǰĿ¼�²����ĵ�", "/img/line_searchDoc.svg"), //

	search("����", "�����Ͽ��������ĵ�", "/img/line_search.svg"), //

	sortDocuments("����", "�Ե�ǰĿ¼�µ��ĵ���������", "/img/line_sort.svg"), //

	addFavour("�ղ�", "����ǰĿ¼��ӵ��ղؼ�", "/img/line_star.svg"), //

	setFolderProperties("����", "����Ŀ¼����", "/img/line_setting.svg"), //

	refresh("ˢ��", "ˢ�µ�ǰĿ¼����Ŀ¼���ĵ�", "/img/line_refresh.svg"), //

	uplevel("��һ��", "������һ��Ŀ¼", "/img/line_up.svg","/img/line_up_disable.svg");

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
