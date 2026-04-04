export interface MetricCard {
  label: string;
  value: string;
  trend: string;
}

export interface ProductSummary {
  id: string;
  name: string;
  price: number;
  stockQuantity: number;
  category: string;
}

export interface CartLine {
  sku: string;
  name: string;
  quantity: number;
  total: number;
}

export interface AiSuggestion {
  title: string;
  detail: string;
}

export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface AiToolInvocation {
  tool: string;
  arguments: Record<string, unknown>;
  status: string;
  errorMessage?: string | null;
}

export interface AiAssistantResponse {
  assistantMessage: string;
  intent: string;
  toolInvocations: AiToolInvocation[];
  result: unknown;
  requiresConfirmation: boolean;
}

export interface AiProductMatch {
  productId: number;
  confidence: number;
  reason: string;
  matchType: string;
  product: ProductSummary;
}

export interface AiClarificationOption {
  productId: number;
  confidence: number;
  reason: string;
  matchType: string;
  product: ProductSummary;
}

export interface AiClarificationResult {
  type: 'clarification';
  targetIntent: string;
  reason: string;
  query: string;
  options: AiClarificationOption[];
}

export interface AiProductSearchResult {
  type: 'product_search';
  status: string;
  clarification: boolean;
  query: string;
  activeOnly: boolean;
  limit: number;
  count: number;
  items: ProductSummary[];
  matches: AiProductMatch[];
}

export interface AiLowStockResult {
  type: 'low_stock';
  threshold: number;
  count: number;
  items: ProductSummary[];
}

export interface AiOrderListItem {
  id: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  createdByUsername: string;
}

export interface AiOrderListResult {
  type: 'order_list';
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  items: AiOrderListItem[];
}

export interface AiOrderDetailItem {
  productId: number;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface AiOrderDetail {
  id: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  createdByUsername: string;
  createdByUserId: string;
  items: AiOrderDetailItem[];
}

export interface AiOrderDetailResult {
  type: 'order_detail';
  order: AiOrderDetail;
}

export interface AiDailySummary {
  businessDate: string;
  totalOrders: number;
  completedOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
}

export interface AiDailySummaryResult {
  type: 'daily_summary';
  zone: string;
  businessDate: string;
  summary: AiDailySummary;
}

export interface AiUserContextResult {
  type: 'user_context';
  user: {
    subject: string;
    username: string;
    email: string;
    roles: string[];
    tenantId: string;
    tenantName: string;
  };
}

export interface AiTenantContextResult {
  type: 'tenant_context';
  tenant: {
    tenantId: string;
    tenantName: string;
  };
}

export type AiStructuredResult =
  | AiClarificationResult
  | AiProductSearchResult
  | AiLowStockResult
  | AiOrderListResult
  | AiOrderDetailResult
  | AiDailySummaryResult
  | AiUserContextResult
  | AiTenantContextResult
  | { type: string; [key: string]: unknown };

export interface OrderSummary {
  id: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  createdByUsername: string;
}

export interface CheckoutRequest {
  items: {
    productId: string;
    quantity: number;
  }[];
  paymentType: string;
  amountPaid: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
