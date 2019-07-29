[
		// Stage 1
		{
			$match: {
			    "_id" : "<problem_id>"
			}
		},

		// Stage 2
		{
			$unwind: {
			    path : "$classifyProblems"
			}
		},

		// Stage 3
		{
			$lookup: {
			    "from" : "problemActionPlan",
			    "let" : {
			        "problemClass_id" : "$classifyProblems"
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
		},

		// Stage 4
		{
			$unwind: {
			    "path" : "$plan"
			}
		},

		// Stage 5
		{
			$group: {
				_id: null, uniqueValues: {$addToSet: "$plan"}
			}
		},

		// Stage 6
		{
			$unwind: {
			    path : "$uniqueValues"
			}
		},

		// Stage 7
		{
			$replaceRoot: {
			    "newRoot" : "$uniqueValues"
			}
		},

	]