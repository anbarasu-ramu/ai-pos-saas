import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { AgGridAngular } from 'ag-grid-angular';
import {
  AllCommunityModule,
  ColDef,
  FirstDataRenderedEvent,
  GridApi,
  GridReadyEvent,
  GridSizeChangedEvent,
  ICellRendererParams,
  Module,
  ValueFormatterParams,
} from 'ag-grid-community';
import { PosApiService } from '../../core/api/pos-api.service';
import { OrderSummary } from '../../core/api/pos.models';

@Component({
  selector: 'app-orders-page',
  standalone: true,
  imports: [CommonModule, AgGridAngular],
  templateUrl: './orders-page.component.html',
  styleUrls: ['./orders-page.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersPageComponent {
  private readonly api = inject(PosApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private gridApi?: GridApi<OrderSummary>;
  private readonly currencyFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  });
  private readonly dateFormatter = new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });

  readonly modules: Module[] = [AllCommunityModule];
  readonly defaultColDef: ColDef<OrderSummary> = {
    sortable: true,
    filter: true,
    floatingFilter: true,
    resizable: true,
    flex: 1,
    minWidth: 150,
  };
  readonly columnDefs: ColDef<OrderSummary>[] = [
    {
      field: 'id',
      headerName: 'Order ID',
      minWidth: 120,
      maxWidth: 140,
      valueFormatter: (params: ValueFormatterParams<OrderSummary, number>) => {
        if (params.value == null) {
          return '';
        }

        return `#${params.value}`;
      },
    },
    {
      field: 'createdByUsername',
      headerName: 'Created By',
      minWidth: 180,
      valueFormatter: (params: ValueFormatterParams<OrderSummary, string>) => params.value || 'Unknown',
    },
    {
      field: 'status',
      headerName: 'Status',
      minWidth: 150,
      maxWidth: 170,
      cellRenderer: (params: ICellRendererParams<OrderSummary>) => {
        const status = params.value ? String(params.value) : 'Unknown';
        return this.buildStatusBadge(status);
      },
    },
    {
      field: 'totalAmount',
      headerName: 'Total',
      filter: 'agNumberColumnFilter',
      minWidth: 140,
      maxWidth: 160,
      cellStyle: { textAlign: 'right' },
      valueFormatter: (params: ValueFormatterParams<OrderSummary, number>) => {
        if (params.value == null) {
          return '';
        }

        return this.currencyFormatter.format(params.value);
      },
    },
    {
      field: 'createdAt',
      headerName: 'Created At',
      minWidth: 220,
      valueFormatter: (params: ValueFormatterParams<OrderSummary, string>) => {
        if (!params.value) {
          return '';
        }

        return this.dateFormatter.format(new Date(params.value));
      },
    },
  ];

  protected readonly orders = signal<OrderSummary[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(20);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);

  constructor() {
    this.loadOrders();
  }

  protected loadOrders(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.api.getOrders(this.currentPage(), this.pageSize()).subscribe({
      next: (response) => {
        this.orders.set(response.content ?? []);
        this.totalElements.set(response.totalElements ?? 0);
        this.totalPages.set(response.totalPages ?? 0);
        this.pageSize.set(response.size ?? this.pageSize());
        this.currentPage.set(response.number ?? this.currentPage());
        this.isLoading.set(false);
        this.cdr.markForCheck();
        this.sizeColumnsToFit();
      },
      error: () => {
        this.errorMessage.set('Unable to load orders right now.');
        this.isLoading.set(false);
      },
    });
  }

  protected onGridReady(event: GridReadyEvent<OrderSummary>) {
    this.gridApi = event.api;
    this.sizeColumnsToFit();
  }

  protected onFirstDataRendered(_event: FirstDataRenderedEvent<OrderSummary>) {
    this.sizeColumnsToFit();
  }

  protected onGridSizeChanged(_event: GridSizeChangedEvent<OrderSummary>) {
    this.sizeColumnsToFit();
  }

  protected previousPage(): void {
    if (this.currentPage() === 0 || this.isLoading()) {
      return;
    }

    this.currentPage.set(this.currentPage() - 1);
    this.loadOrders();
  }

  protected nextPage(): void {
    if (!this.hasNextPage() || this.isLoading()) {
      return;
    }

    this.currentPage.set(this.currentPage() + 1);
    this.loadOrders();
  }

  protected retry(): void {
    this.loadOrders();
  }

  protected hasPreviousPage(): boolean {
    return this.currentPage() > 0;
  }

  protected hasNextPage(): boolean {
    return this.currentPage() + 1 < this.totalPages();
  }

  protected pageLabel(): string {
    if (!this.totalPages()) {
      return 'Page 0 of 0';
    }

    return `Page ${this.currentPage() + 1} of ${this.totalPages()}`;
  }

  protected resultsLabel(): string {
    if (!this.totalElements()) {
      return 'Showing 0 results';
    }

    const start = this.currentPage() * this.pageSize() + 1;
    const end = Math.min(start + this.orders().length - 1, this.totalElements());

    return `Showing ${start}-${end} of ${this.totalElements()} orders`;
  }

  private buildStatusBadge(status: string): HTMLSpanElement {
    const badge = document.createElement('span');
    const normalizedStatus = status.toUpperCase();

    badge.textContent = status;
    badge.className = 'status-chip';

    if (normalizedStatus === 'COMPLETED' || normalizedStatus === 'PAID') {
      badge.classList.add('status-chip-success');
    } else if (normalizedStatus === 'PENDING') {
      badge.classList.add('status-chip-pending');
    } else if (normalizedStatus === 'FAILED' || normalizedStatus === 'CANCELLED') {
      badge.classList.add('status-chip-danger');
    } else {
      badge.classList.add('status-chip-neutral');
    }

    return badge;
  }

  private sizeColumnsToFit() {
    if (!this.gridApi) {
      return;
    }

    requestAnimationFrame(() => {
      this.gridApi?.sizeColumnsToFit();
    });
  }
}
