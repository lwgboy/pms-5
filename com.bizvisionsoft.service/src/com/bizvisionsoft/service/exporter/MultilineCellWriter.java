package com.bizvisionsoft.service.exporter;

import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;

import com.bizvisionsoft.service.tools.Check;

public class MultilineCellWriter extends CellWriter {

	private List<?> value;

	public MultilineCellWriter(ExportableFormField f, List<?> list) {
		super(f);
		this.value = list;
	}

	@Override
	protected XWPFVertAlign getDefaultVerticalAlign() {
		return null;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		if (Check.isAssigned(value)) {
			for (int i = 0; i < value.size(); i++) {
				XWPFParagraph p = cell.addParagraph();
				XWPFRun pRun = p.createRun();
				applyCellText(pRun, "" + value.get(i));
			}
		}
	}

}
