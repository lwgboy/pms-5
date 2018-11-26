//db.getCollection("resourceType").aggregate(

	// Pipeline
	[
		// Stage 1
		{
			$lookup: {
			    from: "resourcePlan",
			    localField: "_id",
			    foreignField: "resTypeId",
			    as: "resPlan"
			}
		},

		// Stage 2
		{
			$lookup: {
			    from: "resourceActual",
			    localField: "_id",
			    foreignField: "resTypeId",
			    as: "resActual"
			}
		},

		// Stage 3
		{
			$addFields: {
			    "resPlan.type": "plan",
			    "resPlan.resType":{"_id":"$_id","id":"$id","name":"$name","type":"$type","priceModel":"$priceModel","overtimeRate":"$overtimeRate","basicRate":"$basicRate","cal_id":"$cal_id"}  , 
			    "resActual.type":"actual",
			    "resActual.resType":{"_id":"$_id","id":"$id","name":"$name","type":"$type","priceModel":"$priceModel","overtimeRate":"$overtimeRate","basicRate":"$basicRate","cal_id":"$cal_id"} 
			}
		},

		// Stage 4
		{
			$addFields: {
			   
			    "resUsage": {"$concatArrays":["$resPlan","$resActual"]}
			}
		},

		// Stage 5
		{
			$project: {
			    "resUsage":true
			}
		},

		// Stage 6
		{
			$unwind: {
			    path : "$resUsage"
			}
		},

		// Stage 7
		{
			$replaceRoot: {
			    newRoot: "$resUsage"
			}
		},

		// Stage 8
		{
			$addFields: {
			    "basicQty": {"$ifNull":["$planBasicQty","$actualBasicQty"]},
			    "overtimeQty": {"$ifNull":["$planOverTimeQty","$actualOverTimeQty"]},
			}
		},

		// Stage 9
		{
			$graphLookup: {
			    from: "work",
			    startWith: "$work_id", // connectToField value(s) that recursive search starts with
			    connectFromField: "parent_id",
			    connectToField: "_id",
			    as: "parentWorks"
			}
		},

		// Stage 10
		{
			$addFields: {
			    "rootWork": {"$arrayElemAt":["$parentWorks",0]}
			}
		},

		// Stage 11
		{
			$lookup: // Equality Match
			{
			    from: "project",
			    localField: "rootWork.project_id",
			    foreignField: "_id",
			    as: "project"
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

		// Stage 12
		{
			$unwind: {
			    path : "$project"
			}
		},

		// Stage 13
		{
			$graphLookup: {
			    from: "eps",
			    startWith: "$project.eps_id", // connectToField value(s) that recursive search starts with
			    connectFromField: "parent_id",
			    connectToField: "_id",
			    as: "eps"
			}
		},

		// Stage 14
		{
			$lookup: // Equality Match
			{
			    from: "user",
			    localField: "usedHumanResId",
			    foreignField: "userId",
			    as: "hr"
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

		// Stage 15
		{
			$lookup: // Equality Match
			{
			    from: "equipment",
			    localField: "usedEquipResId",
			    foreignField: "id",
			    as: "eq"
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

		// Stage 16
		{
			$unwind: {
			    path : "$hr",
			    preserveNullAndEmptyArrays : true // optional
			}
		},

		// Stage 17
		{
			$unwind: {
			    path : "$eq",
			    preserveNullAndEmptyArrays : true // optional
			}
		},

		// Stage 18
		{
			$project: {
			   id:true,
			   resType:true,
			   basicQty:true,
			   overtimeQty:true,
			   qty:true,
			   type:true,
			   res:{"$cond":["$hr",{"_id":"$hr._id", "id":"$hr.userId", "name":"$hr.name","org_id":"$hr.org_id","type":"user"},{"$cond":["$eq",{"_id":"$eq._id", "id":"$eq.id", "name":"$eq.name","org_id":"$eq.org_id","type":"equipment"},{"_id":"$resType._id", "id":"$resType.id", "name":"$resType.name","type":"resourceType"}]}]},
			   work_id:true,
			   eps:true,
			   project:{"_id":"$project._id","name":"$project.name", "impUnit_id":"$project.impUnit_id", "eps_id":"$project.eps_id"},
			   stage:{"$cond":["$rootWork.stage",{"_id":"$rootWork._id", "name":"$rootWork.fullName"},null]}
			}
		},

	]

	// Created with Studio 3T, the IDE for MongoDB - https://studio3t.com/

//);
