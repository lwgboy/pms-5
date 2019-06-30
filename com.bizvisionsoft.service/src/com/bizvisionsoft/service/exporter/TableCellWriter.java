package com.bizvisionsoft.service.exporter;

import java.util.List;
import java.util.Optional;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc.Enum;

public class TableCellWriter extends CellWriter {

	private List<?> value;
	private int width;

	public TableCellWriter(ExportableFormField f, List<?> value, int width) {
		super(f);
		this.value = value;
		this.width = width;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		if (value == null || value.size() < 2)// 第一行保存列宽数据
			return;
		XWPFParagraph p = cell.getParagraphArray(0);
		XmlCursor cursor = p.getCTP().newCursor();
		XWPFTable table = cell.insertNewTbl(cursor);
		table.setWidth(width);
		table.setCellMargins(40, 40, 40, 40);// 设定内边距

		// 第0行保存了列宽
		List<?> colWidthInSetting = (List<?>) value.get(0);
		int total = 0;
		for (int i = 0; i < colWidthInSetting.size(); i++) {
			total += (int) colWidthInSetting.get(i);
		}
		int[] colWidth = new int[colWidthInSetting.size()];
		for (int i = 0; i < colWidth.length; i++) {
			float ratio = (1f * (int) colWidthInSetting.get(i)) / total;
			colWidth[i] = width * (int) ratio;
		}

		for (int i = 1; i < value.size(); i++) {
			List<?> rowData = (List<?>) value.get(i);
			XWPFTableRow row = table.createRow();
			for (int j = 0; j < rowData.size(); j++) {
				String cellData = "" + rowData.get(j);
				XWPFTableCell c = row.getCell(j);
				if (c == null)
					c = row.addNewTableCell();

				WordUtil.setCellWidth(c, (int) WordUtil.mm2halfPt(colWidth[j]));
				// 单元格对齐方式，垂直居中，水平居中
				XWPFVertAlign al = Optional.ofNullable(getVerticalAlignment()).orElse(XWPFVertAlign.CENTER);
				WordUtil.setCellVerticalAlign(c, al);
				// 水平居中
				Enum val = Optional.ofNullable(getHorizontalAlign()).orElse(STJc.CENTER);
				WordUtil.setCellHorizontalAlign(c, val);

				XWPFRun pRun = c.getParagraphArray(0).createRun();
				if (i == 1) {// TODO
					pRun.setFontFamily("宋体");
					pRun.setFontSize(9);
					pRun.setBold(true);
				} else {
					pRun.setFontFamily("宋体");
					pRun.setFontSize(9);
				}
				pRun.setText(cellData);// 处理超文本
			}
		}
	}

}
