{
  "yAxis": [
    {
      "type": "value"
    }
  ],
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
  "legend": {
    "data": [
      "预算",
      "成本"
    ],
    "orient": "horizontal",
    "left": 12,
    "top": 24
  },
  toolbox: {
      show: true,
      feature: {
          dataZoom: {
              yAxisIndex: 'none'
          },
          magicType: {type: ['line', 'bar']},
          restore: {},
          saveAsImage: {}
      }
  },
  tooltip:{
      show: true,
      trigger: "axis"
  },
  "grid": {
    "top": 64.0,
    "bottom": 32.0,
    "left": 32.0,
    "right": 32.0
  },
  "series": [
    {
      "name": "预算",
      "type": "bar",
      "label": {
        "normal": {
          "show": false,
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
          "show": false,
          "position": "inside"
        }
      },
      "data": "<cost>"
    }
  ],
  "title": {
    "text": "2018年 资金预算和使用状况（万元）",
    "textStyle": {
      "color": "#757575",
      "fontFamily": "Microsoft YaHei",
      "fontWeight": 400.0,
      "fontSize": "14"
    },
    "padding": 8.0
  }
}