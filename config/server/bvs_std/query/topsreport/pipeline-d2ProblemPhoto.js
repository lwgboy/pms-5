[
		{
			"$match" : {
				"problem_id" :"<id>"
			}
		},
		{
			"$unwind" : {
				"path" : "$problemImg",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"img" : {
					"$concat" : [ "<img alt='' src='/bvs/fs?domain=","<domain>", "&id=", {
						"$toString" : "$problemImg._id"
					}, "&namespace=", "$problemImg.namepace", "&name=",
							"$problemImg.name",
							"&sid=rwt' style='width:200px' />" ]
				}
			}
		} ]