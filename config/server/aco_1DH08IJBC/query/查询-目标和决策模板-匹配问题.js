[
		{
			"$match" : {
				"_id" : "<problem_id>"
			}
		},
		{
			"$lookup" : {
				"from" : "solutionCriteriaTemplate",
				"let" : {
					"problemClass_id" : "$classifyProblem"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$and" : [
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