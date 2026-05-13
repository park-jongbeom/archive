# Archive — 부트캠프 6기 보조강사 운영 시스템

> 매일 **10:00 AM / 16:00 PM** 팀 미팅 점검 + 회의록 / 위험 신호 관리를 자동화한 보조강사 운영 시스템.
> 1팀 **Scoffee** (애자일) / 2팀 **Umma** (애자일) / 3팀 **BBip** (워터폴) — 6주 일정 (2026-05-06 ~ 2026-06-16).

---

## 🚀 빠른 시작 — 오늘 사용할 명령

| 시각 | 명령 | 의미 | 산출물 |
|---|---|---|---|
| **09:30** | `/team-check-am <팀>` | AM 미팅 사전 준비 (18h 변화량 + 어제 PM carry-over) | `snapshots/YYMMDD_am.md` 사전 체크리스트 |
| 10:00~10:30 | (미팅 진행 + OBS 녹화) | — | `meeting/YYYY-MM-DD <팀> 오전미팅.mkv` |
| 미팅 직후 | `/team-meeting-transcribe <팀>` | 녹음 → transcript → 스냅샷 결과 반영 | `meeting/YYMMDD_am_transcript.txt` + AM 스냅샷 갱신 + 필요 시 `mom/` 회의록 |
| **15:30** | `/team-check-pm <팀>` | PM 미팅 사전 준비 (6h 변화량 + 오전 합의 진척) | `snapshots/YYMMDD_pm.md` 사전 체크리스트 |
| 16:00~16:30 | (미팅 진행 + OBS 녹화) | — | `meeting/YYYY-MM-DD <팀> 오후미팅.mkv` |
| 미팅 직후 | `/team-meeting-transcribe <팀>` | 녹음 → transcript → PM 스냅샷 결과 반영 | PM 스냅샷 갱신 + 필요 시 회의록 |
| 매주 금요일 PM 후 | (수동: `.shared/premortem_template.md`) | 주 1회 사전 부검 | `snapshots/YYMMDD_premortem.md` |

**팀 모두 일괄 처리**: `<팀>` 자리에 `all` 입력 (예: `/team-meeting-transcribe all`).

---

## 📦 스킬 (Slash Commands) — `.claude/skills/`

3개 스킬, 매일 회의 직전·직후 호출:

| 스킬 | 사용법 | 분량 | 시점 |
|---|---|---|---|
| [team-check-am](.claude/skills/team-check-am/SKILL.md) | `/team-check-am <1\|2\|3>` | medium | 매일 09:30 |
| [team-check-pm](.claude/skills/team-check-pm/SKILL.md) | `/team-check-pm <1\|2\|3>` | medium | 매일 15:30 |
| [team-meeting-transcribe](.claude/skills/team-meeting-transcribe/SKILL.md) | `/team-meeting-transcribe <팀\|all> [YYMMDD] [am\|pm]` | medium | 회의 종료 후 |

### 호출 흐름 한눈에

```
어제 PM 스냅샷 ──┐
                 ├─→ /team-check-am ─→ AM 사전 체크리스트
오늘 09:30 ──────┘                            │
                                              ▼
                                     (오전 회의 진행 + OBS 녹화)
                                              │
                                              ▼
                              /team-meeting-transcribe (am)
                                              │
                                              ▼
                                  AM 스냅샷 (회의 결과 반영)
                                              │
                                              ▼
                                  /team-check-pm (15:30)
                                              │
                                              ▼
                                      PM 사전 체크리스트
                                              │
                                              ▼
                                     (오후 회의 + 녹화)
                                              │
                                              ▼
                              /team-meeting-transcribe (pm)
                                              │
                                              ▼
                                  PM 스냅샷 → 내일 AM 비교 기준
```

---

## 🛡️ Hooks (.claude/hooks/) — 자동 보안 가드

### [token-leak-guard](.claude/hooks/token-leak-guard.py) (PreToolUse)

Write / Edit / MultiEdit / NotebookEdit 도구 호출 시 자동 실행. 12개 토큰 패턴 (Figma PAT, Google API key, Stripe, GitHub PAT, Slack, AWS, Anthropic, OpenAI, JWT, Private Key 등) 감지 시 **즉시 차단**.

- 면제 파일: `.figma_token`, `.env*`, `*.token`, `*.key`, `*.pem`, `*.jks`, `*.p12`, `credentials*`
- 등록: [.claude/settings.json](.claude/settings.json) `PreToolUse` matcher
- 래퍼: [.claude/hooks/token-leak-guard.sh](.claude/hooks/token-leak-guard.sh) — 크로스 플랫폼 Python 탐색 (uv 경로 fallback 포함)

---

## 📂 폴더 구조 (보조강사 운영 관련)

