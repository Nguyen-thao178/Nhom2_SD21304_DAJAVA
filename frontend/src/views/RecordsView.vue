<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const tables = [
  { id: 'tbl_don_hang', label: 'Đơn hàng & Hóa đơn', count: '1.084 bản ghi' },
  { id: 'tbl_chi_tiet_don', label: 'Order Items', count: '4.290 bản ghi' },
  { id: 'tbl_tai_khoan', label: 'Tài khoản nhân sự', count: '36 bản ghi' },
  { id: 'tbl_khach_hang', label: 'Khách hàng CRM', count: '890 bản ghi' },
  { id: 'tbl_san_pham', label: 'Thực đơn món', count: '64 bản ghi' },
  { id: 'tbl_kho_nguyen_lieu', label: 'Kho & Tồn kho', count: '152 bản ghi' },
]

const shortcuts = [
  { id: 'tbl_don_hang', label: 'Hóa đơn & Đơn hàng', count: '' },
  { id: 'tbl_chi_tiet_don', label: 'Chi tiết món (Order Items)', count: '4.290' },
  { id: 'payment', label: 'Giao dịch thanh toán (Payments)', count: '1.084' },
  { id: 'approval', label: 'Nhật ký phê duyệt (Approvals)', count: '142' },
  { id: 'feedback', label: 'Phản hồi & Khiếu nại (Feedback)', count: '58' },
  { id: 'tbl_khach_hang', label: 'Khách hàng thành viên (Customers)', count: '890' },
  { id: 'tbl_tai_khoan', label: 'Nhân viên ca trực (Staff)', count: '36' },
]

const selectedTable = ref(String(route.query.table || 'tbl_don_hang'))
const dropdownOpen = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const editModalOpen = ref(false)
const editingRecord = ref(null)

watch(
  () => route.query.table,
  (val) => {
    if (val) selectedTable.value = String(val)
  },
)

const records = ref([
  {
    id: 1092,
    code: 'HD-1092',
    date: '24/02/2026',
    time: '10:14',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'Khách lẻ (Walk-in)',
    customerMuted: true,
    payload: '[{"item": "Cà phê muối", "qty": 2}, {"item": "Bạc xỉu", "qty": 1}]',
    total: 102000,
    method: 'VietQR Pro',
    status: 'Đang pha chế',
    statusKey: 'pending',
    statusClass: 'bg-secondary-fixed text-on-secondary-fixed',
    dotClass: 'bg-secondary animate-pulse',
    isVat: true,
  },
  {
    id: 1091,
    code: 'HD-1091',
    date: '24/02/2026',
    time: '10:08',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'Minh Anh',
    customerTag: '#KH0092',
    payload: '[{"item": "Trà sen vàng", "qty": 1, "sugar": "70%"}]',
    total: 48000,
    method: 'Tiền mặt',
    status: 'Chờ trả món',
    statusKey: 'pending',
    statusClass: 'bg-surface-container-high text-primary-container',
    dotClass: 'bg-primary',
    isVat: false,
  },
  {
    id: 1090,
    code: 'HD-1090',
    date: '24/02/2026',
    time: '09:55',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'Đức Huy',
    customerTag: '#KH0148',
    payload: '[{"item": "Cold Brew Cam", "qty": 2}, {"item": "Croissant", "qty": 1}]',
    total: 128000,
    method: 'Thẻ POS',
    status: 'Hoàn thành',
    statusKey: 'active',
    statusClass: 'bg-primary-fixed text-primary',
    dotClass: 'bg-primary',
    isVat: true,
  },
  {
    id: 1089,
    code: 'HD-1089',
    date: '24/02/2026',
    time: '09:42',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'GrabFood',
    customerTag: '#DRV-882',
    payload: '[{"item": "Cà phê đen đá", "qty": 3}]',
    total: 75000,
    method: 'VietQR Pro',
    status: 'Hoàn thành',
    statusKey: 'active',
    statusClass: 'bg-primary-fixed text-primary',
    dotClass: 'bg-primary',
    isVat: false,
  },
  {
    id: 1088,
    code: 'HD-1088',
    date: '24/02/2026',
    time: '09:30',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'Thanh Hà',
    customerTag: '#KH0034',
    payload: '[{"item": "Bánh Mousse", "qty": 1}, {"item": "Trà Đào", "qty": 1}]',
    total: 82000,
    method: 'VietQR Pro',
    status: 'Hoàn thành',
    statusKey: 'active',
    statusClass: 'bg-primary-fixed text-primary',
    dotClass: 'bg-primary',
    isVat: true,
  },
  {
    id: 1087,
    code: 'HD-1087',
    date: '24/02/2026',
    time: '09:12',
    branch: 'Cầu Giấy #02',
    branchTone: 'bg-secondary',
    customer: 'Khách lẻ',
    customerMuted: true,
    payload: '[{"item": "Matcha Latte", "qty": 2}]',
    total: 110000,
    totalError: true,
    method: 'Tiền mặt',
    status: 'Đã hoàn tiền',
    statusKey: 'cancelled',
    statusClass: 'bg-error-container text-on-error-container',
    dotClass: 'bg-error',
    isVat: false,
  },
  {
    id: 1086,
    code: 'HD-1086',
    date: '24/02/2026',
    time: '08:45',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'Hoàng Nam',
    customerTag: '#KH0211',
    payload: '[{"item": "Espresso Tonic", "qty": 1}]',
    total: 49000,
    method: 'VietQR Pro',
    status: 'Hoàn thành',
    statusKey: 'active',
    statusClass: 'bg-primary-fixed text-primary',
    dotClass: 'bg-primary',
    isVat: false,
  },
  {
    id: 1085,
    code: 'HD-1085',
    date: '24/02/2026',
    time: '08:30',
    branch: 'Hoàn Kiếm #01',
    branchTone: 'bg-primary-container',
    customer: 'Bàn 06 (Dine-in)',
    customerMuted: true,
    payload: '[{"item": "Bạc xỉu", "qty": 2}, {"item": "Cà phê sữa", "qty": 1}]',
    total: 113000,
    method: 'Tiền mặt',
    status: 'Đã khóa sổ',
    statusKey: 'locked',
    statusClass: 'bg-surface-dim text-on-surface-variant',
    dotClass: 'bg-outline',
    isVat: true,
  },
])

