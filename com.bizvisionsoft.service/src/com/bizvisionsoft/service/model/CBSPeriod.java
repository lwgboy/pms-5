package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("cbsPeriod")
public class CBSPeriod {

	////////////////////////////////////////////////////////////////////////////////////////////////
	// 基本的一些字段

	/** 标识 Y **/
	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private Double cost;

	@ReadValue
	private Double budget;

	@ReadValue
	private String id;

	@ReadValue
	@WriteValue
	private ObjectId cbsItem_id;

	@Exclude
	private Date[] range;

	@WriteValue("期间预算编辑器/period")
	public void writePeriod(Date period) {
		checkRange(period);

		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		this.id = "" + cal.get(Calendar.YEAR);
		this.id += String.format("%02d", cal.get(Calendar.MONTH) + 1);
	}

	public void checkRange(Date period) {
		if (range != null) {
			if (period.before(range[0]) || period.after(range[1])) {
				throw new RuntimeException("超过预算范围限定。要求范围：" + new SimpleDateFormat("yyyy-MM").format(range[0]) + "  "
						+ new SimpleDateFormat("yyyy-MM").format(range[1]));
			}
		}
	}

	@WriteValue("期间预算编辑器/budget")
	public void writeBudget(String _budget) {
		double __budget;
		try {
			__budget = Double.parseDouble(_budget);
		} catch (Exception e) {
			throw new RuntimeException("输入类型错误。");
		}
		if (__budget <= 0) {
			throw new RuntimeException("预算金额需大于零。");
		}
		budget = __budget;
	}

	public CBSPeriod setCBSItem_id(ObjectId cbsItem_id) {
		this.cbsItem_id = cbsItem_id;
		return this;
	}

	public void setRange(Date[] inputRange) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputRange[0]);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date _from = cal.getTime();

		cal = Calendar.getInstance();
		cal.setTime(inputRange[1]);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, 1);
		Date _to = cal.getTime();

		this.range = new Date[] { _from, _to };
	}

	public ObjectId get_id() {
		return _id;
	}

	public Double getBudget() {
		return budget;
	}

	public ObjectId getCbsItem_id() {
		return cbsItem_id;
	}

	public Double getCost() {
		return cost;
	}

	public String getId() {
		return id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public void setBudget(Double budget) {
		this.budget = budget;
	}

	public void setCbsItem_id(ObjectId cbsItem_id) {
		this.cbsItem_id = cbsItem_id;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public void setId(String id) {
		this.id = id;
	}

}
