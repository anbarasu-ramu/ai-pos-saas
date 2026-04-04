import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { InventoryComponent } from './inventory-component';
import { NotificationService } from '../../core/notification.service';
import { PosApiService } from '../../core/api/pos-api.service';
import { ProductService } from '../products/product.service';

describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let fixture: ComponentFixture<InventoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventoryComponent],
      providers: [
        provideHttpClient(),
        {
          provide: ProductService,
          useValue: {
            getProducts: jest.fn(() => of([])),
            updateProduct: jest.fn(() => of({})),
            createProduct: jest.fn(() => of({})),
            deactivateProduct: jest.fn(() => of({})),
            activateProduct: jest.fn(() => of({})),
          },
        },
        {
          provide: PosApiService,
          useValue: {
            addToCart: jest.fn(),
          },
        },
        {
          provide: NotificationService,
          useValue: {
            success: jest.fn(),
            error: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
