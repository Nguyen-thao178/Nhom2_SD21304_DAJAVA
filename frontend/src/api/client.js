let csrf = null
let csrfRequest = null
export class ApiError extends Error {
  constructor(status, body) { super(body.message || 'Không thể thực hiện yêu cầu.'); this.status = status; this.body = body }
}
export function clearCsrf() { csrf = null; csrfRequest = null }
async function token() {
  if (csrf) return csrf
  if (!csrfRequest) csrfRequest = fetch('/api/v1/auth/csrf', { credentials: 'include' }).then(async response => {
    if (!response.ok) throw new Error('Không lấy được phiên bảo mật. Kiểm tra kết nối server.')
    return response.json()
  }).then(value => { csrf = value; return value }).finally(() => { csrfRequest = null })
  return csrfRequest
}
export async function api(path, options = {}) {
  const method = options.method || 'GET', headers = { Accept: 'application/json', ...options.headers }
  if (!['GET', 'HEAD'].includes(method)) { const value = await token(); headers[value.headerName] = value.token; headers['Content-Type'] = 'application/json' }
  let response
  try { response = await fetch(path, { ...options, method, headers, credentials: 'include', body: options.body === undefined ? undefined : JSON.stringify(options.body) }) }
  catch { throw new Error('Không kết nối được server. Chưa xác định kết quả; giữ mã yêu cầu và tải lại để kiểm tra trước khi thao tác tiếp.') }
  const data = await response.json().catch(() => ({}))
  if (!response.ok) { if ([401, 403].includes(response.status)) clearCsrf(); throw new ApiError(response.status, data) }
  return data
}
export const command = (path, body) => api('/api/v1/operations' + path, { method: 'POST', body })
export const requestId = () => crypto.randomUUID()
