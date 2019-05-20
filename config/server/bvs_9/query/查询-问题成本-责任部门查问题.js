[ {
	"$graphLookup" : {
		"from" : "organization",
		"startWith" : "$_id",
		"connectFromField" : "_id",
		"connectToField" : "parent_id",
		"as" : "children"
	}
}, {
	"$addFields" : {
		"_id" : {
			"$concatArrays" : [ [ "$_id" ], "$children._id" ]
		}
	}
}, {
	"$lookup" : {
		"from" : "problem",
		"localField" : "_id",
		"foreignField" : "dept_id",
		"as" : "problem"
	}
}, {
	"$unwind" : {
		"path" : "$problem"
	}
} ]