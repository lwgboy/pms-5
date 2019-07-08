 {
    title: {
        x: "center",
        bottom: 200,
        subtext: '信用等级'
    },
    tooltip: {
        show: true,
        backgroundColor: '#F7F9FB',
        borderColor: '#92DAFF',
        borderWidth: '1px',
        textStyle: {
            color: 'black'
        }

    },
    series: [{
        name: '信用分',
        type: 'gauge',
        min: 350,
        max: 950,
        axisLine: {
            show: true,
            lineStyle: {
                width: 40,
                shadowBlur: 0,
                color: [
                    [0.2, '#E43F3D'],
                    [0.4, '#E98E2C'],
                    [0.6, '#DDBD4D'],
                    [0.8, '#7CBB55'],
                    [1, '#9CD6CE']
                ]
            }
        },
        axisTick: {
            show: true,
            splitNumber: 1
        },
        splitLine: {
            show: true,
            length: 40
        },
        axisLabel: {
            textStyle: {
                fontSize: 12,
                fontWeight: ""
            }
        },
        pointer: {
            show: true,
        },
        detail: {
            offsetCenter: [0, 140],
            textStyle: {
                fontSize: 40
            }
        },
        data: [{
            name: "",
            value: 685
        }]
    }]
}