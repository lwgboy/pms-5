[ {
	"$lookup" : {
		"from" : "obsInTemplate",
		"let" : {
			"scope_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$scope_id", "$$scope_id" ]
					}, {
						"$not" : "$scopeRoot"
					} ]
				}
			}
		}, {
			"$sort" : {
				"seq" : -1.0
			}
		}, {
			"$lookup" : {
				"from" : "user",
				"localField" : "managerId",
				"foreignField" : "userId",
				"as" : "user"
			}
		}, {
			"$unwind" : {
				"path" : "$user",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$addFields" : {
				"text" : {
					"$concat" : [ {
						"$ifNull" : [ {
							"$concat" : [ "$name", " " ]
						}, "" ]
					}, "$roleName", {
						"$ifNull" : [ {
							"$concat" : [ ":", "$user.name" ]
						}, "" ]
					} ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"text" : {
					"$addToSet" : "$text"
				}
			}
		} ],
		"as" : "obsInTemplate"
	}
}, {
	"$unwind" : {
		"path" : "$obsInTemplate",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"obsTeam" : "$obsInTemplate.text"
	}
}, {
	"$project" : {
		"obsInTemplate" : false
	}
} ]