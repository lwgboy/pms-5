package com.bizvisionsoft.service.exporter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import com.bizvisionsoft.service.tools.Formatter;

public class Form2WordExporter {

	public static final float DEFAULT_LOGO_HEIGHT = 8.5f;

	public static final String FIELD_DOCNUM = "rptDocNum";

	public static final String STATIC_FIELD_COMPANY_NAME = "rptComName";

	public static final String STATIC_FIELD_LOGO_INPUTSTREAM = "rptLogoIs";

	public static final String STATIC_FIELD_LOGO_FILENAME = "rptLogoFileName";

	public static final String FIELD_DOC_STD_NUM = "rptDocStdNum";

	public static final String FIELD_DOC_NAME = "rptDocName";

	private ExportableForm config;

	private Function<String, Object> getFieldValue;

	private Map<String, String> docxProperties;

	public Form2WordExporter(ExportableForm config, Function<String, Object> getFieldText, Map<String, String> docxProperties) {
		this.config = config;
		this.getFieldValue = getFieldText;
		this.docxProperties = docxProperties;
	}

	public String export() throws Exception {
		return exportWithNoneTemplate();
	}

	private String exportWithNoneTemplate() throws Exception {
		if (config.fields == null || config.fields.isEmpty())
			throw new Exception("组件配置错误，缺少字段");

		FileOutputStream out = null;
		try {
			XWPFDocument docx = new XWPFDocument();
			// 预处理
			preTreatment(docx);

			writePage(docx, config.fields);

			postTreatment(docx);

			out = new FileOutputStream(config.fileName);

			if (isReadonly()) {
				docx.enforceReadonlyProtection(UUID.randomUUID().toString(), HashAlgorithm.sha512);
			}

			docx.write(out);
			docx.close();
			// 构建下载地址并打开
			// System.out.println(DigestUtils.md5Hex(new ZipInputStream(new
			// FileInputStream(filePath))));
			return config.fileName;
		} finally {
			if (out != null)
				out.close();
		}
	}

	private boolean isReadonly() {
		return Boolean.FALSE.equals(config.stdReportEditable);
	}

	/**
	 * 后处理
	 * 
	 * @param docx
	 */
	private void postTreatment(XWPFDocument docx) {
		////////////////////////////////////////////////////////////////////////
		// 设置文档属性
		CoreProperties prop = docx.getProperties().getCoreProperties();

		if (docxProperties != null) {
			String text = docxProperties.get("creator");
			if (text != null)
				prop.setCreator(text);
			text = docxProperties.get("created");
			if (text != null)
				prop.setCreated(text);
		}
	}

	private int[] getPageSize() {
		// 取自定义的纸张设置
		if (config.stdReportPageSize != null) {
			try {
				String[] _s = config.stdReportPageSize.split("x");
				return new int[] { Integer.parseInt(_s[0].trim()), Integer.parseInt(_s[1].trim()) };
			} catch (Exception e) {
			}
		}
		if (config.stdReportPaperType != null) {
			int[] result = PageSize.valueOf(config.stdReportPaperType).getSize(config.stdReportPaperOrientation);
			if (result != null)
				return result;
		}

		return PageSize.A4.getSize();
	}

	/**
	 * 
	 * @return 上右下左
	 */
	private int[] getPageMargin() {
		try {
			Integer[] result = Stream.of(config.stdReportPageMargin.split(" ")).map(Integer::parseInt).collect(Collectors.toList())
					.toArray(new Integer[0]);
			return new int[] { result[0], result[1], result[2], result[3] };
		} catch (Exception e) {
		}
		return new int[] { 20, 15, 15, 20 };
	}

