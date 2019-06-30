package com.bizvisionsoft.service.exporter;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.bizvisionsoft.service.tools.Check;

public class TextCellWriter extends CellWriter {

	private String value;

	public TextCellWriter(ExportableFormField f, String text) {
		super(f);
		this.value = text;
	}

	public void write(org.apache.poi.xwpf.usermodel.XWPFTableCell cell) {
		super.write(cell);
		if (Check.isAssigned(value)) {
			XWPFParagraph p = cell.getParagraphArray(0);
			XWPFRun pRun = p.createRun();
			applyCellText(pRun, value);
		}
	}

}
