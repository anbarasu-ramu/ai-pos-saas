import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';

@Component({
  selector: 'app-ai-assistant-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="page">
      <div class="hero">
        <div>
          <p class="eyebrow">AI Assistant</p>
          <h2>Tool-calling and insights scaffold</h2>
        </div>
        <button type="button">Open Chat Overlay</button>
      </div>

      <div class="suggestions">
        <article *ngFor="let suggestion of suggestions">
          <h3>{{ suggestion.title }}</h3>
          <p>{{ suggestion.detail }}</p>
        </article>
      </div>
    </section>
  `,
  styles: [`
    .page { display: grid; gap: 1rem; }
    .hero { display: flex; justify-content: space-between; gap: 1rem; align-items: center; }
    .eyebrow { margin: 0; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.75rem; color: #0284c7; }
    h2 { margin: 0.4rem 0 0; }
    button { border: 0; border-radius: 999px; background: #0ea5e9; color: white; padding: 0.9rem 1.2rem; cursor: pointer; }
    .suggestions { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
    article { background: white; padding: 1.4rem; border-radius: 22px; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08); }
    h3 { margin-top: 0; }
    p { margin-bottom: 0; color: #475569; line-height: 1.6; }
  `],
})
export class AiAssistantPageComponent {
  private readonly api = inject(PosApiService);
  protected readonly suggestions = this.api.getAiSuggestions();
}
