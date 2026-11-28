<script setup>
const highlights = [
  {
    icon: 'filter_vintage',
    title: '100% Arabica',
    desc: 'Cầu Đất & Sơn La chất lượng cao',
  },
  {
    icon: 'local_fire_department',
    title: 'Rang thủ công',
    desc: 'Từng mẻ nhỏ giữ trọn hương vị',
  },
  {
    icon: 'eco',
    title: 'Nguyên liệu tươi mỗi ngày',
    desc: 'Sữa thanh trùng & nông sản sạch',
  },
  {
    icon: 'yard',
    title: 'Không gian xanh & yên tĩnh',
    desc: 'Thiết kế tối giản, nhiều cây xanh',
  },
]

const testimonials = [
  {
    initials: 'MA',
    name: 'Minh Anh',
    role: 'Khách quen',
    quote:
      'Cà phê sữa đá ở đây đậm đà đúng gu mình thích, vị cà phê rõ chứ không bị ngấy vị sữa đặc. Quán bài trí trang nhã và nhân viên rất lịch sự.',
  },
  {
    initials: 'TH',
    name: 'Thanh Hà',
    role: 'Thành viên PolyClub',
    quote:
      'Tầng lửng là điểm hẹn làm việc lý tưởng nhất Quận 1 của mình. WiFi ổn định, ghế ngồi êm và không khí cực kỳ tập trung, đồ uống pha rất đều tay.',
  },
  {
    initials: 'ĐH',
    name: 'Đức Huy',
    role: 'Thành viên PolyClub',
    quote:
      'Cold Brew Cam tại PolyCafe có hậu vị thanh tao hiếm thấy. Rất thích cách quán hạn chế rác thải nhựa và dùng ly thuỷ tinh chỉn chu.',
  },
]
</script>