	private void writePage(XWPFDocument docx, List<ExportableFormField> fields) {
		boolean noPage = true;
		for (int i = 0; i < fields.size(); i++) {
			ExportableFormField field = fields.get(i);
			if (ExportableFormField.TYPE_PAGE.equals(field.type)) {// 添加页面
				writePageTitle(docx, field);
				writeTable(docx, field.formFields);
				if (i != fields.size() - 1)
					writePageBreaker(docx);
				noPage = false;
			} else if (ExportableFormField.TYPE_PAGE_NOTE.equals(field.type)) {// 文本页面
				writePageTitle(docx, field);
				writeTable(docx, Arrays.asList(field));
				if (i != fields.size() - 1)
					writePageBreaker(docx);
				noPage = false;
			} else if (ExportableFormField.TYPE_PAGE_HTML.equals(field.type)) {
				writePageTitle(docx, field);
				writeTable(docx, Arrays.asList(field));
				if (i != fields.size() - 1)
					writePageBreaker(docx);
				noPage = false;
			}
		}
		if (noPage)
			writeTable(docx, fields);
	}

	private void writePageBreaker(XWPFDocument docx) {
		if (Boolean.TRUE.equals(config.stdReportBreakByTabPage)) {
			docx.createParagraph().setPageBreak(true);
		}
	}

	private void writePageTitle(XWPFDocument docx, ExportableFormField field) {
		if (Boolean.TRUE.equals(config.stdReportExportTabPageTitle)) {
			String title = field.text;
			writeStyleParagraphText(docx, title, "标题 1");
		}
	}

	private void writeTable(XWPFDocument docx, List<ExportableFormField> formFields) {
		if (formFields == null)
			return;
		int cols = caculatePageTableColumns(formFields);
		XWPFTable table = createTable(docx, cols);

		for (int i = 0; i < formFields.size(); i++) {
			ArrayList<CellWriter> cells = createFormattableCells(formFields.get(i), new ArrayList<>());
			// 如果字段数量和列数相等，无需合并
			boolean needMerge = cells.size() != cols;
			XWPFTableRow row = i == 0 ? table.getRow(0) : table.createRow();
			for (int idx = 0; idx < cols; idx++) {
				XWPFTableCell cell = row.getCell(idx);
				if (cells.size() == 2) {// 内容最大化策略
					if (idx == 0) {
						cells.get(0).write(cell);
					} else if (idx == 1) {
						if (needMerge) {
							cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
						}
						cells.get(1).write(cell);
					} else {
						if (needMerge) {
							cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
						}
					}
				} else {// 平均分布策略
					int step = cols / cells.size();// 取每个数据需要占有的格数
					if (idx % step == 0) {
						// 合并单元格
						if (needMerge) {
							cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
						}
						// 写入内容
						cells.get(idx / step).write(cell);
					} else {
						if (needMerge) {
							cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
						}
					}
				}
			}
		}

	}

