[ {
	"$match" : {
		"summary" : false,
		"distributed" : true,
		"stage" : {
			"$ne" : true
		}
	}
}, {
	"$lookup" : {
		"from" : "workReport",
		"let" : {
			"chargerId" : "$chargerId",
			"assignerId" : "$assignerId"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$or" : [ {
						"$eq" : [ "$reporter", "$$chargerId" ]
					}, {
						"$eq" : [ "$reporter", "$$assignerId" ]
					} ]
				}
			}
		} ],
		"as" : "workReport"
	}
}, {
	"$unwind" : "$workReport"
}, {
	"$addFields" : {
		"workReport_id" : "$workReport._id",
		"period" : "$workReport.period",
		"finish" : {
			"$eq" : [ {
				"$dateToString" : {
					"format" : "%Y-%m-%d",
					"timezone" : "GMT",
					"date" : "$actualFinish"
				}
			}, {
				"$dateToString" : {
					"format" : "%Y-%m-%d",
					"timezone" : "GMT",
					"date" : "$workReport.period"
				}
			} ]
		}
	}
}, {
	"$match" : {
		"workReport_id" : "<workReport_id>",
		"$or" : [ {
			"actualFinish" : null
		}, {
			"finish" : true
		} ]
	}
}, {
	"$lookup" : {
		"from" : "workInReport",
		"let" : {
			"work_id" : "$_id",
			"workReport_id" : "$workReport_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$work_id", "$$work_id" ]
					}, {
						"$eq" : [ "$workReport_id", "$$workReport_id" ]
					} ]
				}
			}
		} ],
		"as" : "workInReport"
	}
}, {
	"$unwind" : {
		"path" : "$workInReport",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"estimatedFinish" : "$workInReport.estimatedFinish",
		"executionInfo" : "$workInReport.executionInfo",
		"question" : "$workInReport.question"
	}
}, {
	"$project" : {
		"workReport" : false,
		"workInReport" : false
	}
} ]