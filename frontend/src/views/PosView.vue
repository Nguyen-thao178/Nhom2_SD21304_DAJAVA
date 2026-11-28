<script setup>
import { ref, computed } from 'vue'

const searchQuery = ref('')
const selectedCategory = ref('all')
const orderMode = ref('dine-in')
const selectedBranch = ref('hk01')
const orderNote = ref('')
const toastMessage = ref('')
let toastTimer = null

const categories = [
  { id: 'all', label: 'Tất cả' },
  { id: 'phin', label: 'Cà phê phin' },
  { id: 'espresso', label: 'Espresso & Sữa' },
  { id: 'tea', label: 'Trà & Macchiato' },
  { id: 'coldbrew', label: 'Cold Brew' },
  { id: 'pastry', label: 'Bánh ngọt' },
]

const products = [
  {
    id: 'p1',
    name: 'Cà phê sữa đá Sài Gòn',
    category: 'phin',
    categoryLabel: 'CÀ PHÊ PHIN',
    desc: 'Robusta Buôn Ma Thuột đậm đà, sữa đặc truyền thống',
    price: 35000,
    note: 'Ít đá, đậm vị',
    badge: 'Sẵn sàng',
    badgeClass: 'bg-[#edf2e9] text-[#557146]',
    artBg: 'bg-[#e9e7dc]',
    artType: 'ca-phe-sua',
  },
  {
    id: 'p2',
    name: 'Bạc xỉu 3 tầng sữa tươi',
    category: 'phin',
    categoryLabel: 'CÀ PHÊ TRUYỀN THỐNG',
    desc: 'Sữa béo ngậy, cà phê dịu nhẹ, cân bằng vị giác',
    price: 39000,
    note: '3 tầng cân bằng',
    badge: 'Best Seller',
    badgeClass: 'bg-[#fcf1de] text-[#997139]',
    artBg: 'bg-[#f0e5d5]',
    artType: 'bac-xiu',
  },
  {
    id: 'p3',
    name: 'Cà phê muối đặc biệt Poly',
    category: 'phin',
    categoryLabel: 'CÀ PHÊ ĐẶC SẢN',
    desc: 'Lớp kem muối béo mặn nhẹ đọng trên cốt cà phê phin sánh',
    price: 42000,
    note: 'Ít đá, 50% ngọt',
    badge: 'Best Seller',
    badgeClass: 'bg-[#fcf1de] text-[#997139]',
    artBg: 'bg-[#f0e5d5]',
    artType: 'ca-phe-muoi',
  },
  {
    id: 'p4',
    name: 'Cold Brew Cam vàng Cầu Đất',
    category: 'coldbrew',
    categoryLabel: 'COLD BREW',
    desc: 'Arabica Cầu Đất ủ chậm 18h hòa quyện tép cam tươi',
    price: 55000,
    note: 'Ủ lạnh 18h',
    badge: 'Đặc biệt',
    badgeClass: 'bg-[#fcf1de] text-[#997139]',
    artBg: 'bg-[#e5eadd]',
    artType: 'cold-brew',
  },
  {
    id: 'p5',
    name: 'Trà sen vàng Macchiato',
    category: 'tea',
    categoryLabel: 'TRÀ & MACCHIATO',
    desc: 'Trà sen Ô Long thanh nhẹ phủ bọt kem sữa phô mai',
    price: 45000,
    note: 'Hạt sen Đồng Tháp',
    badge: 'Sẵn sàng',
    badgeClass: 'bg-[#edf2e9] text-[#557146]',
    artBg: 'bg-[#e2e9e8]',
    artType: 'tra-sen',
  },
  {
    id: 'p6',
    name: 'Espresso Tonic chanh vàng',
    category: 'espresso',
    categoryLabel: 'ESPRESSO BAR',
    desc: 'Espresso đậm đặc nổi bọt sảng khoái trên nền tonic',
    price: 49000,
    note: 'Shot kép Arabica',
    badge: 'Sẵn sàng',
    badgeClass: 'bg-[#edf2e9] text-[#557146]',
    artBg: 'bg-[#e9e7dc]',
    artType: 'espresso-tonic',
  },
  {
    id: 'p7',
    name: 'Matcha Latte Uji',
    category: 'tea',
    categoryLabel: 'TRÀ NHẬT BẢN',
    desc: 'Bột matcha Uji nguyên chất đánh sữa tơi xốp',
    price: 55000,
    note: 'Uji Kyoto, sữa tươi',
    badge: 'Sẵn sàng',
    badgeClass: 'bg-[#edf2e9] text-[#557146]',
    artBg: 'bg-[#e5eadd]',
    artType: 'matcha',
  },
  {
    id: 'p8',
    name: 'Croissant bơ Pháp hạnh nhân',
    category: 'pastry',
    categoryLabel: 'BÁNH NƯỚNG PHÁP',
    desc: 'Bánh ngàn lớp thơm lừng bơ Pháp cao cấp rắc hạnh nhân',
    price: 38000,
    note: 'Nướng nóng giòn',
    badge: 'Sắp hết',
    badgeClass: 'bg-[#faeae7] text-[#a25d52]',
    artBg: 'bg-[#f0e5d5]',
    artType: 'croissant',
  },
  {
    id: 'p9',
    name: 'Bánh Mousse cà phê hạt dẻ',
    category: 'pastry',
    categoryLabel: 'BÁNH TRÁNG MIỆNG',
    desc: 'Vị đắng êm dịu, kem mousse mịn màng kèm hạt dẻ nướng',
    price: 42000,
    note: 'Giữ lạnh chuẩn',
    badge: 'Món mới',
    badgeClass: 'bg-[#edf2e9] text-[#557146]',
    artBg: 'bg-[#e9e7dc]',
    artType: 'mousse',
  },
]

