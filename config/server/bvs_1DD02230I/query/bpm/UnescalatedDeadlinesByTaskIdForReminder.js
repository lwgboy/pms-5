[ {
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
	"$match" : {
		"task.archived" : 0,
		"task._id" : "<taskId>"
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