[
    { 
        "$match" : "<match>"
    }, 
    { 
        "$unwind" : "$workPackageSetting"
    }, 
    { 
        "$match" : {
            "workPackageSetting.catagory" : "<catagory>"
        }
    }, 
    { 
        "$addFields" : {
            "packageName" : "$workPackageSetting.name"
        }
    }, 
    { 
        "$addFields" : {
            "workPackageSetting" : [
                "$workPackageSetting"
            ]
        }
    }
]