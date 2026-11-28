<script setup>
import { ref } from 'vue'

const selectedPeriod = ref('7 ngày qua')
const fromDate = ref('18/02/2026')
const toDate = ref('24/02/2026')
const paymentMethod = ref('Tất cả phương thức')

const periods = ['Hôm nay', '7 ngày qua', 'Tháng này', 'Quý 1/2026']

const dailyChart = [
  { date: '18/02', day: 'T4', label: '7.65 Tr', height: 98 },
  { date: '19/02', day: 'T5', label: '8.12 Tr', height: 104 },
  { date: '20/02', day: 'T6', label: '9.45 Tr', height: 122 },
  { date: '21/02', day: 'T7', label: '11.28 Tr', height: 148, peak: true },
  { date: '22/02', day: 'CN', label: '10.84 Tr', height: 138 },
  { date: '23/02', day: 'T2', label: '6.88 Tr', height: 88 },
  { date: 'Hôm nay', day: 'Đang bán', label: '4.02 Tr', height: 52, inProgress: true },
]

const paymentBreakdown = [
  { label: 'VietQR Pro / Chuyển khoản', amount: '39.600.000 ₫ (68%)', width: '68%', barClass: 'bg-primary-container' },
  { label: 'Tiền mặt tại quầy', amount: '15.140.000 ₫ (26%)', width: '26%', barClass: 'bg-secondary' },
  { label: 'Thẻ ngân hàng / POS quẹt', amount: '3.500.000 ₫ (6%)', width: '6%', barClass: 'bg-outline' },
]

const dailyRows = [
  { date: '24/02/2026', sub: '(Hôm nay - Ca sáng)', orders: '74 đơn', gross: '4.100.000 ₫', refunds: '-80.000 ₫', net: '4.020.000 ₫', open: true },
  { date: '23/02/2026', sub: 'Thứ 2', orders: '128 đơn', gross: '6.950.000 ₫', refunds: '-70.000 ₫', net: '6.880.000 ₫' },
  { date: '22/02/2026', sub: 'Chủ Nhật', orders: '198 đơn', gross: '10.980.000 ₫', refunds: '-140.000 ₫', net: '10.840.000 ₫' },
  { date: '21/02/2026', sub: 'Thứ 7 · Cao điểm', orders: '208 đơn', gross: '11.450.000 ₫', refunds: '-170.000 ₫', net: '11.280.000 ₫', peak: true },
  { date: '20/02/2026', sub: 'Thứ 6', orders: '172 đơn', gross: '9.560.000 ₫', refunds: '-110.000 ₫', net: '9.450.000 ₫' },
  { date: '19/02/2026', sub: 'Thứ 5', orders: '151 đơn', gross: '8.180.000 ₫', refunds: '-60.000 ₫', net: '8.120.000 ₫' },
  { date: '18/02/2026', sub: 'Thứ 4', orders: '153 đơn', gross: '7.700.000 ₫', refunds: '-50.000 ₫', net: '7.650.000 ₫' },
]
</script>

