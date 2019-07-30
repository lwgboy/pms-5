[ {
	"$match" : {
		"scope_id" : "<scope_id>",
		"$nor" : [ {
			"scopeRoot" : true
		} ]
	}
}, {
	"$lookup" : {
		"from" : "obsInTemplate",
		"let" : {
			"roleId" : "$roleId"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$roleId", "$$roleId" ]
					}, {
						"$eq" : [ "$scope_id", "<module_id>" ]
					}, {
						"$not" : "$scopeRoot"
					} ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"count" : {
					"$sum" : 1
				}
			}
		} ],
		"as" : "obsInTemplate"
	}
}, {
	"$unwind" : "$obsInTemplate"
}, {
	"$match" : {
		"obsInTemplate.count" : {
			"$gt" : 0
		}
	}
} ]