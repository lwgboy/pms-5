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