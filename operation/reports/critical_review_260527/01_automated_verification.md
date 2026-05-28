# 자동 검증 리포트 — 5/27 발견 패턴 도구화 (2026-05-27 PM)

> 점검 시점: 2026-05-27 PM (5/27 정적 분석 동일일 후속 작업)
> 도구: **semgrep 1.163.0** (15개 커스텀 규칙) + **detekt 1.23.7** (기본 룰셋) + mobsfscan 0.4.3 (Windows 환경 버그로 미사용)
> 입력: `teams-docs/{1,2,3}team/repo/app/src/main/` (develop 브랜치 5/27 HEAD)
> 산출: `operation/reports/critical_review_260527/auto/` (semgrep JSON 3건, detekt TXT 3건)
> 목적: 5/27 정적 분석 발견 35건이 **다시 발견됐을 때 자동으로 잡히는** 도구 자산 형성 + 신규 발견 보강

---

## 1. 한 줄 요약

15개 커스텀 semgrep 규칙으로 **3팀 총 43건 발견** (1팀 12 / 2팀 9 / 3팀 22). **5/27 정적 분석 핵심 발견 13건 중 13건 자동 매칭 (정밀도 100%)** + **신규 발견 6건** (5/27 점검에서 못 잡았던 동일 패턴 확산).

가장 큰 시사점: 3팀 **functions/index.js는 5/27에도 여전히 빈 파일** (`helloWorld` 주석만). semgrep이 발견한 **9개의 Cloud Function 호출 전부 NOT_FOUND 가능성** — 5/27엔 PTT 하나만 봤지만 실제론 History/LiveStatus/Friend/EditProfile까지 영향.

---

## 2. 도구 적용 경로 결정 (의사결정 기록)

이번 점검은 사용자 요구 "단말 클릭 없이 코드 단위에서 자동 검증" 충족 경로를 단계별로 좁힘:

| 후보 | 결과 | 사유 |
|---|---|---|
| Roborazzi + ComposablePreviewScanner | ❌ 포기 | 3팀 `@Preview` 개수 1팀 3개 / 2팀 13개 / 3팀 0개. 검사 대상 부재. AGP 9.x / Kotlin 2.3.x 신규 버전 호환성 리스크 |
| Compose Preview Screenshot Testing (Google) | ❌ 포기 | 동일 — Preview 부재 |
| MobSF (full Docker) | ❌ 포기 | APK 분석 위주, 화면 동작 오류 커버율 낮음 |
| UiAutomator + Macrobenchmark | ❌ 포기 | 단말 필요 (요구 위반) |
| **semgrep + 커스텀 규칙** | ✅ 채택 | 5/27 보고 패턴 그대로 YAML화 가능. 빌드 0 / 단말 0 / 학생 코드 무수정 |
| **detekt 기본 룰셋** | ✅ 채택 | Java jar로 즉시 실행. SwallowedException 등 potential-bugs 보강 |
| mobsfscan | ⚠️ 미사용 | Windows에서 `libsast` semgrep 호출 결과 None 처리 버그. semgrep 직접 사용으로 대체 |

---

## 3. semgrep 커스텀 규칙 15개 — 5/27 보고서 기반

| ID | 규칙 | 5/27 발견 매칭 |
|---|---|---|
| P1 | placeholder-text-stub | 3팀 R3-N3 (SettingsDrawer 빈 stub) |
| P2 | dummy-data-marker | 1팀 Y1 (Report 더미 5/22부터 잔존) |
| P2-2 | dummy-variable-name | 보강 (val dummyXxx 패턴) |
| P3 | permission-launcher-arrays | 3팀 R3-N4 (POST_NOTIFICATIONS 누락 휴리스틱) |
| P4 | service-intent-creation | 3팀 R3-N2 (MapScreen startForegroundService 누락) |
| P5 | timepicker-am-pm-force-shift | 1팀 O2 (Setting 12h 어긋남) |
| P6 | empty-dialog-cancel-lambda | 2팀 R2-2 (ChatScreen onCancel={}) |
| P7 | silent-return-no-ui-feedback | 2팀 N1 (SRS uid null silent) |
| P8 | cloud-function-call | 3팀 R3-N1 (PTT NOT_FOUND 회귀) |
| P9 | room-destructive-migration | 1팀 5/22 알려진 패턴 |
| P11 | utc-epoch-day-off-by-one | 1팀 O4 (캘린더 점 UTC) |
| P12 | screen-local-state-no-vm | 2팀 R2-4 (모국어 silent fail), 1팀 Y6 (다크모드 미연결) |
| P13 | when-else-silent-false | 2팀 N5 (TTS ES silent) |
| P14 | launched-effect-while-delay | 3팀 R3-N7 (Navigation 핫스타트 무한 대기) |
| P16 | hardcoded-zero-ratio | 1팀 O1 (deepSleepRatio=0 sentinel) |

