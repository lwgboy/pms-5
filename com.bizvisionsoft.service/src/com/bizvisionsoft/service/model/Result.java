package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.mongodb.BasicDBObject;

public class Result {

	public static final int TYPE_ERROR = 999;

	public static final int TYPE_WARNING = 1;

	public static final int TYPE_INFO = 2;

	public static final int TYPE_QUESTION = 3;

	public static final int CODE_ERROR = 699;

	public static final int CODE_SUCCESS = 900;

	public static final int CODE_USER_TERMINATE = 901;

	public static final int CODE_USER_IGNORED = 904;

	public static final int CODE_CBS_DEFF_BUDGET = 903;

	public static final int CODE_CBS_REPEATSUBMIT = 902;

	public static final int CODE_FINISH_ERROR = 100;

	public static final int CODE_PROJECT_START_NOTAPPROVED = 101;

	public static final int CODE_PROJECT_NOWORK = 102;

	public static final int CODE_PROJECT_NOOBS = 103;

	public static final int CODE_PROJECT_NOWORKROLE = 104;

	public static final int CODE_PROJECT_NOSTOCKHOLDER = 105;

	public static final int CODE_PROJECT_NOCBS = 106;

	public static final int CODE_PROJECTCHANGE_NOTASKUSER = 300;

	public static final int CODE_WORK_SUCCESS = 600;

	public static final int CODE_HASCHECKOUTSUB = 601;

	public static final int CODE_UNAUTHORIZED = 602;

	public static final int CODE_UPDATEMANAGEITEM = 603;

	public static final int CODE_UPDATESTAGE = 604;

	public static final int CODE_UPDATEPROJECT = 605;

	public static final int CODE_WORKREPORT_HASNOSTATEMENTWORK = 610;

	public static final int CODE_UPDATE_FAILURE = 0x9001;

	public static final int CODE_NOTFOUND = 0x9002;

	public static final int CODE_NOT_ALLOWED = 0x9003;

	public static final int CODE_CHECK_FORMDEF = 0x8001;

	public static final int CODE_CHECK_EXPORTDOCRULE_FORM = 0x8002;

	public static final int CODE_CHECK_EXPORTDOCRULE_EXPORTABLE = 0x8003;

	public static final int CODE_CHECK_REFDEF_FORM = 0x8004;

	public int code;

	public String message;

	public int type;

	public BasicDBObject data;

	public static final Result updateFailure(String message) {
		Result e = new Result();
		e.code = CODE_UPDATE_FAILURE;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result notFoundError(String message) {
		Result e = new Result();
		e.code = CODE_NOTFOUND;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result notAllowedError(String message) {
		Result e = new Result();
		e.code = CODE_NOT_ALLOWED;
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
		e.code = CODE_FINISH_ERROR;
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
		e.code = Result.CODE_SUCCESS;
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

	public static Result error(String message) {
		Result e = new Result();
		e.code = CODE_ERROR;
		e.message = message;
		e.type = Result.TYPE_ERROR;
		return e;
	}

	public static Result warning(String message) {
		Result e = new Result();
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

	public static Result success(String message) {
		Result e = new Result();
		e.code = CODE_SUCCESS;
		e.message = message;
		e.type = Result.TYPE_INFO;
		return e;
	}

	public static Result info(String message) {
		Result e = new Result();
		e.code = CODE_SUCCESS;
		e.message = message;
		e.type = Result.TYPE_INFO;
		return e;
	}

	public static Result question(String message) {
		Result e = new Result();
		e.message = message;
		e.type = Result.TYPE_QUESTION;
		return e;
	}

	public static Result success() {
		return success("");
	}

	public static Result terminated() {
		Result result = info("用户终止");
		result.code = CODE_USER_TERMINATE;
		return result;
	}

	public static Result ignored() {
		Result result = info("忽略");
		result.code = CODE_USER_IGNORED;
		return result;
	}

	@Override
	public String toString() {
		return "[" + getTypeValue() + "] 代码： " + code + " " + message;
	}

	@ImageURL()
	public String getImage() {
		if (type == Result.TYPE_QUESTION) {
			return "/img/question_c.svg";
		} else if (type == Result.TYPE_ERROR) {
			return "/img/error_c.svg";
		} else if (type == Result.TYPE_WARNING) {
			return "/img/warning_c.svg";
		} else if (type == Result.TYPE_INFO) {
			return "/img/info_c.svg";
		}
		return "";
	}

	public String getTypeValue() {
		if (type == Result.TYPE_QUESTION) {
			return "消息";
		} else if (type == Result.TYPE_ERROR) {
			return "错误";
		} else if (type == Result.TYPE_WARNING) {
			return "警告";
		} else if (type == Result.TYPE_INFO) {
			return "消息";
		}
		return "";
	}

}
