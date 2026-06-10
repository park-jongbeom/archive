# 1팀 Snoffee — 발표 슬라이드 소재집

> **작성**: 2026-06-10 / 공통 가이드: [.shared/final_presentation_guide.md](../../.shared/final_presentation_guide.md)
> 슬라이드 5(트러블슈팅)·6(협업규칙)·7(회고/개선)에 비중. 모든 항목에 출처·수치 표기.

---

## 슬라이드 매핑 한눈에

| 슬라이드 | 이 팀의 추천 소재 |
|---|---|
| S1-2 개요 | Snoffee = Sleep+Coffee, 카페인-수면 분석 AI + Galaxy Watch |
| S4 기술 | Kotlin·Compose·MVVM+CleanArch·Hilt·Room·Firestore·Gemini·WearOS |
| **S5 트러블슈팅** | ① google-services.json 노출 ② Health Connect 방어코드 ③ 헬스 SDK 분기 결정 |
| **S6 협업규칙** | mock-first 3건 / `Closes #N` / Clean Arch 레이어 / Decision Log |
| **S7 회고·개선** | ⭐ 회의 6일 교착 → 진행자 전환 → 9분+ 회복 |
| S8 성과 | 5/22 🔴6 → 5/27 🔴0 (5일 만에 치명결함 0) |

---

## S1-2. 프로젝트 개요

- **한 줄 정의**: 카페인 섭취·수면 데이터를 분석해 개인 맞춤 카페인 컷오프 시간을 Gemini AI가 알려주는 Galaxy Watch 연동 안드로이드 앱
- **팀**: 4명 시작 → **5/12 송성호 PO 중도포기 → 3명 체제**, 손지희 신임 팀장(팀장·테크리드·PO 3중 역할)
  - 발표 톤: 인원 변동은 "위기였고 어떻게 대응했나"의 소재로. 개인 비난 X.
- 출처: `repo/README.md`, `memory/team1_personnel_change_260512.md`

## S4. 아키텍처 / 기술스택 (왜 골랐나 1줄씩)

| 영역 | 스택 | "왜" |
|---|---|---|
| UI | Jetpack Compose | 선언형 UI, 빠른 반복 |
| 아키텍처 | MVVM + Clean Architecture(domain/data/presentation) | 테스트·유지보수 |
| DI | Hilt | 보일러플레이트 감소 |
| DB | Room(로컬) + Firestore(원격) | 오프라인+동기화 |
| AI | Gemini API | 개인화 분석/인사이트 |
| 워치 | WearOS + Health Connect / Samsung Health | 수면·헬스 데이터 |

---

## S5. 트러블슈팅 (대표 2~3건 — 5단계 형식)

### TS-1. google-services.json 민감정보 노출 (보안)
- **문제**: Firebase 설정 파일(`AIzaSy***`)이 public repo에 git 추적 상태로 노출
- **발견**: 5/13 PM 회의 중 강사가 코드 보다가 직접 발견 (팀 인지 이전)
- **원인**: `.gitignore` 미설정 + git 추적 제거 절차 미숙지
- **해결**: 손지희 담당 → 5/15 정섭 자체 해소(`.gitignore` 추가) → `origin/develop` CLEAN
- **결과·학습**: 5/15 이후 재발 0건. "민감정보 체크리스트는 프로젝트 첫날 세팅" 교훈
- 출처: `mom/260513_pm_minutes.md`, `snapshots/260519_am.md §R13`

### TS-2. Health Connect 미설치 크래시 방어 (안정성)
- **문제**: Health Connect 미설치 기기에서 헬스 데이터 조회 시 `IllegalStateException` 크래시 위험
- **해결**: `SleepRepositoryImpl.kt:66-74` `runCatching { … }.getOrDefault(emptyList())` 가드 → 미설치 시 크래시 없이 빈 목록 반환
- **결과·학습**: 테스터가 "코어 기능은 크래시 0"으로 검증. 외부 의존성은 항상 실패 경로를 가드한다는 원칙
- 출처: `test/TESTER_PARK_BETA_REVIEW_260608.md §D-01`

### TS-3. Samsung Health SDK vs Health Connect 분기 결정 (기술 의사결정)
- **문제**: 수면 데이터 경로 2개 — Health Connect(데이터 소실 가능) vs Samsung Health SDK(파트너십 필요). 추가로 API 24~29 기기는 WearOS 연동 불가
- **가설·검증**: 정섭이 양쪽 SDK 비교 조사 → "Samsung Health 직접 연동, 개발 단계는 파트너십 불필요" 결론
- **해결**: min SDK 분기(API 24~29 워치 연동 제외) + Decision Log 기록
- **결과·학습**: 결정이 명시화된 5/19 직후 진척이 가장 빨랐음 → "조사→결정→기록" 사이클 확립
- 출처: `mom/260519_am_minutes.md §1`

> **보너스(질문 대비)**: Room `fallbackToDestructiveMigration()` 데이터 소실 위험 / `CaffeineSensitivity` enum을 UseCase에서 Int로 받는 도메인 타입 무력화 — 둘 다 코드리뷰에서 식별된 개선 포인트로 솔직하게 언급 가능.

