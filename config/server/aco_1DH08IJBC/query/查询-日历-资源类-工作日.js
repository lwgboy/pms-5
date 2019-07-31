[ {
	"$lookup" : {
		"from" : "resourceType",
		"localField" : "_id",
		"foreignField" : "cal_id",
		"as" : "resourceType"
	}
}, {
	"$unwind" : "$resourceType"
}, {
	"$addFields" : {
		"resTypeId" : "$resourceType._id"
	}
}, {
	"$project" : {
		"resourceType" : false
	}
}, {
	"$match" : {
		"resTypeId" : "<resTypeId>"
	}
}, {
	"$unwind" : "$workTime"
}, {
	"$addFields" : {
		"date" : "$workTime.date",
		"workingDay" : "$workTime.workingDay",
		"day" : "$workTime.day"
	}
}, {
	"$project" : {
		"workTime" : false
	}
}, {
	"$unwind" : {
		"path" : "$day",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"workdate" : {
			"$cond" : [ {
				"$eq" : [ "$date", "<date>" ]
			}, true, false ]
		},
		"workday" : {
			"$eq" : [ "$day", "<week>" ]
		}
	}
}, {
	"$project" : {
		"workdate" : true,
		"workday" : true,
		"workingDay" : true
	}
}, {
	"$match" : {
		"$or" : [ {
			"workdate" : true
		}, {
			"workday" : true
		} ]
	}
}, {
	"$sort" : {
		"workdate" : -1,
		"workingDay" : -1
	}
} ]