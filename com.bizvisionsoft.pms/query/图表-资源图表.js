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
            "right": 24,
            "bottom": "55%",
            "containLabel": false
        },
        {
            "left": 72,
            "right": 24,
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
    "yAxis": "<yAxis>",
    "series": "<series>"
}