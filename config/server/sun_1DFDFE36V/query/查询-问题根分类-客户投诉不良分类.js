
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
			$unwind: {
			    path : "$dateSet",
			    preserveNullAndEmptyArrays : true // optional
			}
		},

		// Stage 4
		{
			$group: {
			    '_id':{
			        'name':'$name',
			        'date': {    
			      "$dateToString" : {
			        "date" :'$dateSet.issueDate',
			                "format" : "%Y-%m"
			            }
			        },
			    },
			    'count':{'$sum':1}    
			}
		},
		{
			$match:{
			    "$or":[{"_id.anddate":"<anddate>"},{"_id.anddate":null}]
			}
		},
		// Stage 5
		{
			$project: {
			    '_id':'$_id.name',
			    'date': '$_id.date',
			    'count':'$count'   
			}
		},

		// Stage 6
		{
			$group: {
				'_id':"$_id",
				'source':{
				  "$addToSet" : {
										"_id" : "$date",
										"value" : "$count"
									}
				}
				
			}
		},

	]

	// Created with Studio 3T, the IDE for MongoDB - https://studio3t.com/

