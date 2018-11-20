{
    "title": {
        "text": "<title>",
        "x": "center"
    },
    "legend": {
        "data": [
            "累计工时",
            "计划金额",
            "加班工时",
            "计划工时",
            "实际工时",
            "计划金额",
            "实际金额"
        ],
        "y": "30",
        "x": "right"
    },
    "grid": [
        {
            "left": 10,
            "right": 10,
            "bottom": "50%",
            "containLabel": true
        },
        {
            "left": 10,
            "right": 10,
            "bottom": 40,
            "top": "55%",
            "containLabel": true
        }
    ],
    "dataZoom": [
        {
            "type": "slider",
            "xAxisIndex": [
                0,
                1
            ],
            "start": 50,
            "end": 100
        }
    ],
    "xAxis": [
        {
            "type": "category",
            "data": "<xAxisData>"
        },
        {
            "type": "category",
            "data": "<xAxisData>",
            "gridIndex": 1
        }
    ],
    "yAxis": [
        {
            "type": "value",
            "name": "工时",
            "axisLabel": {
                "formatter": "{value} 小时"
            }
        },
        {
            "type": "value",
            "name": "金额",
            "axisLabel": {
                "formatter": "{value} 万元"
            },
            "gridIndex": 1
        }
    ],
    "series": "<series>"
}