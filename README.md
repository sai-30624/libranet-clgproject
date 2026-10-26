# LibraNet

A web library where students find, upload, download and bookmark course material, with an admin dashboard for usage statistics and user management.

**Stack:** Spring Boot 3.3 · Java 21 · Spring Security + JWT · Spring Data JPA · MySQL · React 19 · TypeScript · Vite · TanStack Query · Tailwind

<!-- TODO: add the live demo link and a screenshot once the deployment is verified:
**Live demo:** https://...
![Home page](docs/screenshots/home.png)
-->

## Features

- Sign up and log in with JWT; `USER` and `ADMIN` roles enforced on the server
- Browse resources with text search, a category filter and pagination
- Upload resources (authenticated users) and download files, with download counts
- Bookmarks and a profile page (change name and password)
- Admin dashboard: usage statistics and enabling or disabling users

## Architecture

```
React SPA (Vite)  ──HTTPS/JSON──>  Spring Boot REST API  ──JPA──>  MySQL
     │  JWT in Authorization header        │
     └─────────────────────────────────────┴── uploaded files on local disk (./uploads)
```

The backend is organised by feature: `auth` (users, login, bookmarks), `resource` (resources, categories, tags, reviews), `file` (downloads), `admin`, plus `config` (security, JWT, data source) and `exception` (one handler that turns errors into consistent JSON responses).

A request to a protected endpoint passes through `JwtAuthFilter`, which validates the token and loads the user. Then Spring Security's rules in `SecurityConfig` check the role before the controller runs.

## Run locally

Prerequisites: Java 21, Node 20+, MySQL 8.

1. Set the environment variables listed in [`backend/.env.example`](backend/.env.example). Spring Boot does not read `.env` files by itself, so export them in your shell or IDE run configuration. `APP_JWT_SECRET` is required (32+ bytes):
   ```bash
   export APP_JWT_SECRET="$(openssl rand -base64 48)"
   export SPRING_DATASOURCE_PASSWORD=your-local-mysql-password
   export APP_SEED_ADMIN_PASSWORD=choose-a-dev-admin-password
   ```
2. Start the API (port 8081):
   ```bash
   cd backend && ./mvnw spring-boot:run
   ```
   Locally the `dev` profile is active by default. It **wipes and reseeds** demo data on every start, and creates `admin@libranet.com` with `APP_SEED_ADMIN_PASSWORD`.
3. Start the UI (port 5173):
   ```bash
   cd frontend && npm ci && npm run dev
   ```
   Set `VITE_API_URL` (for example `http://localhost:8081/api`) if the UI is not served behind the same host as the API.

## Tests

```bash
cd backend && ./mvnw test
```

`AuthSecurityTest` runs against an in-memory H2 database and checks that:

- clients cannot register as admin;
- regular users get 403 from admin endpoints;
- passwords cannot be reset without logging in;
- changing a password requires the current one;
- missing or forged tokens are rejected.

GitHub Actions runs the backend tests and the frontend build on every push.

## Deployment

`render.yaml` describes a static frontend and a Docker backend on Render. Set these on the host:

- `SPRING_PROFILES_ACTIVE=prod` (disables the data seeder)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_CORS_ALLOWED_ORIGINS`, set to your frontend's URL

## Security notes

- Passwords are hashed with BCrypt, and authentication uses stateless JWTs that expire after 24 hours.
- Roles are assigned on the server only. Public sign-up always creates a regular user.
- Secrets come only from environment variables; the app refuses to start without a strong JWT secret.
- CORS is limited to configured origins, and error responses never include internal exception messages.

## Limitations

- No email verification and no email-based password reset yet.
- Uploaded files live on the server's disk. Use a persistent disk (configured in `render.yaml`) or object storage in production.
- The schema is managed by Hibernate `ddl-auto: update`. Versioned migrations (Flyway) would be safer.
- The JWT is stored in `localStorage`, which is simple but exposed to any XSS bug in the frontend.

## License

No license has been chosen yet. All rights reserved by the author until one is added.
