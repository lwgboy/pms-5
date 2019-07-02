package com.bizvisionsoft.service.exporter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc.Enum;

import com.bizvisionsoft.service.tools.Check;

public class TableCellWriter extends CellWriter {

	private TableCellValue value;
	private int width;

	public TableCellWriter(ExportableFormField f, TableCellValue value, int width) {
		super(f);
		this.value = value;
		this.width = width;
	}

	@Override
	public void write(XWPFTableCell cell) {
		super.write(cell);
		if (value == null)
			return;
		XWPFParagraph p = cell.getParagraphArray(0);
		XmlCursor cursor = p.getCTP().newCursor();
		XWPFTable table = cell.insertNewTbl(cursor);
		table.setWidth(width);
		table.setTableAlignment(TableRowAlign.CENTER);
		table.setCellMargins(40, 40, 40, 40);// 设定内边距

		int total = 0;
		for (int i = 0; i < value.columnWidths.size(); i++) {
			total += value.columnWidths.get(i);
		}
		String[] colWidth = new String[value.columnWidths.size()];
		for (int i = 0; i < colWidth.length; i++) {
			int ratio =(int)( (1f * width*value.columnWidths.get(i)) / total);
			colWidth[i] = ""+ratio;
		}

		if (Check.isAssigned(value.headers)) {
			createRow(table, value.headers, colWidth, r -> {
				r.setFontFamily("宋体");
				r.setFontSize(9);
				r.setBold(true);
			});
		}
		if (Check.isAssigned(value.rows)) {
			createDataRow(table, value.rows, colWidth);
		}
	}

	private void createDataRow(XWPFTable table, List<List<String>> rows, String[] colWidth) {
		for (int i = 0; i < rows.size(); i++) {
			createRow(table, rows.get(i), colWidth, r -> {
				r.setFontFamily("宋体");
				r.setFontSize(9);
			});
		}
	}

	private void createRow(XWPFTable table, List<String> cellsText, String[] colWidth, Consumer<XWPFRun> formatter) {
		XWPFTableRow row = table.createRow();
		for (int j = 0; j < cellsText.size(); j++) {
			String cellData = cellsText.get(j);
			XWPFTableCell c = row.getCell(j);
			if (c == null)
				c = row.addNewTableCell();

			WordUtil.setCellWidth(c, colWidth[j]);
			// 单元格对齐方式，垂直居中，水平居中
			XWPFVertAlign al = Optional.ofNullable(getVerticalAlignment()).orElse(XWPFVertAlign.CENTER);
			WordUtil.setCellVerticalAlign(c, al);
			// 水平居中
			Enum val = Optional.ofNullable(getHorizontalAlign()).orElse(STJc.CENTER);
			WordUtil.setCellHorizontalAlign(c, val);

			XWPFRun pRun = c.getParagraphArray(0).createRun();
			pRun.setText(cellData);// 处理超文本
		}
	}

}