---

## S6. 협업 Ground Rules + 준수율

| 규칙 | 동기(왜) | 준수 증거 | 출처 |
|---|---|---|---|
| **mock-first 3건** (Gemini/Health/알림 Week3 실연동) | 외부 API 디버깅이 다른 작업 막는 리스크 차단 | ✅ Week1-2 mock 정확히 유지 | `review/brief_review_w1.md` |
| **`Closes #N` 이슈 컨벤션** | 진척 가시화 + 이력 추적 | ⚠️ 초기 PR 0/13 미정착 → 회고 소재(아래) | `review/issue_pr_matrix.md` |
| **Clean Architecture 레이어** | 테스트·유지보수 | 부분 준수(enum 무력화 1건) | `review/team_specific_checks.md` |
| **Decision Log 문서화** | 결정 근거 보존 | ✅ 5/19 단일일 **8건** 기록 | `memory/team1_mtg_full_recovery_260519.md` |
| **백로그 본인 assign** | "누가 뭘 하는지" 가시화 | ✅ 5/19 도입 | `mom/260519_am_minutes.md` |

> 발표 포인트: "규칙을 100% 지키진 못했다. `Closes #N`은 초기 0/13이었다"를 **먼저 인정**하고 → 회고(S7)로 연결하면 신뢰가 산다.

---

## S7. 회고 · 개선 — ⭐ 단일 내러티브 (발표 중심축)

### 회의 채널 6일 교착 → 진행자 전환 → 완전 회복

**KPT 표**

| | 내용 |
|---|---|
| **Problem** | 5/13~5/18 6일 연속 회의 실질 발화 2분 이하. **5/18 AM: 15'25" 회의 중 실질 발화 약 30초**. 사전통지 이행 0%(4일), carry-over 0/14, 신호등 7일 미시행 |
| **원인(blameless)** | 개인 게으름이 아니라 **PO 공백 + 팀장 번아웃 + 3명 체제 결석**의 구조적 문제 |
| **Try → 액션** | 보조강사 사전 의제 채널 운영 + 5/18 PM 이제이 자발 보고 + **5/19 손지희가 진행자로 등판**(이제이 결석에도 자율 분담 인수) |
| **Keep → 결과** | **5/19 AM 9'24" + PM 9'51" (베이스라인 대비 +76% / +84%)**, Decision Log 8건/일, 사전통지 0%→50% |

**핵심 메시지**: 회의 침묵의 원인은 환경이나 개인이 아니라 팀 구조 변화였고, 해결도 외부 개입이 아닌 **팀 내부의 자생적 역할 재편**으로 이뤄졌다.

- 출처: `memory/team1_mtg_mic_6day_threshold_260518.md`, `memory/team1_mtg_full_recovery_260519.md`

### 보조 개선 사례
- **정섭 R-stall 자체 회수**: 정체된 #20·#26을 5/18 단독 commit(19파일 +392/-60)으로 회수 — "회의는 멈췄어도 데이터 채널은 작동" (`memory/team1_mtg_mic_6day_threshold_260518.md`)

---

## S8. 성과 지표

| 지표 | 값 |
|---|---|
| 치명 결함 해소 | 5/22 🔴6 → **5/27 🔴0** (5일), 신규 크래시 0 |
| 회의 시간 회복 | 5/18 ~30초 → 5/19 9'24"/9'51" |
| Decision Log | 5/19 단일일 8건 |
| 보안 | google-services.json 5/15 자체 해소 후 CLEAN |

---

## 오프닝/클로징용 "한 장면" 스토리

1. **"30초 회의"** — 6일의 침묵과 하루 만의 회복 (S7 내러티브의 스토리텔링 버전). 오프닝 훅으로 강력.
2. **"회의 중에 강사가 발견했다"** — `.gitignore` 하나를 놓쳐 생긴 보안 사고. 보안의 중요성 + 솔직함.
3. **"README엔 있고 앱엔 없다"** — 간판 기능(워치·AI·FCM)은 미완, 코어(카페인 계산·방어코드)는 견고. "작동하는 것과 광고하는 것의 간극을 줄이는 게 다음 목표"라는 성숙한 클로징.

> 클로징 권고: 3번 스토리로 "PO 중도포기·3명 체제라는 제약 속에서 코어를 견고히 했고, 미완 영역을 정확히 안다"는 메타인지를 보이면 면접관에게 강한 인상.

---

## 출처 인덱스
`memory/`: team1_personnel_change_260512, team1_burn_attend_260514_am, team1_mtg_mic_6day_threshold_260518, team1_pm_partial_recovery_260518, team1_mtg_full_recovery_260519, code_review_260522_cohort, runtime_critical_review_260527, cohort_ui_stability_260601
`teams-docs/1team/`: review/{context,brief_review_w1,team_specific_checks,issue_pr_matrix,sprint1_demo_checklist_260520}, mom/{260513_pm,260514_am,260518_am,260519_am}, snapshots/{260514_am,260519_am,260519_pm}, repo/README.md, test/TESTER_PARK_BETA_REVIEW_260608.md