	private XWPFTable createTable(XWPFDocument docx, int cols) {
		XWPFTable tbl = docx.createTable(1, cols);
		// 设定对齐方式
		tbl.setTableAlignment(TableRowAlign.CENTER);

		// 计算表格尺寸
		int[] pageSize = getPageSize();
		int[] pageMargin = getPageMargin();
		int bodyWidth = pageSize[0] - pageMargin[3] - pageMargin[1];
		tbl.setWidth((int) WordUtil.mm2halfPt(bodyWidth));

		// 设定内边距
		int[] internalMargin = getTableInternalMargin();
		tbl.setCellMargins(internalMargin[0], internalMargin[3], internalMargin[2], internalMargin[3]);

		// 设定表格边框

		int size;
		String color;
		XWPFBorderType borderType = Optional.ofNullable(config.stdReportTableTopBorderType).map(XWPFBorderType::valueOf).orElse(null);
		if (borderType != null) {
			size = Optional.ofNullable(config.stdReportTableTopBorderSize).orElse(1);
			color = config.stdReportTableTopBorderColor;
			tbl.setTopBorder(borderType, size, 0, color);// int space;//如果是段落边框，才考虑这个设置
		}

		borderType = Optional.ofNullable(config.stdReportTableRightBorderType).map(XWPFBorderType::valueOf).orElse(null);
		if (borderType != null) {
			size = Optional.ofNullable(config.stdReportTableRightBorderSize).orElse(1);
			color = config.stdReportTableRightBorderColor;
			tbl.setRightBorder(borderType, size, 0, color);
		}

		borderType = Optional.ofNullable(config.stdReportTableBottomBorderType).map(XWPFBorderType::valueOf).orElse(null);
		if (borderType != null) {
			size = Optional.ofNullable(config.stdReportTableBottomBorderSize).orElse(1);
			color = config.stdReportTableBottomBorderColor;
			tbl.setBottomBorder(borderType, size, 0, color);
		}

		borderType = Optional.ofNullable(config.stdReportTableLeftBorderType).map(XWPFBorderType::valueOf).orElse(null);
		if (borderType != null) {
			size = Optional.ofNullable(config.stdReportTableLeftBorderSize).orElse(1);
			color = config.stdReportTableLeftBorderColor;
			tbl.setLeftBorder(borderType, size, 0, color);
		}

		borderType = Optional.ofNullable(config.stdReportTableInsideHBorderType).map(XWPFBorderType::valueOf).orElse(null);
		if (borderType != null) {
			size = Optional.ofNullable(config.stdReportTableInsideHBorderSize).orElse(1);
			color = config.stdReportTableInsideHBorderColor;
			tbl.setInsideHBorder(borderType, size, 0, color);
		}

		borderType = Optional.ofNullable(config.stdReportTableInsideVBorderType).map(XWPFBorderType::valueOf).orElse(null);
		if (borderType != null) {
			size = Optional.ofNullable(config.stdReportTableInsideVBorderSize).orElse(1);
			color = config.stdReportTableInsideVBorderColor;
			tbl.setInsideVBorder(borderType, size, 0, color);
		}

		return tbl;
	}

	private int[] getTableInternalMargin() {
		String pageMargin = config.stdReportTableInternalMargin;
		try {
			Integer[] result = Stream.of(pageMargin.split(" ")).map(s -> (int) WordUtil.mm2halfPt(Integer.parseInt(s)))
					.collect(Collectors.toList()).toArray(new Integer[0]);
			return new int[] { result[0], result[1], result[2], result[3] };
		} catch (Exception e) {
		}

		return new int[] { 40, 40, 40, 40 };
	}

	private int getFooterMargin() {
		Integer value = config.stdReportFootMargin;
		if (value != null)
			return value.intValue();
		return 8;
	}

	private int getHeaderMargin() {
		Integer value = config.stdReportHeaderMargin;
		if (value != null)
			return value.intValue();
		return 14;
	}