<template>
  <div class="flex flex-col w-full text-on-surface">
    <!-- SECTION 2 — HERO SECTION -->
    <section class="relative w-full min-h-[85vh] bg-[#f6f6f0] flex items-center overflow-hidden py-16 px-margin" id="home">
      <div class="max-w-7xl mx-auto w-full grid grid-cols-1 lg:grid-cols-2 gap-space-xl items-center relative z-10">
        <!-- Left Column: Copy & Actions -->
        <div class="flex flex-col items-start gap-space-md max-w-xl">
          <span class="text-[10px] tracking-[1.8px] font-bold uppercase text-[#7a8076]">
            SPECIALTY COFFEE · SAIGON
          </span>
          <h1 class="font-display-lg text-[44px] md:text-[54px] leading-[1.15] tracking-[-2px] text-[#2f382f]">
            Một quán cà phê.<br class="hidden sm:inline" /> Nhiều điều kết nối.
          </h1>
          <p class="font-body-lg text-[15px] leading-[1.8] text-[#7a8076] max-w-[440px]">
            Nơi ly cà phê được pha với sự tỉ mỉ, và mỗi góc ngồi đều mang một câu chuyện riêng.
          </p>
          <div class="flex flex-wrap items-center gap-space-sm pt-space-xs">
            <a
              class="min-h-[48px] px-7 py-3.5 rounded-[9px] bg-primary text-on-primary font-label-md text-[13px] hover:bg-primary-container transition-all duration-200 shadow-sm flex items-center justify-center gap-2 group"
              href="#menu"
            >
              <span>Khám phá thực đơn</span>
              <span class="group-hover:translate-x-1 transition-transform">→</span>
            </a>
            <a
              class="min-h-[48px] px-6 py-3.5 rounded-[9px] bg-surface-container-lowest text-primary font-label-md text-[13px] hover:bg-surface-container-low transition-colors shadow-sm flex items-center justify-center"
              href="#contact"
            >
              Tìm cửa hàng
            </a>
          </div>
          <div class="flex items-center gap-2 text-[11px] text-[#7a8076] pt-space-xs">
            <span class="text-[13px]">☕</span>
            <span class="font-medium tracking-wide">Mở cửa mỗi ngày · 7:00 — 22:00</span>
          </div>
        </div>

        <!-- Right Column: Abstract CSS Coffee Art Artwork -->
        <div class="relative w-full h-[420px] lg:h-[480px] flex items-center justify-center select-none">
          <div class="absolute -top-12 -right-8 w-72 h-72 rounded-full bg-[#edf2e9] blur-2xl opacity-75"></div>
          <div class="absolute -bottom-8 -left-6 w-64 h-64 rounded-full bg-[#e3b674]/25 blur-xl"></div>

          <div class="absolute top-6 right-10 rotate-12 select-none pointer-events-none">
            <span class="font-headline-md italic text-[21px] text-[#b6c4a2] tracking-wider opacity-85">
              Brew. Serve. Connect.
            </span>
          </div>

          <!-- The Floating Giant CSS Cup Illustration -->
          <div class="relative w-[320px] h-[320px] flex items-center justify-center animate-float-cup cursor-default">
            <div class="absolute bottom-6 w-[290px] h-[105px] rounded-[50%] bg-[#8fa17d] shadow-[0_18px_32px_rgba(41,75,61,0.18)] flex items-center justify-center">
              <div class="w-[260px] h-[85px] rounded-[50%] bg-[#9eaf8d] opacity-90 flex items-center justify-center">
                <div class="w-[210px] h-[65px] rounded-[50%] bg-[#7e906c]"></div>
              </div>
            </div>
            <div class="absolute bottom-16 w-[200px] h-[120px] rounded-b-[100px] bg-[#294b3d]/10 blur-sm"></div>
            <div class="absolute bottom-14 w-[210px] h-[160px] rounded-b-[110px] bg-[#d9d6c1] shadow-inner flex flex-col items-center justify-start pt-2">
              <div class="absolute right-[-42px] top-6 w-[64px] h-[95px] rounded-r-[45px] rounded-l-[10px] border-[14px] border-[#d9d6c1] -z-10 shadow-sm"></div>
              <div class="w-[204px] h-[84px] rounded-[50%] bg-[#e9e4cf] shadow-sm flex items-center justify-center p-2">
                <div class="relative w-[184px] h-[72px] rounded-[50%] bg-gradient-to-tr from-[#aa7c4d] via-[#855627] to-[#d5b689] overflow-hidden shadow-inner flex items-center justify-center">
                  <div class="absolute w-[110px] h-[45px] rounded-[50%] bg-[#f7eedf]/85 blur-[1px] rotate-[-8deg] flex items-center justify-center">
                    <div class="w-[80px] h-[28px] rounded-[50%] bg-[#aa7c4d]/50"></div>
                  </div>
                  <div class="absolute w-[60px] h-[25px] rounded-[50%] bg-[#fffcf5]/90 blur-[0.5px]"></div>
                  <div class="absolute bottom-3 left-6 w-3 h-2 rounded-[50%] bg-[#fdfaf3]/80"></div>
                  <div class="absolute top-3 right-8 w-2 h-1.5 rounded-[50%] bg-[#fdfaf3]/70"></div>
                </div>
              </div>
            </div>
            <div class="absolute -top-6 w-16 h-8 rounded-full border-t-2 border-dashed border-[#b6c4a2]/50 rotate-[-15deg] blur-[0.5px]"></div>
            <div class="absolute -top-12 left-28 w-12 h-6 rounded-full border-t border-dashed border-[#b6c4a2]/40 rotate-[10deg]"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 3 — HIGHLIGHT BAR -->
    <section class="w-full bg-surface-container-lowest border-y border-[#e6e8df] py-[36px] px-margin">
      <div class="max-w-7xl mx-auto grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-space-lg">
        <div v-for="item in highlights" :key="item.title" class="flex items-start gap-space-sm">
          <div class="w-10 h-10 rounded-lg bg-surface-container-low text-primary flex items-center justify-center shrink-0">
            <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
          </div>
          <div class="flex flex-col">
            <h3 class="font-headline-sm text-[15px] font-semibold text-[#2f382f]">{{ item.title }}</h3>
            <p class="font-body-sm text-[12px] text-[#7a8076] leading-tight mt-0.5">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 4 — THỰC ĐƠN NỔI BẬT (FEATURED MENU) -->
    <section class="w-full bg-[#f6f6f0] py-20 px-margin" id="menu">
      <div class="max-w-7xl mx-auto flex flex-col items-center">
        <div class="text-center max-w-2xl flex flex-col items-center gap-space-xs mb-14">
          <span class="text-[10px] tracking-[2px] font-bold uppercase text-[#7a8076]">THỰC ĐƠN</span>
          <h2 class="font-headline-lg text-[32px] md:text-[38px] text-[#2f382f] tracking-tight">
            Những ly cà phê được yêu thích nhất.
          </h2>
          <p class="font-body-sm text-[13px] text-[#7a8076]">
            Mỗi thức uống đều được chăm chút từ hạt cà phê đến cách trình bày.
          </p>
        </div>

        <div class="w-full grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <!-- Drink 1: Cà Phê Sữa Đá -->
          <div class="group bg-surface-container-lowest rounded-xl p-5 border border-[#e6e8df] hover:-translate-y-1 hover:shadow-lg transition-all duration-300 flex flex-col justify-between">
            <div>
              <div class="w-full h-44 rounded-lg bg-[#e9e7dc] flex items-center justify-center relative overflow-hidden mb-4">
                <div class="relative w-16 h-28 rounded-b-xl border-2 border-white/80 bg-gradient-to-t from-[#6e4624] via-[#c69766] to-[#ffffff]/90 flex flex-col justify-end p-1 shadow-sm">
                  <div class="absolute top-3 left-2 w-4 h-4 rounded-sm bg-white/70 rotate-12"></div>
                  <div class="absolute top-9 right-2 w-5 h-4 rounded-sm bg-white/60 -rotate-6"></div>
                  <div class="w-full h-6 rounded-b-lg bg-[#fff8e7]"></div>
                </div>
                <span class="absolute top-3 right-3 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-white/80 text-[#294b3d]">
                  Cà phê
                </span>
              </div>
              <div class="flex items-baseline justify-between mb-1">
                <h3 class="font-headline-sm text-[17px] text-[#2f382f]">Cà Phê Sữa Đá</h3>
                <span class="font-currency-stat text-[16px] text-primary">35.000 ₫</span>
              </div>
              <p class="font-body-sm text-[12px] text-[#7a8076] leading-relaxed">
                Phin truyền thống, sữa đặc thơm béo, đá viên mát lạnh đặc trưng Sài Gòn.
              </p>
            </div>
            <div class="mt-4 pt-3 border-t border-[#e6e8df]/70 flex items-center justify-between text-[11px] text-[#7a8076]">
              <span>Độ ngọt: Vừa phải</span>
              <span class="text-primary font-medium group-hover:underline">Chi tiết</span>
            </div>
          </div>

          <!-- Drink 2: Bạc Xỉu -->
          <div class="group bg-surface-container-lowest rounded-xl p-5 border border-[#e6e8df] hover:-translate-y-1 hover:shadow-lg transition-all duration-300 flex flex-col justify-between">
            <div>
              <div class="w-full h-44 rounded-lg bg-[#e5eadd] flex items-center justify-center relative overflow-hidden mb-4">
                <div class="relative w-16 h-28 rounded-b-xl border-2 border-white/80 bg-gradient-to-t from-[#fffdf5] via-[#faefe0] to-[#8d623d] flex flex-col justify-between p-1 shadow-sm">
                  <div class="w-full h-7 rounded-t-sm bg-[#5c3c1e]"></div>
                  <div class="w-full h-10 bg-[#f7eedf]/95"></div>
                  <div class="w-full h-7 rounded-b-lg bg-[#fff6db]"></div>
                </div>
                <span class="absolute top-3 right-3 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-white/80 text-[#294b3d]">
                  Cà phê
                </span>
              </div>
              <div class="flex items-baseline justify-between mb-1">
                <h3 class="font-headline-sm text-[17px] text-[#2f382f]">Bạc Xỉu</h3>
                <span class="font-currency-stat text-[16px] text-primary">39.000 ₫</span>
              </div>
              <p class="font-body-sm text-[12px] text-[#7a8076] leading-relaxed">
                Nhiều sữa tươi thanh trùng, ít cà phê phin, béo ngậy ngọt dịu êm ái.
              </p>
            </div>
            <div class="mt-4 pt-3 border-t border-[#e6e8df]/70 flex items-center justify-between text-[11px] text-[#7a8076]">
              <span>3 tầng hương vị</span>
              <span class="text-primary font-medium group-hover:underline">Chi tiết</span>
            </div>
          </div>

          <!-- Drink 3: Cold Brew Cam -->
          <div class="group bg-surface-container-lowest rounded-xl p-5 border border-[#e6e8df] hover:-translate-y-1 hover:shadow-lg transition-all duration-300 flex flex-col justify-between">
            <div>
              <div class="w-full h-44 rounded-lg bg-[#f0e5d5] flex items-center justify-center relative overflow-hidden mb-4">
                <div class="relative w-16 h-28 rounded-b-xl border-2 border-white/80 bg-gradient-to-t from-[#f59e0b] to-[#452b19] flex items-center justify-center shadow-sm">
                  <div class="absolute -top-2 -right-3 w-8 h-8 rounded-full bg-[#f97316] border-2 border-white flex items-center justify-center shadow">
                    <div class="w-5 h-5 rounded-full border border-dashed border-white/70"></div>
                  </div>
                  <div class="w-5 h-5 rounded-sm bg-white/50 rotate-45"></div>
                </div>
                <span class="absolute top-3 right-3 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-[#ffd08b] text-[#7a581f]">
                  Đặc biệt
                </span>
              </div>
              <div class="flex items-baseline justify-between mb-1">
                <h3 class="font-headline-sm text-[17px] text-[#2f382f]">Cold Brew Cam</h3>
                <span class="font-currency-stat text-[16px] text-primary">55.000 ₫</span>
              </div>
              <p class="font-body-sm text-[12px] text-[#7a8076] leading-relaxed">
                Cold brew ủ chậm 18 giờ, kết hợp nước cam vàng mọng nước sảng khoái.
              </p>
            </div>
            <div class="mt-4 pt-3 border-t border-[#e6e8df]/70 flex items-center justify-between text-[11px] text-[#7a8076]">
              <span>Ủ lạnh 18h</span>
              <span class="text-primary font-medium group-hover:underline">Chi tiết</span>
            </div>
          </div>

          <!-- Drink 4: Matcha Latte -->
          <div class="group bg-surface-container-lowest rounded-xl p-5 border border-[#e6e8df] hover:-translate-y-1 hover:shadow-lg transition-all duration-300 flex flex-col justify-between">
            <div>
              <div class="w-full h-44 rounded-lg bg-[#e2e9e8] flex items-center justify-center relative overflow-hidden mb-4">
                <div class="relative w-16 h-28 rounded-b-xl border-2 border-white/80 bg-gradient-to-t from-[#53734b] via-[#7d9b73] to-[#eaf2e7] flex flex-col items-center justify-start p-1 shadow-sm">
                  <div class="w-10 h-3 rounded-full bg-white/80 mt-1"></div>
                  <div class="absolute bottom-4 w-5 h-5 rounded-sm bg-white/40 rotate-12"></div>
                </div>
                <span class="absolute top-3 right-3 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-white/80 text-[#294b3d]">
                  Trà &amp; Sữa
                </span>
              </div>
              <div class="flex items-baseline justify-between mb-1">
                <h3 class="font-headline-sm text-[17px] text-[#2f382f]">Matcha Latte</h3>
                <span class="font-currency-stat text-[16px] text-primary">55.000 ₫</span>
              </div>
              <p class="font-body-sm text-[12px] text-[#7a8076] leading-relaxed">
                Bột matcha Uji tuyển chọn từ Nhật Bản, quyện cùng sữa tươi thanh ngọt.
              </p>
            </div>
            <div class="mt-4 pt-3 border-t border-[#e6e8df]/70 flex items-center justify-between text-[11px] text-[#7a8076]">
              <span>Matcha Uji nguyên chất</span>
              <span class="text-primary font-medium group-hover:underline">Chi tiết</span>
            </div>
          </div>

          <!-- Drink 5: Espresso Tonic -->
          <div class="group bg-surface-container-lowest rounded-xl p-5 border border-[#e6e8df] hover:-translate-y-1 hover:shadow-lg transition-all duration-300 flex flex-col justify-between">
            <div>
              <div class="w-full h-44 rounded-lg bg-[#e9e7dc] flex items-center justify-center relative overflow-hidden mb-4">
                <div class="relative w-16 h-28 rounded-b-xl border-2 border-white/80 bg-gradient-to-t from-[#dce7eb] via-[#8db0be]/40 to-[#3b2314] flex flex-col justify-between p-1 shadow-sm">
                  <div class="w-8 h-2 rounded-full bg-[#fde047] self-center mt-1 rotate-[-12deg]"></div>
                  <div class="flex gap-1 justify-center pb-2">
                    <div class="w-1.5 h-1.5 rounded-full bg-white/70"></div>
                    <div class="w-1 h-1 rounded-full bg-white/90"></div>
                    <div class="w-1.5 h-1.5 rounded-full bg-white/60"></div>
                  </div>
                </div>
                <span class="absolute top-3 right-3 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-[#ffd08b] text-[#7a581f]">
                  Đặc biệt
                </span>
              </div>
              <div class="flex items-baseline justify-between mb-1">
                <h3 class="font-headline-sm text-[17px] text-[#2f382f]">Espresso Tonic</h3>
                <span class="font-currency-stat text-[16px] text-primary">49.000 ₫</span>
              </div>
              <p class="font-body-sm text-[12px] text-[#7a8076] leading-relaxed">
                Double shot espresso sánh đậm bồng bềnh trên tonic sủi bọt cùng vỏ chanh thơm.
              </p>
            </div>
            <div class="mt-4 pt-3 border-t border-[#e6e8df]/70 flex items-center justify-between text-[11px] text-[#7a8076]">
              <span>Sảng khoái &amp; thanh mát</span>
              <span class="text-primary font-medium group-hover:underline">Chi tiết</span>
            </div>
          </div>

          <!-- Drink 6: Trà Sen Vàng -->
          <div class="group bg-surface-container-lowest rounded-xl p-5 border border-[#e6e8df] hover:-translate-y-1 hover:shadow-lg transition-all duration-300 flex flex-col justify-between">
            <div>
              <div class="w-full h-44 rounded-lg bg-[#e5eadd] flex items-center justify-center relative overflow-hidden mb-4">
                <div class="relative w-16 h-28 rounded-b-xl border-2 border-white/80 bg-gradient-to-t from-[#c99834] via-[#e5b95a] to-[#fffde8] flex flex-col justify-end items-center p-2 shadow-sm">
                  <div class="w-4 h-4 rounded-full bg-[#e8ce90] border border-white/60 mb-1"></div>
                  <div class="w-3 h-3 rounded-full bg-[#e8ce90] border border-white/60"></div>
                </div>
                <span class="absolute top-3 right-3 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-white/80 text-[#294b3d]">
                  Trà
                </span>
              </div>
              <div class="flex items-baseline justify-between mb-1">
                <h3 class="font-headline-sm text-[17px] text-[#2f382f]">Trà Sen Vàng</h3>
                <span class="font-currency-stat text-[16px] text-primary">45.000 ₫</span>
              </div>
              <p class="font-body-sm text-[12px] text-[#7a8076] leading-relaxed">
                Trà ướp sen Bách Diệp Tây Hồ thanh tao, mật ong hoa rừng cùng hạt sen bùi béo.
              </p>
            </div>
            <div class="mt-4 pt-3 border-t border-[#e6e8df]/70 flex items-center justify-between text-[11px] text-[#7a8076]">
              <span>Hương thơm tao nhã</span>
              <span class="text-primary font-medium group-hover:underline">Chi tiết</span>
            </div>
          </div>
        </div>

        <a
          class="mt-12 inline-flex items-center gap-2 font-label-lg text-primary hover:text-primary-container transition-colors group"
          href="#menu"
        >
          <span>Xem toàn bộ thực đơn</span>
          <span class="group-hover:translate-x-1.5 transition-transform">→</span>
        </a>
      </div>
    </section>

    <!-- SECTION 5 — CÂU CHUYỆN QUÁN (OUR STORY) -->
    <section class="w-full bg-surface-container-lowest py-24 px-margin border-t border-[#e6e8df]" id="story">
      <div class="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-space-xl items-center">
        <!-- Left Column -->
        <div class="lg:col-span-5 w-full">
          <div class="relative w-full aspect-[4/5] max-h-[500px] rounded-2xl border border-[#e6e8df] bg-[#e9e4cf]/40 p-6 flex flex-col justify-between overflow-hidden shadow-sm">
            <div class="absolute -right-16 -top-16 w-56 h-56 rounded-full bg-[#294b3d]/10"></div>
            <div class="absolute left-6 bottom-10 w-44 h-44 rounded-full bg-[#e3b674]/20 blur-md"></div>
            <div class="absolute inset-0 bg-gradient-to-b from-transparent via-[#e9e4cf]/20 to-[#294b3d]/5 pointer-events-none"></div>

            <div class="relative z-10 w-full flex items-center justify-between">
              <span class="text-[11px] font-bold tracking-widest text-[#745c43] uppercase">Kể từ 2021</span>
              <div class="w-2.5 h-2.5 rounded-full bg-[#294b3d]"></div>
            </div>

            <div class="relative z-10 flex flex-col items-center justify-center my-auto">
              <div class="relative w-28 h-36 rounded-[50%] bg-[#745c43] shadow-md flex items-center justify-center rotate-[-15deg]">
                <div class="w-1.5 h-28 rounded-full bg-[#e9e4cf]/75 -rotate-6"></div>
              </div>
              <div
                class="absolute w-44 h-44 rounded-full border border-dashed border-[#745c43]/30 animate-spin"
                style="animation-duration: 40s;"
              ></div>
            </div>

            <div class="relative z-10 flex items-end justify-between">
              <p class="font-headline-lg italic text-[#294b3d] text-2xl font-normal">
                Slow Drip.<br />Real Connection.
              </p>
              <span class="font-body-sm text-[11px] text-[#7a8076]">Origin: Đà Lạt · Sơn La</span>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="lg:col-span-7 flex flex-col items-start gap-space-md lg:pl-space-md">
          <span class="text-[10px] tracking-[2px] font-bold uppercase text-[#7a8076]">CÂU CHUYỆN CỦA CHÚNG TÔI</span>
          <h2 class="font-headline-lg text-[32px] text-[#2f382f] leading-snug">
            Bắt đầu từ một ý tưởng đơn giản.
          </h2>
          <div class="flex flex-col gap-4 font-body-lg text-[15px] leading-[1.8] text-[#7a8076]">
            <p>
              PolyCafe ra đời từ niềm tin rằng: một ly cà phê ngon có sức mạnh biến một buổi sáng bình thường trở nên đáng nhớ. Chúng tôi chọn đồng hành cùng những người nông dân tâm huyết tại Cầu Đất (Lâm Đồng) và Chiềng Ban (Sơn La), nơi từng hạt cà phê Arabica được hái chín đỏ mọng và sơ chế tỉ mỉ.
            </p>
            <p>
              Không ồn ào vội vã, PolyCafe là chốn dừng chân giữa lòng thành phố nhộn nhịp. Một không gian vừa đủ riêng tư để bạn tập trung làm việc, vừa đủ ấm áp để sẻ chia một buổi hẹn thân tình cùng bạn bè hay đơn giản là tự thưởng cho mình một thoáng tĩnh lặng bên hương thơm cà phê mới xay.
            </p>
          </div>
          <div class="pt-space-xs">
            <p class="font-headline-md italic text-[18px] text-[#294b3d]">
              — Đội ngũ PolyCafe
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 6 — KHÔNG GIAN (SPACE & ATMOSPHERE) -->
    <section class="w-full bg-[#edf2e9] py-24 px-margin" id="space">
      <div class="max-w-7xl mx-auto flex flex-col items-center">
        <div class="text-center max-w-xl flex flex-col items-center gap-space-xs mb-14">
          <span class="text-[10px] tracking-[2px] font-bold uppercase text-[#7a8076]">KHÔNG GIAN</span>
          <h2 class="font-headline-lg text-[32px] md:text-[36px] text-[#2f382f]">
            Một góc yên để bắt đầu ngày mới.
          </h2>
        </div>

        <div class="w-full grid grid-cols-1 md:grid-cols-3 gap-space-lg">
          <!-- Space 1: Bar Area -->
          <div class="bg-surface-container-lowest rounded-2xl overflow-hidden shadow-sm border border-[#e6e8df] flex flex-col">
            <div class="w-full h-52 bg-[#e9e7dc] relative flex items-center justify-center p-6 overflow-hidden">
              <div class="absolute top-0 flex gap-12">
                <div class="w-1 h-14 bg-[#7a8076]/40 flex flex-col items-center justify-end">
                  <div class="w-6 h-4 rounded-b-md bg-[#e3b674]"></div>
                  <div class="w-10 h-10 rounded-full bg-[#e3b674]/20 blur-sm -mt-2"></div>
                </div>
                <div class="w-1 h-14 bg-[#7a8076]/40 flex flex-col items-center justify-end">
                  <div class="w-6 h-4 rounded-b-md bg-[#e3b674]"></div>
                  <div class="w-10 h-10 rounded-full bg-[#e3b674]/20 blur-sm -mt-2"></div>
                </div>
              </div>
              <div class="absolute bottom-0 w-full h-16 bg-[#745c43] flex items-start justify-center pt-2 border-t-4 border-[#8e7356]">
                <div class="w-8 h-6 rounded-t-sm bg-[#ffffff]/80 shadow-inner"></div>
              </div>
            </div>
            <div class="p-6 flex flex-col gap-2">
              <h3 class="font-headline-sm text-[18px] text-[#2f382f]">Tầng trệt · Quầy bar</h3>
              <p class="font-body-sm text-[13px] text-[#7a8076] leading-relaxed">
                Nơi bạn ngồi xem barista pha cà phê. Ánh sáng ấm áp, nhạc nhẹ du dương và hương thơm cà phê lan tỏa khắp gian phòng.
              </p>
            </div>
          </div>

          <!-- Space 2: Mezzanine Work Corner -->
          <div class="bg-surface-container-lowest rounded-2xl overflow-hidden shadow-sm border border-[#e6e8df] flex flex-col">
            <div class="w-full h-52 bg-[#e3e8de] relative flex items-center justify-center p-6 overflow-hidden">
              <div class="w-32 h-36 rounded-t-xl border-2 border-[#b5c2ad] bg-white/70 flex items-center justify-center">
                <div class="w-full h-0.5 bg-[#b5c2ad]"></div>
              </div>
              <div class="absolute bottom-4 w-48 h-2 bg-[#294b3d] rounded-full"></div>
              <div class="absolute bottom-6 right-20 w-8 h-12 flex flex-col items-center">
                <div class="w-6 h-3 rounded-t-full bg-[#e3b674]"></div>
                <div class="w-0.5 h-8 bg-[#7a8076]"></div>
              </div>
            </div>
            <div class="p-6 flex flex-col gap-2">
              <h3 class="font-headline-sm text-[18px] text-[#2f382f]">Tầng lửng · Góc làm việc</h3>
              <p class="font-body-sm text-[13px] text-[#7a8076] leading-relaxed">
                WiFi tốc độ cao, từng bàn đều sẵn ổ cắm, không gian yên tĩnh tuyệt đối — lý tưởng cho những buổi sáng làm việc tập trung.
              </p>
            </div>
          </div>

          <!-- Space 3: Garden Outdoor -->
          <div class="bg-surface-container-lowest rounded-2xl overflow-hidden shadow-sm border border-[#e6e8df] flex flex-col">
            <div class="w-full h-52 bg-[#e7ebe0] relative flex items-end justify-center pb-2 overflow-hidden">
              <div class="absolute bottom-0 -left-6 w-24 h-40 rounded-[80px_0px_80px_0px] bg-[#294b3d] opacity-85 rotate-12"></div>
              <div class="absolute bottom-0 -right-4 w-28 h-44 rounded-[0px_90px_0px_90px] bg-[#436556] opacity-90 -rotate-12"></div>
              <div class="absolute bottom-2 w-20 h-28 rounded-full bg-[#8fa17d]/80 blur-[0.5px]"></div>
              <div class="absolute -top-8 right-6 w-16 h-36 bg-gradient-to-b from-white/70 to-transparent rotate-[25deg]"></div>
            </div>
            <div class="p-6 flex flex-col gap-2">
              <h3 class="font-headline-sm text-[18px] text-[#2f382f]">Sân vườn · Khu ngoài trời</h3>
              <p class="font-body-sm text-[13px] text-[#7a8076] leading-relaxed">
                Cây xanh rợp mát, bóng nắng dịu nhẹ, gió trời tự nhiên và một ly cold brew cho những buổi chiều thảnh thơi trò chuyện.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 7 — ĐÁNH GIÁ KHÁCH HÀNG (TESTIMONIALS) -->
    <section class="w-full bg-[#f6f6f0] py-20 px-margin">
      <div class="max-w-7xl mx-auto flex flex-col items-center">
        <h2 class="font-headline-lg text-[28px] md:text-[32px] text-[#2f382f] text-center mb-12">
          Khách hàng nói gì về PolyCafe?
        </h2>

        <div class="w-full grid grid-cols-1 md:grid-cols-3 gap-6">
          <div
            v-for="review in testimonials"
            :key="review.name"
            class="bg-surface-container-lowest border border-[#e6e8df] rounded-[14px] p-7 flex flex-col justify-between shadow-sm"
          >
            <div>
              <span class="font-headline-lg text-[#e3b674] text-5xl leading-none select-none block mb-2">“</span>
              <p class="font-body-md text-[14px] text-[#2f382f] leading-relaxed">
                {{ review.quote }}
              </p>
            </div>
            <div class="flex items-center gap-3 pt-6 mt-6 border-t border-[#e6e8df]/70">
              <div class="w-[33px] h-[33px] rounded-full bg-[#ebe9db] text-[#294b3d] font-bold text-[13px] flex items-center justify-center shrink-0">
                {{ review.initials }}
              </div>
              <div class="flex flex-col">
                <span class="font-body-md font-semibold text-[13px] text-[#2f382f]">{{ review.name }}</span>
                <span class="text-[11px] text-[#7a8076]">{{ review.role }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 8 — LIÊN HỆ & ĐỊA CHỈ (CONTACT & LOCATION) -->
    <section class="w-full bg-surface-container-lowest py-24 px-margin border-t border-[#e6e8df]" id="contact">
      <div class="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-space-xl items-center">
        <div class="flex flex-col items-start gap-space-md">
          <span class="text-[10px] tracking-[2px] font-bold uppercase text-[#7a8076]">GHÉ THĂM CHÚNG TÔI</span>
          <h2 class="font-headline-lg text-[30px] md:text-[34px] text-[#2f382f] tracking-tight">
            Luôn chào đón bạn.
          </h2>

          <div class="flex flex-col gap-4 font-body-md text-[14px] text-[#2f382f] w-full pt-2">
            <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-[20px] text-primary shrink-0 mt-0.5">location_on</span>
              <div>
                <p class="font-semibold text-[#2f382f]">Địa chỉ cửa hàng</p>
                <p class="text-[#7a8076] text-[13px]">123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-[20px] text-primary shrink-0 mt-0.5">call</span>
              <div>
                <p class="font-semibold text-[#2f382f]">Hotline hỗ trợ</p>
                <p class="text-[#7a8076] text-[13px]">0901 234 567</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-[20px] text-primary shrink-0 mt-0.5">mail</span>
              <div>
                <p class="font-semibold text-[#2f382f]">Thư điện tử</p>
                <p class="text-[#7a8076] text-[13px]">hello@polycafe.vn</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-[20px] text-primary shrink-0 mt-0.5">schedule</span>
              <div>
                <p class="font-semibold text-[#2f382f]">Giờ hoạt động</p>
                <p class="text-[#7a8076] text-[13px]">Thứ Hai — Chủ Nhật · 7:00 — 22:00</p>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-3 pt-4">
            <a
              class="w-10 h-10 rounded-full bg-[#edf2e9] text-[#294b3d] flex items-center justify-center hover:bg-primary hover:text-white transition-colors text-[13px] font-bold"
              href="#"
              title="Facebook"
            >
              FB
            </a>
            <a
              class="w-10 h-10 rounded-full bg-[#edf2e9] text-[#294b3d] flex items-center justify-center hover:bg-primary hover:text-white transition-colors text-[13px] font-bold"
              href="#"
              title="Instagram"
            >
              IG
            </a>
            <a
              class="w-10 h-10 rounded-full bg-[#edf2e9] text-[#294b3d] flex items-center justify-center hover:bg-primary hover:text-white transition-colors text-[13px] font-bold"
              href="#"
              title="TikTok"
            >
              TT
            </a>
          </div>
        </div>

        <!-- Right Column: Abstract Map Preview Mockup -->
        <div class="w-full h-[360px] md:h-[400px] rounded-2xl bg-[#edf2e9] border border-[#e6e8df] relative overflow-hidden flex items-center justify-center shadow-inner">
          <div class="absolute inset-0 opacity-40">
            <div class="absolute top-1/4 left-0 right-0 h-4 bg-white/70 -rotate-3"></div>
            <div class="absolute top-2/3 left-0 right-0 h-6 bg-white/70 rotate-6"></div>
            <div class="absolute top-0 bottom-0 left-1/3 w-6 bg-white/70 rotate-12"></div>
            <div class="absolute top-0 bottom-0 right-1/4 w-8 bg-white/80 -rotate-6"></div>
            <div class="absolute top-12 left-10 w-28 h-20 bg-[#d8e3d3] rounded-lg"></div>
            <div class="absolute bottom-10 right-12 w-32 h-24 bg-[#d8e3d3] rounded-lg"></div>
          </div>

          <div class="relative z-10 flex flex-col items-center">
            <div class="absolute -top-3 w-16 h-16 rounded-full bg-primary/15 animate-ping"></div>
            <div class="w-11 h-11 rounded-full bg-primary text-white flex items-center justify-center shadow-lg relative z-10">
              <span class="material-symbols-outlined text-[22px]">storefront</span>
            </div>
            <div class="w-2.5 h-2.5 bg-primary rotate-45 -mt-1.5 shadow"></div>
            <div class="mt-3 bg-surface-container-lowest px-4 py-2 rounded-lg shadow-md border border-[#e6e8df] flex flex-col items-center text-center">
              <span class="font-headline-sm text-[13px] font-bold text-[#2f382f]">PolyCafe Nguyễn Huệ</span>
              <span class="text-[11px] text-[#7a8076]">Phố đi bộ Nguyễn Huệ, Quận 1</span>
            </div>
          </div>

          <div class="absolute bottom-3 right-4 text-[10px] text-[#7a8076] bg-white/80 px-2 py-0.5 rounded">
            Sài Gòn · Trung tâm Q1
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 9 — CTA BANNER -->
    <section class="w-full bg-[#294b3d] text-on-primary py-20 px-margin relative overflow-hidden">
      <div class="absolute -left-20 -top-20 w-80 h-80 rounded-full border border-white/5 pointer-events-none"></div>
      <div class="absolute -right-20 -bottom-20 w-96 h-96 rounded-full border border-white/5 pointer-events-none"></div>
      <div class="max-w-4xl mx-auto flex flex-col items-center text-center relative z-10">
        <h2 class="font-headline-lg text-[32px] md:text-[38px] leading-tight text-white mb-3">
          Sẵn sàng cho ly cà phê tiếp theo?
        </h2>
        <p class="font-body-md text-[14px] text-[#afc0a9] max-w-lg mb-8">
          Đặt bàn trước để có chỗ ngồi ưng ý hoặc ghé thăm chúng tôi bất cứ lúc nào bạn cần một khoảng lặng.
        </p>
        <div class="flex flex-wrap items-center justify-center gap-4">
          <a
            class="min-h-[46px] px-8 py-3 rounded-lg bg-[#e3b674] text-[#2d4538] font-bold text-[14px] hover:bg-[#d6a55e] transition-all duration-200 shadow-md"
            href="#contact"
          >
            Đặt bàn ngay
          </a>
          <a
            class="min-h-[46px] px-8 py-3 rounded-lg bg-transparent text-white border border-white/40 hover:bg-white/10 transition-colors font-medium text-[14px]"
            href="#menu"
          >
            Xem thực đơn
          </a>
        </div>
      </div>
    </section>
  </div>
</template>
