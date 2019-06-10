[ {
	"$match" : {
		"archived" : 0,
		"taskData.status" : {
			"$in" : "<status>"
		},
		"$or" : [ {
			"taskData.actualOwner_id" : "<userId>"
		}, {
			"taskData.status" : "Ready",
			"peopleAssignments.potentialOwners" : "<userId>",
			"peopleAssignments.excludedOwners" : {
				"$ne" : "<userId>"
			}
		} ]
	}
}, {
	"$lookup" : {
		"from" : "bpm_ProcessInstanceInfo",
		"let" : {
			"var" : "$taskData.processInstanceId"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$eq" : [ "$$var", "$_id" ]
				}
			}
		}, {
			"$lookup" : {
				"from" : "bpm_CorrelationKeyInfo",
				"let" : {
					"var1" : "$_id"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$eq" : [ "$$var1", "$processInstanceId" ]
						}
					}
				} ],
				"as" : "meta"
			}
		}, {
			"$unwind" : {
				"path" : "$meta",
				"preserveNullAndEmptyArrays" : true
			}
		}, {
			"$addFields" : {
				"meta" : "$meta.properties.documentAsMap"
			}
		} ],
		"as" : "processInstance"
	}
}, {
	"$unwind" : {
		"path" : "$processInstance"
	}
} ]