	private ArrayList<CellWriter> createFormattableCells(ExportableFormField f, ArrayList<CellWriter> result) {
		String type = f.type;
		boolean hideLabel = Boolean.TRUE.equals(f.stdReportLabelHide);// 是否隐藏标签
		Object value = getFieldValue.apply(f.name);// 获取取值

		if (ExportableFormField.TYPE_INLINE.equals(type)) {
			f.formFields.forEach(s -> createFormattableCells(s, result));

		} else if (ExportableFormField.TYPE_BANNER.equals(type)) {
			result.add(new BannerCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_CHECK.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new CheckCellWriter(f, Boolean.TRUE.equals(value)));

		} else if (ExportableFormField.TYPE_COMBO.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_DATETIME.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_DATETIME_RANGE.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_FILE.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new FileCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_IMAGE_FILE.equals(type)) {
			int[] pageSize = getPageSize();
			int[] pageMargin = getPageMargin();
			int width = pageSize[0] - pageMargin[3] - pageMargin[1];
			if (!hideLabel) {
				LabelCellWriter w = new LabelCellWriter(f);
				result.add(w);
				width = width - w.getWidthInMM();
			}
			width = (int) WordUtil.mm2halfPt(width);
			result.add(new ImageCellWriter(f, (String[]) value, width));

		} else if (ExportableFormField.TYPE_LABEL.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_LABEL_MULTILINE.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_MULTI_CHECK.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new ChoiceCellWriter(f, (List<?>) value));

		} else if (ExportableFormField.TYPE_MULTI_FILE.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new MultiFileCellWriter(f, (List<?>) value));

		} else if (ExportableFormField.TYPE_MULTI_SELECTION.equals(type)) {
			result.add(CellWriter.BLANK_CELL);// TODO

		} else if (ExportableFormField.TYPE_PAGE.equals(type)) {
			result.add(CellWriter.BLANK_CELL);// 不可能的

		} else if (ExportableFormField.TYPE_PAGE_HTML.equals(type)) {
			String text = Optional.ofNullable(getFieldValue.apply(f.name)).map(o -> o.toString()).orElse("");
			result.add(new HtmlCellWriter(f, (String) text));

		} else if (ExportableFormField.TYPE_PAGE_NOTE.equals(type)) {
			String text = Optional.ofNullable(getFieldValue.apply(f.name)).map(o -> o.toString()).orElse("");
			result.add(new ParaCellWriter(f, (String) text));

		} else if (ExportableFormField.TYPE_RADIO.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new ChoiceCellWriter(f, (List<?>) value));

		} else if (ExportableFormField.TYPE_SELECTION.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_SPINNER.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_TABLE.equals(type)) {
			int[] pageSize = getPageSize();
			int[] pageMargin = getPageMargin();
			float width = pageSize[0] - pageMargin[1] - pageMargin[3];
			width = WordUtil.mm2halfPt(width);

			int[] internalMargin = getTableInternalMargin();
			width = width - internalMargin[1] - internalMargin[3];

			if (!hideLabel) {
				LabelCellWriter w = new LabelCellWriter(f);
				result.add(w);
				width = width - WordUtil.mm2halfPt(w.getWidthInMM());
			}
			result.add(new TableCellWriter(f, (List<?>) value, (int) width));

		} else if (ExportableFormField.TYPE_TEXT.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_TEXT_HTML.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new HtmlCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_TEXT_MULTILINE.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new ParaCellWriter(f, (String) value));

		} else if (ExportableFormField.TYPE_TEXT_RANGE.equals(type)) {
			if (!hideLabel)
				result.add(new LabelCellWriter(f));
			result.add(new TextCellWriter(f, (String) value));

		} else {
			result.add(CellWriter.BLANK_CELL);
		}
		return result;
	}

	private int caculatePageTableColumns(List<ExportableFormField> formFields) {
		if (formFields == null)
			return 0;
		int[] counts = new int[formFields.size()];
		for (int i = 0; i < formFields.size(); i++) {
			counts[i] = columnCount(formFields.get(i));
		}
		return Formatter.getMinMultiCommonMultiple(counts);// 最小公倍数
	}

	private int columnCount(ExportableFormField f) {
		String type = f.type;
		boolean hideLabel = Boolean.TRUE.equals(f.stdReportLabelHide);
		if (ExportableFormField.TYPE_INLINE.equals(type)) {
			int summary = 0;
			for (int i = 0; i < f.formFields.size(); i++) {
				summary += columnCount(f.formFields.get(i));
			}
			return summary;
		} else if (ExportableFormField.TYPE_BANNER.equals(type)) {
			return 1;
		} else if (ExportableFormField.TYPE_CHECK.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_COMBO.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_DATETIME.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_DATETIME_RANGE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_FILE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_IMAGE_FILE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_LABEL.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_LABEL_MULTILINE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_MULTI_CHECK.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_MULTI_FILE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_MULTI_IMAGE_FILE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_MULTI_SELECTION.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_PAGE.equals(type)) {
			return 1;
		} else if (ExportableFormField.TYPE_PAGE_HTML.equals(type)) {
			return 1;
		} else if (ExportableFormField.TYPE_PAGE_NOTE.equals(type)) {
			return 1;
		} else if (ExportableFormField.TYPE_RADIO.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_SELECTION.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_SPINNER.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_TABLE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_TEXT.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_TEXT_HTML.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_TEXT_MULTILINE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else if (ExportableFormField.TYPE_TEXT_RANGE.equals(type)) {
			return hideLabel ? 1 : 2;
		} else {
			return 1;
		}
	}

	// private void writeParagraphHtml(XWPFDocument docx, String html) {
	// if (html == null || html.isEmpty())
	// return;
	// Document doc = Jsoup.parseBodyFragment(html);
	// Element body = doc.body();
	// Elements es = body.getAllElements();
	// for (int i = 1; i < es.size(); i++) {// 忽略第一个元素
	// createXWPFParagraph(docx, es.get(i));
	// }
	// }
	//
	// private void createXWPFParagraph(XWPFDocument docx, Element e) {
	// XWPFParagraph paragraph = docx.createParagraph();
	// XWPFRun run = paragraph.createRun();
	// run.setText(e.text());
	// run.setTextPosition(35);// 设置行间距
	// String tagName = e.tagName();
	// if (tagName.equals("titlename")) {
	// paragraph.setAlignment(ParagraphAlignment.CENTER);// 对齐方式
	// run.setBold(true);// 加粗
	// // run.setColor("000000");// 设置颜色--十六进制
	// // run.setFontFamily("宋体");// 字体
	// // run.setFontSize(24);// 字体大小
	// } else if (tagName.equals("h1")) {
	// paragraph.setStyle("标题 1");
	// // run.setBold(true);
	// // run.setColor("000000");
	// // run.setFontFamily("宋体");
	// // run.setFontSize(20);
	// } else if (tagName.equals("h2")) {
	// paragraph.setStyle("标题 2");
	// // run.setBold(true);
	// // run.setColor("000000");
	// // run.setFontFamily("宋体");
	// // run.setFontSize(18);
	// } else if (tagName.equals("h3")) {
	// paragraph.setStyle("标题 3");
	// // run.setBold(true);
	// // run.setColor("000000");
	// // run.setFontFamily("宋体");
	// // run.setFontSize(16);
	// } else if (tagName.equals("p")) { // 内容
	// // paragraph.setAlignment(ParagraphAlignment.BOTH);// 对齐方式
	// paragraph.setIndentationFirstLine(WordUtil.ONE_CHAR * 2);// 首行缩进：567==1厘米
	// // run.setBold(false);
	// // run.setColor("001A35");
	// // run.setFontFamily("宋体");
	// // run.setFontSize(14);
	// // run.addCarriageReturn();//回车键
	// } else if (tagName.equals("break")) {
	// paragraph.setPageBreak(true);// 段前分页(ctrl+enter)
	// }
	// }
	//
	// private void writeParagraphText(XWPFDocument docx, String text) {
	// if (!text.isEmpty()) {
	// // 添加段落内容
	// Stream.of(text.split("\n")).forEach(s -> {
	// XWPFParagraph p = docx.createParagraph();
	// p.setIndentationFirstLine(WordUtil.ONE_CHAR * 2);
	// XWPFRun run = p.createRun();
	// run.setText(s);
	// });
	// }
	// }

	private void writeStyleParagraphText(XWPFDocument docx, String text, String style) {
		if (!text.isEmpty()) {
			XWPFParagraph p = docx.createParagraph();
			p.setStyle(style);
			XWPFRun run = p.createRun();
			run.setFontSize(9);
			run.setText(text);
		}
	}

	private void preTreatment(XWPFDocument docx) throws Exception {
		int[] pageSize = getPageSize();
		int[] pageMargin = getPageMargin();
		int contentWidth = (int) WordUtil.mm2halfPt(pageSize[0] - pageMargin[1] - pageMargin[3]);

		////////////////////////////////////////////////////////////////////////
		// 页面尺寸，纸张方向等标准属性
		// 设置页眉距离页面顶端
		int hMar = getHeaderMargin();
		// 设置页脚距离页面底端
		int fMar = getFooterMargin();
		WordUtil.setPage(docx, pageSize, pageMargin, hMar, fMar);

		////////////////////////////////////////////////////////////////////////
		// 创建默认的页眉
		createHeader(docx, contentWidth);

		////////////////////////////////////////////////////////////////////////
		// 创建默认的页脚
		createFooter(docx, contentWidth);
	}

	private void createHeader(XWPFDocument docx, int contentWidth) throws IOException, InvalidFormatException {
		XWPFHeader header = docx.createHeader(HeaderFooterType.DEFAULT);
		XWPFTable tbl = header.createTable(1, 2);
		// 去掉边框
		tbl.setWidth(contentWidth);
		WordUtil.removeTableBorder(tbl);
		tbl.setBottomBorder(XWPFBorderType.SINGLE, 12, 0, "00529e");

		XWPFRun pRun;
		XWPFTableRow row = tbl.getRow(0);
		XWPFTableCell cell = row.getCell(0);
		// 第一个格子放置公司logo
		String fileName = (String) getFieldValue.apply(STATIC_FIELD_LOGO_FILENAME);
		InputStream is = (InputStream) getFieldValue.apply(STATIC_FIELD_LOGO_INPUTSTREAM);
		if (fileName != null || is != null) {
			int format = WordUtil.getImageFormat(fileName);
			if (format != 0) {
				BufferedImage image = ImageIO.read(is);
				is.close();
				int srcWidth = image.getWidth(); // 源图宽度
				int srcHeight = image.getHeight(); // 源图高度
				float aspectRadio = 1f * srcHeight / srcWidth;// 获得纵横比
				// 高度为8.5毫米
				double heightInPixel = WordUtil.mm2px(DEFAULT_LOGO_HEIGHT);
				double widthInPixel = heightInPixel / aspectRadio;
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(image, "png", os);
				is = new ByteArrayInputStream(os.toByteArray());
				pRun = cell.getParagraphArray(0).createRun();
				pRun.addPicture(is, format, fileName, Units.toEMU(widthInPixel), Units.toEMU(heightInPixel));
				is.close();
			}
		}

		// 第二个格子第一行放置文档标准号
		cell = row.getCell(1);
		WordUtil.setCellHorizontalAlign(cell, STJc.RIGHT);
		XWPFParagraph para = cell.getParagraphArray(0);
		pRun = para.createRun();
		WordUtil.setXWPFRunStyle(pRun, "Consolas", 8);
		String value = Optional.ofNullable((String) getFieldValue.apply(FIELD_DOC_STD_NUM)).orElse(" ");// 如果没有值要把这一行撑开
		pRun.setText(value);
		// 第二个各自第二行放置文档名称+版本号
		pRun.addBreak();
		value = Optional.ofNullable((String) getFieldValue.apply(FIELD_DOC_NAME)).orElse(" ");// 如果没有值要把这一行撑开
		pRun = para.createRun();
		WordUtil.setXWPFRunStyle(pRun, "黑体", 10);
		pRun.setText(value);
	}

	private void createFooter(XWPFDocument docx, int contentWidth) {
		XWPFTable tbl;
		XWPFFooter footer = docx.createFooter(HeaderFooterType.DEFAULT);
		tbl = footer.createTable(1, 3);
		// 去掉边框
		tbl.setWidth(contentWidth);
		WordUtil.removeTableBorder(tbl);
		tbl.setTopBorder(XWPFBorderType.SINGLE, 12, 0, "00529e");
		XWPFTableRow row = tbl.getRow(0);
		// 第一个格子放置文件编号
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphArray(0);
		String value = (String) getFieldValue.apply(FIELD_DOCNUM);
		if (value != null) {
			XWPFRun run = para.createRun();
			WordUtil.setXWPFRunStyle(run, "Consolas", 10);
			run.setText(value);
		}
		// 中间格子放页码
		cell = row.getCell(1);
		WordUtil.setCellHorizontalAlign(cell, STJc.CENTER);
		para = cell.getParagraphArray(0);
		WordUtil.createPageNum(para);

		// 第三个格子放置公司名称
		cell = row.getCell(2);
		WordUtil.setCellHorizontalAlign(cell, STJc.RIGHT);
		para = cell.getParagraphArray(0);
		value = (String) getFieldValue.apply(STATIC_FIELD_COMPANY_NAME);
		if (value != null) {
			XWPFRun run = para.createRun();
			WordUtil.setXWPFRunStyle(run, "黑体", 10);
			run.setText(value);
		}
	}

}