const cart = ref([
  { id: 'p3', name: 'Cà phê muối đặc biệt Poly', price: 42000, note: 'Ít đá, 50% ngọt', qty: 2 },
  { id: 'p2', name: 'Bạc xỉu 3 tầng sữa tươi', price: 39000, note: 'Chuẩn vị', qty: 1 },
  { id: 'p8', name: 'Croissant bơ Pháp hạnh nhân', price: 38000, note: 'Nướng ấm nóng', qty: 1 },
])

const filteredProducts = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  return products.filter((p) => {
    const matchCat = selectedCategory.value === 'all' || p.category === selectedCategory.value
    const matchSearch = !q || p.name.toLowerCase().includes(q) || p.desc.toLowerCase().includes(q)
    return matchCat && matchSearch
  })
})

const cartTotal = computed(() => cart.value.reduce((sum, item) => sum + item.price * item.qty, 0))

function formatVnd(amount) {
  return Number(amount || 0).toLocaleString('vi-VN') + ' ₫'
}

function addToOrder(product) {
  const existing = cart.value.find((c) => c.id === product.id)
  if (existing) {
    existing.qty += 1
  } else {
    cart.value.unshift({
      id: product.id,
      name: product.name,
      price: product.price,
      note: product.note,
      qty: 1,
    })
  }
}

function updateQty(item, delta) {
  item.qty += delta
  if (item.qty <= 0) {
    cart.value = cart.value.filter((c) => c.id !== item.id)
  }
}

function clearCart() {
  cart.value = []
}

function showNotification(msg) {
  toastMessage.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toastMessage.value = ''
  }, 2500)
}

function saveDraftOrder() {
  if (!cart.value.length) return
  showNotification('✓ Đã lưu đơn nháp vào hệ thống POS')
}

function printDraftOrder() {
  if (!cart.value.length) return
  showNotification('🖨 Đang in phiếu soạn đơn ra máy in quầy...')
}
</script>

