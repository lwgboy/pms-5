[ {
	"$match" : {
		"status" : "已关闭"
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
					"$eq" : [ "$problem_id", "$$problem_id" ]
				}
			}
		}, {
			"$project" : {
				"value" : {
					"$subtract" : [ "$drAmount", "$crAmount" ]
				}
			}
		} ],
		"as" : "cost"
	}
}, {
	"$unwind" : {
		"path" : "$cost"
	}
}, {
	"$group" : {
		"_id" : {
			"_id" : "$classifyProblem._id",
			"path" : "$classifyProblem.path"
		},
		"value" : {
			"$sum" : "$cost.value"
		}
	}
}, {
	"$group" : {
		"_id" : null,
		"value" : {
			"$sum" : "$value"
		},
		"items" : {
			"$push" : {
				"_id" : "$_id",
				"value" : "$value"
			}
		}
	}
}, {
	"$unwind" : {
		"path" : "$items"
	}
}, {
	"$project" : {
		"_id" : "$items._id._id",
		"name" : "$items._id.path",
		"value" : {
			"$divide" : [ "$items.value", "$value" ]
		}
	}
}, {
	"$sort" : {
		"_id" : 1.0
	}
} ]