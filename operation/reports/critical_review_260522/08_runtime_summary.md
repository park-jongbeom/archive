# 강사진 시연/테스트 단계 — 화면 동작/크래시 통합 리포트 (2026-05-22)

> 관점: 강사진이 실 단말에서 빌드/시연/테스트 중 만나는 사용자 체감 버그
> 보안/구조 이슈는 [05_final_report.md](05_final_report.md) 참조 (이번 리포트에서 중복 제외)
> 점검 방식: 정적 분석 (실 단말 빌드/실행 미수행)
> 산출 보고서: [07_team1_runtime.md](07_team1_runtime.md), [07_team2_runtime.md](07_team2_runtime.md), [07_team3_runtime.md](07_team3_runtime.md)

---

## 0. 합계

| 팀 | 시나리오 수 | 🔴 Critical | 🟠 High | 🟡 Medium | 즉시 크래시 위험 |
|---|---|---|---|---|---|
| 1팀 Snoffee | 21 | 5 | 8 | 8 | 🔴 SecurityException + 권한 dead-end |
| 2팀 Umma | 10 + 부가 11 | 3 | 2 | 5 | 🟢 거의 없음 (silent UX 위주) |
| 3팀 BBipIt | 25 + 7 | 6 | 14 | 12 | 🔴 functions 17개 미배포 → 다수 경로 실패 |
| **합계** | **56+** | **14** | **24** | **25** | |

---

## 1. 강사진 시연 시 가장 빨리 발견될 Top 시나리오 (코호트 횡단)

### 🔴 시연 5분 안에 발견 (즉시 패치 권고)

