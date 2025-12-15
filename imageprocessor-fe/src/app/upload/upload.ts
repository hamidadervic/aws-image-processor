import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment.development';
import { Subscription, switchMap } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-upload',
  imports: [CommonModule],
  templateUrl: './upload.html',
  styleUrl: './upload.css',
})
export class Upload {
  protected subs?: Subscription;
  private http = inject(HttpClient);

  upload(event: any): void {
    const file = event?.target?.files[0];

    if (!file) return;

    this.subs = this.http
      .get<{ url: string }>(
        `${environment.getPresignedUrlUrl}?filename=${file.name}&contentType=${file.type}`
      )
      .pipe(
        switchMap((response: { url: string }) => {
          return this.http.put(response.url, file, {
            headers: { 'Content-Type': file?.type ?? "image/jpeg" },
          })
        })
      ).subscribe({});
  }
}
