import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../core/notification.service';
import { PosApiService } from '../../core/api/pos-api.service';
import {
  AiAssistantResponse,
  AiClarificationResult,
  AiDailySummaryResult,
  AiLowStockResult,
  AiOrderDetailResult,
  AiOrderListResult,
  AiProductSearchResult,
  AiStructuredResult,
  AiSuggestion,
  AiTenantContextResult,
  AiUserContextResult,
} from '../../core/api/pos.models';

interface ChatMessage {
  id: number;
  role: 'user' | 'assistant';
  text: string;
  timestamp: string;
  response?: AiAssistantResponse;
}

@Component({
  selector: 'app-ai-assistant-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-assistant-page.component.html',
  styleUrl: './ai-assistant-page.component.css',
})
export class AiAssistantPageComponent {
  private readonly api = inject(PosApiService);
  private readonly notifications = inject(NotificationService);

  protected readonly suggestions = this.api.getAiSuggestions();
  protected readonly isLoading = signal(false);
  protected readonly messages = signal<ChatMessage[]>([
    {
      id: 1,
      role: 'assistant',
      text: 'I’m connected to the backend AI orchestration endpoint. Ask about low stock, product search, order history, or daily sales.',
      timestamp: this.timestamp(),
    },
  ]);

  protected draft = '';

  protected readonly latestResponse = computed(() => {
    const assistantMessages = this.messages()
      .filter((message) => message.role === 'assistant' && message.response);
    return assistantMessages.length ? assistantMessages[assistantMessages.length - 1].response : null;
  });

  protected readonly latestResultPreview = computed(() => {
    const response = this.latestResponse();
    return response ? this.formatJson(response.result) : '{\n  "message": "No structured result yet"\n}';
  });
  protected readonly latestStructuredResult = computed(() => this.asStructuredResult(this.latestResponse()?.result));
  protected readonly latestHighlights = computed(() => this.buildHighlights(this.latestStructuredResult()));

  protected sendMessage(): void {
    const message = this.draft.trim();
    if (!message || this.isLoading()) {
      return;
    }

    this.pushMessage({
      role: 'user',
      text: message,
    });
    this.draft = '';
    this.isLoading.set(true);

    this.api.chatWithAssistant(message).subscribe({
      next: (response: any) => {
        this.pushMessage({
          role: 'assistant',
          text: response.data.assistantMessage || response.message,
          response: response.data,
        });

        if (response.data.requiresConfirmation) {
          this.notifications.info('The assistant is waiting for explicit confirmation before executing that action.');
        }

        this.isLoading.set(false);
      },
      error: (error: any) => {
        const messageText = error?.error?.message || 'The assistant is unavailable right now.';
        this.pushMessage({
          role: 'assistant',
          text: messageText,
        });
        this.notifications.error(messageText, 'AI Assistant');
        this.isLoading.set(false);
      },
    });
  }

  protected applySuggestion(suggestion: AiSuggestion): void {
    this.draft = suggestion.detail;
  }

  protected confirmLastAction(): void {
    const lastUserMessage = [...this.messages()].reverse().find((message) => message.role === 'user');
    if (!lastUserMessage) {
      return;
    }

    this.draft = `confirm ${lastUserMessage.text}`;
    this.sendMessage();
  }

  protected canConfirmLastAction(): boolean {
    return !!this.latestResponse()?.requiresConfirmation;
  }

  protected resetConversation(): void {
    this.messages.set([
      {
        id: 1,
        role: 'assistant',
        text: 'Conversation cleared. Ask about inventory, orders, or checkout when you are ready.',
        timestamp: this.timestamp(),
      },
    ]);
    this.draft = '';
  }

  protected copyLatestResult(): void {
    const response = this.latestResponse();
    if (!response) {
      return;
    }

    navigator.clipboard.writeText(this.formatJson(response.result))
      .then(() => this.notifications.success('Latest AI result copied to clipboard.'))
      .catch(() => this.notifications.error('Unable to copy the AI result right now.'));
  }

  protected formatJson(value: unknown): string {
    if (value == null) {
      return '{}';
    }

    try {
      return JSON.stringify(value, null, 2);
    } catch {
      return String(value);
    }
  }

  protected trackMessage(_index: number, message: ChatMessage): number {
    return message.id;
  }

  protected resultTypeLabel(result: AiStructuredResult | null): string {
    if (!result) {
      return 'No request yet';
    }

    switch (result.type) {
      case 'product_search':
        return 'Product search';
      case 'low_stock':
        return 'Low stock';
      case 'order_list':
        return 'Recent orders';
      case 'order_detail':
        return 'Order detail';
      case 'daily_summary':
        return 'Daily summary';
      case 'user_context':
        return 'User context';
      case 'tenant_context':
        return 'Tenant context';
      case 'clarification':
        return 'Clarification needed';
      default:
        return result.type;
    }
  }

