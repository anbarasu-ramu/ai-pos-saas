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
