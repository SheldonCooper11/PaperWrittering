import request from '@/utils/request'

export const rewriteText = (data) => request.post('/rewrite/text', data)
export const precheckDocument = (data) => request.post('/rewrite/document/precheck', data, {
  headers: { 'Content-Type': 'multipart/form-data' },
})
export const rewriteDocument = (data) => request.post('/rewrite/document', data, {
  headers: { 'Content-Type': 'multipart/form-data' },
})
export const rewriteRecords = () => request.get('/rewrite/records')
export const fetchPresets = (module, platform, language) => request.get('/rewrite/presets', { params: { module, platform, language } })
export const fetchPlatforms = (module, language) => request.get('/rewrite/platforms', { params: { module, language } })
export const fetchDetectionOptions = (language) => request.get('/detection/options', { params: { language } })
export const checkDetection = (data) => request.post('/detection/check', data)