<template>
  <div class="px-6 lg:px-8 py-7 space-y-6 max-w-[1400px] w-full mx-auto">
    <!-- 1. HEADER SECTION -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
      <div class="space-y-1.5">
        <div class="flex items-center gap-2">
          <span class="inline-block w-2 h-2 rounded-full bg-secondary"></span>
          <span class="font-label-sm text-label-sm uppercase tracking-[1.5px] text-outline font-semibold">
            BÁO CÁO &amp; PHÂN TÍCH DOANH THU
          </span>
        </div>
        <h1 class="font-headline-lg text-headline-lg text-on-surface font-semibold tracking-tight">
          Báo cáo doanh thu
        </h1>
        <p class="font-body-md text-body-md text-on-surface-variant max-w-2xl leading-relaxed">
          Tổng hợp doanh số bán hàng, đối soát hoàn tiền và biến động doanh thu thuần theo chu kỳ kinh doanh của quán.
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-1.5 p-1 bg-surface-container rounded-lg self-start md:self-auto">
        <button
          v-for="p in periods"
          :key="p"
          type="button"
          class="px-3.5 py-1.5 rounded font-label-md text-label-md transition-all"
          :class="
            selectedPeriod === p
              ? 'bg-primary-container text-on-primary shadow-sm font-semibold'
              : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container-high'
          "
          @click="selectedPeriod = p"
        >
          {{ p }}
        </button>
        <button
          type="button"
          class="px-3 py-1.5 rounded font-label-md text-label-md text-on-surface-variant hover:text-on-surface hover:bg-surface-container-high transition-colors flex items-center gap-1"
        >
          <span>Tùy chỉnh</span>
          <span class="material-symbols-outlined text-[15px]">date_range</span>
        </button>
      </div>
    </div>

    <!-- 2. FILTER TOOLBAR SECTION -->
    <div class="bg-surface-container-lowest rounded-xl p-5 md:p-6 shadow-sm space-y-3.5">
      <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3.5 flex-1 max-w-3xl">
          <div class="flex flex-col gap-1.5">
            <label class="font-label-sm text-label-sm uppercase tracking-wider text-outline font-semibold">Từ ngày</label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-outline text-[18px]">calendar_today</span>
              <input
                v-model="fromDate"
                class="w-full pl-9 pr-3 py-2 bg-surface-container-lowest border border-[#e6e8df] text-on-surface font-body-md text-body-md rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-container/20 focus:bg-surface-container-low transition-all"
                type="text"
              />
            </div>
          </div>

          <div class="flex flex-col gap-1.5">
            <label class="font-label-sm text-label-sm uppercase tracking-wider text-outline font-semibold">Đến ngày</label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-outline text-[18px]">event</span>
              <input
                v-model="toDate"
                class="w-full pl-9 pr-3 py-2 bg-surface-container-lowest border border-[#e6e8df] text-on-surface font-body-md text-body-md rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-container/20 focus:bg-surface-container-low transition-all"
                type="text"
              />
            </div>
          </div>

          <div class="flex flex-col gap-1.5">
            <label class="font-label-sm text-label-sm uppercase tracking-wider text-outline font-semibold">Kênh &amp; Phương thức</label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-outline text-[18px]">payments</span>
              <select
                v-model="paymentMethod"
                class="w-full pl-9 pr-8 py-2 bg-surface-container-lowest border border-[#e6e8df] text-on-surface font-body-md text-body-md rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-container/20 appearance-none cursor-pointer"
              >
                <option>Tất cả phương thức</option>
                <option>Tiền mặt (Cash)</option>
                <option>VietQR Pro (Mã động)</option>
                <option>Chuyển khoản POS</option>
              </select>
              <span class="material-symbols-outlined absolute right-2.5 text-outline text-[18px] pointer-events-none">unfold_more</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-2.5 pt-2 lg:pt-0">
          <button
            class="flex-1 sm:flex-none flex items-center justify-center gap-2 px-5 py-2.5 rounded-lg bg-primary-container text-on-primary font-label-lg text-label-lg shadow hover:bg-primary transition-all"
            type="button"
          >
            <span class="material-symbols-outlined text-[18px]">search</span>
            <span>Xem báo cáo</span>
          </button>
          <button
            class="flex-1 sm:flex-none flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface font-label-lg text-label-lg transition-colors"
            type="button"
          >
            <span class="material-symbols-outlined text-[18px] text-outline">download</span>
            <span>Tải CSV</span>
          </button>
        </div>
      </div>

      <div class="flex items-center gap-2 text-outline font-body-sm text-body-sm pt-2">
        <span class="material-symbols-outlined text-[15px] text-secondary">info</span>
        <span>Theo ngày kinh doanh của hóa đơn đã thu đủ tiền hoặc đã hoàn tất đối soát ca làm việc. Dữ liệu đồng bộ thời gian thực mỗi 5 phút.</span>
      </div>
    </div>

    <!-- 3. STATS GRID -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-surface-container-lowest rounded-xl p-5 shadow-sm flex flex-col justify-between">
        <div class="flex items-center justify-between text-outline mb-2">
          <span class="font-label-sm text-label-sm uppercase tracking-wider font-semibold">SỐ HÓA ĐƠN</span>
          <div class="w-8 h-8 rounded-lg bg-surface-container-low flex items-center justify-center text-primary-container">
            <span class="material-symbols-outlined text-[18px]">receipt_long</span>
          </div>
        </div>
        <div class="space-y-2">
          <div class="font-currency-stat text-currency-stat text-on-surface font-bold tracking-tight">1.084</div>
          <div class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-surface-container-low text-primary-container font-label-sm text-label-sm font-semibold">
            <span class="material-symbols-outlined text-[14px]">trending_up</span>
            <span>+12.4% so với kỳ trước</span>
          </div>
        </div>
      </div>

      <div class="bg-surface-container-lowest rounded-xl p-5 shadow-sm flex flex-col justify-between">
        <div class="flex items-center justify-between text-outline mb-2">
          <span class="font-label-sm text-label-sm uppercase tracking-wider font-semibold">GIÁ TRỊ BÁN</span>
          <div class="w-8 h-8 rounded-lg bg-surface-container-low flex items-center justify-center text-secondary">
            <span class="material-symbols-outlined text-[18px]">shopping_bag</span>
          </div>
        </div>
        <div class="space-y-2">
          <div class="font-currency-stat text-currency-stat text-on-surface font-bold tracking-tight">58.920.000 ₫</div>
          <div class="font-body-sm text-body-sm text-outline flex items-center gap-1.5">
            <span class="w-1 h-1 rounded-full bg-outline"></span>
            <span>Trung bình 54.350 ₫ / đơn</span>
          </div>
        </div>
      </div>

      <div class="bg-surface-container-lowest rounded-xl p-5 shadow-sm flex flex-col justify-between">
        <div class="flex items-center justify-between text-outline mb-2">
          <span class="font-label-sm text-label-sm uppercase tracking-wider font-semibold">ĐÃ HOÀN TIỀN</span>
          <div class="w-8 h-8 rounded-lg bg-error-container/40 flex items-center justify-center text-error">
            <span class="material-symbols-outlined text-[18px]">keyboard_return</span>
          </div>
        </div>
        <div class="space-y-2">
          <div class="font-currency-stat text-currency-stat text-error font-bold tracking-tight">680.000 ₫</div>
          <div class="inline-flex items-center gap-1 text-error font-body-sm text-body-sm font-medium">
            <span class="material-symbols-outlined text-[14px]">error_outline</span>
            <span>11 đơn hoàn (1.01% tổng đơn)</span>
          </div>
        </div>
      </div>

      <div class="bg-surface-container-lowest rounded-xl p-5 shadow-sm flex flex-col justify-between">
        <div class="flex items-center justify-between text-outline mb-2">
          <span class="font-label-sm text-label-sm uppercase tracking-wider font-semibold text-primary-container">DOANH THU THUẦN</span>
          <div class="w-8 h-8 rounded-lg bg-primary-container text-on-primary flex items-center justify-center">
            <span class="material-symbols-outlined text-[18px]">monetization_on</span>
          </div>
        </div>
        <div class="space-y-2">
          <div class="font-currency-stat text-currency-stat text-primary-container font-bold tracking-tight">58.240.000 ₫</div>
          <div class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-surface-container text-primary-container font-label-sm text-label-sm font-semibold">
            <span class="material-symbols-outlined text-[14px]">verified</span>
            <span>+11.8% tăng trưởng ròng</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. VISUAL CHART & PERFORMANCE PANEL -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <div class="lg:col-span-8 bg-surface-container-lowest rounded-xl p-6 shadow-sm flex flex-col justify-between">
        <div>
          <div class="flex flex-col sm:flex-row sm:items-center justify-between pb-4 gap-2">
            <div>
              <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">Doanh thu theo ngày</h2>
              <p class="font-body-sm text-body-sm text-outline mt-0.5">
                Biểu đồ cột thể hiện doanh thu thuần từng ngày trong 7 ngày gần nhất (18/02 - 24/02/2026)
              </p>
            </div>
            <div class="flex items-center gap-3 self-start sm:self-auto font-label-sm text-label-sm">
              <div class="flex items-center gap-1.5">
                <span class="w-3 h-3 rounded-sm bg-primary-container"></span>
                <span class="text-on-surface-variant font-medium">Doanh thu thuần</span>
              </div>
              <div class="flex items-center gap-1.5">
                <span class="w-2.5 h-2.5 rounded-full bg-error"></span>
                <span class="text-on-surface-variant font-medium">Hoàn tiền</span>
              </div>
              <div class="flex items-center gap-1.5">
                <span class="w-3 h-0.5 bg-outline-variant"></span>
                <span class="text-outline">Mục tiêu 9Tr</span>
              </div>
            </div>
          </div>

          <div class="relative bg-surface-container-low/40 rounded-xl p-5 pt-9 mt-2 flex flex-col justify-end min-h-[260px] overflow-x-auto">
            <div class="absolute left-4 right-4 top-[32%] flex items-center pointer-events-none z-10">
              <div class="w-full border-t border-dashed border-outline-variant"></div>
              <span class="font-label-sm text-[9px] bg-surface-container-highest px-1.5 py-0.5 rounded text-outline ml-2 whitespace-nowrap">
                Mục tiêu: 9.0 Tr
              </span>
            </div>
            <div class="absolute left-4 right-4 bottom-[38px] border-t border-outline-variant/40 pointer-events-none"></div>

            <div class="relative z-20 flex items-end justify-between px-2 sm:px-6 w-full min-w-[480px] h-[180px]">
              <div
                v-for="bar in dailyChart"
                :key="bar.date"
                class="flex flex-col items-center gap-2 group cursor-pointer relative"
              >
                <span
                  v-if="bar.peak"
                  class="absolute -top-7 px-2 py-0.5 rounded-full bg-secondary-container text-on-secondary-container font-label-sm text-[9px] font-bold tracking-wide shadow-sm whitespace-nowrap"
                >
                  ĐỈNH TUẦN
                </span>
                <span
                  class="font-label-md text-label-md font-semibold transition-colors"
                  :class="bar.peak ? 'text-primary-container font-bold' : bar.inProgress ? 'text-secondary' : 'text-on-surface group-hover:text-primary'"
                >
                  {{ bar.label }}
                </span>
                <div
                  class="w-8 sm:w-11 rounded-t-md transition-all duration-300"
                  :class="
                    bar.peak
                      ? 'bg-primary-container group-hover:bg-primary shadow-sm'
                      : bar.inProgress
                        ? 'bg-secondary-fixed-dim/70 border-t-2 border-dashed border-secondary'
                        : 'bg-surface-tint group-hover:bg-primary'
                  "
                  :style="{ height: bar.height + 'px' }"
                ></div>
                <div class="flex flex-col items-center">
                  <span
                    class="font-label-md text-label-md font-semibold"
                    :class="bar.peak ? 'text-primary-container font-bold' : bar.inProgress ? 'text-secondary' : 'text-on-surface'"
                  >
                    {{ bar.date }}
                  </span>
                  <span
                    class="font-label-sm text-[10px]"
                    :class="bar.peak ? 'text-primary-container font-semibold' : bar.inProgress ? 'text-secondary' : 'text-outline'"
                  >
                    {{ bar.day }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-4 pt-3 flex items-center justify-between text-body-sm text-outline">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[16px] text-secondary">workspace_premium</span>
            <span>
              Ngày đạt doanh thu cao nhất: <strong class="text-on-surface font-semibold">Thứ Bảy 21/02/2026</strong> (11.280.000 ₫ · 208 hóa đơn).
            </span>
          </div>
          <span class="hidden md:inline font-label-sm text-label-sm bg-surface-container px-2.5 py-1 rounded text-on-surface-variant font-medium">
            Hoàn tiền trung bình: 1.15%
          </span>
        </div>
      </div>

      <div class="lg:col-span-4 flex flex-col gap-4">
        <div class="bg-surface-container-lowest rounded-xl p-5 shadow-sm space-y-4 flex-1">
          <div class="flex items-center justify-between">
            <span class="font-label-sm text-label-sm uppercase tracking-wider text-outline font-semibold">CƠ CẤU PHƯƠNG THỨC</span>
            <span class="font-label-sm text-label-sm text-primary-container font-semibold">7 ngày qua</span>
          </div>
          <div class="space-y-3">
            <div v-for="pm in paymentBreakdown" :key="pm.label">
              <div class="flex justify-between font-label-md text-label-md mb-1">
                <span class="text-on-surface font-medium">{{ pm.label }}</span>
                <span class="text-on-surface font-semibold">{{ pm.amount }}</span>
              </div>
              <div class="w-full bg-surface-container h-2 rounded-full overflow-hidden">
                <div class="h-full rounded-full" :class="pm.barClass" :style="{ width: pm.width }"></div>
              </div>
            </div>
          </div>
          <div class="p-3 bg-surface-container-low rounded-lg space-y-1 mt-2">
            <span class="font-label-sm text-[10px] uppercase font-bold text-primary-container tracking-wider">LƯU Ý THU NGÂN</span>
            <p class="font-body-sm text-[12px] text-on-surface-variant leading-relaxed">
              Tỷ lệ thanh toán không tiền mặt đạt 74%. Số dư két tiền mặt chốt ca tối qua khớp 100% với biên bản kiểm quỹ.
            </p>
          </div>
        </div>

        <div class="bg-surface-container-lowest rounded-xl p-5 shadow-sm flex items-center justify-between">
          <div class="space-y-1">
            <span class="font-label-sm text-label-sm uppercase tracking-wider text-outline font-semibold">KHUNG GIỜ ĐÔNG KHÁCH</span>
            <div class="font-headline-sm text-headline-sm font-bold text-on-surface">07:30 - 09:30</div>
            <p class="font-body-sm text-body-sm text-on-surface-variant">Chiếm 41.5% doanh số ca sáng</p>
          </div>
          <div class="w-12 h-12 rounded-full bg-secondary-container/40 flex items-center justify-center text-secondary">
            <span class="material-symbols-outlined text-[24px]">coffee</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 5. DETAILED DATA TABLE SECTION -->
    <div class="bg-surface-container-lowest rounded-xl shadow-sm overflow-hidden">
      <div class="px-6 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-surface-container-lowest">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-primary-container text-[20px]">table_chart</span>
          <h3 class="font-headline-sm text-headline-sm font-semibold text-on-surface">Chi tiết doanh thu theo ngày</h3>
          <span class="font-label-sm text-label-sm px-2.5 py-0.5 rounded-full bg-surface-container text-on-surface-variant font-medium">
            (7 ngày kinh doanh)
          </span>
        </div>
        <button
          class="inline-flex items-center gap-1.5 text-primary-container hover:text-primary font-label-md text-label-md font-semibold transition-colors self-start sm:self-auto"
          type="button"
        >
          <span class="material-symbols-outlined text-[17px]">file_download</span>
          <span>Xuất dữ liệu Excel (.xlsx)</span>
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-surface-container-low text-outline font-label-sm text-label-sm uppercase tracking-[1px]">
              <th class="py-3 px-6 font-semibold">NGÀY BÁN</th>
              <th class="py-3 px-4 font-semibold text-center">SỐ HÓA ĐƠN</th>
              <th class="py-3 px-6 font-semibold text-right">GIÁ TRỊ BÁN</th>
              <th class="py-3 px-6 font-semibold text-right">HOÀN TIỀN</th>
              <th class="py-3 px-6 font-semibold text-right">DOANH THU THUẦN</th>
              <th class="py-3 px-6 font-semibold text-center">TRẠNG THÁI ĐỐI SOÁT</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-surface-container font-body-md text-body-md text-on-surface">
            <tr
              v-for="row in dailyRows"
              :key="row.date"
              :class="row.peak ? 'bg-surface-container-low/30 hover:bg-surface-container-low/70' : 'hover:bg-surface-container-low/50'"
              class="transition-colors"
            >
              <td class="py-3.5 px-6">
                <div v-if="row.open" class="flex items-center gap-2.5">
                  <span class="w-2 h-2 rounded-full bg-secondary animate-pulse"></span>
                  <div>
                    <span class="font-semibold text-on-surface">{{ row.date }}</span>
                    <span class="font-label-sm text-label-sm text-secondary font-medium ml-1.5">{{ row.sub }}</span>
                  </div>
                </div>
                <div v-else class="flex items-center gap-2">
                  <span :class="row.peak ? 'font-bold text-primary-container' : 'font-semibold text-on-surface'">{{ row.date }}</span>
                  <span
                    v-if="row.peak"
                    class="font-label-sm text-label-sm px-1.5 py-0.5 rounded bg-secondary-container/50 text-on-secondary-fixed-variant font-semibold"
                  >
                    {{ row.sub }}
                  </span>
                  <span v-else class="text-outline font-body-sm text-body-sm">{{ row.sub }}</span>
                </div>
              </td>
              <td class="py-3.5 px-4 text-center" :class="row.peak ? 'font-bold text-primary-container' : 'font-medium'">
                {{ row.orders }}
              </td>
              <td class="py-3.5 px-6 text-right" :class="row.peak ? 'font-semibold' : 'font-medium'">{{ row.gross }}</td>
              <td class="py-3.5 px-6 text-right text-error font-medium">{{ row.refunds }}</td>
              <td class="py-3.5 px-6 text-right text-primary-container" :class="row.peak ? 'font-bold' : 'font-semibold'">
                {{ row.net }}
              </td>
              <td class="py-3.5 px-6 text-center">
                <span
                  v-if="row.open"
                  class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-secondary-container/40 text-on-secondary-fixed-variant font-label-sm text-label-sm font-semibold"
                >
                  <span class="w-1.5 h-1.5 rounded-full bg-secondary"></span>
                  <span>Đang mở ca</span>
                </span>
                <span
                  v-else
                  class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-surface-container text-primary-container font-label-sm text-label-sm font-semibold"
                >
                  <span class="material-symbols-outlined text-[13px]">check_circle</span>
                  <span>Đã đối soát</span>
                </span>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="bg-surface-container font-label-lg text-label-lg text-on-surface font-semibold">
              <td class="py-4 px-6 text-primary-container">TỔNG CỘNG (7 NGÀY)</td>
              <td class="py-4 px-4 text-center text-primary-container">1.084 đơn</td>
              <td class="py-4 px-6 text-right">58.920.000 ₫</td>
              <td class="py-4 px-6 text-right text-error font-bold">-680.000 ₫</td>
              <td class="py-4 px-6 text-right text-primary-container font-bold text-headline-sm">58.240.000 ₫</td>
              <td class="py-4 px-6 text-center">
                <span class="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-primary-container text-on-primary font-label-sm text-label-sm font-bold">
                  <span class="material-symbols-outlined text-[14px]">task_alt</span>
                  <span>KHỚP 100%</span>
                </span>
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  </div>
</template>
