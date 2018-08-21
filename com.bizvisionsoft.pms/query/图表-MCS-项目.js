{
    grid:{
        top:64,
        left:64,
        right:64,
        bottom:64
    },
    xAxis: {
        type: "category",
        name: "工期（天）",
        nameLocation :"center",
        nameGap :24
    },
    yAxis: [{
        type: "value",
        name:"概率（%）",
        nameLocation :"center",
        nameGap :24
    },
    {
        type: 'value',
        name: '累积概率（%）',
        nameLocation :"center",
        nameGap :24
        min: 0,
        max: 100,
        position: 'right',
    },
    ]
    ,
    legend: {
        data: ["概率(%)","累积概率(%)"],
        show:true
    },
    tooltip:{
        show: true,
        axisPointer: {
                type: "cross"
            },
    	trigger: "axis"
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
    series: [{
        data: "<data>",
        type: "line",
        smooth: true,
        name:"概率(%)",
        yAxisIndex: 0,
        markPoint: {
            data: [
                {type: "max", name: "最大值", label:{formatter: '{@[1]}%'}},
                {type: "min", name: "最小值", label:{formatter: '{@[1]}%'}}
            ]
        }
    },
    {
        data: "<sum>",
        type: "line",
        smooth: true,
        name:"累积概率(%)",
        yAxisIndex: 1,
        areaStyle: {
            normal: {
                color: "new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0,color: 'rgba(219, 50, 51, 0.3)'}, {offset: 0.8,color: 'rgba(219, 50, 51, 0)'}], false)",
                shadowColor: 'rgba(0, 0, 0, 0.1)',
                shadowBlur: 10,
                opacity:0.3
            }
        },
        itemStyle: {
            normal: {
                color: 'rgb(219,50,51)',
                borderColor: 'rgba(219,50,51,0.2)'
            },
        }

    }
    ]
}