[ {
	"$match" : {
		"_id" : "<stage_id>"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"wbsCode" : "$wbsCode",
			"project_id" : "$project_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, {
						"$eq" : [ {
							"$indexOfBytes" : [ "$wbsCode", {
								"$concat" : [ "$$wbsCode", "." ]
							} ]
						}, 0.0 ]
					}, {
						"$not" : "$summary"
					}, {
						"$not" : "$actualFinish"
					} ]
				}
			}
		} ],
		"as" : "children"
	}
}, {
	"$project" : {
		"count" : {
			"$size" : "$children"
		}
	}
} ]