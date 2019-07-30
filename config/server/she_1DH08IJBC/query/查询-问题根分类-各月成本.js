[ {
	"$match" : {
		"parent_id" : null
	}
}, {
	"$lookup" : {
		"from" : "problem",
		"localField" : "_id",
		"foreignField" : "classifyProblem._ids",
		"as" : "problem"
	}
}, {
	"$unwind" : {
		"path" : "$problem"
	}
}, {
	"$lookup" : {
		"from" : "problemCostItem",
		"let" : {
			"problem_id" : "$problem._id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$problem_id", "$$problem_id" ]
					} ]
				}
			}
		}, {
			"$project" : {
				"date" : {
					"$dateToString" : {
						"date" : "$date",
						"format" : "%Y-%m"
					}
				},
				"amount" : {
					"$subtract" : [ "$drAmount", "$crAmount" ]
				},
				"problem_id" : true
			}
		} ],
		"as" : "costItems"
	}
}, {
	"$unwind" : {
		"path" : "$costItems"
	}
}, {
	"$project" : {
		"name" : true,
		"date" : "$costItems.date",
		"_id" : "$costItems._id",
		"amount" : "$costItems.amount",
		"problem_id" : "$problem._id"
	}
}, {
	"$group" : {
		"_id" : {
			"date" : "$date",
			"name" : "$name"
		},
		"amount" : {
			"$sum" : "$amount"
		}
	}
}, {
	"$sort" : {
		"_id.date" : 1.0
	}
}, {
	"$group" : {
		"_id" : "$_id.name",
		"source" : {
			"$push" : {
				"_id" : "$_id.date",
				"value" : "$amount"
			}
		}
	}
} ]