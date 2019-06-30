package com.bizvisionsoft.service.exporter;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;

import com.bizvisionsoft.service.tools.Check;

public class ParaCellWriter extends CellWriter {

	private String text;

	public ParaCellWriter(ExportableFormField f, String text) {
		super(f);
		this.text = text;
	}

	@Override
	protected XWPFVertAlign getDefaultVerticalAlign() {
		return null;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		if (Check.isAssigned(text)) {
			String[] paras = text.split("\n");
			for (int i = 0; i < paras.length; i++) {
				XWPFParagraph p = i == 0 ? cell.getParagraphArray(0) : cell.addParagraph();
				XWPFRun pRun = p.createRun();
				applyCellText(pRun, paras[i]);
			}
		}

	}
}
