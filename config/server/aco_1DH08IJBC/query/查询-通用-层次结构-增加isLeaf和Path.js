[ {
	"$graphLookup" : {
		"from" : "<from>",
		"startWith" : "$parent_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "parents"
	}
}, {
	"$lookup" : {
		"from" : "<from>",
		"localField" : "_id",
		"foreignField" : "parent_id",
		"as" : "children"
	}
}, {
	"$addFields" : {
		"path" : {
			"$reduce" : {
				"input" : "$parents.name",
				"initialValue" : "",
				"in" : {
					"$concat" : [ "$$value", "$$this", " / " ]
				}
			}
		},
		"isLeaf" : {
			"$cond" : [ {
				"$arrayElemAt" : [ "$children", 0.0 ]
			}, false, true ]
		}
	}
}, {
	"$addFields" : {
		"path" : {
			"$concat" : [ "$path", "$name" ]
		},
		"_ids" : {
			"$concatArrays" : [ [ "$_id" ], "$parents._id" ]
		}
	}
}, {
	"$project" : {
		"children" : false,
		"parents" : false
	}
} ]