<template>
  <div class="p-6 lg:p-8 max-w-[1600px] mx-auto w-full">
    <!-- Breadcrumb & POS Header -->
    <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
      <div class="flex flex-col">
        <span class="font-label-sm text-outline uppercase tracking-wider text-[10px]">POS STATION · THU NGÂN 01</span>
        <h1 class="font-headline-lg text-primary text-2xl lg:text-3xl font-serif mt-0.5">Bán hàng tại quầy</h1>
      </div>
      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2 bg-surface-container px-3 py-1.5 rounded-full">
          <span class="w-2 h-2 rounded-full bg-primary-container animate-pulse"></span>
          <span class="font-label-md text-on-surface text-[12px]">Đồng bộ KDS trực tiếp</span>
        </div>
        <div class="hidden sm:flex items-center gap-2 px-3.5 py-1.5 rounded-lg bg-surface-container-high text-on-surface-variant font-label-md text-[12px]">
          <span class="material-symbols-outlined text-[18px]">cloud_download</span>
          <span>Ca sáng · 24/02/2026</span>
        </div>
      </div>
    </div>

    <!-- 2-Column POS Layout -->
    <div class="flex flex-col lg:flex-row gap-6 items-start w-full">
      <!-- CỘT TRÁI: DANH MỤC & LƯỚI SẢN PHẨM -->
      <div class="flex-1 w-full space-y-6 min-w-0">
        <div class="bg-[#ffffff] p-4 rounded-xl border border-[#e6e8df] shadow-sm space-y-3.5">
          <div class="flex flex-col sm:flex-row gap-3 items-stretch sm:items-center justify-between">
            <div class="relative flex-1">
              <span class="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-outline text-[19px]">search</span>
              <input
                v-model="searchQuery"
                class="w-full pl-11 pr-4 py-[11px] rounded-[9px] border border-[#e6e8df] bg-surface-container-low/40 font-body-md text-on-surface placeholder:text-outline text-[13px] focus:bg-[#ffffff] focus:ring-1 focus:ring-primary-container outline-none transition-all"
                placeholder="Hôm nay khách chọn món gì?"
                type="text"
              />
            </div>
            <div class="flex items-center justify-between sm:justify-end gap-3 px-1">
              <span class="font-label-md text-outline flex items-center gap-1.5 whitespace-nowrap text-[12px]">
                <span class="w-2 h-2 rounded-full bg-secondary"></span>
                <span class="font-semibold text-on-surface">{{ filteredProducts.length }}</span> món đang phục vụ
              </span>
              <button
                type="button"
                class="p-2 rounded-lg bg-surface-container-high text-on-surface-variant hover:bg-surface-dim transition-colors"
                title="Bộ lọc nâng cao"
              >
                <span class="material-symbols-outlined text-[19px]">tune</span>
              </button>
            </div>
          </div>

          <div class="flex items-center gap-2 overflow-x-auto pb-1 text-nowrap">
            <button
              v-for="cat in categories"
              :key="cat.id"
              type="button"
              class="px-4 py-1.5 rounded-full font-label-md text-[12px] transition-all"
              :class="
                selectedCategory === cat.id
                  ? 'bg-primary-container text-on-primary shadow-sm'
                  : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
              "
              @click="selectedCategory = cat.id"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>

        <!-- Product Grid -->
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-5">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="group bg-[#ffffff] border border-[#e6e8df] rounded-[14px] overflow-hidden flex flex-col justify-between transition-all duration-200 hover:-translate-y-1 hover:shadow-md cursor-pointer"
            @click="addToOrder(product)"
          >
            <div class="relative h-[150px] flex items-center justify-center overflow-hidden" :class="product.artBg">
              <span class="absolute top-3 left-3 px-2.5 py-0.5 rounded-full font-label-sm text-[10px] font-semibold" :class="product.badgeClass">
                {{ product.badge }}
              </span>

              <!-- Drink CSS Art Variants -->
              <div
                v-if="product.artType === 'ca-phe-sua'"
                class="relative w-16 h-24 bg-gradient-to-b from-[#4a2e1d] to-[#1a110a] rounded-b-xl shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300 flex items-center justify-center"
              >
                <div class="absolute inset-x-1 bottom-1 h-8 bg-gradient-to-t from-[#f6ebd4] to-transparent opacity-80 rounded-b-lg"></div>
                <div class="w-8 h-8 rounded-full bg-white/20 blur-[1px]"></div>
                <div class="absolute -top-3 w-12 h-3 bg-[#8c7457] rounded-sm shadow-sm"></div>
              </div>

              <div
                v-else-if="product.artType === 'bac-xiu'"
                class="relative w-14 h-24 rounded-b-xl overflow-hidden shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300"
              >
                <div class="h-8 bg-[#53301a]"></div>
                <div class="h-9 bg-[#fbf5eb] flex items-center justify-center">
                  <div class="w-6 h-6 bg-white/40 rounded-sm rotate-45"></div>
                </div>
                <div class="h-7 bg-[#ebd5ad]"></div>
              </div>

              <div
                v-else-if="product.artType === 'ca-phe-muoi'"
                class="relative w-14 h-24 rounded-b-xl overflow-hidden shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300"
              >
                <div class="h-7 bg-[#fffaf0] flex items-center justify-center">
                  <div class="w-full h-1 bg-[#ecd9be]/60 mt-auto"></div>
                </div>
                <div class="h-16 bg-gradient-to-b from-[#3a2012] to-[#1a0c05] flex items-center justify-center">
                  <div class="w-6 h-6 bg-white/20 rounded-sm rotate-45"></div>
                </div>
              </div>

              <div
                v-else-if="product.artType === 'cold-brew'"
                class="relative w-14 h-22 bg-gradient-to-b from-[#3a2012]/90 to-[#22130a] rounded-b-xl shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300 flex items-center justify-center h-24"
              >
                <div class="w-6 h-6 bg-white/30 rounded-sm rotate-12 backdrop-blur-[1px]"></div>
                <div class="absolute -top-3 -right-2 w-9 h-9 rounded-full bg-[#f4a127] border-2 border-dashed border-white/60 shadow-sm flex items-center justify-center">
                  <div class="w-6 h-6 rounded-full bg-[#ffb74d]"></div>
                </div>
              </div>

              <div
                v-else-if="product.artType === 'tra-sen'"
                class="relative w-14 h-24 rounded-b-xl overflow-hidden shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300"
              >
                <div class="h-6 bg-[#ffffff] shadow-inner"></div>
                <div class="h-18 bg-gradient-to-b from-[#d4af37]/80 to-[#a37f17] flex flex-col justify-end p-1.5 h-[72px]">
                  <div class="flex gap-1 justify-center">
                    <span class="w-2.5 h-2.5 rounded-full bg-[#f3deb3]"></span>
                    <span class="w-2.5 h-2.5 rounded-full bg-[#f3deb3]"></span>
                  </div>
                </div>
              </div>

              <div
                v-else-if="product.artType === 'espresso-tonic'"
                class="relative w-14 h-24 rounded-b-xl overflow-hidden shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300 flex flex-col"
              >
                <div class="h-8 bg-[#42220f]"></div>
                <div class="flex-1 bg-gradient-to-b from-[#e3ede3] to-[#c2dcc2]/70 flex items-center justify-center relative">
                  <div class="w-2 h-2 rounded-full bg-white/70 absolute bottom-3 left-2"></div>
                  <div class="w-1.5 h-1.5 rounded-full bg-white/80 absolute bottom-7 right-2"></div>
                  <div class="w-4 h-4 rounded-full bg-[#f9e05d] -rotate-12 border border-white"></div>
                </div>
              </div>

              <div
                v-else-if="product.artType === 'matcha'"
                class="relative w-14 h-24 bg-gradient-to-b from-[#4d6d39] to-[#2a441b] rounded-b-xl shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300 flex items-center justify-center"
              >
                <div class="absolute top-2 w-10 h-3 bg-white/40 rounded-full blur-[1px]"></div>
                <div class="w-7 h-7 rounded-sm bg-white/25 rotate-12"></div>
              </div>

              <div
                v-else-if="product.artType === 'croissant'"
                class="relative w-20 h-14 bg-gradient-to-r from-[#d9943f] via-[#efb96b] to-[#c57d2a] rounded-[30px_30px_10px_10px] shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300 flex items-center justify-center"
              >
                <div class="w-4 h-6 bg-[#8a5214]/30 rounded-full mx-1"></div>
                <div class="w-5 h-7 bg-[#8a5214]/20 rounded-full mx-1"></div>
                <div class="w-4 h-6 bg-[#8a5214]/30 rounded-full mx-1"></div>
              </div>

              <div
                v-else
                class="relative w-16 h-16 bg-[#3d2417] rounded-tl-2xl rounded-br-lg shadow-md rotate-[-9deg] group-hover:rotate-0 transition-transform duration-300 overflow-hidden flex flex-col justify-between"
              >
                <div class="h-3 bg-[#e8cdb3]"></div>
                <div class="h-4 bg-[#754728]"></div>
                <div class="h-4 bg-[#2b170c]"></div>
              </div>
            </div>

            <div class="p-[17px] flex flex-col flex-1 justify-between">
              <div>
                <span class="font-label-sm uppercase tracking-[1px] text-outline text-[9px] font-semibold">
                  {{ product.categoryLabel }}
                </span>
                <h3 class="font-label-lg font-bold text-[#2f382f] text-[14px] group-hover:text-primary transition-colors mt-0.5">
                  {{ product.name }}
                </h3>
                <p class="font-body-sm text-[#7a8076] text-[11px] line-clamp-1 mt-0.5">{{ product.desc }}</p>
              </div>
              <div class="flex items-center justify-between pt-1 mt-[14px]">
                <span class="font-label-lg font-bold text-primary-container text-[15px]">{{ formatVnd(product.price) }}</span>
                <button
                  class="px-3.5 py-[7px] rounded-lg bg-[#294b3d] text-on-primary font-label-md text-[12px] font-semibold flex items-center gap-1 hover:bg-primary transition-colors active:scale-95"
                  type="button"
                  @click.stop="addToOrder(product)"
                >
                  <span class="material-symbols-outlined text-[15px]">add</span>
                  <span>Thêm</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- CỘT PHẢI: ORDER CARD -->
      <aside class="w-full lg:w-[330px] flex-shrink-0 bg-[#ffffff] border border-[#e6e8df] rounded-[16px] p-5 shadow-[0_4px_20px_rgba(0,0,0,0.03)] lg:sticky lg:top-[100px] self-start flex flex-col">
        <div class="flex items-center justify-between">
          <span class="font-label-sm uppercase tracking-[1.5px] text-outline text-[10px] font-bold">TẠI QUẦY</span>
          <span class="px-2 py-0.5 rounded-[5px] bg-[#f0f0e9] text-[#878974] font-label-md text-[10px] font-medium">
            Chưa lưu
          </span>
        </div>
        <h2 class="font-headline-sm text-[#2f382f] font-bold text-[18px] mt-1">Đơn đang soạn</h2>

        <!-- Phục vụ: Tại bàn / Mang đi -->
        <div class="grid grid-cols-2 gap-1.5 p-1 rounded-xl bg-surface-container-low font-label-md my-3">
          <button
            type="button"
            class="py-1.5 px-2.5 rounded-lg text-center transition-all flex items-center justify-center gap-1 text-[12px]"
            :class="orderMode === 'dine-in' ? 'bg-surface-container-lowest text-primary font-semibold shadow-sm' : 'text-on-surface-variant hover:text-on-surface'"
            @click="orderMode = 'dine-in'"
          >
            <span class="material-symbols-outlined text-[16px]">table_bar</span>
            <span>Tại bàn (Bàn 04)</span>
          </button>
          <button
            type="button"
            class="py-1.5 px-2.5 rounded-lg text-center transition-all flex items-center justify-center gap-1 text-[12px]"
            :class="orderMode === 'takeaway' ? 'bg-surface-container-lowest text-primary font-semibold shadow-sm' : 'text-on-surface-variant hover:text-on-surface'"
            @click="orderMode = 'takeaway'"
          >
            <span class="material-symbols-outlined text-[16px]">shopping_bag</span>
            <span>Mang đi</span>
          </button>
        </div>

        <!-- Cart Items List -->
        <div class="space-y-2.5 max-h-[380px] overflow-y-auto pr-1 border-b border-[#e6e8df] pb-3 mb-4">
          <div v-if="!cart.length" class="py-6 text-center text-outline font-body-sm">
            Chưa có món nào được chọn
          </div>
          <div
            v-for="item in cart"
            :key="item.id"
            class="bg-surface-container-low/50 p-2.5 rounded-xl flex items-center justify-between gap-2"
          >
            <div class="flex-1 min-w-0">
              <h4 class="font-label-md text-on-surface font-bold text-[13px] truncate">{{ item.name }}</h4>
              <p class="font-body-sm text-[11px] text-outline truncate">{{ formatVnd(item.price) }} · {{ item.note }}</p>
              <div class="font-label-md text-primary font-semibold text-[13px] mt-0.5">
                {{ formatVnd(item.price * item.qty) }}
              </div>
            </div>
            <div class="flex items-center gap-1 bg-[#ffffff] p-1 rounded-lg border border-[#e6e8df]">
              <button
                type="button"
                class="w-[24px] h-[25px] rounded-[6px] border border-[#dce0d5] flex items-center justify-center text-outline hover:bg-surface-container hover:text-on-surface active:scale-95 transition-colors"
                @click="updateQty(item, -1)"
              >
                <span class="material-symbols-outlined text-[14px]">remove</span>
              </button>
              <span class="font-label-md text-on-surface font-bold text-[12px] w-[22px] text-center">{{ item.qty }}</span>
              <button
                type="button"
                class="w-[24px] h-[25px] rounded-[6px] border border-[#dce0d5] flex items-center justify-center text-outline hover:bg-surface-container hover:text-on-surface active:scale-95 transition-colors"
                @click="updateQty(item, 1)"
              >
                <span class="material-symbols-outlined text-[14px]">add</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Form Inputs -->
        <div class="space-y-3">
          <div class="space-y-1">
            <label class="font-label-sm text-outline text-[10px] uppercase font-semibold">Chi nhánh thực hiện</label>
            <div class="relative">
              <select
                v-model="selectedBranch"
                class="w-full appearance-none bg-surface-container-low/40 rounded-[8px] border border-[#dce0d5] px-3 py-[9px] text-[#2f382f] font-label-md text-[12px] focus:outline-none focus:ring-1 focus:ring-primary-container pr-8 cursor-pointer"
              >
                <option value="hk01">Chi nhánh Hoàn Kiếm #01</option>
                <option value="th02">Chi nhánh Tây Hồ #02</option>
                <option value="cg03">Chi nhánh Cầu Giấy #03</option>
              </select>
              <span class="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[18px]">
                expand_more
              </span>
            </div>
          </div>

          <div class="space-y-1">
            <label class="font-label-sm text-outline text-[10px] uppercase font-semibold">Ghi chú đơn hàng</label>
            <textarea
              v-model="orderNote"
              class="w-full bg-surface-container-low/40 rounded-[8px] border border-[#dce0d5] p-[9px_12px] text-[#2f382f] font-body-sm text-[12px] placeholder:text-outline/70 focus:outline-none focus:ring-1 focus:ring-primary-container resize-none"
              placeholder="Ghi chú đơn hàng (ví dụ: ít ngọt, nhiều đá, mang về)..."
              rows="3"
            ></textarea>
          </div>
        </div>

        <!-- Total Row -->
        <div class="flex items-center justify-between my-4 pt-2 border-t border-[#e6e8df]">
          <span class="font-label-md text-[#7a8076] text-[13px]">Tạm tính</span>
          <span class="font-currency-stat text-[#294b3d] text-[23px] font-bold">{{ formatVnd(cartTotal) }}</span>
        </div>

        <!-- Actions -->
        <div class="space-y-2">
          <button
            class="w-full py-[11px] rounded-[9px] bg-[#294b3d] text-on-primary font-label-md font-semibold text-[13px] flex items-center justify-center gap-2 hover:bg-primary transition-all active:scale-[0.98] shadow-sm disabled:opacity-50"
            type="button"
            :disabled="!cart.length"
            @click="saveDraftOrder"
          >
            <span class="material-symbols-outlined text-[18px]">save</span>
            <span>Lưu đơn nháp</span>
          </button>
          <button
            class="w-full py-[10px] rounded-[9px] bg-[#ffffff] border border-[#e6e8df] text-[#2f382f] font-label-md font-semibold text-[12px] flex items-center justify-center gap-2 hover:bg-surface-container transition-all active:scale-[0.98] disabled:opacity-50"
            type="button"
            :disabled="!cart.length"
            @click="printDraftOrder"
          >
            <span class="material-symbols-outlined text-[17px]">print</span>
            <span>In phiếu soạn đơn</span>
          </button>
          <div class="text-center pt-0.5">
            <button
              class="font-label-md text-[#a23d39] text-[11px] font-semibold hover:underline inline-flex items-center gap-1 transition-opacity hover:opacity-80 cursor-pointer"
              type="button"
              @click="clearCart"
            >
              <span class="material-symbols-outlined text-[13px]">delete_sweep</span>
              <span>Xóa danh sách đang soạn</span>
            </button>
          </div>
        </div>

        <p class="font-body-sm text-outline text-[10px] text-center mt-3 leading-tight">
          Dữ liệu giỏ hàng được tự động lưu tạm thời tại máy thu ngân
        </p>
      </aside>
    </div>

    <!-- Toast Notification -->
    <div
      v-if="toastMessage"
      class="fixed bottom-6 right-6 bg-primary-container text-on-primary px-4 py-2.5 rounded-lg shadow-xl font-label-md z-50 transition-all"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>
