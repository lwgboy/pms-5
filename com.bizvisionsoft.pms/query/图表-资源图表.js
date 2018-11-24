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
            "left": "4%",
            "right": "3%",
            "bottom": "55%",
            "containLabel": true
        },
        {
            "left": "0%",
            "right": "0%",
            "bottom": "10%",
            "top": "50%",
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