[

{
	"$lookup" : {
		"from" : "user",
		"localField" : "usedHumanResId",
		"foreignField" : "userId",
		"as" : "hr"
	}
}, {
	"$lookup" : {
		"from" : "equipment",
		"localField" : "usedEquipResId",
		"foreignField" : "id",
		"as" : "eq"
	}
}, {
	"$lookup" : {
		"from" : "resourceType",
		"localField" : "usedTypedResId",
		"foreignField" : "id",
		"as" : "tp"
	}
}, 
{ 
    "$unwind" : {
        "path" : "$hr", 
        "preserveNullAndEmptyArrays" : true
    }
}, 
{ 
    "$unwind" : {
        "path" : "$eq", 
        "preserveNullAndEmptyArrays" : true
    }
}, 
{ 
    "$unwind" : {
        "path" : "$tp", 
        "preserveNullAndEmptyArrays" : true
    }
}

]