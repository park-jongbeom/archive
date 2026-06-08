# 박종범 테스터 — 1팀 Snoffee 베타 배포본 점검 (2026-06-08)

> **대상**: 박종범 본인 (Note 10+ AOS12 / Z Fold 4 AOS16 병행)
> **소스 최신화**: `develop` @ `4c8e29b` (origin/develop, #116 feature/wear_mainUI 머지본) — 1팀은 **release 브랜치·태그가 없어 `develop`이 베타 배포 라인**
> **점검 방식**: 코드 정적분석(read-only) + 공식 문서 교차검증. 코드 라인 근거 명시.

> ⚠️ **브랜치 가정 주의**: 1팀은 `release/*` 브랜치도 git tag도 없음. `develop`(06-08 최신)을 배포본으로 간주해 최신화했음. 만약 팀이 다른 브랜치로 베타 빌드를 뽑았다면 알려주면 즉시 그 브랜치로 다시 맞춤.

---

## 0. 1줄 결론

> **배포 부적합 — "부분 완성 베타".** 로컬 카페인 기록 + 수면 리포트 코어는 **정상 동작 + 반감기 수학도 정확**하지만, README가 내세운 **간판 기능(Gemini AI 컷오프·주간 인사이트 / FCM 푸시 / 갤럭시 워치)은 전부 빈 스텁이거나 키 불일치로 깨져 있음.** 본인 단말 테스트에서 **워치가 가장 눈에 띄게 고장** 나 보일 것이고, **AI 기능은 화면에 아예 안 나타남.**

| README 간판 기능 | 실제 상태 |
|---|---|
| 카페인 잔류량(반감기) | ✅ 동작, 수학 정확 |
| 수면 리포트(Health Connect) | ✅ 동작 (HC 미설치 시 주의) |
| AI(Gemini) 컷오프/주간 인사이트 | 🔴 미구현 스텁, UI 미연결 |
| FCM 푸시 | 🔴 0바이트 빈 파일, 미등록 |
| 갤럭시 워치 | 🔴 0mg 표시 + 영구 "연결 끊김" + 알림 안 옴 |
| Firebase Functions | 🔴 없음 |
| Firestore 규칙 / release 서명 | 🟠 repo에 없음 |

---

## 1. 🔴 배포 블로커 (코드 근거 + 본인 단말 검증법)

### 🔴 S-01. 갤럭시 워치 잔류 카페인 게이지가 **항상 0 mg** (데이터 키 불일치)
- 폰은 `residualCaffeineMg`로 쓰고, 워치는 `residualMg`로 읽음:
  - 폰 송신: [PhoneDataClient.kt:36](../repo/app/src/main/java/com/snoffee/app/data/wear/PhoneDataClient.kt#L36) `putDouble("residualCaffeineMg", …)`
  - 워치 파싱: [WearDataClient.kt:143](../repo/wear/src/main/java/com/snoffee/wear/data/WearDataClient.kt#L143) `getDouble("residualMg", 0.0)` → 키 없음 → **기본 0.0**
  - 워치 UI: [HomeScreen.kt:109](../repo/wear/src/main/java/com/snoffee/wear/presentation/home/HomeScreen.kt#L109) "0 mg" 고정
- **본인 검증**: 폰에 카페인 기록 후 워치 홈 → 숫자가 0으로 고정인지. (riskLevel/대사시간 등 다른 키는 일치하므로 같이 동기화됨, **mg 숫자만** 깨짐)

### 🔴 S-02. 워치 ↔ 폰 연결 핸드셰이크가 **영원히 실패** (capability 선언 누락)
- 워치가 capability `"verify_snoffee_phone_app"`를 조회([WearDataClient.kt:35,66-72](../repo/wear/src/main/java/com/snoffee/wear/data/WearDataClient.kt#L35))하는데, **폰 앱에 `res/values/wear.xml`의 `android_wear_capabilities` 선언이 없음**(repo에 wear.xml 자체 없음). → `getCapability().nodes` 항상 empty → [handleDisconnect()](../repo/wear/src/main/java/com/snoffee/wear/data/WearDataClient.kt#L105) → 워치가 **페어링돼 있어도 항상 "핸드폰 유실 또는 연결이 끊어졌습니다"** 표시.
- 웹 근거: [Wear Data Layer — wear.xml capability 선언 + 동일 applicationId 필수](https://developer.android.com/training/wearables/data/messages)
- **본인 검증**: 워치 페어링 후 앱 진입 → **무조건 "연결 끊김"** 뜨면 이 누락. 워치 영역에서 가장 먼저 보일 결함.

### 🔴 S-03. 워치 컷오프 알림이 **절대 안 울림** (워커 미스케줄 + 하드코딩)
- [CaffeineCutoffWorker.kt:22](../repo/wear/src/main/java/com/snoffee/wear/worker/CaffeineCutoffWorker.kt#L22) `val residual = 60.0` **하드코딩**(실제 계산 호출은 21번 줄 주석처리). 게다가 어디에서도 `WorkManager.enqueue`/`PeriodicWorkRequest`가 **없음** → 워커가 등록조차 안 됨. [BootReceiver.kt:8-13](../repo/wear/src/main/java/com/snoffee/wear/receiver/BootReceiver.kt#L8)는 로그만 찍고 재등록은 TODO.
- **본인 검증**: 카페인 많이 기록해도 워치 컷오프 알림 안 옴 → 정상(=고장)임을 확인. 죽은 기능.

### 🔴 S-04. FCM 푸시 + NotificationHelper가 **0바이트 빈 파일**
- [FcmService.kt](../repo/app/src/main/java/com/snoffee/app/presentation/notification/FcmService.kt) = 0바이트(매니페스트 등록도 없음), [NotificationHelper.kt](../repo/app/src/main/java/com/snoffee/app/presentation/notification/NotificationHelper.kt) = 0바이트. 앱바 종모양 아이콘은 [MainActivity.kt:105-107](../repo/app/src/main/java/com/snoffee/app/MainActivity.kt#L105) 빈 람다. **푸시·폰측 알림 헬퍼가 존재하지 않음.** README의 FCM은 허위.

### 🔴 S-05. release 서명 설정 없음 + minify 비활성
- [build.gradle.kts:23-31](../repo/app/build.gradle.kts#L23) `release { isMinifyEnabled = false }` — **`signingConfigs`/`signingConfig` 블록 자체 없음**. wear 모듈도 동일. → release AAB가 **미서명**으로 빌드 → Play 업로드 불가. [체크리스트 🟠-09](../../PLAY_STORE_RELEASE_CHECKLIST_260601.md)

### 🟠 S-06. Firestore 규칙 / Functions repo에 없음
- `firebase.json`/`firestore.rules`/`functions/` **전부 없음**. 앱은 Firestore `drinks` 컬렉션(DB id `"drink"`)을 직접 read([DrinkRemoteDataSourceImpl.kt:31](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L31)). 규칙 미배포 시 — locked 모드면 **음료 검색이 빈 목록**, open 모드면 **DB 개방**. 어느 쪽이든 음료 검색이 소스 밖 설정에 의존.

### 🟢 S-07. 시크릿은 정상 (참고)
- `local.properties` + `app/google-services.json` 모두 **git-ignored + 미커밋**(확인됨). 소스 `.kt`에 하드코딩 키 없음. `AIza…`는 `app/build/` 생성물(미커밋)에만 존재. **Gemini 키는 호출부 자체가 없어 키도 없음.** → 단, **빌드하려면 본인이 google-services.json + local.properties를 직접 넣어야 함.**

---

## 2. 🟠 기능 버그 / 크래시 리스크

| ID | 위치 | 증상 / 본인 단말 검증 |
|---|---|---|
| **F-01 AI 기능 미연결** | [GeminiRepositoryImpl.kt:19,24](../repo/app/src/main/java/com/snoffee/app/data/repository/GeminiRepositoryImpl.kt#L19) `TODO()` | 컷오프/주간 인사이트 usecase가 **어떤 ViewModel/UI에도 연결 안 됨**(grep 확인). 즉 **탭하면 크래시가 아니라, 화면에 버튼/기능이 아예 안 나타남.** 홈의 컷오프 시간은 로컬 수학([CalculateResidualUseCase](../repo/app/src/main/java/com/snoffee/app/domain/usecase/CalculateResidualUseCase.kt))이지 Gemini 아님 |
| **F-02 Health Connect 미설치 크래시 위험** | [SamsungHealthDataSourceImpl.kt:24](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/SamsungHealthDataSourceImpl.kt#L24) `by lazy { getOrCreate(context) }` | `getSdkStatus` 가드 없는 lazy 생성. **Note10+(AOS12)에 Health Connect 미설치 가능성** → 화면 가드 밖 경로에서 `getLatestSleepData()` 호출 시 `IllegalStateException`. 리포트 화면은 catch로 토스트화([ReportViewModel.kt:365](../repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L365))되나, **본인: HC 미설치 상태로 수면/리포트 진입 시 크래시 없이 빈/에러 처리되는지 확인** |
| F-03 워치 설정 동기화 무동작 | [SettingViewModel.kt:17,22](../repo/wear/src/main/java/com/snoffee/wear/presentation/setting/SettingViewModel.kt#L17) | 워치가 `/settings/*`로 MessageClient 송신하나 **폰측 수신기 없음**(폰은 `/caffeine` DataClient만 처리). 워치 설정 토글 → 폰에 반영 안 됨 |
| F-04 Room 파괴적 마이그레이션 | [DatabaseModule.kt:30](../repo/app/src/main/java/com/snoffee/app/data/di/DatabaseModule.kt#L30) `fallbackToDestructiveMigration` | DB 버전(현재 4) 올라가는 업데이트 시 **로컬 데이터 전체 삭제**. 베타 허용, production 전 마이그레이션 필요 |
| F-05 오프라인 첫 실행 음료 검색 공백 | [DrinkRemoteDataSourceImpl.kt:42](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L42) | 비행기 모드 첫 실행 시 catch→빈 목록, **에러 메시지 없이 음료 검색이 비어 보임** |

### 🟢 정상 확인 (회귀만 재검증)
- **반감기 수학 정확**: [CaffeineCalculator.kt:8-20](../repo/app/src/main/java/com/snoffee/app/core/caffeine/CaffeineCalculator.kt#L8) 지수감쇠 `intake * 0.5^(t/halfLife)`, `require(halfLife>0)`(÷0 없음)·미래시각 클램프(시계 역행 안전). 0카페인 음료도 안전.
- **POST_NOTIFICATIONS 런타임 요청** API33+ 가드 정상([OnboardingPermissionScreen.kt:91-99](../repo/app/src/main/java/com/snoffee/app/presentation/onboarding/OnboardingPermissionScreen.kt#L91)).
- **portrait 잠금 + fontScale 캡(0.95~1.2)** 적용([MainActivity.kt:49](../repo/app/src/main/java/com/snoffee/app/MainActivity.kt#L49)) — 코호트 UI 안정화 조치 반영. 가로/폰트max 깨짐 회피.
- **ACTIVITY_RECOGNITION** 선언했으나 런타임 요청·심박 사용 없음 → 권한 카드 문구("심박수 기반 반감기 계산")가 **오해 소지**(실제론 사용자 선택 민감도 상수). 🟡 신뢰성/UX.

---

## 3. 🧭 앱 방향성 기반 — 본인이 일부러 시도할 실발생 오류 시나리오

카페인·수면·AI·워치 앱 특성상:

**카페인/시간**
1. 음료 기록 후 **자정 넘김** — `ACTION_DATE_CHANGED`로 리포트/메인 재로드([ReportViewModel.kt:49-62](../repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L49)). "오늘" 합계 리셋 + 잔류량 이월 정확한지.
2. **0 카페인 커스텀 음료** — ÷0 없이 "안전" 표시되는지.
3. **시계 뒤로** (consumedAt > now) — 클램프되어 크래시 없는지([CalculateResidualUseCase:29](../repo/app/src/main/java/com/snoffee/app/domain/usecase/CalculateResidualUseCase.kt#L29)).
4. 여러 날 5잔+ 기록 → 5일 윈도우 잔류 합산.

**수면/Health Connect**
5. **Health Connect 제거 후** 수면/리포트 진입 → 크래시 NO, 빈/에러 처리 YES인지 (F-02 핵심). 웹 근거: [HC는 AOS12 단말에 미설치일 수 있음, getSdkStatus로 SDK_UNAVAILABLE 확인 필요](https://developer.android.com/health-and-fitness/health-connect/get-started)
6. READ_SLEEP 거부 → 수면 목록 빈/리포트 "0h 00m", 크래시 없음.
7. 수면 기록 0건 → 빈 상태 뷰([ReportViewModel:138](../repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L138) `isDbEmpty`).

**워치 (고장 영역 — 실패가 정상)**
8. 워치 앱 진입 → **영구 "연결 끊김"**(S-02) — 워치 최대 결함.
9. 연결돼 보여도 **워치 mg 게이지 0**(S-01).
10. **워치 컷오프 알림 영원히 안 옴**(S-03).
11. 워치 설정 토글 → **폰 무반영**(F-03).

**AI/네트워크**
12. "AI 컷오프"/"주간 인사이트" 버튼 → **연결된 게 없음**(F-01). 보이는 컷오프는 로컬 수학.
13. 비행기 모드 첫 실행 → **음료 검색 빈 화면**, 에러 없음(F-05).

---

## 4. 본인 단말 우선순위 (6/08 배포본 기준)

| 순위 | 항목 | 이유 |
|---|---|---|
| 🔥 1 | **F-02 Health Connect 미설치/거부 크래시** | 유일하게 실제 크래시로 갈 수 있는 경로. Note10+ AOS12에서 HC 미설치 가능성 |
| 🔥 2 | **S-01~S-03 워치 3종** | 간판 기능인데 전부 고장. 시연·평가 직격 |
| 🔥 3 | **카페인 자정/시계역행/0카페인** | 코어 기능 경계값(수학은 안전하나 UI 리셋 확인) |
| ⭐ 4 | **F-01 AI 미연결 / S-04 FCM 빈 파일** | README↔실구현 간극 — "없는 기능"임을 명확히 보고 |
| 5 | S-05 서명 / S-06 규칙 / F-04 마이그레이션 | 배포 인프라 |

> **요약 보고 포인트**: 1팀은 "로컬 코어는 견고, 간판(AI·FCM·워치)은 미완"이 핵심 메시지. 워치/AI는 **버그가 아니라 미구현**이라 본인 단말에서 "고장"으로 보여도 크래시는 대부분 없음 — 유일한 실크래시 후보는 **F-02 Health Connect 미설치**다.

---

## 변경 이력
| 일자 | 변경 |
|---|---|
| 2026-06-08 | develop @ 4c8e29b 배포본 최신화 + 정적분석/웹검증 기반 블로커·실발생오류 정리 (1팀 test 폴더 신규) |
