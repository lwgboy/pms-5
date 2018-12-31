package com.bizvisionsoft.pms.problem.assembly;

import java.util.Optional;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridItem;

import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.tools.Formatter;

public class CauseLabelProvider extends ColumnLabelProvider {

	String type;

	CauseLabelProvider(String type) {
		this.type = type;
	}

	@Override
	public void update(ViewerCell cell) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		Object element = cell.getElement();
		if (element instanceof String) {
			gridItem.setHeight(36);
			cell.setText("<div class='label_subhead' style='height:100%;padding:6px 6px 6px 8px;'>" + element + "</div>");
			gridItem.setBackground(BruiColors.getColor(BruiColor.Grey_50));
		} else if (element instanceof CauseConsequence) {
			gridItem.setHeight(96);
			CauseConsequence cc = (CauseConsequence) element;
			String name = Optional.ofNullable(cc.getName()).orElse("");
			String description = Optional.ofNullable(cc.getDescription()).orElse("");
			int w = cc.getWeight();
			double p = cc.getProbability();
			StringBuffer sb = new StringBuffer();
			sb.append("<div style='height:96px;width:100%;padding:2px 0px 2px 8px;display:flex;'>");
			sb.append("<div style='display:flex;flex-direction:column;justify-content:space-around;'>");

			sb.append("<div style='border-radius:4px;width:48px;display:flex;flex-direction:column;justify-content:space-around;align-items:center;background:#4957ad;padding:4px;color:white;'>");
			sb.append("<div class='label_caption'>");
			sb.append(w);
			sb.append("</div>");
			sb.append("<div class='label_caption'>»®÷ÿ</div>");
			sb.append("</div>");

			sb.append("<div style='border-radius:4px;width:48px;display:flex;flex-direction:column;justify-content:space-around;align-items:center;background:#4957ad;padding:4px;color:white;'>");
			sb.append("<div class='label_caption'>");
			sb.append(Formatter.getPercentageFormatString(p));
			sb.append("</div>");
			sb.append("<div class='label_caption'>∏≈¬ </div>");
			sb.append("</div>");
			sb.append("</div>");

			sb.append("<div style='flex-grow:1;background:#f9f9f9;padding:0px 8px;border-radius:4px;margin:4px 0px 4px 4px;display:flex;flex-direction:column;justify-content:space-around;'>");
			sb.append("<div class='brui_text_line'>" + name + "</div>" + "<div class='brui_card_text3 label_caption' style='height:48px;'>"
					+ description + "</div>");
			sb.append("</div>");

			sb.append("</div>");
			cell.setText(sb.toString());
		}
	}

}