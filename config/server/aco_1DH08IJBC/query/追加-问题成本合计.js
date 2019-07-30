[ {
	"$lookup" : {
		"from" : "problemCostItem",
		"let" : {
			"p_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ "$problem_id", "$$p_id" ]
				}
			}
		}, {
			"$group" : {
				"_id" : "$problem_id",
				"drAmount" : {
					"$sum" : "$drAmount"
				},
				"crAmount" : {
					"$sum" : "$crAmount"
				}
			}
		}, {
			"$addFields" : {
				"summary" : {
					"$subtract" : [ "$drAmount", "$crAmount" ]
				}
			}
		}, {
			"$project" : {
				"_id" : false
			}
		} ],
		"as" : "cost"
	}
}, {
	"$unwind" : {
		"path" : "$cost",
		"preserveNullAndEmptyArrays" : true
	}
} ]