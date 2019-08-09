
	// Pipeline
	[
		// Stage 1
		{
			$lookup: // Equality Match
			{
			     from:"problem" ,
			        localField:"_id" ,
			        foreignField:"classifyProblems._id" ,
			        as: "dateSet"
			}
			
			// Uncorrelated Subqueries
			// (supported as of MongoDB 3.6)
			// {
			//    from: "<collection to join>",
			//    let: { <var_1>: <expression>, …, <var_n>: <expression> },
			//    pipeline: [ <pipeline to execute on the collection to join> ],
			//    as: "<output array field>"
			// }
		},

		// Stage 2
		{
			$match: {
			     '$and' :[{'parent_id' :  ObjectId('5d26a064a756a527e8bf25f0')}],
			}
		},

		// Stage 3
		{
			$project: {
			    "name":"$name",
			    "dateSet":"$dateSet",
			    "count":{"$size":"$dateSet"}
			}
		},

		// Stage 4
		{
			$match: {
			     '$or' :[{"dateSet.issueDate":{"$gt":ISODate("2019-01-24T00:00:00.000Z")}},{"count":0}],
			}
			//{2019-01-24T00:00:00.000Z
			//    "name":"$name",
			//    "count":{"$size":"$dateSet"}
			//}
		},

		// Stage 5
		{
			$project: {
			    "name":"$name",
			    "count":{"$size":"$dateSet"}
			}
		},
		{
			$sort:{
				count:-1
			}
		},
//		{
//			$group:{
//				'_id':"$name",
//				'source':{
//				  "$addToSet" : {
//						"_id" : "$name",
//						"value" : "$count"
//				  }
//				}
//			}
//		}

	]
