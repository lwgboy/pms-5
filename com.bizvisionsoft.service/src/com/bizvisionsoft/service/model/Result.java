package com.bizvisionsoft.service.model;

import com.mongodb.BasicDBObject;

public class Result {

	public static final int TYPE_ERROR = 0;

	public static final int TYPE_WARNING = 1;

	public static final int TYPE_INFO = 2;

	public static final int CODE_WORK_SUCCESS = 600;

	public static final int CODE_HASCHECKOUTSUB = 601;

	public static final int CODE_UNAUTHORIZED = 602;

	public static final int CODE_UPDATEMANAGEITEM = 603;

	public static final int CODE_ERROR = 699;

	public static final int CODE_CBS_DEFF_BUDGET = 901;

	public static final int CODE_CBS_SUCCESS = 900;

	public int code;

	public String message;

	public int type;

	public BasicDBObject data;

	public static final Result updateFailure(String message) {
		Result e = new Result();
		e.code = 0x301;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result checkoutError(String message, int code) {
		Result e = new Result();
		e.code = code;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	};

	public static Result checkoutSuccess(String message) {
		Result e = new Result();
		e.code = Result.CODE_WORK_SUCCESS;
		e.message = message;
		e.type = Result.TYPE_INFO;
		return e;
	}

	public Result setResultDate(BasicDBObject data) {
		this.data = data;
		return this;
	}

	public static Result finishError(String message) {
		Result e = new Result();
		e.code = 100;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	};

	public static Result cbsError(String message, int code) {
		Result e = new Result();
		e.code = code;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	};

	public static Result cbsSuccess(String message) {
		Result e = new Result();
		e.code = Result.CODE_CBS_SUCCESS;
		e.message = message;
		e.type = Result.TYPE_INFO;
		return e;
	}

}
