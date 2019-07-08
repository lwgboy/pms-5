[ {
	"$match" : {
		"escalated" : 0
	}
}, {
	"$lookup" : {
		"from" : "Task",
		"localField" : "<deadlineField>",
		"foreignField" : "id",
		"as" : "task"
	}
}, {
	"$unwind" : {
		"path" : "$task"
	}
}, {
	"$project" : {
		"taskId" : "$task.id",
		"deadlineId" : "$id",
		"date" : "$deadline_date"
	}
}, {
	"$sort" : {
		"date" : 1
	}
} ]