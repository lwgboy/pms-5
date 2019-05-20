[
		{
			"$match" : {
				"stage" : "pca",
				"actionType":"out",
				"problem_id" :"<id>"
			}
		},
		{
			"$addFields" : {
				"title" : "$verification.title",
				"comment" : "$verification.comment",
				"verificationuser" : "$verification.user_meta.name",
				"verificationdate" : "$verification.date",
				"chargername" : "$charger_meta.name",
				"finish" : {
					"$cond" : [ "$finish", "是", "否" ]
				},
				"verificationAttachment" : {
					"$concatArrays" : [
							{
								"$ifNull" : [
										{
											"$map" : {
												"input" : "$verification.attachment",
												"as" : "attarchments",
												"in" : {
													"$concat" : [
															"<a href=\"/bvs/fs?id=",
															{
																"$toString" : "$$attarchments._id"
															},
															"&namespace=",
															"$$attarchments.namepace",
															"&name=",
															"$$attarchments.name",
															"&sid=rwt\">",
															"$$attarchments.name",
															"</a>" ]
												}
											}
										}, [] ]
							},
							{
								"$ifNull" : [
										{
											"$map" : {
												"input" : "$attachments",
												"as" : "attarchments",
												"in" : {
													"$concat" : [
															"<a href=\"/bvs/fs?id=",
															{
																"$toString" : "$$attarchments._id"
															},
															"&namespace=",
															"$$attarchments.namepace",
															"&name=",
															"$$attarchments.name",
															"&sid=rwt\">",
															"$$attarchments.name",
															"</a>" ]
												}
											}
										}, [] ]
							} ]
				}
			}
		} ]