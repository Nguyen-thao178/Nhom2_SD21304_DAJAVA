<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('barista.nam@polycafe.vn')
const password = ref('123456')
const showPassword = ref(false)
const rememberDevice = ref(true)
const errorMessage = ref('')

function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    errorMessage.value = 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu ca làm.'
    return
  }
  errorMessage.value = ''
  router.push('/app/overview')
}
</script>

<template>
  <div class="min-h-screen w-full flex flex-col md:flex-row bg-[#fbfcf7] font-['Plus_Jakarta_Sans',sans-serif] text-[#1a221e] overflow-x-hidden">
    <!-- LEFT COLUMN: Dark green Vietnamese Cafe branding -->
    <section class="w-full md:w-1/2 bg-[#294b3d] text-white flex flex-col justify-between p-8 sm:p-12 lg:p-16 relative overflow-hidden min-h-[520px] md:min-h-screen">
      <div class="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-[#355f4e] blur-3xl opacity-60 pointer-events-none"></div>
      <div class="absolute -bottom-24 -right-24 w-80 h-80 rounded-full bg-[#1b352a] blur-2xl opacity-70 pointer-events-none"></div>

      <!-- Top: Brand Header -->
      <header class="relative z-10 flex items-center gap-3">
        <RouterLink to="/" class="flex items-center gap-3">
          <div
            class="w-9 h-9 rounded-lg flex items-center justify-center text-[#294b3d] text-xl font-bold shadow-sm select-none bg-[#e3b674]"
            style="font-family: Georgia, serif;"
          >
            P.
          </div>
          <span class="text-white text-xl tracking-tight font-bold">PolyCafe</span>
        </RouterLink>
        <span class="ml-2 px-2 py-0.5 text-[10px] font-semibold tracking-wider uppercase bg-[#375e4e] text-[#d6e2cf] rounded-full border border-[#4a7764]">
          POS &amp; Shift OS
        </span>
      </header>

      <!-- Center Content -->
      <main class="relative z-10 my-auto py-8">
        <div class="inline-block uppercase font-bold text-[#afbd9b] text-[10px] tracking-[1.8px] mb-4">
          MỖI NGÀY, MỘT TRẢI NGHIỆM TỐT HƠN
        </div>

        <h1
          class="text-3xl sm:text-4xl lg:text-[46px] leading-[1.15] text-[#f7faf4] mb-4 max-w-lg font-normal"
          style="font-family: Georgia, 'Playfair Display', serif;"
        >
          Một quán cà phê.<br />Nhiều điều kết nối.
        </h1>

        <p class="text-[#cfdad1] text-sm sm:text-base leading-relaxed max-w-md font-normal mb-8">
          Từ ly cà phê đầu ngày đến lần bàn giao cuối ca.
        </p>

        <!-- Decorative Coffee Cup Illustration using pure CSS shapes -->
        <div class="relative w-48 h-36 flex items-center justify-center my-2 select-none" aria-label="Minh họa tách cà phê PolyCafe">
          <div class="absolute -top-4 left-16 w-1.5 h-6 bg-gradient-to-t from-[#afbd9b]/40 to-transparent rounded-full blur-[1px] animate-pulse"></div>
          <div
            class="absolute -top-7 left-24 w-1.5 h-8 bg-gradient-to-t from-[#afbd9b]/50 to-transparent rounded-full blur-[1px] animate-pulse"
            style="animation-delay: 0.3s;"
          ></div>
          <div
            class="absolute -top-3 left-32 w-1.5 h-5 bg-gradient-to-t from-[#afbd9b]/35 to-transparent rounded-full blur-[1px] animate-pulse"
            style="animation-delay: 0.6s;"
          ></div>

          <div class="absolute bottom-2 w-44 h-8 bg-[#203c30] rounded-full border border-[#3d6554] shadow-lg flex items-center justify-center">
            <div class="w-36 h-5 rounded-full border border-[#487360]/40"></div>
          </div>

          <div class="absolute right-5 bottom-8 w-11 h-16 rounded-r-2xl border-4 border-[#e3b674] border-l-0 shadow-inner"></div>

          <div class="relative w-32 h-[88px] bg-gradient-to-b from-[#f2efe9] to-[#d8d3c7] rounded-b-[42px] rounded-t-lg border-2 border-[#e6e2d8] shadow-2xl flex items-start justify-center pt-1.5 overflow-hidden">
            <div class="w-28 h-10 bg-[#422818] rounded-full border border-[#2b190f] relative overflow-hidden flex items-center justify-center shadow-inner">
              <div class="w-full h-full bg-[#52331f] rounded-full relative flex items-center justify-center">
                <div class="absolute rounded-full border-[1.5px] border-[#e3b674]/45 w-20 h-7"></div>
                <div class="absolute rounded-full border-[1.5px] border-[#e3b674]/70 w-14 h-5"></div>
                <div class="absolute rounded-full border-[1.5px] border-white/80 bg-[#e3b674]/30 w-8 h-3"></div>
                <div class="absolute w-1.5 h-6 bg-white/70 rounded-full rotate-45 blur-[0.5px]"></div>
              </div>
            </div>
            <div class="absolute left-2.5 top-3 w-2.5 h-12 bg-white/40 rounded-full blur-[1px]"></div>
          </div>

          <div class="absolute bottom-6 w-5 h-5 rounded-full bg-[#e3b674] text-[#294b3d] font-bold text-[9px] flex items-center justify-center shadow">
            P
          </div>
        </div>

        <div class="flex items-center gap-6 mt-6 pt-6 border-t border-[#3d6453]/60 text-xs text-[#b8c7b8]">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-[#7ec89b] inline-block animate-ping"></span>
            <span>Ca sáng: 06:30 - 14:30</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-[#e3b674] inline-block"></span>
            <span>Chi nhánh: Hoàn Kiếm, HN</span>
          </div>
        </div>
      </main>

      <footer class="relative z-10 pt-4 text-[#8ea492] text-[8px] uppercase tracking-[1.7px] font-semibold">
        KHÔNG GIAN LÀM VIỆC CỦA ĐỘI NGŨ POLYCAFE
      </footer>
    </section>

    <!-- RIGHT COLUMN: Vertically Centered Form -->
    <section class="w-full md:w-1/2 bg-[#fbfcf7] flex flex-col justify-between p-8 sm:p-12 lg:p-16 relative">
      <div class="flex justify-between md:justify-end items-center text-xs text-[#6e7870] mb-6 md:mb-0">
        <RouterLink to="/" class="md:hidden text-[#294b3d] font-semibold hover:underline">← Trang chủ</RouterLink>
        <span class="inline-flex items-center gap-1.5 bg-[#f0f2eb] px-3 py-1.5 rounded-full text-[#48534b] font-medium">
          <svg class="w-3.5 h-3.5 text-[#294b3d]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          Hệ thống trực tuyến v2.4
        </span>
      </div>

      <div class="max-w-[420px] w-full mx-auto my-auto py-4">
        <div class="mb-7">
          <p class="text-[11px] font-bold text-[#657762] tracking-[1.5px] uppercase mb-1.5">
            CHÀO MỪNG TRỞ LẠI
          </p>
          <h2 class="text-[29px] leading-tight text-[#1d2722] mb-2 tracking-tight font-semibold">
            Bắt đầu một ca mới.
          </h2>
          <p class="text-sm text-[#667268] leading-relaxed">
            Đăng nhập bằng tài khoản được cấp cho bạn.
          </p>
        </div>

        <!-- Error Banner (chỉ hiện khi có lỗi) -->
        <div
          v-if="errorMessage"
          class="mb-5 p-3 rounded-[9px] bg-[#fbebea] border border-[#f5c6cb]/40 flex items-start gap-2.5 text-[#a23d39] text-xs leading-snug"
        >
          <svg class="w-4 h-4 mt-0.5 shrink-0 text-[#a23d39]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" stroke-width="2"></circle>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01"></path>
          </svg>
          <div>
            <span class="font-semibold">Đăng nhập không thành công:</span> {{ errorMessage }}
          </div>
        </div>

        <form class="space-y-4" @submit.prevent="handleLogin">
          <div>
            <label for="username" class="block text-xs font-semibold text-[#323d36] mb-1.5">
              Tên đăng nhập / Mã nhân viên
            </label>
            <div class="relative">
              <input
                id="username"
                v-model="username"
                type="text"
                name="username"
                placeholder="VD: NV1029 hoặc email"
                class="w-full border border-[#dce0d5] rounded-[9px] py-[11px] pl-3.5 pr-10 text-sm bg-white text-[#242c27] placeholder-[#9aa39b] focus:outline-none focus:ring-2 focus:ring-[#76957c] focus:border-[#76957c] transition-all"
                required
              />
              <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-[#869288]">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-1.5">
              <label for="password" class="block text-xs font-semibold text-[#323d36]">
                Mật khẩu ca làm
              </label>
              <a href="#" class="text-[11px] font-medium text-[#294b3d] hover:underline hover:text-[#1c352a]" @click.prevent>
                Quên mã PIN / Mật khẩu?
              </a>
            </div>
            <div class="relative">
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                name="password"
                placeholder="Nhập mật khẩu ca làm"
                class="w-full border border-[#dce0d5] rounded-[9px] py-[11px] pl-3.5 pr-11 text-sm bg-white text-[#242c27] placeholder-[#9aa39b] focus:outline-none focus:ring-2 focus:ring-[#76957c] focus:border-[#76957c] transition-all"
                required
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 flex items-center pr-3 text-[#79857b] hover:text-[#294b3d] transition-colors focus:outline-none"
                title="Hiện / ẩn mật khẩu"
                @click="showPassword = !showPassword"
              >
                <svg v-if="!showPassword" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18"></path>
                </svg>
              </button>
            </div>
          </div>

          <div class="flex items-center justify-between pt-1">
            <label class="flex items-center gap-2 cursor-pointer select-none">
              <input v-model="rememberDevice" type="checkbox" class="w-4 h-4 rounded text-[#294b3d] border-[#dce0d5] focus:ring-[#76957c]" />
              <span class="text-xs text-[#525f56]">Ghi nhớ thiết bị này tại quầy</span>
            </label>
            <span class="text-[11px] text-[#7d8b80] bg-[#f0f2eb] px-2 py-0.5 rounded">Quầy Thu Ngân #02</span>
          </div>

          <div class="pt-2">
            <button
              type="submit"
              class="w-full min-h-[42px] rounded-[9px] font-semibold text-[13px] tracking-[0.2px] bg-[#294b3d] hover:bg-[#1f3b30] text-white flex items-center justify-center gap-2 shadow-sm hover:-translate-y-[1px] active:translate-y-0 transition-all cursor-pointer"
            >
              <span>Đăng nhập</span>
              <span class="text-sm font-normal">→</span>
            </button>
          </div>

          <div class="pt-2 flex items-center justify-center gap-2">
            <button
              type="button"
              class="w-full py-2 px-3 border border-[#dce0d5] hover:border-[#b8c2b4] hover:bg-[#f5f7f0] rounded-[9px] text-xs font-semibold text-[#404c44] flex items-center justify-center gap-1.5 transition"
              @click="handleLogin"
            >
              <svg class="w-3.5 h-3.5 text-[#294b3d]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              Quét thẻ nhân viên NFC / Mã QR
            </button>
          </div>
        </form>

        <div class="mt-6 pt-5 border-t border-[#e8ece3] text-center">
          <p class="text-xs text-[#6e7a71]">
            Cần hỗ trợ tài khoản? <a href="#" class="font-semibold text-[#294b3d] hover:underline" @click.prevent>Liên hệ quản lý quán.</a>
          </p>
        </div>
      </div>

      <div class="mt-8 text-center text-[11px] text-[#8e9990] tracking-wide">
        PolyCafe · Không gian quản lý quán cà phê
      </div>
    </section>
  </div>
</template>
