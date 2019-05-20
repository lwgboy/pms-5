[ {
	"$match" : "<match>"
}, {
	"$graphLookup" : {
		"from" : "organization",
		"startWith" : "$org_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "org"
	}
}, {
	"$unwind" : {
		"path" : "$org",
		"includeArrayIndex" : "idx"
	}
}, {
	"$match" : {
		"org.type" : "<orgType>"
	}
} ]