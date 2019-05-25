[ {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"project_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, {
						"$eq" : [ "$summary", false ]
					} ]
				}
			}
		}, {
			"$addFields" : {
				"planDuration" : {
					"$divide" : [ {
						"$subtract" : [ "$planFinish", "$planStart" ]
					}, 86400000 ]
				},
				"actualDuration" : {
					"$divide" : [ {
						"$subtract" : [ {
							"$ifNull" : [ "$actualFinish", new Date() ]
						}, {
							"$ifNull" : [ "$actualStart", new Date() ]
						} ]
					}, 86400000 ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"actualWorks" : {
					"$sum" : "$actualWorks"
				},
				"planWorks" : {
					"$sum" : "$planWorks"
				},
				"planDuration" : {
					"$sum" : "$planDuration"
				},
				"actualDuration" : {
					"$sum" : "$actualDuration"
				}
			}
		} ],
		"as" : "work"
	}
}, {
	"$unwind" : {
		"path" : "$work",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"summaryActualWorks" : "$work.actualWorks",
		"summaryPlanWorks" : "$work.planWorks",
		"summaryPlanDuration" : "$work.planDuration",
		"summaryActualDuration" : "$work.actualDuration"
	}
}, {
	"$project" : {
		"work" : false
	}
} ]