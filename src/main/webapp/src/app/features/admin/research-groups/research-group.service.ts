import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ResearchGroup,
  ResearchGroupImportResult,
  BatchAssignResult,
} from './research-group.model';

@Injectable({
  providedIn: 'root',
})
export class ResearchGroupService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/v2/research-groups`;

  getAll(search?: string): Observable<ResearchGroup[]> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<ResearchGroup[]>(this.apiUrl, { params });
  }

  create(researchGroup: Partial<ResearchGroup>): Observable<ResearchGroup> {
    return this.http.post<ResearchGroup>(this.apiUrl, researchGroup);
  }

  update(id: string, researchGroup: Partial<ResearchGroup>): Observable<ResearchGroup> {
    return this.http.put<ResearchGroup>(`${this.apiUrl}/${id}`, researchGroup);
  }

  archive(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  importFromCsv(file: File): Observable<ResearchGroupImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ResearchGroupImportResult>(`${this.apiUrl}/import`, formData);
  }

  batchAssignPositions(): Observable<BatchAssignResult> {
    return this.http.post<BatchAssignResult>(`${this.apiUrl}/batch-assign-positions`, {});
  }

  deleteAll(): Observable<{ deleted: number }> {
    return this.http.delete<{ deleted: number }>(this.apiUrl);
  }
}
