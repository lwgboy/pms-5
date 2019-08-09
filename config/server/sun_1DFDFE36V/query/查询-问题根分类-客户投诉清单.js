
	// Pipeline
	[
		// Stage 1
		{
			$lookup: // Equality Match
			{
			     from:"problem" ,
			        localField:"_id" ,
			        foreignField:"customer_id" ,
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
			$project: {
			    name:{
			        "$cond": {
			                  if: {"$eq" :["$name",'华为终端（深圳）有限公司'] },then: "华为",
			                  else :{
			                    "$cond": {
			                        if: {"$eq" :["$name",'SAMSUNG ELECTRO-MECHANICS VIETNAM'] },then: "MS",
			                        else:"三星及其他"
			                    }
			                  }
			                }
			    },	
			    "dateSet":"$dateSet"
			}
		},

		// Stage 3
		{
			$match: {
			    '$or' :[{'name' : {$ne:""}} ]
			}
		},

		// Stage 4
		{
			$unwind: {
			    path : "$dateSet",
			    preserveNullAndEmptyArrays : true // optional
			}
		},

		// Stage 5
		{
			$project: {
			    name:"$name",
			    date: {    
			      "$dateToString" : {
			        "date" : "$dateSet.issueDate",
			                "format" : "%Y-%m"
			            }
			        },
			    anddate: {    
			      "$dateToString" : {
			        "date" : "$dateSet.issueDate",
			                "format" : "%Y"
			            }
			        }
			}
		},

		// Stage 6
		{
			$match: {
			     '$or' :[{'anddate' : '2019'}],
			}
		},

		// Stage 7
		{
			$group: {
			    '_id':{
			        '_id':'$_id',
			        'name':'$name',
			        'date': '$date'
			    },
			    'count':{'$sum':1}    
			}
		},

		// Stage 8
		{
			$project: {
			    '_id': '$_id._id',
			    name : '$_id.name',
			    date : '$_id.date',
			    count: '$count'
			}
		},

		// Stage 9
		{
			$group: {
			    '_id':"$name",
			    'source':{
			      "$addToSet" : {
			                            "_id" : "$date",
			                            "value" : "$count"
			                        }
			    }
			    
			}
		},

	]

