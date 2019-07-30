[
        { 
            "$match" : {
                "project_id" : "<project_id>"
            }
        }, 
        { 
            "$unwind" : {
                "path" : "$Tdata", 
                "preserveNullAndEmptyArrays" : false
            }
        }, 
        { 
            "$project" : {
                "t" : {
                    "$arrayElemAt" : [
                        "$Tdata", 
                        0.0
                    ]
                }, 
                "p" : {
                    "$arrayElemAt" : [
                        "$Tdata", 
                        1.0
                    ]
                }
            }
        }, 
        { 
            "$sort" : {
                "p" : -1.0
            }
        }, 
        { 
            "$group" : {
                "_id" : "$_id", 
                "minT" : {
                    "$min" : {
                        "t" : "$t", 
                        "p" : "$p"
                    }
                }, 
                "maxT" : {
                    "$max" : {
                        "t" : "$t", 
                        "p" : "$p"
                    }
                }, 
                "maxP" : {
                    "$first" : {
                        "t" : "$t", 
                        "p" : "$p"
                    }
                }
            }
        }
    ]