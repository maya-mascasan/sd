import { inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, finalize, Observable, of, tap } from 'rxjs';
import { LoginRequest, LoginResponse, LoginService } from '../../services/login.service';
import { Router } from '@angular/router'; // <--- Add this

interface AuthSnapshot {
  isAuthenticated: boolean;
  role: string | null;
}

const STORAGE_KEY = 'demo-app-auth';

@Injectable({ providedIn: 'root' })
export class LoginStore {
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router); // <--- Add this
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isAuthenticated = signal(false);
  readonly role = signal<string | null>(null);


  // ✅ Add these two signals!
  readonly name = signal<string | null>(null);
  readonly email = signal<string | null>(null);

  constructor() {
    this.restoreAuthState();
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    return this.loginService.login(request).pipe(
      tap((response) => { if (response.success) {
      this.email.set(request.email);
      // If your backend doesn't return the name yet, we can default it
      // to the part before the '@' for now:
      this.name.set(request.email.split('@')[0]);
    }
    this.applyResponse(response);
  }),
      catchError((error: unknown) => {
        const response = this.normalizeError(error);
        this.applyResponse(response);
        return of(response);
      }),
      finalize(() => this.isSubmitting.set(false)),
    );
  }

  logout(): void {
    this.clearSession();
  }

  private applyResponse(response: LoginResponse): void {
    if (response.success) {
      this.isAuthenticated.set(true);
      this.role.set(response.role);
      this.errorMessage.set(null);
      this.persistAuthState();

      // Inside login.store.ts -> applyResponse
      const role = response.role?.toLowerCase();

      if (role === 'admin') {
        // ✅ Update this to match your app.routes.ts
        void this.router.navigate(['/admin/people']);
      } else if (role === 'professor') {
        void this.router.navigate(['/professor-courses']);
      } else if(role == 'student') {
        void this.router.navigate(['/student-courses']);
      } else {
        void this.router.navigate(['/error']);
      }
      return;
    }

    this.clearSession(response.errorMessage ?? 'Login failed. Please try again.');
  }

  private normalizeError(error: unknown): LoginResponse {
    if (error instanceof HttpErrorResponse && error.error) {
      const maybeError = error.error as Partial<LoginResponse>;
      if (typeof maybeError.success === 'boolean') {
        return {
          success: maybeError.success,
          role: maybeError.role ?? null,
          errorMessage:
            maybeError.errorMessage ??
            (error.status === 401
              ? 'Invalid email or password.'
              : 'Unable to complete login. Please try again.'),
        };
      }
    }

    return {
      success: false,
      role: null,
      errorMessage: 'Unable to complete login. Please try again.',
    };
  }

  private restoreAuthState(): void {
    const stored = sessionStorage.getItem(STORAGE_KEY);
    if (!stored) {
      return;
    }

    try {
      const snapshot = JSON.parse(stored) as AuthSnapshot;
      this.isAuthenticated.set(snapshot.isAuthenticated);
      this.role.set(snapshot.role ?? null);
    } catch {
      this.clearSession();
    }
  }

  private persistAuthState(): void {
    const snapshot: AuthSnapshot = {
      isAuthenticated: this.isAuthenticated(),
      role: this.role(),
    };

    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));
  }

  private clearSession(errorMessage: string | null = null): void {
    this.isAuthenticated.set(false);
    this.role.set(null);
    this.errorMessage.set(errorMessage);
    sessionStorage.removeItem(STORAGE_KEY);
  }
}
