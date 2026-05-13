import request from '@/utils/request'

export const rewriteText = (data) => request.post('/rewrite/text', data)
export const rewriteDocument = (data) => request.post('/rewrite/document', data, {
  headers: { 'Content-Type': 'multipart/form-data' },
})
export const rewriteRecords = () => request.get('/rewrite/records')
export const fetchPresets = (module, platform, language) => request.get('/rewrite/presets', { params: { module, platform, language } })
