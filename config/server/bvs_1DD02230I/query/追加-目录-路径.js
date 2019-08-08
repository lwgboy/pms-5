[ {
	"$graphLookup" : {
		"from" : "folder",
		"startWith" : "$parent_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "path",
		"depthField" : "level"
	}
}, {
	"$addFields" : {
		"path" : {
			"$reduce" : {
				"input" : "$path.desc",
				"initialValue" : "",
				"in" : {
					"$concat" : [ "$$value", "/", "$$this" ]
				}
			}
		},
	}
} ]