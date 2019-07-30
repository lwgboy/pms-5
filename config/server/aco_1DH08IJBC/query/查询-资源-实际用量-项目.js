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
		"actualBasicQty" : {
			"$multiply" : [ "$actualBasicQty", {
				"$ifNull" : [ "$qty", 1 ]
			} ]
		},
		"actualOverTimeQty" : {
			"$multiply" : [ "$actualOverTimeQty", {
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
		"actualQty" : {
			"$sum" : [ "$actualBasicQty", "$actualOverTimeQty" ]
		},
		"actualAmount" : {
			"$sum" : [ {
				"$multiply" : [ {
					"$sum" : "$actualBasicQty"
				}, "$resourceType.basicRate" ]
			}, {
				"$multiply" : [ {
					"$sum" : "$actualOverTimeQty"
				}, "$resourceType.overtimeRate" ]
			} ]
		}
	}
}, {
	"$group" : {
		"_id" : {
			"$dateToString" : {
				"format" : "%Y%m",
				"date" : "$id"
			}
		},
		"actualQty" : {
			"$sum" : "$actualQty"
		},
		"actualAmount" : {
			"$sum" : "$actualAmount"
		}
	}
} ]