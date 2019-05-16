package com.bizvisionsoft.service.model;

public interface ICBSAmount {

	double getDurationBudget(String startPeriod, String endPeriod);

	double getPeriodBudget(String period);

	double getPeriodCost(String period);

	double getDurationCost(String startPeriod, String endPeriod);
	
	

}
