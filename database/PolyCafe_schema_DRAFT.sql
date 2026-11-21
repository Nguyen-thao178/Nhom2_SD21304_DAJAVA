-- PolyCafe SRS 2.0 — DRAFT. Not applied to any hosted project.

-- Backend Spring Boot owns authorization. Public Data API has no grants.

-- Inspect existing objects and test on PostgreSQL before applying.

BEGIN;

SET LOCAL lock_timeout = '5s';

SET LOCAL statement_timeout = '60s';

CREATE TABLE public.branch (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "code" varchar(20) NOT NULL,
  "name" varchar(200) NOT NULL,
  "address" varchar(300) NOT NULL,
  "multi_station" boolean NOT NULL DEFAULT false,
  "timezone" varchar(60) NOT NULL,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'inactive')),
  "brand_name" varchar(200) NOT NULL,
  "business_day_cutoff" time NOT NULL,
  "layout_config" jsonb NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.branch ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.branch FROM anon, authenticated;

CREATE TABLE public.dining_table (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "code" varchar(20) NOT NULL,
  "seats" int NOT NULL CHECK ("seats" > 0),
  "qr_token" varchar(64) NOT NULL UNIQUE,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('free', 'occupied', 'reserved', 'inactive')),
  "zone_code" varchar(40) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.dining_table ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.dining_table FROM anon, authenticated;

CREATE TABLE public.station (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "name" varchar(80) NOT NULL,
  "kind" varchar(20) NOT NULL CHECK ("kind" IN ('bar', 'kitchen', 'pack', 'other')),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'inactive')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.station ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.station FROM anon, authenticated;

CREATE TABLE public.app_user (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "username" varchar(60) NOT NULL UNIQUE,
  "full_name" varchar(150) NOT NULL,
  "password_hash" varchar(255) NOT NULL,
  "pin_hash" varchar(255),
  "supplier_id" uuid,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'locked')),
  "failed_count" int NOT NULL DEFAULT 0,
  "locked_until" timestamptz,
  "account_kind" varchar(20) NOT NULL,
  "refresh_version" integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.app_user ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.app_user FROM anon, authenticated;

CREATE TABLE public.menu_item (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "name" varchar(150) NOT NULL,
  "description" varchar(500),
  "station_kind" varchar(20) NOT NULL CHECK ("station_kind" IN ('bar', 'kitchen', 'pack')),
  "allergen_note" varchar(200),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'paused', 'seasonal', 'sold_out', 'archived')),
  "category_code" varchar(40) NOT NULL,
  "category_name" varchar(100) NOT NULL,
  "kind" varchar(10) NOT NULL,
  "valid_from" timestamptz,
  "valid_to" timestamptz,
  "branch_overrides" jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.menu_item ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.menu_item FROM anon, authenticated;

