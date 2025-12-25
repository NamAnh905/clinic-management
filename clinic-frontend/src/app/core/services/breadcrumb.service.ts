import { Injectable, inject } from '@angular/core';
import { Router, NavigationEnd, ActivatedRoute, Data } from '@angular/router';
import { BehaviorSubject, filter } from 'rxjs';
import { MenuItem } from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  // Subject lưu trữ danh sách breadcrumb hiện tại
  private _breadcrumbs$ = new BehaviorSubject<MenuItem[]>([]);

  // Observable để Header component subscribe
  breadcrumbs$ = this._breadcrumbs$.asObservable();

  constructor() {
    // Lắng nghe sự kiện NavigationEnd để biết khi nào trang đã đổi xong
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      const root = this.activatedRoute.root;
      const breadcrumbs = this.createBreadcrumbs(root);
      this._breadcrumbs$.next(breadcrumbs);
    });
  }

  /**
   * Logic đệ quy để quét qua cây Routing và lấy thông tin breadcrumb
   */
  private createBreadcrumbs(route: ActivatedRoute, url: string = '', breadcrumbs: MenuItem[] = []): MenuItem[] {
    const children: ActivatedRoute[] = route.children;

    // Nếu không còn con, trả về kết quả cuối cùng
    if (children.length === 0) {
      return breadcrumbs;
    }

    for (const child of children) {
      const routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');

      if (routeURL !== '') {
        url += `/${routeURL}`;
      }

      // Lấy label từ thuộc tính 'breadcrumb' trong phần data của Routes
      const label = child.snapshot.data['breadcrumb'];

      if (label !== null && label !== undefined) {
        // Kiểm tra xem đã có breadcrumb này chưa để tránh trùng lặp
        const isDuplicate = breadcrumbs.some(b => b.label === label);
        if (!isDuplicate) {
          breadcrumbs.push({
            label: label,
            routerLink: url
          });
        }
      }

      // Tiếp tục đệ quy xuống cấp con tiếp theo
      return this.createBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }
}
