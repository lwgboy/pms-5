{
    "title" : {
        "text": "<title>",
        "x":"center"
    },
    "tooltip": {
        "trigger": "axis",
        "axisPointer": {
            "type": "cross",
            "crossStyle": {
                "color": "#999"
            }
        }
    },
   "legend": { 
	   "x" : "center",
       "y" : "bottom",
        "data":"<legendData>"
    },
    "xAxis": [
        {
            "type": "category",
            "data": "<xAxisData>",
            "axisPointer": {
                "type": "shadow"
            }
        }
    ],
   "yAxis": [
        {
            "type": "value",
            "name": "完成率（%）",
            "axisLabel": {
                "formatter": "{value}"
            }
        }
    ],
    "series": "<series>"
}