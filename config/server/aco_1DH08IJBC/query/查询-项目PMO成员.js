[ {
	"$lookup" : {
		"from" : "obs",
		"let" : {
			"scope_id" : "<scopeIdName>"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$scope_id", "$$scope_id" ]
					}, {
						"$eq" : [ "$roleId", "PMO" ]
					} ]
				}
			}
		}, {
			"$graphLookup" : {
				"from" : "obs",
				"startWith" : "$_id",
				"connectFromField" : "_id",
				"connectToField" : "parent_id",
				"as" : "child"
			}
		}, {
			"$unwind" : {
				"path" : "$member",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$unwind" : {
				"path" : "$child",
				"preserveNullAndEmptyArrays" : true
			}
		},

		{
			"$project" : {
				"userId" : {
					"$concatArrays" : [ [ {
						"$ifNull" : [ "$child.managerId", null ]
					} ], {
						"$ifNull" : [ "$child.member", [

						] ]
					}, [ {
						"$ifNull" : [ "$managerId", null ]
					} ], [ {
						"$ifNull" : [ "$member", [

						] ]
					} ] ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"userId" : {
					"$addToSet" : "$userId"
				}
			}
		}, {
			"$project" : {
				"userId" : {
					"$reduce" : {
						"input" : "$userId",
						"initialValue" : [],
						"in" : {
							"$concatArrays" : [ "$$value", "$$this" ]
						}
					}
				}
			}
		}, {
			"$unwind" : "$userId"
		}, {
			"$project" : {
				"_id" : false
			}
		}, {
			"$group" : {
				"_id" : null,
				"userId" : {
					"$addToSet" : "$userId"
				}
			}
		}, {
			"$unwind" : "$userId"
		}, {
			"$project" : {
				"_id" : false
			}
		} ],
		"as" : "obs"
	}
}, {
	"$unwind" : "$obs"
}, {
	"$addFields" : {
		"obsUserId" : "$obs.userId"
	}
}, {
	"$project" : {
		"obs" : false
	}
}, {
	"$match" : {
		"obsUserId" : "<userId>"
	}
} ]