[ {
	"$match" : {
		"_id" : {
			"$in" : "<cbsItem_id>"
		}
	}
}, {
	"$lookup" : {
		"from" : "cbs",
		"let" : {
			"parent_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$parent_id", "$$parent_id" ]
					} ]
				}
			}
		}, {
			"$count" : "count"
		} ],
		"as" : "count"
	}
}, {
	"$unwind" : {
		"path" : "$count",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$match" : {
		"count" : null
	}
}, {
	"$lookup" : {
		"from" : "cbsSubject",
		"let" : {
			"cbsItem_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$cbsItem_id", "$$cbsItem_id" ]
					} ]
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
		"as" : "cbsSubject1"
	}
}, {
	"$unwind" : {
		"path" : "$cbsSubject1",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "cbsSubject",
		"let" : {
			"cbsItem_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$cbsItem_id", "$$cbsItem_id" ]
					}, {
						"$gte" : [ "$id", "<startPeriod>" ]
					}, {
						"$lte" : [ "$id", "<endPeriod>" ]
					} ]
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
		"as" : "cbsSubject2"
	}
}, {
	"$unwind" : {
		"path" : "$cbsSubject2",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"totalCost" : "$cbsSubject1.cost",
		"totalBudget" : "$cbsSubject1.budget",
		"cost" : "$cbsSubject2.cost",
		"budget" : "$cbsSubject2.budget"
	}
}, {
	"$group" : {
		"_id" : null,
		"totalCost" : {
			"$sum" : "$totalCost"
		},
		"totalBudget" : {
			"$sum" : "$totalBudget"
		},
		"cost" : {
			"$sum" : "$cost"
		},
		"budget" : {
			"$sum" : "$budget"
		}
	}
} ]