const activeTableMeta = computed(() => {
  return tables.find((t) => t.id === selectedTable.value) || { id: selectedTable.value, label: 'Bảng dữ liệu hệ thống', count: '1.084 bản ghi' }
})

const filteredRecords = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  return records.value.filter((row) => {
    const matchStatus = statusFilter.value === 'all' || row.statusKey === statusFilter.value
    const matchSearch =
      !q ||
      row.code.toLowerCase().includes(q) ||
      row.customer.toLowerCase().includes(q) ||
      row.payload.toLowerCase().includes(q) ||
      row.branch.toLowerCase().includes(q)
    return matchStatus && matchSearch
  })
})

function formatVnd(amount) {
  return Number(amount || 0).toLocaleString('vi-VN') + ' ₫'
}

function selectTable(id) {
  selectedTable.value = id
  dropdownOpen.value = false
}

function openEditModal(row) {
  editingRecord.value = row
    ? { ...row }
    : {
        id: null,
        code: 'HD-1093',
        date: '24/02/2026',
        time: '10:30',
        branch: 'CN01 - Hoàn Kiếm #01 (36 Hàng Bè)',
        customer: 'KH0000 - Khách vãng lai / Trực tiếp',
        total: 0,
        method: 'VietQR Pro (Chuyển khoản QR Động)',
        status: 'PROCESSING (Đang pha chế)',
        isVat: true,
        payload: '[\n  {"sku": "CF-04", "name": "Cà phê muối đặc biệt", "qty": 1, "price": 42000}\n]',
      }
  editModalOpen.value = true
}

function saveRecord() {
  editModalOpen.value = false
}

function deleteRow(id) {
  records.value = records.value.filter((r) => r.id !== id)
}
</script>

