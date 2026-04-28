import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, catchError, EMPTY, tap } from 'rxjs';

export interface Course {
  id: string;
  name: string;
  // Add other properties based on your backend Course model
}

interface AuthData {
  userId: string;
  token: string;
  role: string;
}
@Injectable()
export class CourseListStore {
  private readonly http = inject(HttpClient);

  // State
  private readonly coursesSubject = new BehaviorSubject<Course[]>([]);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  // Selectors
  readonly courses = this.coursesSubject.asObservable();
  readonly loading = this.loadingSubject.asObservable();

  // Columns to display in the Material Table
  readonly displayedColumns: string[] = ['name', 'actions'];


  load(): void {
    this.loadingSubject.next(true);

    // 1. Get the token from session storage (where your login page saved it)
    const token = sessionStorage.getItem('token');

    // 2. Attach it to the Authorization header
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    // 3. Fetch data
    this.http.get<Course[]>('http://localhost:8080/courses', { headers }).pipe(
      tap((courses) => {
        this.coursesSubject.next(courses);
        this.loadingSubject.next(false);
      }),
      catchError((error) => {
        console.error('Error fetching courses:', error);
        this.loadingSubject.next(false);
        return EMPTY;
      })
    ).subscribe();
  }

  enroll(courseId: string): void {
    // 1. Get the raw string from storage
    const rawData = sessionStorage.getItem('demo-app-auth');

    // 2. If it doesn't exist, stop here
    if (!rawData) {
      console.error("Not logged in!");
      return;
    }

    // 3. Convert the string to a real object using our Interface
    const authData = JSON.parse(rawData) as AuthData;
    const studentId = authData.userId;
    const token = authData.token;

    // 4. Double check we actually have a studentId
    if (!studentId) {
      alert("Error: User ID not found in session. Please log in again.");
      return;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    // 5. Send the request
    this.http.post(`http://localhost:8080/person/${studentId}/enroll/${courseId}`, {}, { headers })
      .pipe(
        tap(() => {
          alert('Successfully enrolled!');
          this.load();
        }),
        catchError((err) => {
          alert('Enrollment failed.');
          return EMPTY;
        })
      ).subscribe();
  }

}
