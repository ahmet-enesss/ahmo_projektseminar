import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  username: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';
  private readonly CREDENTIALS_KEY = 'auth_credentials';
  private readonly USERNAME_KEY = 'auth_username';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasCredentials());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<string | null>(this.getStoredUsername());
  public currentUser$ = this.currentUserSubject.asObservable();

  login(credentials: LoginRequest): Observable<LoginResponse> {
    // Erstelle Basic Auth Header
    const authHeader = this.createBasicAuthHeader(credentials.username, credentials.password);
    
    // Teste die Credentials mit einem einfachen Request
    return this.http.get<any>(`${this.baseUrl}/auth/validate`, {
      headers: { 'Authorization': authHeader }
    }).pipe(
      tap(() => {
        // Speichere Credentials (base64 encoded)
        this.storeAuthData(credentials.username, credentials.password);
        this.isAuthenticatedSubject.next(true);
        this.currentUserSubject.next(credentials.username);
      }),
      map(() => ({
        username: credentials.username,
        message: 'Anmeldung erfolgreich'
      })),
      catchError(this.handleError)
    );
  }

  logout(): void {
    this.clearAuthData();
    this.isAuthenticatedSubject.next(false);
    this.currentUserSubject.next(null);
  }

  getBasicAuthHeader(): string | null {
    const credentials = this.getStoredCredentials();
    if (!credentials) {
      return null;
    }
    return this.createBasicAuthHeader(credentials.username, credentials.password);
  }

  getUsername(): string | null {
    return this.getStoredUsername();
  }

  isLoggedIn(): boolean {
    return this.hasCredentials();
  }

  private createBasicAuthHeader(username: string, password: string): string {
    const credentials = btoa(`${username}:${password}`);
    return `Basic ${credentials}`;
  }

  private hasCredentials(): boolean {
    return !!localStorage.getItem(this.CREDENTIALS_KEY);
  }

  private getStoredUsername(): string | null {
    return localStorage.getItem(this.USERNAME_KEY);
  }

  private getStoredCredentials(): { username: string; password: string } | null {
    const credentialsJson = localStorage.getItem(this.CREDENTIALS_KEY);
    if (!credentialsJson) {
      return null;
    }
    try {
      return JSON.parse(credentialsJson);
    } catch {
      return null;
    }
  }

  private storeAuthData(username: string, password: string): void {
    localStorage.setItem(this.CREDENTIALS_KEY, JSON.stringify({ username, password }));
    localStorage.setItem(this.USERNAME_KEY, username);
  }

  private clearAuthData(): void {
    localStorage.removeItem(this.CREDENTIALS_KEY);
    localStorage.removeItem(this.USERNAME_KEY);
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ein unbekannter Fehler ist aufgetreten.';
    if (error.status === 401) {
      errorMessage = 'Benutzername oder Passwort ist falsch.';
    } else if (error.status === 400 || error.status === 409) {
      errorMessage = error.error?.message || error.error?.error || JSON.stringify(error.error);
    }
    return throwError(() => new Error(errorMessage));
  }
}
