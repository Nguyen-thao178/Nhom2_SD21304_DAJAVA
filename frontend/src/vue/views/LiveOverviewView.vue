<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../api/client'
import { workspace, money, labels } from '../../services/workspace'
const orders = ref([]), error = ref(''), loading = ref(true)
onMounted(async () => { try { orders.value = await api('/api/v1/operations/orders?branch=' + workspace.branch) } catch (e) { error.value = e.message } finally { loading.value = false } })
</script>
<template><h1>Tổng quan chi nhánh</h1><p>Dữ liệu lấy trực tiếp từ server. Danh sách gồm tối đa 100 đơn gần nhất, không thay báo cáo cả kỳ.</p><p v-if="error" class="error">{{ error }}</p><p v-if="loading">Đang tải…</p><section class="metrics"><div class="card"><strong>{{ orders.length }}</strong><span>Đơn đã tải</span></div><div class="card"><strong>{{ orders.filter(x => x.status === 'draft').length }}</strong><span>Chờ xác nhận</span></div><div class="card"><strong>{{ orders.filter(x => ['confirmed','partially_paid'].includes(x.status)).length }}</strong><span>Đang phục vụ</span></div></section><table><thead><tr><th>Đơn</th><th>Trạng thái</th><th>Phải thu</th><th>Đã thu</th></tr></thead><tbody><tr v-for="o in orders" :key="o.id"><td><RouterLink :to="'/app/orders?id=' + o.id">{{ o.invoice_no }}</RouterLink></td><td>{{ labels[o.status] || o.status }}</td><td>{{ money(o.total) }}</td><td>{{ money(o.paid_total) }}</td></tr></tbody></table></template>
