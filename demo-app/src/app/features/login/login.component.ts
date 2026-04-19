import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { LoginStore } from './login.store';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class LoginComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly isSubmitting = this.loginStore.isSubmitting;
  protected readonly errorMessage = this.loginStore.errorMessage;

  protected readonly loginForm = this.formBuilder.group({
    email: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  protected submit(): void {
    if (this.loginForm.invalid || this.isSubmitting()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const {email, password} = this.loginForm.getRawValue();
    this.loginStore
      .login({email: email.trim(), password})
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((response) => {
        if (!response.success) {
          return;
        }

        this.navigateBasedOnRole(response.role);
      });
  }

  private navigateBasedOnRole(role: string | number | null | undefined): void {
    console.log('Role from backend:', role);

    const roleValue = String(role ?? '').toUpperCase().trim();

    if (roleValue === 'ADMIN' || roleValue === '0') {
      console.log('Detected Admin - Navigating to /admin/people');
      void this.router.navigate(['/admin/people']);
    }
    else if (roleValue === 'PROFESSOR' || roleValue === '1') {
      void this.router.navigate(['/professor-courses']);
    }
    else if (roleValue === 'STUDENT' || roleValue === '2') {
      void this.router.navigate(['/student-courses']);
    }
    else {
      console.log('Detected other - Navigating to /error');
      void this.router.navigate(['/error']);
    }
  }
}