| # | 팀 | 시나리오 | 1줄 핫픽스 |
|---|---|---------|------------|
| 1 | 1팀 | **온보딩 권한 거부 → dead-end** (앱 진입 불가) | [OnboardingPermissionScreen.kt:73-88](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L73) 거부 분기에 `onNextClick()` 추가 |
| 2 | 1팀 | **리포트 탭 모든 서브탭 → 항상 같은 더미** (스타벅스 150mg 등 고정) | [ReportViewModel.kt:84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84) 더미 삭제 + 주석 해제, 또는 "샘플 데이터" 라벨 |
| 3 | 1팀 | **수면 탭 진입 → SecurityException 크래시** (Health Connect 미설치/거부 시) | [SleepViewModel.kt:55-67](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L55) `runCatching` |
| 4 | 1팀 | **카페인 직접 등록 dialog → `intakeSize`/`intakeCaffeine` 이중 매핑 버그** (계산에 0 입력) | dialog 필드 매핑 수정 |
| 5 | 2팀 | **BottomBar 5탭 중 3개가 placeholder** (복습/통계/교정 = "○○ 플레이스 홀더" 한 줄) | 데모 동선 Dashboard↔Chat 2탭으로 한정 사전 안내 |
| 6 | 2팀 | **Chat 진입 시 관심 주제 5개 다이얼로그에서 사용자 갇힘** ([ChatScreen.kt:159-165](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L159) `onCancel={}` 빈 람다) | `onCancel = { viewModel.dismissTopicDialog() }` |
| 7 | 2팀 | **Chat → Dashboard → Chat 사이클 후 AI 음성 silent fail** (AudioPlayer @Singleton release 후 재초기화 없음) | `startPlaying()` 진입 시 null 체크 → reinit |
| 8 | 3팀 | **MapScreen 권한 허용 후 BackgroundListenerService startForegroundService 호출이 주석 처리됨** ([MapScreen.kt:84-102](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L84)) — 권한 줘도 서비스 미가동, 재시작 필요 | 주석 4줄 해제 |
| 9 | 3팀 | **ChatDetailScreen 하드코딩 roomId/receiverId** ([ChatDetailScreen.kt:91-96](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt#L91)) — 어떤 친구를 눌러도 동일 채팅방 진입 + 본인 메시지가 다른 사람 방에 들어감 | navigation arg 사용 (주석 해제 + 하드코딩 삭제) |
| 10 | 3팀 | **functions 17~20개 미배포 → 채팅·친구·음성·알림 다수 NOT_FOUND** | 콘솔 배포 여부 즉시 확인 (학생 push) |

---

## 2. 팀별 종합 — 시연 전 핫픽스 권고

### 1팀 Snoffee (총 30~45분)

#### 🔴 시연 직전 패치 (반드시)
1. **권한 거부 분기 우회** ([OnboardingPermissionScreen.kt:73-88](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L73)) — 5분
2. **ReportViewModel 더미 토글** — 보여줄지 숨길지 결정 ([ReportViewModel.kt:84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84)) — 10분
3. **SleepViewModel runCatching** ([SleepViewModel.kt:55-67](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L55)) — 5분
4. **CaffeineInputDialog 필드 매핑 수정** — 10분

#### 🟢 보정 (04 보고서 우려와 다름)
- **Gemini "AI 컷오프" / "주간 리포트" 크래시 dormant** — `GetCutoffTimeUseCase` / `GetWeeklyInsightUseCase`가 어떤 presentation 코드에서도 호출되지 않음 (grep 0건). **시연 안전**

#### 시연 동선 권고
- Watch / FCM / Setting 탭은 **시연에서 제외** (모듈 빈 껍데기, 항목 하드코딩)
- 권장 동선: Onboarding(권한 통과 가정) → Home → Caffeine 추가 → Report (단, 더미 라벨 명시)

---

### 2팀 Umma (총 45~60분)

#### 🔴 시연 직전 패치
1. **R2-2 onCancel 1줄 패치** ([ChatScreen.kt:159-165](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L159)) — 5분
2. **R2-3 AudioPlayer reinit** ([AudioPlayer.kt:78-81](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioPlayer.kt#L78)) — 15분
3. **R2-4 MyPage 모국어 설정 임시 숨김** — 5분
4. **Placeholder 3개 화면에 "다음 스프린트 예정" 안내** — 20분

#### 🟢 강점 (강사 칭찬 자료)
- 즉시 크래시 위험 거의 없음 — try/catch 일관, onFailure 분기 구비
- AudioRecord try/finally release
- recentFullContext 100턴 제한
- callbackFlow awaitClose 패턴

#### 시연 동선 권고
- BottomBar 5탭 중 데모 가능 화면: **Dashboard + Chat 2탭만**
- 신규 사용자 첫 진입 시 관심 주제 5개 선택 강제 — 시연 전에 시연용 계정에 미리 등록 권고

---

### 3팀 BBipIt (총 60분, 5/26 데드라인 영향) ⭐⭐⭐

#### 🔴 오늘 안 패치 (11개 권고, 5/26 데드라인 직격)
1. **R3-1 ChatDetailScreen navigation arg 사용** ([ChatDetailScreen.kt:91-96](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt#L91)) — 5분
2. **R3-2 MapScreen 주석 4줄 해제** ([MapScreen.kt:84-102](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L84)) — 1분
3. **R3-5 FriendList 친구 삭제 `viewModel.deleteFriend()` 호출** — 3분
4. **R3-6 NotificationScreen REQ 알림 클릭 시 FriendRequestList navigate** — 1분
5. **R3-7 ChatList 이중 호출 LaunchedEffect 1줄 삭제** — 1분
6. **워치 PTT 하드코딩 UID 제거 → 친구 선택 UI** ([BackgroundListenerService.kt:439](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L439)) — 20분
7. **functions 콘솔 배포 여부 즉시 확인** (학생 push) — 0분
8. **Notification 헤더 개발용 테스트 버튼 (`[+ DM 추가]` 등) 숨김** — 5분
9. **자기 자신 친구 추가 가드** — 5분
10. **FCM onMessageReceived 구현** (또는 시연 시 푸시 시연 제외) — 10분
11. **EditProfile/VoicePlayer functions 의존 — `updateProfile`/`getUserProfile` 콘솔 배포 확인** — 0분

#### 🟢 강점 (강사 칭찬 자료) ⭐⭐
- foregroundServiceType 4종 OR + 권한 매칭 (Android 14 정책 견고)
- snapshot listener 8개 모두 awaitClose 또는 명시적 remove
- AudioRecord/MediaPlayer release 견고 (try/finally + Completion/Error)

#### 시연 동선 권고
- **시연 전 functions 콘솔 배포 상태 확인 필수**. 미배포면:
  - DM/친구추가/음성메시지/알림 읽음 처리/온라인 상태 전부 실패 경로 진입
  - 시연 가능 영역: SignIn → Map → PTT(direct fallback) → 워치 페어링
- 워치 PTT는 하드코딩 UID 영향으로 **단일 수신자만 가능**

---

## 3. 코호트 패턴 (이번 점검에서 발견된 새 신호)

### 패턴 1: "버튼은 눌리는데 아무 일도 안 일어남" 패턴
| 팀 | 사례 |
|---|---|
| 1팀 | Setting 모든 항목 클릭 no-op, 카페인 "수정" 메뉴 dead-end |
| 2팀 | MyPage 모국어 설정 silent fail, Topic Dialog onCancel={} |
| 3팀 | FriendList 친구 삭제 Log.d만 찍힘, NotificationScreen REQ navigate 누락 |

→ 코호트 100% 발현. **콜백 wire 단계가 일관되게 미완**. 학생들이 ViewModel/Repository는 만들었으나 UI 콜백 연결이 마지막 1km에서 빠짐.

### 패턴 2: "하드코딩 테스트 값이 production 코드에 잔존"
| 팀 | 사례 |
|---|---|
| 1팀 | Setting 모든 값 (168cm/62kg/22:30), ReportViewModel 더미 |
| 2팀 | (없음) |
| 3팀 | ChatDetailScreen roomId/receiverId, 워치 PTT 수신 UID |

→ 1팀/3팀 발현. 2팀은 더미 데이터가 코드 대신 placeholder Screen으로 분리되어 있어 상대적 안전.

### 패턴 3: "권한 거부 dead-end / silent fail"
| 팀 | 사례 |
|---|---|
| 1팀 | 온보딩 권한 거부 → 다음 진행 불가 |
| 2팀 | (양호 — RECORD_AUDIO fallback 구현) |
| 3팀 | MapScreen 권한 허용 후 startForegroundService 주석 처리 |

→ 1팀 dead-end, 3팀 reverse dead-end (허용해도 안 됨).

### 패턴 4: "외부 호출 실패 UI 처리 격차"
- 1팀: Firestore 실패 시 빈 결과로 fall through (R1-7)
- 2팀: try/catch + onFailure 분기 양호
- 3팀: functions 실패 시 Toast만 노출 + UI 복원 없음 (낙관 업데이트 후 미보정)

→ 2팀이 가장 견고, 1팀/3팀은 보강 필요.

---

## 4. 강사진 매니저 1줄 보고용

> "1팀 권한 거부 dead-end + Report 더미 고정 + Sleep Health Connect 크래시; 2팀 BottomBar 5탭 중 3개 placeholder + Chat 다이얼로그 사용자 갇힘; 3팀 ChatDetailScreen 하드코딩 roomId + MapScreen 권한 후 서비스 미가동 + functions 17~20개 미배포 — 강사 시연 5분 내 발견될 critical 14건. 상세: `operation/reports/critical_review_260522/07_team{N}_runtime.md`"

---

## 5. 점검 한계 (사용자 인지 필요)

- **실 단말 빌드/실행 미수행** — 정적 분석만. 빌드 자체 실패 가능성 별도 검증 필요
- **콘솔 측 설정 미확인** — 3팀 functions 콘솔 배포 여부, Firestore rules, App Check enforcement는 모두 콘솔 상태가 최종
- **시연 시나리오는 가설** — 강사가 실제로 어떤 동선을 시연할지 모름. 가능한 동선을 가정하여 우선순위화
- **ProGuard release 빌드 영향 미검증** — minify 활성 시 추가 발견 가능

---

## 6. 작성 파일

- [00_progress.md](00_progress.md) — 진행 기록
- [05_final_report.md](05_final_report.md) — 보안/구조 통합 리포트 (이전)
- [06_runtime_focus_plan.md](06_runtime_focus_plan.md) — 본 재점검 계획
- [07_team1_runtime.md](07_team1_runtime.md) — 1팀 시나리오 21개
- [07_team2_runtime.md](07_team2_runtime.md) — 2팀 시나리오 10개 + 부가 11개
- [07_team3_runtime.md](07_team3_runtime.md) — 3팀 시나리오 25개 + 7개
- [08_runtime_summary.md](08_runtime_summary.md) — **본 문서**
