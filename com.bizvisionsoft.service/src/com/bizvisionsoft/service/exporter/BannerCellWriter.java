package com.bizvisionsoft.service.exporter;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bizvisionsoft.service.tools.Check;

public class BannerCellWriter extends CellWriter {

	private String value;

	public BannerCellWriter(ExportableFormField f, String html) {
		super(f);
		this.value = html;
	}

	@Override
	protected XWPFVertAlign getDefaultVerticalAlign() {
		return null;
	}

	@Override
	protected Boolean getDefaultBold() {
		return true;
	}

	@Override
	protected String getDefaultFontFamily() {
		return "微软雅黑";
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		if (Check.isAssigned(value)) {
			Document doc = Jsoup.parseBodyFragment(value);
			Element body = doc.body();
			Elements es = body.getAllElements();
			XWPFParagraph p;
			XWPFRun pRun;
			if (es.size() == 1) {// 只有纯文本
				p = cell.getParagraphArray(0);
				pRun = p.createRun();
				applyCellText(pRun, es.get(0).text());
			} else {
				for (int i = 1; i < es.size(); i++) {// 忽略第一个元素
					p = i == 1 ? cell.getParagraphArray(0) : cell.addParagraph();
					pRun = p.createRun();
					applyCellText(pRun, es.get(i).text());
				}
			}
		}
	}

}
