package com.bizvisionsoft.serviceimpl.renderer;

import com.bizvisionsoft.service.model.Work;

public class CardTheme {

	String headBgColor;
	String headFgColor;
	String contrastBgColor;
	String contrastFgColor;
	String lightText = "9e9e9e";
	String emphasizeText = "616161";

	public CardTheme(Work work) {
		switch (work.getWarningLevel()) {
		case Work.warningLevelDelayed:
			headBgColor = "e84e40";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
//			contrastBgColor = "fde0dc";
//			contrastFgColor = "f36c60";
			break;
		case Work.warningLevelEstDelay:
		case Work.warningLevelLag:
			headBgColor = "ff9800";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
//			contrastBgColor = "fff3e0";
//			contrastFgColor = "ffb74d";
			break;
		case Work.warningLevelLead:
			headBgColor = "26a69a";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
//			contrastBgColor = "fff3e0";
//			contrastFgColor = "ffb74d";
			break;
		default:
			headBgColor = "5c6bc0";
			headFgColor = "ffffff";
			contrastBgColor = "e1f5fe";
			contrastFgColor = "03a9f4";
			break;
		}
	}

	public CardTheme(String color) {
		if ("deepGrey".equalsIgnoreCase(color)) {
			headBgColor = "78909c";
			headFgColor = "ffffff";
			contrastBgColor = "eceff1";
			contrastFgColor = "90a4ae";
		} else if ("lightGrey".equalsIgnoreCase(color)) {
			headBgColor = "eeeeee";
			headFgColor = "000000";
			contrastBgColor = "eeeeee";
			contrastFgColor = "999999";
		} else {
			headBgColor = "3f51b5";
			headFgColor = "ffffff";
			contrastBgColor = "81d4fa";
			contrastFgColor = "0091ea";
		}
	}

}
