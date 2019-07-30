[ {
	"$lookup" : {
		"from" : "cbs",
		"let" : {
			"scope_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ "$scope_id", "$$scope_id" ]
				}
			}
		}, {
			"$graphLookup" : {
				"from" : "cbs",
				"startWith" : "$_id",
				"connectFromField" : "_id",
				"connectToField" : "parent_id",
				"as" : "child"
			}
		}, {
			"$unwind" : {
				"path" : "$child",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$addFields" : {
				"cbsChild_id" : {
					"$ifNull" : [ "$child._id", "$_id" ]
				}
			}
		}, {
			"$lookup" : {
				"from" : "cbs",
				"localField" : "cbsChild_id",
				"foreignField" : "parent_id",
				"as" : "cbs"
			}
		}, {
			"$addFields" : {
				"cbsChildSize" : {
					"$size" : "$cbs"
				}
			}
		}, {
			"$match" : {
				"cbsChildSize" : 0.0
			}
		}, {
			"$group" : {
				"_id" : "$scope_id",
				"cbsChild_id" : {
					"$addToSet" : "$cbsChild_id"
				}
			}
		} ],
		"as" : "cbs"
	}
}, {
	"$unwind" : {
		"path" : "$cbs",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"cbsChild_id" : "$cbs.cbsChild_id"
	}
}, {
	"$project" : {
		"cbs" : false
	}
} ]