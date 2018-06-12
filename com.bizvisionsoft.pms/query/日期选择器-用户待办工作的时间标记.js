   [
        { 
            "$match" : {
                "$or" : [
                    {
                        "chargerId" : "zh"
                    }, 
                    {
                        "assignerId" : "zh"
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
            "$project" : {
                "date" : true, 
                "type" : "dhx_time_block",
                "style" : {
                    "$cond" : [
                        "$index", 
                        "dhx_mark_finish", 
                        "dhx_mark_start"
                    ]
                }, 
                "text" : {
                    "$cond" : [
                        "$index", 
                        "<i class='layui-icon layui-icon-flag' style='color:#ff9800;font-size:18px;'></i>", 
                        "<i class='layui-icon layui-icon-release' style='color:#3f51b5;font-size:18px;'></i>"
                    ]
                }
            }
        }
    ]