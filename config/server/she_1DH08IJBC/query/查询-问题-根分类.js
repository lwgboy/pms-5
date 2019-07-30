[ {
	"$match" : {
		"_id" : "<problem_id>"
	}
}, {
	$unwind: {
	    path : "$classifyProblems"
	}
},{
	"$graphLookup" : {
		"from" : "classifyProblem",
		"startWith" : "$classifyProblems._id",
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