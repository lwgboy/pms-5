
	// Pipeline
	[
		// Stage 1
		{
			$lookup: // Equality Match
			{
			     from:"problem" ,
			        localField:"_id" ,
			        foreignField:"depts_id" ,    
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
			                              if: {"$eq" :["$name",'组装车间'] },then: "$name",
			                              else :{
			                                "$cond": {
			                                    if: {"$eq" :["$name",'IQC来料'] },then: "$name",
			                                    else:{
			                                        "$cond": {
			                                                if: {"$eq" :["$name",'设计'] },then: "$name",
			                                            else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'GW104组装车间'] },then: "$name",
			                                                    else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'O型弹片车间'] },then: "$name",
			                                                    else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'注塑'] },then: "$name",
			                                                    else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'冲压'] },then: "$name",
			                                                    else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'MS组装车间'] },then: "$name",
			                                                    else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'测试车间'] },then: "$name",
			                                                    else:{
			                                            "$cond": {
			                                                    if: {"$eq" :["$name",'物流'] },then: "$name",
			                                                    else:""
			                                                }
			                                              }
			                                                }
			                                              }
			                                                }
			                                              }
			                                                }
			                                              }
			                                                }
			                                              }
			                                                }
			                                              }
			                                                }
			                                              }
			                                        }
			                              }
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

