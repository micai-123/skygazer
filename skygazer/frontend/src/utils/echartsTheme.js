import * as echarts from 'echarts'

export const SKYGAZER_THEME = 'skygazer-dark'

export const palette = {
  signal: '#34E3E0',
  signalStrong: '#16C7C4',
  warn: '#FF9F45',
  danger: '#F87171',
  ok: '#34D399',
  violet: '#7C8CF8',
  textPrimary: '#EAF2FF',
  textSecondary: '#9FB0CC',
  textMuted: '#6B7A99',
  surface1: '#141B2D',
  surface2: '#1B2438',
  surface3: '#22304D',
  border: 'rgba(159, 176, 204, 0.14)',
  series: ['#34E3E0', '#FF9F45', '#34D399', '#7C8CF8', '#F87171', '#5FD9D6', '#C084FC']
}

let registered = false

export function installSkygazerTheme() {
  if (registered) return
  echarts.registerTheme(SKYGAZER_THEME, {
    color: palette.series,
    backgroundColor: 'transparent',
    textStyle: {
      color: palette.textSecondary,
      fontFamily: 'Sora, -apple-system, BlinkMacSystemFont, sans-serif'
    },
    title: {
      textStyle: { color: palette.textPrimary, fontWeight: 700 },
      subtextStyle: { color: palette.textMuted }
    },
    legend: {
      textStyle: { color: palette.textSecondary }
    },
    tooltip: {
      backgroundColor: palette.surface2,
      borderColor: palette.border,
      borderWidth: 1,
      textStyle: { color: palette.textPrimary },
      extraCssText: 'box-shadow:0 12px 28px rgba(0,0,0,0.5);border-radius:12px;'
    },
    categoryAxis: {
      axisLine: { lineStyle: { color: palette.border } },
      axisTick: { lineStyle: { color: palette.border } },
      axisLabel: { color: palette.textMuted },
      splitLine: { show: false }
    },
    valueAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: palette.textMuted },
      splitLine: { lineStyle: { color: palette.border, type: 'dashed' } }
    },
    line: { symbol: 'circle' }
  })
  registered = true
}
