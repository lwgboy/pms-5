{
    title: {
        x: "center",
        bottom:40,
        subtext: '<subtext>'
    },
    series: [{
        type: 'gauge',
        min:0,
        max:10,
        axisLabel: {show: false},
        axisTick:{show:false},
        axisLine: {
            show: true,
            lineStyle: {
                width: 10,
                shadowBlur: 0,
                color: [
                    [0.2, '#9CD6CE'],
                    [0.4, '#7CBB55'],
                    [0.6, '#DDBD4D'],
                    [0.8, '#E98E2C'],
                    [1, '#E43F3D']
                ]
            }
        },
        splitLine:{length:12},
        pointer:{
            width:4
        },
        detail:false,
        data: [{
            value: "<value>"
        }]

    }]
}