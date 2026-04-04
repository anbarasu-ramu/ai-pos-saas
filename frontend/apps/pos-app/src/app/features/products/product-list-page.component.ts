import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, inject, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { AgGridAngular } from 'ag-grid-angular';
import {
  AllCommunityModule,
  ColDef,
  FirstDataRenderedEvent,
  GridApi,
  PaginationChangedEvent,
  GridReadyEvent,
  GridSizeChangedEvent,
  ICellRendererParams,
  Module,
  ValueFormatterParams,
} from 'ag-grid-community';
import { PosApiService } from '../../core/api/pos-api.service';
import { ProductService } from './product.service';
import { NotificationService } from '../../core/notification.service';
import { ProductSummary } from '../../core/api/pos.models';

interface ProductRow extends Omit<ProductSummary, 'id'> {
  id: number | string;
  active?: boolean;
}

@Component({
  selector: 'app-product-list-page',
  standalone: true,
  imports: [CommonModule, AgGridAngular],
  templateUrl: './product-list-page.component.html',
  styleUrl: './product-list-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductListPageComponent implements OnInit, OnChanges {
  private readonly api = inject(PosApiService);
  private readonly productService = inject(ProductService);
  private readonly cdr = inject(ChangeDetectorRef);
  private notification = inject(NotificationService);

  private gridApi?: GridApi<ProductRow>;
  private readonly currencyFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  });

  readonly modules: Module[] = [AllCommunityModule];
  readonly defaultColDef: ColDef<ProductRow> = {
    sortable: true,
    filter: true,
    floatingFilter: true,
    resizable: true,
    flex: 1,
    minWidth: 140,
  };

  @Output() updateProduct = new EventEmitter<ProductRow>();
  @Output() toggleActive = new EventEmitter<ProductRow>();
  @Input() showAddToCart = true;
  @Input() isInventoryView = false;
  @Input() pageSize = 10;
  @Output() addToCart = new EventEmitter<ProductRow>();

  @Input() products: ProductRow[] = [];
  columnDefs: ColDef<ProductRow>[] = [];
  currentPage = 0;
  totalPages = 0;
  totalRows = 0;

  @Output() refresh = new EventEmitter<void>();

  ngOnInit() {
    this.updateColumnDefs();

    if (this.shouldLoadFromBackend()) {
      this.loadProductsFromBackend();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    this.updateColumnDefs();

    if (changes['pageSize'] && this.gridApi) {
      this.gridApi.setGridOption('paginationPageSize', this.normalizedPageSize);
    }

    if (changes['isInventoryView'] && !changes['isInventoryView'].firstChange && this.shouldLoadFromBackend()) {
      this.loadProductsFromBackend();
    }
  }

  private shouldLoadFromBackend(): boolean {
    return !this.isInventoryView;
  }

  private loadProductsFromBackend() {
    this.productService.getProducts().subscribe({
      next: (res) => {
        this.products = res.filter((product) => product.active) as ProductRow[];
        this.cdr.markForCheck();
        this.sizeColumnsToFit();
      },
      error: (err) => {
        console.error('Failed to load products', err);
      }
    });
  }

  onRefresh() {
    this.refresh.emit();
  }

  onGridReady(event: GridReadyEvent<ProductRow>) {
    this.gridApi = event.api;
    this.gridApi.setGridOption('paginationPageSize', this.normalizedPageSize);
    this.syncPaginationState();
    this.sizeColumnsToFit();
  }

  onFirstDataRendered(_event: FirstDataRenderedEvent<ProductRow>) {
    this.syncPaginationState();
    this.sizeColumnsToFit();
  }

  onGridSizeChanged(_event: GridSizeChangedEvent<ProductRow>) {
    this.sizeColumnsToFit();
  }

  onPaginationChanged(_event: PaginationChangedEvent<ProductRow>) {
    this.syncPaginationState();
  }

  previousPage() {
    if (!this.gridApi || !this.hasPreviousPage()) {
      return;
    }

    this.gridApi.paginationGoToPreviousPage();
    this.syncPaginationState();
  }

  nextPage() {
    if (!this.gridApi || !this.hasNextPage()) {
      return;
    }

    this.gridApi.paginationGoToNextPage();
    this.syncPaginationState();
  }

  hasPreviousPage(): boolean {
    return this.currentPage > 0;
  }

  hasNextPage(): boolean {
    return this.currentPage + 1 < this.totalPages;
  }

  pageLabel(): string {
    if (!this.totalPages) {
      return 'Page 0 of 0';
    }

    return `Page ${this.currentPage + 1} of ${this.totalPages}`;
  }

  resultsLabel(): string {
    if (!this.totalRows) {
      return 'Showing 0 products';
    }

    const start = this.currentPage * this.normalizedPageSize + 1;
    const end = Math.min(start + this.gridApiPageRowCount() - 1, this.totalRows);

    return `Showing ${start}-${end} of ${this.totalRows} products`;
  }

  onEdit(product: ProductRow) {
    console.log('Edit product:', product);
    this.updateProduct.emit(product);
  }

  onToggleActive(product: ProductRow) {
    console.log('Toggle active for product:', product);
    this.toggleActive.emit(product);
  }

  isProductActive(product: ProductRow): boolean {
    if (this.showAddToCart) {
      // Cashier flow may show all available products, allow adding to cart regardless
      // of admin-side active flag to avoid cart being blocked for default product data.
      return true;
    }

    return product?.active !== false;
  }

  onAddToCart(product: ProductRow) {
    console.log('Add to cart clicked for product:', product);
    if (!this.isProductActive(product)) {
      return;
    }

    this.api.addToCart({
      id: String(product.id),
      name: product.name,
      price: product.price,
      stockQuantity: product.stockQuantity,
      category: product.category,
    });
    this.addToCart.emit(product);
    this.notification.success(`${product.name} added to cart`);
    console.log('Added to cart:', product);
  }

  private get normalizedPageSize(): number {
    return Math.max(1, this.pageSize || 10);
  }

  private updateColumnDefs() {
    const baseColumns: ColDef<ProductRow>[] = [
      {
        field: 'name',
        headerName: 'Name',
        minWidth: 180,
      },
      {
        field: 'category',
        headerName: 'Category',
        minWidth: 160,
      },
      {
        field: 'price',
        headerName: 'Price',
        filter: 'agNumberColumnFilter',
        maxWidth: 150,
        cellStyle: { textAlign: 'right' },
        valueFormatter: (params: ValueFormatterParams<ProductRow, number>) => {
          if (params.value == null) {
            return '';
          }

          return this.currencyFormatter.format(params.value);
        },
      },
    ];

    const inventoryColumns: ColDef<ProductRow>[] = [
      {
        field: 'stockQuantity',
        headerName: 'Stock',
        filter: 'agNumberColumnFilter',
        maxWidth: 130,
        cellStyle: { textAlign: 'right' },
      },
      this.createButtonColumn('Edit', (product) => this.onEdit(product), {
        maxWidth: 130,
        isDisabled: (product) => !product.active,
      }),
      this.createButtonColumn(
        'Deactivate',
        (product) => this.onToggleActive(product),
        {
          maxWidth: 160,
          getLabel: (product) => product.active ? 'Deactivate' : 'Activate',
          getVariant: (product) => product.active ? 'danger' : 'success',
        }
      ),
    ];

    const posColumns: ColDef<ProductRow>[] = [
      this.createButtonColumn('Add', (product) => this.onAddToCart(product), {
        maxWidth: 160,
        getLabel: () => 'Add to Cart',
      }),
    ];

    this.columnDefs = this.isInventoryView
      ? [baseColumns[0], baseColumns[1], inventoryColumns[0], baseColumns[2], inventoryColumns[1], inventoryColumns[2]]
      : [...baseColumns, ...posColumns];

    this.cdr.markForCheck();
  }

  private createButtonColumn(
    headerName: string,
    onClick: (product: ProductRow) => void,
    options?: {
      maxWidth?: number;
      getLabel?: (product: ProductRow) => string;
      getVariant?: (product: ProductRow) => 'danger' | 'success' | 'neutral';
      isDisabled?: (product: ProductRow) => boolean;
    }
  ): ColDef<ProductRow> {
    return {
      headerName,
      colId: headerName.toLowerCase().replace(/\s+/g, '-'),
      sortable: false,
      filter: false,
      floatingFilter: false,
      resizable: false,
      minWidth: 120,
      maxWidth: options?.maxWidth,
      cellStyle: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      },
      cellRenderer: (params: ICellRendererParams<ProductRow>) => {
        const product = params.data;

        if (!product) {
          return '';
        }

        return this.buildActionButton(
          options?.getLabel?.(product) ?? headerName,
          options?.getVariant?.(product) ?? 'danger',
          () => onClick(product),
          options?.isDisabled?.(product) ?? false,
        );
      },
    };
  }

  private buildActionButton(
    label: string,
    variant: 'danger' | 'success' | 'neutral',
    onClick: () => void,
    disabled = false,
  ): HTMLButtonElement {
    const button = document.createElement('button');
    const palette = this.getButtonPalette(variant, disabled);

    button.type = 'button';
    button.textContent = label;
    button.disabled = disabled;
    button.style.border = '0';
    button.style.borderRadius = '999px';
    button.style.padding = '0.45rem 0.85rem';
    button.style.minWidth = '100px';
    button.style.fontWeight = '600';
    button.style.cursor = disabled ? 'not-allowed' : 'pointer';
    button.style.opacity = disabled ? '0.55' : '1';
    button.style.color = palette.color;
    button.style.background = palette.background;
    button.style.whiteSpace = 'nowrap';

    button.addEventListener('click', (event) => {
      event.preventDefault();
      event.stopPropagation();

      if (!disabled) {
        onClick();
      }
    });

    return button;
  }

  private getButtonPalette(variant: 'danger' | 'success' | 'neutral', disabled: boolean) {
    if (disabled) {
      return {
        background: '#e2e8f0',
        color: '#64748b',
      };
    }

    if (variant === 'success') {
      return {
        background: '#16a34a',
        color: '#ffffff',
      };
    }

    if (variant === 'neutral') {
      return {
        background: '#0f172a',
        color: '#ffffff',
      };
    }

    return {
      background: '#da1010',
      color: '#ffffff',
    };
  }

  private sizeColumnsToFit() {
    if (!this.gridApi) {
      return;
    }

    requestAnimationFrame(() => {
      this.gridApi?.sizeColumnsToFit();
    });
  }

  private syncPaginationState() {
    if (!this.gridApi) {
      this.currentPage = 0;
      this.totalPages = 0;
      this.totalRows = this.products.length;
      this.cdr.markForCheck();
      return;
    }

    this.currentPage = this.gridApi.paginationGetCurrentPage();
    this.totalPages = this.gridApi.paginationGetTotalPages();
    this.totalRows = this.gridApi.paginationGetRowCount();
    this.cdr.markForCheck();
  }

  private gridApiPageRowCount(): number {
    if (!this.gridApi) {
      return Math.min(this.normalizedPageSize, this.products.length);
    }

    const pageSize = this.gridApi.paginationGetPageSize();
    const remaining = this.totalRows - this.currentPage * pageSize;
    return Math.max(0, Math.min(pageSize, remaining));
  }
}
