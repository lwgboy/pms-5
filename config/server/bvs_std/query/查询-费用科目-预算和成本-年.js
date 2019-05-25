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
				"_id" : "$id",
				"cost" : {
					"$sum" : "$cost"
				},
				"budget" : {
					"$sum" : "$budget"
				},
				"count" : {
					"$sum" : 1
				}
			}
		}, {
			"$sort" : {
				"_id" : 1
			}
		} ],
		"as" : "cbsSubject"
	}
}, {
	"$sort" : {
		"id" : 1
	}
} ]