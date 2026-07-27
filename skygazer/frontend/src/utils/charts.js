import * as echarts from 'echarts'
import { SKYGAZER_THEME, installSkygazerTheme, palette } from './echartsTheme'

installSkygazerTheme()

export const chartColors = {
  highTemp: palette.warn,
  lowTemp: palette.signal,
  precipitation: palette.signalStrong,
  pressure: palette.ok
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
        lineStyle: { color: palette.border }
      },
      axisLabel: {
        color: palette.textMuted,
        fontWeight: 500
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: {
        color: palette.textMuted
      },
      splitLine: {
        lineStyle: {
          color: palette.border,
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
      borderColor: palette.surface1
    },
    areaStyle: {
      color: createAreaGradient(color)
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
    backgroundColor: palette.surface2,
    borderColor: palette.border,
    borderWidth: 1,
    textStyle: {
      color: palette.textPrimary
    },
    extraCssText: 'box-shadow:0 12px 28px rgba(0,0,0,0.5);border-radius:12px;',
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

  const chart = echarts.init(chartRef, SKYGAZER_THEME)
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
