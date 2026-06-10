# 2팀 Umma — 발표 슬라이드 소재집

> **작성**: 2026-06-10 / 공통 가이드: [.shared/final_presentation_guide.md](../../.shared/final_presentation_guide.md)
> 슬라이드 5(트러블슈팅)·6(협업규칙)·7(회고/개선)에 비중. 모든 항목에 출처·수치 표기.

---

## 슬라이드 매핑 한눈에

| 슬라이드 | 이 팀의 추천 소재 |
|---|---|
| S1-2 개요 | Umma = AI 음성 영어회화, "아기는 말하고 싶어서 배운다" |
| S4 기술 | Kotlin·CleanArch·Hilt·Firebase·OpenAI Realtime·Gemini 2.5·SRS |
| **S5 트러블슈팅** | ① "네트워크오류" 오분류=Race Condition ② AI 6턴 절단 한계 ③ 평문 API key |
| **S6 협업규칙** | Sprint 운영 3종 / PR 리뷰 룰 / 이슈·PR 템플릿 / WIP 제한 |
| **S7 회고·개선** | ⭐ 코드리뷰 0→정착 / 문서 1인부담→3자 분산 |
| S8 성과 | 5/22 critical 17 → 5/27 🔴0 (안정화 1위), App Check 정상(유일) |

---

## S1-2. 프로젝트 개요

- **한 줄 정의**: AI와 실시간 음성 대화로 영어를 배우는 앱. 세션 후 AI가 능력 분석·교정 + SRS 플래시카드
- **슬로건**: "아기는 문법을 배워서 말하지 않습니다. '말하고 싶어서' 배웁니다." (클로징에 재활용하면 강력)
- **팀**: 5명 시작 → **5/19 정재훈 탈퇴 → 4명** (박재민 팀장/정원화 부팀장/김태환 멘토역/김명준)
  - 발표 톤: 탈퇴는 "팀이 어떻게 케어·대응했나" 관점으로. 개인 비난 X.
- 출처: `review/context.md`, `memory/team2_jaehoon_official_withdrawal_260519.md`

## S4. 아키텍처 / 기술스택 (왜 골랐나)

| 영역 | 스택 | "왜" |
|---|---|---|
| 아키텍처 | Kotlin + Clean Architecture + MVVM, Hilt | 테스트·유지보수 |
| 음성 AI | **OpenAI Realtime(gpt-realtime-mini, WebSocket)** | 초기 "Gemini Live" 설계 → 프로덕션 요건 따라 전환 |
| 분석 AI | Gemini 2.5 Flash | 세션 후 능력분석·교정으로 역할 분리 |
| Backend | Firebase(Firestore/Auth/App Check) | 인증·동기화 |
| 학습 | SRS(간격반복) + Vico 차트 | 복습 알고리즘·통계 |

> 발표 포인트: **AI 백엔드 전환(Gemini Live → OpenAI Realtime)**은 "설계를 실용적으로 수정한" 좋은 기술 의사결정 사례.

---

## S5. 트러블슈팅 (대표 2~3건 — 5단계 형식)

### TS-1. "네트워크 오류"의 진짜 정체 = Race Condition (오분류 버그)
- **문제**: 신규 계정이 주제 5개 선택 직후 AI Chat 진입 시 "네트워크 문제" 표시
- **가설·검증**: 네트워크가 아니었음. `user_learning_preference/current` 문서 작성이 Chat 진입보다 느린 **Race Condition** → 데이터 미작성을 "네트워크 오류"로 잘못 매핑
- **근거**: 회복 경로가 2개(앱 재시작 / 언어셀렉터 변경으로 preference 재작성)라는 사실 자체가 매핑 결함의 증거
- **학습**: 에러 메시지는 원인별로 분기해야 한다. "네트워크 오류"라는 만능 메시지가 디버깅을 늦춘다
- 출처: `test/BUG_REPRO_CHAT_NETWORK_ERROR.md`

### TS-2. AI가 6번째 대화를 잊는다 = 설계적 한계의 솔직한 정의
- **문제**: 8턴 이상 대화 후 초반 정보를 물으면 AI가 모름/환각
- **원인**: `BuildPromptUseCase.kt:517 MAX_CONTEXT_TURN_COUNT = 6` — 최근 6턴만 AI에 전달(비용·레이턴시 트레이드오프). 버그가 아니라 **설계 결정**
- **추가**: 빈 응답 조용히 스킵(`ChatViewModel.kt:839`) → "소리는 나는데 자막 없는 유령 답변" / 힌트 180ms 타임아웃
- **학습**: "대화 어색함"의 원인을 코드 한 줄로 설명할 수 있다는 것 = 제품의 한계를 정확히 안다는 증거. 다음 버전 숙제로 정의
- 출처: `test/TESTER_PARK_BETA_REVIEW_260608.md §C-01~03`

### TS-3. SHA1 미등록 / 평문 API key (보안·인증)
- **SHA1**: debug keystore SHA1 미등록으로 Firebase 인증 실패 → 김명준 자체 해결(5/14) — 비전공자의 자발 디버깅 (`memory/team2_recovery_260514_pm.md`)
- **평문 API key**: `local.properties`의 `AIzaSy***`가 `BuildConfig`로 APK에 포함(🔴) → `.gitignore` + 런타임 주입으로 전환, 5/27 해소 (`memory/code_review_260522_cohort.md`)

