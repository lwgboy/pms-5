 [
        { 
            "$match" : {
                "org_id" : "<org_id>"
            }
        }, 
        { 
            "$group" : {
                "_id" : {
                    "org_id" : "$org_id", 
                    "resourceType_id" : "$resourceType_id"
                }
            }
        }, 
        { 
            "$lookup" : {
                "from" : "resourceType", 
                "localField" : "_id.resourceType_id", 
                "foreignField" : "_id", 
                "as" : "resourceType"
            }
        },
        {
            "$unwind":{"path" : "$resourceType"}
        }
    ]