package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.ServicesLoader;

public class EPSInfo implements Comparable<EPSInfo> {

	public static final String TYPE_EPS = "eps";

	public static final String TYPE_PROJECTSET = "projectset";

	public static final String TYPE_PROJECT = "project";

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	@SetValue
	private ObjectId _id;

	/**
	 * 编号
	 */
	@ReadValue
	@WriteValue
	private String id;

	@ImageURL("name")
	public String getIcon() {
		if (TYPE_EPS.equals(type))
			return "/img/eps_c.svg";
		else if (TYPE_PROJECTSET.equals(type))
			return "/img/project_set_c.svg";
		else
			return "/img/project_c.svg";
	}

	/**
	 * 父节点
	 */
	@ReadValue
	@WriteValue
	private ObjectId parent_id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 描述属性
	/**
	 * 名称
	 */
	@ReadValue
	@WriteValue
	private String name;

	/**
	 * 描述
	 */
	@ReadValue
	@WriteValue
	private String description;

	private List<EPSInfo> children;

	public ObjectId get_id() {
		return _id;
	}

	public EPSInfo setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	@Override
	public int compareTo(EPSInfo o) {
		return id.compareTo(o.id);
	}

	@Structure("EPS浏览-投资分析 /list")
	public List<EPSInfo> listSub() {
		if (children == null) {
			children = ServicesLoader.get(EPSService.class).listSubEPSInfo(_id);
		}
		return children;
	}

	@Structure("EPS浏览-投资分析/count")
	public long countSub() {
		return ServicesLoader.get(EPSService.class).countSubEPSInfo(_id);
	}

	@ReadValue(ReadValue.TYPE)
	private String typeName = "EPS";

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	@SetValue
	List<SalesItem> salesItems;

	public List<SalesItem> getSalesItems() {
		if (salesItems == null || salesItems.size() == 0) {
			salesItems = new ArrayList<SalesItem>();
			listSub().forEach(epsInfo -> {
				salesItems.addAll(epsInfo.getSalesItems());
			});
		}

		Collections.sort(salesItems, new Comparator<SalesItem>() {
			public int compare(SalesItem salesItem1, SalesItem salesItem2) {
				return salesItem1.getId().compareTo(salesItem2.getId());
			}
		});
		return salesItems;
	}

	@SetValue
	List<CBSSubject> cbsSubjects;

	public List<CBSSubject> getCbsSubjects() {
		// TODO
		if (cbsSubjects == null || cbsSubjects.size() == 0) {
			cbsSubjects = new ArrayList<CBSSubject>();
			listSub().forEach(epsInfo -> {
				cbsSubjects.addAll(epsInfo.getCbsSubjects());
			});
		}
		Collections.sort(cbsSubjects, new Comparator<CBSSubject>() {
			public int compare(CBSSubject cbsSubject1, CBSSubject cbsSubject2) {
				return cbsSubject1.getId().compareTo(cbsSubject2.getId());
			}
		});
		return cbsSubjects;
	}

	private Double totalProfit;

	@ReadValue("totalProfit")
	public double getProfitSummary() {
		if (totalProfit == null) {
			totalProfit = 0d;
			List<EPSInfo> listSub = listSub();
			for (EPSInfo epsInfo : listSub) {
				totalProfit += epsInfo.getProfitSummary();
			}
		}
		return totalProfit;
	}

	@SetValue("totalProfit")
	public void setProfitSummary(Object totalProfit) {
		if (totalProfit != null && totalProfit instanceof Number)
			this.totalProfit = ((Number) totalProfit).doubleValue();
		else
			this.totalProfit = null;
	}

	private Double totalCost;

	@ReadValue("totalCost")
	public double getCostSummary() {
		if (totalCost == null) {
			totalCost = 0d;
			List<EPSInfo> listSub = listSub();
			for (EPSInfo epsInfo : listSub) {
				totalCost += epsInfo.getCostSummary();
			}
		}
		return totalCost;
	}

	@SetValue("totalCost")
	public void setCostSummary(Object totalCost) {
		if (totalCost != null && totalCost instanceof Number)
			this.totalCost = ((Number) totalCost).doubleValue();
		else
			this.totalCost = null;
	}

	private String type;

	public EPSInfo setType(String type) {
		this.type = type;
		return this;
	}
	
	public String getType() {
		return type;
	}
	
	@SetValue
	private String status;
	
	public String getStatus() {
		return status;
	}

	@ReadValue("finish")
	@SetValue
	private Date actualFinish;

	@ReadValue("irp")
	public Integer getIRP() {
		double cost = getCostSummary();
		if (salesItems != null && salesItems.size() > 0) {
			double profit = 0d;
			String id = null;
			for (SalesItem salesItem : getSalesItems()) {
				profit += salesItem.getProfit();
				if (cost <= profit) {
					id = salesItem.getId();
					break;
				}
			}
			if (actualFinish != null && id != null) {
				try {
					Calendar bef = Calendar.getInstance();
					Calendar aft = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
					bef.setTime(actualFinish);
					aft.setTime(sdf.parse(id));
					int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
					result += ((aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12);
					return Math.abs(result);
				} catch (ParseException e) {
				}
			}
		}
		return null;
	}

	public double getCost(String period) {
		Double summary = 0d;
		List<CBSSubject> cbsSubjects = getCbsSubjects();
		if (cbsSubjects != null && cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				if (period.equals(cbsSubject.getId())) {
					summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getCost(String startPeriod, String endPeriod) {
		Double summary = 0d;
		List<CBSSubject> cbsSubjects = getCbsSubjects();
		if (cbsSubjects != null && cbsSubjects.size() > 0) {
			for (CBSSubject cbsSubject : cbsSubjects) {
				if (startPeriod.compareTo(cbsSubject.getId()) <= 0 && endPeriod.compareTo(cbsSubject.getId()) >= 0) {
					summary += Optional.ofNullable(cbsSubject.getBudget()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getProfit(String period) {
		Double summary = 0d;
		List<SalesItem> salesItems = getSalesItems();
		if (salesItems != null && salesItems.size() > 0) {
			for (SalesItem salesItem : salesItems) {
				if (period.equals(salesItem.getId())) {
					summary += Optional.ofNullable(salesItem.getProfit()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getProfit(String startPeriod, String endPeriod) {
		Double summary = 0d;
		List<SalesItem> salesItems = getSalesItems();
		if (salesItems != null && salesItems.size() > 0) {
			for (SalesItem salesItem : salesItems) {
				if (startPeriod.compareTo(salesItem.getId()) <= 0 && endPeriod.compareTo(salesItem.getId()) >= 0) {
					summary += Optional.ofNullable(salesItem.getProfit()).orElse(0d);
				}
			}
		}
		return summary;
	}

	public double getROI(String startPeriod, String endPeriod) {
		double profit = getProfit(startPeriod, endPeriod);
		double totalCost = getCostSummary();
		if (totalCost == 0d) {
			return profit;
		}
		return profit / totalCost;
	}
}
