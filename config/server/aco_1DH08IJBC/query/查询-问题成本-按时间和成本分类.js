[ {
	"$graphLookup" : {
		"from" : "classifyProblemLost",
		"startWith" : "$_id",
		"connectFromField" : "_id",
		"connectToField" : "parent_id",
		"as" : "children"
	}
}, {
	"$addFields" : {
		"_id" : {
			"$concatArrays" : [ [ "$_id" ], "$children._id" ]
		}
	}
}, {
	"$lookup" : {
		"from" : "problemCostItem",
		"let" : {
			"classId" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$in" : [ "$classifyProblemLost._id", "$$classId" ]
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
		}, {
			"$group" : {
				"_id" : {
					"date" : "$date",
					"pb" : "$problem_id"
				},
				"total" : {
					"$sum" : "$amount"
				}
			}
		}, {
			"$group" : {
				"_id" : "$_id.date",
				"amount" : {
					"$sum" : "$total"
				}
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
		"_id" : "$costItems._id",
		"amount" : "$costItems.amount"
	}
}, {
	"$group" : {
		"_id" : "$name",
		"source" : {
			"$addToSet" : {
				"_id" : "$_id",
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
}

]