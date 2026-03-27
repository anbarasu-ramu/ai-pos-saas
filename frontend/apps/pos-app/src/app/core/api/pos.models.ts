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

export interface OrderSummary {
  id: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  createdByUsername: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
