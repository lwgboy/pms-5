package com.bizvisionsoft.serviceimpl.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Risk {

	/**
	 * 发生的概率
	 */
	public final float probability;

	/**
	 * 次生风险
	 */
	public List<Risk> secondary;

	/**
	 * 后果
	 */
	public List<Consequence> consequences;

	/**
	 * 该风险对整体项目的影响
	 */
	public float T;

	/**
	 * 风险影响指标
	 */
	public Float RCI;

	/**
	 * id
	 */
	public final String id;

	/**
	 * 
	 * @param probability 概率
	 * @param consequences 后果
	 */
	public Risk(String id,float probability, Consequence... consequences) {
		this.id = id;
		this.consequences = new ArrayList<Consequence>();
		this.secondary = new ArrayList<Risk>();

		this.probability = probability;
		this.consequences.addAll(Arrays.asList(consequences));
	}

	/**
	 * 添加次生风险
	 * @param risks 次生风险
	 */
	public void addSecondaryRisks(Risk... risks) {
		secondary.addAll(Arrays.asList(risks));
	}

	public String getId() {
		return id;
	}

}
