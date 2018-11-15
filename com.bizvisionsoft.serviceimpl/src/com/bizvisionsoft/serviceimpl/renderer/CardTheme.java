package com.bizvisionsoft.serviceimpl.renderer;

import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;

public class CardTheme {

	public static final String DEEP_GREY = "deepGrey";

	public static final String LIGHT_GREY = "lightGrey";
	public static final String[] CONTRAST_LIGHT_GREY = {"f5f5f5","616161"}; 

	public static final String RED = "red";
	public static final String[] CONTRAST_RED = {"fde0dc","e00032"}; 

	public static final String CYAN = "cyan";
	public static final String[] CONTRAST_CYAN = {"e0f7fa","00b8d4"}; 

	public static final String ORANGE = "orange";
	public static final String[] CONTRAST_ORANGE = {"fff3e0","ff6d00"}; 

	public static final String TEAL = "teal";
	public static final String[] CONTRAST_TEAL = {"e0f2f1","00bfa5"}; 

	public static final String INDIGO = "indigo";
	public static final String[] CONTRAST_INDIGO = {"e8eaf6","304ffe"}; 

	public static final String BLUE = "blue";
	public static final String[] CONTRAST_BLUE = {"e1f5fe","0091ea"}; 
	
	public static final String[] TEXT_LINE = {"616161","9e9e9e"};
	

	String headBgColor;
	String headFgColor;
	String contrastBgColor;
	String contrastFgColor;
	String lightText = "9e9e9e";
	String emphasizeText = "616161";

	public CardTheme(Project pj) {
		String status = pj.getStatus();
		if (ProjectStatus.Created.equals(status)) {
			apply(TEAL);
		} else if (ProjectStatus.Processing.equals(status)) {
			apply(BLUE);
		} else if (ProjectStatus.Closing.equals(status)) {
			apply(INDIGO);
		} else {
			apply(DEEP_GREY);
		}
	}

	public CardTheme(Work work) {
		switch (work.getWarningLevel()) {
		case Work.warningLevelDelayed:
			apply(RED);
			break;
		case Work.warningLevelEstDelay:
		case Work.warningLevelLag:
			apply(ORANGE);
			break;
		case Work.warningLevelLead:
			apply(TEAL);
			break;
		default:
			apply(INDIGO);
			break;
		}
	}

	public CardTheme(String color) {
		apply(color);
	}

	private void apply(String color) {
		if (DEEP_GREY.equalsIgnoreCase(color)) {
			headBgColor = "78909c";
			headFgColor = "ffffff";
			contrastBgColor = "eceff1";
			contrastFgColor = "90a4ae";
		} else if (LIGHT_GREY.equalsIgnoreCase(color)) {
			headBgColor = "eeeeee";
			headFgColor = "000000";
			contrastBgColor = "eeeeee";
			contrastFgColor = "999999";
		} else if (RED.equalsIgnoreCase(color)) {
			headBgColor = "e84e40";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
		} else if (ORANGE.equalsIgnoreCase(color)) {
			headBgColor = "ff9800";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
		} else if (TEAL.equalsIgnoreCase(color)) {
			headBgColor = "26a69a";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
		} else if (INDIGO.equalsIgnoreCase(color)) {
			headBgColor = "5c6bc0";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
		} else if (CYAN.equalsIgnoreCase(color)) {
			headBgColor = "00bcd4";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
		} else {
			headBgColor = "03a9f4";
			headFgColor = "ffffff";
			contrastBgColor = "81d4fa";
			contrastFgColor = "0091ea";
		}
	}

}
