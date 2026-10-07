# Penpot 전수조사 분석 정리 (한국어)

> 작성일: 2026-10-07
> 대상 레포지토리: **https://github.com/bmshin94/penpot**
> 원본(upstream): **https://github.com/penpot/penpot**
> 분석 기준 버전: **2.17.0** / 브랜치: `develop`, `claude/eloquent-planck-mshqi6`

---

## 목차

1. [이게 뭐하는 건가](#1-이게-뭐하는-건가)
2. [폴더별 전수조사](#2-폴더별-전수조사)
3. [MCP 서버 심층 분석](#3-mcp-서버-심층-분석)
4. [AI 에이전트 운영 체계](#4-ai-에이전트-운영-체계)
5. [설치 및 사용법](#5-설치-및-사용법)
6. [플러그인 vs 스킬 vs MCP](#6-플러그인-vs-스킬-vs-mcp)
7. [API 토큰 필요 여부와 보안](#7-api-토큰-필요-여부와-보안)
8. [AI 에이전트 구축에 주는 도움](#8-ai-에이전트-구축에-주는-도움)
9. [React / PHP 로 만들 수 있는 범위](#9-react--php-로-만들-수-있는-범위)
10. [유튜브 강의 제작 계획](#10-유튜브-강의-제작-계획)
11. [수익화 아이디어 9가지](#11-수익화-아이디어-9가지)
12. [추천 로드맵](#12-추천-로드맵)
13. [참고 링크](#13-참고-링크)

---

## 1. 이게 뭐하는 건가

**Penpot** 은 Kaleidos INC(스페인)가 만든 **오픈소스 디자인 플랫폼**이다.
Figma 의 오픈소스 대안이며, 자체 호스팅으로 디자인 인프라를 완전히 소유할 수 있다.

| 항목 | 값 |
|---|---|
| 라이선스 | **MPL-2.0** (상업적 이용 가능, 수정분만 공개) |
| 버전 | 2.17.0 |
| 추적 파일 수 | 6,337 개 |
| 주 언어 | ClojureScript 856 / CLJC 255 / Clojure 248 / TypeScript 284 / Rust 104 |
| Digital Public Good | 인증됨 |
| 포크에서 추가된 것 | `CLAUDE.md` 페르소나 가이드 (PR #1) 외에는 upstream 과 동일 |

핵심 가치:

- **데이터 주권** — 내 서버에 설치, 벤더 종속 없음
- **개방 표준** — SVG / CSS / HTML / JSON
- **코드로서의 디자인** — Inspect 모드로 즉시 코드 추출
- **네이티브 Design Tokens** — 디자인·개발 단일 진실 공급원
- **MCP 서버 + 플러그인 시스템** — 워크스페이스를 프로그래밍 가능하게 만듦

### 어떨 때 쓰는가

1. 디자인 툴 자체 호스팅 (금융·공공·의료 등 보안 규제 환경)
2. 디자인 ↔ 코드 ↔ AI 양방향 워크플로 구축
3. 대규모 디자인 시스템 관리 (토큰 / 컴포넌트 / 배리언트)
4. 플러그인으로 사내 툴체인과 디자인 연결
5. MCP 서버 설계의 프로덕션 레퍼런스로 학습

---

## 2. 폴더별 전수조사

### 핵심 애플리케이션

| 폴더 | 용량 | 스택 | 역할 |
|---|---|---|---|
| `frontend/` | 163M | ClojureScript + React(rumext) + SCSS | 디자인 에디터 본체 |
| `backend/` | 6.6M | Clojure 1.12.5 + PostgreSQL + Redis | API 서버, RPC 커맨드 27 모듈 |
| `common/` | 6.9M | CLJC | 프론트·백엔드 공유 로직, 기하연산, changes 모델 |
| `render-wasm/` | 3.1M | Rust → WebAssembly | 고성능 캔버스 렌더링 엔진 |
| `exporter/` | 412K | Node + Playwright | PNG / PDF / SVG 내보내기 |
| `media-processor/` | 608K | ImageMagick | 이미지 리사이즈·변환 |
| `library/` | 200K | Clojure | `.penpot` 파일 포맷 라이브러리 |

백엔드 주요 의존성: `buddy`(암호화·JWT), `lettuce`(Redis), `reitit`(라우팅),
`yetti`(HTTP), `next.jdbc`, `HikariCP`, **Prometheus 메트릭 내장**.

### AI / 자동화 레이어

| 폴더 | 용량 | 내용 |
|---|---|---|
| `mcp/` | 1.5M | **Penpot 공식 MCP 서버** (모노레포 4 패키지) |
| `plugins/` | 3.7M | 플러그인 SDK + 예제 플러그인 12 개 |
| `.agents/` | - | AI 에이전트 스킬 24 개 + 프롬프트 |
| `.claude/` | - | `skills` → `.agents/skills` 심볼릭 링크 |
| `.serena/` | - | 계층형 메모리 그래프 (프로젝트 지식 베이스) |
| `.opencode/` | - | opencode 에이전트 설정 |

### 인프라 / 문서

| 폴더 | 내용 |
|---|---|
| `docker/` | Dockerfile 6 종 (backend, frontend, exporter, **mcp**, media-processor, storybook) + compose + devenv |
| `docs/` | 98M. Eleventy 정적 사이트 (user-guide, technical-guide, mcp, plugins) |
| `scripts/` | `ci`, `check-commit`, `paren-repair`, `nrepl-eval.mjs`, `gh.py` |
| `manage.sh` | 63KB 운영 스크립트. devenv 관리, 빌드, `start-coding-agent` 등 |
| `CHANGES.md` | 304KB 체인지로그 |
| `experiments/` | 실험용 JS / HTML |

---

## 3. MCP 서버 심층 분석

### 모노레포 구조

```
mcp/packages/
├─ common/           서버 ↔ 플러그인 공용 TypeScript 타입
├─ server/           MCP 서버 (LLM 에게 툴 제공)
├─ plugin/           Penpot 내부 브릿지 플러그인
└─ types-generator/  Plugin API 타입 자동 생성 (Python / pixi)
```

### 아키텍처

```
[AI 클라이언트 (Claude 등)]
      ↕  MCP (Streamable HTTP :4401 / Legacy SSE)
[Penpot MCP 서버]        Express 5 + @modelcontextprotocol/sdk ^1.29
      ↕  WebSocket (:4402)
[Penpot MCP 플러그인]    브라우저 안에서 실행
      ↕  Penpot Plugin API
[실제 디자인 파일]
```

서버가 디자인을 직접 만지지 못하는 이유는 디자인 파일이 **사용자 브라우저
세션 안에 열려 있기** 때문이다. 그래서 브라우저 안에 플러그인을 띄우고
WebSocket 으로 지시를 전달하는 구조를 택했다.

### LLM 에 노출되는 툴 10개

| 툴 | 역할 | 활성 조건 |
|---|---|---|
| `execute_code` | Penpot 안에서 **임의 JavaScript 실행** (가장 강력) | 항상 |
| `high_level_overview` | Penpot 개념 / 구조 설명 반환 | 항상 |
| `penpot_api_info` | Plugin API 타입 문서 조회 (`api_types.yml` 611KB) | 항상 |
| `export_shape` | 도형을 이미지로 내보내기 (VLM 시각 검증용) | 항상 |
| `import_image` | 로컬 이미지를 디자인에 삽입 | 로컬 모드 |
| `import_penpot_file` | `.penpot` 파일 임포트 | devenv |
| `cljs_repl` | ClojureScript REPL 평가 | devenv |
| `cljs_compiler_output` | 컴파일 에러 확인 | devenv |
| `clj_check_parentheses` | Clojure 괄호 검사 / 수정 | devenv |
| `read_taiga_issue` | Taiga 이슈 트래커 읽기 | devenv |

### 설계상 배울 점

1. **"만능 툴" 철학** — 도형 생성·색 변경 등 수십 개 툴을 만드는 대신
   `execute_code` 하나로 Plugin API 전체를 노출했다. Penpot 에 새 API 가
   추가돼도 MCP 서버를 수정할 필요가 없다.
2. **`storage` 객체로 세션 간 상태 유지** — 1 회차에 찾은 결과를 저장하고
   2 회차에서 이어 쓴다. 멀티턴 에이전트의 핵심 패턴.
3. **온디맨드 문서 조회로 토큰 절약** — 611KB 타입 YAML 을 프롬프트에 넣지
   않고, 타입 목록만 주고 필요할 때 `penpot_api_info` 로 조회하게 한다.
4. **비전 피드백 루프** — `export_shape` 로 결과 이미지를 AI 에 돌려줘서
   AI 가 자기 작업을 눈으로 검증한다.
5. **타입세이프 툴 추상화** — `src/Tool.ts` 의 제네릭 `Tool<TArgs>` +
   Zod 스키마 + 실행 ID 로깅. 신규 툴은 로직만 쓰면 된다.
6. **Redis pub/sub 수평 확장** — `RedisBridge.ts` + `PENPOT_MCP_REDIS_URI`
   로 멀티 인스턴스 태스크 라우팅. SaaS 화 설계가 이미 들어있다.
7. **28KB 시스템 프롬프트** — `data/initial_instructions.md` 에 좌표계,
   z-order, 읽기 전용 속성(`width`/`height` 는 `resize()` 로만 변경),
   비동기 업데이트(`await penpot.waitForLayoutUpdate()`) 등 함정을 미리 설명.

### 환경변수 (13개)

| 변수 | 설명 | 기본값 |
|---|---|---|
| `PENPOT_MCP_SERVER_HOST` | MCP 서버 바인딩 주소 | `localhost` |
| `PENPOT_MCP_SERVER_PORT` | HTTP / SSE 포트 | `4401` |
| `PENPOT_MCP_WEBSOCKET_PORT` | 플러그인 연결 WebSocket 포트 | `4402` |
| `PENPOT_MCP_REPL_PORT` | REPL 서버 포트 (개발용) | `4403` |
| `PENPOT_MCP_REPL_ENABLE` | REPL 활성화 | (unset) |
| `PENPOT_MCP_REMOTE_MODE` | 원격 모드 (파일시스템 접근 차단) | `false` |
| `PENPOT_MCP_DEVENV` | 개발환경 툴 활성화 | `false` |
| `PENPOT_MCP_TOOL_TIMEOUT_S` | 툴 호출 타임아웃 (초) | `120` |
| `PENPOT_MCP_EXPORT_SHAPE_MAX_PARALLEL_REQUESTS` | 병렬 export 제한 | `0` (무제한) |
| `PENPOT_MCP_REDIS_URI` | Redis 연결 URI (수평 확장) | (unset) |
| `PENPOT_MCP_LOG_LEVEL` | 로그 레벨 | `info` |
| `PENPOT_MCP_LOG_DIR` | 로그 파일 디렉터리 | (unset) |
| `PENPOT_MCP_PLUGIN_SERVER_HOST` | 플러그인 웹서버 바인딩 주소 | (local only) |

---

## 4. AI 에이전트 운영 체계

Penpot 팀은 AI 코딩 에이전트를 안전하게 쓰기 위한 인프라를 레포에 넣어두었다.
이 구조는 다른 프로젝트에 그대로 이식할 수 있다.

### `AGENTS.md` 하드 룰 (발췌)

- `git push`, force-push, remote 변경 **금지** — 사람이 직접 푸시
- 푸시된 커밋 amend 금지
- `CHANGES.md` 손으로 수정 금지 (릴리스 프로세스가 생성)
- 테스트 출력을 `| head`, `| grep` 으로 필터링 금지 — 실패가 숨겨진다.
  파일로 리다이렉트한 뒤 읽을 것
- 커밋 제목 70 자 이하 / 본문 76 자 래핑, `scripts/check-commit` 통과 필수
- 코드 작성 전에 `critical-info` 메모리부터 읽을 것
- 산문 작성 시 조지 오웰 6 원칙 적용 (1946)

### 스킬 24개 (`.agents/skills/`)

`create-commit`, `create-pr`, `create-issue`, `make-a-plan`, `implement-plan`,
`planner`, `review-code`, `code-review-criteria`, `review-plan`,
`plan-review-criteria`, `local-ci`, `testing`, `security-and-hardening`,
`resolve-git-conflicts`, `update-changelog`, `refine-prompt`, `nrepl-eval`,
`taiga`, `ste`, `ripgrep`, `fd-find`, `bat-cat`, `jq-json-processor`

### 메모리 그래프 (`.serena/memories/`)

```
critical-info            ← 그래프 루트, 가장 먼저 읽는다
  └─ <module>/core       ← 모듈별 핵심 메모리 (frontend/core, backend/core ...)
       └─ <topic>        ← 세부 토픽 메모리
```

`mem:<section>/<name>` 형식으로 상호 참조하며, 필요한 깊이까지만 점진적으로
읽어 들인다. 토큰을 아끼면서 정확도를 유지하는 방식이다.

### `manage.sh` 에이전트 함수

`start-coding-agent`, `write-instance-mcp-configs`, `parse-opencode-config-dir`
등으로 에이전트 인스턴스를 여러 개 띄워 병렬 개발하는 체계를 갖췄다.

---

## 5. 설치 및 사용법

### 방법 A — MCP 만 사용 (가장 쉬움, 추천)

Penpot 전체 설치 없이 클라우드 Penpot + 로컬 MCP 서버 조합.
전제 조건: Node.js 22.x

```bash
# 1. MCP 서버 + 플러그인 서버 실행
npx -y @penpot/mcp@latest
#    MCP 서버      : localhost:4401
#    플러그인 서버 : localhost:4400

# 2. MCP 클라이언트 등록
npx -y add-mcp -g -n penpot http://localhost:4401/mcp
```

Claude Desktop 은 stdio 전송만 지원하므로 프록시가 필요하다.

```json
{
  "mcpServers": {
    "penpot": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:4401/mcp", "--allow-http"]
    }
  }
}
```

설정 파일 위치:

- Windows: `%APPDATA%/Claude/claude_desktop_config.json`
- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

설정 후 앱을 **완전 종료**해야 적용된다 (창 닫기로는 부족, File → Quit).

```
3. Penpot 브라우저에서 연결
   design.penpot.app 접속 → 디자인 파일 열기
   → Plugins 메뉴 → http://localhost:4400/manifest.json 입력
   → 플러그인 UI 에서 "Connect to MCP server" 클릭
   → 상태가 "Connected to MCP server" 로 바뀌면 성공
```

### 주의할 함정 3가지

1. **플러그인 UI 를 닫으면 연결이 끊긴다.** 열어둬야 한다.
2. **Penpot 탭을 활성 상태로 유지해야 한다.** 브라우저가 비활성 탭을
   정지시키면 MCP 서버가 작업을 거부한다. Chrome 은
   `설정 → 성능 → 항상 활성 상태로 유지할 사이트` 에 추가하거나 탭을 고정한다.
3. **Chromium 142 이상의 PNA 제한** — `localhost` 접근 허용 팝업을 승인해야
   한다. Brave 는 해당 사이트의 Shield 를 끈다. 막히면 Firefox 를 쓴다.

### 방법 B — Penpot 자체 호스팅

```bash
cd penpot/docker/images
docker compose -f docker-compose.yaml up -d
```

PostgreSQL, Redis, backend, frontend, exporter 가 함께 올라간다.
Dockerfile 6 종이 있어 Kubernetes 배포도 가능하고, Elestio 원클릭도 지원한다.

### 방법 C — 소스 개발 환경

```bash
./manage.sh pull-devenv
./manage.sh create-devenv
./manage.sh run-devenv

# MCP 만 소스로 실행
cd mcp
./scripts/setup
pnpm run bootstrap    # 의존성 설치 + 빌드 + 서버 2개 실행
```

### 버전 및 모델 요구사항

- **Penpot 버전과 MCP 버전이 일치해야 한다.** 불일치 시 플러그인이 경고를
  표시한다. 최신 개발 버전을 쓰려면 `develop` 브랜치를 클론한다.
- **최상급 프론티어 모델을 권장한다.** 약한 모델이나 로컬 모델은 간단한
  작업 외에는 쓸만한 결과를 내지 못한다 (공식 README 명시).
- **비전 모델(VLM) 이 필요하다.** 많은 디자인 작업이 시각적 확인을 요구한다.

---

## 6. 플러그인 vs 스킬 vs MCP

세 가지가 모두 들어있다.

| 구분 | 위치 | 정체 |
|---|---|---|
| **MCP** | `mcp/packages/server` | 진짜 MCP 서버. SDK ^1.29, 툴 10 개, HTTP + SSE |
| **플러그인** | `mcp/packages/plugin` | MCP 의 손발. WebSocket 클라이언트 |
| **플러그인 SDK** | `plugins/` | 플러그인 개발 도구 + 예제 12 개 (MCP 와 별개) |
| **스킬** | `.agents/skills/` | Claude Code / opencode 스킬 24 개 |

```
Penpot 본체 (Clojure 앱)
   │
   ├─ Plugin API ──┬─ plugins/apps/*          (일반 플러그인 12개)
   │               └─ mcp/packages/plugin     (MCP 브릿지)
   │                         ↕ WebSocket
   │                   mcp/packages/server    (MCP 서버)
   │                         ↕ MCP
   │                     [Claude / Cursor]
   │
   └─ .agents/skills/*  (Penpot 소스 개발용 스킬 24개)
          └─ .claude/skills  (심볼릭 링크, 호환성)
```

한 줄 요약: **MCP 서버가 머리, 플러그인이 손발, 스킬은 개발자용 매뉴얼.**

주의: `.agents/skills/` 는 디자인을 조작하는 스킬이 아니다. Penpot
소스코드를 고칠 때 쓰는 개발용 스킬(커밋 작성, 코드 리뷰, Clojure 테스트)이다.

---

## 7. API 토큰 필요 여부와 보안

### MCP 사용 시에는 토큰이 필요 없다

```
일반적인 방식 : MCP 서버 → (API 토큰) → Penpot API → 디자인
Penpot 방식   : MCP 서버 → (WebSocket) → 플러그인 → 디자인
                                              ↑
                                 이미 로그인된 브라우저 세션
```

플러그인이 사용자가 이미 로그인한 브라우저 세션 안에서 돌기 때문에 인증이
자동으로 해결된다.

### 토큰이 필요한 경우

| 상황 | 토큰 |
|---|---|
| MCP 로 AI 작업 | 불필요 |
| REST API 직접 호출 (CI/CD, 백업 스크립트, 서버 간 통신) | 필요 |
| 웹훅 연동 | 필요 |
| 외부 시스템 자동화 | 필요 |

발급: Penpot → 프로필 설정 → Access tokens → 생성.
사용: `Authorization: Token <토큰>` 헤더.
백엔드 구현은 `backend/src/app/rpc/commands/access_token.clj` 와
`backend/src/app/http/access_token.clj` 에 있다.

### 보안 주의사항

- **원격 운영 시 `PENPOT_MCP_REMOTE_MODE=true` 필수** — 로컬 파일시스템
  접근을 차단한다.
- **`PENPOT_MCP_SERVER_HOST=0.0.0.0` 은 신뢰된 네트워크에서만** 사용한다.
  공식 README 도 신뢰할 수 없는 네트워크에서는 주의하라고 경고한다.
- **MCP 서버 자체에 인증이 없다.** 공개 노출하면 누구나 디자인을 조작할 수
  있다. 리버스 프록시와 인증 레이어를 반드시 앞에 둔다.
- `execute_code` 는 임의 JavaScript 를 실행한다. 외부 공개 시 더 주의한다.
- `PENPOT_MCP_DEVENV=true` 는 로컬 개발 전용이다 (REPL, 파일 임포트 활성화).
- **디자인 데이터가 AI 모델 제공사로 전송된다.** 기밀 디자인이라면 사내
  정책을 먼저 확인한다.

---

## 8. AI 에이전트 구축에 주는 도움

| 배울 포인트 | 위치 | 가치 |
|---|---|---|
| MCP 서버 정석 구현 | `packages/server/src/PenpotMcpServer.ts` | HTTP / SSE 동시 지원 |
| 타입세이프 툴 추상화 | `src/Tool.ts` | 제네릭 + Zod + 실행 ID 로깅 |
| 만능 툴 설계 철학 | `tools/ExecuteCodeTool.ts` | 툴 50 개 대신 1 개 |
| 세션 간 상태 유지 | `storage` 객체 | 멀티턴 에이전트 핵심 |
| 대용량 컨텍스트 전략 | `data/api_types.yml` 611KB | 온디맨드 조회로 토큰 절약 |
| 시스템 프롬프트 작성법 | `data/initial_instructions.md` 28KB | 함정·규칙 구조화 |
| 수평 확장 | `RedisBridge.ts` | Redis pub/sub 태스크 라우팅 |
| 태스크 상관관계 | `PluginTask.ts`, `RemotePluginTask.ts` | task ID 매칭, 타임아웃 |
| 동시성 제어 | `utils/Semaphore.ts` | 병렬 요청 제한 |
| 멀티유저 모드 | `docs/multi-user-mode.md` | 세션 격리 설계 |
| 에이전트 운영 체계 | `AGENTS.md`, `.agents/skills/` | 즉시 이식 가능 |
| 멀티에이전트 오케스트레이션 | `manage.sh:993` | 인스턴스 병렬 실행 |

### 그대로 베낄 가치가 가장 큰 3가지

**1. `Tool<TArgs>` 베이스 클래스**

```typescript
class MyTool extends Tool<MyArgs> {
  getToolName() { return "my_tool"; }
  getToolDescription() { return "..."; }
  async executeCore(args: MyArgs) { /* 로직만 작성 */ }
}
// 검증, 로깅, 에러 처리는 베이스 클래스가 처리한다
```

**2. 온디맨드 문서 조회 패턴** — 611KB 를 프롬프트에 넣으면 비용이 폭발한다.
타입 목록만 주고 필요할 때 조회하게 한다.

**3. 비전 피드백 루프** — 결과 이미지를 AI 에 돌려줘 스스로 검증하게 한다.

### 한계

- `execute_code` 는 강력하지만 샌드박스 보안이 약하다. 자체 서비스에는 추가
  격리가 필요하다.
- 브라우저 탭에 의존하므로 완전한 헤드리스 자동화가 불가능하다.
- MCP 서버에 인증이 없어 프로덕션에는 인증 레이어를 직접 추가해야 한다.

---

## 9. React / PHP 로 만들 수 있는 범위

| 만들 것 | React | PHP | 평가 |
|---|---|---|---|
| MCP 서버 대체 구현 | 가능 (쉬움) | 가능하나 비추천 | Node / TS 가 정석 |
| Penpot 플러그인 | **최적** | 불가 | 플러그인 UI 는 iframe 웹앱 |
| 플러그인 백엔드 API | - | 좋음 | Laravel 로 라이선스·결제 |
| Penpot 본체 재구현 | 비현실적 | 불가능 | 6,337 파일 + Rust WASM |
| Penpot 연동 웹서비스 | 최적 | 좋음 | REST API + 토큰 |
| AI 디자인 자동화 SaaS | 프론트 | 백엔드 | 조합이 적합 |

### React 플러그인 예시

```jsx
// 플러그인 UI (iframe 안에서 실행)
function App() {
  const applyColor = (hex) => {
    parent.postMessage({ type: "apply-color", hex }, "*");
  };
  return <button onClick={() => applyColor("#0066FF")}>적용</button>;
}
```

```js
// plugin.js (Penpot 샌드박스 안에서 실행)
penpot.ui.onMessage((msg) => {
  if (msg.type === "apply-color") {
    penpot.selection.forEach((s) => {
      s.fills = [{ fillColor: msg.hex }];
    });
  }
});
```

```json
{
  "name": "My Plugin",
  "code": "plugin.js",
  "permissions": ["content:read", "content:write", "library:read"]
}
```

타입 정의는 `plugins/libs/plugin-types/index.d.ts` 를 참고한다.
공식 예제 12 개(`plugins/apps/`) 중 `rename-layers-plugin`,
`colors-to-tokens-plugin` 이 구조 학습에 좋다.

### 자체 MCP 서버 (TypeScript)

```typescript
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";

const server = new McpServer({ name: "my-mcp", version: "1.0.0" });

server.registerTool(
  "my_tool",
  { description: "...", inputSchema: {} },
  async (args) => ({ content: [{ type: "text", text: "결과" }] })
);
```

### PHP 로 할 수 있는 것

```php
$res = Http::withHeaders(['Authorization' => 'Token '.$token])
    ->post('https://design.penpot.app/api/rpc/command/get-file',
           ['id' => $fileId]);
```

- 플러그인 라이선스 / 결제 서버 (Stripe, 토스페이먼츠 연동)
- 웹훅 수신 서버 (디자인 변경 → Slack 알림)
- 사내 디자인 포털 (에셋 검색 / 다운로드)

MCP 서버 자체는 WebSocket 과 상태 유지 때문에 PHP 로 하기 불편하다.
Node 로 MCP 를 만들고 PHP 는 비즈니스 로직에 쓰는 편이 낫다.

### 추천 조합

```
React / Next.js  → 플러그인 UI + 사용자 대시보드
Node / TypeScript → MCP 서버 (Penpot 소스 참고)
PHP / Laravel     → 결제, 라이선스, 사용자 관리, 웹훅
PostgreSQL        → 데이터
```

---

## 10. 유튜브 강의 제작 계획

### 기회 요인

- Penpot MCP 는 2025 년 Penpot Fest 에서 공개된 최신 기능이라 한국어 콘텐츠가
  거의 없다.
- "AI + 디자인 자동화" 는 현재 가장 주목받는 키워드다.
- Figma 유료화 피로감과 자체 호스팅 수요가 늘고 있다.
- MPL-2.0 이고 공식 영상 자료가 있어 저작권 문제가 없다.

### 시즌 1 — Penpot 입문 (5편, 각 10~15분)

| EP | 제목 |
|---|---|
| 1 | Figma 구독료 0원? 오픈소스 Penpot 완전정복 |
| 2 | 내 컴퓨터에 Penpot 설치하기 (Docker 10분) |
| 3 | Penpot vs Figma 솔직 비교 |
| 4 | Design Tokens 로 디자인 시스템 만들기 |
| 5 | Inspect 모드로 디자인을 코드로 바꾸기 |

### 시즌 2 — AI 연동 (6편, 메인 콘텐츠)

| EP | 제목 | 포인트 |
|---|---|---|
| 1 | Claude 가 내 디자인을 직접 만든다? MCP 충격 | 썸네일 소재 |
| 2 | Penpot MCP 서버 설치 10분 완성 | 실용성 |
| 3 | 프롬프트 한 줄로 랜딩페이지 만들기 | 바이럴 1순위 |
| 4 | 버튼 237개 색 일괄 변경, 3시간에서 10초로 | 비포·애프터 |
| 5 | React 코드를 디자인으로 역변환 | 희귀 콘텐츠 |
| 6 | MCP 트러블슈팅 10가지 | 검색 유입 최강 |

### 시즌 3 — 개발자 심화 (6편)

1. React 로 Penpot 플러그인 만들기 (Hello World)
2. 실전 플러그인: 레이어 일괄 리네이머
3. MCP 서버 직접 만들기 (Penpot 소스 분석)
4. `execute_code` 패턴: 툴 50 개를 1 개로 줄이는 설계
5. `AGENTS.md` 로 AI 코딩 에이전트 길들이기
6. Penpot 자체 호스팅 프로덕션 배포 (Docker / Kubernetes)

### 수익 모델

```
유튜브 광고 → 멤버십 월 4,900원 → 인프런·유데미 강의 99,000원
→ 전자책 19,900원 → 기업 교육 및 컨설팅 외주
```

### 제작 팁

- 썸네일: "AI 가 디자인을 직접 만든다" + 비포·애프터 분할 화면
- 첫 15 초에 프롬프트 입력 → 디자인 생성 타임랩스를 넣어 이탈을 막는다
- `mcp/resources/architecture.png` 다이어그램을 설명 자료로 쓴다
- 영상에 버전(2.17.0)을 명시하고 고정 댓글로 업데이트를 공지한다

---

## 11. 수익화 아이디어 9가지

### 법적 기반

MPL-2.0 이므로 상업적 이용, 판매, SaaS 운영이 모두 합법이다.
조건은 두 가지다.

1. 수정한 MPL 파일의 소스를 공개한다.
2. "Penpot" 상표를 공식 승인 없이 제품명에 쓰지 않는다.
   ("Penpot 기반 XX" 는 가능, "Penpot Pro" 는 불가)

### 티어 1 — 즉시 시작 가능 (투자 거의 0원)

#### 아이디어 1. Penpot 플러그인 유료 판매

PenpotHub 플러그인 생태계가 초기 단계라 선점 기회가 있다. Figma 플러그인
시장은 포화 상태지만 Penpot 은 비어 있다.

| 플러그인 | 기능 | 가격 |
|---|---|---|
| 한글 폰트 팩 | 한글 폰트 원클릭 적용 + 조합 추천 | $9 |
| AI 카피라이터 | 선택 텍스트를 LLM 으로 톤앤매너 변환 | $19 + 사용량 |
| 접근성 검사기 | WCAG 대비비·폰트크기·터치영역 리포트 | $15 |
| Figma 임포터 | Figma 파일을 Penpot 으로 변환 | $29 (최고 수요) |
| 차트 생성기 | CSV / JSON 을 차트 도형으로 | $12 |
| 목업 생성기 | 디자인을 기기 목업에 합성 | $9 |
| Jira / Notion 연동 | 디자인과 티켓 양방향 링크 | $25/월 |
| 반응형 미리보기 | 여러 해상도 동시 미리보기 | $9 |

수익 시나리오: 플러그인 3 개 × $15 × 월 40 개 = 월 약 $1,800 (260 만원),
1 년 누적 약 3,100 만원.

투자 0 원 (정적 호스팅 무료), 회수 1~2 개월, 난이도 중하.

#### 아이디어 2. 유튜브 + 온라인 강의

```
1단: 유튜브 무료 유입      → 광고 수익 월 50~300 만원
2단: 유료 강의 99,000원    × 200명 = 1,980 만원
3단: 멤버십 월 4,900원     × 300명 = 월 147 만원 (안정 수익)
```

가장 돈 되는 주제: ① Penpot MCP AI 자동화 ② MCP 서버 직접 만들기
③ AI 코딩 에이전트 운영법(`AGENTS.md` 패턴).

투자 10 만원 (마이크), 회수 3~6 개월, 난이도 중하.

#### 아이디어 3. 전자책 / 템플릿

| 상품 | 가격 | 타겟 |
|---|---|---|
| "Penpot 완전정복" PDF | 19,900원 | 디자이너 |
| "MCP 서버 개발 실전" | 29,900원 | 개발자 |
| "AI 에이전트 운영 매뉴얼" | 24,900원 | 팀 리드 |
| Penpot 디자인시스템 템플릿 | 39,900원 | 스타트업 |

투자 0 원, 회수 1~3 개월, 난이도 하.

### 티어 2 — 중간 투자, 높은 단가 (B2B)

#### 아이디어 4. MCP 서버 구축 컨설팅 / 외주 (ROI 최고)

Penpot MCP 소스를 전수 분석했다면 이미 국내 상위 수준의 MCP 전문가다.

| 서비스 | 단가 | 기간 |
|---|---|---|
| MCP 도입 컨설팅 (진단 + 로드맵) | 500 만원 | 2 주 |
| 사내 전용 MCP 서버 개발 | 1,500~3,000 만원 | 1~2 개월 |
| AI 에이전트 워크플로 구축 | 2,000~5,000 만원 | 2~3 개월 |
| 유지보수 리테이너 | 월 150~300 만원 | 지속 |

타겟: 자체 디자인 시스템 보유 중견기업, 디자인 에이전시, SI / 개발사,
AI 도입 계획이 있는 대기업.

영업 포인트: "Penpot 공식 MCP 서버 소스를 전수 분석했습니다. Redis 수평
확장, 세션 격리, 611KB 타입을 온디맨드로 제공하는 토큰 절약 패턴까지
프로덕션 레벨 설계를 그대로 적용합니다."

수익: 연 3 건 × 2,000 만원 = 6,000 만원 + 리테이너.
투자: 포트폴리오 데모 2~4 주. 난이도 중. 회수 첫 계약 즉시.

#### 아이디어 5. Figma → Penpot 마이그레이션 대행

Figma 가격 인상, 데이터 주권 이슈, 공공·금융의 자체 호스팅 요구로 수요가
늘고 있다.

| 패키지 | 내용 | 가격 |
|---|---|---|
| 스타터 | 파일 50 개 이하 변환 | 300 만원 |
| 비즈니스 | 파일 200 개 + 디자인시스템 재구축 | 1,000 만원 |
| 엔터프라이즈 | 무제한 + 자체 호스팅 구축 + 팀 교육 | 2,000~5,000 만원 |

핵심 자산: 변환 자동화 툴을 직접 만든다 (Figma API → Penpot MCP
`execute_code`). 한 번 만들면 계속 재사용한다.

타겟: 공공기관, 금융권, 의료, 방산. 난이도 중. 회수 2~3 개월.

#### 아이디어 6. 자체 호스팅 운영 대행 (MSP)

설치, 모니터링, 백업, 업데이트, 장애 대응을 대신한다.

| 플랜 | 내용 | 월 요금 |
|---|---|---|
| Basic | 설치 + 월간 백업 + 이메일 지원 | 50 만원 |
| Pro | + 모니터링 + 24시간 응답 + 업데이트 | 150 만원 |
| Enterprise | + SSO / LDAP + 전담 매니저 + SLA 99.9% | 300~500 만원 |

기술 기반: `docker/` 의 Dockerfile 6 개 + compose. 백엔드에 Prometheus
메트릭이 내장돼 Grafana 대시보드를 바로 붙일 수 있다.

매력: 월 반복 수익(MRR). 고객 10 개 × 150 만원 = 월 1,500 만원.
투자 서버 월 20~50 만원. 난이도 중상. 회수 3~6 개월.

### 티어 3 — 큰 투자, 큰 리턴

#### 아이디어 7. AI 디자인 자동화 SaaS

컨셉: 프롬프트로 디자인 시스템을 만들고 관리하는 서비스.

킬러 기능:

- 텍스트를 랜딩페이지 / 앱 화면으로 자동 생성
- 브랜드 리뉴얼 일괄 적용 (색, 폰트, 간격 전체 교체) — 가장 강력한 가치
- 디자인 시스템 일관성 자동 감사 + 리포트
- React / Vue / Flutter 코드 자동 생성
- 접근성 자동 검사 및 수정

| 플랜 | 월 요금 | 대상 |
|---|---|---|
| Free | $0 (월 10 회 생성) | 유입 |
| Pro | $29/유저 | 프리랜서 |
| Team | $79/유저 | 디자인팀 |
| Enterprise | 협의 ($10k+/년) | 대기업 |

기술 기반: `PENPOT_MCP_REMOTE_MODE` + `PENPOT_MCP_REDIS_URI` 로 멀티유저
수평 확장이 이미 지원된다. 인프라 설계를 공짜로 얻는 셈이다.

수익 시뮬레이션: 유료 500 명 × 평균 $50 = 월 $25,000 (약 3,600 만원),
연 매출 약 4.3 억원.

투자: 개발 6 개월 + 서버·AI API 월 500 만원 이상.
리스크: AI API 원가 관리가 생명이다. Penpot 공식이 같은 기능을 낼 가능성도
고려한다. 난이도 최상. 회수 1~2 년.

#### 아이디어 8. 버티컬 디자인 SaaS (틈새 공략)

Penpot 을 특정 산업 전용 도구로 리브랜딩한다. 범용 경쟁을 피한다.

| 버티컬 | 제품 | 타겟 | 가격 |
|---|---|---|---|
| 의료 | HIPAA 준수 의료 UI 디자인 | 의료 IT | $200/유저/월 |
| 금융 | 금융권 컴플라이언스 디자인 플랫폼 | 은행·증권 | $300/유저/월 |
| 게임 | 게임 UI 전용 (스프라이트 / 아틀라스) | 게임사 | $100/유저/월 |
| 공공 | 전자정부 웹표준 자동 검증 | 공공기관 | 입찰 |
| 교육 | 디자인 교육용 (과제 제출 / AI 채점) | 대학·학원 | $20/학생/월 |

장점: 규제 준수 비용이 더 비싸므로 가격 저항이 낮다. 경쟁이 없다.
레퍼런스 하나로 업계 전체에 확산된다. 난이도 중상. 회수 1 년.

#### 아이디어 9. 플러그인 마켓플레이스 운영

Penpot 플러그인 유료 판매 플랫폼을 직접 운영한다.

수익: 거래 수수료 20~30% + 입점 프리미엄 + 광고.
기술: PHP / Laravel (결제·라이선스) + React (스토어 프론트).
리스크: 닭과 달걀 문제. 직접 만든 플러그인 5 개로 시작해 해결한다.
난이도 중상. 회수 1~2 년.

### 종합 비교표

| # | 아이디어 | 투자 | 난이도 | 회수 | 예상 수익 |
|---|---|---|---|---|---|
| 1 | 플러그인 판매 | 0원 | 중하 | 1~2달 | 월 200~500만 |
| 2 | 유튜브·강의 | 10만원 | 중하 | 3~6달 | 월 100~500만 |
| 3 | 전자책 | 0원 | 하 | 1~3달 | 월 50~200만 |
| 4 | MCP 컨설팅 | 0원 | 중 | 즉시 | 연 6,000만+ |
| 5 | 마이그레이션 | 소 | 중 | 2~3달 | 건당 300~2,000만 |
| 6 | 운영대행 MSP | 월 50만 | 중상 | 3~6달 | 월 500~1,500만 |
| 7 | AI SaaS | 수억 | 최상 | 1~2년 | 연 4억+ |
| 8 | 버티컬 SaaS | 중 | 중상 | 1년 | 연 1~5억 |
| 9 | 마켓플레이스 | 중 | 중상 | 1~2년 | 거래액 20~30% |

---

## 12. 추천 로드맵

```
1~2개월차  씨 뿌리기
  - 플러그인 2개 개발 후 PenpotHub 무료 배포 (포트폴리오 + 인지도)
  - 유튜브 시즌1 5편 업로드 (한국어 최초 선점)
  - MCP 소스 분석 블로그 5편으로 전문성 증명

3~4개월차  수익화 시작
  - 플러그인 2개 유료화 ($15)
  - 유튜브 시즌2 (MCP 편) 업로드
  - 전자책 출간 (19,900원)
  - 온라인 강의 오픈 (99,000원)

5~6개월차  고단가 전환
  - MCP 컨설팅 데모 제작 후 영업 시작
  - 첫 외주 수주 목표 (500~1,500만원)
  - 마이그레이션 자동화 툴 개발 (재사용 자산)

7~12개월차  확장
  - 컨설팅·외주 정례화 (연 6,000만원)
  - MSP 리테이너 고객 3~5개 확보 (안정 MRR)
  - 반응이 가장 좋았던 기능으로 SaaS 프로토타입
```

핵심 전략은 **작게 시작해 신뢰를 쌓고 고단가로 전환**하는 것이다.
무료 플러그인과 유튜브로 인지도를 얻고, 전자책·강의로 현금 흐름을 만들고,
컨설팅·외주로 고수익을 확보한 뒤 SaaS 로 확장한다.

- 가장 빠른 첫 수익: 플러그인 1 개 + 유튜브 3 편 (2~3 주)
- 가장 큰 수익: MCP 컨설팅

---

## 13. 참고 링크

### 이 레포

- 분석 대상 포크: https://github.com/bmshin94/penpot
- 원본 레포: https://github.com/penpot/penpot

### 공식

- Penpot 웹사이트: https://penpot.app/
- Penpot SaaS: https://design.penpot.app
- MCP 서버 소개: https://penpot.app/penpot-mcp-server
- MCP 퀵스타트: https://help.penpot.app/mcp/#quick-start
- 자체 호스팅 가이드: https://help.penpot.app/technical-guide/getting-started/
- 플러그인 허브: https://penpot.app/penpothub/plugins
- Design Tokens: https://penpot.dev/collaboration/design-tokens
- 커뮤니티 포럼: https://community.penpot.app/
- 유튜브 채널: https://www.youtube.com/@Penpot
- MCP 영상 플레이리스트: https://www.youtube.com/playlist?list=PLgcCPfOv5v57SKMuw1NmS0-lkAXevpn10
- 이슈 트래커 (Taiga): https://tree.taiga.io/project/penpot/

### 도구

- Model Context Protocol: https://modelcontextprotocol.io/
- MCP TypeScript SDK: https://github.com/modelcontextprotocol/typescript-sdk
- add-mcp 헬퍼: https://github.com/neon-solutions/add-mcp
- mcp-remote 프록시: https://github.com/geelen/mcp-remote
- Claude Desktop 다운로드: https://claude.ai/download

### 레포 내부 문서

- `README.md` — 프로젝트 소개
- `AGENTS.md` — AI 에이전트 행동 규약
- `CONTRIBUTING.md` — 기여 가이드
- `mcp/README.md` — MCP 서버 설치·설정 전체 안내
- `mcp/docs/multi-user-mode.md` — 멀티유저 모드
- `plugins/docs/create-plugin.md` — 플러그인 만들기
- `.serena/memories/critical-info.md` — 메모리 그래프 루트
