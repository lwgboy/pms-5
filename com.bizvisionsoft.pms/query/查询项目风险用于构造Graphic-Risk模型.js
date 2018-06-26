[ {
	"$match" : {
		"project_id" : "<project_id>"
	}
}, {
	"$lookup" : {
		"from" : "riskEffect",
		"localField" : "_id",
		"foreignField" : "rbsItem_id",
		"as" : "riskEffect"
	}
}, {
	"$project" : {
		"parent_id" : true,
		"probability" : true,
		"riskEffect.work_id" : true,
		"riskEffect.timeInf" : true
	}
} ]