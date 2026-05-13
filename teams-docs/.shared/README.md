# 공유 리소스 (`.shared/`)

> 보조강사가 1~3팀 (또는 그 이상) 작업을 점검할 때 **공통으로 사용하는 자격증명 + 점검 메서드 + 양식**을 한 곳에 모아 관리합니다.
> 팀별 폴더(`1team/`, `2team/`, `3team/`)에서 이 폴더를 참조하는 구조입니다.

---

## 1. 폴더 구조 (2026-05-13 개편)

```
teams-docs/.shared/
├── README.md                        ← 본 문서 (registry + 보안 정책)
├── .figma_token                     ← Figma 토큰 (gitignored, token-leak-guard 면제 패턴)
├── daily_check_method.md            ← 일일 점검 방법론 (보조강사 관점 + 10AM/4PM 리듬)
├── risk_taxonomy.md                 ← 위험 신호 R1~R14 + 워터폴 R-W1~R-W4
├── meeting_prep_template.md         ← 회의 30분 전 작성 양식 (AM/PM 변형)
└── premortem_template.md            ← 주1회 사전 부검 (금요일)

<archive>/.claude/                   ← Claude Code 자동 활성화 자산 (옵션 2)
├── settings.json                    ← PreToolUse hook 등록
├── skills/
│   ├── team-check-am/SKILL.md       ← /team-check-am 슬래시 명령
│   └── team-check-pm/SKILL.md       ← /team-check-pm 슬래시 명령
└── hooks/
    ├── token-leak-guard.sh          ← bash wrapper (Python 자동 탐색)
    └── token-leak-guard.py          ← 실제 시크릿 패턴 검사
```

향후 추가 가능 (필요 시):
- `.github_token` — GitHub PAT (현재는 미사용, WebFetch로 공개 정보만 조회)
- `cron_config.yml` — 일일 체크 자동화 설정

---

## 0. 빠른 호출

### 0-1. 슬래시 명령 (권장, 옵션 2 도입 후)

| 슬래시 명령 | 동작 |
|---|---|
| `/team-check-am 1` | 1팀 오전 10시 미팅 사전 준비 (18시간 윈도우) |
| `/team-check-am 2` | 2팀 동일 |
| `/team-check-am 3` | 3팀 동일 (워터폴 톤) |
| `/team-check-pm 1` | 1팀 오후 4시 미팅 사전 준비 (6시간 윈도우 + 오전 액션 진척) |
| `/team-check-pm 2` | 2팀 동일 |
| `/team-check-pm 3` | 3팀 동일 |

Skill 정의: [`<archive>/.claude/skills/team-check-am/SKILL.md`](../../.claude/skills/team-check-am/SKILL.md), [`team-check-pm/SKILL.md`](../../.claude/skills/team-check-pm/SKILL.md)

### 0-2. 자연어 발화 (대안)

