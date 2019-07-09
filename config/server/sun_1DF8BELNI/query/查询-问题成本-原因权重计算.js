[ {
	"$group" : {
		"_id" : "$problem_id",
		"cause" : {
			"$addToSet" : {
				"weight" : {
					"$multiply" : [ "$probability", "$weight" ]
				},
				"name" : "$classifyCause.path"
			}
		}
	}
}, {
	"$lookup" : {
		"from" : "problemCostItem",
		"let" : {
			"problem_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ "$$problem_id", "$problem_id" ]
				}
			}
		}, {
			"$project" : {
				"amount" : {
					"$subtract" : [ "$drAmount", "$crAmount" ]
				}
			}
		} ],
		"as" : "cost"
	}
}, {
	"$addFields" : {
		"cost" : {
			"$reduce" : {
				"input" : "$cost.amount",
				"initialValue" : 0.0,
				"in" : {
					"$add" : [ "$$value", "$$this" ]
				}
			}
		},
		"weight" : {
			"$reduce" : {
				"input" : "$cause.weight",
				"initialValue" : 0.0,
				"in" : {
					"$add" : [ "$$value", "$$this" ]
				}
			}
		}
	}
}, {
	"$match" : {
		"cost" : {
			"$ne" : 0.0
		}
	}
}, {
	"$unwind" : {
		"path" : "$cause"
	}
}, {
	"$addFields" : {
		"cost" : {
			"$ceil" : {
	            "$cond":[
		            {"$eq":["$weight",0]},
		            0,
		            {"$divide" : [ {
		                "$multiply" : [ "$cost", "$cause.weight" ]
		            }, "$weight" ]}
		            ]
		        }
		}
	}
}, {
	"$group" : {
		"_id" : "$cause.name",
		"amount" : {
			"$sum" : "$cost"
		}
	}
}, {
	"$match" : {
		"_id" : {
			"$ne" : null
		}
	}
}, {
	"$sort" : {
		"_id" : 1.0
	}
} ]