CREATE TABLE public.price_rule (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid,
  "channel" varchar(20) CHECK ("channel" IN ('counter', 'table', 'qr', 'kiosk', 'takeaway', 'delivery')),
  "price" numeric(14,0) NOT NULL CHECK ("price" >= 0),
  "valid_from" timestamptz NOT NULL,
  "valid_to" timestamptz,
  "item_id" uuid,
  "variant_id" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.price_rule ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.price_rule FROM anon, authenticated;

CREATE TABLE public.combo_item (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "combo_id" uuid NOT NULL,
  "item_id" uuid NOT NULL,
  "size_id" uuid,
  "quantity" int NOT NULL CHECK ("quantity" > 0),
  "group_no" int NOT NULL DEFAULT 0,
  "allow_swap" boolean NOT NULL DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.combo_item ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.combo_item FROM anon, authenticated;

CREATE TABLE public.promotion (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "name" varchar(150) NOT NULL,
  "kind" varchar(20) NOT NULL CHECK ("kind" IN ('percent', 'amount', 'gift', 'bxgy')),
  "value" numeric(14,2) NOT NULL,
  "min_total" numeric(14,0),
  "branch_id" uuid,
  "channel" varchar(20),
  "priority" int NOT NULL,
  "stackable" boolean NOT NULL DEFAULT false,
  "code" varchar(40) UNIQUE,
  "max_uses" int,
  "used_count" int NOT NULL DEFAULT 0,
  "valid_from" timestamptz NOT NULL,
  "valid_to" timestamptz,
  "rules" jsonb NOT NULL,
  "campaign_key" varchar(40),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.promotion ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.promotion FROM anon, authenticated;

CREATE TABLE public.customer (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "member_code" varchar(20) NOT NULL UNIQUE,
  "kind" varchar(10) NOT NULL CHECK ("kind" IN ('member', 'guest')),
  "full_name" varchar(150),
  "phone" varchar(20) UNIQUE,
  "email" varchar(254) UNIQUE,
  "tier" varchar(20),
  "points_balance" int NOT NULL DEFAULT 0,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'anonymized')),
  "user_id" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.customer ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.customer FROM anon, authenticated;

CREATE TABLE public.point_ledger (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "customer_id" uuid NOT NULL,
  "invoice_id" uuid,
  "kind" varchar(10) NOT NULL CHECK ("kind" IN ('earn', 'redeem', 'expire', 'reverse', 'adjust')),
  "points" int NOT NULL,
  "balance_after" int NOT NULL,
  "expires_at" timestamptz,
  "origin_entry_id" uuid,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.point_ledger ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.point_ledger FROM anon, authenticated;

CREATE TABLE public.invoice (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "invoice_no" varchar(20) NOT NULL,
  "table_id" uuid,
  "source" varchar(10) NOT NULL CHECK ("source" IN ('staff', 'qr', 'kiosk')),
  "staff_id" uuid,
  "customer_id" uuid NOT NULL,
  "channel" varchar(20) NOT NULL CHECK ("channel" IN ('counter', 'table', 'qr', 'kiosk', 'takeaway', 'delivery')),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('draft', 'confirmed', 'partially_paid', 'paid', 'merged', 'cancelled', 'refunded')),
  "subtotal" numeric(14,0) NOT NULL CHECK ("subtotal" >= 0),
  "discount_total" numeric(14,0) NOT NULL CHECK ("discount_total" >= 0),
  "total" numeric(14,0) NOT NULL CHECK ("total" >= 0),
  "paid_total" numeric(14,0) NOT NULL CHECK ("paid_total" >= 0),
  "is_compensation" boolean NOT NULL DEFAULT false,
  "compensates_invoice_id" uuid,
  "merged_into_invoice_id" uuid,
  "split_from_invoice_id" uuid,
  "idempotency_key" varchar(64) NOT NULL UNIQUE,
  "confirmed_by" uuid,
  "confirmed_at" timestamptz,
  "business_date" date NOT NULL,
  "session_id" uuid,
  "created_shift_id" uuid,
  "responsible_shift_id" uuid,
  "driver_user_id" uuid,
  "partner" varchar(50),
  "partner_order_id" varchar(80),
  "delivery_data" jsonb,
  "einvoice_data" jsonb,
  "discount_snapshot" jsonb NOT NULL,
  "version" integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.invoice ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.invoice FROM anon, authenticated;

CREATE TABLE public.invoice_line (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "invoice_id" uuid NOT NULL,
  "item_id" uuid,
  "size_id" uuid,
  "combo_id" uuid,
  "quantity" int NOT NULL CHECK ("quantity" > 0),
  "unit_price" numeric(14,0) NOT NULL CHECK ("unit_price" >= 0),
  "options_snapshot" jsonb NOT NULL,
  "note" varchar(300),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('pending_accept', 'waiting', 'in_progress', 'ready', 'handed_over', 'canceled')),
  "origin_line_id" uuid,
  "station_id" uuid,
  "accepted_by" uuid,
  "accepted_at" timestamptz,
  "started_at" timestamptz,
  "ready_at" timestamptz,
  "priority" int NOT NULL DEFAULT 0,
  "remake_cause" varchar(20) CHECK ("remake_cause" IN ('quality', 'mistake', 'complaint', 'other')),
  "remake_reason" varchar(300),
  "parent_line_id" uuid,
  "recipe_id" uuid,
  "line_kind" varchar(20) NOT NULL,
  "origin_kind" varchar(20),
  "handed_qty" integer NOT NULL DEFAULT 0,
  "prepared_by" uuid,
  "ready_by" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.invoice_line ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.invoice_line FROM anon, authenticated;

CREATE TABLE public.ticket (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "invoice_id" uuid NOT NULL,
  "ticket_no" varchar(20) NOT NULL,
  "pager_id" uuid,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('open', 'ready', 'received', 'closed')),
  "issued_by" uuid NOT NULL,
  "closed_at" timestamptz,
  "reopened_count" int NOT NULL DEFAULT 0,
  "business_date" date NOT NULL,
  "issued_at" timestamptz NOT NULL,
  "returned_at" timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.ticket ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.ticket FROM anon, authenticated;

CREATE TABLE public.pager_device (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "code" varchar(20) NOT NULL,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('ready', 'issued', 'calling', 'pending_return', 'returned', 'faulty', 'lost')),
  "supports_remote" boolean NOT NULL DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.pager_device ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.pager_device FROM anon, authenticated;

CREATE TABLE public.payment (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "shift_id" uuid,
  "method" varchar(20) NOT NULL CHECK ("method" IN ('cash', 'transfer', 'card', 'wallet')),
  "amount" numeric(14,0) NOT NULL CHECK ("amount" > 0),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('pending', 'succeeded', 'failed', 'pending_reconcile')),
  "tx_ref" varchar(80) NOT NULL UNIQUE,
  "confirmed_by" uuid,
  "confirm_note" varchar(200),
  "paid_at" timestamptz,
  "kind" varchar(20) NOT NULL,
  "parent_payment_id" uuid,
  "purchase_order_id" uuid,
  "approval_id" uuid,
  "requested_by" uuid,
  "reason" varchar(300),
  "reconcile_batch_key" varchar(80),
  "reconciled_at" timestamptz,
  "invoice_id" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.payment ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.payment FROM anon, authenticated;

CREATE TABLE public.shift (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "opened_by" uuid NOT NULL,
  "opened_at" timestamptz NOT NULL,
  "closed_at" timestamptz,
  "opening_cash" numeric(14,0) NOT NULL CHECK ("opening_cash" >= 0),
  "expected_cash" numeric(14,0),
  "counted_cash" numeric(14,0),
  "difference" numeric(14,0),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('open', 'closed', 'handed_over')),
  "handed_to" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.shift ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.shift FROM anon, authenticated;

CREATE TABLE public.shift_assignment (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "shift_id" uuid NOT NULL,
  "user_id" uuid NOT NULL,
  "station_id" uuid,
  "cash_drawer" varchar(20),
  "from_at" timestamptz NOT NULL,
  "to_at" timestamptz,
  "zone_code" varchar(40),
  "duty" varchar(30) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.shift_assignment ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.shift_assignment FROM anon, authenticated;

CREATE TABLE public.cash_entry (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "shift_id" uuid NOT NULL,
  "kind" varchar(10) NOT NULL CHECK ("kind" IN ('in', 'out')),
  "amount" numeric(14,0) NOT NULL CHECK ("amount" > 0),
  "reason" varchar(200) NOT NULL,
  "created_by" uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.cash_entry ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.cash_entry FROM anon, authenticated;

CREATE TABLE public.ingredient (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "name" varchar(150) NOT NULL,
  "base_unit" varchar(20) NOT NULL,
  "min_stock" numeric(14,4),
  "shelf_life_days" int,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'inactive')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.ingredient ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.ingredient FROM anon, authenticated;

CREATE TABLE public.ingredient_unit (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "ingredient_id" uuid NOT NULL,
  "name" varchar(30) NOT NULL,
  "to_base_qty" numeric(14,4) NOT NULL CHECK ("to_base_qty" > 0),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.ingredient_unit ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.ingredient_unit FROM anon, authenticated;

CREATE TABLE public.recipe (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "item_id" uuid NOT NULL,
  "size_id" uuid,
  "version" int NOT NULL,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('draft', 'active', 'retired')),
  "effective_from" timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.recipe ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.recipe FROM anon, authenticated;

CREATE TABLE public.recipe_line (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "recipe_id" uuid NOT NULL,
  "ingredient_id" uuid NOT NULL,
  "quantity" numeric(14,4) NOT NULL CHECK ("quantity" > 0),
  "unit_id" uuid NOT NULL,
  "option_id" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.recipe_line ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.recipe_line FROM anon, authenticated;

CREATE TABLE public.stock_lot (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "ingredient_id" uuid NOT NULL,
  "lot_code" varchar(40) NOT NULL,
  "expiry_date" date,
  "qty_remaining" numeric(14,4) NOT NULL CHECK ("qty_remaining" >= 0),
  "unit_cost" numeric(14,4),
  "reserved_qty" numeric(14,4) NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.stock_lot ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.stock_lot FROM anon, authenticated;

CREATE TABLE public.stock_movement (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "ingredient_id" uuid NOT NULL,
  "lot_id" uuid,
  "kind" varchar(20) NOT NULL CHECK ("kind" IN ('receipt', 'issue', 'consume', 'waste', 'transfer_out', 'transfer_in', 'adjust', 'reserve', 'release')),
  "qty_base" numeric(14,4) NOT NULL,
  "waste_cause" varchar(20),
  "ref_type" varchar(30) NOT NULL,
  "ref_id" uuid NOT NULL,
  "created_by" uuid,
  "operation_key" varchar(100) NOT NULL,
  "seq_no" integer NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.stock_movement ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.stock_movement FROM anon, authenticated;

CREATE TABLE public.stock_document (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "kind" varchar(20) NOT NULL CHECK ("kind" IN ('receipt', 'issue', 'transfer', 'stocktake', 'waste', 'adjust', 'return', 'transfer_request')),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('draft', 'approved', 'cancelled', 'requested', 'in_transit', 'partial', 'received', 'closed')),
  "purchase_order_id" uuid,
  "reason" varchar(300),
  "approved_by" uuid,
  "from_branch_id" uuid,
  "to_branch_id" uuid,
  "transfer_request_id" uuid,
  "needed_by" date,
  "requested_by" uuid,
  "received_by" uuid,
  "supplier_submission" jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.stock_document ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.stock_document FROM anon, authenticated;

CREATE TABLE public.stock_document_line (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "document_id" uuid NOT NULL,
  "ingredient_id" uuid NOT NULL,
  "unit_id" uuid NOT NULL,
  "quantity" numeric(14,4) NOT NULL CHECK ("quantity" >= 0),
  "counted_qty" numeric(14,4),
  "lot_code" varchar(40),
  "expiry_date" date,
  "purchase_order_line_id" uuid,
  "accepted_qty" numeric(14,4),
  "rejected_qty" numeric(14,4),
  "unit_cost" numeric(14,4),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.stock_document_line ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.stock_document_line FROM anon, authenticated;

CREATE TABLE public.supplier (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "name" varchar(200) NOT NULL,
  "contact" varchar(200),
  "tax_code" varchar(20),
  "payment_terms_days" int,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'inactive')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.supplier ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.supplier FROM anon, authenticated;

CREATE TABLE public.purchase_order (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "supplier_id" uuid NOT NULL,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('requested', 'approved', 'ordered', 'partially_received', 'received', 'closed', 'cancelled')),
  "requested_by" uuid NOT NULL,
  "approved_by" uuid,
  "ordered_at" timestamptz,
  "total_amount" numeric(14,0) NOT NULL DEFAULT 0,
  "paid_amount" numeric(14,0) NOT NULL DEFAULT 0,
  "due_date" date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.purchase_order ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.purchase_order FROM anon, authenticated;

CREATE TABLE public.purchase_order_line (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "purchase_order_id" uuid NOT NULL,
  "ingredient_id" uuid NOT NULL,
  "unit_id" uuid NOT NULL,
  "qty_ordered" numeric(14,4) NOT NULL CHECK ("qty_ordered" > 0),
  "qty_received" numeric(14,4) NOT NULL DEFAULT 0,
  "qty_rejected" numeric(14,4) NOT NULL DEFAULT 0,
  "unit_price" numeric(14,2) NOT NULL CHECK ("unit_price" >= 0),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.purchase_order_line ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.purchase_order_line FROM anon, authenticated;

CREATE TABLE public.complaint (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "invoice_id" uuid NOT NULL,
  "line_id" uuid,
  "category" varchar(20) NOT NULL CHECK ("category" IN ('quality', 'service', 'wrong_item', 'missing', 'other')),
  "description" varchar(500) NOT NULL,
  "resolution" varchar(20) CHECK ("resolution" IN ('apology', 'remake', 'compensate', 'refund')),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('open', 'handling', 'resolved', 'rejected')),
  "handled_by" uuid,
  "compensation_invoice_id" uuid,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.complaint ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.complaint FROM anon, authenticated;

CREATE TABLE public.approval_request (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "kind" varchar(30) NOT NULL CHECK ("kind" IN ('cancel_made_item', 'over_discount', 'refund', 'stock_adjust', 'reopen', 'compensate')),
  "ref_type" varchar(30) NOT NULL,
  "ref_id" uuid NOT NULL,
  "amount" numeric(14,0),
  "reason" varchar(300) NOT NULL,
  "requested_by" uuid NOT NULL,
  "approver_id" uuid,
  "status" varchar(20) NOT NULL CHECK ("status" IN ('pending', 'approved', 'rejected', 'expired')),
  "decided_at" timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.approval_request ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.approval_request FROM anon, authenticated;

CREATE TABLE public.audit_log (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid,
  "actor_id" uuid,
  "device_id" varchar(80),
  "action" varchar(50) NOT NULL,
  "entity_type" varchar(40) NOT NULL,
  "entity_id" uuid NOT NULL,
  "before_data" jsonb,
  "after_data" jsonb,
  "reason" varchar(300),
  "request_id" varchar(64) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.audit_log ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.audit_log FROM anon, authenticated;

CREATE TABLE public.integration_config (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid,
  "kind" varchar(20) NOT NULL CHECK ("kind" IN ('payment', 'einvoice', 'delivery', 'pager')),
  "endpoint" varchar(300),
  "secret_ref" varchar(100),
  "mode" varchar(10) NOT NULL CHECK ("mode" IN ('manual', 'live', 'sandbox')),
  "status" varchar(20) NOT NULL CHECK ("status" IN ('active', 'inactive')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.integration_config ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.integration_config FROM anon, authenticated;

CREATE TABLE public.menu_variant (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "item_id" uuid,
  "kind" varchar(10) NOT NULL,
  "name" varchar(80) NOT NULL,
  "extra_price" numeric(14,0) NOT NULL CHECK ("extra_price" >= 0),
  "sort_order" integer NOT NULL,
  "status" varchar(20) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.menu_variant ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.menu_variant FROM anon, authenticated;

CREATE TABLE public.user_access (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "user_id" uuid NOT NULL,
  "branch_id" uuid,
  "role" varchar(20) NOT NULL,
  "discount_limit_pct" numeric(5,2),
  "refund_limit" numeric(14,0),
  "permissions" jsonb NOT NULL,
  "status" varchar(20) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.user_access ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.user_access FROM anon, authenticated;

CREATE TABLE public.dining_session (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "branch_id" uuid NOT NULL,
  "table_id" uuid NOT NULL,
  "status" varchar(10) NOT NULL,
  "opened_by" uuid,
  "opened_at" timestamptz NOT NULL,
  "closed_at" timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.dining_session ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.dining_session FROM anon, authenticated;

CREATE TABLE public.fulfillment_event (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "invoice_id" uuid NOT NULL,
  "line_id" uuid,
  "ticket_id" uuid,
  "pager_id" uuid,
  "kind" varchar(30) NOT NULL,
  "quantity" integer NOT NULL CHECK ("quantity" >= 0),
  "actor_id" uuid,
  "receiver_id" uuid,
  "receiver_label" varchar(150),
  "result" varchar(20) NOT NULL,
  "request_key" varchar(100) NOT NULL UNIQUE,
  "note" text,
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.fulfillment_event ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.fulfillment_event FROM anon, authenticated;

CREATE TABLE public.async_operation (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "kind" varchar(20) NOT NULL,
  "branch_id" uuid,
  "actor_id" uuid,
  "device_id" varchar(80),
  "dedupe_key" varchar(120) NOT NULL UNIQUE,
  "payload_hash" varchar(64) NOT NULL,
  "payload" jsonb NOT NULL,
  "status" varchar(20) NOT NULL,
  "attempts" integer NOT NULL,
  "available_at" timestamptz NOT NULL,
  "lease_until" timestamptz,
  "result" jsonb,
  "last_error" text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.async_operation ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.async_operation FROM anon, authenticated;

ALTER TABLE public.dining_table ADD CONSTRAINT fk_dining_table_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.station ADD CONSTRAINT fk_station_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.app_user ADD CONSTRAINT fk_app_user_supplier_id FOREIGN KEY ("supplier_id") REFERENCES public.supplier(id) ON DELETE RESTRICT;

ALTER TABLE public.price_rule ADD CONSTRAINT fk_price_rule_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.combo_item ADD CONSTRAINT fk_combo_item_combo_id FOREIGN KEY ("combo_id") REFERENCES public.menu_item(id) ON DELETE RESTRICT;

ALTER TABLE public.combo_item ADD CONSTRAINT fk_combo_item_item_id FOREIGN KEY ("item_id") REFERENCES public.menu_item(id) ON DELETE RESTRICT;

ALTER TABLE public.combo_item ADD CONSTRAINT fk_combo_item_size_id FOREIGN KEY ("size_id") REFERENCES public.menu_variant(id) ON DELETE RESTRICT;

ALTER TABLE public.promotion ADD CONSTRAINT fk_promotion_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.customer ADD CONSTRAINT fk_customer_user_id FOREIGN KEY ("user_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.point_ledger ADD CONSTRAINT fk_point_ledger_customer_id FOREIGN KEY ("customer_id") REFERENCES public.customer(id) ON DELETE RESTRICT;

ALTER TABLE public.point_ledger ADD CONSTRAINT fk_point_ledger_invoice_id FOREIGN KEY ("invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.point_ledger ADD CONSTRAINT fk_point_ledger_origin_entry_id FOREIGN KEY ("origin_entry_id") REFERENCES public.point_ledger(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_table_id FOREIGN KEY ("table_id") REFERENCES public.dining_table(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_staff_id FOREIGN KEY ("staff_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_customer_id FOREIGN KEY ("customer_id") REFERENCES public.customer(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_compensates_invoice_id FOREIGN KEY ("compensates_invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_merged_into_invoice_id FOREIGN KEY ("merged_into_invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_split_from_invoice_id FOREIGN KEY ("split_from_invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_confirmed_by FOREIGN KEY ("confirmed_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_session_id FOREIGN KEY ("session_id") REFERENCES public.dining_session(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_created_shift_id FOREIGN KEY ("created_shift_id") REFERENCES public.shift(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_responsible_shift_id FOREIGN KEY ("responsible_shift_id") REFERENCES public.shift(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice ADD CONSTRAINT fk_invoice_driver_user_id FOREIGN KEY ("driver_user_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_invoice_id FOREIGN KEY ("invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_item_id FOREIGN KEY ("item_id") REFERENCES public.menu_item(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_size_id FOREIGN KEY ("size_id") REFERENCES public.menu_variant(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_combo_id FOREIGN KEY ("combo_id") REFERENCES public.menu_item(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_origin_line_id FOREIGN KEY ("origin_line_id") REFERENCES public.invoice_line(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_station_id FOREIGN KEY ("station_id") REFERENCES public.station(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_accepted_by FOREIGN KEY ("accepted_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_parent_line_id FOREIGN KEY ("parent_line_id") REFERENCES public.invoice_line(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_recipe_id FOREIGN KEY ("recipe_id") REFERENCES public.recipe(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_prepared_by FOREIGN KEY ("prepared_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.invoice_line ADD CONSTRAINT fk_invoice_line_ready_by FOREIGN KEY ("ready_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.ticket ADD CONSTRAINT fk_ticket_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.ticket ADD CONSTRAINT fk_ticket_invoice_id FOREIGN KEY ("invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.ticket ADD CONSTRAINT fk_ticket_pager_id FOREIGN KEY ("pager_id") REFERENCES public.pager_device(id) ON DELETE RESTRICT;

ALTER TABLE public.ticket ADD CONSTRAINT fk_ticket_issued_by FOREIGN KEY ("issued_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.pager_device ADD CONSTRAINT fk_pager_device_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_shift_id FOREIGN KEY ("shift_id") REFERENCES public.shift(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_confirmed_by FOREIGN KEY ("confirmed_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_parent_payment_id FOREIGN KEY ("parent_payment_id") REFERENCES public.payment(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_purchase_order_id FOREIGN KEY ("purchase_order_id") REFERENCES public.purchase_order(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_approval_id FOREIGN KEY ("approval_id") REFERENCES public.approval_request(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_requested_by FOREIGN KEY ("requested_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.payment ADD CONSTRAINT fk_payment_invoice_id FOREIGN KEY ("invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.shift ADD CONSTRAINT fk_shift_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.shift ADD CONSTRAINT fk_shift_opened_by FOREIGN KEY ("opened_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.shift ADD CONSTRAINT fk_shift_handed_to FOREIGN KEY ("handed_to") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.shift_assignment ADD CONSTRAINT fk_shift_assignment_shift_id FOREIGN KEY ("shift_id") REFERENCES public.shift(id) ON DELETE RESTRICT;

ALTER TABLE public.shift_assignment ADD CONSTRAINT fk_shift_assignment_user_id FOREIGN KEY ("user_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.shift_assignment ADD CONSTRAINT fk_shift_assignment_station_id FOREIGN KEY ("station_id") REFERENCES public.station(id) ON DELETE RESTRICT;

ALTER TABLE public.cash_entry ADD CONSTRAINT fk_cash_entry_shift_id FOREIGN KEY ("shift_id") REFERENCES public.shift(id) ON DELETE RESTRICT;

ALTER TABLE public.cash_entry ADD CONSTRAINT fk_cash_entry_created_by FOREIGN KEY ("created_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.ingredient_unit ADD CONSTRAINT fk_ingredient_unit_ingredient_id FOREIGN KEY ("ingredient_id") REFERENCES public.ingredient(id) ON DELETE RESTRICT;

ALTER TABLE public.recipe ADD CONSTRAINT fk_recipe_item_id FOREIGN KEY ("item_id") REFERENCES public.menu_item(id) ON DELETE RESTRICT;

ALTER TABLE public.recipe ADD CONSTRAINT fk_recipe_size_id FOREIGN KEY ("size_id") REFERENCES public.menu_variant(id) ON DELETE RESTRICT;

ALTER TABLE public.recipe_line ADD CONSTRAINT fk_recipe_line_recipe_id FOREIGN KEY ("recipe_id") REFERENCES public.recipe(id) ON DELETE RESTRICT;

ALTER TABLE public.recipe_line ADD CONSTRAINT fk_recipe_line_ingredient_id FOREIGN KEY ("ingredient_id") REFERENCES public.ingredient(id) ON DELETE RESTRICT;

ALTER TABLE public.recipe_line ADD CONSTRAINT fk_recipe_line_unit_id FOREIGN KEY ("unit_id") REFERENCES public.ingredient_unit(id) ON DELETE RESTRICT;

ALTER TABLE public.recipe_line ADD CONSTRAINT fk_recipe_line_option_id FOREIGN KEY ("option_id") REFERENCES public.menu_variant(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_lot ADD CONSTRAINT fk_stock_lot_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_lot ADD CONSTRAINT fk_stock_lot_ingredient_id FOREIGN KEY ("ingredient_id") REFERENCES public.ingredient(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_movement ADD CONSTRAINT fk_stock_movement_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_movement ADD CONSTRAINT fk_stock_movement_ingredient_id FOREIGN KEY ("ingredient_id") REFERENCES public.ingredient(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_movement ADD CONSTRAINT fk_stock_movement_lot_id FOREIGN KEY ("lot_id") REFERENCES public.stock_lot(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_movement ADD CONSTRAINT fk_stock_movement_created_by FOREIGN KEY ("created_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_purchase_order_id FOREIGN KEY ("purchase_order_id") REFERENCES public.purchase_order(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_approved_by FOREIGN KEY ("approved_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_from_branch_id FOREIGN KEY ("from_branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_to_branch_id FOREIGN KEY ("to_branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_transfer_request_id FOREIGN KEY ("transfer_request_id") REFERENCES public.stock_document(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_requested_by FOREIGN KEY ("requested_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document ADD CONSTRAINT fk_stock_document_received_by FOREIGN KEY ("received_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document_line ADD CONSTRAINT fk_stock_document_line_document_id FOREIGN KEY ("document_id") REFERENCES public.stock_document(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document_line ADD CONSTRAINT fk_stock_document_line_ingredient_id FOREIGN KEY ("ingredient_id") REFERENCES public.ingredient(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document_line ADD CONSTRAINT fk_stock_document_line_unit_id FOREIGN KEY ("unit_id") REFERENCES public.ingredient_unit(id) ON DELETE RESTRICT;

ALTER TABLE public.stock_document_line ADD CONSTRAINT fk_stock_document_line_purchase_order_line_id FOREIGN KEY ("purchase_order_line_id") REFERENCES public.purchase_order_line(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order ADD CONSTRAINT fk_purchase_order_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order ADD CONSTRAINT fk_purchase_order_supplier_id FOREIGN KEY ("supplier_id") REFERENCES public.supplier(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order ADD CONSTRAINT fk_purchase_order_requested_by FOREIGN KEY ("requested_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order ADD CONSTRAINT fk_purchase_order_approved_by FOREIGN KEY ("approved_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order_line ADD CONSTRAINT fk_purchase_order_line_purchase_order_id FOREIGN KEY ("purchase_order_id") REFERENCES public.purchase_order(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order_line ADD CONSTRAINT fk_purchase_order_line_ingredient_id FOREIGN KEY ("ingredient_id") REFERENCES public.ingredient(id) ON DELETE RESTRICT;

ALTER TABLE public.purchase_order_line ADD CONSTRAINT fk_purchase_order_line_unit_id FOREIGN KEY ("unit_id") REFERENCES public.ingredient_unit(id) ON DELETE RESTRICT;

ALTER TABLE public.complaint ADD CONSTRAINT fk_complaint_invoice_id FOREIGN KEY ("invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.complaint ADD CONSTRAINT fk_complaint_line_id FOREIGN KEY ("line_id") REFERENCES public.invoice_line(id) ON DELETE RESTRICT;

ALTER TABLE public.complaint ADD CONSTRAINT fk_complaint_handled_by FOREIGN KEY ("handled_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.complaint ADD CONSTRAINT fk_complaint_compensation_invoice_id FOREIGN KEY ("compensation_invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.approval_request ADD CONSTRAINT fk_approval_request_requested_by FOREIGN KEY ("requested_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.approval_request ADD CONSTRAINT fk_approval_request_approver_id FOREIGN KEY ("approver_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.audit_log ADD CONSTRAINT fk_audit_log_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.audit_log ADD CONSTRAINT fk_audit_log_actor_id FOREIGN KEY ("actor_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.integration_config ADD CONSTRAINT fk_integration_config_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.menu_variant ADD CONSTRAINT fk_menu_variant_item_id FOREIGN KEY ("item_id") REFERENCES public.menu_item(id) ON DELETE RESTRICT;

ALTER TABLE public.user_access ADD CONSTRAINT fk_user_access_user_id FOREIGN KEY ("user_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.user_access ADD CONSTRAINT fk_user_access_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.dining_session ADD CONSTRAINT fk_dining_session_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.dining_session ADD CONSTRAINT fk_dining_session_table_id FOREIGN KEY ("table_id") REFERENCES public.dining_table(id) ON DELETE RESTRICT;

ALTER TABLE public.dining_session ADD CONSTRAINT fk_dining_session_opened_by FOREIGN KEY ("opened_by") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.fulfillment_event ADD CONSTRAINT fk_fulfillment_event_invoice_id FOREIGN KEY ("invoice_id") REFERENCES public.invoice(id) ON DELETE RESTRICT;

ALTER TABLE public.fulfillment_event ADD CONSTRAINT fk_fulfillment_event_line_id FOREIGN KEY ("line_id") REFERENCES public.invoice_line(id) ON DELETE RESTRICT;

ALTER TABLE public.fulfillment_event ADD CONSTRAINT fk_fulfillment_event_ticket_id FOREIGN KEY ("ticket_id") REFERENCES public.ticket(id) ON DELETE RESTRICT;

ALTER TABLE public.fulfillment_event ADD CONSTRAINT fk_fulfillment_event_pager_id FOREIGN KEY ("pager_id") REFERENCES public.pager_device(id) ON DELETE RESTRICT;

ALTER TABLE public.fulfillment_event ADD CONSTRAINT fk_fulfillment_event_actor_id FOREIGN KEY ("actor_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.fulfillment_event ADD CONSTRAINT fk_fulfillment_event_receiver_id FOREIGN KEY ("receiver_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

ALTER TABLE public.async_operation ADD CONSTRAINT fk_async_operation_branch_id FOREIGN KEY ("branch_id") REFERENCES public.branch(id) ON DELETE RESTRICT;

ALTER TABLE public.async_operation ADD CONSTRAINT fk_async_operation_actor_id FOREIGN KEY ("actor_id") REFERENCES public.app_user(id) ON DELETE RESTRICT;

CREATE INDEX ix_dining_table_branch_id ON public.dining_table("branch_id");

CREATE INDEX ix_station_branch_id ON public.station("branch_id");

CREATE INDEX ix_app_user_supplier_id ON public.app_user("supplier_id");

CREATE INDEX ix_price_rule_branch_id ON public.price_rule("branch_id");

CREATE INDEX ix_combo_item_combo_id ON public.combo_item("combo_id");

CREATE INDEX ix_combo_item_item_id ON public.combo_item("item_id");

CREATE INDEX ix_combo_item_size_id ON public.combo_item("size_id");

CREATE INDEX ix_promotion_branch_id ON public.promotion("branch_id");

CREATE INDEX ix_customer_user_id ON public.customer("user_id");

CREATE INDEX ix_point_ledger_customer_id ON public.point_ledger("customer_id");

CREATE INDEX ix_point_ledger_invoice_id ON public.point_ledger("invoice_id");

CREATE INDEX ix_point_ledger_origin_entry_id ON public.point_ledger("origin_entry_id");

CREATE INDEX ix_invoice_branch_id ON public.invoice("branch_id");

CREATE INDEX ix_invoice_table_id ON public.invoice("table_id");

CREATE INDEX ix_invoice_staff_id ON public.invoice("staff_id");

CREATE INDEX ix_invoice_customer_id ON public.invoice("customer_id");

CREATE INDEX ix_invoice_compensates_invoice_id ON public.invoice("compensates_invoice_id");

CREATE INDEX ix_invoice_merged_into_invoice_id ON public.invoice("merged_into_invoice_id");

CREATE INDEX ix_invoice_split_from_invoice_id ON public.invoice("split_from_invoice_id");

CREATE INDEX ix_invoice_confirmed_by ON public.invoice("confirmed_by");

CREATE INDEX ix_invoice_session_id ON public.invoice("session_id");

CREATE INDEX ix_invoice_created_shift_id ON public.invoice("created_shift_id");

CREATE INDEX ix_invoice_responsible_shift_id ON public.invoice("responsible_shift_id");

CREATE INDEX ix_invoice_driver_user_id ON public.invoice("driver_user_id");

CREATE INDEX ix_invoice_line_invoice_id ON public.invoice_line("invoice_id");

CREATE INDEX ix_invoice_line_item_id ON public.invoice_line("item_id");

CREATE INDEX ix_invoice_line_size_id ON public.invoice_line("size_id");

CREATE INDEX ix_invoice_line_combo_id ON public.invoice_line("combo_id");

CREATE INDEX ix_invoice_line_origin_line_id ON public.invoice_line("origin_line_id");

CREATE INDEX ix_invoice_line_station_id ON public.invoice_line("station_id");

CREATE INDEX ix_invoice_line_accepted_by ON public.invoice_line("accepted_by");

CREATE INDEX ix_invoice_line_parent_line_id ON public.invoice_line("parent_line_id");

CREATE INDEX ix_invoice_line_recipe_id ON public.invoice_line("recipe_id");

CREATE INDEX ix_invoice_line_prepared_by ON public.invoice_line("prepared_by");

CREATE INDEX ix_invoice_line_ready_by ON public.invoice_line("ready_by");

CREATE INDEX ix_ticket_branch_id ON public.ticket("branch_id");

CREATE INDEX ix_ticket_invoice_id ON public.ticket("invoice_id");

CREATE INDEX ix_ticket_pager_id ON public.ticket("pager_id");

CREATE INDEX ix_ticket_issued_by ON public.ticket("issued_by");

CREATE INDEX ix_pager_device_branch_id ON public.pager_device("branch_id");

CREATE INDEX ix_payment_shift_id ON public.payment("shift_id");

CREATE INDEX ix_payment_confirmed_by ON public.payment("confirmed_by");

CREATE INDEX ix_payment_parent_payment_id ON public.payment("parent_payment_id");

CREATE INDEX ix_payment_purchase_order_id ON public.payment("purchase_order_id");

CREATE INDEX ix_payment_approval_id ON public.payment("approval_id");

CREATE INDEX ix_payment_requested_by ON public.payment("requested_by");

CREATE INDEX ix_payment_invoice_id ON public.payment("invoice_id");

CREATE INDEX ix_shift_branch_id ON public.shift("branch_id");

CREATE INDEX ix_shift_opened_by ON public.shift("opened_by");

CREATE INDEX ix_shift_handed_to ON public.shift("handed_to");

CREATE INDEX ix_shift_assignment_shift_id ON public.shift_assignment("shift_id");

CREATE INDEX ix_shift_assignment_user_id ON public.shift_assignment("user_id");

CREATE INDEX ix_shift_assignment_station_id ON public.shift_assignment("station_id");

CREATE INDEX ix_cash_entry_shift_id ON public.cash_entry("shift_id");

CREATE INDEX ix_cash_entry_created_by ON public.cash_entry("created_by");

CREATE INDEX ix_ingredient_unit_ingredient_id ON public.ingredient_unit("ingredient_id");

CREATE INDEX ix_recipe_item_id ON public.recipe("item_id");

CREATE INDEX ix_recipe_size_id ON public.recipe("size_id");

CREATE INDEX ix_recipe_line_recipe_id ON public.recipe_line("recipe_id");

CREATE INDEX ix_recipe_line_ingredient_id ON public.recipe_line("ingredient_id");

CREATE INDEX ix_recipe_line_unit_id ON public.recipe_line("unit_id");

CREATE INDEX ix_recipe_line_option_id ON public.recipe_line("option_id");

CREATE INDEX ix_stock_lot_branch_id ON public.stock_lot("branch_id");

CREATE INDEX ix_stock_lot_ingredient_id ON public.stock_lot("ingredient_id");

CREATE INDEX ix_stock_movement_branch_id ON public.stock_movement("branch_id");

CREATE INDEX ix_stock_movement_ingredient_id ON public.stock_movement("ingredient_id");

CREATE INDEX ix_stock_movement_lot_id ON public.stock_movement("lot_id");

CREATE INDEX ix_stock_movement_created_by ON public.stock_movement("created_by");

CREATE INDEX ix_stock_document_branch_id ON public.stock_document("branch_id");

CREATE INDEX ix_stock_document_purchase_order_id ON public.stock_document("purchase_order_id");

CREATE INDEX ix_stock_document_approved_by ON public.stock_document("approved_by");

CREATE INDEX ix_stock_document_from_branch_id ON public.stock_document("from_branch_id");

CREATE INDEX ix_stock_document_to_branch_id ON public.stock_document("to_branch_id");

CREATE INDEX ix_stock_document_transfer_request_id ON public.stock_document("transfer_request_id");

CREATE INDEX ix_stock_document_requested_by ON public.stock_document("requested_by");

CREATE INDEX ix_stock_document_received_by ON public.stock_document("received_by");

CREATE INDEX ix_stock_document_line_document_id ON public.stock_document_line("document_id");

CREATE INDEX ix_stock_document_line_ingredient_id ON public.stock_document_line("ingredient_id");

CREATE INDEX ix_stock_document_line_unit_id ON public.stock_document_line("unit_id");

CREATE INDEX ix_stock_document_line_purchase_order_line_id ON public.stock_document_line("purchase_order_line_id");

CREATE INDEX ix_purchase_order_branch_id ON public.purchase_order("branch_id");

CREATE INDEX ix_purchase_order_supplier_id ON public.purchase_order("supplier_id");

CREATE INDEX ix_purchase_order_requested_by ON public.purchase_order("requested_by");

CREATE INDEX ix_purchase_order_approved_by ON public.purchase_order("approved_by");

CREATE INDEX ix_purchase_order_line_purchase_order_id ON public.purchase_order_line("purchase_order_id");

CREATE INDEX ix_purchase_order_line_ingredient_id ON public.purchase_order_line("ingredient_id");

CREATE INDEX ix_purchase_order_line_unit_id ON public.purchase_order_line("unit_id");

CREATE INDEX ix_complaint_invoice_id ON public.complaint("invoice_id");

CREATE INDEX ix_complaint_line_id ON public.complaint("line_id");

CREATE INDEX ix_complaint_handled_by ON public.complaint("handled_by");

CREATE INDEX ix_complaint_compensation_invoice_id ON public.complaint("compensation_invoice_id");

CREATE INDEX ix_approval_request_requested_by ON public.approval_request("requested_by");

CREATE INDEX ix_approval_request_approver_id ON public.approval_request("approver_id");

CREATE INDEX ix_audit_log_branch_id ON public.audit_log("branch_id");

CREATE INDEX ix_audit_log_actor_id ON public.audit_log("actor_id");

CREATE INDEX ix_integration_config_branch_id ON public.integration_config("branch_id");

CREATE INDEX ix_menu_variant_item_id ON public.menu_variant("item_id");

CREATE INDEX ix_user_access_user_id ON public.user_access("user_id");

CREATE INDEX ix_user_access_branch_id ON public.user_access("branch_id");

CREATE INDEX ix_dining_session_branch_id ON public.dining_session("branch_id");

CREATE INDEX ix_dining_session_table_id ON public.dining_session("table_id");

CREATE INDEX ix_dining_session_opened_by ON public.dining_session("opened_by");

CREATE INDEX ix_fulfillment_event_invoice_id ON public.fulfillment_event("invoice_id");

CREATE INDEX ix_fulfillment_event_line_id ON public.fulfillment_event("line_id");

CREATE INDEX ix_fulfillment_event_ticket_id ON public.fulfillment_event("ticket_id");

CREATE INDEX ix_fulfillment_event_pager_id ON public.fulfillment_event("pager_id");

CREATE INDEX ix_fulfillment_event_actor_id ON public.fulfillment_event("actor_id");

CREATE INDEX ix_fulfillment_event_receiver_id ON public.fulfillment_event("receiver_id");

CREATE INDEX ix_async_operation_branch_id ON public.async_operation("branch_id");

CREATE INDEX ix_async_operation_actor_id ON public.async_operation("actor_id");

CREATE UNIQUE INDEX uq_branch_business_key ON public.branch(code);

CREATE UNIQUE INDEX uq_dining_table_business_key ON public.dining_table(branch_id,code);

CREATE UNIQUE INDEX uq_invoice_business_key ON public.invoice(branch_id,invoice_no);

CREATE UNIQUE INDEX uq_pager_device_business_key ON public.pager_device(branch_id,code);

CREATE UNIQUE INDEX uq_ticket_business_key ON public.ticket(branch_id,business_date,ticket_no);

CREATE UNIQUE INDEX uq_stock_movement_business_key ON public.stock_movement(operation_key,seq_no);

CREATE UNIQUE INDEX uq_ingredient_unit_business_key ON public.ingredient_unit(ingredient_id,name);

CREATE UNIQUE INDEX uq_active_table_session ON public.dining_session(table_id) WHERE status='open';

CREATE UNIQUE INDEX uq_assigned_pager ON public.ticket(pager_id) WHERE pager_id IS NOT NULL AND returned_at IS NULL;

CREATE UNIQUE INDEX uq_partner_order ON public.invoice(partner,partner_order_id) WHERE partner_order_id IS NOT NULL;

CREATE UNIQUE INDEX uq_branch_access ON public.user_access(user_id,branch_id,role) WHERE branch_id IS NOT NULL;

CREATE UNIQUE INDEX uq_global_access ON public.user_access(user_id,role) WHERE branch_id IS NULL;

CREATE UNIQUE INDEX uq_customer_user ON public.customer(user_id) WHERE user_id IS NOT NULL;

ALTER TABLE public.price_rule ADD CONSTRAINT ck_price_rule_1 CHECK (num_nonnulls(item_id,variant_id)=1);

ALTER TABLE public.price_rule ADD CONSTRAINT ck_price_rule_2 CHECK (valid_to IS NULL OR valid_to>valid_from);

ALTER TABLE public.invoice_line ADD CONSTRAINT ck_invoice_line_1 CHECK (num_nonnulls(item_id,combo_id)=1);

ALTER TABLE public.invoice_line ADD CONSTRAINT ck_invoice_line_2 CHECK (handed_qty BETWEEN 0 AND quantity);

ALTER TABLE public.invoice_line ADD CONSTRAINT ck_invoice_line_3 CHECK (line_kind IN ('sale','combo_parent','combo_component','remake'));

ALTER TABLE public.approval_request ADD CONSTRAINT ck_approval_request_1 CHECK (approver_id IS NULL OR approver_id<>requested_by);

ALTER TABLE public.stock_document ADD CONSTRAINT ck_stock_document_1 CHECK (from_branch_id IS NULL OR to_branch_id IS NULL OR from_branch_id<>to_branch_id);

ALTER TABLE public.stock_lot ADD CONSTRAINT ck_stock_lot_1 CHECK (reserved_qty>=0 AND reserved_qty<=qty_remaining);

ALTER TABLE public.shift_assignment ADD CONSTRAINT ck_shift_assignment_1 CHECK (to_at IS NULL OR to_at>from_at);

ALTER TABLE public.menu_variant ADD CONSTRAINT ck_menu_variant_1 CHECK (kind IN ('size','sugar','ice','topping'));

ALTER TABLE public.menu_variant ADD CONSTRAINT ck_menu_variant_2 CHECK (status IN ('active','inactive'));

ALTER TABLE public.app_user ADD CONSTRAINT ck_app_user_1 CHECK (account_kind IN ('staff','customer','supplier','driver'));

ALTER TABLE public.payment ADD CONSTRAINT ck_payment_1 CHECK (kind IN ('collect','refund','supplier_pay','supplier_refund'));

ALTER TABLE public.payment ADD CONSTRAINT ck_payment_2 CHECK ((kind IN ('collect','refund') AND invoice_id IS NOT NULL AND purchase_order_id IS NULL) OR (kind IN ('supplier_pay','supplier_refund') AND invoice_id IS NULL AND purchase_order_id IS NOT NULL));

ALTER TABLE public.payment ADD CONSTRAINT ck_payment_3 CHECK (kind NOT IN ('refund','supplier_refund') OR parent_payment_id IS NOT NULL);

ALTER TABLE public.async_operation ADD CONSTRAINT ck_async_operation_1 CHECK (kind IN ('outbox','sync','import','export'));

ALTER TABLE public.async_operation ADD CONSTRAINT ck_async_operation_2 CHECK (status IN ('pending','processing','succeeded','conflict','dead_letter'));

ALTER TABLE public.async_operation ADD CONSTRAINT ck_async_operation_3 CHECK (attempts>=0);

ALTER TABLE public.dining_session ADD CONSTRAINT ck_dining_session_1 CHECK (status IN ('open','closed'));

ALTER TABLE public.user_access ADD CONSTRAINT ck_user_access_1 CHECK (role IN ('staff','shift_lead','owner','accountant','sysadmin','customer','supplier','driver'));

CREATE INDEX ix_invoice_workload ON public.invoice(branch_id,business_date,status);

CREATE INDEX ix_invoice_line_workload ON public.invoice_line(station_id,status,created_at,id);

CREATE INDEX ix_payment_workload ON public.payment(invoice_id,kind,status);

CREATE INDEX ix_stock_movement_workload ON public.stock_movement(branch_id,ingredient_id,created_at);

CREATE INDEX ix_audit_log_workload ON public.audit_log(entity_type,entity_id,created_at);

CREATE INDEX ix_fulfillment_event_workload ON public.fulfillment_event(line_id,created_at);

CREATE INDEX ix_async_operation_workload ON public.async_operation(status,available_at);

COMMIT;

-- Verify table names, FK coverage, RLS and denied anonymous access after execution.