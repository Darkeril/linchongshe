import {
	baseUrl
} from '@/.env.js'

// let baseUrl = "http://localhost/dev-api/web"
// let baseUrl = 'http://115.190.73.103/prod-api/web'

let captchaBaseUrl = baseUrl + '/web';

export const myRequest = (option = {}) => {
	return new Promise((reslove, reject) => {
		uni.request({
			url: captchaBaseUrl + option.url,
			data: option.data,
			method: option.method || "GET",
			success: (result) => {
				reslove(result)
			},
			fail: (error) => {
				reject(error)
			}
		})
	})
}