  protected asClarificationResult(result: AiStructuredResult | null): AiClarificationResult | null {
    return result?.type === 'clarification' ? (result as AiClarificationResult) : null;
  }

  protected asProductSearchResult(result: AiStructuredResult | null): AiProductSearchResult | null {
    return result?.type === 'product_search' ? (result as AiProductSearchResult) : null;
  }

  protected asLowStockResult(result: AiStructuredResult | null): AiLowStockResult | null {
    return result?.type === 'low_stock' ? (result as AiLowStockResult) : null;
  }

  protected asOrderListResult(result: AiStructuredResult | null): AiOrderListResult | null {
    return result?.type === 'order_list' ? (result as AiOrderListResult) : null;
  }

  protected asOrderDetailResult(result: AiStructuredResult | null): AiOrderDetailResult | null {
    return result?.type === 'order_detail' ? (result as AiOrderDetailResult) : null;
  }

  protected asDailySummaryResult(result: AiStructuredResult | null): AiDailySummaryResult | null {
    return result?.type === 'daily_summary' ? (result as AiDailySummaryResult) : null;
  }

  protected asUserContextResult(result: AiStructuredResult | null): AiUserContextResult | null {
    return result?.type === 'user_context' ? (result as AiUserContextResult) : null;
  }

  protected asTenantContextResult(result: AiStructuredResult | null): AiTenantContextResult | null {
    return result?.type === 'tenant_context' ? (result as AiTenantContextResult) : null;
  }

  private pushMessage(input: Omit<ChatMessage, 'id' | 'timestamp'>): void {
    this.messages.update((messages) => [
      ...messages,
      {
        ...input,
        id: messages.length + 1,
        timestamp: this.timestamp(),
      },
    ]);
  }

  private timestamp(): string {
    return new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: '2-digit',
    }).format(new Date());
  }

  private asStructuredResult(result: unknown): AiStructuredResult | null {
    if (!result || typeof result !== 'object' || !('type' in result)) {
      return null;
    }

    const typedResult = result as { type?: unknown };
    return typeof typedResult.type === 'string' ? result as AiStructuredResult : null;
  }

  private buildHighlights(result: AiStructuredResult | null): Array<{ label: string; value: string }> {
    if (!result) {
      return [];
    }

    switch (result.type) {
      case 'product_search': {
        const typed = result as AiProductSearchResult;
        return [
          { label: 'Query', value: typed.query },
          { label: 'Matches', value: String(typed.count) },
          { label: 'Status', value: typed.status.replaceAll('_', ' ') },
        ];
      }
      case 'low_stock': {
        const typed = result as AiLowStockResult;
        return [
          { label: 'Threshold', value: String(typed.threshold) },
          { label: 'Flagged items', value: String(typed.count) },
        ];
      }
      case 'order_list': {
        const typed = result as AiOrderListResult;
        return [
          { label: 'Orders', value: String(typed.totalElements) },
          { label: 'Page', value: `${typed.page + 1} / ${Math.max(typed.totalPages, 1)}` },
        ];
      }
      case 'order_detail': {
        const typed = result as AiOrderDetailResult;
        return [
          { label: 'Order', value: `#${typed.order.id}` },
          { label: 'Status', value: typed.order.status },
          { label: 'Items', value: String(typed.order.items.length) },
        ];
      }
      case 'daily_summary': {
        const typed = result as AiDailySummaryResult;
        return [
          { label: 'Business date', value: typed.businessDate },
          { label: 'Orders', value: String(typed.summary.totalOrders) },
          { label: 'Revenue', value: this.formatCurrency(typed.summary.totalRevenue) },
        ];
      }
      case 'user_context': {
        const typed = result as AiUserContextResult;
        return [
          { label: 'User', value: typed.user.username },
          { label: 'Roles', value: typed.user.roles.join(', ') || 'None' },
        ];
      }
      case 'tenant_context': {
        const typed = result as AiTenantContextResult;
        return [
          { label: 'Tenant', value: typed.tenant.tenantName },
          { label: 'Tenant ID', value: typed.tenant.tenantId },
        ];
      }
      case 'clarification': {
        const typed = result as AiClarificationResult;
        return [
          { label: 'Reason', value: typed.reason.replaceAll('_', ' ') },
          { label: 'Options', value: String(typed.options.length) },
        ];
      }
      default:
        return [];
    }
  }

  private formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 2,
    }).format(Number(value ?? 0));
  }
}
