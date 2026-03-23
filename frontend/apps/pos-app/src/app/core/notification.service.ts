import { Injectable, inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private toastr = inject(ToastrService);

  success(message: string, title?: string) {
    this.toastr.success(message, title, {
      timeOut: 1500,
      positionClass: 'toast-bottom-right'
    });
  }

  error(message: string, title?: string) {
    this.toastr.error(message, title, {
      timeOut: 3000,
      positionClass: 'toast-bottom-right'
    });
  }

  info(message: string, title?: string) {
    this.toastr.info(message, title);
  }

  warning(message: string, title?: string) {
    this.toastr.warning(message, title);
  }
}