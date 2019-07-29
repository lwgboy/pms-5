[
		// Stage 1
		{
			$match: {
			    "_id" : "<problem_id>"
			}
		},

		// Stage 2
		{
			$project: {
			    // specifications
			    "classifyProblems":1
			}
		},

		// Stage 3
		{
			$unwind: {
			    path : "$classifyProblems"
			}
		},

		// Stage 4
		{
			$graphLookup: {
			    "from" : "classifyProblem",
			    "startWith" : "$classifyProblems._id",
			    "connectFromField" : "parent_id",
			    "connectToField" : "_id",
			    "as" : "root"
			}
		},

		// Stage 5
		{
			$project: {
			    // specifications
			    "root":1
			}
		},

		// Stage 6
		{
			$unwind: {
			    path : "$root"
			}
		},

		// Stage 7
		{
			$match: {
				"root.parent_id":null
			}
		},

		// Stage 8
		{
			$group: {
				_id: null, uniqueValues: {$addToSet: "$root"}
			}
		},

		// Stage 9
		{
			$unwind: {
			    path : "$uniqueValues",
			}
		},

		// Stage 10
		{
			$replaceRoot: {
			    "newRoot" : "$uniqueValues"
			}
		},
		
		{
			$sort: {
				id:1
			}
		},
	]