```
archive/
├── README.md ⭐ (본 파일)
│
├── .claude/                            ← Claude Code 설정 (프로젝트 전용)
│   ├── settings.json                   ← PreToolUse hook 등록
│   ├── hooks/
│   │   ├── token-leak-guard.py         ← 12개 토큰 패턴 차단
│   │   └── token-leak-guard.sh         ← 크로스 플랫폼 래퍼
│   └── skills/
│       ├── team-check-am/SKILL.md      ← AM 사전 준비
│       ├── team-check-pm/SKILL.md      ← PM 사전 준비
│       └── team-meeting-transcribe/SKILL.md  ← 녹음 → 분석 → 갱신
│
├── teams-docs/                         ← 일일 점검 산출물
│   ├── .shared/                        ← 공통 메서드 (1/2/3팀 모두)
│   │   ├── README.md                   ← Figma 토큰·레지스트리·슬래시 명령 목록
│   │   ├── daily_check_method.md       ← 5가지 점검 관점
│   │   ├── risk_taxonomy.md            ← R1~R14 + R-W1~R-W4 정량 기준
│   │   ├── meeting_prep_template.md    ← AM/PM 양식
│   │   ├── premortem_template.md       ← 주1회 사전 부검 (금요일)
│   │   ├── daily_check_method.md
│   │   └── .figma_token                ← 공통 Figma PAT (gitignored)
│   │
│   ├── 1team/                          ← Scoffee (애자일, 3명 — 5/12 송성호 포기)
│   │   ├── review/                     ← 컨텍스트·점검 항목·매트릭스
│   │   │   ├── context.md
│   │   │   ├── team_specific_checks.md
│   │   │   ├── daily_check_guide.md
│   │   │   ├── brief_review_w1.md
│   │   │   └── issue_pr_matrix.md
│   │   ├── snapshots/                  ← 매일 AM/PM 점검 결과
│   │   │   ├── 260513.md               ← 베이스라인 (5/12 이전)
│   │   │   └── 260513_pm.md            ← 첫 PM 스냅샷
│   │   ├── meeting/                    ← 회의 녹음 + transcript
│   │   ├── mom/                        ← 회의록 (Decision Log)
│   │   ├── figma/                      ← Figma 보드 자료 (raw JSON 포함)
│   │   ├── product_brief/              ← Product Brief
│   │   ├── flow/                       ← Flow A/B 다이어그램
│   │   └── members                     ← 팀 구성 (권위 파일)
│   │
│   ├── 2team/                          ← Umma (애자일, 5명)
│   │   ├── review/ snapshots/ meeting/ mom/ figma/ docs/ spec/ members
│   │
│   └── 3team/                          ← BBip (워터폴, 4명)
│       ├── review/ snapshots/ meeting/ mom/ figma/ docs/ prd/ members
│       ├── review_260508.md
│       ├── review_background_location.md
│       └── feedback_background_processing.md
│
├── final-project/                      ← 부트캠프 가이드 자료
├── mid-project/
├── operation/
├── plan/
├── ta-guides/
└── sample-project/
```

---

## 🎯 점검 시스템 핵심 개념

### 1. 3-tier 문서 구조

```
.shared/        ← 공통 메서드 (3개 팀 모두 적용)
  ↓
<X>team/review/ ← 팀 컨텍스트 + 고유 점검 항목
  ↓
<X>team/snapshots/ ← 매일 점검 결과 (시간 축)
```

### 2. R-항목 (위험 신호 정량 기준)

[.shared/risk_taxonomy.md](teams-docs/.shared/risk_taxonomy.md) — R1~R14 (애자일) + R-W1~R-W4 (워터폴) + 팀별 특화 R-항목.

대표 즉시 에스컬레이션:
- **R1**: PO 7일 누적 활동 0 — (1팀은 5/12부 무효화)
- **R13** 🚨: 시크릿/설정 파일 commit (`google-services.json`, `.env`, API 키 등) — **회의 보고 상단 박스**
- **R-out**: 팀원 14일 무활동 — 강사 보고
- **R-demo**: 데모 1주 전 핵심 FLOW 미동작
- **R-W1**: 워터폴 Must Meet 미충족 단계 종료

### 3. 팀별 방법론 차이 (반드시 인지)

| 팀 | 방법론 | 점검 강조점 | 권고 금지 |
|---|---|---|---|
| 1팀 Scoffee | **애자일** (Scrum-lite) | Sprint 백로그 · Issue Closes · mock-first | — |
| 2팀 Umma | **애자일** | 적응 단계 멤버 (김명준/정재훈) · 박재민 WIP | — |
| 3팀 BBip | **워터폴** (Phase-gate) | 단계 종료 Must Meet · 명세 완성도 | **MVP 다이어트 / Sprint 권고 ❌** |

→ 3팀에 애자일 톤 권고는 메모리에 명시적으로 금지됨 ([team3_methodology.md](C:/Users/ibebu/.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md)).

---

## 🛠️ 처음 사용하는 경우 — 환경 점검

