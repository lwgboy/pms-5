[ {
	"$match" : {
		"report_id" : "<report_id>"
	}
}, {
	"$lookup" : {
		"from" : "work",
		"localField" : "work_id",
		"foreignField" : "_id",
		"as" : "work"
	}
}, {
	"$unwind" : {
		"path" : "$work",
		"preserveNullAndEmptyArrays" : false
	}
}, {
	"$lookup" : {
		"from" : "workReport",
		"localField" : "report_id",
		"foreignField" : "_id",
		"as" : "workReport"
	}
}, {
	"$unwind" : {
		"path" : "$workReport",
		"preserveNullAndEmptyArrays" : false
	}
}, {
	"$addFields" : {
		"project_id" : "$workReport.project_id"
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "project_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$unwind" : {
		"path" : "$project",
		"preserveNullAndEmptyArrays" : false
	}
}, {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"project_id" : "$project_id",
			"wbsCode" : "$work.wbsCode"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, "$stage", {
						"$eq" : [ {
							"$indexOfBytes" : [ "$$wbsCode", {
								"$concat" : [ "$wbsCode", "." ]
							} ]
						}, 0.0 ]
					} ]
				}
			}
		} ],
		"as" : "stage"
	}
}, {
	"$addFields" : {
		"pmIds" : {
			"$concatArrays" : [ "$stage.chargerId", [ "$project.pmId" ] ]
		}
	}
}, {
	"$project" : {
		"workReport" : false,
		"project" : false,
		"stage" : false,
		"project_id" : false
	}
} ]