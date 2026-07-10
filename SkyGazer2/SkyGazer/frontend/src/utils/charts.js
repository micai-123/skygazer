import * as echarts from 'echarts'

export const chartColors = {
  highTemp: '#fb923c',
  lowTemp: '#60a5fa',
  precipitation: '#3b82f6',
  pressure: '#22c55e'
}

export const createBaseChartOption = (customOptions = {}) => {
  return {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: {
        lineStyle: { color: '#e2e8f0' }
      },
      axisLabel: {
        color: '#64748b',
        fontWeight: 500
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: {
        color: '#64748b'
      },
      splitLine: {
        lineStyle: {
          color: '#e2e8f0',
          type: 'dashed'
        }
      }
    },
    ...customOptions
  }
}

export const createLineSeries = (data, color, name) => {
  return {
    name,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 10,
    data,
    lineStyle: {
      width: 3,
      color
    },
    itemStyle: {
      color,
      borderWidth: 3,
      borderColor: '#fff'
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [
          { offset: 0, color: color.replace(')', ', 0.3)').replace('rgb', 'rgba') },
          { offset: 1, color: color.replace(')', ', 0)').replace('rgb', 'rgba') }
        ]
      }
    }
  }
}

export const createBarSeries = (data, color) => {
  return {
    type: 'bar',
    data,
    itemStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [
          { offset: 0, color: color },
          { offset: 1, color: color }
        ]
      },
      borderRadius: [4, 4, 0, 0]
    }
  }
}

export const createTooltip = (formatter) => {
  return {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e2e8f0',
    borderWidth: 1,
    textStyle: {
      color: '#1e293b'
    },
    formatter
  }
}

export const createAreaGradient = (color) => {
  const rgbaColor = color.startsWith('#') 
    ? `rgba(${parseInt(color.slice(1, 3), 16)}, ${parseInt(color.slice(3, 5), 16)}, ${parseInt(color.slice(5, 7), 16)}`
    : color
  
  return {
    type: 'linear',
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: [
      { offset: 0, color: `${rgbaColor}, 0.3)` },
      { offset: 1, color: `${rgbaColor}, 0)` }
    ]
  }
}

export const initChart = (chartRef, options) => {
  if (!chartRef) return null
  
  const chart = echarts.init(chartRef)
  chart.setOption(options)
  
  return chart
}

export const disposeChart = (chart) => {
  if (chart) {
    chart.dispose()
  }
}

export const resizeChart = (chart) => {
  if (chart) {
    chart.resize()
  }
}
