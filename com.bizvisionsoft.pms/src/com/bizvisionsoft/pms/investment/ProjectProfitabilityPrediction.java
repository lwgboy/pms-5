package com.bizvisionsoft.pms.investment;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnFooter;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHandler;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderCompare;
import com.bizvisionsoft.annotations.ui.grid.GridRenderInput;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProjectProfitabilityPrediction extends GridPartDefaultRender {

	private static final String type = "��Ŀӯ��Ԥ�����";

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	@GridRenderInput
	private List<Document> input;

	private ObjectId host_id;

	@DataSet("list")
	public List<Document> data() {
		List<Document> result = Services.get(CommonService.class)
				.listStructuredData(new BasicDBObject("host_id", host_id).append("type", type));
		if (result.isEmpty()) {
			result = initStructuredData();
		}
		return result;
	}

	@Init
	private void init() {
		host_id = context.getRootInput(Project.class, false).get_id();
	}

	@GridRenderUICreated
	private void uiCreated() {
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column,
			@MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		Document row = (Document) cell.getElement();
		String name = column.getName();
		Object cellValue = getCellValue(row, name);
		if (isPercentRow(row) || isPercentCell(row, name)) {
			cellValue = Util.getFormatText(cellValue, "0.0%", null);
		}

		super.renderCell(cell, column, cellValue, image);
		if (!inputableCell(row, name)) {
			cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));
		}
	}

	private boolean isPercentCell(Document row, String name) {
		return Arrays.asList("7.1", "7.1.1", "7.1.2", "7.1.3").contains(row.getString("index"))
				&& "��߰��_��һ��".equals(name);
	}

	/**
	 * ʹ�ðٷֱȵ���
	 * 
	 * @param row
	 * @return
	 */
	private boolean isPercentRow(Document row) {
		return Arrays.asList("2.1.1", "2.2.1", "4.1", "6.1").contains(row.getString("index"));
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

	@Override
	@GridRenderColumnFooter
	public void renderColumnFooter(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnFooter(col, column);
	}

	@Override
	@GridRenderCompare
	public int compare(@MethodParam(GridRenderCompare.PARAM_COLUMN) Column col,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT1) Object e1,
			@MethodParam(GridRenderCompare.PARAM_ELEMENT2) Object e2) {
		return super.compare(col, e1, e2);
	}

	private List<Document> initStructuredData() {
		List<Document> result = Arrays.asList(

				createRowData("�����루����˰����������", "1"), createRowData("�������", "1.1"), createRowData("��Ȩ����", "1.2"),
				createRowData("��Ȩ����", "1.3"), createRowData("���ɱ�������˰��", "2"), createRowData("��߳ɱ�", "2.1"),
				createRowData("���ë����", "2.1.1"), createRowData("Ӱ��Ƭ�ɱ�", "2.2"), createRowData("���ë���ʣ���Ӱ��Ƭ ��", "2.2.1"),
				createRowData("��Ȩ�ɱ�", "2.3"), createRowData("��Ȩ�ɱ�", "2.4"), createRowData("Ӫҵ˰�𼰸���", "2.5"),
				createRowData("��̯ǰ����", "3"), createRowData("ǰ�ڵ��м����Ͷ��", "3.1"), createRowData("�̶��ʲ�/���", "3.2"),
				createRowData("ģ��Ͷ��", "3.3"), createRowData("�ְ�Ͷ��", "3.4"), createRowData("�г�����", "3.5"),

				createRowData("��Ʒ�", "3.6"), createRowData("��ѯ��/�����", "3.7"), createRowData("�����̼��/������Է�", "3.8"),
				createRowData("�����", "3.9"), createRowData("�����ճ�Ӫ�˷��ã�н��/����/����/������", "3.10"),
				createRowData("Ӫ��ר��-�����з���Ͷ��", "3.11"), createRowData("��̯ǰ����", "4"), createRowData("��̯ǰ������", "4.1"),
				createRowData("ְ�ܷ�̯����", "4.1.1"), createRowData("Ӫ����̯����", "4.1.2"), createRowData("Ӫ��ר��-����", "4.1.3"),
				createRowData("�����ֵ", "4.1.4"), createRowData("�����ܶ�", "5"), createRowData("��������˰����", "5.1"),
				createRowData("������", "6"),

				createRowData("��������", "6.1"), createRowData("��̯ǰ��߱�������", "7"), createRowData("�䶯�ɱ�������", "7.1"),
				createRowData("������%��̯��ǰ���ã�", "7.1.1"), createRowData("��߳ɱ���%", "7.1.2"),
				createRowData("��Ȩ����%", "7.1.3"), createRowData("�̶����ã��̶�Ͷ��&���ŷ��ã�", "7.2"), createRowData("������Ȩ��", "7.3"),
				createRowData("��ȫ���ɱ�", "7.4"), createRowData("��������˰�����������۶�", "7.5"),
				createRowData("������˰���������۶�", "7.6"), createRowData("Ͷ�ʻر���", "8"),

				createRowData("���ֽ���", "8.1"), createRowData("PV�����ֽ�����ֵ��", "8.2"), createRowData("Ͷ�ʻ�����(��)", "8.3"));

		Services.get(CommonService.class).insertStructuredData(result);
		return result;
	}

	private Document createRowData(String name, String index) {
		return new Document("name", name).append("index", index).append("_id", new ObjectId())
				.append("host_id", host_id).append("type", type);
	}

	private Object getCellValue(Document row, String name) {
		String index = row.getString("index");
		// ��Ŀ��
		if ("��Ŀ".equals(name)) {
			String text = row.getString("name");
			char[] c = index.toCharArray();
			for (int i = 0; i < c.length; i++) {
				if (c[i] == '.') {
					text = "    " + text;
				}
			}
			return text;
		}

		if ("���".equals(name)) {
			return row.getString("index");
		}

		if ("ռ��".equals(name)) {
			if ("1".equals(index)) {
				return 1;
			} else if ("2.1.1".equals(index) || "2.2.1".equals(index)) {
				return "";
			} else {
				return calculateRate(row);
			}
		}
		return getRowValue(row, name);
	}

	private double getRowValue(Document row, String name) {
		String index = row.getString("index");
		if ("1".equals(index)) {
			return getVerticalSummaryByPrefix("1", name);
		}

		if ("1.1".equals(index) || "2.1".equals(index)) {
			if ("��߰��_��һ��".equals(name))
				return getRowValue(row, "Ӫ������_��һ��");
			if ("��߰��_�ڶ���".equals(name))
				return getRowValue(row, "Ӫ������_�ڶ���");
			if ("��߰��_������".equals(name))
				return getRowValue(row, "Ӫ������_������");
			if ("��߰��_�ϲ�".equals(name))
				return getHorizontalSummary(row, "��߰��_��һ��", "��߰��_�ڶ���", "��߰��_������");

			if ("�г�����_��һ��".equals(name))
				return getInputValue(row, name);
			if ("�г�����_�ڶ���".equals(name))
				return getInputValue(row, name);
			if ("�г�����_������".equals(name))
				return getInputValue(row, name);
			if ("�г�����_�ϲ�".equals(name))
				return getHorizontalSummary(row, "�г�����_��һ��", "�г�����_�ڶ���", "�г�����_������");

			if ("Ӫ������_��һ��".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_�ڶ���".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_������".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_�ϲ�".equals(name))
				return getHorizontalSummary(row, "Ӫ������_��һ��", "Ӫ������_�ڶ���", "Ӫ������_������");

		}

		if (Arrays.asList("1.2", "1.3", "2.2", "2.3", "2.4", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8",
				"3.9", "3.A", "3.B", "4.1.1", "4.1.3", "4.1.4").contains(index)) {
			if ("��߰��_��һ��".equals(name))
				return getHorizontalSummary(row, "�г�����_��һ��", "Ӫ������_��һ��");
			if ("��߰��_�ڶ���".equals(name))
				return getHorizontalSummary(row, "�г�����_�ڶ���", "Ӫ������_�ڶ���");
			if ("��߰��_������".equals(name))
				return getHorizontalSummary(row, "�г�����_������", "Ӫ������_������");
			if ("��߰��_�ϲ�".equals(name))
				return getHorizontalSummary(row, "��߰��_��һ��", "��߰��_�ڶ���", "��߰��_������");

			if ("�г�����_��һ��".equals(name))
				return getInputValue(row, name);
			if ("�г�����_�ڶ���".equals(name))
				return getInputValue(row, name);
			if ("�г�����_������".equals(name))
				return getInputValue(row, name);
			if ("�г�����_�ϲ�".equals(name))
				return getHorizontalSummary(row, "�г�����_��һ��", "�г�����_�ڶ���", "�г�����_������");

			if ("Ӫ������_��һ��".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_�ڶ���".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_������".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_�ϲ�".equals(name))
				return getHorizontalSummary(row, "Ӫ������_��һ��", "Ӫ������_�ڶ���", "Ӫ������_������");

		}

		if ("2".equals(index)) {
			return getVerticalSummary(name, "2.1", "2.2", "2.3", "2.4");
		}

		if ("2.1.1".equals(index)) {
			return 1 - calculateDivide(name, "2.1", "1.1");
		}

		if ("2.2.1".equals(index)) {
			double d9 = getRowValue(getRow("2.1"), name);
			double d11 = getRowValue(getRow("2.2"), name);
			double d5 = getRowValue(getRow("1.1"), name);
			if (d5 != 0) {
				return 1 - (d9 + d11) / d5;
			} else {
				return 0;
			}
		}

		if ("2.5".equals(index)) {
			double d4 = getRowValue(getRow("1"), name);
			double d8 = getRowValue(getRow("2"), name);
			return (d4 - d8) * 0.17 * 0.12;
		}

		if ("3".equals(index)) {
			return getVerticalSummaryByPrefix("3", name);
		}

		if ("4".equals(index)) {
			double d4 = getRowValue(getRow("1"), name);
			double d8 = getRowValue(getRow("2"), name);
			double d15 = getRowValue(getRow("2.5"), name);
			double d16 = getRowValue(getRow("3"), name);
			return d4 - d8 - d15 - d16;
		}

		if ("4.1".equals(index)) {
			return calculateDivide(name, "4", "1");
		}

		if ("4.1.2".equals(index)) {
			if ("�г�����_��һ��".equals(name))
				return getInputValue(row, name);
			if ("�г�����_�ڶ���".equals(name))
				return getInputValue(row, name);
			if ("�г�����_������".equals(name))
				return getInputValue(row, name);
			if ("�г�����_�ϲ�".equals(name))
				return getHorizontalSummary(row, "�г�����_��һ��", "�г�����_�ڶ���", "�г�����_������");

			if ("Ӫ������_��һ��".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_�ڶ���".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_������".equals(name))
				return getInputValue(row, name);
			if ("Ӫ������_�ϲ�".equals(name))
				return getHorizontalSummary(row, "Ӫ������_��һ��", "Ӫ������_�ڶ���", "Ӫ������_������");
		}

		if ("5".equals(index)) {
			// =D28-D30-D31-D32-D33
			double d28 = getRowValue(getRow("4"), name);
			double d30 = getRowValue(getRow("4.1.1"), name);
			double d31 = getRowValue(getRow("4.1.2"), name);
			double d32 = getRowValue(getRow("4.1.3"), name);
			double d33 = getRowValue(getRow("4.1.4"), name);
			return d28 - d30 - d31 - d32 - d33;
		}

		if ("5.1".equals(index)) {
			return getRowValue(getRow("5"), name) * 0.15;
		}

		if ("6".equals(index)) {
			double d34 = getRowValue(getRow("5"), name);
			double d35 = getRowValue(getRow("5.1"), name);
			return d34 - d35;
		}

		if ("6.1".equals(index)) {
			return calculateDivide(name, "6", "1");
		}

		if ("��߰��_��һ��".equals(name)) {
			if ("7.1".equals(index)) {
				double d711 = getRowValue(getRow("7.1.1"), name);
				double d712 = getRowValue(getRow("7.1.2"), name);
				double d713 = getRowValue(getRow("7.1.3"), name);
				return d711 + d712 + d713;
			}
			if ("7.1.1".equals(index)) {
				// =IFERROR((G11+G13+G15+G16-D43)/$K$5,0)
				double g11 = getRowValue(getRow("2.2"), "��߰��_�ϲ�");
				double g13 = getRowValue(getRow("2.3"), "��߰��_�ϲ�");
				double g15 = getRowValue(getRow("2.5"), "��߰��_�ϲ�");
				double g16 = getRowValue(getRow("3"), "��߰��_�ϲ�");
				double d43 = getRowValue(getRow("7.2"), "��߰��_��һ��");
				double k5 = getRowValue(getRow("1.1"), "�г�����_�ϲ�");
				if (k5 == 0) {
					return 0d;
				} else {
					return (g11 + g13 + g15 + g16 - d43) / k5;
				}
			}
			if ("7.1.2".equals(index)) {
				// =IFERROR(1-G10,"��Ϣδȫ")
				return 1 - getRowValue(getRow("2.1.1"), "��߰��_�ϲ�");
			}
			if ("7.1.3".equals(index)) {
				return calculateRate(getRow("2.3"));
			}
			if ("7.2".equals(index)) {
				double g11 = getRowValue(getRow("2.2"), "��߰��_�ϲ�");
				double g17 = getRowValue(getRow("3.1"), "��߰��_�ϲ�");
				double g18 = getRowValue(getRow("3.2"), "��߰��_�ϲ�");
				double g19 = getRowValue(getRow("3.3"), "��߰��_�ϲ�");
				double g20 = getRowValue(getRow("3.4"), "��߰��_�ϲ�");
				double g26 = getRowValue(getRow("3.A"), "��߰��_�ϲ�");
				return g11 + g17 + g18 + g19 + g20 + g26;
			}
			if ("7.3".equals(index)) {
				return getInputValue(row, name);
			}

			if ("7.4".equals(index)) {
				double d43 = getRowValue(getRow("7.2"), "��߰��_��һ��");
				double d39 = getRowValue(getRow("7.1"), "��߰��_��һ��");
				double d42 = getRowValue(getRow("7.1.3"), "��߰��_��һ��");
				double d44 = getRowValue(getRow("7.3"), "��߰��_��һ��");
				double c45 = 0.05;// getRowValue(getRow("7.3"), "��߰��_�ϲ�");
				double d41 = getRowValue(getRow("7.1.2"), "��߰��_��һ��");

				if (d43 / (1 - d39) * d42 > d44) {
					double v = (1 - d39) * c45 * d41;
					if (v != 0)
						return d43 / v;
					return 0;
				} else {
					double v = (1 - (d39 - d42));
					if (v != 0) {
						return ((d43 + d44) / v) * c45 * d41;
					} else {
						return 0;
					}
				}
			}

			if ("7.5".equals(index)) {
				// =IFERROR(IF(D43/(1-D39)*D42>D44,D43/(1-D39)+D45,((D43+D44)/(1-(D39-D42)))+D45),"��Ϣδȫ")
				double d43 = getRowValue(getRow("7.2"), "��߰��_��һ��");
				double d39 = getRowValue(getRow("7.1"), "��߰��_��һ��");
				double d42 = getRowValue(getRow("7.1.3"), "��߰��_��һ��");
				double d44 = getRowValue(getRow("7.3"), "��߰��_��һ��");
				double d45 = getRowValue(getRow("7.4"), "��߰��_��һ��");
				if (d43 / (1 - d39) * d42 > d44) {
					if (d39 != 1) {
						return d43 / (1 - d39) + d45;
					} else {
						return 0;
					}
				} else {
					double v = (1 - (d39 - d42));
					if (v != 0) {
						return ((d43 + d44) / v) + d45;
					} else {
						return 0;
					}
				}
			}

			if ("7.6".equals(index)) {
				return 1.17 * getRowValue(getRow("7.5"), "��߰��_��һ��") / (1 - 0.05);
			}

			if ("8.1".equals(index)) {
				// =G4-G8-G15-G16
				double g4 = getRowValue(getRow("1"), "��߰��_�ϲ�");
				double g8 = getRowValue(getRow("2"), "��߰��_�ϲ�");
				double g15 = getRowValue(getRow("2.5"), "��߰��_�ϲ�");
				double g16 = getRowValue(getRow("3"), "��߰��_�ϲ�");
				return g4 - g8 - g15 - g16;
			}

			if ("8.2".equals(index)) {
				//TODO
			}

			if ("8.3".equals(index)) {
				//TODO
			}

		}

		return 0d;
	}

	private double calculateRate(Document row) {
		double s = getRowValue(row, "�г�����_�ϲ�");
		double p = getRowValue(getRow("1"), "�г�����_�ϲ�");
		if (p != 0) {
			return s / p;
		} else {
			return 0;
		}
	}

	private double calculateDivide(String name, String index1, String index2) {
		double i1 = getRowValue(getRow(index1), name);
		double i2 = getRowValue(getRow(index2), name);
		if (i2 != 0) {
			return i1 / i2;
		} else {
			return 0;
		}

	}

	private double getInputValue(Document row, String name) {
		return Optional.ofNullable(row.getDouble(getKey(name))).orElse(0d);
	}

	private double getHorizontalSummary(Document row, String... names) {
		double summary = 0d;
		for (int i = 0; i < names.length; i++) {
			summary += getRowValue(row, names[i]);
		}
		return summary;
	}

	private double getVerticalSummaryByPrefix(String prefix, String name) {
		double summary = 0d;
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i).getString("index").startsWith(prefix + ".")) {
				summary += getRowValue(input.get(i), name);
			}
		}
		return summary;
	}

	private double getVerticalSummary(String name, String... indexs) {
		List<String> list = Arrays.asList(indexs);
		double summary = 0d;
		for (int i = 0; i < input.size(); i++) {
			if (list.contains(input.get(i).getString("index"))) {
				summary += getRowValue(input.get(i), name);
			}
		}
		return summary;
	}

	@GridRenderColumnHandler
	private void enableEditing(@MethodParam("") GridViewerColumn vcol) {
		final String name = (String) vcol.getColumn().getData("name");
		if (inputableCol(name)) {
			final ColumnViewer viewer = vcol.getViewer();
			vcol.setEditingSupport(new EditingSupport(viewer) {

				@Override
				protected void setValue(Object element, Object value) {
					try {
						update((Document) element, name, value);
						viewer.refresh();
					} catch (Exception e) {
						Layer.message(e.getMessage(), Layer.ICON_CANCEL);
					}
				}

				@Override
				protected Object getValue(Object element) {
					return getNumberText(getRowValue((Document) element, name), "0.0");
				}

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor((Grid) viewer.getControl());
				}

				@Override
				protected boolean canEdit(Object element) {
					return inputableCell((Document) element, name);
				}

			});

		}

	}

	private boolean inputableCell(Document element, String name) {
		if ("��߰��_��һ��".equals(name)) {
			return "7.3".equals(element.get("index"));
		}
		return !Arrays
				.asList("1", "2", "2.1.1", "2.2.1", "2.5", "3", "4", "4.1", "5", "5.1", "6", "6.1", "7", "7.1", "7.1.1",
						"7.1.2", "7.1.3", "7.2", "7.3", "7.4", "7.5", "7.6", "8", "8.1", "8.2", "8.3")
				.contains(element.get("index"));
	}

	private boolean inputableCol(String name) {
		return name.equals("��߰��_��һ��") || ((name.startsWith("�г�") || name.startsWith("Ӫ��")) && !name.endsWith("�ϲ�"));
	}

	protected void update(Document row, String name, Object input) throws Exception {
		double v = getDoubleValue(input);
		String key = getKey(name);
		double oldValue = Optional.ofNullable((Double) row.get(key)).orElse(0d);
		if (v == oldValue)
			return;
		row.put(key, v);
		BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", row.get("_id")))
				.set(new BasicDBObject(key, v)).bson();
		Services.get(CommonService.class).updateStructuredData(fu);

	}

	private String getNumberText(Number number, String format) {
		if (number == null || number.doubleValue() == 0)
			return "";
		return Util.getFormatText(number, format, null);
	}

	private double getDoubleValue(Object input) throws Exception {
		double inputAmount;
		try {
			if ("".equals(input)) {
				inputAmount = 0;
			} else {
				inputAmount = Double.parseDouble(input.toString());
			}
		} catch (Exception e) {
			throw new Exception("����������");
		}
		return inputAmount;
	}

	private String getKey(String name) {
		if ("��Ŀ".equals(name))
			return "name";
		if ("���".equals(name))
			return "index";
		if ("ռ��".equals(name))
			return "zb";
		if ("��߰��_��һ��".equals(name))
			return "wjy1";
		if ("��߰��_�ڶ���".equals(name))
			return "wjy2";
		if ("��߰��_������".equals(name))
			return "wjy3";
		if ("��߰��_�ϲ�".equals(name))
			return "wjsum";
		if ("�г�����_��һ��".equals(name))
			return "scy1";
		if ("�г�����_�ڶ���".equals(name))
			return "scy2";
		if ("�г�����_������".equals(name))
			return "scy3";
		if ("�г�����_�ϲ�".equals(name))
			return "scsum";
		if ("Ӫ������_��һ��".equals(name))
			return "yxy1";
		if ("Ӫ������_�ڶ���".equals(name))
			return "yxy2";
		if ("Ӫ������_������".equals(name))
			return "yxy3";
		if ("Ӫ������_�ϲ�".equals(name))
			return "yxsum";
		return null;
	}

	private Document getRow(String index) {
		return input.stream().filter(d -> d.get("index").equals(index)).findFirst().orElse(null);
	}

}
