<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../api/client'
import { workspace } from '../../services/workspace'
const rows = ref([]), orders = ref([]), invoice = ref(''), error = ref(''), busy = ref(false)
async function load() { busy.value = true; error.value = ''; try { rows.value = await api('/api/v1/operations/audit?' + new URLSearchParams({ branch: workspace.branch, ...(invoice.value ? { invoice: invoice.value } : {}) })) } catch (e) { error.value = e.message } finally { busy.value = false } }
onMounted(async () => { try { orders.value = await api('/api/v1/operations/orders?branch=' + workspace.branch); await load() } catch (e) { error.value = e.message } })
</script>
<template><h1>Nhật ký và chuỗi trách nhiệm</h1><p>Hiển thị trước/sau và người thực hiện. Mật khẩu, PIN, token và khóa bí mật đã được loại bỏ. Việc đọc nhật ký cũng được ghi lại.</p><label>Lọc đơn<select v-model="invoice"><option value="">Nhật ký chi nhánh, tối đa 300 sự kiện</option><option v-for="o in orders" :key="o.id" :value="o.id">{{ o.invoice_no }}</option></select></label><button :disabled="busy" @click="load">Tra cứu</button><p v-if="error" class="error">{{ error }}</p><article v-for="a in rows" :key="a.id" class="card"><h2>{{ a.action }} · {{ a.actor_name || a.actor_id }}</h2><p>{{ new Date(a.created_at).toLocaleString('vi-VN') }} · Thiết bị {{ a.device_id }} · Mã yêu cầu {{ a.request_id }}</p><p>{{ a.entity_type }} · {{ a.entity_id }} · {{ a.reason }}</p><details><summary>Nội dung trước và sau</summary><h3>Trước</h3><pre>{{ JSON.stringify(a.before_data, null, 2) }}</pre><h3>Sau</h3><pre>{{ JSON.stringify(a.after_data, null, 2) }}</pre></details></article></template>
