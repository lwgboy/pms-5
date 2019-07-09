[ {
	"$match" : {
		"enabled" : true
	}
}, {
	"$lookup" : {
		"from" : "funcPermission",
		"localField" : "roles",
		"foreignField" : "role",
		"as" : "roles"
	}
}, {
	"$unwind" : {
		"path" : "$roles",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "organization",
		"let" : {
			"var" : "$roles.id",
			"var1" : "$roles.type"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$$var1", "组织" ]
					}, {
						"$eq" : [ "$$var", "$id" ]
					} ]
				}
			}
		} ],
		"as" : "_roleorg"
	}
}, {
	"$addFields" : {
		"org" : {
			"$concatArrays" : [ "$organizations", "$_roleorg._id" ]
		}
	}
}, {
	"$graphLookup" : {
		"from" : "organization",
		"startWith" : "$org",
		"connectFromField" : "_id",
		"connectToField" : "parent_id",
		"as" : "allorg"
	}
}, {
	"$addFields" : {
		"org" : {
			"$concatArrays" : [ "$org", "$allorg._id" ]
		}
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "org",
		"foreignField" : "org_id",
		"as" : "_orgUsers"
	}
}, {
	"$match" : {
		"$or" : [ {
			"allUser" : true
		}, {
			"users" : "<userId>"
		}, {
			"$and" : [ {
				"roles.id" : "<userId>",
				"roles.type" : "用户"
			} ]
		}, {
			"_orgUsers.0.userId" : "<userId>"
		} ]
	}
}, {
	"$group" : {
		"_id" : "$_id"
	}
} ]