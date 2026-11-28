<script setup>
import { ref } from 'vue'

const showCashModal = ref(false)
const cashBalance = ref('3.250.000')
const cashNote = ref('')

const recentOrders = [
  {
    code: '#HD-1092',
    time: '10:14',
    items: 'Cà phê muối đặc biệt (x2)',
    sub: 'Bạc xỉu sữa tươi (x1)',
    subWarn: false,
    channel: 'Tại bàn (Bàn 04)',
    channelIcon: 'table_restaurant',
    isPartner: false,
    total: '102.000 ₫',
    status: 'Đang pha chế',
    statusClass: 'bg-[#fef3c7] text-[#b45309]',
  },
  {
    code: '#HD-1091',
    time: '10:08',
    items: 'Trà sen vàng macchiato (x1)',
    sub: 'Ít đường, 70% đá',
    subWarn: true,
    channel: 'Mang đi (Takeaway)',
    channelIcon: 'shopping_bag',
    isPartner: false,
    total: '48.000 ₫',
    status: 'Sẵn sàng trả món',
    statusClass: 'bg-[#e0f2fe] text-[#0369a1]',
  },
  {
    code: '#HD-1090',
    time: '09:55',
    items: 'Cold Brew Cam Vàng (x2)',
    sub: 'Croissant Hạnh Nhân (x1)',
    subWarn: false,
    channel: 'Tại bàn (Bàn 02)',
    channelIcon: 'table_restaurant',
    isPartner: false,
    total: '128.000 ₫',
    status: 'Hoàn thành',
    statusClass: 'bg-[#dcfce7] text-[#15803d]',
  },
  {
    code: '#HD-1089',
    time: '09:42',
    items: 'Cà phê đen đá Sài Gòn (x3)',
    sub: 'Kèm túi giữ nhiệt đối tác',
    subWarn: false,
    channel: 'GrabFood #882',
    channelIcon: 'two_wheeler',
    isPartner: true,
    total: '75.000 ₫',
    status: 'Hoàn thành',
    statusClass: 'bg-[#dcfce7] text-[#15803d]',
  },
  {
    code: '#HD-1088',
    time: '09:30',
    items: 'Bánh Mousse Cà Phê (x1)',
    sub: 'Trà Đào Cam Sả (x1)',
    subWarn: false,
    channel: 'Sân thượng 01',
    channelIcon: 'deck',
    isPartner: false,
    total: '82.000 ₫',
    status: 'Hoàn thành',
    statusClass: 'bg-[#dcfce7] text-[#15803d]',
  },
]

const quickLinks = [
  {
    num: '01',
    title: 'Mở quầy POS bán hàng',
    desc: 'Tạo đơn mới, in hóa đơn tạm tính & quét mã QR',
    to: '/app/pos',
    primary: true,
  },
  {
    num: '02',
    title: 'Sơ đồ bàn & phục vụ',
    desc: 'Kiểm tra bàn trống (18/24 bàn có khách)',
    to: '/app/data?table=dining_table',
  },
  {
    num: '03',
    title: 'Màn hình KDS pha chế',
    desc: '3 ticket đang chờ thực hiện theo thứ tự',
    to: '/app/data?table=invoice_line',
  },
  {
    num: '04',
    title: 'Kiểm kê hạt & sữa tươi',
    desc: 'Cảnh báo sữa tươi thanh trùng còn 4 hộp',
    to: '/app/data?table=tbl_kho_nguyen_lieu',
    warn: true,
  },
  {
    num: '05',
    title: 'Bàn giao & chốt ca',
    desc: 'In biên bản kết ca và số dư thực tế',
    to: '/app/data?table=tbl_tai_khoan',
  },
]

function saveCashCount() {
  showCashModal.value = false
  cashNote.value = ''
}
</script>

