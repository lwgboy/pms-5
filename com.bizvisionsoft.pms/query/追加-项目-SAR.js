[ {
	"$lookup" : {
		"from" : "work",
		"let" : {
			"project_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, "$stage", "$actualFinish" ]
				}
			}
		}, {
			"$project" : {
				"_sar" : {
					"$divide" : [ {
						"$subtract" : [ "$planFinish", "$planStart" ]
					}, {
						"$subtract" : [ "$actualFinish", "$actualStart" ]
					} ]
				}
			}
		} ],
		"as" : "_stage"
	}
}, {
	"$addFields" : {
		"_sar" : {
			"$avg" : "$_stage._sar"
		}
	}
}, {
	"$addFields" : {
		"sar" : {
			"$cond" : [ {
				"$gt" : [ "$_sar", 1.0 ]
			}, 1.0, "$_sar" ]
		}
	}
}, {
	"$lookup" : {
		"from" : "projectChange",
		"let" : {
			"project_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, {
						"$in" : [ "$status", [ "关闭", "通过" ] ]
					} ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"count" : {
					"$sum" : 1.0
				}
			}
		} ],
		"as" : "_change"
	}
}, {
	"$unwind" : {
		"path" : "$_change",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "projectChange",
		"let" : {
			"project_id" : "$_id"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$eq" : [ "$project_id", "$$project_id" ]
					}, {
						"$eq" : [ "$status", "通过" ]
					} ]
				}
			}
		}, {
			"$group" : {
				"_id" : null,
				"count" : {
					"$sum" : 1.0
				}
			}
		} ],
		"as" : "_changeStatus"
	}
}, {
	"$unwind" : {
		"path" : "$_changeStatus",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"changeCount" : {"$cond":["$_change.count","$_change.count",0.0]},
		"changeStatus" : {
			"$cond" : [ {
				"$gt" : [ {
					"$ifNull" : [ "$_changeStatus.count", 0.0 ]
				}, 0.0 ]
			}, "变更中", "" ]
		}
	}
}, {
	"$project" : {
		"_change" : false,
		"_changeStatus" : false,
		"_stage" : false,
		"_sar" : false
	}
} ],