| 사용자 발화 | Claude 진입 절차 |
|---|---|
| "1팀 작업 현황 체크해줘" / "1팀 오전 미팅 준비" | [daily_check_method.md](./daily_check_method.md) → [1team/review/context.md](../1team/review/context.md) → [1team/review/team_specific_checks.md](../1team/review/team_specific_checks.md) → [meeting_prep_template.md AM 양식](./meeting_prep_template.md#1-am-양식-오전-10시-미팅용) |
| "1팀 오후 미팅 준비" | 동일 흐름, PM 양식 |
| "2팀 / 3팀 ..." | 동일 흐름, 해당 팀 폴더 |
| "이번 주 1팀 pre-mortem" | [premortem_template.md](./premortem_template.md) |
| "위험 신호 점검" | [risk_taxonomy.md § 4 명령](./risk_taxonomy.md#4-위험-신호-점검-명령-모음) |

→ 슬래시 명령이 진입 절차 결정적·일관적이라 권장. 자연어 발화도 Skill 안 거치고 같은 결과 가능.

### 0-3. 토큰 누출 사전 차단 (자동)

`<archive>/.claude/hooks/token-leak-guard.sh` (+ `.py`) 가 PreToolUse 단계에서 Write/Edit/MultiEdit/NotebookEdit 호출 시 자동 검사:

- 검출 시: 차단 + 마스킹된 토큰 위치 안내
- 면제: `.figma_token`, `*.token`, `*.env`, `*.env.*`, `*.key`, `*.pem`, `*.jks`, `*.p12`, `credentials*`
- 검출 패턴: Figma PAT, Google API key, Stripe, GitHub PAT, Slack, AWS access key, JWT, Anthropic API key, OpenAI API key, Private key blocks

이 hook은 `<archive>/.claude/settings.json` 으로 자동 활성화. 별도 작업 불필요.

---

## 2. Figma 토큰

### 2-1. 토큰 정보

| 항목 | 값 |
|---|---|
| 파일 | `teams-docs/.shared/.figma_token` |
| 소유 계정 | qk54r71z@gmail.com (JongBeom Park) |
| Scope (권장) | `File content: Read-only`, `Library content: Read-only` |
| 적용 범위 | **1팀, 2팀, 3팀 모두** (소유자가 초대된 모든 보드) |
| 발급일 | 2026-05-13 |
| 만료/Revoke 예정 | 2026-06-16 (프로젝트 종료 시) |
| Git 추적 | ❌ `.gitignore`로 차단 |

### 2-2. 팀별 Figma 보드 레지스트리 ⭐

매일 체크 시 이 표에서 fileKey를 조회해서 사용:

### 팀별 Figma 보드

| 팀 | 보드명 | fileKey | URL | 타입 | Role | 등록 상태 |
|---|---|---|---|---|---|---|
| **1팀 (Scoffee)** | 파이널 1조 피그마 | `NKBVG1F8BcFXzjpnJxsC4u` | [link](https://www.figma.com/board/NKBVG1F8BcFXzjpnJxsC4u) | FigJam | viewer | ✅ |
| **2팀 (Umma)** | 멋사 파이널 2팀 라스트딴따라 | `BTzbV8SDChx6ywQwycniQi` | [link](https://www.figma.com/design/BTzbV8SDChx6ywQwycniQi) | Design | viewer | ✅ |
| **3팀-A (BBip 브레인스토밍)** | 멋사 최프 3조 BBip 브레인스토밍 | `X1hPf6U7hRuUGadqBoAr77` | [link](https://www.figma.com/board/X1hPf6U7hRuUGadqBoAr77) | FigJam | viewer | ✅ |
| **3팀-B (BBip UI)** | 멋사 최프 3조 BBip UI | `gjhxpOboz1famP1hlLVbjq` | [link](https://www.figma.com/design/gjhxpOboz1famP1hlLVbjq) | Design | **editor** | ✅ |

> 💡 **3팀은 2개 파일 운영** — 브레인스토밍(FigJam) + UI(Design) 분리. 워터폴 단계에 맞춰 자연스러운 구성.
> 💡 **2팀 명칭 주의**: 팀 닉네임은 "라스트딴따라"이나 **실제 앱명은 Umma (움마)**.

### 팀별 GitHub 저장소

| 팀 | 저장소 | URL | 상태 |
|---|---|---|---|
| **1팀** | Snoffee | https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee | ✅ |
| **2팀** | Umma | https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma | ✅ |
| **3팀** | FinalProject-BBipit-BBip | https://github.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip | ✅ |

### 2-3. 공통 사용 예시 (Bash)

모든 팀 점검에서 동일하게 토큰 로드:

```bash
cd /c/Users/ibebu/bootcamp6_final/archive
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')

# 1팀 (FigJam)
curl -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/NKBVG1F8BcFXzjpnJxsC4u?depth=1"

# 2팀 (Design)
curl -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/BTzbV8SDChx6ywQwycniQi?depth=1"

# 3팀-A (FigJam 브레인스토밍)
curl -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/X1hPf6U7hRuUGadqBoAr77?depth=1"

# 3팀-B (Design UI)
curl -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/gjhxpOboz1famP1hlLVbjq?depth=1"
```

### 2-4. 팀별 작업 패턴 요약 (점검 시 가중치 조정용)

| 팀 | 핵심 단위 | 보드 활용 패턴 | 점검 시 강조 |
|---|---|---|---|
| 1팀 (Scoffee) | Sprint 0/1 + 일별 섹션 | 애자일 — 작업 중 발견 메모, FLOW-A/B 별도 섹션 | Sprint 1 가시화, Issue↔PR↔Figma 3중 정합성 |
| 2팀 (Umma) | 일별 작업(05-07~) + 버전(v1.1~v1.4) | 버전 단위 iteration 빠름 | 최신 버전(v1.x) 진척, 와이어프레임 페이지 별도 정리 여부, **75:10 기여 비대칭 점검** |
| 3팀-A (브레인스토밍) | 레퍼런스 + 뼈대 잡기 | 워터폴 — 요구분석·탐색 단계 | 단계 종료 명확성 (다음 단계 진입 신호 있는가) |
| 3팀-B (UI) | 화면 + DB | 워터폴 — 설계 단계 | 명세 완성도, 화면-DB 정합성 |
| 3팀 코드 | 4개 모듈(app/bbipit/functions/공통) + Firebase Functions | 워터폴 — **구현 초기** (커밋 15개) | **이슈 닫힘률(40%) 우수**, R8(단일 인물) 점검, 멀티 모듈 정합성 |

---

## 3. 신규 팀 등록 절차

새 팀 보드를 추적 대상에 추가할 때:

1. 사용자(보조강사)가 해당 팀 Figma 보드에 **초대받은 상태**인지 확인
2. 보드 URL 입수 → fileKey 추출
   - `figma.com/board/:fileKey/...` 또는 `figma.com/design/:fileKey/...`
   - `:fileKey`가 추출 대상
3. § 2-2 레지스트리 표에 한 행 추가
4. 해당 팀 폴더에 `figma/` 서브폴더 생성
5. `figma/README.md` 작성 (1팀 [README](../1team/figma/README.md) 참고)
6. 해당 팀의 `review/daily_check_guide.md` 작성 (1팀 [가이드](../1team/review/daily_check_guide.md) 복사 후 팀 컨텍스트 수정)
7. 베이스라인 스냅샷 1회 수집 후 정식 운영

---

## 4. 보안 정책

### 4-1. 절대 금지

- ❌ 토큰 파일을 git에 커밋
- ❌ 토큰을 채팅·메신저·이슈에 평문 붙여넣기
- ❌ 토큰을 다른 사용자 PC에 복사
- ❌ Write scope 권한 부여

### 4-2. 토큰 유출 시 대응

1. **즉시** figma.com → Settings → Security → Personal access tokens → **Revoke**
2. 새 토큰 발급 후 `.figma_token` 덮어쓰기
3. git history에 남았다면 `BFG Repo-Cleaner` 또는 `git filter-repo`로 제거 + force push
4. 사용자(소유 계정)의 Figma 활동 로그 비정상 활동 점검

### 4-3. 프로젝트 종료 후 (2026-06-16+)

- 토큰 **반드시 Revoke** (Figma 설정)
- `.figma_token` 파일 삭제
- 더 이상 점검 필요 없으므로 `.shared/` 폴더 통째로 정리

---

## 5. 참조

### 공통 메서드 (본 폴더)
- [`./daily_check_method.md`](./daily_check_method.md) — 점검 방법론 (보조강사 관점 + 10AM/4PM)
- [`./risk_taxonomy.md`](./risk_taxonomy.md) — 위험 신호 R1~R14 + R-W1~R-W4 (워터폴)
- [`./meeting_prep_template.md`](./meeting_prep_template.md) — 회의 30분 전 양식 (AM/PM)
- [`./premortem_template.md`](./premortem_template.md) — 주1회 사전 부검

### 팀별 컨텍스트
- 1팀 (Scoffee, 애자일): [`../1team/review/context.md`](../1team/review/context.md), [`team_specific_checks.md`](../1team/review/team_specific_checks.md), [`daily_check_guide.md`](../1team/review/daily_check_guide.md) (인덱스)
- 2팀 (Umma, 애자일): [`../2team/review/context.md`](../2team/review/context.md), [`team_specific_checks.md`](../2team/review/team_specific_checks.md)
- 3팀 (BBip, **워터폴**): [`../3team/review/context.md`](../3team/review/context.md), [`team_specific_checks.md`](../3team/review/team_specific_checks.md)

### Figma 가이드
- 1팀: [`../1team/figma/README.md`](../1team/figma/README.md)
- 2팀: [`../2team/figma/README.md`](../2team/figma/README.md)
- 3팀: [`../3team/figma/README.md`](../3team/figma/README.md)

### 스냅샷 보관
- 1팀: [`../1team/snapshots/`](../1team/snapshots/)
- 2팀: [`../2team/snapshots/`](../2team/snapshots/)
- 3팀: [`../3team/snapshots/`](../3team/snapshots/)

### 외부 참조
- Figma REST API 공식 문서: https://www.figma.com/developers/api
- [Atlassian — Daily Standups](https://www.atlassian.com/agile/scrum/standups)
- [Scrum.org — Going Beyond Three Questions](https://www.scrum.org/resources/blog/going-beyond-three-questions-daily-scrum)
- [Parabol — Scrum Master Daily Checklist](https://www.parabol.co/blog/new-scrum-master-daily-checklist/)
- [PMI — Early Warning Signs](https://www.pmi.org/learning/library/identifying-warning-signs-complex-projects-6259)
- [Smartsheet — Phase-Gate Process](https://www.smartsheet.com/phase-gate-process)
- [Parabol — Pre-mortem Questions](https://www.parabol.co/resources/pre-mortem-questions/)
