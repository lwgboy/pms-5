{
    "grid": {
        "left": "3%",
        "right": "4%",
        "bottom": "6%",
        "top": "30%",
        "containLabel": true
    },
    "xAxis": {
        "type": "category",
        "data": [ "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" ]
    },
    "legend": {
        "type": "scroll",
        "bottom": 20,
        "data": "<legendData>"
    },
    "yAxis": [
        { "type": "value" }
    ],
    "series": [
        {
            "name": "直接访问",
            "type": "bar",
            "barMaxWidth": "80%",
            "data": [ 10, 52, 200, 334, 390, 330, 220 ]
        },
        {
            "name": "GDP占比",
            "type": "pie",
            "center": [ "80%", "20%" ],
            "radius": "25%",
            "z": 100,
            "data": [
                { "name": "第1产业", "value": 10 },
                { "name": "第2产业", "value": 120 },
                { "name": "第3产业", "value": 130 }
            ]
        },
        {
            "name": "GDP1占比",
            "type": "pie",
            "center": [ "30%", "20%" ],
            "radius": "25%",
            "z": 100,
            "data": [
                { "name": "第1产业", "value": 10 },
                { "name": "第2产业", "value": 120 },
                { "name": "第3产业", "value": 130 }
            ]
        }
    ]
}