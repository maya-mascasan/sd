import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Assignment {
  id: string;
  title: string;
  description: string;
  deadline: string;
  course?: { id: string; title: string };
}

export interface AssignmentCreateDTO {
  title: string;
  description: string;
  deadline: string;
  courseId: string;
}

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/assignment';

  getByCourse(courseId: string): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(`${this.base}/course/${courseId}`);
  }

  create(dto: AssignmentCreateDTO): Observable<Assignment> {
    return this.http.post<Assignment>(this.base, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
