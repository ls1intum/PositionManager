import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../core/auth';

@Component({
  selector: 'app-landing-page',
  imports: [ButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="landing-container">
      <h1>StaffPlan</h1>
      <p>Welcome to StaffPlan - Manage your staff positions efficiently.</p>
      <p-button label="Login" (onClick)="login()" />
    </div>
  `,
  styles: `
    .landing-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 80vh;
      text-align: center;
      padding: 2rem;
    }

    h1 {
      font-size: 2.5rem;
      margin-bottom: 1rem;
    }

    p {
      margin-bottom: 1.5rem;
      color: var(--text-color-secondary);
    }
  `,
})
export class LandingPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    // Redirect authenticated users to positions
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/positions']);
    }
  }

  login(): void {
    this.authService.login();
  }
}
