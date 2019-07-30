[ {
	"$lookup" : {
		"from" : "work",
		"localField" : "work_id",
		"foreignField" : "_id",
		"as" : "work"
	}
}, {
	"$unwind" : "$work"
}, {
	"$addFields" : {
		"project_id" : "$work.project_id",
		"year" : {
			"$dateToString" : {
				"format" : "%Y",
				"date" : "$id"
			}
		},
		"planBasicQty" : {
			"$multiply" : [ "$planBasicQty", {
				"$ifNull" : [ "$qty", 1 ]
			} ]
		},
		"planOverTimeQty" : {
			"$multiply" : [ "$planOverTimeQty", {
				"$ifNull" : [ "$qty", 1 ]
			} ]
		}
	}
}, {
	"$match" : "<match>"
}, {
	"$lookup" : {
		"from" : "resourceType",
		"localField" : "resTypeId",
		"foreignField" : "_id",
		"as" : "resourceType"
	}
}, {
	"$unwind" : "$resourceType"
}, {
	"$addFields" : {
		"planQty" : {
			"$sum" : [ "$planBasicQty", "$planOverTimeQty" ]
		},
		"planAmount" : {
			"$sum" : [ {
				"$multiply" : [ {
					"$sum" : "$planBasicQty"
				}, "$resourceType.basicRate" ]
			}, {
				"$multiply" : [ {
					"$sum" : "$planOverTimeQty"
				}, "$resourceType.overtimeRate" ]
			} ]
		}
	}
}, {
	"$lookup" : {
		"from" : "user",
		"localField" : "usedHumanResId",
		"foreignField" : "userId",
		"as" : "user"
	}
}, {
	"$unwind" : {
		"path" : "$user",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$lookup" : {
		"from" : "equipment",
		"localField" : "usedEquipResId",
		"foreignField" : "id",
		"as" : "equipment"
	}
}, {
	"$unwind" : {
		"path" : "$equipment",
		"preserveNullAndEmptyArrays" : true
	}
}, {
	"$match" : {
		"$or" : [ {
			"user.org_id" : "<org_ids>"
		}, {
			"equipment.org_id" : "<org_ids>"
		} ]
	}
}, {
	"$group" : {
		"_id" : {
			"$dateToString" : {
				"format" : "%Y%m",
				"date" : "$id"
			}
		},
		"planQty" : {
			"$sum" : "$planQty"
		},
		"planAmount" : {
			"$sum" : "$planAmount"
		}
	}
} ]