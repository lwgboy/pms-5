{
    "title": {
        "text": "<title>",
        "x": "center"
    },
    "legend": {
        "data": [
            "预算",
            "成本"
        ],
        "orient": "vertical",
        "left": "right"
    },
    "grid": {
        "left": "3%",
        "right": "4%",
        "bottom": "3%",
        "containLabel": true
    },
    "xAxis": [
        {
            "type": "category",
            "data": [
                " 1月",
                " 2月",
                " 3月",
                " 4月",
                " 5月",
                " 6月",
                " 7月",
                " 8月",
                " 9月",
                "10月",
                "11月",
                "12月"
            ]
        }
    ],
    "yAxis": [
        {
            "type": "value"
        }
    ],
    "series": [
        {
            "name": "预算",
            "type": "bar",
            "label": {
                "normal": {
                    "show": true,
                    "position": "inside"
                }
            },
            "data": "<budget>"
        },
        {
            "name": "成本",
            "type": "bar",
            "label": {
                "normal": {
                    "show": true,
                    "position": "inside"
                }
            },
            "data": "<cost>"
        }
    ]
}