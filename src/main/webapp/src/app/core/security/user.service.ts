import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserDTO {
  id: string;
  universityId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  lastLoginAt: string | null;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface UserSearchParams {
  page?: number;
  size?: number;
  search?: string;
  role?: string;
}

export interface CreateUserDTO {
  universityId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/v2/users`;

  getAllUsers(params: UserSearchParams = {}): Observable<PagedResponse<UserDTO>> {
    let httpParams = new HttpParams()
      .set('page', (params.page ?? 0).toString())
      .set('size', (params.size ?? 20).toString());

    if (params.search) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params.role) {
      httpParams = httpParams.set('role', params.role);
    }

    return this.http.get<PagedResponse<UserDTO>>(this.apiUrl, { params: httpParams });
  }

  updateUserRoles(userId: string, roles: string[]): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.apiUrl}/${userId}/roles`, roles);
  }

  createUser(dto: CreateUserDTO): Observable<UserDTO> {
    return this.http.post<UserDTO>(this.apiUrl, dto);
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }
}