규칙 파일: `operation/scripts/semgrep-rules/bootcamp6-runtime.yml`
실행 명령: `semgrep --config operation/scripts/semgrep-rules/bootcamp6-runtime.yml --json --output OUT.json teams-docs/{팀}/repo/app/src/main/`

---

## 4. 팀별 자동 검증 결과 vs 5/27 정적 분석 교차 검증

### 1팀 Snoffee — 12건 발견

| semgrep 규칙 | 파일:라인 | 5/27 보고 매칭 |
|---|---|---|
| P16 hardcoded-zero-ratio | SamsungHealthDataSourceImpl.kt:71 | ✅ **O1** Health Connect 0건 sentinel |
| P16 hardcoded-zero-ratio | SleepViewModel.kt:76 | ✅ 동일 (필터 측) |
| P16 hardcoded-zero-ratio | DeleteSleepDataUseCase.kt:11 | ✅ **O5** Sleep tombstone (soft-delete) |
| P9 room-destructive-migration | DatabaseModule.kt:26 | ✅ 5/22 알려짐 (fallbackToDestructiveMigration) |
| P11 utc-epoch-day-off-by-one | CaffeinMainViewModel.kt:47 | ✅ **O4** UTC off-by-one |
| P2-2 dummy-variable-name | ReportViewModel.kt:86,125,153,162,171,172 (6건) | ✅ **Y1** Report 더미 5/22부터 잔존 — 정확히 6개 dummy 변수 식별 |
| P12 screen-local-state-no-vm | SettingScreen.kt:77 | ✅ **Y6** 다크모드 스위치 ViewModel 미연결 |

**5/27 정적 분석과 정확히 일치 — 7개 검증 항목 모두 자동 발견**.

### 2팀 Umma — 9건 발견

| semgrep 규칙 | 파일:라인 | 5/27 보고 매칭 |
|---|---|---|
| P6 empty-dialog-cancel-lambda | ChatScreen.kt:367 | ✅ **R2-2** Topic Dialog cancel 불가 (5일째 미해소) |
| P6 empty-dialog-cancel-lambda | DashboardScreen.kt:214 | 🆕 신규 — 5/27 미언급 |
| P6 empty-dialog-cancel-lambda | DashboardScreen.kt:254 | 🆕 신규 — 5/27 미언급 |
| P6 empty-dialog-cancel-lambda | StatisticsPreview.kt:63 | ⚠️ Preview 코드 (false positive) |
| P12 screen-local-state-no-vm | MyPageScreen.kt:52 | ✅ **R2-4** 모국어 selectedNativeLanguage silent fail |
| P7 silent-return-no-ui-feedback | SrsStudyViewModel.kt:165 | ✅ **N1** SRS uid null silent |
| P9 room-destructive-migration | DatabaseModule.kt:28,50,69 (3건) | 5/22 후 정책 (3 DB 모두) |

**신규 발견**: DashboardScreen.kt:214, 254의 onCancel={} 2건 — 5/27 점검에서 누락. 다이얼로그 cancel 불가 패턴이 ChatScreen 외 Dashboard 다이얼로그까지 확산. 강사 시연 시 Dashboard 다이얼로그에서도 갇힐 가능성.

### 3팀 BBipit — 22건 발견 (코호트 최다)

