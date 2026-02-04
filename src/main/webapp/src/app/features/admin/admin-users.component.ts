import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { Checkbox } from 'primeng/checkbox';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { Tooltip } from 'primeng/tooltip';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { UserService, UserDTO, SecurityStore } from '../../core/security';

const AVAILABLE_ROLES = ['admin', 'job_manager', 'professor', 'employee'] as const;

const ROLE_LABELS: Record<string, string> = {
  admin: 'Administrator',
  job_manager: 'Stellenverwalter',
  professor: 'Professor',
  employee: 'Mitarbeiter',
};

const ROLE_OPTIONS = [
  { label: 'Alle Rollen', value: null },
  { label: 'Administrator', value: 'admin' },
  { label: 'Stellenverwalter', value: 'job_manager' },
  { label: 'Professor', value: 'professor' },
  { label: 'Mitarbeiter', value: 'employee' },
];

@Component({
  selector: 'app-admin-users',
  imports: [
    FormsModule,
    DatePipe,
    TableModule,
    Checkbox,
    Button,
    Tag,
    Tooltip,
    InputText,
    Select,
    IconField,
    InputIcon,
  ],
  template: `
    <div class="admin-users-page">
      <div class="page-header">
        <div class="header-left">
          <h2>Benutzerverwaltung</h2>
          <p-iconfield class="search-field">
            <p-inputicon styleClass="pi pi-search" />
            <input
              pInputText
              type="text"
              placeholder="Suchen..."
              [ngModel]="searchTerm()"
              (ngModelChange)="onSearchChange($event)"
            />
          </p-iconfield>
          <p-select
            [options]="roleOptions"
            [ngModel]="filterRole()"
            (ngModelChange)="onRoleFilterChange($event)"
            optionLabel="label"
            optionValue="value"
            placeholder="Rolle filtern"
            styleClass="role-filter"
          />
        </div>
        <p-button
          label="Aktualisieren"
          icon="pi pi-refresh"
          [outlined]="true"
          (onClick)="loadUsers()"
        />
      </div>

      <div class="table-container">
        <p-table
          [value]="users()"
          [lazy]="true"
          [paginator]="true"
          [rows]="pageSize()"
          [totalRecords]="totalRecords()"
          [rowsPerPageOptions]="[10, 20, 50, 100]"
          [loading]="loading()"
          [scrollable]="true"
          scrollHeight="flex"
          (onLazyLoad)="onLazyLoad($event)"
        >
          <ng-template #header>
            <tr>
              <th>Name</th>
              <th>Kennung</th>
              <th>E-Mail</th>
              <th>Letzter Login</th>
              <th>Rollen</th>
              <th>Aktionen</th>
            </tr>
          </ng-template>
          <ng-template #body let-user>
            <tr>
              <td>{{ user.firstName }} {{ user.lastName }}</td>
              <td>{{ user.universityId }}</td>
              <td>{{ user.email }}</td>
              <td>
                @if (user.lastLoginAt) {
                  <span
                    class="login-date"
                    [pTooltip]="(user.lastLoginAt | date: 'dd.MM.yyyy HH:mm:ss') ?? ''"
                  >
                    {{ user.lastLoginAt | date: 'dd.MM.yyyy' }}
                  </span>
                } @else {
                  <p-tag
                    value="Nie angemeldet"
                    severity="secondary"
                    pTooltip="Benutzer wurde importiert, hat sich aber noch nie angemeldet"
                  />
                }
              </td>
              <td>
                <div class="roles-container">
                  @for (role of availableRoles; track role) {
                    <div class="role-checkbox">
                      <p-checkbox
                        [binary]="true"
                        [ngModel]="hasRole(user, role)"
                        (ngModelChange)="toggleRole(user, role, $event)"
                        [inputId]="user.id + '-' + role"
                      />
                      <label [for]="user.id + '-' + role">{{ formatRole(role) }}</label>
                    </div>
                  }
                </div>
              </td>
              <td>
                <p-button
                  label="Speichern"
                  icon="pi pi-check"
                  size="small"
                  [disabled]="!hasChanges(user)"
                  (onClick)="saveUser(user)"
                />
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="6" class="empty-message">Keine Benutzer gefunden.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    </div>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
    }

    .admin-users-page {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      padding: 1rem;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
      margin-bottom: 1rem;

      h2 {
        margin: 0;
      }

      .header-left {
        display: flex;
        align-items: center;
        gap: 1rem;
        flex-wrap: wrap;
      }

      .search-field {
        input {
          width: 250px;
        }
      }
    }

    :host ::ng-deep .role-filter {
      width: 180px;
    }

    .table-container {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      border: 1px solid var(--p-surface-200);
      border-radius: 8px;
      overflow: hidden;
    }

    .roles-container {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .role-checkbox {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .login-date {
      color: var(--p-text-color);
      cursor: default;
    }

    .empty-message {
      text-align: center;
      padding: 2rem;
      color: var(--p-text-muted-color);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUsersComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly securityStore = inject(SecurityStore);
  private readonly destroyRef = inject(DestroyRef);

  private readonly searchSubject = new Subject<string>();

  readonly users = signal<UserDTO[]>([]);
  readonly loading = signal(false);
  readonly totalRecords = signal(0);
  readonly pageSize = signal(20);
  readonly currentPage = signal(0);
  readonly searchTerm = signal('');
  readonly filterRole = signal<string | null>(null);

  readonly availableRoles = AVAILABLE_ROLES;
  readonly roleOptions = ROLE_OPTIONS;

  private pendingChanges = new Map<string, Set<string>>();

  ngOnInit(): void {
    // Set up debounced search
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((term) => {
        this.searchTerm.set(term);
        this.currentPage.set(0);
        this.loadUsers();
      });

    this.loadUsers();
  }

  onSearchChange(term: string): void {
    this.searchSubject.next(term);
  }

  onRoleFilterChange(role: string | null): void {
    this.filterRole.set(role);
    this.currentPage.set(0);
    this.loadUsers();
  }

  onLazyLoad(event: TableLazyLoadEvent): void {
    const page = event.first !== undefined ? Math.floor(event.first / (event.rows ?? 20)) : 0;
    const size = event.rows ?? 20;

    this.currentPage.set(page);
    this.pageSize.set(size);
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService
      .getAllUsers({
        page: this.currentPage(),
        size: this.pageSize(),
        search: this.searchTerm() || undefined,
        role: this.filterRole() || undefined,
      })
      .subscribe({
        next: (response) => {
          this.users.set(response.content);
          this.totalRecords.set(response.totalElements);
          // Initialize pending changes with current roles
          response.content.forEach((user) => {
            this.pendingChanges.set(user.id, new Set(user.roles));
          });
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
        },
      });
  }

  hasRole(user: UserDTO, role: string): boolean {
    return this.pendingChanges.get(user.id)?.has(role) ?? false;
  }

  toggleRole(user: UserDTO, role: string, checked: boolean): void {
    const roles = this.pendingChanges.get(user.id) ?? new Set<string>();
    if (checked) {
      roles.add(role);
    } else {
      roles.delete(role);
    }
    this.pendingChanges.set(user.id, roles);
  }

  hasChanges(user: UserDTO): boolean {
    const pending = this.pendingChanges.get(user.id);
    if (!pending) return false;

    const original = new Set(user.roles);
    if (pending.size !== original.size) return true;

    for (const role of pending) {
      if (!original.has(role)) return true;
    }
    return false;
  }

  saveUser(user: UserDTO): void {
    const roles = Array.from(this.pendingChanges.get(user.id) ?? []);
    this.userService.updateUserRoles(user.id, roles).subscribe({
      next: (updatedUser) => {
        // Update the user in the list
        this.users.update((users) => users.map((u) => (u.id === updatedUser.id ? updatedUser : u)));
        // Update pending changes to match saved state
        this.pendingChanges.set(updatedUser.id, new Set(updatedUser.roles));

        // If the current user's roles were updated, refresh the page to update permissions
        if (updatedUser.id === this.securityStore.user()?.id) {
          window.location.reload();
        }
      },
      error: (err) => {
        console.error('Failed to update user roles:', err);
      },
    });
  }

  formatRole(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }
}
