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
	// ��ʶ����
	/**
	 * _id
	 */
	@ReadValue
	@WriteValue
	private ObjectId _id;

	/**
	 * ���
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String id;

	@ImageURL("name")
	private String icon = "/img/project_set_c.svg";

	/**
	 * �������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String workOrder;

	/**
	 * �ϼ���Ŀ��Id
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId parent_id;

	/**
	 * EPS�ڵ�Id
	 */
	@Persistence
	private ObjectId eps_id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��������
	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String name;

	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "��Ŀ��";

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

	@Structure("EPS��� /list")
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

	@Structure("EPS���/count")
	public long countSubProjectSetsAndProjects() {
		// ���¼�
		long cnt = ServicesLoader.get(ProjectService.class).count(new BasicDBObject("projectSet_id", _id));
		cnt += ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
		return cnt;
	}

	@Structure("EPS����Ŀ��ѡ�� /list")
	public List<ProjectSet> getSubProjectSets() {
		return ServicesLoader.get(ProjectSetService.class)
				.createDataSet(new Query().filter(new BasicDBObject("parent_id", _id)).bson());
	}

	@Structure("EPS����Ŀ��ѡ��/count")
	public long countSubProjectSets() {
		return ServicesLoader.get(ProjectSetService.class).count(new BasicDBObject("parent_id", _id));
	}

	@Behavior("EPS���/�༭��Ŀ��") // ����action
	private boolean enableEdit() {
		return true;// TODO ����Ȩ��
	}

	@Behavior("EPS���/������Ŀ��") // ����action
	private boolean enableAdd() {
		return true;// TODO ����Ȩ��
	}

	@Behavior("EPS���/ɾ����Ŀ��") // ����action
	private boolean enableDelete() {
		return true;// TODO ����Ȩ��
	}

	@Behavior("EPS���/��") // ����action
	private boolean enableOpen() {
		return true;// TODO ����Ȩ��
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public ObjectId getOBS_id() {
		return obs_id;
	}///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// �ƻ�����

	/**
	 * �ƻ���ʼ
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
	 * �ƻ����
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
	 * �ƻ�����
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
	 * �ƻ���ʱ
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
	 * ʵ�ʿ�ʼ
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
	 * ʵ�����
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
	 * ʵ�ʹ��� ///TODO ���ݼƻ���ʼ������Զ�����
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
	 * ʵ�ʹ�ʱ //TODO �ƻ���ʱ�ļ���
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
	// ��������� �ٷֱ�
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
	// ����������� �ٷֱ�
	@ReadValue("war")
	public Object getWAR() {
		if (getPlanWorks() != 0) {
			return 1d * getActualWorks() / getPlanWorks();
		}
		return "--";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ʱ����� �ٷֱ�
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
