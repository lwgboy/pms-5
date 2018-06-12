    [
        { 
            "$match" : {
                "$or" : [
                    {
                        "chargerId" : "<userId>"
                    }, 
                    {
                        "assignerId" : "<userId>"
                    }
                ], 
                "summary" : false, 
                "actualFinish" : null, 
                "distributed" : true, 
                "stage" : {
                    "$ne" : true
                }
            }
        }, 
        { 
            "$project" : {
                "date" : [
                    "$planStart", 
                    "$planFinish"
                ]
            }
        }, 
        { 
            "$unwind" : {
                "path" : "$date", 
                "includeArrayIndex" : "index", 
                "preserveNullAndEmptyArrays" : false
            }
        }, 
        { 
            "$group" : {
                "_id" : {
                    "$dateToString" : {
                        "format" : "%Y-%m-%d", 
                        "date" : "$date"
                    }
                }, 
                "idx" : {
                    "$first" : "$index"
                }
            }
        }, 
        { 
            "$project" : {
                "type" : "dhx_time_block", 
                "text" : {
                    "$cond" : [
                        "$idx", 
                        "<i class='layui-icon layui-icon-flag' style='color:#ff9800;font-size:18px;'></i>", 
                        "<i class='layui-icon layui-icon-release' style='color:#3f51b5;font-size:18px;'></i>"
                    ]
                }, 
                "style" : {
                    "$cond" : [
                        "$idx", 
                        "dhx_mark_finish", 
                        "dhx_mark_start"
                    ]
                }, 
                "date" : {
                    "$dateFromString" : {
                        "dateString" : "$_id"
                    }
                }
            }
        }
    ]