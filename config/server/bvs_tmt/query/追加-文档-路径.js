[ {
	"$graphLookup" : {
		"from" : "folder",
		"startWith" : "$folder_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "paths",
		"depthField" : "level"
	}
} ]