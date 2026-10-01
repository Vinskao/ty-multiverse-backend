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
| Controller + security rules | `@WebMvcTest` + `@ContextConfiguration(classes = {Controller, SecurityConfig})` + `@MockBean` service (plain `@WebMvcTest` loads `TYMBackendApplication` and fails on the websocket exporter) (see `AiTokenUsageSecurityWebTest`) |
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

## Reference tests reviewed as examples

- `core/config/security/InternalWriteTokenFilterTest` – filter, no Spring.
- `module/ai_usage/controller/AiTokenUsageSecurityWebTest` – web slice with the real security chain.

## Known gaps (write tests here first when touching these)

Other controllers, remaining `SecurityConfig` rules, `AiTokenUsageService`,
`learn` and `resource` modules, DAOs, message producers. Coverage tooling (JaCoCo) is not set up.