<template>
  <div class="px-6 lg:px-8 py-7 space-y-6">
    <!-- PAGE HEADER -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
      <div>
        <div class="flex items-center gap-2 mb-1.5">
          <span class="inline-block w-2 h-2 rounded-full bg-secondary"></span>
          <span class="font-label-sm text-secondary uppercase tracking-[1.5px]">Cơ sở dữ liệu &amp; Quản trị hệ thống · 34 Bảng</span>
        </div>
        <h1 class="font-headline-lg text-primary text-3xl font-medium tracking-tight">
          Quản lý dữ liệu hệ thống (Generic CRUD)
        </h1>
        <p class="font-body-md text-on-surface-variant mt-1">
          Xem, tra cứu, chỉnh sửa và quản lý trực tiếp các bản ghi trong cơ sở dữ liệu vận hành PolyCafe.
        </p>
      </div>

      <div class="flex items-center gap-3 bg-surface-container-low px-4 py-2.5 rounded-xl self-start md:self-auto">
        <div class="flex items-center gap-2 text-primary font-label-md">
          <span class="material-symbols-outlined text-secondary text-[20px]">database</span>
          <span>PostgreSQL 16.2</span>
        </div>
        <span class="text-outline-variant">·</span>
        <div class="font-label-md text-on-surface-variant">
          Dung lượng: <span class="text-on-surface font-semibold">142.8 MB</span>
        </div>
        <span class="text-outline-variant">·</span>
        <div class="flex items-center gap-1 font-label-md text-primary-container font-semibold">
          <span class="w-1.5 h-1.5 rounded-full bg-primary-container"></span>
          <span>Online</span>
        </div>
      </div>
    </div>

    <!-- TOOLBAR PANEL -->
    <div class="bg-surface-container-lowest rounded-xl p-4 shadow-sm space-y-4">
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-3">
        <div class="flex flex-wrap items-center gap-3 flex-1">
          <!-- Table selector -->
          <div class="relative min-w-[280px] sm:min-w-[320px]">
            <div
              class="flex items-center gap-2 bg-surface-container-low px-3.5 py-2.5 rounded-lg text-on-surface font-label-md cursor-pointer hover:bg-surface-container transition-colors"
              @click="dropdownOpen = !dropdownOpen"
            >
              <span class="material-symbols-outlined text-[18px] text-primary">table_chart</span>
              <span class="font-semibold text-primary truncate flex-1">
                {{ activeTableMeta.id }}
                <span class="font-normal text-on-surface-variant">({{ activeTableMeta.label }} - {{ activeTableMeta.count }})</span>
              </span>
              <span class="material-symbols-outlined text-[18px] text-outline">expand_more</span>
            </div>

            <div
              v-if="dropdownOpen"
              class="absolute top-full left-0 mt-1.5 w-full bg-surface-container-lowest rounded-xl shadow-xl z-30 p-2 space-y-1 max-h-72 overflow-y-auto border border-[#e6e8df]"
            >
              <div class="px-3 py-1 font-label-sm text-outline uppercase tracking-wider">Bảng nghiệp vụ chính</div>
              <button
                v-for="t in tables"
                :key="t.id"
                type="button"
                class="w-full text-left px-3 py-2 rounded-lg font-label-md flex items-center justify-between transition-colors"
                :class="
                  selectedTable === t.id
                    ? 'bg-surface-container font-semibold text-primary'
                    : 'text-on-surface hover:bg-surface-container-low'
                "
                @click="selectTable(t.id)"
              >
                <span>{{ t.id }} ({{ t.label }})</span>
                <span class="font-label-sm text-outline">{{ t.count }}</span>
              </button>
            </div>
          </div>

          <!-- Search -->
          <div class="relative w-full sm:w-[320px]">
            <span class="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-outline text-[18px]">search</span>
            <input
              v-model="searchQuery"
              class="w-full h-10 pl-10 pr-3.5 rounded-lg bg-surface-container-low text-on-surface placeholder:text-outline font-body-md focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary-container outline-none transition-all"
              placeholder="Lọc trong danh sách đang hiển thị…"
              type="text"
            />
          </div>

          <!-- Status Filter -->
          <div class="flex items-center gap-1.5 bg-surface-container-low px-3 py-2 rounded-lg">
            <span class="font-label-sm text-outline uppercase tracking-wide">Trạng thái:</span>
            <select v-model="statusFilter" class="bg-transparent font-label-md text-on-surface font-semibold outline-none cursor-pointer">
              <option value="all">Tất cả bản ghi</option>
              <option value="active">Hoàn thành</option>
              <option value="pending">Chờ xử lý</option>
              <option value="locked">Đã khóa sổ</option>
              <option value="cancelled">Đã hoàn tiền</option>
            </select>
          </div>
        </div>

        <div class="flex items-center gap-2.5">
          <button
            type="button"
            class="flex items-center gap-1.5 px-3.5 py-2.5 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface font-label-md transition-colors"
          >
            <span class="material-symbols-outlined text-[18px] text-secondary">file_download</span>
            <span>Xuất JSON / SQL</span>
          </button>
          <button
            type="button"
            class="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-primary hover:bg-primary-container text-on-primary font-label-md font-semibold transition-all shadow-sm"
            @click="openEditModal(null)"
          >
            <span class="material-symbols-outlined text-[18px]">add</span>
            <span>+ Thêm mới bản ghi</span>
          </button>
        </div>
      </div>

      <!-- MODULE SHORTCUTS -->
      <div class="pt-3 border-t border-surface-container-high flex flex-wrap items-center gap-2">
        <span class="font-label-sm text-outline uppercase tracking-wider mr-1 flex items-center gap-1">
          <span class="material-symbols-outlined text-[14px]">share</span> Bảng dữ liệu liên quan:
        </span>
        <button
          v-for="s in shortcuts"
          :key="s.id"
          type="button"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full font-label-sm transition-colors"
          :class="
            selectedTable === s.id
              ? 'bg-primary text-on-primary font-semibold shadow-sm'
              : 'bg-surface-container-low hover:bg-surface-container text-on-surface'
          "
          @click="selectTable(s.id)"
        >
          <span v-if="selectedTable === s.id" class="material-symbols-outlined text-[13px]">check_circle</span>
          <span>{{ s.label }}</span>
          <span v-if="s.count" class="text-outline font-semibold">{{ s.count }}</span>
        </button>
      </div>
    </div>

    <!-- TABLE METADATA CALLOUT -->
    <div class="bg-surface-container-low/70 rounded-xl p-3.5 flex items-start gap-3">
      <div class="w-7 h-7 rounded-lg bg-surface-container-high flex items-center justify-center flex-shrink-0 text-primary mt-0.5">
        <span class="material-symbols-outlined text-[17px]">info</span>
      </div>
      <div class="space-y-1">
        <div class="flex items-center gap-2">
          <span class="font-label-md font-semibold text-primary">Quy tắc chỉnh sửa bảng '{{ activeTableMeta.id }}'</span>
          <span class="font-label-sm px-2 py-0.5 rounded bg-surface-container font-mono text-outline">PostgreSQL schema: public</span>
        </div>
        <p class="font-body-sm text-on-surface-variant leading-relaxed">
          Cột <code class="font-mono text-primary font-semibold">id</code>, <code class="font-mono text-primary font-semibold">created_at</code> được tạo tự động bởi PostgreSQL sequence. Các hóa đơn ở trạng thái <strong>“Hoàn thành”</strong> hoặc <strong>“Đã chốt ca”</strong> yêu cầu quyền Quản lý trưởng (Admin Level 2) để sửa đổi trường giá trị thanh toán hoặc hoàn tiền.
        </p>
      </div>
    </div>

    <!-- DATA TABLE CONTAINER -->
    <div class="bg-surface-container-lowest rounded-xl shadow-sm overflow-hidden flex flex-col">
      <div class="px-5 py-3.5 bg-surface-container-lowest flex flex-wrap items-center justify-between gap-2">
        <div class="flex items-center gap-3">
          <span class="font-label-md text-on-surface font-semibold">Hiển thị {{ filteredRecords.length }} / 1.084 bản ghi</span>
          <span class="font-body-sm text-outline">
            (Trang 1 / 136 · Sắp xếp theo: <span class="font-semibold text-on-surface-variant">created_at DESC</span>)
          </span>
        </div>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="flex items-center gap-1 font-label-md text-outline hover:text-primary transition-colors px-2.5 py-1 rounded bg-surface-container-low hover:bg-surface-container"
          >
            <span class="material-symbols-outlined text-[16px]">refresh</span>
            <span>Làm mới</span>
          </button>
          <button
            type="button"
            class="flex items-center gap-1 font-label-md text-outline hover:text-primary transition-colors px-2.5 py-1 rounded bg-surface-container-low hover:bg-surface-container"
          >
            <span class="material-symbols-outlined text-[16px]">view_column</span>
            <span>Cột hiển thị (10/14)</span>
          </button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-surface-container-high/60 text-outline font-label-sm uppercase tracking-wider">
              <th class="py-3 px-4 w-12 text-center">
                <input class="w-4 h-4 rounded text-primary focus:ring-primary" type="checkbox" />
              </th>
              <th class="py-3 px-4 font-semibold">ID</th>
              <th class="py-3 px-4 font-semibold">Mã hóa đơn</th>
              <th class="py-3 px-4 font-semibold">Thời gian tạo</th>
              <th class="py-3 px-4 font-semibold">Chi nhánh</th>
              <th class="py-3 px-4 font-semibold">Khách hàng</th>
              <th class="py-3 px-4 font-semibold max-w-[240px]">Chi tiết món &amp; JSON Payload</th>
              <th class="py-3 px-4 font-semibold text-right">Tổng tiền</th>
              <th class="py-3 px-4 font-semibold">Hình thức</th>
              <th class="py-3 px-4 font-semibold">Trạng thái</th>
              <th class="py-3 px-4 font-semibold text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody class="font-body-sm text-on-surface divide-y divide-surface-container-low">
            <tr v-for="row in filteredRecords" :key="row.id" class="hover:bg-surface-container-low/40 transition-colors">
              <td class="py-3.5 px-4 text-center">
                <input class="w-4 h-4 rounded text-primary focus:ring-primary" type="checkbox" />
              </td>
              <td class="py-3.5 px-4 font-mono font-semibold text-outline text-[11px]">{{ row.id }}</td>
              <td class="py-3.5 px-4 font-label-md font-semibold text-primary">{{ row.code }}</td>
              <td class="py-3.5 px-4 whitespace-nowrap text-on-surface-variant font-mono text-[11px]">
                {{ row.date }} <span class="text-outline">{{ row.time }}</span>
              </td>
              <td class="py-3.5 px-4 whitespace-nowrap">
                <span class="inline-flex items-center gap-1 font-label-sm px-2 py-0.5 rounded bg-surface-container text-on-surface font-medium">
                  <span class="w-1.5 h-1.5 rounded-full" :class="row.branchTone"></span> {{ row.branch }}
                </span>
              </td>
              <td class="py-3.5 px-4 whitespace-nowrap font-medium" :class="row.customerMuted ? 'text-on-surface-variant' : 'text-on-surface'">
                {{ row.customer }}
                <span v-if="row.customerTag" class="text-outline font-mono text-[10px] ml-1">{{ row.customerTag }}</span>
              </td>
              <td class="py-3.5 px-4 max-w-[240px]">
                <div class="font-mono text-[11px] text-outline truncate bg-surface-container-low px-2 py-1 rounded" :title="row.payload">
                  {{ row.payload }}
                </div>
              </td>
              <td class="py-3.5 px-4 text-right font-label-md font-semibold" :class="row.totalError ? 'text-error' : 'text-primary'">
                {{ formatVnd(row.total) }}
              </td>
              <td class="py-3.5 px-4 whitespace-nowrap">
                <span class="font-label-sm text-on-surface-variant px-2 py-0.5 rounded bg-surface-container">{{ row.method }}</span>
              </td>
              <td class="py-3.5 px-4 whitespace-nowrap">
                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-label-sm font-semibold" :class="row.statusClass">
                  <span class="w-1.5 h-1.5 rounded-full" :class="row.dotClass"></span> {{ row.status }}
                </span>
              </td>
              <td class="py-3.5 px-4 text-center whitespace-nowrap">
                <div class="flex items-center justify-center gap-2.5 font-label-sm">
                  <button
                    type="button"
                    class="text-on-surface-variant hover:text-primary transition-colors flex items-center gap-0.5"
                    @click="openEditModal(row)"
                  >
                    <span class="material-symbols-outlined text-[15px]">visibility</span>
                    <span>Chi tiết</span>
                  </button>
                  <button
                    type="button"
                    class="text-primary font-semibold hover:underline flex items-center gap-0.5"
                    @click="openEditModal(row)"
                  >
                    <span class="material-symbols-outlined text-[15px]">edit_note</span>
                    <span>Sửa</span>
                  </button>
                  <button
                    type="button"
                    class="text-outline hover:text-error transition-colors flex items-center gap-0.5"
                    @click="deleteRow(row.id)"
                  >
                    <span class="material-symbols-outlined text-[15px]">delete</span>
                    <span>Xóa</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- PAGINATION BAR -->
      <div class="p-4 bg-surface-container-lowest border-t border-surface-container-high flex flex-col sm:flex-row items-center justify-between gap-3">
        <div class="font-body-sm text-outline">
          Hiển thị <span class="font-semibold text-on-surface">{{ filteredRecords.length }}</span> / 1.084 bản ghi đã tải (Đang xem
          <span class="font-semibold text-on-surface">1 - {{ filteredRecords.length }}</span>)
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <button
            class="px-3 py-1.5 rounded-lg bg-surface-container-low text-outline font-label-md cursor-not-allowed opacity-60 flex items-center gap-1"
            disabled
            type="button"
          >
            <span class="material-symbols-outlined text-[16px]">chevron_left</span>
            <span>Trang trước</span>
          </button>
          <div class="px-3 py-1.5 rounded-lg bg-surface-container text-primary font-label-md font-semibold">
            Trang 1 / 136
          </div>
          <button
            class="px-3 py-1.5 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface font-label-md transition-colors flex items-center gap-1"
            type="button"
          >
            <span>Trang sau</span>
            <span class="material-symbols-outlined text-[16px]">chevron_right</span>
          </button>
          <button
            class="ml-2 px-3 py-1.5 rounded-lg bg-surface-container-high hover:bg-surface-dim text-primary font-label-md font-semibold transition-colors flex items-center gap-1"
            type="button"
          >
            <span class="material-symbols-outlined text-[16px]">downloading</span>
            <span>+ Tải thêm 20 bản ghi</span>
          </button>
        </div>
      </div>
    </div>

    <!-- BOTTOM ROW: SYSTEM LOG & AUDIT INSIGHTS -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 pt-2">
      <div class="bg-surface-container-lowest p-4 rounded-xl shadow-sm space-y-2">
        <div class="flex items-center justify-between">
          <span class="font-label-sm text-outline uppercase tracking-wider">Khóa ngoại (Foreign Keys)</span>
          <span class="material-symbols-outlined text-secondary text-[18px]">account_tree</span>
        </div>
        <p class="font-headline-sm text-primary">3 Ràng buộc tham chiếu</p>
        <p class="font-body-sm text-on-surface-variant">branch_id → tbl_chi_nhanh (RESTRICT)<br />customer_id → tbl_khach_hang (SET NULL)</p>
      </div>

      <div class="bg-surface-container-lowest p-4 rounded-xl shadow-sm space-y-2">
        <div class="flex items-center justify-between">
          <span class="font-label-sm text-outline uppercase tracking-wider">Chỉ mục tăng tốc (Indexes)</span>
          <span class="material-symbols-outlined text-primary text-[18px]">speed</span>
        </div>
        <p class="font-headline-sm text-primary">5 B-Tree Indexes</p>
        <p class="font-body-sm text-on-surface-variant">idx_don_hang_created_at (DESC)<br />idx_don_hang_order_code (UNIQUE)</p>
      </div>

      <div class="bg-surface-container-lowest p-4 rounded-xl shadow-sm space-y-2">
        <div class="flex items-center justify-between">
          <span class="font-label-sm text-outline uppercase tracking-wider">An toàn &amp; Soft-Delete</span>
          <span class="material-symbols-outlined text-secondary text-[18px]">security</span>
        </div>
        <p class="font-headline-sm text-primary">Audit Log Kích Hoạt</p>
        <p class="font-body-sm text-on-surface-variant">
          Mọi truy vấn UPDATE / DELETE được lưu nhật ký vào bảng <code class="font-mono text-primary font-semibold">sys_audit_trails</code>.
        </p>
      </div>
    </div>

    <!-- DYNAMIC EDITOR DIALOG / MODAL (Mặc định đóng, mở khi click Sửa / Thêm mới) -->
    <div
      v-if="editModalOpen && editingRecord"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-tertiary-container/60 backdrop-blur-sm"
      @click.self="editModalOpen = false"
    >
      <div class="relative w-full max-w-[760px] bg-surface-container-lowest rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        <div class="px-6 py-4 bg-surface-container-lowest flex items-start justify-between border-b border-surface-container-high">
          <div>
            <div class="flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-secondary"></span>
              <span class="font-label-sm text-secondary uppercase tracking-[1.2px]">
                Generic CRUD · {{ editingRecord.id ? 'Chỉnh sửa dữ liệu' : 'Thêm mới bản ghi' }}
              </span>
            </div>
            <h2 class="font-headline-md text-primary mt-0.5">
              {{ editingRecord.id ? `Sửa bản ghi: ${editingRecord.code}` : 'Thêm bản ghi mới' }}
              <span v-if="editingRecord.id" class="font-mono text-outline font-normal text-sm">(ID: {{ editingRecord.id }})</span>
            </h2>
          </div>
          <button
            type="button"
            class="p-1 rounded-lg text-outline hover:text-on-surface hover:bg-surface-container transition-colors"
            title="Đóng cửa sổ"
            @click="editModalOpen = false"
          >
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>

        <div class="p-6 space-y-5 overflow-y-auto">
          <div class="p-3.5 rounded-lg bg-error-container/40 text-on-error-container flex items-start gap-2.5">
            <span class="material-symbols-outlined text-[18px] text-error flex-shrink-0 mt-0.5">warning</span>
            <div class="font-body-sm leading-snug">
              <strong>Lưu ý kiểm tra toàn vẹn:</strong> Tổng tiền không được thấp hơn giá trị chi tiết món trừ khi có mã coupon chiết khấu hợp lệ được đối soát trong giỏ hàng.
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Mã hóa đơn (order_code)</label>
                <span class="font-mono text-[10px] text-outline">varchar(32) · NOT NULL</span>
              </div>
              <input
                v-model="editingRecord.code"
                class="w-full h-10 px-3 rounded-lg bg-surface-container-low text-on-surface font-body-md focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all"
                type="text"
              />
            </div>

            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Thời gian tạo (created_at)</label>
                <span class="font-mono text-[10px] text-outline">timestamptz</span>
              </div>
              <input
                :value="`${editingRecord.date} ${editingRecord.time}`"
                class="w-full h-10 px-3 rounded-lg bg-surface-container-low text-on-surface font-mono text-[13px] focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all"
                type="text"
              />
            </div>

            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Chi nhánh (branch_id)</label>
                <span class="font-mono text-[10px] text-outline">FK → tbl_chi_nhanh</span>
              </div>
              <select class="w-full h-10 px-3 rounded-lg bg-surface-container-low text-on-surface font-body-md focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all cursor-pointer">
                <option>CN01 - Hoàn Kiếm #01 (36 Hàng Bè)</option>
                <option>CN02 - Cầu Giấy #02 (124 Xuân Thủy)</option>
                <option>CN03 - Đống Đa #03 (88 Chùa Bộc)</option>
              </select>
            </div>

            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Khách hàng (customer_id)</label>
                <span class="font-mono text-[10px] text-outline">FK → tbl_khach_hang</span>
              </div>
              <select class="w-full h-10 px-3 rounded-lg bg-surface-container-low text-on-surface font-body-md focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all cursor-pointer">
                <option>KH0000 - Khách vãng lai / Trực tiếp</option>
                <option>KH0092 - Minh Anh (+84 912***456)</option>
                <option>KH0148 - Đức Huy (+84 983***789)</option>
                <option>KH0034 - Thanh Hà (+84 904***112)</option>
              </select>
            </div>

            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Tổng tiền thanh toán (total_amount)</label>
                <span class="font-mono text-[10px] text-outline">numeric(12,2)</span>
              </div>
              <div class="relative">
                <input
                  v-model.number="editingRecord.total"
                  class="w-full h-10 pl-3 pr-12 rounded-lg bg-surface-container-low text-on-surface font-mono font-semibold text-[14px] focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all"
                  type="number"
                />
                <span class="absolute right-3 top-1/2 -translate-y-1/2 font-label-md text-outline font-semibold">VNĐ</span>
              </div>
            </div>

            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Hình thức thanh toán (payment_method)</label>
                <span class="font-mono text-[10px] text-outline">enum_payment</span>
              </div>
              <select class="w-full h-10 px-3 rounded-lg bg-surface-container-low text-on-surface font-body-md focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all cursor-pointer">
                <option>VietQR Pro (Chuyển khoản QR Động)</option>
                <option>Tiền mặt (Cash at counter)</option>
                <option>Thẻ POS (Chạm / Chip VISA/Mastercard)</option>
                <option>Ví điện tử Momo / ZaloPay</option>
              </select>
            </div>

            <div class="space-y-1.5">
              <div class="flex items-center justify-between">
                <label class="font-label-md text-on-surface font-semibold">Trạng thái đơn (status)</label>
                <span class="font-mono text-[10px] text-outline">varchar(20)</span>
              </div>
              <select class="w-full h-10 px-3 rounded-lg bg-surface-container-low text-on-surface font-body-md focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all cursor-pointer">
                <option>PROCESSING (Đang pha chế)</option>
                <option>WAITING_PICKUP (Chờ trả món)</option>
                <option>COMPLETED (Hoàn thành giao khách)</option>
                <option>CLOSED (Đã khóa sổ ca)</option>
                <option>REFUNDED (Đã hoàn tiền khách)</option>
                <option>CANCELLED (Đã hủy đơn)</option>
              </select>
            </div>

            <div class="space-y-1.5 flex flex-col justify-end">
              <div class="flex items-center justify-between mb-1">
                <span class="font-label-sm text-outline uppercase tracking-wider">Cờ trạng thái hóa đơn</span>
                <span class="font-mono text-[10px] text-outline">boolean</span>
              </div>
              <label class="flex items-center gap-2.5 h-10 px-3 rounded-lg bg-surface-container-low cursor-pointer hover:bg-surface-container transition-colors">
                <input v-model="editingRecord.isVat" class="w-4 h-4 rounded text-primary focus:ring-primary" type="checkbox" />
                <span class="font-label-md text-on-surface">Đã xuất hóa đơn VAT điện tử (is_vat_invoiced)</span>
              </label>
            </div>
          </div>

          <div class="space-y-1.5 pt-1">
            <div class="flex items-center justify-between">
              <label class="font-label-md text-on-surface font-semibold flex items-center gap-1.5">
                <span>Dữ liệu giỏ hàng (items_payload)</span>
                <span class="font-mono text-[10px] px-1.5 py-0.5 rounded bg-surface-container text-primary font-bold">JSONB</span>
              </label>
              <span class="font-label-sm text-secondary font-semibold hover:underline cursor-pointer">Kiểm tra cú pháp JSON</span>
            </div>
            <textarea
              v-model="editingRecord.payload"
              class="w-full p-3 font-mono text-[12px] leading-relaxed rounded-lg bg-surface-container text-primary-container focus:bg-surface-container-lowest focus:ring-2 focus:ring-primary outline-none transition-all"
              rows="4"
            ></textarea>
            <div class="font-body-sm text-outline">
              Dữ liệu JSON định dạng chuẩn PostgreSQL JSONB. Bắt buộc có các khóa <code class="font-mono font-semibold text-primary">sku</code>,
              <code class="font-mono font-semibold text-primary">name</code>, <code class="font-mono font-semibold text-primary">qty</code>,
              <code class="font-mono font-semibold text-primary">price</code>.
            </div>
          </div>
        </div>

        <div class="px-6 py-4 bg-surface-container-low flex flex-col sm:flex-row items-center justify-between gap-3 border-t border-surface-container-high">
          <div class="font-body-sm text-outline flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[15px]">history</span>
            <span>Cập nhật lần cuối: 24/02/2026 10:15 bởi <span class="font-semibold text-on-surface">barista.nam</span></span>
          </div>
          <div class="flex items-center gap-2.5 w-full sm:w-auto justify-end">
            <button
              class="px-4 py-2 rounded-lg bg-surface-container hover:bg-surface-container-high text-on-surface font-label-md transition-colors"
              type="button"
              @click="editModalOpen = false"
            >
              Hủy bỏ
            </button>
            <button
              class="px-5 py-2 rounded-lg bg-primary hover:bg-primary-container text-on-primary font-label-md font-semibold transition-all shadow-sm flex items-center gap-1.5"
              type="button"
              @click="saveRecord"
            >
              <span class="material-symbols-outlined text-[17px]">save</span>
              <span>Lưu thay đổi (UPDATE)</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
