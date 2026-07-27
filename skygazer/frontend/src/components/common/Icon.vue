<template>
  <svg
    :width="size"
    :height="size"
    :viewBox="viewBox"
    :aria-hidden="!ariaLabel"
    :aria-label="ariaLabel"
    role="img"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <component :is="iconComponent" />
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: [Number, String],
    default: 24
  },
  ariaLabel: {
    type: String,
    default: ''
  }
})

const viewBox = computed(() => {
  return '0 0 24 24'
})

const iconComponent = computed(() => {
  const icons = {
    sun: 'IconSun',
    moon: 'IconMoon',
    cloud: 'IconCloud',
    'cloud-sun': 'IconCloudSun',
    rain: 'IconRain',
    'cloud-rain': 'IconCloudRain',
    snow: 'IconSnow',
    thunderstorm: 'IconThunderstorm',
    fog: 'IconFog',
    hail: 'IconHail',
    wind: 'IconWind',
    'wind-strong': 'IconWindStrong',
    humidity: 'IconHumidity',
    temperature: 'IconTemperature',
    uv: 'IconUV',
    visibility: 'IconVisibility',
    pressure: 'IconPressure'
  }
  return icons[props.name] || 'IconDefault'
})
</script>

<script>
import { h } from 'vue'

const IconSun = {
  render() {
    return h('g', [
      h('circle', { cx: '12', cy: '12', r: '5', fill: '#FCD34D' }),
      h('line', { x1: '12', y1: '1', x2: '12', y2: '3', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '12', y1: '21', x2: '12', y2: '23', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '4.22', y1: '4.22', x2: '5.64', y2: '5.64', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '18.36', y1: '18.36', x2: '19.78', y2: '19.78', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '1', y1: '12', x2: '3', y2: '12', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '21', y1: '12', x2: '23', y2: '12', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '4.22', y1: '19.78', x2: '5.64', y2: '18.36', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('line', { x1: '18.36', y1: '5.64', x2: '19.78', y2: '4.22', stroke: '#FCD34D', 'stroke-width': '2', 'stroke-linecap': 'round' })
    ])
  }
}

const IconWind = {
  render() {
    return h('g', { stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }, [
      h('path', { d: 'M12 2L12 22M12 2L8 6M12 2L16 6' }),
      h('path', { d: 'M17.5 10C19 11 20 12.5 20 14.5C20 17.5 17.5 20 14 20H12' })
    ])
  }
}

const IconHumidity = {
  render() {
    return h('g', { fill: 'currentColor' }, [
      h('path', {
        d: 'M12 2.69L17.66 8.35C19.55 10.24 20.5 12.67 20.5 15.15C20.5 18.5 17.85 21.15 14.5 21.15C12.87 21.15 11.37 20.5 10.25 19.4L12 17.65L13.75 19.4C12.63 20.5 11.13 21.15 9.5 21.15C6.15 21.15 3.5 18.5 3.5 15.15C3.5 12.67 4.45 10.24 6.34 8.35L12 2.69Z',
        opacity: '0.3'
      })
    ])
  }
}

const IconUV = {
  render() {
    return h('g', { stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }, [
      h('circle', { cx: '12', cy: '12', r: '4' }),
      h('path', { d: 'M12 2V4M12 20V22M2 12H4M20 12H22' })
    ])
  }
}

const IconVisibility = {
  render() {
    return h('g', { stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }, [
      h('path', { d: 'M2 12H22M12 2V22' }),
      h('circle', { cx: '12', cy: '12', r: '3', fill: 'currentColor', opacity: '0.3' })
    ])
  }
}

const IconPressure = {
  render() {
    return h('g', { stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }, [
      h('path', { d: 'M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12' }),
      h('path', { d: 'M12 6V12L16 14' })
    ])
  }
}

const IconDefault = {
  render() {
    return h('circle', { cx: '12', cy: '12', r: '10', stroke: 'currentColor', 'stroke-width': '2', fill: 'none' })
  }
}

export default {
  components: {
    IconSun,
    IconWind,
    IconHumidity,
    IconUV,
    IconVisibility,
    IconPressure,
    IconDefault
  }
}
</script>