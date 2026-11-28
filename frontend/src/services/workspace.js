import { reactive } from 'vue'
import { api } from '../api/client'
export const workspace = reactive({ branches: [], branch: '', capabilities: [], loaded: false })
export async function loadWorkspace() {
  workspace.branches = await api('/api/v1/operations/branches')
  if (!workspace.branches.some(b => b.id === workspace.branch)) workspace.branch = workspace.branches[0]?.id || ''
  workspace.loaded = true
  await capabilities()
}
export async function capabilities() {
  workspace.capabilities = workspace.branch ? await api('/api/v1/operations/capabilities?branch=' + encodeURIComponent(workspace.branch)) : []
}
export const can = action => workspace.capabilities.includes(action)
export function resetWorkspace() { workspace.branch = ''; workspace.branches = []; workspace.capabilities = []; workspace.loaded = false }
export function money(value) { return value === null || value === undefined ? 'Chưa đủ dữ liệu' : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value) }
export const labels = { draft: 'Chờ xác nhận', confirmed: 'Đã xác nhận', partially_paid: 'Thu một phần', paid: 'Đã thu đủ', refunded: 'Đã hoàn toàn bộ', cancelled: 'Đã hủy', pending_accept: 'Chờ xác nhận', waiting: 'Chờ chế biến', in_progress: 'Đang chế biến', ready: 'Sẵn sàng', handed_over: 'Đã giao đủ', canceled: 'Đã hủy', open: 'Đang mở', closed: 'Đã đóng', pending: 'Chờ duyệt', approved: 'Đã duyệt', rejected: 'Từ chối', succeeded: 'Thành công', collect: 'Thu', refund: 'Hoàn' }
