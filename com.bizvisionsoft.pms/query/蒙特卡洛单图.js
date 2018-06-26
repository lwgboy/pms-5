{
    grid:{
        top:48,
        left:48
    },
    xAxis: {
        type: "category",
        name: "工期（天）"
    },
    yAxis: {
        type: "value",
        name:"概率（%）"
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