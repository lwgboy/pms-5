package com.bizvisionsoft.service.model;

public class Result {
	
	public static final int TYPE_ERROR = 0;
	
	public static final int TYPE_WARNING = 1;

	public static final int TYPE_INFO = 2;

	public static final int CODE_SUCCESS = 600;

	public static final int CODE_HASCHECKOUTSUB = 601;
	
	public static final int CODE_UNAUTHORIZED = 602;
	
	public int code;

	public String message;

	public int type;

	public static final Result updateFailure(String message) {
		Result e = new Result();
		e.code = 0x301;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result checkOutSchedulePlan(String message,int type) {
		Result e = new Result();
		e.code = 0x601;
		e.message = message;
		e.type = type;
		return e;
	};

}
