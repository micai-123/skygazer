export const weatherIcons = {
  sun: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <circle cx="12" cy="12" r="5" fill="#FFA726"/>
    <path d="M12 2V4M12 20V22M2 12H4M20 12H22M4.93 4.93L6.34 6.34M17.66 17.66L19.07 19.07M4.93 19.07L6.34 17.66M17.66 6.34L19.07 4.93" stroke="#FFA726" stroke-width="2" stroke-linecap="round"/>
  </svg>`,
  'cloud-sun': `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <circle cx="8" cy="8" r="4" fill="#FFA726"/>
    <path d="M18 20H8C5.79 20 4 18.21 4 16C4 13.79 5.79 12 8 12C8.28 12 8.55 12.02 8.81 12.07C9.76 10.82 11.28 10 13 10C15.76 10 18 12.24 18 15V20Z" fill="#64B5F6"/>
  </svg>`,
  cloud: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <path d="M18 20H8C5.79 20 4 18.21 4 16C4 13.79 5.79 12 8 12C8.28 12 8.55 12.02 8.81 12.07C9.76 10.82 11.28 10 13 10C15.76 10 18 12.24 18 15V20Z" fill="#94A3B8"/>
  </svg>`,
  moon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z" fill="#94A3B8"/>
  </svg>`,
  'moon-fog': `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z" fill="#64B5F6" opacity="0.6"/>
    <path d="M3 21H21" stroke="#94A3B8" stroke-width="2" stroke-linecap="round"/>
  </svg>`,
  rain: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <path d="M18 14H8C5.79 14 4 12.21 4 10C4 7.79 5.79 6 8 6C8.28 6 8.55 6.02 8.81 6.07C9.76 4.82 11.28 4 13 4C15.76 4 18 6.24 18 9V14Z" fill="#64B5F6"/>
    <path d="M8 18V20M12 17V19M16 18V20" stroke="#42A5F5" stroke-width="2" stroke-linecap="round"/>
  </svg>`,
  wind: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none">
    <path d="M17.7 7.7C18.1 7.3 18.1 6.7 17.7 6.3C17.3 5.9 16.7 5.9 16.3 6.3L6.3 16.3C5.9 16.7 5.9 17.3 6.3 17.7C6.7 18.1 7.3 18.1 7.7 17.7L17.7 7.7Z" fill="#64B5F6"/>
    <path d="M3 12H21M3 6H12M12 18H21" stroke="#42A5F5" stroke-width="2" stroke-linecap="round"/>
  </svg>`
}

export const lifestyleIcons = {
  't-shirt': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none">
    <path d="M6 2L2 6V20C2 21.1 2.9 22 4 22H20C21.1 22 22 21.1 22 20V6L18 2H6Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M12 2V8M8 2H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>`,
  dumbbells: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none">
    <path d="M20.57 14.86L22 13.43L20.57 12L17 15.57L8.43 7L12 3.43L10.57 2L9.14 3.43L7.71 2L5.57 4.14L4.14 2.71L2.71 4.14L4.14 5.57L2 7.71L3.43 9.14L2 10.57L3.43 12L7 8.43L15.57 17L12 20.57L13.43 22L14.86 20.57L16.29 22L18.43 19.86L19.86 21.29L21.29 19.86L19.86 18.43L22 16.29L20.57 14.86Z" fill="currentColor"/>
  </svg>`,
  wash: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none">
    <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
    <path d="M3 10H21" stroke="currentColor" stroke-width="2"/>
    <circle cx="8" cy="15" r="2" fill="currentColor"/>
    <circle cx="16" cy="15" r="2" fill="currentColor"/>
  </svg>`,
  mask: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none">
    <path d="M12 2C8 2 4 4 4 8V16C4 18 6 20 8 20H16C18 20 20 18 20 16V8C20 4 16 2 12 2Z" stroke="currentColor" stroke-width="2"/>
    <path d="M4 12H20M8 8V10M16 8V10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>`
}

export const getWeatherIcon = (icon) => {
  return weatherIcons[icon] || weatherIcons.sun
}

export const getLifestyleIcon = (icon) => {
  return lifestyleIcons[icon] || ''
}
