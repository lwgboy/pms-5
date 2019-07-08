package com.bizvisionsoft.service.exporter;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc.Enum;

import com.bizvisionsoft.service.tools.Check;

public class LabelCellWriter extends CellWriter {

	private String value;

	public LabelCellWriter(ExportableFormField f) {
		super(f);
		value = f.stdReportLabelText;
		if (!Check.isAssigned(value))
			value = f.text;
	}

	@Override
	protected String getDefaultFontFamily() {
		return "ºÚÌå";
	}

	@Override
	protected Enum getHorizontalAlign() {
		return STJc.CENTER;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		if (Check.isAssigned(value)) {
			XWPFParagraph p = cell.getParagraphArray(0);
			XWPFRun pRun = p.createRun();
			applyCellText(pRun, value);
		}
	}

	@Override
	protected boolean isLabel() {
		return true;
	}

}
