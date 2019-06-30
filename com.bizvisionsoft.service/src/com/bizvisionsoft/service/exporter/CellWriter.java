package com.bizvisionsoft.service.exporter;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc.Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.tools.Check;

public abstract class CellWriter {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final CellWriter BLANK_CELL = new CellWriter(null) {
	};

	protected ExportableFormField field;

	public CellWriter(ExportableFormField f) {
		this.field = f;
	}

	public void write(XWPFTableCell cell) {
		Integer width = getWidthInMM();
		WordUtil.setCellWidth(cell, width);

		applyCellStyle(cell);
	}

	protected void applyCellStyle(XWPFTableCell cell) {
		String color = getBackgoundColorInRGB();
		WordUtil.setCellColor(cell, color);

		// 单元格对齐方式，垂直居中，水平居中
		XWPFVertAlign al = getVerticalAlignment();
		WordUtil.setCellVerticalAlign(cell, al);

		Enum val = getHorizontalAlign();
		WordUtil.setCellHorizontalAlign(cell, val);

		int[] cellMar = getCellMargin();
		WordUtil.setCellMargin(cell, cellMar);

		if (field != null) {
			if (isLabel()) {
				applyLabelCellBorder(cell);
			} else {
				applyFieldCellBorder(cell);
			}
		}

	}

