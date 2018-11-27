{
    "name": "<name>",
    "type": "pie",
    "center": [ "25%", "20%" ],
    "radius": "30%",
    "z": 100,
    "label": {
        "normal": {
            "formatter": "{b|{b}：}{per|{d}%}",
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
    "data": "<data>"
}