[ {
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
	"$project" : {
		"dept" : "$dept_id",
		"cost" : {
			"$reduce" : {
				"input" : "$cost.amount",
				"initialValue" : 0.0,
				"in" : {
					"$add" : [ "$$value", "$$this" ]
				}
			}
		}
	}
}, {
	"$group" : {
		"_id" : "$dept",
		"cost" : {
			"$sum" : "$cost"
		}
	}
}, {
	"$match" : {
		"cost" : {
			"$ne" : 0.0
		}
	}
}, {
	"$lookup" : {
		"from" : "organization",
		"localField" : "_id",
		"foreignField" : "_id",
		"as" : "org"
	}
}, {
	"$unwind" : {
		"path" : "$org"
	}
}, {
	"$project" : {
		"_id" : "$org.fullName",
		"cost" : true
	}
} ]