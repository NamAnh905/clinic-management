import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {

  // URL API của bạn (Sửa lại port nếu cần)
  private apiUrl = `${environment.apiUrl}/chat/ask`;

  constructor(private http: HttpClient) { }

  sendMessage(question: string): Observable<any> {
    return this.http.post<any>(this.apiUrl, { question: question });
  }
}
