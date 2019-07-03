[ {
	"$match" : {
		"type" : "因果分析-流出",
		"problem_id" : "<id>"
	}
}, {
	"$sort" : {
		"subject" : 1,
		"classifyCause.id" : 1,
		"_id" : 1
	}
}, {
	"$addFields" : {
		"classifyCausePath" : "$classifyCause.path"
	}
} ]