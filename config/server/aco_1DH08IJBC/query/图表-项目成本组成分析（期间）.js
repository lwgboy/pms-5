{
    "title": {
        "text": "<title>",
        "x": "center"
    },
    "tooltip": {
        "trigger": "item",
        "formatter": "{b} : {c} ({d}%)"
    },
    "legend": {
        "orient": "vertical",
        "left": "left",
        "data": "<data1>"
    },
    "series": [
        {
            "name": "成本组成",
            "type": "pie",
            "radius": "55%",
            "center": [
                "50%",
                "60%"
            ],
            "label": {
                "normal": {
                    "formatter": "{b|{b}：{c}万元} {per|{d}%}",
                    "rich": {
                        "b": {
                            "color": "#747474",
                            "lineHeight": 22,
                            "align": "center"
                        },
                        "hr": {
                            "color": "#aaa",
                            "width": "100%",
                            "borderWidth": 0.5,
                            "height": 0
                        },
                        "per": {
                            "color": "#eee",
                            "backgroundColor": "#334455",
                            "padding": [
                                2,
                                4
                            ],
                            "borderRadius": 2
                        }
                    }
                }
            },
            "data": "<data2>"
        }
    ]
}