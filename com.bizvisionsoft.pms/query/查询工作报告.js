[
    { 
        "$match" : "<match>"
    }, 
    { 
        "$lookup" : {
            "from" : "project", 
            "localField" : "project_id", 
            "foreignField" : "_id", 
            "as" : "project"
        }
    }, 
    { 
        "$unwind" : "$project"
    }, 
    { 
        "$lookup" : {
            "from" : "work", 
            "localField" : "stage_id", 
            "foreignField" : "_id", 
            "as" : "work"
        }
    }, 
    { 
        "$unwind" : {
            "path" : "$work", 
            "preserveNullAndEmptyArrays" : true
        }
    }, 
    { 
        "$lookup" : {
            "from" : "user", 
            "localField" : "reporter", 
            "foreignField" : "userId", 
            "as" : "user1"
        }
    }, 
    { 
        "$unwind" : "$user1"
    }, 
    { 
        "$lookup" : {
            "from" : "user", 
            "localField" : "verifier", 
            "foreignField" : "userId", 
            "as" : "user2"
        }
    }, 
    { 
        "$unwind" : {
            "path" : "$user2", 
            "preserveNullAndEmptyArrays" : true
        }
    }, 
    { 
        "$addFields" : {
            "projectInfo" : "$project.name", 
            "stageInfo" : "$work.name", 
            "reporterInfo" : {
                "$concat" : [
                    "$user1.name", 
                    "[", 
                    "$user1.userId", 
                    "]"
                ]
            }, 
            "verifierInfo" : {
                "$concat" : [
                    "$user2.name", 
                    "[", 
                    "$user2.userId", 
                    "]"
                ]
            }
        }
    }, 
    { 
        "$project" : {
            "project" : false, 
            "work" : false, 
            "user1" : false, 
            "user2" : false
        }
    }
]