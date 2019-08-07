[ {
	"$addFields" : {
		"vid" : {
			"$concat" : [ "$major_vid", ".", {
				"$toString" : "$svid"
			} ]
		},
		"createBy" : "$_caccount.username",
		"createOn" : "$_cdate"
	}
} ]