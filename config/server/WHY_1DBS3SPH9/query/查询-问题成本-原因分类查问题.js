[ {
	"$lookup" : {
		"from" : "causeRelation",
		"localField" : "_id",
		"foreignField" : "classifyCause._ids",
		"as" : "causeRelation"
	}
}, {
	"$unwind" : {
		"path" : "$causeRelation"
	}
}, {
	"$project" : {
		"name" : true,
		"problem._id" : "$causeRelation.problem_id"
	}
} ]