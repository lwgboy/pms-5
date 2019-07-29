[ {
	"$lookup" : {
		"from" : "problem",
		"localField" : "_id",
		"foreignField" : "classifyProblem._ids",
		"as" : "problem"
	}
}, {
	"$unwind" : {
		"path" : "$problem"
	}
}, ]