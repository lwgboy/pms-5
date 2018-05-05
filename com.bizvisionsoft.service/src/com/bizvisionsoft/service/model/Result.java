package com.bizvisionsoft.service.model;

public class Result {
	
	public static final int TYPE_ERROR = 0;
	
	public static final int TYPE_WARNING = 1;

	public static final int TYPE_INFO = 2;


	public int code;

	public String message;

	public int type;

	public static final Result updateFailure(String message) {
		Result e = new Result();
		e.code = 0x301;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	};

}
