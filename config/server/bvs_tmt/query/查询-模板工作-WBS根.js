[ {
	"$match" : {
		"parent_id" : "<parent_id>",
		"template_id": "<template_id>"
	}
}, {
	"$sort" : {
		"index" : 1
	}
}, {
	"$lookup" : {
		"from" : "obsInTemplate",
		"let" : {
			"template_id" : "$template_id",
			"roleId" : "$chargerRoleId"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$scope_id", "$$template_id" ]
					}, {
						"$eq" : [ "$roleId", "$$roleId" ]
					} ]
				}
			}
		} ],
		"as" : "chargerRole"
	}
}, {
	"$unwind" : {
		"path" : "$chargerRole",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "obsInTemplate",
		"let" : {
			"template_id" : "$template_id",
			"roleId" : "$assignerRoleId"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$scope_id", "$$template_id" ]
					}, {
						"$eq" : [ "$roleId", "$$roleId" ]
					} ]
				}
			}
		} ],
		"as" : "assignerRole"
	}
}, {
	"$unwind" : {
		"path" : "$assignerRole",
		"preserveNullAndEmptyArrays" : true
	}
} ]