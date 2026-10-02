# HTTP API test coverage (66 endpoints, 12 controllers)

Every endpoint is covered by a per-controller functional test (`*WebTest`, happy path + validation +
error mapping) and by the authorization matrix in `core/config/security/SecurityRulesWebTest`.
RabbitMQ-enabled (202 Accepted) behavior is covered by the `*AsyncWebTest` classes.

| Controller | Endpoints | Functional test | Async test |
|---|---|---|---|
| WeaponController `/weapons` | 15 | `WeaponControllerWebTest` | `WeaponControllerAsyncWebTest` |
| LearnController `/learn` | 11 | `LearnControllerWebTest` | – |
| AuthController `/auth` | 8 | `AuthControllerWebTest` | – |
| PeopleController `/people` | 7 | `PeopleControllerWebTest` | `PeopleControllerAsyncWebTest` |
| AiTokenUsageController `/ai-usage` | 5 | `AiTokenUsageSecurityWebTest` (+ `AiTokenUsageControllerTest`) | – |
| GalleryController `/gallery` | 5 | `GalleryControllerWebTest` | – |
| PeopleImageController `/people-images` | 5 | `PeopleImageControllerWebTest` | – |
| KeycloakController `/keycloak` | 3 | `KeycloakControllerWebTest` | – |
| WeaponDamageController `/people/*Damage*` | 2 | `WeaponDamageControllerWebTest` | `PeopleControllerAsyncWebTest` |
| FileUploadController `/ckeditor` | 2 | `FileUploadControllerWebTest` | – |
| JavaDocController `/docs` | 2 | `JavaDocControllerWebTest` | – |
| ResourceController `/resources` | 1 | `ResourceControllerWebTest` | – |

Not HTTP, not covered: `MetricsWSController` (WebSocket).

## Known-bug (red) tests – run with `./mvnw test -DexcludedGroups=none`

| Test | Root cause |
|---|---|
| `WeaponControllerWebTest.updateAttributes_Should_Return404_When_WeaponMissing` | Controller throws a bare `RuntimeException` (no `@ControllerAdvice`) → 500 instead of 404. |

## Fixed via TDD (2026-10-02)

The authorization matrix (`SecurityRulesWebTest`) was written red first and drove these `SecurityConfig` fixes:

- `requestMatchers("GET", "/x/**")` → `requestMatchers(HttpMethod.GET, "/x/**")` (the string was a path, so every method was public).
- `@EnableMethodSecurity` added so `@PreAuthorize` on `AuthController` / `PeopleImageController` is enforced.
- `hasRole('manage-users')` delete-all rules moved before the generic write rules (first match wins).
- `InternalWriteTokenFilter` now covers `POST /weapons/**` and `/people-images/**` (it missed `/weapons/insert-multiple`).
- `POST /ckeditor/get-content` stays public explicitly (the frontend reads content through it).
- `GET /people-images/**` is no longer `permitAll`; it follows `@PreAuthorize` (the frontend does not call this API).
