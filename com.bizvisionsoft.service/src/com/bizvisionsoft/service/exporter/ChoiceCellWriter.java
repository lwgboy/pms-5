package com.bizvisionsoft.service.exporter;

import java.util.List;
import java.util.Map.Entry;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSym;

public class ChoiceCellWriter extends CellWriter {

	private List<?> value;

	public ChoiceCellWriter(ExportableFormField f, List<?> choice) {
		super(f);
		this.value = choice;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);

		XWPFParagraph p = cell.getParagraphArray(0);
		XWPFRun pRun = p.createRun();
		List<CTSym> symList = pRun.getCTR().getSymList();
		for (int i = 0; i < value.size(); i++) {
			if (i != 0) {
				applyCellText(pRun, "  ");
				if (ExportableFormField.RADIO_STYLE_VERTICAL.equals(field.radioStyle)) // ×ÝÏò
					pRun.addBreak();
			}
			Entry<?, ?> entry = (Entry<?, ?>) value.get(i);
			boolean value = (boolean) entry.getValue();
			int code = value ? 162 : 163;
			symList.add(WordUtil.getCTSym("Wingdings 2", "F0" + Integer.toHexString(code)));
			applyCellText(pRun, entry.getKey() + "");
		}

	}
}
