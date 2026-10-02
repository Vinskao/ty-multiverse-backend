# TDD Workflow (mandatory for all agents and contributors)

Every behavior change in `ty-multiverse-backend` follows Red → Green → Refactor.
No production code is written before a failing test that demands it.

## The loop

1. **Red** – Write one test for the new/changed behavior. Run it and confirm it
   fails *for the right reason* (an assertion failure – not a compile error or a missing bean):
   `./mvnw test -Dtest=FooTest#bar_Should_X_When_Y`
2. **Green** – Write the minimum production code to pass. Run that test, then its class.
3. **Refactor** – Clean up with tests green, then run the full suite: `./mvnw test`.
4. Repeat per behavior.

A new test that passes immediately proves nothing: if it does, either the behavior
already exists (say so) or the test is wrong (fix it).

**Bug fixes:** first write a test that reproduces the bug (red), then fix it.
**Security rules** (auth, tokens, `SecurityConfig`, filters): test denial paths
(missing / wrong / blank token, wrong method, read endpoints) as well as success.

## Where tests go (mirror `src/main`)

| Layer | Style |
|---|---|
| Service / util | JUnit 5 + `@ExtendWith(MockitoExtension.class)`, mock DAOs |
| Filter | `MockHttpServletRequest` + `MockFilterChain`, no Spring context |
| Controller + security rules | `@WebMvcTest` + `@ContextConfiguration(classes = {Controller, SecurityConfig})` + `@MockBean` service (plain `@WebMvcTest` loads `TYMBackendApplication` and fails on the websocket exporter) (see `AiTokenUsageSecurityWebTest`, `WeaponControllerWebTest`) |
| DAO / SQL | `@DataJpaTest` with H2 |

Avoid `@SpringBootTest` unless truly cross-layer. Never hit real external services.

## Conventions

- Name: `methodName_Should_ExpectedBehavior_When_Condition`.
- One behavior per test; Arrange / Act / Assert; no logic in tests.
- Deterministic: no sleeps, no wall-clock/network dependence.
- Assert observable behavior (responses, state, security context) over `verify(...)` on mocks.
- Never weaken, delete or `@Disabled` a test to get green. If a test is wrong, say why.

## Definition of done

- [ ] A test failed before the production change (state which one and why).
- [ ] `./mvnw test` is green – paste the `Tests run:` summary.
- [ ] Each new public behavior and each error/denial branch has a test.
- [ ] No unrelated test modified.

## Red tests for confirmed bugs (`known-bug`)

When a test proves a production bug and the fix is not part of the current task, keep the test red
but tag it `@Tag("known-bug")` with a javadoc explaining the root cause. `pom.xml` skips that tag by
default so `./mvnw test` stays green; run the red ones with:

```bash
./mvnw test -DexcludedGroups=none          # everything, including known bugs
./mvnw test -DexcludedGroups=none -Dtest=SecurityRulesWebTest
```

Fixing a bug = make its `known-bug` test pass, then delete the tag. Never delete the test.

## Test support (`src/test/java/tw/com/tymbackend/support`)

- `@SecuredWebSlice` – properties that let the real `SecurityConfig` load in a web slice.
- `TestAuth.admin()` / `TestAuth.user()` – Keycloak-style JWTs (`ROLE_manage-users` / `ROLE_user`);
  `TestAuth.INTERNAL_HEADER` / `INTERNAL_TOKEN` for the `X-Internal-Token` path.

Per-controller `*WebTest` classes send a JWT and verify controller behavior. The authorization matrix
for every endpoint lives in one place: `core/config/security/SecurityRulesWebTest`.

## Where the 66 endpoints are tested

See [API_TEST_COVERAGE.md](API_TEST_COVERAGE.md).

## Known gaps

Services without tests (`AiTokenUsageService`, `AuthService`, `LearnService`, `CompanyProductMappingService`,
`KeycloakController` internals beyond HTTP), DAOs/repositories, message producers/consumers, WebSocket
(`MetricsWSController`). Coverage tooling (JaCoCo) is not set up.
