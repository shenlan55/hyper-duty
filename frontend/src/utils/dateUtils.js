import dayjs from 'dayjs'

export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  return dayjs(date).format(format)
}

export function formatDateTime(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

export function formatTime(date) {
  if (!date) return ''
  return dayjs(date).format('HH:mm:ss')
}