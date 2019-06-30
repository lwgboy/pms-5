package com.bizvisionsoft.service.exporter;

import java.util.List;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSym;

public class CheckCellWriter extends CellWriter {

	private boolean value;

	public CheckCellWriter(ExportableFormField f, boolean selection) {
		super(f);
		this.value = selection;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		XWPFParagraph p = cell.getParagraphArray(0);
		XWPFRun pRun = p.createRun();
		applyCellText(pRun, "    ");
		List<CTSym> symList = pRun.getCTR().getSymList();
		int code = value ? 162 : 163;
		symList.add(WordUtil.getCTSym("Wingdings 2", "F0" + Integer.toHexString(code)));// 83ÊÇÑ¡ÖÐ
	}
}
