[ {
	"$match" : {
		"_id" : "<_id>"
	}
}, {
	"$graphLookup" : {
		"from" : "folder",
		"startWith" : "$_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "paths",
		"depthField" : "level"
	}
}, {
	"$unwind" : {
		"path" : "$paths"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$paths"
	}
} ]