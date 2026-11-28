"use strict";

(() => {
  const context = document.querySelector('meta[name="app-context"]')?.content || "";
  const screen = document.body.dataset.screen;
  const manager = document.body.dataset.manager === "true";
  const $ = (selector) => document.querySelector(selector);
  const money = (value) => new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 })
    .format(typeof value === "bigint" ? value : /^-?\d+$/.test(String(value)) ? BigInt(value) : Number(value || 0));
  const tableNames = {
    app_user: "Tài khoản", approval_request: "Đề nghị phê duyệt", async_operation: "Đồng bộ & tác vụ nền",
    audit_log: "Nhật ký thao tác", branch: "Chi nhánh", cash_entry: "Thu chi trong ca", combo_item: "Thành phần combo",
    complaint: "Phản hồi & khiếu nại", customer: "Khách hàng", dining_session: "Phiên phục vụ", dining_table: "Bàn",
    fulfillment_event: "Lịch sử gọi & giao món", ingredient: "Nguyên liệu", ingredient_unit: "Quy đổi đơn vị",
    integration_config: "Kết nối đối tác", invoice: "Đơn hàng / hóa đơn", invoice_line: "Chi tiết món",
    invoice_line_ingredient: "Nguyên liệu theo món", menu_item: "Món & combo", menu_variant: "Kích cỡ & tùy chọn",
    pager_device: "Thiết bị rung", payment: "Thanh toán & hoàn tiền", point_ledger: "Sổ điểm thành viên",
    price_rule: "Bảng giá", promotion: "Khuyến mãi & voucher", purchase_order: "Đơn mua", purchase_order_line: "Chi tiết đơn mua",
    recipe: "Phiên bản công thức", recipe_line: "Định lượng công thức", shift: "Ca làm việc", shift_assignment: "Phân công nhân viên",
    station: "Quầy chế biến", stock_document: "Chứng từ kho", stock_document_line: "Chi tiết chứng từ", stock_lot: "Lô nguyên liệu",
    stock_movement: "Sổ kho", supplier: "Nhà cung cấp", ticket: "Ticket nhận món", user_access: "Quyền truy cập"
  };
  const labels = {
    day: "Ngày", orders: "Số hóa đơn", gross: "Giá trị bán", refunds: "Hoàn tiền", net: "Doanh thu thuần",
    id: "Mã định danh", name: "Tên", code: "Mã", status: "Trạng thái", kind: "Loại", full_name: "Họ tên", username: "Tên đăng nhập",
    password: "Mật khẩu mới", account_kind: "Loại tài khoản", phone: "Số điện thoại", email: "Email", description: "Mô tả", note: "Ghi chú",
    created_at: "Ngày tạo", updated_at: "Cập nhật", branch_id: "Chi nhánh", invoice_id: "Đơn hàng", invoice_no: "Số hóa đơn",
    item_id: "Món", variant_id: "Kích cỡ / tùy chọn", combo_id: "Combo", size_id: "Kích cỡ", category_code: "Mã danh mục",
    category_name: "Danh mục", station_kind: "Loại quầy", allergen_note: "Lưu ý dị ứng", price: "Giá bán", extra_price: "Phụ thu",
    quantity: "Số lượng", unit_price: "Đơn giá", total: "Phải thu", subtotal: "Tạm tính", discount_total: "Tổng giảm", paid_total: "Đã thu",
    customer_id: "Khách hàng", member_code: "Mã thành viên", tier: "Hạng", points_balance: "Điểm hiện tại", points: "Điểm thay đổi",
    balance_after: "Điểm sau giao dịch", origin_entry_id: "Bút toán điểm gốc", expires_at: "Hết hạn", table_id: "Bàn", session_id: "Phiên phục vụ",
    seats: "Số chỗ", zone_code: "Khu vực", qr_token: "Mã QR", station_id: "Quầy", shift_id: "Ca", user_id: "Nhân viên / tài khoản",
    actor_id: "Người thực hiện", approved_by: "Người duyệt", approver_id: "Người duyệt", requested_by: "Người đề nghị", created_by: "Người tạo",
    issued_by: "Người phát", issued_at: "Thời điểm phát", closed_at: "Đóng lúc", opened_at: "Mở lúc", opened_by: "Người mở ca",
    from_at: "Bắt đầu phụ trách", to_at: "Kết thúc phụ trách", cash_drawer: "Két tiền", duty: "Nhiệm vụ", handed_to: "Người nhận bàn giao",
    opening_cash: "Tiền đầu ca", expected_cash: "Tiền theo hệ thống", counted_cash: "Tiền kiểm đếm", difference: "Chênh lệch",
    amount: "Số tiền", reason: "Lý do", method: "Phương thức", tx_ref: "Mã giao dịch", parent_payment_id: "Giao dịch gốc", confirm_note: "Ghi chú đối chiếu",
    payment_id: "Giao dịch", approval_id: "Đề nghị phê duyệt", paid_at: "Thanh toán lúc", business_date: "Ngày kinh doanh", source: "Nguồn đơn",
    channel: "Kênh bán", is_compensation: "Đơn bù", parent_line_id: "Dòng combo cha", origin_line_id: "Dòng món gốc", line_kind: "Loại dòng",
    options_snapshot: "Lựa chọn đã chốt", remake_cause: "Nguyên nhân làm lại", remake_reason: "Lý do làm lại", priority: "Ưu tiên", handed_qty: "Số đã giao",
    accepted_at: "Nhận làm lúc", started_at: "Bắt đầu làm", ready_at: "Sẵn sàng lúc", ticket_no: "Số ticket", ticket_id: "Ticket", pager_id: "Thiết bị rung",
    supports_remote: "Điều khiển từ xa", reopened_count: "Số lần mở lại", returned_at: "Thu hồi lúc", receiver_id: "Người nhận", receiver_label: "Tên người nhận",
    result: "Kết quả", request_key: "Mã chống trùng", ingredient_id: "Nguyên liệu", base_unit: "Đơn vị cơ sở", min_stock: "Mức tồn tối thiểu",
    shelf_life_days: "Hạn dùng (ngày)", to_base_qty: "Hệ số quy đổi", unit_id: "Đơn vị", recipe_id: "Công thức", option_id: "Tùy chọn áp dụng",
    version: "Phiên bản", effective_from: "Áp dụng từ", lot_id: "Lô", lot_code: "Mã lô", expiry_date: "Hạn sử dụng", qty_remaining: "Tồn vật lý",
    reserved_qty: "Đang giữ chỗ", unit_cost: "Giá vốn đơn vị", qty_base: "Lượng theo đơn vị cơ sở", ref_type: "Loại nguồn", ref_id: "Mã nguồn",
    waste_cause: "Nguyên nhân hao hụt", operation_key: "Mã nghiệp vụ", seq_no: "Thứ tự phân bổ", document_id: "Chứng từ", counted_qty: "Lượng kiểm đếm",
    from_branch_id: "Chi nhánh cấp", to_branch_id: "Chi nhánh nhận", transfer_request_id: "Yêu cầu điều chuyển", needed_by: "Hạn cần hàng",
    supplier_id: "Nhà cung cấp", contact: "Liên hệ", tax_code: "Mã số thuế", payment_terms_days: "Hạn thanh toán (ngày)", purchase_order_id: "Đơn mua",
    purchase_order_line_id: "Dòng đơn mua", qty_ordered: "Số đặt", qty_received: "Số đã nhận", qty_rejected: "Số từ chối", accepted_qty: "Số chấp nhận",
    rejected_qty: "Số từ chối", total_amount: "Giá trị đơn mua", paid_amount: "Đã trả", ordered_at: "Đặt lúc", due_date: "Hạn thanh toán",
    category: "Nhóm phản hồi", resolution: "Cách giải quyết", handled_by: "Người xử lý", compensation_invoice_id: "Đơn bù", line_id: "Dòng món",
    role: "Vai trò", permissions: "Quyền bổ sung", discount_limit_pct: "Hạn mức giảm (%)", refund_limit: "Hạn mức hoàn tiền",
    mode: "Chế độ kết nối", endpoint: "Địa chỉ dịch vụ", secret_ref: "Tham chiếu kho bí mật", valid_from: "Hiệu lực từ", valid_to: "Hiệu lực đến",
    value: "Giá trị ưu đãi", min_total: "Giá trị tối thiểu", stackable: "Cho cộng dồn", max_uses: "Số lượt tối đa", used_count: "Số lượt đã dùng",
    campaign_key: "Mã chiến dịch", rules: "Điều kiện chương trình", group_no: "Nhóm lựa chọn", allow_swap: "Cho thay món", sort_order: "Thứ tự hiển thị",
    address: "Địa chỉ", brand_name: "Thương hiệu", multi_station: "Nhiều quầy", timezone: "Múi giờ", business_day_cutoff: "Giờ chốt ngày",
    layout_config: "Bố trí tầng & khu vực", branch_overrides: "Thiết lập theo chi nhánh", supplier_submission: "Chứng từ nhà cung cấp",
    action: "Thao tác", entity_type: "Đối tượng", entity_id: "Mã đối tượng", device_id: "Thiết bị", request_id: "Mã yêu cầu",
    dedupe_key: "Mã chống trùng", payload_hash: "Mã kiểm tra nội dung", attempts: "Số lần thử", available_at: "Lần xử lý tiếp", lease_until: "Giữ xử lý đến",
    staff_id: "Nhân viên tạo", partner: "Đối tác giao hàng", partner_order_id: "Mã đơn đối tác", driver_user_id: "Tài xế", origin_kind: "Nguồn thay đổi"
  };
  const statuses = {
    active: "Đang hoạt động", inactive: "Ngừng hoạt động", paused: "Tạm ngừng", seasonal: "Theo mùa", sold_out: "Hết món", archived: "Đã lưu trữ",
    draft: "Bản nháp", confirmed: "Đã xác nhận", partially_paid: "Thu một phần", paid: "Đã thanh toán", merged: "Đã ghép", cancelled: "Đã hủy", canceled: "Đã hủy", refunded: "Đã hoàn tiền",
    pending_accept: "Chờ tiếp nhận", waiting: "Chờ chế biến", in_progress: "Đang chế biến", ready: "Sẵn sàng", handed_over: "Đã bàn giao",
    open: "Đang mở", received: "Đã nhận", closed: "Đã đóng", issued: "Đã phát", calling: "Đang gọi", pending_return: "Chờ thu hồi", returned: "Đã thu hồi", faulty: "Bị lỗi", lost: "Bị mất",
    requested: "Đã đề nghị", approved: "Đã duyệt", rejected: "Từ chối", pending: "Chờ xử lý", succeeded: "Thành công", failed: "Thất bại", pending_reconcile: "Chờ đối soát",
    ordered: "Đã đặt", partially_received: "Nhận một phần", handling: "Đang xử lý", resolved: "Đã giải quyết", locked: "Đã khóa", retired: "Ngừng áp dụng", expired: "Hết hạn",
    preparing: "Đang chuẩn bị", packed: "Đã đóng gói", waiting_driver: "Chờ tài xế", handed_driver: "Đã giao tài xế", delivered: "Đã giao khách", in_transit: "Đang vận chuyển"
  };
  const valueLabels = {
    counter: "Tại quầy", table: "Tại bàn", qr: "Quét QR", kiosk: "Kiosk", takeaway: "Mang đi", delivery: "Giao hàng",
    staff: "Nhân viên", shift_lead: "Trưởng ca", owner: "Chủ quán", accountant: "Kế toán", sysadmin: "Quản trị hệ thống", supplier: "Nhà cung cấp", driver: "Tài xế",
    member: "Thành viên", guest: "Khách vãng lai", drink: "Đồ uống", food: "Đồ ăn", combo: "Combo", bar: "Pha chế", kitchen: "Bếp", pack: "Đóng gói",
    cash: "Tiền mặt", transfer: "Chuyển khoản", card: "Thẻ", wallet: "Ví điện tử", collect: "Thu tiền", refund: "Hoàn tiền",
    size: "Kích cỡ", sugar: "Mức đường", ice: "Mức đá", topping: "Topping", sale: "Món bán", combo_parent: "Combo", combo_component: "Thành phần combo", remake: "Làm lại"
  };
  const moduleTables = {
    orders: ["invoice", "invoice_line", "payment", "approval_request", "complaint"],
    tables: ["dining_table", "dining_session"], kitchen: ["invoice_line", "station", "recipe", "recipe_line"],
    tickets: ["ticket", "pager_device", "fulfillment_event"], inventory: ["ingredient", "ingredient_unit", "stock_lot", "stock_document", "stock_document_line", "stock_movement"],
    purchasing: ["purchase_order", "purchase_order_line", "supplier"], customers: ["customer", "point_ledger", "complaint", "promotion"],
    shifts: ["shift", "shift_assignment", "app_user", "cash_entry"], settings: ["branch", "station", "user_access", "integration_config", "async_operation", "audit_log"]
  };
  const immutable = new Set(["audit_log", "stock_movement", "point_ledger", "fulfillment_event", "invoice_line_ingredient"]);
  const managed = new Set(["id", "created_at", "updated_at", "password_hash", "pin_hash", "refresh_version", "failed_count", "locked_until", "actor_id", "created_by", "requested_by", "issued_by", "opened_by", "staff_id", "paid_total", "points_balance", "qty_remaining", "reserved_qty", "handed_qty", "used_count", "qty_received", "qty_rejected", "paid_amount", "approved_by", "approver_id", "balance_after", "paid_at", "decided_at", "returned_at", "closed_at", "accepted_by", "prepared_by", "ready_by", "started_at", "ready_at", "confirmed_at", "confirmed_by", "einvoice_data", "delivery_data", "is_compensation", "merged_into_invoice_id", "compensates_invoice_id"]);
  const draftStatuses = { invoice: "draft", invoice_line: "pending_accept", stock_document: "draft", purchase_order: "requested", recipe: "draft", payment: "pending", approval_request: "pending" };
  const foreignTables = {
    branch_id: "branch", from_branch_id: "branch", to_branch_id: "branch", item_id: "menu_item", combo_id: "menu_item", size_id: "menu_variant", variant_id: "menu_variant", option_id: "menu_variant",
    invoice_id: "invoice", customer_id: "customer", table_id: "dining_table", session_id: "dining_session", station_id: "station", shift_id: "shift", user_id: "app_user", supplier_id: "supplier", recipe_id: "recipe",
    ingredient_id: "ingredient", unit_id: "ingredient_unit", lot_id: "stock_lot", document_id: "stock_document", transfer_request_id: "stock_document", purchase_order_id: "purchase_order", purchase_order_line_id: "purchase_order_line",
    ticket_id: "ticket", pager_id: "pager_device", parent_payment_id: "payment", approval_id: "approval_request", origin_entry_id: "point_ledger", line_id: "invoice_line", parent_line_id: "invoice_line", origin_line_id: "invoice_line",
    receiver_id: "app_user", driver_user_id: "app_user", handed_to: "app_user", handled_by: "app_user", compensation_invoice_id: "invoice", created_shift_id: "shift", responsible_shift_id: "shift"
  };
  let csrf;
  let toastTimer;
  let selectedTable = document.body.dataset.resource;
  let records = [];
  let schema;
  let loading = false;
  let generation = 0;
  let productPage = 0;
  let products = [];
  let editorSubmit;
  let reportRows = [];
  let draftRequestId = crypto.randomUUID();
  const cart = new Map();
  const referenceLabels = new Map();

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }
  function showMessage(message, error = false) {
    const box = $("#page-message");
    if (!box) return;
    box.textContent = message;
    box.className = error ? "notice error" : "notice";
    box.hidden = false;
  }
  function toast(message) {
    const box = $("#toast");
    if (!box) return;
    clearTimeout(toastTimer);
    box.textContent = message;
    box.hidden = false;
    toastTimer = setTimeout(() => { box.hidden = true; }, 4000);
  }
  async function api(path, options = {}) {
    const headers = { Accept: "application/json", ...options.headers };
    if (options.body !== undefined) headers["Content-Type"] = "application/json";
    if (options.method && options.method !== "GET") {
      if (!csrf) csrf = await api("/api/v1/auth/csrf");
      headers[csrf.headerName] = csrf.token;
    }
    const response = await fetch(context + path, { credentials: "same-origin", ...options, headers });
    const body = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
      if (response.status === 401 && screen !== "login") location.assign(context + "/login");
      const detail = body?.errors ? " " + Object.values(body.errors).join(" ") : "";
      throw new Error((body?.message || "Không thể thực hiện yêu cầu. Mã " + response.status + ".") + detail);
    }
    return body;
  }
  function resourcePath(table) { return "/api/v1/" + table.replaceAll("_", "-"); }
  function badge(value) {
    const tone = ["cancelled", "canceled", "failed", "lost", "faulty", "locked"].includes(value) ? "danger"
      : ["pending", "waiting", "pending_accept", "in_progress", "requested", "calling"].includes(value) ? "warn"
        : ["draft", "inactive", "closed", "archived"].includes(value) ? "neutral" : "";
    return element("span", "badge " + tone, statuses[value] || value || "—");
  }
  function display(value, key = "") {
    if (value === null || value === undefined) return "—";
    if (typeof value === "boolean") return value ? "Có" : "Không";
    if (typeof value === "object") return "Chi tiết trong biểu mẫu";
    if (["gross", "refunds", "net"].includes(key)) return money(value);
    if (/(total|amount|price|cost|cash|difference|refund_limit)$/.test(key)) return money(value);
    if (/_at$/.test(key)) return new Date(value).toLocaleString("vi-VN");
    if (key === "status") return statuses[value] || value;
    if (foreignTables[key] && referenceLabels.has(foreignTables[key])) return referenceLabels.get(foreignTables[key]).get(String(value)) || String(value);
    if (["channel", "kind", "source", "account_kind", "method", "role", "station_kind", "line_kind"].includes(key)) return valueLabels[value] || value;
    return String(value);
  }
  function dataTable(rows, columns, actions) {
    if (!rows.length) return element("p", "empty-state", "Chưa có dữ liệu phù hợp.");
    const table = element("table");
    const header = element("tr");
    columns.forEach(key => header.append(element("th", "", labels[key] || key.replaceAll("_", " "))));
    if (actions) header.append(element("th", "", "Thao tác"));
    table.append(element("thead")); table.tHead.append(header);
    const body = element("tbody");
    rows.forEach(row => {
      const tr = element("tr");
      columns.forEach(key => {
        const td = element("td");
        if (key === "status") td.append(badge(row[key]));
        else td.textContent = display(row[key], key);
        td.title = display(row[key], key);
        tr.append(td);
      });
      if (actions) {
        const td = element("td");
        actions(row).forEach(action => td.append(action));
        tr.append(td);
      }
      body.append(tr);
    });
    table.append(body);
    return table;
  }
  function button(text, handler, className = "text-button") {
    const node = element("button", className, text);
    node.type = "button";
    node.addEventListener("click", handler);
    return node;
  }
  async function confirmAction(copy) {
    const dialog = $("#confirm-dialog");
    $("#confirm-copy").textContent = copy;
    return new Promise(resolve => {
      const finish = value => { dialog.close(); resolve(value); };
      $("#confirm-ok").onclick = () => finish(true);
      $("#confirm-cancel").onclick = () => finish(false);
      dialog.oncancel = () => resolve(false);
      dialog.showModal();
    });
  }
  async function login() {
    $("#toggle-password").addEventListener("click", () => {
      const field = $("#password");
      field.type = field.type === "password" ? "text" : "password";
      $("#toggle-password").textContent = field.type === "password" ? "Hiện" : "Ẩn";
    });
    $("#login-form").addEventListener("submit", async event => {
      event.preventDefault();
      const submit = event.submitter;
      submit.disabled = true; $("#login-error").hidden = true;
      try {
        const signedIn = await api("/api/v1/auth/login", { method: "POST", body: JSON.stringify({ username: $("#username").value.trim(), password: $("#password").value }) });
        $("#password").value = "";
        location.assign(context + (signedIn.roles.includes("customer") ? "/customer" : signedIn.canManageProducts ? "/app/overview" : "/app/qr-codes"));
      } catch (error) {
        $("#login-error").textContent = error.message;
        $("#login-error").hidden = false;
      } finally { submit.disabled = false; }
    });
  }
  async function overview() {
    const menu = await api("/api/v1/products?size=100&page=0&status=active");
    $("#stat-menu").textContent = menu.length;
    if (!manager) {
      $("#recent-orders").replaceChildren(element("p", "empty-state", "Danh sách đơn và báo cáo cần quyền quản trị. Bạn có thể xem thực đơn và soạn đơn tại quầy."));
      return;
    }
    const [orders, lines, tickets] = await Promise.all(["invoice", "invoice_line", "ticket"].map(t => api(resourcePath(t) + "?size=100")));
    $("#stat-orders").textContent = orders.length;
    $("#stat-prep").textContent = lines.filter(x => ["waiting", "pending_accept", "in_progress"].includes(x.status)).length;
    $("#stat-tickets").textContent = tickets.filter(x => x.status !== "closed").length;
    const recent = [...orders].sort((a, b) => String(b.created_at).localeCompare(String(a.created_at))).slice(0, 6);
    $("#recent-orders").replaceChildren(dataTable(recent, ["invoice_no", "channel", "total", "status"]));
  }
  function productFields(row) {
    const fields = [
      ["name", "Tên món", "text"], ["price", "Giá bán (₫)", "number"], ["categoryCode", "Mã danh mục", "text"], ["categoryName", "Tên danh mục", "text"],
      ["kind", "Loại món", ["drink", "food"]], ["stationKind", "Quầy phụ trách", ["bar", "kitchen", "pack"]],
      ["status", "Trạng thái", ["active", "paused", "seasonal", "sold_out", "archived"]], ["allergenNote", "Lưu ý dị ứng", "text"], ["description", "Mô tả", "textarea"]
    ];
    const nodes = [];
    fields.forEach(([key, label, type]) => {
      const field = element("div", "field" + (type === "textarea" ? " wide" : ""));
      const title = element("label", "", label); title.htmlFor = "f-" + key;
      let input;
      if (Array.isArray(type)) {
        input = element("select");
        type.forEach(value => { const option = element("option", "", statuses[value] || ({ drink: "Đồ uống", food: "Đồ ăn", bar: "Pha chế", kitchen: "Bếp", pack: "Đóng gói" })[value] || value); option.value = value; input.append(option); });
      } else {
        input = element(type === "textarea" ? "textarea" : "input");
        if (type !== "textarea") input.type = type;
        if (type === "number") { input.min = "0"; input.step = "1"; }
      }
      input.id = "f-" + key; input.name = key;
      input.value = row?.[key] ?? ({ kind: "drink", stationKind: "bar", status: "active" })[key] ?? "";
      input.required = !["description", "allergenNote"].includes(key);
      input.maxLength = ({ name: 150, categoryCode: 40, categoryName: 100, description: 500, allergenNote: 200 })[key] || 150;
      if (key === "categoryCode") input.pattern = "[A-Z0-9_]{1,40}";
      field.append(title, input); nodes.push(field);
    });
    return nodes;
  }
  function openEditor(title, nodes, onSubmit, readOnly = false) {
    $("#editor-title").textContent = title;
    $("#editor-fields").replaceChildren(...nodes);
    $("#editor-error").hidden = true;
    $("#editor-form button[type=submit]").hidden = readOnly;
    editorSubmit = onSubmit;
    $("#editor-dialog").showModal();
  }
  function editProduct(row) {
    openEditor(row ? "Chỉnh sửa món" : "Thêm món mới", productFields(row), async form => {
      const payload = Object.fromEntries(new FormData(form));
      payload.price = Number(payload.price);
      if (row) payload.expectedUpdatedAt = row.updatedAt;
      await api("/api/v1/products" + (row ? "/" + row.id : ""), {
        method: row ? "PATCH" : "POST",
        headers: row && row.updatedAt ? { "X-Expected-Updated-At": row.updatedAt } : {},
        body: JSON.stringify(payload)
      });
      await loadProducts();
    });
  }
  function productCard(product, index) {
    const card = element("article", "product-card");
    const art = element("div", "product-art tone-" + index % 4);
    art.append(element("div", "drink-drawing")); art.append(badge(product.status));
    const info = element("div", "product-info");
    info.append(element("p", "category", product.categoryName || "Thực đơn"), element("h3", "", product.name), element("p", "", product.description || ""));
    const bottom = element("div", "product-bottom");
    bottom.append(element("span", "price", product.price == null ? "Chưa có giá" : money(product.price)));
    const actions = element("div", "product-actions");
    if (screen === "pos") {
      const add = button("+ Thêm", () => addToCart(product), "button primary");
      add.disabled = product.price == null || product.status !== "active"; actions.append(add);
    } else if (manager) {
      actions.append(button("Sửa", () => editProduct(product)));
      if (product.status !== "archived") actions.append(button("Lưu trữ", async () => {
        if (await confirmAction("Lưu trữ món “" + product.name + "”? Món sẽ ngừng hiển thị trong danh sách bán.")) {
          try { await api("/api/v1/products/" + product.id, { method: "DELETE" }); await loadProducts(); toast("Đã lưu trữ món."); }
          catch (error) { showMessage(error.message, true); }
        }
      }));
    }
    bottom.append(actions); info.append(bottom); card.append(art, info);
    return card;
  }
  async function loadProducts() {
    const query = new URLSearchParams({ q: $("#product-search").value, page: productPage, size: screen === "pos" ? 60 : 12, status: screen === "pos" ? "active" : $("#product-status").value });
    products = await api("/api/v1/products?" + query);
    $("#product-grid").replaceChildren(...(products.length ? products.map(productCard) : [element("p", "empty-state", "Chưa có món phù hợp.")]));
    if ($("#products-page")) $("#products-page").textContent = "Trang " + (productPage + 1);
    if ($("#products-prev")) $("#products-prev").disabled = productPage === 0;
    $("#products-next").disabled = products.length < (screen === "pos" ? 60 : 12);
  }
  async function setupProducts() {
    let timer;
    $("#product-search").addEventListener("input", () => { clearTimeout(timer); timer = setTimeout(() => { productPage = 0; loadProducts().catch(e => showMessage(e.message, true)); }, 300); });
    $("#product-status")?.addEventListener("change", () => { productPage = 0; loadProducts().catch(e => showMessage(e.message, true)); });
    $("#products-next").addEventListener("click", () => { productPage++; loadProducts().catch(e => showMessage(e.message, true)); });
    $("#products-prev")?.addEventListener("click", () => { productPage = Math.max(0, productPage - 1); loadProducts().catch(e => showMessage(e.message, true)); });
    $("#add-product")?.addEventListener("click", () => editProduct(null));
    await loadProducts();
  }
  function addToCart(product) {
    const entry = cart.get(product.id) || { product, quantity: 0 };
    if (entry.quantity >= 99) return;
    entry.quantity++; cart.set(product.id, entry); draftRequestId = crypto.randomUUID(); renderCart();
  }
  function renderCart() {
    const nodes = [];
    let total = 0;
    cart.forEach((entry, id) => {
      const row = element("div", "cart-item");
      const info = element("div"); info.append(element("strong", "", entry.product.name), element("small", "", money(entry.product.price)));
      const controls = element("div", "quantity-controls");
      controls.append(button("−", () => { entry.quantity--; if (!entry.quantity) cart.delete(id); draftRequestId = crypto.randomUUID(); renderCart(); }), element("span", "", entry.quantity), button("+", () => addToCart(entry.product)));
      row.append(info, controls); nodes.push(row); total += Number(entry.product.price) * entry.quantity;
    });
    $("#cart-items").replaceChildren(...(nodes.length ? nodes : [element("p", "empty-state", "Chọn một món để bắt đầu.")]));
    $("#cart-total").textContent = money(total);
    $("#print-draft").disabled = cart.size === 0;
    $("#save-draft").disabled = !manager || cart.size === 0 || !$("#pos-branch").value;
  }
  async function setupPos() {
    if (manager) {
      const branches = await api(resourcePath("branch") + "?size=100");
      branches.filter(branch => branch.status === "active").forEach(branch => $("#pos-branch").append(new Option(branch.name, branch.id)));
      if (!$("#pos-branch").options.length) showMessage("Chưa có chi nhánh hoạt động. Vào Thiết lập để khai báo chi nhánh trước.");
    } else $("#pos-branch").disabled = true;
    $("#pos-branch").addEventListener("change", () => { draftRequestId = crypto.randomUUID(); renderCart(); });
    $("#order-note").addEventListener("input", () => { draftRequestId = crypto.randomUUID(); });
    $("#save-draft").addEventListener("click", async () => {
      const layout = $(".pos-layout");
      layout.inert = true;
      try {
        const order = await api("/api/v1/orders/drafts", {
          method: "POST", body: JSON.stringify({
            requestId: draftRequestId, branchId: $("#pos-branch").value,
            lines: [...cart.values()].map(entry => ({ itemId: entry.product.id, quantity: entry.quantity })), note: $("#order-note").value
          })
        });
        toast("Đã lưu " + order.invoice_no + " · " + money(order.total));
        cart.clear(); $("#order-note").value = ""; draftRequestId = crypto.randomUUID(); renderCart();
      } catch (error) { showMessage(error.message, true); }
      finally { layout.inert = false; }
    });
    renderCart();
  }
  function columnsFor(rows) {
    const preferences = {
      invoice: ["invoice_no", "business_date", "channel", "total", "paid_total", "status"],
      invoice_line: ["item_id", "quantity", "unit_price", "handed_qty", "status", "created_at"],
      ticket: ["ticket_no", "business_date", "pager_id", "status", "issued_at", "returned_at"],
      dining_table: ["code", "zone_code", "seats", "status"], pager_device: ["code", "status", "supports_remote"],
      customer: ["member_code", "full_name", "phone", "tier", "points_balance", "status"],
      ingredient: ["name", "base_unit", "min_stock", "shelf_life_days", "status"],
      stock_lot: ["lot_code", "ingredient_id", "expiry_date", "qty_remaining", "reserved_qty"],
      app_user: ["username", "full_name", "account_kind", "status"], shift: ["opened_at", "opening_cash", "counted_cash", "difference", "status"],
      audit_log: ["action", "entity_type", "actor_id", "created_at"], payment: ["tx_ref", "kind", "method", "amount", "status"],
      branch: ["code", "name", "address", "status"], purchase_order: ["supplier_id", "ordered_at", "total_amount", "due_date", "status"]
    };
    return preferences[selectedTable] || Object.keys(rows[0] || {}).filter(k => !["id", "updated_at"].includes(k)).slice(0, 6);
  }
  function renderRecords() {
    const query = $("#record-search").value.toLocaleLowerCase("vi");
    const filtered = records.filter(row => Object.values(row).some(v => typeof v !== "object" && String(v ?? "").toLocaleLowerCase("vi").includes(query)));
    $("#record-list").replaceChildren(dataTable(filtered, columnsFor(records), row => {
      const actions = [button("Chi tiết", () => editRecord(row, true))];
      if (!immutable.has(selectedTable)) {
        actions.push(button("Sửa", () => editRecord(row, false)));
        actions.push(button("Ngừng / xóa", () => deleteRecord(row)));
      }
      return actions;
    }));
    $("#record-count").textContent = "Hiển thị " + filtered.length + " / " + records.length + " bản ghi đã tải";
  }
  async function loadRecords(append = false) {
    if (loading) return;
    loading = true;
    const version = ++generation;
    const table = selectedTable;
    try {
      const after = append && records.length ? "&after=" + records[records.length - 1].id : "";
      const [rows, meta] = await Promise.all([api(resourcePath(table) + "?size=50" + after), api(resourcePath(table) + "/schema")]);
      if (version !== generation || table !== selectedTable) return;
      records = append ? [...records, ...rows] : rows; schema = meta;
      const references = [...new Set(columnsFor(records).map(key => foreignTables[key]).filter(Boolean))];
      await Promise.all(references.map(async reference => {
        const options = await api(resourcePath(reference) + "?size=100");
        referenceLabels.set(reference, new Map(options.map(option => [option.id, option.name || option.full_name || option.invoice_no || option.code || option.ticket_no || option.member_code || option.id])));
      }));
      renderRecords();
      $("#records-next").disabled = rows.length < 50;
      $("#add-record").disabled = selectedTable === "invoice_line_ingredient" || selectedTable === "audit_log";
      $("#resource-help").textContent = immutable.has(selectedTable)
        ? "Lịch sử được giữ nguyên sau khi ghi nhận. Dữ liệu sửa sai cần một bút toán hoặc sự kiện bổ sung."
        : "Có thể chỉnh sửa dữ liệu được phép. Đơn và chứng từ đã chốt được bảo vệ; các thao tác hủy, duyệt và hoàn tiền cần đúng quy trình.";
    } finally { loading = false; }
  }
  function enumValues(column) {
    const check = schema.checks.find(text => text.includes("(" + column.name + ")") || text.includes("(" + column.name + " ") || text.startsWith("CHECK ((" + column.name + " "));
    return check?.includes("ARRAY[") ? [...check.matchAll(/'([^']+)'/g)].map(m => m[1]) : [];
  }
  async function recordFields(row, readOnly) {
    const nodes = [];
    const fields = schema.columns.filter(c => readOnly ? Object.hasOwn(row, c.name) : !managed.has(c.name));
    if (!readOnly && selectedTable === "app_user") fields.push({ name: "password", type: "password", nullable: Boolean(row), hasDefault: Boolean(row), maxLength: 72 });
    for (const column of fields) {
      const name = column.name;
      if (!readOnly && row && name === "status") continue;
      const wide = ["jsonb", "text", "_uuid"].includes(column.type);
      const field = element("div", "field" + (wide ? " wide" : ""));
      const title = element("label", "", labels[name] || name.replaceAll("_", " ")); title.htmlFor = "f-" + name;
      const enums = enumValues(column);
      let input;
      if (!readOnly && foreignTables[name]) {
        input = element("select"); input.append(new Option("Chọn…", ""));
        try {
          const options = await api(resourcePath(foreignTables[name]) + "?size=100");
          options.forEach(option => input.append(new Option(option.name || option.full_name || option.invoice_no || option.code || option.ticket_no || option.member_code || option.id, option.id)));
          if (row?.[name] && !options.some(option => option.id === row[name])) input.append(new Option(row[name], row[name]));
        } catch (_) { input.append(new Option("Không tải được danh sách lựa chọn", "")); }
      } else if (!readOnly && (enums.length || column.type === "bool")) {
        input = element("select");
        const values = column.type === "bool" ? ["true", "false"] : enums;
        values.forEach(value => input.append(new Option(column.type === "bool" ? (value === "true" ? "Có" : "Không") : statuses[value] || valueLabels[value] || value, value)));
      } else {
        input = element(wide ? "textarea" : "input");
        if (!wide) input.type = ({ int4: "number", int8: "number", numeric: "number", date: "date", time: "time", password: "password", timestamptz: "text" })[column.type] || "text";
        if (["int4", "int8"].includes(column.type)) input.step = "1";
        if (column.type === "numeric") input.step = column.scale === 0 ? "1" : "any";
        if (column.maxLength) input.maxLength = column.maxLength;
        if (wide) input.rows = 5;
      }
      input.id = "f-" + name; input.name = name; input.dataset.columnType = column.type;
      const value = row?.[name];
      input.value = value == null ? "" : typeof value === "object" ? JSON.stringify(value, null, 2) : String(value);
      if (readOnly) { input.disabled = true; field.append(title, input); nodes.push(field); continue; }
      const optional = row ? value == null : column.nullable || column.hasDefault;
      const toggle = element("input"); toggle.type = "checkbox"; toggle.checked = !optional;
      if (row && value != null) toggle.checked = true;
      input.disabled = !toggle.checked;
      input.required = !column.nullable && !column.hasDefault;
      if (draftStatuses[selectedTable] && name === "status") { input.value = draftStatuses[selectedTable]; input.disabled = true; input.dataset.omit = "true"; }
      field.append(title, input);
      if (optional) {
        const label = element("label", "optional-control"); label.append(toggle, document.createTextNode("Nhập giá trị cho mục này"));
        toggle.addEventListener("change", () => { input.disabled = !toggle.checked; }); field.append(label);
      }
      if (column.type === "timestamptz") field.append(element("small", "", "Thời gian có múi giờ, ví dụ 2026-11-26T08:00:00+07:00."));
      if (foreignTables[name]) field.append(element("small", "", "Danh sách tối đa 100 mục. Dữ liệu phải thuộc đúng chi nhánh và đối tượng liên quan."));
      if (wide) field.append(element("small", "", "Dữ liệu cấu hình có cấu trúc; giữ schema_version khi được yêu cầu."));
      nodes.push(field);
    }
    return nodes;
  }
  async function editRecord(row, readOnly = false) {
    try {
      const table = selectedTable;
      const fresh = row ? await api(resourcePath(table) + "/" + row.id) : null;
      const nodes = await recordFields(fresh, readOnly);
      openEditor((readOnly ? "Chi tiết · " : row ? "Chỉnh sửa · " : "Thêm · ") + tableNames[table], nodes, async form => {
        const body = {};
        for (const input of form.querySelectorAll("[data-column-type]")) {
          if (input.disabled || input.dataset.omit) continue;
          const type = input.dataset.columnType;
          let value = input.value;
          if (value === "" && schema.columns.find(c => c.name === input.name)?.nullable) value = null;
          else if (["jsonb", "_uuid"].includes(type)) value = JSON.parse(value);
          else if (type === "bool") value = value === "true";
          // Keep decimal strings intact so the backend can validate exact precision.
          if (fresh && JSON.stringify(value) === JSON.stringify(fresh[input.name])) continue;
          if (input.name === "password" && !value) continue;
          body[input.name] = value;
        }
        if (!Object.keys(body).length) throw new Error("Chưa có thay đổi để lưu.");
        await api(resourcePath(table) + (fresh ? "/" + fresh.id : ""), {
          method: fresh ? "PATCH" : "POST", body: JSON.stringify(body),
          headers: fresh ? { "X-Expected-Updated-At": fresh.updated_at || fresh.created_at } : {}
        });
        await loadRecords();
      }, readOnly);
    } catch (error) { showMessage(error.message, true); }
  }
  async function deleteRecord(row) {
    if (!await confirmAction("Ngừng hoặc xóa bản ghi này? Dữ liệu đã có giao dịch có thể được lưu trữ hoặc bị từ chối theo quy tắc nghiệp vụ.")) return;
    try {
      await api(resourcePath(selectedTable) + "/" + row.id, { method: "DELETE", headers: { "X-Expected-Updated-At": row.updated_at || row.created_at } });
      await loadRecords(); toast("Đã xử lý bản ghi.");
    } catch (error) { showMessage(error.message, true); }
  }
  async function setupRecords() {
    for (const option of $("#resource-select").options) option.textContent = tableNames[option.value] || option.value;
    (moduleTables[screen] || []).forEach(table => {
      const link = element("a", table === selectedTable ? "active" : "", tableNames[table]);
      link.href = context + "/app/data/" + table;
      $("#module-shortcuts").append(link);
    });
    $("#resource-select").addEventListener("change", event => location.assign(context + "/app/data/" + event.target.value));
    $("#record-search").addEventListener("input", renderRecords);
    $("#add-record").addEventListener("click", () => editRecord(null));
    $("#records-next").addEventListener("click", () => loadRecords(true).catch(e => showMessage(e.message, true)));
    if (!manager) {
      $("#record-list").replaceChildren(element("p", "empty-state", "Màn hình quản lý này cần tài khoản owner hoặc quản trị toàn dự án. Liên hệ quản lý để được phân công phù hợp."));
      $("#add-record").disabled = true; $("#records-next").disabled = true; return;
    }
    await loadRecords();
  }
  async function loadReport() {
    const from = $("#report-from").value, to = $("#report-to").value;
    const data = await api("/api/v1/reports/revenue?" + new URLSearchParams({ from, to }));
    reportRows = data.daily;
    const totals = reportRows.reduce((acc, row) => {
      acc.orders += Number(row.orders || 0);
      ["gross", "refunds", "net"].forEach(key => { acc[key] += BigInt(String(row[key] || "0").split(".")[0]); });
      return acc;
    }, { orders: 0, gross: 0n, refunds: 0n, net: 0n });
    ["orders", "gross", "refunds", "net"].forEach(key => { $("#report-" + key).textContent = key === "orders" ? totals[key] : money(totals[key]); });
    $("#report-description").textContent = data.basis;
    const chart = $("#report-chart"); chart.replaceChildren();
    const maximum = Math.max(1, ...reportRows.map(row => Math.abs(Number(row.net))));
    reportRows.forEach(row => {
      const item = element("div", "bar-item"); const bar = element("div", "bar");
      bar.style.height = Math.max(2, Math.abs(Number(row.net)) / maximum * 125) + "px";
      if (Number(row.net) < 0) bar.style.background = "#a23d39";
      item.append(element("strong", "", money(row.net)), bar, element("small", "", String(row.day).slice(5))); chart.append(item);
    });
    if (!reportRows.length) chart.append(element("p", "empty-state", "Chưa có hóa đơn đủ điều kiện trong kỳ này."));
    $("#report-table").replaceChildren(dataTable(reportRows, ["day", "orders", "gross", "refunds", "net"]));
    $("#report-export").disabled = !reportRows.length;
  }
  function exportReport() {
    // Fixed headers and numeric/date values keep spreadsheet formulas out of CSV cells.
    const rows = [["Ngày", "Số đơn", "Giá trị bán", "Hoàn tiền", "Doanh thu thuần"], ...reportRows.map(row => [row.day, row.orders, row.gross, row.refunds, row.net])];
    const csv = "\uFEFF" + rows.map(row => row.map(value => '"' + String(value).replaceAll('"', '""') + '"').join(",")).join("\r\n");
    const link = element("a"); link.href = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" })); link.download = "PolyCafe-doanh-thu.csv"; link.click(); URL.revokeObjectURL(link.href);
  }
  async function start() {
    if (screen === "login") { await login(); return; }
    $("#today").textContent = new Intl.DateTimeFormat("vi-VN", { weekday: "short", day: "2-digit", month: "2-digit", year: "numeric" }).format(new Date());
    $("#menu-toggle").addEventListener("click", event => {
      const open = $("#sidebar").classList.toggle("open"); event.currentTarget.setAttribute("aria-expanded", String(open));
    });
    $("#logout").addEventListener("click", async () => {
      try { await api("/api/v1/auth/logout", { method: "POST" }); location.assign(context + "/login"); }
      catch (error) { showMessage(error.message, true); }
    });
    $("#refresh-page").addEventListener("click", () => location.reload());
    document.querySelectorAll(".close-dialog").forEach(node => node.addEventListener("click", () => $("#editor-dialog").close()));
    $("#editor-form").addEventListener("submit", async event => {
      event.preventDefault();
      const submit = event.submitter; submit.disabled = true; $("#editor-error").hidden = true;
      try { await editorSubmit(event.currentTarget); $("#editor-dialog").close(); toast("Đã lưu thay đổi."); }
      catch (error) { $("#editor-error").textContent = error.message; $("#editor-error").hidden = false; }
      finally { submit.disabled = false; }
    });
    if (screen === "overview") await overview();
    else if (["menu", "pos"].includes(screen)) {
      await setupProducts();
      if (screen === "pos") {
        await setupPos(); $("#print-draft").addEventListener("click", () => window.print());
        $("#clear-cart").addEventListener("click", async () => { if (await confirmAction("Xóa danh sách món đang soạn trong tab này?")) { cart.clear(); draftRequestId = crypto.randomUUID(); renderCart(); } });
      }
    } else if (screen === "reports") {
      const now = new Date(), from = new Date(now); from.setDate(from.getDate() - 6);
      const date = value => value.getFullYear() + "-" + String(value.getMonth() + 1).padStart(2, "0") + "-" + String(value.getDate()).padStart(2, "0");
      $("#report-from").value = date(from); $("#report-to").value = date(now);
      $("#report-form").addEventListener("submit", event => { event.preventDefault(); loadReport().catch(e => showMessage(e.message, true)); });
      $("#report-export").addEventListener("click", exportReport);
      if (manager) await loadReport(); else showMessage("Báo cáo tổng hợp cần quyền quản trị toàn dự án.");
    } else await setupRecords();
  }
  start().catch(error => showMessage(error.message, true));
})();
