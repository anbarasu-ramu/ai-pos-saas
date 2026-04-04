import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NotificationService } from '../../core/notification.service';
import { PosApiService } from '../../core/api/pos-api.service';
import { AiAssistantPageComponent } from './ai-assistant-page.component';

describe('AiAssistantPageComponent', () => {
  let fixture: ComponentFixture<AiAssistantPageComponent>;
  let component: AiAssistantPageComponent;

  const api = {
    getAiSuggestions: jest.fn(() => [
      { title: 'Low-stock sweep', detail: 'Show low stock under 3 units' },
    ]),
    chatWithAssistant: jest.fn(),
  };

  const notifications = {
    info: jest.fn(),
    error: jest.fn(),
    success: jest.fn(),
  };

  beforeEach(async () => {
    jest.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AiAssistantPageComponent],
      providers: [
        { provide: PosApiService, useValue: api },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AiAssistantPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders structured low-stock results', () => {
    api.chatWithAssistant.mockReturnValue(of({
      message: 'Found 2 low-stock products.',
      data: {
        assistantMessage: 'Found 2 low-stock products.',
        intent: 'GET_LOW_STOCK_PRODUCTS',
        toolInvocations: [],
        requiresConfirmation: false,
        result: {
          type: 'low_stock',
          threshold: 3,
          count: 2,
          items: [
            { id: '1', name: 'Arabica Beans', price: 300, stockQuantity: 2, category: 'Coffee' },
            { id: '2', name: 'Paper Cups', price: 30, stockQuantity: 1, category: 'Supplies' },
          ],
        },
      },
    }));

    component['draft'] = 'show low stock under 3';
    component['sendMessage']();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Structured assistant output');
    expect(text).toContain('Low-stock items');
    expect(text).toContain('Arabica Beans');
    expect(text).toContain('Paper Cups');
  });

  it('renders clarification options without confirmation CTA', () => {
    api.chatWithAssistant.mockReturnValue(of({
      message: 'I found a few close matches for "coffee". Which product did you mean?',
      data: {
        assistantMessage: 'I found a few close matches for "coffee". Which product did you mean?',
        intent: 'SEARCH_PRODUCTS',
        toolInvocations: [],
        requiresConfirmation: false,
        result: {
          type: 'clarification',
          targetIntent: 'SEARCH_PRODUCTS',
          reason: 'AMBIGUOUS_PRODUCT_QUERY',
          query: 'coffee',
          options: [
            {
              productId: 1,
              confidence: 0.82,
              reason: 'Close beverage match',
              matchType: 'TOKEN_OVERLAP',
              product: { id: '1', name: 'Iced Coffee', price: 120, stockQuantity: 8, category: 'Beverages' },
            },
          ],
        },
      },
    }));

    component['draft'] = 'find coffee';
    component['sendMessage']();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Clarification needed');
    expect(text).toContain('Iced Coffee');
    expect(text).not.toContain('Confirm last action');
  });

  it('shows confirmation CTA only when confirmation is required', () => {
    api.chatWithAssistant.mockReturnValue(of({
      message: 'I’m ready to do that, but I need your explicit confirmation before executing it.',
      data: {
        assistantMessage: 'I’m ready to do that, but I need your explicit confirmation before executing it.',
        intent: 'CREATE_CHECKOUT_ORDER',
        toolInvocations: [],
        requiresConfirmation: true,
        result: {
          type: 'checkout_result',
        },
      },
    }));

    component['draft'] = 'create order for 2 cappuccinos';
    component['sendMessage']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Confirm last action');
    expect(notifications.info).toHaveBeenCalled();
  });

  it('renders assistant error responses', () => {
    api.chatWithAssistant.mockReturnValue(throwError(() => ({
      error: { message: 'The assistant is unavailable right now.' },
    })));

    component['draft'] = 'today sales';
    component['sendMessage']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('The assistant is unavailable right now.');
    expect(notifications.error).toHaveBeenCalled();
  });
});
