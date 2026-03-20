import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PosApiService } from '../../core/api/pos-api.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="page">
      <header>
        <p class="eyebrow">Overview</p>
        <h2>Operational dashboard scaffold</h2>
      </header>

      <div class="grid">
        <article class="metric" *ngFor="let metric of metrics">
          <p>{{ metric.label }}</p>
          <strong>{{ metric.value }}</strong>
          <span>{{ metric.trend }}</span>
        </article>
      </div>
    </section>
  `,
  styles: [`
    .page { display: grid; gap: 1.5rem; }
    .eyebrow { margin: 0; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.75rem; color: #0284c7; }
    h2 { margin: 0.4rem 0 0; font-size: 2rem; }
    .grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
    .metric { padding: 1.4rem; border-radius: 22px; background: white; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08); }
    p { margin: 0; color: #64748b; }
    strong { display: block; margin: 0.8rem 0 0.35rem; font-size: 2rem; color: #0f172a; }
    span { color: #0f766e; }
  `],
})
export class DashboardPageComponent {
  private readonly api = inject(PosApiService);
  protected readonly metrics = this.api.getDashboardMetrics();
}
