[ {
	"$match" : "<match>"
}, {
	"$lookup" : {
		"from" : "classifyProblemLost",
		"let" : {
			"idlist" : "$classifyProblemLost._ids"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ {
						"$arrayElemAt" : [ "$$idlist", {
							"$subtract" : [ {
								"$size" : "$$idlist"
							}, 1.0 ]
						} ]
					}, "$_id" ]
				}
			}
		} ],
		"as" : "rootAccount"
	}
}, {
	"$unwind" : {
		"path" : "$rootAccount"
	}
}, {
	"$group" : {
		"_id" : {
			"rootAccount" : "$rootAccount.name",
			"period" : {
				"$dateToString" : {
					"date" : "$date",
					"format" : "%Y-%m"
				}
			}
		},
		"drAmount" : {
			"$sum" : "$drAmount"
		},
		"crAmount" : {
			"$sum" : "$crAmount"
		}
	}
}, {
	"$project" : {
		"value" : {
			"$subtract" : [ "$drAmount", "$crAmount" ]
		},
		"series" : "$_id.rootAccount",
		"name" : "$_id.period"
	}
}, {
	"$group" : {
		"_id" : "$series",
		"periods" : {
			"$addToSet" : {
				"k" : "$name",
				"v" : "$value"
			}
		},
		"summary" : {
			"$sum" : "$value"
		}
	}
}, {
	"$addFields" : {
		"data" : {
			"$arrayToObject" : "$periods"
		}
	}
} ]