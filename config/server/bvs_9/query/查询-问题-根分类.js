[ {
	"$match" : {
		"_id" : "<problem_id>"
	}
}, {
	"$graphLookup" : {
		"from" : "classifyProblem",
		"startWith" : "$classifyProblem._id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "root"
	}
}, {
	"$addFields" : {
		"root" : {
			"$arrayElemAt" : [ "$root", 0 ]
		}
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$root"
	}
} ]