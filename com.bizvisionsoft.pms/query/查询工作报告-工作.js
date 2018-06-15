[ {
	"$match" : {
		"summary" : false,
		"project_id" : "<project_id>",
		"actualStart" : {
			"$ne" : null
		},
		"$or" : [ {
			"actualFinish" : null
		}, {
			"actualFinish" : "<actualFinish>"
		} ]
	}
}, {
	"$project" : {
		"work_id" : "$_id",
		"_id" : false
	}
}, {
	"$addFields" : {
		"report_id" : "<report_id>",
		"reportorId" : "<reportorId>"
	}
} ]