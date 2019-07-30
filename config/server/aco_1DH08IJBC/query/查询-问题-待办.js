[
		{
			$lookup: 
			{
			    from: "problem",
			    localField: "problem_id",
			    foreignField: "_id",
			    as: "problem"
			}
		},

		{
			$match: {
				"problem.status":"解决中",
				"member":"<userId>"
			}
		},{
			$addFields: {
			    "roleName": {
			    	$switch:{
			    		branches:[
			    			{
			    				case:{$eq:["$role","0"]},
			    				then:"组长"
			    			},{
			    				case:{$eq:["$role","1"]},
			    				then:"设计"
			    			},{
			    				case:{$eq:["$role","2"]},
			    				then:"工艺"
			    			},{
			    				case:{$eq:["$role","3"]},
			    				then:"生产"
			    			},{
			    				case:{$eq:["$role","4"]},
			    				then:"质量"
			    			},{
			    				case:{$eq:["$role","5"]},
			    				then:"顾客代表"
			    			},{
			    				case:{$eq:["$role","6"]},
			    				then:"ERA"
			    			},{
			    				case:{$eq:["$role","7"]},
			    				then:"ICA"
			    			},{
			    				case:{$eq:["$role","8"]},
			    				then:"PCA"
			    			},{
			    				case:{$eq:["$role","9"]},
			    				then:"SPA"
			    			},{
			    				case:{$eq:["$role","10"]},
			    				then:"LRA"
			    			}
			    		],
			    		default:""
			    	}
			    }
			}
		},{
			$match: {
			    // enter query here
			    roleName:{$in:["ERA","ICA","PCA","SPA","LRA"]}
			}
		},

	]