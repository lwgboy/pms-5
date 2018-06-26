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
    yAxis: {
        type: "value",
        name:"概率（%）",
        nameLocation :"center",
        nameGap :24
    },
    legend: {
        data: ["工期概率"],
        show:false
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
        name:"工期概率",
        markPoint: {
            data: [
                {type: "max", name: "最大值", label:{formatter: '{@[1]}%'}},
                {type: "min", name: "最小值", label:{formatter: '{@[1]}%'}}
            ]
        }
    }]
}