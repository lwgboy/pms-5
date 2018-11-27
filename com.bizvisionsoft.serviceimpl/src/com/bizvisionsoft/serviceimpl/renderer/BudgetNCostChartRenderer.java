package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.serviceimpl.BasicServiceImpl;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;

public class BudgetNCostChartRenderer extends BasicServiceImpl {

	private List<Document> input;
	private Date start;
	private Date end;
	private String dateType;
	private List<String> dataType;
	private String seriesType;
	private boolean aggregate;

	@SuppressWarnings("unchecked")
	public BudgetNCostChartRenderer(Document condition) {
		checkResChartOption(condition);
		Document option = (Document) condition.get("option");
		List<?> dateRange = (List<?>) option.get("dateRange");
		start = (Date) dateRange.get(0);
		end = (Date) dateRange.get(1);
		dateType = option.getString("dateType");
		dataType = (List<String>) option.get("dataType");
		seriesType = option.getString("seriesType");
		aggregate = option.getBoolean("aggregate", false);
		input = ((List<Document>) condition.get("input"));
	}

	public Document render() {
		return new Document();
	}

	/**
	 * 检查图表条件，抛出错误
	 * 
	 * @param condition
	 */
	private void checkResChartOption(Document condition) {
		Object option = condition.get("option");
		if (option instanceof Document) {
			Object dateRange = ((Document) option).get("dateRange");
			if (dateRange instanceof List<?>) {
				if (((List<?>) dateRange).size() == 2) {
					Object d0 = ((List<?>) dateRange).get(0);
					Object d1 = ((List<?>) dateRange).get(1);
					if (!(d0 instanceof Date) || !(d1 instanceof Date) || !((Date) d0).before((Date) d1)) {
						throw new ServiceException("日期范围数据不合法");
					}
				} else {
					throw new ServiceException("日期范围数据不合法");
				}
			} else {
				throw new ServiceException("日期范围类型错误");
			}
		} else {
			throw new ServiceException("选项类型错误");
		}
	}

}