<template>
  <div class="max-w-[1440px] mx-auto p-6 lg:p-8 w-full flex flex-col">
    <!-- Top Welcome Banner -->
    <section
      class="relative overflow-hidden rounded-[16px] min-h-[213px] p-[32px_36px] bg-[#e9ede0] shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-6 border border-[#dbe2d0]"
    >
      <div class="absolute -right-12 -bottom-24 w-96 h-96 rounded-full bg-[#dfe5d4] opacity-50 blur-2xl pointer-events-none"></div>
      <div class="absolute left-1/2 -top-16 w-64 h-64 rounded-full bg-[#f4f7ee] opacity-60 blur-3xl pointer-events-none"></div>

      <div class="relative z-10 flex flex-col max-w-[580px]">
        <div class="flex items-center gap-2">
          <span class="inline-block w-2 h-2 rounded-full bg-[#294b3d] animate-pulse"></span>
          <span class="text-[10px] font-bold uppercase tracking-[1.8px] text-[#5f6f57]">CA SÁNG · 06:30 - 14:30 · QUẦY THU NGÂN #01</span>
        </div>
        <h1 class="font-headline-lg text-[27px] font-bold text-[#294b3d] mt-2 mb-1.5 leading-snug font-serif">
          Chào Nguyễn Nam.
        </h1>
        <p class="font-body-md text-[13px] text-[#6b7762] leading-relaxed max-w-[520px]">
          Hệ thống đang đồng bộ 14 đơn mở, quầy pha chế KDS hoạt động bình thường với 100% nguyên liệu sẵn sàng.
        </p>
        <div class="flex flex-wrap items-center gap-3 mt-[22px]">
          <RouterLink
            to="/app/pos"
            class="inline-flex items-center gap-2 bg-[#294b3d] text-[#ffffff] text-[12px] font-semibold rounded-[9px] px-[22px] py-[11px] hover:bg-[#1f382e] transition-all transform active:scale-95 shadow-[0_4px_12px_rgba(41,75,61,0.18)]"
          >
            <span>Đến quầy bán hàng</span>
            <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
          </RouterLink>
          <button
            class="inline-flex items-center gap-1.5 bg-white/70 backdrop-blur-sm text-[#2f382f] text-[12px] font-semibold rounded-[9px] px-[18px] py-[11px] hover:bg-white transition-colors border border-[#ced7c3]"
            type="button"
            @click="showCashModal = true"
          >
            <span class="material-symbols-outlined text-[16px] text-[#5f6f57]">payments</span>
            <span>Kiểm đếm tiền đầu ca</span>
          </button>
        </div>
      </div>

      <div class="relative z-10 flex flex-row md:flex-col items-center md:items-end justify-between self-stretch md:self-auto gap-4">
        <div class="flex items-center gap-2 bg-white/75 backdrop-blur-sm px-3.5 py-1.5 rounded-full shadow-sm border border-[#ced7c3]">
          <span class="w-2 h-2 rounded-full bg-[#e3b674]"></span>
          <span class="text-[11px] font-semibold text-[#294b3d] tracking-wide">Trạng thái: Sẵn sàng nhận đơn</span>
        </div>
        <div class="flex items-center gap-4 pr-2">
          <div class="flex flex-col text-right">
            <span class="font-serif italic text-[26px] text-[#788a6d] opacity-85 leading-none select-none">good coffee.</span>
            <span class="text-[10px] uppercase font-semibold tracking-widest text-[#8a9980] mt-1">Robusta Cầu Đất #1985</span>
          </div>
          <div class="relative w-[54px] h-[54px] rounded-2xl bg-[#dfe5d4]/70 flex items-center justify-center text-[#294b3d] shadow-sm">
            <svg class="w-7 h-7 text-[#294b3d]" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" viewBox="0 0 24 24">
              <path d="M18 8h1a4 4 0 0 1 0 8h-1"></path>
              <path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"></path>
              <line x1="6" x2="6" y1="1" y2="4"></line>
              <line x1="10" x2="10" y1="1" y2="4"></line>
              <line x1="14" x2="14" y1="1" y2="4"></line>
            </svg>
            <span class="absolute -top-1 -right-1 w-3.5 h-3.5 bg-[#e3b674] rounded-full border-2 border-[#e9ede0]"></span>
          </div>
        </div>
      </div>
    </section>

    <!-- 4-Column Stats Grid -->
    <section class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[18px] mt-[24px]">
      <div class="bg-[#ffffff] rounded-[12px] p-[20px_22px] transition-transform duration-200 hover:-translate-y-0.5 shadow-[0_4px_20px_rgba(41,75,61,0.04)] border border-[#e6e8df]">
        <div class="flex items-center justify-between">
          <span class="text-[11px] font-semibold uppercase tracking-[0.8px] text-[#7a8076]">DOANH THU CA HIỆN TẠI</span>
          <span class="material-symbols-outlined text-[18px] text-[#2e7d32]">trending_up</span>
        </div>
        <div class="font-currency-stat text-[27px] font-semibold text-[#294b3d] tracking-[-0.5px] mt-[10px] mb-[6px]">
          8.420.000 ₫
        </div>
        <div class="flex items-center gap-1.5">
          <span class="inline-block bg-[#e8f5e9] text-[#2e7d32] text-[10px] font-semibold px-[7px] py-[2px] rounded-[4px]">
            +18% so với cùng giờ hôm qua
          </span>
        </div>
      </div>

      <div class="bg-[#ffffff] rounded-[12px] p-[20px_22px] transition-transform duration-200 hover:-translate-y-0.5 shadow-[0_4px_20px_rgba(41,75,61,0.04)] border border-[#e6e8df]">
        <div class="flex items-center justify-between">
          <span class="text-[11px] font-semibold uppercase tracking-[0.8px] text-[#7a8076]">ĐƠN ĐÃ PHỤC VỤ</span>
          <span class="material-symbols-outlined text-[18px] text-[#d97706]">local_cafe</span>
        </div>
        <div class="font-currency-stat text-[27px] font-semibold text-[#2f382f] tracking-[-0.5px] mt-[10px] mb-[6px]">
          142 ly
        </div>
        <div class="flex items-center gap-1.5 text-[10px] font-medium text-[#d97706]">
          <span class="w-1.5 h-1.5 rounded-full bg-[#d97706] animate-ping"></span>
          <span>3 đơn đang pha chế tại quầy KDS</span>
        </div>
      </div>

      <div class="bg-[#ffffff] rounded-[12px] p-[20px_22px] transition-transform duration-200 hover:-translate-y-0.5 shadow-[0_4px_20px_rgba(41,75,61,0.04)] border border-[#e6e8df]">
        <div class="flex items-center justify-between">
          <span class="text-[11px] font-semibold uppercase tracking-[0.8px] text-[#7a8076]">GIÁ TRỊ ĐƠN TRUNG BÌNH</span>
          <span class="material-symbols-outlined text-[18px] text-[#7a8076]">receipt</span>
        </div>
        <div class="font-currency-stat text-[27px] font-semibold text-[#2f382f] tracking-[-0.5px] mt-[10px] mb-[6px]">
          59.300 ₫
        </div>
        <div class="text-[10px] text-[#7a8076] truncate">
          Cao nhất: <span class="font-semibold text-[#2f382f]">Combo Bạc xỉu &amp; Croissant</span>
        </div>
      </div>

      <div class="bg-[#ffffff] rounded-[12px] p-[20px_22px] transition-transform duration-200 hover:-translate-y-0.5 shadow-[0_4px_20px_rgba(41,75,61,0.04)] border border-[#e6e8df]">
        <div class="flex items-center justify-between">
          <span class="text-[11px] font-semibold uppercase tracking-[0.8px] text-[#7a8076]">TIỀN MẶT TRONG KÉT</span>
          <span class="material-symbols-outlined text-[18px] text-[#294b3d]">lock_clock</span>
        </div>
        <div class="font-currency-stat text-[27px] font-semibold text-[#2f382f] tracking-[-0.5px] mt-[10px] mb-[6px]">
          3.250.000 ₫
        </div>
        <div class="flex items-center gap-1.5 text-[10px] font-semibold text-[#294b3d]">
          <span class="material-symbols-outlined text-[13px]">check_circle</span>
          <span>Khớp 100% hóa đơn tiền mặt thu ngân</span>
        </div>
      </div>
    </section>

    <!-- Main Operational Section -->
    <section class="grid grid-cols-1 lg:grid-cols-[1.7fr_1fr] gap-[20px] mt-[24px]">
      <!-- LEFT PANEL: Đơn vừa ghi nhận -->
      <div class="bg-[#ffffff] rounded-[14px] overflow-hidden shadow-[0_4px_20px_rgba(41,75,61,0.04)] flex flex-col border border-[#e6e8df]">
        <div class="flex items-center justify-between p-[18px_22px] bg-white border-b border-[#f0f2eb]">
          <div class="flex items-center gap-3">
            <h2 class="text-[16px] font-bold text-[#2f382f] leading-none">Đơn vừa ghi nhận</h2>
            <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-[#e9ede0] text-[#294b3d] text-[10px] font-semibold">
              <span class="w-1.5 h-1.5 rounded-full bg-[#294b3d] animate-pulse"></span>
              Cập nhật tức thì
            </span>
          </div>
          <RouterLink
            to="/app/data?table=tbl_don_hang"
            class="inline-flex items-center gap-1 text-[12px] font-semibold text-[#294b3d] hover:underline"
          >
            <span>Xem tất cả 58 hóa đơn</span>
            <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
          </RouterLink>
        </div>

        <div class="w-full overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-[#f7f8f3] text-[11px] font-bold uppercase tracking-[0.6px] text-[#7a8076] border-b border-[#ebeee4]">
                <th class="py-3 px-4 font-semibold">Mã đơn</th>
                <th class="py-3 px-3 font-semibold">Thời gian</th>
                <th class="py-3 px-4 font-semibold min-w-[200px]">Món gọi &amp; ghi chú</th>
                <th class="py-3 px-3 font-semibold">Hình thức</th>
                <th class="py-3 px-4 font-semibold text-right">Tổng tiền</th>
                <th class="py-3 px-4 font-semibold text-center">Trạng thái</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-[#f2f4ec] text-[13px] text-[#2f382f]">
              <tr v-for="order in recentOrders" :key="order.code" class="hover:bg-[#fafbf7] transition-colors cursor-pointer">
                <td class="py-3.5 px-4 font-semibold text-[#294b3d] font-mono text-[12px]">{{ order.code }}</td>
                <td class="py-3.5 px-3 text-[#7a8076] font-mono text-[12px]">{{ order.time }}</td>
                <td class="py-3.5 px-4">
                  <div class="font-medium text-[#2f382f]">{{ order.items }}</div>
                  <div :class="order.subWarn ? 'text-[11px] text-[#b45309] font-medium italic' : 'text-[11px] text-[#7a8076]'">
                    {{ order.sub }}
                  </div>
                </td>
                <td class="py-3.5 px-3">
                  <span
                    class="inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded"
                    :class="order.isPartner ? 'text-[#00897b] bg-[#e0f2f1]' : 'text-[#2f382f] bg-[#f2f5ee]'"
                  >
                    <span class="material-symbols-outlined text-[13px]" :class="order.isPartner ? '' : 'text-[#5f6f57]'">
                      {{ order.channelIcon }}
                    </span>
                    {{ order.channel }}
                  </span>
                </td>
                <td class="py-3.5 px-4 font-semibold text-right text-[#294b3d]">{{ order.total }}</td>
                <td class="py-3.5 px-4 text-center">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold" :class="order.statusClass">
                    {{ order.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mt-auto p-[14px_22px] bg-[#fbfbf8] flex items-center justify-between text-[12px] text-[#7a8076] border-t border-[#ebeee4]">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[16px] text-[#294b3d]">info</span>
            <span>Đơn thanh toán gần nhất được thực hiện qua hình thức <strong>VietQR Pro</strong></span>
          </div>
          <span class="font-medium text-[#294b3d]">Tỉ lệ hoàn thành món: 97.4%</span>
        </div>
      </div>

      <!-- RIGHT PANEL: Đi nhanh đến -->
      <div class="bg-[#ffffff] rounded-[14px] p-[22px] shadow-[0_4px_20px_rgba(41,75,61,0.04)] flex flex-col gap-[16px] border border-[#e6e8df]">
        <div>
          <h2 class="text-[16px] font-bold text-[#2f382f] leading-tight">Đi nhanh đến</h2>
          <p class="text-[11px] text-[#7a8076] mt-0.5">Lối tắt thao tác nhanh trong ca</p>
        </div>

        <div class="flex flex-col gap-2">
          <RouterLink
            v-for="link in quickLinks"
            :key="link.num"
            :to="link.to"
            class="group flex items-center justify-between p-2.5 rounded-[10px] hover:bg-[#fafbf7] transition-all border border-[#f0f2ec]"
          >
            <div class="flex items-center gap-3">
              <div
                class="w-[28px] h-[28px] rounded-[8px] flex items-center justify-center text-[12px] font-bold shrink-0"
                :class="link.primary ? 'bg-[#294b3d] text-white' : 'bg-[#e9ede0] text-[#294b3d]'"
              >
                {{ link.num }}
              </div>
              <div class="flex flex-col">
                <div class="flex items-center gap-1.5">
                  <span class="text-[13px] font-semibold text-[#2f382f] group-hover:text-[#294b3d] transition-colors leading-tight">
                    {{ link.title }}
                  </span>
                  <span v-if="link.warn" class="w-1.5 h-1.5 rounded-full bg-[#ba1a1a]"></span>
                </div>
                <span class="text-[11px] line-clamp-1 mt-0.5" :class="link.warn ? 'text-[#ba1a1a] font-medium' : 'text-[#7a8076]'">
                  {{ link.desc }}
                </span>
              </div>
            </div>
            <span class="material-symbols-outlined text-[18px] text-[#7a8076] group-hover:text-[#294b3d] group-hover:translate-x-0.5 transition-all">
              chevron_right
            </span>
          </RouterLink>
        </div>

        <div class="mt-auto bg-[#edf2e9] rounded-[10px] p-[14px] flex items-center justify-between border border-[#dce5d6]">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-full bg-white flex items-center justify-center text-[#294b3d] shadow-sm font-bold text-[11px]">
              HT
            </div>
            <div class="flex flex-col">
              <span class="text-[10px] font-semibold uppercase tracking-wider text-[#5f6f57]">Trưởng ca hôm nay</span>
              <span class="text-[12px] font-bold text-[#294b3d]">Hoàng Thảo (Lead Barista)</span>
            </div>
          </div>
          <div class="text-right">
            <span class="text-[10px] font-semibold uppercase tracking-wider text-[#5f6f57] block">Dự kiến kết ca</span>
            <span class="text-[12px] font-bold text-[#294b3d] font-mono">14:30</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Bottom Micro-Section -->
    <section class="mt-[24px] grid grid-cols-1 md:grid-cols-3 gap-[18px]">
      <div class="bg-white rounded-[12px] p-4 shadow-[0_4px_20px_rgba(41,75,61,0.04)] flex items-center gap-3.5 border border-[#e6e8df]">
        <div class="w-10 h-10 rounded-xl bg-[#e9ede0] text-[#294b3d] flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">coffee_maker</span>
        </div>
        <div class="flex flex-col">
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-bold text-[#2f382f]">Máy pha La Marzocco</span>
            <span class="w-2 h-2 rounded-full bg-[#2e7d32]"></span>
          </div>
          <span class="text-[11px] text-[#7a8076]">Áp suất 9.1 bar · Nhiệt độ 93.4°C chuẩn</span>
        </div>
      </div>

      <div class="bg-white rounded-[12px] p-4 shadow-[0_4px_20px_rgba(41,75,61,0.04)] flex items-center gap-3.5 border border-[#e6e8df]">
        <div class="w-10 h-10 rounded-xl bg-[#fef6e9] text-[#b45309] flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">loyalty</span>
        </div>
        <div class="flex flex-col">
          <span class="text-[13px] font-bold text-[#2f382f]">Happy Morning Hour</span>
          <span class="text-[11px] text-[#7a8076]">Tặng 01 bánh quy bơ cho đơn từ 99k (Còn 22 suất)</span>
        </div>
      </div>

      <div class="bg-white rounded-[12px] p-4 shadow-[0_4px_20px_rgba(41,75,61,0.04)] flex flex-col justify-center border border-[#e6e8df]">
        <div class="flex justify-between items-center mb-1.5">
          <span class="text-[11px] font-bold text-[#7a8076] uppercase tracking-wider">Chỉ tiêu doanh thu ca sáng</span>
          <span class="text-[12px] font-bold text-[#294b3d]">84% / 10.000.000₫</span>
        </div>
        <div class="w-full h-2 rounded-full bg-[#eef2e7] overflow-hidden">
          <div class="h-full bg-[#294b3d] rounded-full" style="width: 84%;"></div>
        </div>
      </div>
    </section>

    <!-- Cash Drawer Modal -->
    <div
      v-if="showCashModal"
      class="fixed inset-0 z-50 bg-[#151e16]/40 backdrop-blur-sm flex items-center justify-center p-4"
      @click.self="showCashModal = false"
    >
      <div class="bg-white rounded-[16px] w-full max-w-[480px] p-6 shadow-2xl border border-[#dbe2d0]">
        <div class="flex items-center justify-between pb-3 border-b border-[#f0f2eb]">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[#294b3d]">account_balance_wallet</span>
            <h3 class="font-bold text-[16px] text-[#2f382f]">Kiểm đếm tiền mặt đầu ca</h3>
          </div>
          <button type="button" class="text-[#7a8076] hover:text-[#2f382f] p-1 rounded-md" @click="showCashModal = false">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>

        <div class="py-4 space-y-3">
          <div class="p-3 rounded-lg bg-[#f7f8f3] text-[12px] text-[#5f6f57]">
            Số dư tiền mặt ban đầu mặc định được bàn giao từ ca tối: <strong>2.000.000 ₫</strong>
          </div>
          <div>
            <label class="block text-[12px] font-semibold text-[#2f382f] mb-1">Số tiền thực tế trong két (VND)</label>
            <input
              v-model="cashBalance"
              class="w-full h-10 px-3 rounded-lg border border-[#e6e8df] text-[14px] font-semibold text-[#294b3d] focus:outline-none focus:border-[#294b3d]"
              type="text"
            />
          </div>
          <div>
            <label class="block text-[12px] font-semibold text-[#2f382f] mb-1">Ghi chú đối soát</label>
            <textarea
              v-model="cashNote"
              class="w-full p-2.5 rounded-lg border border-[#e6e8df] text-[12px] text-[#2f382f] focus:outline-none focus:border-[#294b3d]"
              placeholder="Ví dụ: Đổi thêm 200k tiền lẻ 5.000đ từ quầy tạp hóa..."
              rows="2"
            ></textarea>
          </div>
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-[#f0f2eb]">
          <button
            type="button"
            class="px-4 py-2 text-[12px] font-semibold text-[#7a8076] hover:bg-[#f2f4ec] rounded-lg"
            @click="showCashModal = false"
          >
            Đóng
          </button>
          <button
            type="button"
            class="px-5 py-2 text-[12px] font-semibold text-white bg-[#294b3d] hover:bg-[#1f382e] rounded-lg shadow-sm"
            @click="saveCashCount"
          >
            Xác nhận số dư
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