---

## S6. 협업 Ground Rules + 준수율 (정착도 코호트 1위 팀)

| 규칙 | 동기(왜) | 준수 증거 | 출처 |
|---|---|---|---|
| **Sprint 운영 3종**(Planning/WIP≤5/Retro) | 속도·방향 조율 | ⚠️ WIP 운영O, Planning/Retro는 미시행(회고 소재) | `docs/sprint_operation_guide_260519.md` |
| **PR 리뷰 룰**("강하게 해주세요") | 코드 품질 | ✅ 5/18 첫 시행, 변수명·주석 재PR | `mom/260518_pm_minutes.md` |
| **이슈/PR 템플릿** | 추적·일관성 | ✅ 5/18 commit 반영(AC체크·Closes#·How to Test) | `repo/.github/` |
| **Walking the Board** | WIP 실시간 공유 | ✅ 매 PM 전원 업데이트 | `docs/sprint_operation_guide_260519.md` |
| **시크릿 관리** | 보안 | ✅ 5/15 김태환 자체 해소, App Check 정상 분리(코호트 유일) | `snapshots/260518_am.md` |

**정량 하이라이트**: 강사 사전통지 이행률 **83.3% (5/14·5/18, 코호트 1위)** / Decision Log 단일회의 11건 / 회의 시간 14'42"(주간 최장)

---

## S7. 회고 · 개선 — ⭐ 단일 내러티브

### A. 코드 리뷰 문화 정착 (0 → 정착)

| KPT | 내용 |
|---|---|
| **Problem** | 5/13 코드리뷰 규칙 없음, 박재민 단독 91% 커밋(단일인 과부하) |
| **Try → 액션** | 5/15 강사 "다 같이 시간 정해서 강하게 해주세요" → 5/18 11:00 첫 시행 |
| **Keep → 결과** | 변수명 개선 + 인라인 주석 → 재PR 완전 이행, Decision Log 11건 |

출처: `memory/team2_pm_260515_instructor_baseline.md`, `mom/260518_pm_minutes.md`

### B. 문서 1인 부담 → 3자 합의 분산 (자율 팀 건강 신호)
- **Before**: 정원화가 팀 문서 전체 단독 담당 → R-burn 위험
- **액션**: 5/19 김태환이 **회의 내 자발 의제**로 제기 → 박재민(팀장) 즉각 수용 → 보조강사 코칭, 3자 동시 합의
- **After**: 전원 문서 기여 전환. **강사 주도가 아닌 팀원 자발 의제**로 시작된 개선
- 출처: `memory/team2_doc_distribution_3way_align_260519.md`

### C. (선택) 팀원 케어 — 회복과 한계
- 정재훈 중도포기 요청 → 5/18 다축 케어(멘토링·1:1·격려)로 1일 회복(R-out-3→R-out-2) → 그러나 5/19 탈퇴
- 발표 톤: "팀워크는 한 사람을 살리려 했지만, 구조적 격차는 하루에 메울 수 없었다"는 성숙한 회고. 개인 비난 절대 X
- 출처: `memory/team2_jaehoon_recovery_260518_pm.md`

---

## S8. 성과 지표

| 지표 | 값 |
|---|---|
| 치명 결함 | 5/22 🔴5/🟠6/🟡9 → **5/27 🔴0** (코호트 안정화 1위) |
| App Check | debug/release provider 정상 분리 (**코호트 유일**) |
| 사전통지 이행률 | 83.3% (코호트 1위) |
| @Preview | 13개 (코호트 최다 → 시각 회귀 테스트 기반) |

---

## 오프닝/클로징용 "한 장면" 스토리

1. **"AI가 6번째 대화를 잊을 때"** — 슬로건("아기는 6번째 대화를 잊지 않는다")과 `MAX_CONTEXT_TURN_COUNT=6`을 엮은 클로징. 제품 한계의 솔직한 정의 + 다음 비전.
2. **"주말 카카오톡 한 통"** — 5/15 강사 Sprint2 안내 → 정원화 주말 질문 → 5/18 완전 이행. "프로세스는 사람이 연결할 때 살아난다." 비전공자 기여 강조.
3. **"회복과 탈퇴 사이의 하루"** — 팀 케어의 한계와 성숙한 수용 (민감하므로 사용 시 개인 비난 배제, 팀 시스템 관점으로만).

---

## 출처 인덱스
`memory/`: team2_recovery_260514_pm, team2_pm_260515_instructor_baseline, team2_v2_peak_sprint2_cycle_260518, team2_jaehoon_recovery_260518_pm, team2_jaehoon_official_withdrawal_260519, team2_doc_distribution_3way_align_260519, team2_jaemin_burn_signal_260519, code_review_260522_cohort, runtime_critical_review_260527, cohort_ui_stability_260601
`teams-docs/2team/`: review/{context,team_specific_checks,advisory_myeongjun_260519_session_summary}, docs/sprint_operation_guide_260519, mom/260518_pm_minutes, snapshots/{260518_am,260519_am}, repo/.github/{pull_request_template,ISSUE_TEMPLATE}, test/{BUG_REPRO_CHAT_NETWORK_ERROR,TESTER_PARK_BETA_REVIEW_260608}
