
	// Pipeline
	[
		// Stage 1
		{
			$lookup: {
			  "from" : "organization",
			  "localField" : "charger_meta.org_id",
			  "foreignField" : "_id",
			  "as": "inventory_docs"
			}
		},

		// Stage 2
		{
			$project: { 
			 "item": 1,
			 "total": {
			    "$subtract":
			      [  
			      "$planFinish", "$planStart" 
			         ] 
			    },
			  "org_id":'$charger_meta.org_id',
			  "org_name":'$inventory_docs.name',
			  "planFinish":  {
						"$dateToString" : {
							"date" : "$planFinish",
							"format" : "%Y-%m"
						}
					}
			}
		},

		// Stage 3
		{
			$group: {
			"_id":{
			"name":{
			    "$cond": {
			      if: {$eq :[{$size: "$org_name"},0] },
			      then: ["未指定部门"],
			      else: "$org_name"
			    }
			},
			"date":"$planFinish"
			}
			,
			"nameAvg":{"$avg":"$total"}
			}
			
			
			//"a":"$planFinish"
		},

		// Stage 4
		{
			$project: { 
			 "item": 1,
			 "value": {
			    "$divide":
			      [  
			      "$nameAvg", {$multiply :[1000,60,60,24] }
			      ] 
			    }
			}
		},

		// Stage 5
		{
			$unwind: {
			    path : "$_id.name",
			}
		},

		// Stage 6
		{
			$group: {
					"_id" : "$_id.name",
					"source" : {
						"$addToSet" : {
							"_id" : "$_id.date",
							"value" : "$value"
						}
					}
			}
		},

	]

	// Created with Studio 3T, the IDE for MongoDB - https://studio3t.com/

