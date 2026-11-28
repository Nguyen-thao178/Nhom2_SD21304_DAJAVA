<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login } from '../../services/session'
const username = ref(''), password = ref(''), busy = ref(false), error = ref(''), router = useRouter(), route = useRoute()
async function submit() { busy.value = true; error.value = ''; try { const user = await login(username.value.trim(), password.value); password.value = ''; if (user.roles.includes('customer')) { location.assign('/customer'); return } const next = typeof route.query.next === 'string' && route.query.next.startsWith('/app/') ? route.query.next : '/app/overview'; await router.push(next) } catch (e) { error.value = e.message } finally { busy.value = false } }
</script>
<template><main class="login"><section class="card"><RouterLink class="brand" to="/">P. PolyCafe</RouterLink><h1>Đăng nhập ca làm</h1><p>Thông tin được xác thực trên server. Quyền và chi nhánh do quán cấp.</p><form @submit.prevent="submit"><label>Tên đăng nhập<input v-model="username" required maxlength="60" autocomplete="username"></label><label>Mật khẩu<input v-model="password" type="password" required maxlength="72" autocomplete="current-password"></label><p v-if="error" role="alert" class="error">{{ error }}</p><button :disabled="busy">{{ busy ? 'Đang xác thực…' : 'Đăng nhập' }}</button></form><p><a href="/customer">Khách hàng thành viên và QR</a></p></section></main></template>