### 필수 도구
| 도구 | 용도 | 확인 명령 |
|---|---|---|
| **Buzz 1.4+** | 회의 녹음 → transcript | `winget list --name Buzz` |
| **ffmpeg / ffprobe** | wav 추출 (Buzz 번들 사용) | `ls "/c/Program Files (x86)/Buzz/_internal/ffmpeg.exe"` |
| **whisper-cli** | transcribe (Buzz 번들 사용) | `ls "/c/Program Files (x86)/Buzz/_internal/buzz/whisper_cpp/whisper-cli.exe"` |
| **git** | 코드 변화 추적 | `git --version` |
| **curl** | Figma API + 모델 다운로드 | `curl --version` |
| **OBS Studio** | 회의 화면+음성 녹화 | (별도 설치) |

### 모델 (1회만 다운로드, ~1.5GB)
```bash
curl -L --progress-bar -o "/c/Users/ibebu/AppData/Local/Buzz/Buzz/Cache/models/ggml-medium.bin" \
  "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin"
```

상세: [team-meeting-transcribe SKILL §1 사전 환경 점검](.claude/skills/team-meeting-transcribe/SKILL.md#1-사전-환경-점검-1회만-필요).

---

## 🧠 메모리 (자동 환기되는 사실)

다음 세션에서도 자동 환기되는 프로젝트 메모리 (`~/.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/`):

| 메모리 | 핵심 |
|---|---|
| `team3_methodology.md` | 3팀 워터폴 확정 — Sprint/MVP 다이어트 권고 ❌ |
| `team1_personnel_change_260512.md` | 1팀 송성호 5/12 중도포기, 손지희 신임 팀장, 부팀장 공백, 3명 체제 |
| `team_check_meeting_insights_260513.md` | 3팀 PM 회의 패턴 — 짧은 형식적 보고, 보조강사 사전 의제 push 없으면 R-항목 누락 |

신규 인원 변경 / 반복 패턴 발견 시 자동으로 갱신됩니다.

---

## ❓ 트러블슈팅

### 문제: `/team-meeting-transcribe`가 Python 호출에서 exit 49

→ Windows store stub 문제. 본 스킬은 Python을 직접 호출하지 않으므로 무관. Figma API 파싱 시는 `grep -oE '"lastModified":"[^"]*"'` 폴백 사용.

### 문제: 회의가 너무 짧아 transcript가 1KB 미만

→ 형식적 진척 보고일 가능성. **합의 / 액션 / R-항목 발현이 거의 비어있다는 사실 자체**를 다음 회의 의제 후보로 기록. 보조강사가 강사에게 사전 의제 1줄 통지 권장.

### 문제: `google-services.json` 등 시크릿이 이미 git에 push 됨

→ R13 즉시 에스컬레이션. 절차:
1. 발급 기관(Firebase / Google / GitHub 등)에서 즉시 키 제한 또는 재발급
2. `.gitignore`에 패턴 추가 + `git rm --cached <파일>`
3. git history 정화 검토 (BFG Repo-Cleaner 또는 git filter-repo) — 민감도에 따라 결정
4. **token-leak-guard hook**이 향후 같은 패턴 재커밋을 차단 (이미 활성)

### 문제: mkv 파일이 누적되어 디스크 부족

→ Week 종료(매주 일요일) 시 해당 주 mkv 일괄 zip 압축 또는 삭제. transcript .txt는 영구 보관. 데모 영상만 `<X>team/demo/` 별도 폴더 분리 권장.

### 문제: 변환 결과의 인명/외래어가 부정확

→ medium 모델 한계. "정섭/정훈/성호" 비슷한 발음 혼동 빈번. 추측이 큰 부분은 `?` 또는 `(추정)` 표기. 더 정확한 결과 원하면 `large-v3` (3GB) 모델로 교체 — RTF 2~3배 느려짐 트레이드오프.

---

## 📅 변경 이력

| 날짜 | 변경 |
|---|---|
| 2026-05-13 | 일일 점검 시스템 첫 도입 (3-tier 구조, R-항목 분류, AM/PM 양식) |
| 2026-05-13 | Skills 2개 + Hook 1개 도입 (`team-check-am`, `team-check-pm`, `token-leak-guard`) |
| 2026-05-13 | 1팀 5/12 인원 변경 반영 (송성호 포기, 손지희 승계, 부팀장 공백) |
| 2026-05-13 | 첫 PM 회의 변환 1회차 — Buzz 1.4.4 + ggml-medium 한국어 워크플로우 검증 |
| 2026-05-13 | 스킬 1개 추가 (`team-meeting-transcribe`) + 본 README.md 작성 |

---

## 📚 추가 참조

- 부트캠프 가이드: [final-project/docs/](final-project/) 폴더
- 보조강사 운영 가이드: [ta-guides/](ta-guides/) 폴더
- 외부 자료:
  - [Buzz GitHub](https://github.com/chidiwilliams/buzz)
  - [whisper.cpp](https://github.com/ggerganov/whisper.cpp)
  - [Range — Daily Standup Agenda](https://www.range.co/blog/complete-guide-daily-standup-meeting-agenda)
  - [Smartsheet — Phase-Gate Process](https://www.smartsheet.com/phase-gate-process) (3팀 워터폴 참조)
