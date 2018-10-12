/* TODO 缺少获取PMO团队下级角色和团队*/
[ {
	"$lookup" : {
		"from" : "obs",
		"let" : {
			"scope_id" : "$_id"
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
			"$project" : {
				"userId" : {
					"$concatArrays" : [ [ {
						"$ifNull" : [ "$managerId", null ]
					} ], {
						"$ifNull" : [ "$member", [

						] ]
					} ]
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
}，, 
{ 
    "$match" : {
        "obsUserId" : "<userId>"
    }
} ]