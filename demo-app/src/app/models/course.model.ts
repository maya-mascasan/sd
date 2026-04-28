import { Department } from './department.model';

export interface Course {
  id: string;
  title: string;
  credits: number;
  department: Department;
  // Add these fields to match your backend response
  students?: { id: string; email: string; name?: string }[];
  enrolledStudents?: { id: string; email: string; name?: string }[];
}

export interface CourseCreateDTO {
  title: string;
  credits: number;
  departmentId: string;
}
