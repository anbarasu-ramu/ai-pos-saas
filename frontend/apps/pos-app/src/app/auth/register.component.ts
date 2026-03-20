import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    tenantName: ['', [Validators.required]],
  });

  protected errorMessage = '';
  protected successMessage = '';
  protected isSubmitting = false;
  private hasSubmitError = false;

  constructor() {
    this.form.valueChanges.subscribe(() => {
      if (this.hasSubmitError && this.errorMessage) {
        this.errorMessage = '';
        this.hasSubmitError = false;
      }
    });
  }

  protected showError(controlName: 'email' | 'password' | 'tenantName'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.touched || control.dirty);
  }

  protected fieldError(controlName: 'email' | 'password' | 'tenantName'): string {
    const control = this.form.controls[controlName];

    if (control.hasError('required')) {
      if (controlName === 'email') {
        return 'Email is required.';
      }

      if (controlName === 'password') {
        return 'Password is required.';
      }

      return 'Tenant name is required.';
    }

    if (controlName === 'email' && control.hasError('email')) {
      return 'Enter a valid email address.';
    }

    if (controlName === 'password' && control.hasError('minlength')) {
      return 'Password must be at least 8 characters.';
    }

    return '';
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Please fix the highlighted fields and try again.';
      this.hasSubmitError = true;
      this.resetForm();
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.isSubmitting = true;

    try {
      const response = await firstValueFrom(this.authService.register(this.form.getRawValue()));
      this.successMessage = response.message;
      this.isSubmitting = false;
      window.setTimeout(() => {
        void this.authService.login(response.email);
      }, 800);
    } catch (error: any) {
      this.isSubmitting = false;
      this.errorMessage = error?.error?.message ?? 'Registration failed. Please try again.';
      this.hasSubmitError = true;
      this.resetForm();
    }
  }

  private resetForm(): void {
    this.form.reset({
      email: '',
      password: '',
      tenantName: '',
    }, {
      emitEvent: false,
    });
    this.form.markAsPristine();
    this.form.markAsUntouched();
  }
}
