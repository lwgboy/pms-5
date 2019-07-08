[
		{
			"$match" : {
				"_id" : "<problem_id>"
			}
		},
		{
			"$lookup" : {
				"from" : "problemActionPlan",
				"let" : {
					"problemClass_id" : "$classifyProblem"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$and" : [
									{
										"$in" : "<in>"
									},
									{
										"applicable" : true
									},
									{
										"$in" : [ "$$problemClass_id._id",
												"$classifyProblems._id" ]
									} ]
						}
					}
				} ],
				"as" : "plan"
			}
		}, {
			"$unwind" : {
				"path" : "$plan"
			}
		}, {
			"$replaceRoot" : {
				"newRoot" : "$plan"
			}
		} ]