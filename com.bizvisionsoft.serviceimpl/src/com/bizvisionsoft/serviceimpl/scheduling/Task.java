package com.bizvisionsoft.serviceimpl.scheduling;

import java.util.ArrayList;
import java.util.List;

public class Task implements Comparable<Task> {

	public static final String START = "#START";

	public static final String END = "#END";

	private final String id;
	
	private String name;
	
	public Task setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * 工期
	 */
	private Float D;

	/**
	 * 最早开始
	 */
	private Float ES;

	/**
	 * 最早完成
	 */
	private Float EF;

	/**
	 * 最晚开始
	 */
	private Float LS;

	/**
	 * 最晚完成
	 */
	private Float LF;

	/**
	 * 总时差
	 */
	private Float TF;

	/**
	 * 自由时差
	 */
	private Float FF;

	/**
	 * 关键概率
	 */
	private Float ACP;

	/**
	 * 进度风险指标
	 */
	private Float ACI;

	private List<Task> subTasks = new ArrayList<>();

	public Task(String id, float d) {
		this.id = id;
		this.setD(d);
	}

	public Task(String id) {
		this.id = id;
	}

	public void setSubTasks(List<Task> subTasks) {
		this.subTasks = subTasks;
		this.setD(null);
	}
	
	public Task addSubTask(Task subTask) {
		this.subTasks.add(subTask);
		return this;
	}

	public List<Task> getSubTasks() {
		return subTasks;
	}

	public static Task startTask() {
		return new Task(START, 0);
	}

	public static Task endTask() {
		return new Task(END, 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", name=" + name + ", D=" + D + "]";
	}

	void updateES(float es) {
		if (getES() == null || es > getES()) {
			this.setES(es);
			this.setEF(es + getD());
		}
	}

	public void updateLF(float lf) {
		if (getLF() == null || lf < getLF()) {
			this.setLF(lf);
			this.setLS(lf - getD());
		}
	}

	public void updateFF(float lag) {
		if (getFF() == null || lag < getFF()) {
			setFF(lag);
		}
	}

	@Override
	public int compareTo(Task o) {
		if (id.equals(START) || o.id.equals(END)) {
			return -1;
		} else if (id.equals(END) || o.id.equals(START)) {
			return 1;
		}
		return getId().compareTo(o.getId());
	}

	public String getId() {
		return id;
	}

	public Float getACI() {
		return ACI;
	}

	public void setACI(Float aCI) {
		ACI = aCI;
	}

	public Float getACP() {
		return ACP;
	}

	public void setACP(Float aCP) {
		ACP = aCP;
	}

	public Float getTF() {
		return TF;
	}

	public void setTF(Float tF) {
		TF = tF;
	}

	public Float getFF() {
		return FF;
	}

	public void setFF(Float fF) {
		FF = fF;
	}

	public Float getLS() {
		return LS;
	}

	public void setLS(Float lS) {
		LS = lS;
	}

	public Float getES() {
		return ES;
	}

	public void setES(Float eS) {
		ES = eS;
	}

	public Float getLF() {
		return LF;
	}

	public void setLF(Float lF) {
		LF = lF;
	}

	public Float getEF() {
		return EF;
	}

	public void setEF(Float eF) {
		EF = eF;
	}

	public Float getD() {
		return D;
	}

	public void setD(Float d) {
		D = d;
	}

	public void reset() {
		ES = null;
		EF = null;
		LS = null;
		LF = null;
		TF = null;
		FF = null;
		ACI = null;
		ACP = null;
	}

}
