[
		{
			"$lookup" : {
				"from" : "project",
				"localField" : "_id",
				"foreignField" : "cbs_id",
				"as" : "project"
			}
		},
		{
			"$unwind" : "$project"
		},
		{
			"$match" : {
				"project.status" : {
					"$nin" : [ "已创建", "已关闭" ]
				}
			}
		},
		{
			"$addFields" : {
				"scopeId" : "$project.id"
			}
		},
		{
			"$lookup" : {
				"from" : "user",
				"let" : {
					"userId" : "$project.pmId"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$eq" : [ "$$userId", "$userId" ]
						}
					}
				}, {
					"$project" : {
						"_id" : false,
						"userId" : true,
						"name" : true,
						"headPics" : true,
						"position" : true,
						"tel" : true,
						"org_id" : true,
						"email" : true
					}
				} ],
				"as" : "scopeCharger_meta"
			}
		},
		{
			"$unwind" : {
				"path" : "$scopeCharger_meta",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "organization",
				"localField" : "scopeCharger_meta.org_id",
				"foreignField" : "_id",
				"as" : "temp_Org"
			}
		},
		{
			"$addFields" : {
				"scopeCharger" : {
					"$cond" : [ "$scopeCharger_meta.name",
							"$scopeCharger_meta.name", "" ]
				},
				"scopeCharger_meta.orgInfo" : {
					"$arrayElemAt" : [ "$temp_Org.fullName", 0.0 ]
				}
			}
		}, {
			"$project" : {
				"temp_Org" : false,
				"project" : false
			}
		} ]