import request from '@/utils/request'

export const checkUsername = (username) => request.get('/auth/check-username', { params: { username } })
export const checkPhone = (phone) => request.get('/auth/check-phone', { params: { phone } })
export const sendSms = (data) => request.post('/sms/send', data)
export const register = (data) => request.post('/auth/register', data)
export const passwordLogin = (data) => request.post('/auth/login/password', data)
export const smsLogin = (data) => request.post('/auth/login/sms', data)
export const resetPassword = (data) => request.post('/auth/password/reset', data)
export const getUserInfo = () => request.get('/auth/me')
export const changePassword = (data) => request.put('/auth/password', data)
export const redeemCode = (code) => request.post('/redeem', { code })
