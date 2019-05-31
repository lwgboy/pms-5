[
		{
			"$match" : {
				"_id" : "<id>"
			}
		},
		{
			"$lookup" : {
				"from" : "d2ProblemDesc",
				"localField" : "_id",
				"foreignField" : "_id",
				"as" : "d2ProblemDesc"
			}
		},
		{
			"$unwind" : {
				"path" : "$d2ProblemDesc",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "d4RootCauseDesc",
				"localField" : "_id",
				"foreignField" : "_id",
				"as" : "d4RootCauseDesc"
			}
		},
		{
			"$unwind" : {
				"path" : "$d4RootCauseDesc",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "organization",
				"localField" : "dept_id",
				"foreignField" : "_id",
				"as" : "org"
			}
		},
		{
			"$unwind" : {
				"path" : "$org",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$lookup" : {
				"from" : "d5PCA",
				"let" : {
					"problem_id" : "$_id"
				},
				"pipeline" : [ {
					"$match" : {
						"$expr" : {
							"$and" : [ {
								"$eq" : [ "$$problem_id", "$problem_id" ]
							}, "$selected" ]
						}
					}
				} ],
				"as" : "d5PCA"
			}
		},
		{
			"$unwind" : {
				"path" : "$d5PCA",
				"preserveNullAndEmptyArrays" : true
			}
		},
		{
			"$addFields" : {
				"how" : "$d2ProblemDesc.how",
				"howmany" : "$d2ProblemDesc.howmany",
				"what" : "$d2ProblemDesc.what",
				"why" : "$d2ProblemDesc.why",
				"when" : "$d2ProblemDesc.when",
				"where" : "$d2ProblemDesc.where",
				"who" : "$d2ProblemDesc.who",
				"rootCauseDesc" : "$d4RootCauseDesc.rootCauseDesc",
				"escapePoint" : "$d4RootCauseDesc.escapePoint",
				"evidenceData" : "$d4RootCauseDesc.evidenceData",
				"deptName" : "$org.name",
				"icaConfirmedOn" : "$icaConfirmed.date",
				"icaConfirmedBy" : "$icaConfirmed.userName",
				"pcaApprovedOn" : "$pcaApproved.date",
				"pcaApprovedBy" : "$pcaApproved.userName",
				"pcaValidatedBy" : "$pcaValidated.userName",
				"pcaValidatedOn" : "$pcaValidated.date",
				"pcaConfirmedBy" : "$pcaConfirmed.userName",
				"pcaConfirmedOn" : "$pcaConfirmed.date",
				"closeDate" : "$closeInfo.date",
				"closeBy" : "$closeInfo.userName",
				"severityIndText" : {
					"$concat" : [ {
						"$toString" : "$severityInd.index"
					}, ".", "$severityInd.value", "(", "$severityInd.desc", ")" ]
				},
				"freqIndText" : {
					"$concat" : [ {
						"$toString" : "$freqInd.index"
					}, ".", "$freqInd.value", "(", "$freqInd.text", ")" ]
				},
				"detectionIndText" : {
					"$concat" : [ {
						"$toString" : "$detectionInd.index"
					}, ".", "$detectionInd.value", "(", "$detectionInd.desc",
							")" ]
				},
				"lostIndText" : {
					"$concat" : [ {
						"$toString" : "$lostInd.index"
					}, ".", "$lostInd.value", "(", "$lostInd.desc", ")" ]
				},
				"incidenceIndText" : {
					"$concat" : [ {
						"$toString" : "$incidenceInd.index"
					}, ".", "$incidenceInd.value", "(", "$incidenceInd.desc",
							")" ]
				},
				"d5pcaCharger1name" : "$d5PCA.charger1_meta.name",
				"d5pcaPlanStart1" : "$d5PCA.planStart1",
				"d5pcaPlanFinish1" : "$d5PCA.planFinish1",
				"d5pcaCharger2name" : "$d5PCA.charger2_meta.name",
				"d5pcaPlanStart2" : "$d5PCA.planStart2",
				"d5pcaPlanFinish2" : "$d5PCA.planFinish2",
				"d5pca1" : "$d5PCA.pca1.name",
				"d5pca2" : "$d5PCA.pca2.name",
				"idrrept" : {
					"$map" : {
						"input" : "$idrrept",
						"as" : "idrrept",
						"in" : {
							"$concat" : [ "<a href='/bvs/fs?domain=",
									"<domain>", "&id=", {
										"$toString" : "$$idrrept._id"
									}, "&namespace=", "$$idrrept.namepace",
									"&name=", "$$idrrept.name", "&sid=rwt'>",
									"$$idrrept.name", "</a>" ]
						}
					}
				},
				"attarchments" : {
					"$map" : {
						"input" : "$attarchments",
						"as" : "attarchments",
						"in" : {
							"$concat" : [ "<a href='/bvs/fs?domain=",
									"<domain>", "&id=", {
										"$toString" : "$$attarchments._id"
									}, "&namespace=",
									"$$attarchments.namepace", "&name=",
									"$$attarchments.name", "&sid=rwt'>",
									"$$attarchments.name", "</a>" ]
						}
					}
				}
			}
		}, {
			"$project" : {
				"d2ProblemDesc" : false,
				"d4RootCauseDesc" : false,
				"org" : false,
				"d5PCA" : false
			}
		} ]