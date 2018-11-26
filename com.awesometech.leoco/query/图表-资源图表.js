{
    "tooltip": {
        "trigger": "axis"
    },
    "legend": {
        "data": "<legendData>",
        "type": "scroll",
        "bottom": 20,
    },
    "grid": [
        {
            "left": 72,
            "right": 30,
            "bottom": "55%",
            "containLabel": false
        },
        {
            "left": 72,
            "right": 30,
            "bottom": "10%",
            "top": "50%",
            "containLabel": false
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
            "end": 100,
            "top": "90%",
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
            "name": "工时（时）",
            "axisLabel": {
                "formatter": "{value}"
            }
        },
        {
            "type": "value",
            "name": "金额（元）",
            "axisLabel": {
                "formatter": "{value}"
            },
            "gridIndex": 1
        }
    ],
    "series": "<series>"
}