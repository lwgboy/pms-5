[ {
	"$lookup" : {
		"from" : "riskUrInd",
		"let" : {
			"time" : "<dateField>"
		},
		"pipeline" : [ {
			"$match" : {
				"$expr" : {
					"$and" : [ {
						"$gte" : [ "$max", {
							"$divide" : [ {
								"$subtract" : [ "$$time", "<targetDate>" ]
							}, 86400000 ]
						} ]
					},

					{
						"$lte" : [ "$min", {
							"$divide" : [ {
								"$subtract" : [ "$$time", "<targetDate>" ]
							}, 86400000 ]
						} ]
					} ]
				}
			}
		} ],
		"as" : "riskUrInd"
	}
}, {
	"$addFields" : {
		"<urgencyIndField>" : {
			"$arrayElemAt" : [ "$riskUrInd", 0.0 ]
		}
	}
}, {
	"$project" : {
		"riskUrInd" : false
	}
} ]