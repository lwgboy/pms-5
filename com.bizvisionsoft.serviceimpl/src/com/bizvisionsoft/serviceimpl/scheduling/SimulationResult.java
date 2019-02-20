package com.bizvisionsoft.serviceimpl.scheduling;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {

	/**
	 * 总工期
	 */
	public float T;
	
	/**
	 * 所有的任务
	 */
	public List<Task> tasks;

	/**
	 * 已经计算的风险
	 */
	public ArrayList<Risk> risks;

}
