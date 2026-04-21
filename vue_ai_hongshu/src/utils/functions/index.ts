// 获取当前日期
export function getCurrentDate() {
  const date = new Date()
  const day = date.getDate()
  const month = date.getMonth() + 1
  const year = date.getFullYear()
  return `${year}-${month}-${day}`
}

// 获取当前时间段
export function getGreeting() {
  const now = new Date()
  const hours = now.getHours()

  if (hours < 12)
    return '上午好:sun_with_face:'
  else if (hours >= 12 && hours < 18)
    return '下午好:custard:'
  else
    return '晚上好:crescent_moon:'
}

// 解析头像/图片地址：
// - 已是 http/https、data、blob：直接返回
// - 以 /assets/ 开头（Vite 打包本地资源）：直接返回
// - 以 / 开头或相对路径：拼接 VITE_GLOB_API_URL，并规范斜杠
export function resolveAvatarUrl(input?: string | null): string {
  const value = (input || '').toString().trim()
  if (!value)
    return ''

  const isAbsolute = /^(?:https?:)?\/\//i.test(value) || value.startsWith('data:') || value.startsWith('blob:')
  if (isAbsolute)
    return value

  if (value.startsWith('/assets/'))
    return value

  const base = (import.meta as any).env?.VITE_GLOB_API_URL || ''
  if (!base)
    return value

  const baseNoSlash = base.replace(/\/+$/, '')
  const pathWithSlash = value.startsWith('/') ? value : `/${value}`
  return `${baseNoSlash}${pathWithSlash}`
}

// 构造 API 完整地址（用于上传等 action）
// 优先级：VITE_APP_BASE_API -> VITE_GLOB_API_URL -> window.location.origin
export function resolveApiUrl(path: string): string {
  const p = (path || '').toString().trim()
  const env: any = (import.meta as any).env || {}
  const base = env.VITE_APP_BASE_API || env.VITE_GLOB_API_URL || (typeof window !== 'undefined' ? window.location.origin : '')
  const baseNoSlash = (base || '').replace(/\/+$/, '')
  const pathWithSlash = p.startsWith('/') ? p : `/${p}`
  return `${baseNoSlash}${pathWithSlash}`
}
