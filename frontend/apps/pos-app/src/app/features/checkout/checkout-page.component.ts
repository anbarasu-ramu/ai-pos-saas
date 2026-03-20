import { Component } from '@angular/core';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  template: `
    <section class="page">
      <p class="eyebrow">Checkout</p>
      <h2>Payment and order finalization scaffold</h2>

      <div class="grid">
        <article class="panel">
          <h3>Flow milestones</h3>
          <ul>
            <li>Validate stock before confirmation</li>
            <li>Persist order and line items</li>
            <li>Emit payment status and receipt payload</li>
          </ul>
        </article>

        <article class="panel">
          <h3>Guardrails</h3>
          <ul>
            <li>Reject negative or zero quantities</li>
            <li>Block cross-tenant order access</li>
            <li>Capture structured audit events</li>
          </ul>
        </article>
      </div>
    </section>
  `,
  styles: [`
    .page { display: grid; gap: 1rem; }
    .eyebrow { margin: 0; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.75rem; color: #0284c7; }
    h2 { margin: 0; }
    .grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); }
    .panel { background: white; border-radius: 22px; padding: 1.5rem; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08); }
    h3 { margin-top: 0; }
    ul { margin: 0; padding-left: 1.1rem; color: #475569; line-height: 1.8; }
  `],
})
export class CheckoutPageComponent {}
