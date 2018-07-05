[ {
	"$match" : {
		"summary" : false,
		"project_id" : "<project_id>",
		"actualStart" : {
			"$ne" : null
		}, "$and":[{"$or" : [ {
			"actualFinish" : null
		}, {
			"actualFinish" : {"$gte" : "<actualFinish>"}
		} ]}, {"$or" : [ {
			"chargerId" : "<chargerid>"
		}, {
			"assignerId" : "<chargerid>"
		} ]}]
		
	}
}, {
	"$project" : {
		"work_id" : "$_id",
		"estimatedFinish" : "$estimatedFinish",
		"_id" : false
	}
}, {
	"$addFields" : {
		"report_id" : "<report_id>",
		"reportorId" : "<reportorId>"
	}
} ]