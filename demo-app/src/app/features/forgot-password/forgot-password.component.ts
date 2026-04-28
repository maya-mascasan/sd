import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatStepperModule } from '@angular/material/stepper';
import { CommonModule } from '@angular/common';

interface BackendError {
  details?: string;
  message?: string;
}

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatStepperModule
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page">
      <mat-card class="card">
        <h1>Reset Password</h1>

        @if (step() === 'request') {
          <form [formGroup]="requestForm" (ngSubmit)="sendCode()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput formControlName="email" type="email" />
            </mat-form-field>
            @if (errorMessage()) {
              <p class="error">{{ errorMessage() }}</p>
            }
            <button mat-flat-button type="submit" [disabled]="isSubmitting()">
              {{ isSubmitting() ? 'Sending...' : 'Send Reset Code' }}
            </button>
          </form>
        }

        @if (step() === 'confirm') {
          <form [formGroup]="confirmForm" (ngSubmit)="confirmReset()">
            <p>A 6-digit code was sent to <strong>{{ requestForm.value.email }}</strong></p>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Reset Code</mat-label>
              <input matInput formControlName="code" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>New Password</mat-label>
              <input matInput formControlName="newPassword" type="password" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirm Password</mat-label>
              <input matInput formControlName="confirmPassword" type="password" />
            </mat-form-field>
            @if (errorMessage()) {
              <p class="error">{{ errorMessage() }}</p>
            }
            <button mat-flat-button type="submit" [disabled]="isSubmitting()">
              {{ isSubmitting() ? 'Resetting...' : 'Reset Password' }}
            </button>
          </form>
        }

        @if (step() === 'done') {
          <p class="success">Password reset successfully! You can now <a routerLink="/login">log in</a>.</p>
        }

        <a routerLink="/login" class="back-link">Back to login</a>
      </mat-card>
    </main>
  `,
  styles: [`
    .page { min-height: 100dvh; display: grid; place-content: center; background: #f5f5f5; padding: 1.5rem; }
    .card { width: min(100%, 26rem); padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
    .full-width { width: 100%; }
    .error { color: #b71c1c; font-weight: 500; }
    .success { color: #2e7d32; font-weight: 500; }
    .back-link { font-size: 0.875rem; }
    button { width: 100%; }
  `]
})

export class ForgotPasswordComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly step = signal<'request' | 'confirm' | 'done'>('request');
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly requestForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly confirmForm = this.fb.group({
    code: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  });

  sendCode(): void {
    if (this.requestForm.invalid) return;

    const email = this.requestForm.get('email')?.value as string;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.http.post('http://localhost:8080/password-reset/request', { email })
      .subscribe({
        next: () => {
          this.step.set('confirm');
          this.isSubmitting.set(false);
        },
        error: (err: HttpErrorResponse) => {
          const errorData = err.error as BackendError;
          this.errorMessage.set(errorData?.details ?? 'Failed to send reset code.');
          this.isSubmitting.set(false);
        }
      });
  }

  confirmReset(): void {
    if (this.confirmForm.invalid) return;

    // Extract values with strict types
    const email = this.requestForm.get('email')?.value as string;
    const code = this.confirmForm.get('code')?.value as string;
    const newPassword = this.confirmForm.get('newPassword')?.value as string;
    const confirmPassword = this.confirmForm.get('confirmPassword')?.value as string;

    if (newPassword !== confirmPassword) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.http.post('http://localhost:8080/password-reset/confirm', {
      email,
      code,
      newPassword
    }).subscribe({
      next: () => {
        this.step.set('done');
        this.isSubmitting.set(false);
      },


      error: (err: HttpErrorResponse) => {
        const errorData = err.error as BackendError;
        this.errorMessage.set(errorData?.details ?? 'Invalid or expired code.');
        this.isSubmitting.set(false);
      }
    });
  }

}
