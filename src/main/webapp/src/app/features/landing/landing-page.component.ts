import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
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
export class LandingPageComponent {
  private readonly authService = inject(AuthService);

  login(): void {
    this.authService.login();
  }
}
