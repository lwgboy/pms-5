package com.bizvisionsoft.service.model;

public interface ICBSAmount {

	double getBudget(String startPeriod, String endPeriod);

	double getBudget(String period);

	double getCost(String period);

	double getCost(String startPeriod, String endPeriod);
	
	

}
