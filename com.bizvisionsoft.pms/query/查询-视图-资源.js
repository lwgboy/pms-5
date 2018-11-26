[ {
	"$lookup" : {
		"from" : "resourcePlan",
		"localField" : "_id",
		"foreignField" : "resTypeId",
		"as" : "resPlan"
	}
}, {
	"$lookup" : {
		"from" : "resourceActual",
		"localField" : "_id",
		"foreignField" : "resTypeId",
		"as" : "resActual"
	}
}, {
	"$addFields" : {
		"resPlan.type" : "plan",
		"resPlan.resType" : {
			"_id" : "$_id",
			"id" : "$id",
			"name" : "$name",
			"type" : "$type",
			"priceModel" : "$priceModel",
			"overtimeRate" : "$overtimeRate",
			"basicRate" : "$basicRate",
			"cal_id" : "$cal_id"
		},
		"resActual.type" : "actual",
		"resActual.resType" : {
			"_id" : "$_id",
			"id" : "$id",
			"name" : "$name",
			"type" : "$type",
			"priceModel" : "$priceModel",
			"overtimeRate" : "$overtimeRate",
			"basicRate" : "$basicRate",
			"cal_id" : "$cal_id"
		}
	}
}, {
	"$addFields" : {
		"resUsage" : {
			"$concatArrays" : [ "$resPlan", "$resActual" ]
		}
	}
}, {
	"$project" : {
		"resUsage" : true
	}
}, {
	"$unwind" : {
		"path" : "$resUsage"
	}
}, {
	"$replaceRoot" : {
		"newRoot" : "$resUsage"
	}
}, {
	"$addFields" : {
		"basicQty" : {
			"$ifNull" : [ "$planBasicQty", "$actualBasicQty" ]
		},
		"overtimeQty" : {
			"$ifNull" : [ "$planOverTimeQty", "$actualOverTimeQty" ]
		}
	}
}, {
	"$graphLookup" : {
		"from" : "work",
		"startWith" : "$work_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "parentWorks"
	}
}, {
	"$addFields" : {
		"rootWork" : {
			"$arrayElemAt" : [ "$parentWorks", 0.0 ]
		}
	}
}, {
	"$lookup" : {
		"from" : "project",
		"localField" : "rootWork.project_id",
		"foreignField" : "_id",
		"as" : "project"
	}
}, {
	"$unwind" : {
		"path" : "$project"
	}
}, {
	"$graphLookup" : {
		"from" : "eps",
		"startWith" : "$project.eps_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "eps"
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "usedHumanResId",
		"foreignField" : "userId",
		"as" : "hr"
	}
}, {
	"$lookup" : {
		"from" : "equipment",
		"localField" : "usedEquipResId",
		"foreignField" : "id",
		"as" : "eq"
	}
}, {
	"$unwind" : {
		"path" : "$hr",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$unwind" : {
		"path" : "$eq",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$addFields" : {
		"res" : {
			"$cond" : [ "$hr", {
				"_id" : "$hr._id",
				"id" : "$hr.userId",
				"name" : "$hr.name",
				"org_id" : "$hr.org_id",
				"type" : "user"
			}, {
				"$cond" : [ "$eq", {
					"_id" : "$eq._id",
					"id" : "$eq.id",
					"name" : "$eq.name",
					"org_id" : "$eq.org_id",
					"type" : "equipment"
				}, {
					"_id" : "$resType._id",
					"id" : "$resType.id",
					"name" : "$resType.name",
					"type" : "resourceType"
				} ]
			} ]
		},
		"project" : {
			"_id" : "$project._id",
			"name" : "$project.name",
			"impUnit_id" : "$project.impUnit_id",
			"eps_id" : "$project.eps_id"
		},
		"stage" : {
			"$cond" : [ "$rootWork.stage", {
				"_id" : "$rootWork._id",
				"name" : "$rootWork.fullName"
			}, null ]
		}
	}
}, {
	"$graphLookup" : {
		"from" : "organization",
		"startWith" : "$res.org_id",
		"connectFromField" : "parent_id",
		"connectToField" : "_id",
		"as" : "org"
	}
}, {
	"$project" : {
		"id" : true,
		"resType" : true,
		"basicQty" : true,
		"overtimeQty" : true,
		"qty" : true,
		"type" : true,
		"res" : true,
		"work_id" : true,
		"eps" : true,
		"project" : true,
		"stage" : true,
		"org" : {
			"_id" : true,
			"id" : true,
			"name" : true,
			"fullName" : true,
			"type" : true
		}
	}
} ]