	protected void applyFieldCellBorder(XWPFTableCell cell) {
		String lineType = field.stdReportFieldTopBorderType;
		String lineColor = field.stdReportFieldTopBorderColor;
		Integer lineSize = field.stdReportFieldTopBorderSize;
		WordUtil.setCellBorder(cell, "上", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldLeftBorderType;
		lineColor = field.stdReportFieldLeftBorderColor;
		lineSize = field.stdReportFieldLeftBorderSize;
		WordUtil.setCellBorder(cell, "左", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldBottomBorderType;
		lineColor = field.stdReportFieldBottomBorderColor;
		lineSize = field.stdReportFieldBottomBorderSize;
		WordUtil.setCellBorder(cell, "下", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldRightBorderType;
		lineColor = field.stdReportFieldRightBorderColor;
		lineSize = field.stdReportFieldRightBorderSize;
		WordUtil.setCellBorder(cell, "右", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldInsideHBorderType;
		lineColor = field.stdReportFieldInsideHBorderColor;
		lineSize = field.stdReportFieldInsideHBorderSize;
		WordUtil.setCellBorder(cell, "横", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldInsideVBorderType;
		lineColor = field.stdReportFieldInsideVBorderColor;
		lineSize = field.stdReportFieldInsideVBorderSize;
		WordUtil.setCellBorder(cell, "纵", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldTL2BRBorderType;
		lineColor = field.stdReportFieldTL2BRBorderColor;
		lineSize = field.stdReportFieldTL2BRBorderSize;
		WordUtil.setCellBorder(cell, "上左下右", lineType, lineSize, lineColor);

		lineType = field.stdReportFieldTR2BLBorderType;
		lineColor = field.stdReportFieldTR2BLBorderColor;
		lineSize = field.stdReportFieldTR2BLBorderSize;
		WordUtil.setCellBorder(cell, "上右下左", lineType, lineSize, lineColor);
	}

	protected void applyLabelCellBorder(XWPFTableCell cell) {
		String lineType = field.stdReportLabelTopBorderType;
		String lineColor = field.stdReportLabelTopBorderColor;
		Integer lineSize = field.stdReportLabelTopBorderSize;
		WordUtil.setCellBorder(cell, "上", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelLeftBorderType;
		lineColor = field.stdReportLabelLeftBorderColor;
		lineSize = field.stdReportLabelLeftBorderSize;
		WordUtil.setCellBorder(cell, "左", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelBottomBorderType;
		lineColor = field.stdReportLabelBottomBorderColor;
		lineSize = field.stdReportLabelBottomBorderSize;
		WordUtil.setCellBorder(cell, "下", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelRightBorderType;
		lineColor = field.stdReportLabelRightBorderColor;
		lineSize = field.stdReportLabelRightBorderSize;
		WordUtil.setCellBorder(cell, "右", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelInsideHBorderType;
		lineColor = field.stdReportLabelInsideHBorderColor;
		lineSize = field.stdReportLabelInsideHBorderSize;
		WordUtil.setCellBorder(cell, "横", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelInsideVBorderType;
		lineColor = field.stdReportLabelInsideVBorderColor;
		lineSize = field.stdReportLabelInsideVBorderSize;
		WordUtil.setCellBorder(cell, "纵", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelTL2BRBorderType;
		lineColor = field.stdReportLabelTL2BRBorderColor;
		lineSize = field.stdReportLabelTL2BRBorderSize;
		WordUtil.setCellBorder(cell, "上左下右", lineType, lineSize, lineColor);

		lineType = field.stdReportLabelTR2BLBorderType;
		lineColor = field.stdReportLabelTR2BLBorderColor;
		lineSize = field.stdReportLabelTR2BLBorderSize;
		WordUtil.setCellBorder(cell, "上右下左", lineType, lineSize, lineColor);
	}

	protected void applyCellText(XWPFRun pRun, String text) {
		Optional.ofNullable(text).ifPresent(pRun::setText);
		Optional.ofNullable(getFontSize()).ifPresent(pRun::setFontSize);
		Optional.ofNullable(getFontFamily()).ifPresent(pRun::setFontFamily);
		Optional.ofNullable(getForegoundColorInRGB()).ifPresent(pRun::setColor);
		pRun.setBold(isBold());
	}

	private int[] getCellMargin() {
		if (field != null) {
			String margin = isLabel() ? field.stdReportLabelCellMargin : field.stdReportFieldCellMargin;
			try {
				Integer[] result = Stream.of(margin.split(" ")).map(s -> (int) WordUtil.mm2halfPt(Float.parseFloat(s)))
						.collect(Collectors.toList()).toArray(new Integer[0]);
				return new int[] { result[0], result[1], result[2], result[3] };
			} catch (Exception e) {
			}
		}
		return getDefaultCellMargin();
	}

	protected int[] getDefaultCellMargin() {
		return null;
	}

	protected Enum getHorizontalAlign() {
		if (field != null) {
			String setting = isLabel() ? field.stdReportLabelHorizontalAlign : field.stdReportFieldHorizontalAlign;
			if ("居中对齐".equals(setting)) {
				return STJc.CENTER;
			} else if ("左对齐".equals(setting)) {
				return STJc.LEFT;
			} else if ("右对齐".equals(setting)) {
				return STJc.RIGHT;
			} else if ("两端对齐".equals(setting)) {
				return STJc.DISTRIBUTE;
			}
		}
		return getDefaultHorizontalAlign();
	}

	protected XWPFVertAlign getVerticalAlignment() {
		if (field != null) {
			String setting = isLabel() ? field.stdReportLabelVerticalAlign : field.stdReportFieldVerticalAlign;
			if ("居中对齐".equals(setting)) {
				return XWPFVertAlign.CENTER;
			} else if ("顶端对齐".equals(setting)) {
				return XWPFVertAlign.TOP;
			} else if ("底端对齐".equals(setting)) {
				return XWPFVertAlign.BOTTOM;
			}
		}
		return getDefaultVerticalAlign();
	}

	private boolean isBold() {
		if (field != null) {
			Boolean setting = isLabel() ? field.stdReportLabelBold : field.stdReportFieldBold;
			if (setting != null)
				return setting;
		}
		return getDefaultBold();
	}

	private String getForegoundColorInRGB() {
		if (field != null) {
			String setting = isLabel() ? field.stdReportLabelForegound: field.stdReportFieldForegound;
			if (Check.isAssigned(setting))
				return setting;
		}
		return getDefaultForegoundColorInRGB();
	}

	private String getFontFamily() {
		if (field != null) {
			String setting = isLabel() ? field.stdReportLabelFontFamily : field.stdReportFieldFontFamily;
			if (Check.isAssigned(setting))
				return setting;
		}
		return getDefaultFontFamily();
	}

	private String getBackgoundColorInRGB() {
		if (field != null) {
			String setting = isLabel() ? field.stdReportLabelBackgound : field.stdReportFieldBackgound;
			if (Check.isAssigned(setting))
				return setting;
		}
		return getDefaultBackgoundColorInRGB();
	}

	private int getFontSize() {
		if (field != null) {
			Integer setting = isLabel() ? field.stdReportLabelFontSize : field.stdReportFieldFontSize;
			if (setting != null && setting > 0)
				return setting;
		}
		return getDefaultFontSize();
	}

	public Integer getWidthInMM() {
		if (field != null) {
			Integer setting = isLabel() ? field.stdReportLabelWidth : field.stdReportFieldWidth;
			if (setting != null && setting > 0)
				return setting;
		}
		return getDefaultWidthInMM();

	}

	protected boolean isLabel() {
		return false;
	}

	protected boolean getDefaultBold() {
		return false;
	}

	protected String getDefaultFontFamily() {
		return "宋体";
	}

	protected String getDefaultForegoundColorInRGB() {
		return null;
	}

	protected String getDefaultBackgoundColorInRGB() {
		return null;
	}

	protected int getDefaultFontSize() {
		return 9;
	}

	protected Integer getDefaultWidthInMM() {
		if (isLabel()) {
			return 24;
		} else {
			return null;
		}
	}

	protected XWPFVertAlign getDefaultVerticalAlign() {
		return XWPFVertAlign.CENTER;
	}

	protected Enum getDefaultHorizontalAlign() {
		return null;
	}

}
