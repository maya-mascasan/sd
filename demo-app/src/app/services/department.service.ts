import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Department, DepartmentCreateDTO } from '../models/department.model';
@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/department';

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(this.apiUrl);
  }

  addDepartment(dto: DepartmentCreateDTO): Observable<Department> {
    return this.http.post<Department>(this.apiUrl, dto);
  }

  updateDepartment(id: string, dto: DepartmentCreateDTO): Observable<Department> {
    return this.http.put<Department>(`${this.apiUrl}/${id}`, dto);
  }

  deleteDepartment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
