import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Submission {
  id: string;
  content: string;
  submissionDate: string;
  grade: number | null;
  student?: { id: string; name: string; email: string };
  assignment?: { id: string; title: string };
}

@Injectable({ providedIn: 'root' })
export class SubmissionService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/submission';

  getByAssignment(assignmentId: string): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.base}/assignment/${assignmentId}`);
  }

  getByStudent(studentId: string): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.base}/student/${studentId}`);
  }

  submit(dto: { content: string; assignmentId: string; studentId: string }): Observable<Submission> {
    return this.http.post<Submission>(this.base, dto);
  }

  grade(submissionId: string, grade: number): Observable<Submission> {
    return this.http.patch<Submission>(`${this.base}/${submissionId}/grade`, { grade });
  }
}
