<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { session, logout } from '../../services/session'
import { workspace, loadWorkspace, capabilities, can, resetWorkspace } from '../../services/workspace'
const router = useRouter(), error = ref(''), busy = ref(false)
onMounted(async () => { try { await loadWorkspace() } catch (e) { error.value = e.message } })
async function leave() { busy.value = true; try { await logout(); resetWorkspace(); await router.push('/login') } catch (e) { error.value = e.message } finally { busy.value = false } }
async function changeBranch() { try { await capabilities() } catch (e) { error.value = e.message } }
</script>
<template>
  <div class="workspace"><aside><RouterLink class="brand" to="/app/overview">P. PolyCafe</RouterLink><p>{{ session.user?.fullName }}</p><small>{{ session.user?.username }}</small>
    <label>Chi nhánh<select v-model="workspace.branch" @change="changeBranch"><option v-for="b in workspace.branches" :key="b.id" :value="b.id">{{ b.name }}</option></select></label>
    <nav><RouterLink to="/app/overview">Tổng quan</RouterLink><RouterLink v-if="can('order.create')" to="/app/pos">Bán hàng</RouterLink><RouterLink to="/app/orders">Đơn và chế biến</RouterLink><RouterLink v-if="can('shift.work')" to="/app/shifts">Ca và tiền mặt</RouterLink><RouterLink v-if="can('report.read')" to="/app/reports">Báo cáo thật</RouterLink><RouterLink v-if="can('audit.read')" to="/app/audit">Nhật ký xử lý</RouterLink><RouterLink v-if="can('staff.manage')" to="/app/staff">Nhân viên</RouterLink><RouterLink v-if="session.user?.canManageProducts" to="/app/data">Danh mục quản trị</RouterLink></nav>
    <a href="http://localhost:8080/app/qr-codes" target="_blank" rel="noopener">In QR tại bàn</a><button :disabled="busy" @click="leave">Đăng xuất</button>
  </aside><main><p v-if="error" role="alert" class="error">{{ error }}</p><p v-if="workspace.loaded && !workspace.branch">Bạn chưa được cấp chi nhánh hoạt động.</p><RouterView v-if="workspace.branch" :key="workspace.branch + $route.fullPath" /></main></div>
</template>
