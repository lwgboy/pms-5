package com.bizvisionsoft.service.model;

import com.mongodb.BasicDBObject;

public class Result {

	public static final int TYPE_ERROR = 999;

	public static final int TYPE_WARNING = 1;

	public static final int TYPE_INFO = 2;

	public static final int CODE_WORK_SUCCESS = 600;

	public static final int CODE_HASCHECKOUTSUB = 601;

	public static final int CODE_UNAUTHORIZED = 602;

	public static final int CODE_UPDATEMANAGEITEM = 603;

	public static final int CODE_UPDATESTAGE = 604;

	public static final int CODE_UPDATEPROJECT = 605;

	public static final int CODE_ERROR = 699;

	public static final int CODE_CBS_DEFF_BUDGET = 901;

	public static final int CODE_CBS_SUCCESS = 900;

	public static final int CODE_CBS_REPEATSUBMIT = 902;

	public static final int CODE_WORKREPORT_HASNOSTATEMENTWORK = 610;

	public static final int CODE_PROJECTCHANGE_NOTASKUSER = 300;

	public static final int CODE_PROJECT_START_NOTAPPROVED = 101;

	public static final int CODE_PROJECT_NOWORK = 102;

	public static final int CODE_PROJECT_NOOBS = 103;

	public static final int CODE_PROJECT_NOWORKROLE = 104;

	public static final int CODE_PROJECT_NOSTOCKHOLDER = 105;

	public static final int CODE_PROJECT_NOCBS = 106;

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
	}

	public static Result finishWarning(String message) {
		Result e = new Result();
		e.code = 100;
		e.message = message;
		e.type = Result.TYPE_WARNING;
		return e;
	}

	public static Result cbsError(String message, int code) {
		Result e = new Result();
		e.code = code;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result cbsSuccess(String message) {
		Result e = new Result();
		e.code = Result.CODE_CBS_SUCCESS;
		e.message = message;
		e.type = Result.TYPE_INFO;
		return e;
	}

	public static Result submitWorkReportError(String message) {
		Result e = new Result();
		e.code = Result.CODE_WORKREPORT_HASNOSTATEMENTWORK;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result submitProjectChangeError(String message) {
		Result e = new Result();
		e.code = Result.CODE_PROJECTCHANGE_NOTASKUSER;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result startProjectError(String message, int code) {
		Result e = new Result();
		e.code = code;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result startProjectWarning(String message, int code) {
		Result e = new Result();
		e.code = code;
		e.message = message;
		e.type = Result.TYPE_WARNING;
		return e;
	}

	public static Result submitCBSSubjectError(String message) {
		Result e = new Result();
		e.code = CODE_CBS_REPEATSUBMIT;
		e.message = message;
		e.type = Result.TYPE_WARNING;
		return e;
	}
}
