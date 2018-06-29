{
  "yAxis": [
    {
      "type": "value"
    }
  ],
  "xAxis": [
    {
      "type": "category",
      "data": "<xAxis>"
    }
  ],
  "legend": {
    "data": ["计划", "实际" ],
    "orient": "horizontal",
    "left": 12,
    "top": 24
  },
  "grid": [
    {
        "top": 64.0,
        "bottom": 32.0,
        "left": 32.0,
        "right": 32.0
    }   
  ],
  "series": [
    {
      "name": "计划",
      "type": "bar",
      "label": {
        "normal": {
          "show": true,
          "position": "top"
        }
      },
      "data": "<planWorks>"
    },
    {
      "name": "实际",
      "type": "bar",
      "label": {
        "normal": {
          "show": true,
          "position": "top"
        }
      },
      "data": "<actualWorks>"
    }
  ],
  "tooltip": {
    "trigger": "axis",
    "axisPointer": {
      "type": "shadow"
    }
  },
  "dataZoom": [
    {
      "type": "inside"
    },
    {
      "type": "slider"
    }
  ],
  "title": {
    "text": "<title>",
    "textStyle": {
        "color": "#757575",
        "fontFamily": "Microsoft YaHei",
        "fontWeight": 400.0,
        "fontSize": "14"
      },
    "padding": 8.0
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
  }
}