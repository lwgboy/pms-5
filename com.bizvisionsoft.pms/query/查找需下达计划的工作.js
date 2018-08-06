[ {
	"$match" : {
		"project_id" : ObjectId("5b67c9e31bd81d14d09708f4")
	}
}, {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"project_id" : "$project_id",
			"wbsCode" : "$wbsCode"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, {
						"$eq" : [ "$wbsCode", {
							"$arrayElemAt" : [ {
								"$split" : [ "$$wbsCode", "." ]
							}, 0.0 ]
						} ]
					} ]
				}
			}
		} ],
		"as" : "_stage"
	}
}, {
	"$match" : {
		"$or" : [ {
			"chargerId" : {
				"$ne" : null
			}
		}, {
			"assignerId" : {
				"$ne" : null
			}
		} ],
		"distributed" : {
			"$ne" : null
		},
		"actualFinish" : null,
		"stage" : false
	}
}, {
	"$unwind" : {
		"path" : "$_stage",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$match" : {
		"$or" : [ {
			"_stage" : null
		}, {
			"_stage.status" : "进行中"
		} ]
	}
} ]