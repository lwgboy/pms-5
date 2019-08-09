[ {
	"$graphLookup" : {
		"from" : "folder",
		"startWith" : "$parent_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "paths",
		"depthField" : "level"
	}
} ]