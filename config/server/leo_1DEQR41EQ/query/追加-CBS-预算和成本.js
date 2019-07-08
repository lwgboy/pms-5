[ {
	"$lookup" : {
		"from" : "cbsPeriod",
		"localField" : "_id",
		"foreignField" : "cbsItem_id",
		"as" : "_period"
	}
}, {
	"$addFields" : {
		"_budget" : {
			"$map" : {
				"input" : "$_period",
				"as" : "itm",
				"in" : {
					"k" : "$$itm.id",
					"v" : "$$itm.budget"
				}
			}
		}
	}
}, {
	"$addFields" : {
		"budgetTotal" : {
			"$sum" : "$_budget.b"
		},
		"budget" : {
			"$arrayToObject" : "$_budget"
		}
	}
}, {
	"$lookup" : {
		"from" : "cbs",
		"localField" : "_id",
		"foreignField" : "parent_id",
		"as" : "_children"
	}
}, {
	"$addFields" : {
		"children" : {
			"$map" : {
				"input" : "$_children._id",
				"as" : "id",
				"in" : "$$id"
			}
		}
	}
}, {
	"$graphLookup" : {
		"from" : "cbs",
		"startWith" : "$_id",
		"connectFromField" : "_id",
		"connectToField" : "parent_id",
		"as" : "child"
	}
}, {
	"$addFields" : {
		"child_id" : {
			"$map" : {
				"input" : "$child._id",
				"as" : "id",
				"in" : "$$id"
			}
		}
	}
}, {
	"$lookup" : {
		"from" : "cbsSubject",
		"let" : {
			"child_id" : "$child_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$in" : [ "$cbsItem_id", "$$child_id" ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"cost" : {
					"$sum" : "$cost"
				},
				"budget" : {
					"$sum" : "$budget"
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
		"cbsSubjectBudget" : "$cbsSubject.budget",
		"cbsSubjectCost" : "$cbsSubject.cost"
	}
}, {
	"$project" : {
		"_period" : false,
		"_budget" : false,
		"_children" : false,
		"child" : false,
		"child_id" : false,
		"cbsSubject" : false
	}
} ]