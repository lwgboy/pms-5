package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.Query;
import com.mongodb.BasicDBObject;

@PersistenceCollection("projectSet")
public class ProjectSet {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/**
	 * 编号
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String id;

	@ImageURL("name")
	private String icon = "/img/project_set_c.svg";

	/**
	 * 工作令号
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String workOrder;

	/**
	 * 上级项目集Id
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId parent_id;

	/**
	 * EPS节点Id
	 */
	@Persistence
	private ObjectId eps_id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 描述属性
	/**
	 * 名称
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String name;

	/**
	 * 描述
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "项目集";

	@Persistence
	private ObjectId obs_id;

	@Persistence
	private ObjectId cbs_id;

	@Persistence
	private ObjectId wbs_id;

	public ProjectSet setEps_id(ObjectId eps_id) {
		this.eps_id = eps_id;
		return this;
	}

	public ProjectSet setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	private List<Object> chileren;

	@Structure("EPS浏览 /list")
	public List<Object> getSubProjectSetsAndProjects() {
		if (chileren == null) {
			chileren = new ArrayList<Object>();

			chileren.addAll(ServicesLoader.get(ProjectSetService.class)
					.createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson()));

			chileren.addAll(ServicesLoader.get(ProjectService.class)
					.createDataSet(new Query().filter(new BasicDBObject("projectSet_id", _id)).bson()));
		}
		return chileren;
	}

	@Structure("EPS浏览/count")
	public long countSubProjectSetsAndProjects() {
		// 查下级
		long cnt = ServicesLoader.get(ProjectService.class).count(new BasicDBObject("projectSet_id", _id));
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
		return cnt;
	}

	@Structure("EPS和项目集选择 /list")
	public List<ProjectSet> getSubProjectSets() {
		return ServicesLoader.get(ProjectSetService.class)
				.createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson());
	}

	@Structure("EPS和项目集选择/count")
	public long countSubProjectSets() {
		return ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
	}

	@Behavior("EPS浏览/编辑项目集") // 控制action
	private boolean enableEdit() {
		return true;// TODO 考虑权限
	}

	@Behavior("EPS浏览/创建项目集") // 控制action
	private boolean enableAdd() {
		return true;// TODO 考虑权限
	}

	@Behavior("EPS浏览/删除项目集") // 控制action
	private boolean enableDelete() {
		return true;// TODO 考虑权限
	}

	@Behavior("EPS浏览/打开") // 控制action
	private boolean enableOpen() {
		return true;// TODO 考虑权限
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public ObjectId getOBS_id() {
		return obs_id;
	}///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 计划属性

	/**
	 * 计划开始
	 */
	@ReadValue("planStart")
	public Date getPlanStart() {
		Date planStart = null;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				if (planStart == null || planStart.after(((ProjectSet) obj).getPlanStart())) {
					planStart = ((ProjectSet) obj).getPlanStart();
				}
			} else if (obj instanceof Project) {
				if (planStart == null || planStart.after(((Project) obj).getPlanStart())) {
					planStart = ((Project) obj).getPlanStart();
				}
			}
		}
		return planStart;
	}

	/**
	 * 计划完成
	 */
	@ReadValue("planFinish")
	public Date getPlanFinish() {
		Date planFinish = null;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				if (planFinish == null || planFinish.before(((ProjectSet) obj).getPlanFinish())) {
					planFinish = ((ProjectSet) obj).getPlanFinish();
				}
			} else if (obj instanceof Project) {
				if (planFinish == null || planFinish.before(((Project) obj).getPlanFinish())) {
					planFinish = ((Project) obj).getPlanFinish();
				}
			}
		}
		return planFinish;
	}

	/**
	 * 计划工期
	 **/
	@ReadValue
	@GetValue("planDuration")
	public int getPlanDuration() {
		if (getPlanStart() != null && getPlanFinish() != null) {
			return (int) ((getPlanFinish().getTime() - getPlanStart().getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	/**
	 * 计划工时
	 */
	@ReadValue("planWorks")
	public double getPlanWorks() {
		double summaryPlanWorks = 0;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				summaryPlanWorks += ((ProjectSet) obj).getPlanWorks();
			} else if (obj instanceof Project) {
				summaryPlanWorks += ((Project) obj).getPlanWorks();
			}
		}
		return summaryPlanWorks;
	}

	/**
	 * 实际开始
	 */
	@ReadValue("actualStart")
	private Date getActualStart() {
		Date actualStart = null;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				if (((ProjectSet) obj).getActualStart() == null) {
					return null;
				}
				if (actualStart == null || actualStart.after(((ProjectSet) obj).getActualStart())) {
					actualStart = ((ProjectSet) obj).getActualStart();
				}
			} else if (obj instanceof Project) {
				if (actualStart == null || actualStart.after(((Project) obj).getActualStart())) {
					if (((Project) obj).getActualStart() == null) {
						return null;
					}
					actualStart = ((Project) obj).getActualStart();
				}
			}
		}
		return actualStart;
	}

	/**
	 * 实际完成
	 */
	@ReadValue("actualFinish")
	private Date getActualFinish() {
		Date actualFinish = null;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				if (((ProjectSet) obj).getActualFinish() == null) {
					return null;
				}
				if (actualFinish == null || actualFinish.after(((ProjectSet) obj).getActualFinish())) {
					actualFinish = ((ProjectSet) obj).getActualFinish();
				}
			} else if (obj instanceof Project) {
				if (((Project) obj).getActualFinish() == null) {
					return null;
				}
				if (actualFinish == null || actualFinish.after(((Project) obj).getActualFinish())) {
					actualFinish = ((Project) obj).getActualFinish();
				}
			}
		}
		return actualFinish;
	}
	


	@ReadValue("start")
	public Date getStart_date() {
		Date startOn = null;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				if (((ProjectSet) obj).getStart_date() == null) {
					return null;
				}
				if (startOn == null || startOn.after(((ProjectSet) obj).getStart_date())) {
					startOn = ((ProjectSet) obj).getStart_date();
				}
			} else if (obj instanceof Project) {
				if (((Project) obj).getStart_date() == null) {
					return null;
				}
				if (startOn == null || startOn.after(((Project) obj).getStart_date())) {
					startOn = ((Project) obj).getStart_date();
				}
			}
		}
		return startOn;
	}

	@ReadValue("finish")
	public Date getEnd_date() {
		Date finishOn = null;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				if (((ProjectSet) obj).getEnd_date() == null) {
					return null;
				}
				if (finishOn == null || finishOn.after(((ProjectSet) obj).getEnd_date())) {
					finishOn = ((ProjectSet) obj).getEnd_date();
				}
			} else if (obj instanceof Project) {
				if (((Project) obj).getEnd_date() == null) {
					return null;
				}
				if (finishOn == null || finishOn.after(((Project) obj).getEnd_date())) {
					finishOn = ((Project) obj).getEnd_date();
				}
			}
		}
		return finishOn;
	}

	/**
	 * 实际工期 ///TODO 根据计划开始和完成自动计算
	 */
	@ReadValue
	@GetValue("actualDuration")
	public int getActualDuration() {
		if (getActualFinish() != null && getActualStart() != null) {
			return (int) ((getActualFinish().getTime() - getActualStart().getTime()) / (1000 * 3600 * 24));
		} else if (getActualFinish() == null && getActualStart() != null) {
			return (int) (((new Date()).getTime() - getActualStart().getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	/**
	 * 实际工时 //TODO 计划工时的计算
	 */

	@ReadValue("actualWorks")
	public double getActualWorks() {
		double summaryActualWorks = 0;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				summaryActualWorks += ((ProjectSet) obj).getActualWorks();
			} else if (obj instanceof Project) {
				summaryActualWorks += ((Project) obj).getActualWorks();
			}
		}
		return summaryActualWorks;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期完成率 百分比
	@ReadValue("dar")
	public Object getDAR() {
		int planDuration = getPlanDuration();
		if (planDuration != 0) {
			return 1d * getActualDuration() / planDuration;
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工作量完成率 百分比
	@ReadValue("war")
	public Object getWAR() {
		if (getPlanWorks() != 0) {
			return 1d * getActualWorks() / getPlanWorks();
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工时完成率 百分比
	@ReadValue("sar")
	public Object getSAR() {
		int planDuration = getPlanDuration();
		if (planDuration != 0) {
			return 1d * getActualDuration() / planDuration;
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue("cost")
	public Double getCost() {
		double cost = 0;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				cost += ((ProjectSet) obj).getCost();
			} else if (obj instanceof Project) {
				cost += ((Project) obj).getCost();
			}
		}
		return cost;
	}

	@ReadValue("budget")
	public Double getBudget() {
		double budget = 0;
		for (Object obj : getSubProjectSetsAndProjects()) {
			if (obj instanceof ProjectSet) {
				budget += ((ProjectSet) obj).getBudget();
			} else if (obj instanceof Project) {
				budget += ((Project) obj).getBudget();
			}
		}
		return budget;
	}

	@ReadValue("car")
	public Object getCAR() {
		Double budget = getBudget();
		if (budget != null && budget != 0) {
			return getCost() / budget;
		}
		return "--";
	}

	@ReadValue("bdr")
	public Object getBDR() {
		Double budget = getBudget();
		if (budget != null && budget != 0) {
			return (getCost() - budget) / budget;
		}
		return "--";
	}
}
