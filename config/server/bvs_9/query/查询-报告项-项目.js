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
		"planStart" : "$planStart",
		"planFinish" : "$planFinish",
		"actualStart" : "$actualStart",
		"actualFinish" : "$actualFinish",
		"_id" : false
	}
}, {
	"$addFields" : {
		"report_id" : "<report_id>",
		"reportorId" : "<reportorId>"
	}
} ]