[ {
	"$match" : {
		"_id" : "<taskId>"
	}
}, {
	"$lookup" : {
		"from" : "bpm_CorrelationKeyInfo",
		"localField" : "taskData.processInstanceId",
		"foreignField" : "processInstanceId",
		"as" : "correlationKeyInfo"
	}
}, {
	"$unwind" : {
		"path" : "$correlationKeyInfo"
	}
}, {
	"$lookup" : {
		"from" : "taskDefinition",
		"let" : {
			"pdId" : "$correlationKeyInfo.properties.documentAsMap._id",
			"formName" : "$formName"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$$formName", "$taskName" ]
					}, {
						"$eq" : [ "$$pdId", "$processDefinitionId" ]
					} ]
				}
			}
		} ],
		"as" : "taskDefinition"
	}
}, {
	"$unwind" : {
		"path" : "$taskDefinition"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$taskDefinition"
	}
} ]