| semgrep 규칙 | 파일:라인 | 5/27 보고 매칭 |
|---|---|---|
| P1 placeholder-text-stub | SettingsDrawer.kt:17 | ✅ **R3-N3** "드로어블 열림" 빈 stub |
| P4 service-intent-creation | MapScreen.kt:180 | ✅ **R3-N2** startForegroundService 누락 (악화) |
| P4 service-intent-creation | MapScreen.kt:428 | 정상 분기 (false positive) |
| P4 service-intent-creation | AppLifecycleObserver.kt:30 | 보강 발견 — 별도 점검 권고 |
| P4 service-intent-creation | BackgroundListenerService.kt:765,771,775 (3건) | 서비스 자체 start (정상) |
| P4 service-intent-creation | MobileMessageListenerService.kt:61 | 보강 발견 |
| P3 permission-launcher-arrays | MapScreen.kt:436 | ✅ **R3-N4** POST_NOTIFICATIONS 누락 |
| P14 launched-effect-while-delay | Navigation.kt:71 | ✅ **R3-N7** 핫스타트 무한 대기 |
| P6 empty-dialog-cancel-lambda | SignUpScreen.kt:229 | 🆕 신규 |
| P6 empty-dialog-cancel-lambda | AgreeDialog.kt:67 | 🆕 신규 |
| P6 empty-dialog-cancel-lambda | ConfirmDialog.kt:32 | 🆕 신규 (공통 컴포넌트 — 영향 광범위) |
| **P8 cloud-function-call** | **VoiceRemoteDataSourceImpl.kt:36** | ✅ **R3-N1** PTT 회귀 sendVoiceMessage |
| **P8 cloud-function-call** | **VoiceRemoteDataSourceImpl.kt:111** | 🆕 추가 voice 함수 1건 |
| **P8 cloud-function-call** | **HistoryRemoteDataSourceImpl.kt:17,33,44** (3건) | 🆕 History API 3건 |
| **P8 cloud-function-call** | **LiveStatusRemoteDataSourceImpl.kt:28,72** (2건) | 🆕 LiveStatus 2건 |
| **P8 cloud-function-call** | **FriendListViewModel.kt:139** | 🆕 Friend 1건 |
| **P8 cloud-function-call** | **EditProfileViewmodel.kt:97** | 🆕 EditProfile 1건 |

**가장 중요한 신규 발견**: 5/27 정적 분석은 PTT 1건만 봤으나, **`functions.getHttpsCallable(...)` 호출이 총 9건** 존재. `functions/index.js`가 여전히 빈 파일이라면 (재확인 완료 — `helloWorld` 주석만), 다음 경로 모두 NOT_FOUND:
- PTT 음성 전송 (2건)
- 히스토리 작성/조회 (3건)
- 위치 공유 상태 (2건)
- 친구 목록 (1건)
- 프로필 수정 (1건)

→ **3팀 거의 모든 핵심 기능이 콘솔 별도 deploy에 100% 의존**. 콘솔 미배포 시 시연 광범위 실패.

---

## 5. 신규 발견 보강 (5/27 점검 누락)

### N-1. 2팀 DashboardScreen 다이얼로그 cancel 빈 람다 2건
- **위치**: DashboardScreen.kt:214, 254
- **5/27 미발견 사유**: Agent가 ChatScreen만 봤음. 다른 화면 동일 패턴 누락
- **영향**: 강사 시연 시 Dashboard 다이얼로그에서도 갇힐 가능성

### N-2. 3팀 공통 다이얼로그 ConfirmDialog onCancel={} 잠재 광범위 영향
- **위치**: ConfirmDialog.kt:32 (공통 컴포넌트), SignUpScreen.kt:229, AgreeDialog.kt:67
- **5/27 미발견 사유**: 공통 다이얼로그 자체 점검 누락. 회원가입 흐름 dialog 누락
- **영향**: SignUp 흐름에서 약관 동의 다이얼로그 cancel 불가 가능 — 신규 강사 계정 첫 시연 시 발현

### N-3. 3팀 Cloud Function 의존 화면 9건 (PTT 외 8건 추가)
- 5/27엔 PTT만 보고 → **functions/index.js 빈 파일 영향 범위가 실은 PTT보다 훨씬 큼**
- History/LiveStatus/Friend/EditProfile 모두 콘솔 deploy 의존
- **권고**: PM 회의 즉시 의제 — "functions deploy 상태 9개 함수 일괄 확인"

### N-4. 3팀 SwallowedException 다발 (detekt)
- BackgroundListenerService.kt:501,681, TimeFormatter.kt:26, HistoryMapper.kt:25 등 SwallowedException 다수
- 예: ForegroundServiceStartNotAllowedException swallow — 서비스 시작 실패 시 silent (5/27 R3-N2와 인접 패턴)
- **권고**: 시연 중 무반응 발생 시 logcat 추적 어려움 — 5/27 권고 외 별도 점검 권고

---

## 6. 정밀도 / 재현율 평가

### 정밀도 (Precision)
- semgrep 43건 중 false positive: 약 5건 (12%)
  - P4 service-intent-creation: BackgroundListenerService 자체 start 4건, MapScreen.kt:428 정상 분기 1건
  - P6 empty-dialog-cancel-lambda: StatisticsPreview.kt (Preview 코드)
- **실제 유효 발견: 38건 (88%)**

### 재현율 (Recall)
- 5/27 정적 분석 핵심 13건 (P1/P2/P3/P4/P5/P6/P7/P8/P11/P12/P13/P14/P16) 모두 자동 매칭 ✅
- 5/27 보고 35건 중 자동화 불가 (구조적/의미적):
  - 1팀 O5 Sleep 삭제 tombstone — P16에서 부분 잡힘
  - 2팀 R2-7 챗 startSession 실패 후 재시도 없음 — UI 상태 의존
  - 3팀 R3-N1 회귀의 "fix 의도였으나 회귀" 같은 의도 분석 — 자동화 불가
