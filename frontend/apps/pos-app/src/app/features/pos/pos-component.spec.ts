import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { PosComponent } from './pos-component';
import { NotificationService } from '../../core/notification.service';
import { PosApiService } from '../../core/api/pos-api.service';
import { ProductService } from '../products/product.service';

describe('PosComponent', () => {
  let component: PosComponent;
  let fixture: ComponentFixture<PosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PosComponent],
      providers: [
        {
          provide: ProductService,
          useValue: {
            getProducts: jest.fn(() => of([])),
          },
        },
        {
          provide: PosApiService,
          useValue: {
            addToCart: jest.fn(),
            getCartLines: jest.fn(() => signal([]).asReadonly()),
            increment: jest.fn(),
            decrement: jest.fn(),
            removeFromCart: jest.fn(),
            clearCart: jest.fn(),
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

    fixture = TestBed.createComponent(PosComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
