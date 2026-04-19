import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Course, CourseCreateDTO } from '../models/course.model';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/course';

  getCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl);
  }

  addCourse(dto: CourseCreateDTO): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, dto);
  }

  deleteCourse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  update(id: string, courseDto: CourseCreateDTO): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/${id}`, courseDto);
  }
}