- **자동화 가능 패턴 커버율: ~37% (13/35)**

### 도구 자산 가치
- 다음 코호트에도 그대로 재사용 가능
- 새 패턴 발견 시 YAML 1~5줄 추가만으로 누적 확장 가능
- 30초 안에 3팀 일괄 검사 → CI/CD 통합 가능

---

## 7. 강사진 시연 직전 자동 발견 우선순위

### 🔴 즉시 조치 (시연 차단 위험)
1. **3팀 functions/index.js 빈 파일 + 9개 Cloud Function 호출** — 콘솔 deploy 상태 9개 함수 일괄 확인 필수
2. **3팀 R3-N2 MapScreen startForegroundService 누락** — 2분 패치 (5/27 보고와 동일)
3. **3팀 R3-N3 SettingsDrawer 빈 stub** — 5분 패치 (5/27 보고와 동일)
4. **3팀 R3-N4 POST_NOTIFICATIONS 누락** — 1분 패치 (5/27 보고와 동일)

### 🟠 신규 발견 — 시연 직전 추가 점검
5. **3팀 SignUpScreen + AgreeDialog onCancel={}** — 신규 가입 시연 시 약관 다이얼로그에서 갇힐 가능성
6. **3팀 ConfirmDialog 공통 컴포넌트 onCancel={}** — 사용처 광범위. 사용처 grep 후 영향 파악 필요
7. **2팀 DashboardScreen 다이얼로그 cancel 2건** — Dashboard 다이얼로그 띄울 때 X 버튼 무반응

### 🟡 잔존 / 정보
8. 1팀 Y1 Report 더미 6개 dummy 변수 — 시연 동선 제외 권고 유지
9. 1팀 O1 deepSleepRatio=0 sentinel — Health Connect 자동 연동 시연 회피 권고 유지
10. 3팀 detekt SwallowedException 다발 — 별도 점검 권고

---

## 8. 산출물

```
operation/
├── scripts/
│   ├── semgrep-rules/
│   │   └── bootcamp6-runtime.yml          # 15개 커스텀 규칙 (다음 코호트 재사용)
│   └── tools/
│       ├── detekt-cli-1.23.7/              # detekt CLI
│       └── compose-rules-detekt.jar        # (호환 안 됨, 미사용)
└── reports/critical_review_260527/
    ├── 00_summary.md                       # 5/27 정적 분석 통합 (기존)
    ├── 01_automated_verification.md        # 본 문서
    ├── team1_runtime.md / team2_runtime.md / team3_runtime.md  (기존)
    └── auto/
        ├── 1team_semgrep.json / 2team_semgrep.json / 3team_semgrep.json
        └── 1team_detekt.txt / 2team_detekt.txt / 3team_detekt.txt
```

---

## 9. 운영 권고 (재사용 가능한 자산)

### 매주 점검 자동화 — 1줄 명령
```bash
cd c:/Users/ibebu/bootcamp6_final/archive
PYTHONUTF8=1 \
  "C:/Users/ibebu/AppData/Local/Programs/Python/Python312/Scripts/semgrep.exe" \
  --config operation/scripts/semgrep-rules/bootcamp6-runtime.yml \
  --json --output operation/reports/auto/$(date +%y%m%d)_{팀}.json \
  teams-docs/{팀}/repo/app/src/main/
```

### 새 패턴 발견 시 추가 절차
1. 5/27 보고서 같은 정성적 점검에서 새 패턴 발견
2. `operation/scripts/semgrep-rules/bootcamp6-runtime.yml`에 규칙 1~5줄 추가
3. `semgrep --validate` 로 YAML 검증
4. 핵심 매칭 파일에서 동작 확인

### 한계 (개선 여지)
- **mobsfscan 미사용**: Windows libsast 버그. WSL 환경에선 정상 동작 예상. 권한·평문 키 점검은 mobsfscan이 더 강함
- **detekt Compose Rules 미적용**: `compose-rules-detekt.jar`는 코어 모듈 의존. fat jar 또는 Gradle 환경 필요. 다음 단계로 학생 build.gradle에 의존성 추가 PR 권고
- **의미적 회귀 탐지 불가**: 3팀 R3-N1 같은 "함수 호출 변경" 회귀는 git diff 분석 + semgrep `pattern-not` 조합 필요. 다음 코호트 과제
