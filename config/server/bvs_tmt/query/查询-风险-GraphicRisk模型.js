[ {
	"$match" : {
		"project_id" : "<project_id>"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "work_id",
		"foreignField" : "_id",
		"as" : "work"
	}
}, {
	"$match" : {
		"$expr" : {
			"$not" : {
				"$arrayElemAt" : [ "$work.actualFinish", 0.0 ]
			}
		}
	}
}, {
	"$group" : {
		"_id" : "$rbsItem_id",
		"riskEffect" : {
			"$push" : {
				"work_id" : "$work_id",
				"timeInf" : "$timeInf"
			}
		}
	}
}, {
	"$lookup" : {
		"from" : "rbsItem",
		"localField" : "_id",
		"foreignField" : "_id",
		"as" : "rbsItem"
	}
}, {
	"$project" : {
		"riskEffect" : true,
		"parent_id" : {
			"$arrayElemAt" : [ "$rbsItem.parent_id", 0.0 ]
		},
		"probability" : {
			"$arrayElemAt" : [ "$rbsItem.probability", 0.0 ]
		}
	}
} ]