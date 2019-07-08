[  {
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
					}, {
						"$gte" : [ "$date", "<起始时间>" ]
					}, {
						"$lte" : [ "$date", "<终止时间>" ]
					} ]
				}
			}
		}, {
			"$project" : {
				"date" : {
					"$dateToString" : {
						"date" : "$date",
						"format" : "<日期分组格式>"
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
	"$group" : {
		"_id" : "$_id.name",
		"source" : {
			"$addToSet" : {
				"_id" : "$_id.date",
				"amount" : "$amount"
			}
		}
	}
}, {
	"$addFields" : {
		"amount" : {
			"$reduce" : {
				"input" : "$source.amount",
				"initialValue" : 0.0,
				"in" : {
					"$add" : [ "$$value", "$$this" ]
				}
			}
		}
	}
} ]