[ {
	"$lookup" : {
		"from" : "cbsSubject",
		"let" : {
			"subjectNumber" : "$id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$subjectNumber", "$$subjectNumber" ]
					}, {
						"$eq" : [ {
							"$indexOfBytes" : [ "$id", "<year>" ]
						}, 0 ]
					}, {
						"$in" : [ "$cbsItem_id", "<cbsItem_id>" ]
					} ]
				}
			}
		}, {
			"$group" : {
				"_id" : "$subjectNumber",
				"cost" : {
					"$sum" : "$cost"
				},
				"count" : {
					"$sum" : 1
				}
			}
		} ],
		"as" : "cbsSubject"
	}
}, {
	"$unwind" : {
		"path" : "$cbsSubject",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"cost" : "$cbsSubject.cost",
		"count" : "$cbsSubject.count"
	}
}, {
	"$project" : {
		"cbsSubject" : false
	}
} ]