# 1팀 Snoffee — 화면 동작/크래시 재점검 (2026-05-22)

> 관점: 강사진이 실 단말 빌드 후 화면 클릭 시 만나는 버그
> 보안/구조 이슈는 [04_team1_findings.md](04_team1_findings.md) 참조 (중복 제외)
> 점검 방식: 정적 코드 분석 (실 단말 빌드/실행 미수행)
> 점검 범위: `teams-docs/1team/repo/app/src/main/`

---

## 한눈에 — Top 5 시연 리스크 시나리오

| # | 시나리오 | 결과 | 1차 차단 위치 |
|---|---------|------|---------------|
| 1 | 강사가 권한 거부하고 다음 진행 시도 | **온보딩 dead-end (무반응)** | [OnboardingPermissionScreen.kt:73-88](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L73) |
| 2 | 리포트 탭 클릭 (5개 서브탭 어디든) | **항상 같은 더미 데이터** (스타벅스 150mg 등) | [ReportViewModel.kt:84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84) |
| 3 | 음료 검색 첫 진입 → 검색어 입력 안 함 | **Firestore 의존 (오프라인/규칙 차단 시 무한 빈 결과)** | [DrinkRemoteDataSourceImpl.kt:38-41](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L38) |
| 4 | 수면 탭 진입 → Health Connect 미설치/권한 revoke | **SecurityException 가능 (try/catch 없음)** | [SamsungHealthDataSourceImpl.kt:52-72](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L52) → [SleepViewModel.kt:55-67](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L55) |
| 5 | 설정 탭 → 키/체중/카페인 민감도 변경 시도 | **모든 항목 하드코딩, 클릭해도 변경 안 됨** | [SettingScreen.kt:62-122](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L62) |

---

## 화면별 상태 매트릭스

| 화면 | 진입 | 정상 동작 | 빈 상태 UI | 에러/거부 처리 | 비고 |
|------|------|-----------|------------|----------------|------|
| Onboarding Intro | 🟢 | 🟢 | - | - | 단순 "다음" 버튼 |
| Onboarding Permission | 🟢 | 🟡 | - | 🔴 dead-end | 권한 거부 → 다음 진행 0% |
| Onboarding PersonalInfo | 🟢 | 🟡 | - | - | 입력값 어디에도 저장 안 됨 (04 §A2) |
| Onboarding Complete | 🟢 | 🟢 | - | - | "시작하기" → Home 이동 |
| Home | 🟢 | 🟢 | 🟢 "오늘 기록 없음" 카드 | 🟢 ErrorState + 재시도 | 5분 자동 새로고침 ⭐ |
| Caffeine Main | 🟢 | 🟢 | 🟢 "오늘 기록된 카페인이 없어요" | 🟡 Snackbar | 캘린더 점 표시 OK |
| Caffeine Search | 🟢 | 🟡 | 🟢 "검색 결과 없습니다" | 🟡 try/catch만 (에러 메시지 표시 약함) | Firestore 의존 |
| CaffeineInputDialog (직접 등록) | 🟢 | 🟡 | - | - | 음료명 빈 값 검증 없음 |
| Sleep | 🟢 | 🟡 | 🟡 score=0 분기 | 🔴 SecurityException 가능 | Health Connect ANR/Crash 위험 |
| SleepDialog | 🟢 | 🟢 | - | 🟢 isSavingError + Retry UI | 16시간+ 검증 있음 ⭐ |
| Report | 🟢 진입 | 🟡 **항상 더미** | 🟢 ReportEmptyView (단 isDbEmpty=false 강제) | - | 실 데이터 호출 주석 처리 |
| Report - 기간 탭 | 🟢 | 🟡 더미 | 🟢 | - | DatePicker 정상 |
| Report - 일간 탭 | 🟢 | 🟡 더미 | 🟢 | - | - |
| Report - 주간 탭 | 🟢 | 🟡 더미 차트 | 🟢 | - | - |
| Report - 월간 탭 | 🟢 | 🟡 더미 차트 | 🟡 highCaffeineDaySleepTime 항상 "0h 00m" → 비교바 미표시 | - | UiState 미계산 필드 사용 |
| Report - 추이 탭 | 🟢 | 🟡 더미 + 빈 차트 | 🟡 monthlyCaffeineTrend Map 항상 emptyMap | - | UiState 미계산 필드 사용 |
| Setting | 🟢 | 🔴 모든 클릭 no-op | - | - | ViewModel 빈 파일, 모두 하드코딩 |
| Watch app (Wear OS) | 🔴 | - | - | - | AndroidManifest에 Activity 0건, 모듈 코드 0줄 |
| FCM / Notification | 🔴 | - | - | - | FcmService.kt, NotificationHelper.kt 빈 파일 |

🟢 정상 · 🟡 부분 · 🔴 미구현 또는 차단

---

## 발견 상세 (우선순위 순)

### 🔴 CRITICAL — 시연 직격탄 (반드시 패치 후 시연)

#### R1-1. 온보딩 권한 거부 시 무반응 dead-end ⭐⭐⭐

- **화면**: [OnboardingPermissionScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt) (앱 첫 실행 시 무조건 통과해야 하는 화면)
- **재현 단계**:
  1. 앱 첫 실행 (또는 데이터 초기화 후)
  2. Intro 화면에서 "시작하기" 탭
  3. Permission 화면에서 "권한 허용하고 다음으로" 버튼 탭
  4. Health Connect 권한 시트에서 **거부**
- **결과**:
  - [OnboardingPermissionScreen.kt:73-88](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L73): `healthPermissionLauncher` 콜백에서 `if (grantedPermissions.containsAll(healthPermissions))` 조건이 false → `else` 분기 자체 없음 → `onNextClick()` 호출 안 됨
  - 화면이 그대로 멈춤. 버튼은 다시 탭 가능하나 권한 시트가 다시 뜨거나 같은 무반응 반복
  - **앱 사용 자체 불가** (PersonalInfo / Complete 진입 불가)
- **권고**: 거부 분기에 `onNextClick()` 호출 또는 "권한 없이 계속" 안내 추가. 최소 패치:
  ```kotlin
  ) { grantedPermissions ->
      // 승인 여부와 무관하게 다음 단계로
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else {
          onNextClick()
      }
  }
  ```
  - **04 §C1-4와 동일 이슈, 시나리오 강조 재정리**

#### R1-2. ReportScreen — 모든 탭에서 동일한 가짜 숫자 ⭐⭐⭐

- **화면**: [ReportScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportScreen.kt) → 하단 탭 "리포트"
- **재현 단계**:
  1. 앱 정상 진입 → 하단 탭 "리포트" 탭
  2. 상단 탭 어디든 ("기간/일간/주간/월간/추이") 탭
- **결과**:
  - [ReportViewModel.kt:84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84): `loadReportData()` 내부에 하드코딩 더미 4건 (스타벅스 아메리카노 150mg, 돌체라떼 230mg, 빽다방 콜드브루 200mg, 핫식스 100mg) + 7일 더미 차트
  - 실제 `getReportUseCase` 호출은 [L216-224](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L216) 모두 주석 처리
  - `isDbEmpty`는 [L226-229](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L226) 더미가 `isEmpty=false`로 들어가므로 항상 false → ReportEmptyView 절대 표시되지 않음
  - 사용자가 음료를 새로 추가해도 리포트 숫자는 **불변**
- **권고**: 더미 블록 [L84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84) 삭제 + 주석 [L216-224](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L216) 해제. 단 빈 DB 시 ReportEmptyView가 표시되므로 **시연 시나리오에서 음료 1~3건 사전 입력 필수**

#### R1-3. SleepScreen 진입 시 Health Connect SecurityException ⭐⭐

- **화면**: [SleepScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepScreen.kt) → 하단 탭 "수면"
- **재현 단계**:
  1. 하단 탭 "수면" 탭
  2. (사전 조건: Health Connect 앱 미설치 또는 권한 revoke)
- **결과**:
  - [SleepViewModel.kt:50-53](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L50) `init { refreshSleepData() }` 자동 호출
  - [SleepViewModel.kt:67](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L67) `sleepRepository.getSleepDataByDateRange(...)` 호출
  - [SleepRepositoryImpl.kt:45-49](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/SleepRepositoryImpl.kt#L45) `healthDataSource.getSleepDataByDateRange()` 시도 (가드 없음)
  - [SamsungHealthDataSourceImpl.kt:52-61](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L52) `healthConnectClient.readRecords(...)` 호출 → 권한 미보유 시 `SecurityException`, 미설치 시 `HealthConnectClient.getOrCreate` 단계에서 `IllegalStateException` 가능
  - try/catch 없음 → `viewModelScope.launch` 안에서 propagate → **앱 크래시 또는 ANR**
  - 단 onboarding에서 권한을 통과해야 진입 가능하므로, **권한이 한 번이라도 거부된 적이 있거나 OS에서 revoke된 경우** 재현됨
- **권고**:
  - `SleepRepositoryImpl.getSleepDataByDateRange()`에 `healthDataSource.hasPermissions()` 가드 (이미 [SamsungHealthDataSourceImpl.kt:75-80](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L75)에 존재) 추가, 권한 미보유 시 `localDataSource` 경로로 fallback
  - `SleepViewModel.refreshSleepData()`를 `runCatching`/`try`로 감싸기 — Home 패턴([HomeViewModel.kt:53](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeViewModel.kt#L53))과 동일하게

#### R1-4. SettingScreen — 모든 항목이 하드코딩 + ViewModel 빈 파일 ⭐⭐

- **화면**: [SettingScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt) → 하단 탭 "설정"
- **재현 단계**:
  1. 하단 탭 "설정" 탭
  2. "신장 168cm" / "체중 62kg" / "민감도 보통" / "수면 시간 22:30" / "기상 시간 06:30" 카드 탭
- **결과**:
  - 모든 값이 [SettingScreen.kt:102-121](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L102) 하드코딩 ("168cm", "62kg", "22:30", "06:30")
  - "신장/체중" 카드 탭 시 `onHeightWeightClick` 콜백 → MainActivity에서 빈 `{}` (호출 없음)
  - 카페인 민감도 [SettingScreen.kt:107](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L107) `CaffeineSensitivityCard()` 인자 없는 정적 카드 — 항상 "보통" 50% 슬라이더로 표시
  - 다크모드 스위치는 로컬 `mutableStateOf` 토글만 (앱 테마 반영 안 됨)
  - [SettingViewModel.kt:1-2](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingViewModel.kt#L1) 파일 자체 2줄 (package 선언만)
  - 결과: 강사가 설정을 변경하려고 시도하면 **모든 클릭이 no-op**. 사용자 입력값(키/몸무게/민감도)이 온보딩에서 들어와도 여기 반영 0건
- **권고**:
  - 시연 시나리오에서 "설정 탭은 추후 구현 예정" 명시적 안내
  - 또는 카드에 `enabled=false` 시각 처리 + "준비 중" 라벨
  - 빠른 회피: `Setting` 탭 자체를 [BottomNavBar.kt:50-54](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/core/navigation/BottomNavBar.kt#L50) `items` 리스트에서 임시 제외

#### R1-5. CaffeineMainScreen — "수정" 메뉴 클릭 시 no-op ⭐

- **화면**: [CaffeinMainScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainScreen.kt) → 하단 탭 "카페인" → 활동 로그 카드의 `⋮` (MoreVert) 메뉴
- **재현 단계**:
  1. Home에서 "카페인 추가" 누르고 검색·기록 1건 생성
  2. 하단 탭 "카페인" 탭
  3. 기록 카드 우측 `⋮` 메뉴 탭
  4. "수정" 항목 탭
- **결과**:
  - [CaffeinMainScreen.kt:242](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainScreen.kt#L242): `onEditClick = { record -> /* TODO: 수정 화면 이동 등 구현 */ }` — 빈 람다
  - 메뉴는 닫히지만 아무 화면도 열리지 않음 → 사용자 혼동
- **권고**: DropdownMenu에서 "수정" 항목을 임시 비활성화 (`enabled = false`) 또는 제거. "삭제"만 노출

---

### 🟠 HIGH — 시연 가능하나 즉시 발견됨

#### R1-6. CaffeineSearchScreen — Firestore 실패 시 빈 결과 무한 루프

- **화면**: [CaffeineSearchScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt) → "카페인 추가" 버튼
- **재현 단계**:
  1. Home → "카페인 추가" 버튼
  2. 검색창에 "아메리카노" 입력 후 Enter
  3. (사전 조건: Firestore named DB `drink` 미생성 또는 `drinks` 컬렉션 비어있음 또는 보안 규칙 차단)
- **결과**:
  - [DrinkRemoteDataSourceImpl.kt:25-41](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L25): 실패 시 `Log.e` + `emptyList()` 반환, **재시도 없음**
  - [CaffeineDataInitializer.kt:15-19](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/initializer/CaffeineDataInitializer.kt#L15): `localDataSource.getCount() == 0` 조건에서만 호출. Firestore가 첫 시도에서 실패하면 Room이 0건 상태로 남고, 다음 검색에서 다시 시도하긴 하지만 동일 실패 패턴 반복
  - 사용자 입장: "검색 결과가 없습니다" ([CaffeineSearchScreen.kt:274-281](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L274)) — 영원히 빈 결과
  - 강사가 Firebase Console 점검 안 했으면 100% 발현
- **권고**:
  - **시연 직전 sanity check**: Firebase Console에서 named DB `drink` + `drinks` 컬렉션 존재 + 보안 규칙(현재 부재, 04 §C1-12) 작성
  - 빠른 회피: "직접 등록하기" 배너 ([CaffeineSearchScreen.kt:231-236](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L231))로 음료 수동 추가 가능 (이 경로는 Room만 사용하므로 동작)

#### R1-7. CaffeineSearchScreen 빈 상태 화면 — 검색 결과 카운트 헤더 단독 노출

- **화면**: [CaffeineSearchScreen.kt:212-226](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L212)
- **재현 단계**: 검색 결과 0개일 때 (R1-6과 동일)
- **결과**: "검색 결과 0개" 텍스트가 상단에 표시되고, 한참 아래에야 "검색 결과가 없습니다" 텍스트 표시. 빈 영역 UX 좋지 않음
- **권고**: 0개일 때 카운트 헤더 숨김 처리

#### R1-8. CaffeineInputDialog — 음료명 빈 값으로 저장 가능

- **화면**: [CaffeinInputDialog.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/dialog/CaffeinInputDialog.kt) (CaffeineSearchScreen에서 "직접 등록하기" 진입)
- **재현 단계**:
  1. 카페인 추가 → "직접 등록하기" 배너 또는 EmptySearchState에서 + 버튼
  2. 음료명 비워둠
  3. 카페인 함량만 입력 → "기록하기" 탭
- **결과**:
  - [CaffeinInputDialog.kt:311-323](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/dialog/CaffeinInputDialog.kt#L311): `onConfirm` 호출 시 빈 `drinkName` 그대로 전달
  - Home 최근 기록에 빈 이름 카드가 추가됨 ([HomeScreen.kt:146-151](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeScreen.kt#L146))
- **권고**: `Button(enabled = drinkName.isNotBlank() && caffeineAmount > 0f, ...)` 추가

#### R1-9. CaffeineInputDialog — `intakeSize` 항상 0.0 + `intakeCaffeine`이 잘못된 필드

- **화면**: [CaffeineSearchScreen.kt:454-468](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L454) / [L362-377](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L362)
- **재현 단계**:
  1. 직접 등록 다이얼로그에서 카페인 함량 50, 음료 용량 250 입력 → "기록하기"
- **결과**:
  - DirectRegisterBanner의 `onConfirm` 콜백에서 `intakeSize = 0.0`, `intakeCaffeine = record.intakeSize` ← **record.intakeSize는 항상 0**
  - 즉 카페인 50mg 입력해도 Home 잔류량 계산에 **0mg로 들어감**
  - 또한 [CaffeinInputDialog.kt:319](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/dialog/CaffeinInputDialog.kt#L319): 다이얼로그 내부 `onConfirm`은 `intakeCaffeine = caffeineAmount.toDouble()` 으로 정상이지만, 외부 콜백에서 다시 `record.intakeSize`(0.0)를 넣음 → **이중 버그**
- **권고**: [CaffeineSearchScreen.kt:362-377](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L362) 및 [L454-468](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L454) 두 곳 모두 수정:
  ```kotlin
  onConfirm = { record ->
      viewModelScope.launch {
          viewModel.saveCaffeineRecord(
              record.copy(consumedAt = selectedTime.toTodayEpochMilli())
          )
      }
      showDialog = false
      onConfirmSuccess()
  }
  ```
  다이얼로그가 만든 record를 그대로 사용. 외부에서 필드 덮어쓰기 금지

#### R1-10. Report 월간 탭 — `highCaffeineDaySleepTime`/`lowCaffeineDaySleepTime` 미계산

- **화면**: [MonthlyReportView.kt:148-175](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/MonthlyReportView.kt#L148)
- **재현 단계**: Report 탭 → "월간" 탭 (더미 데이터 토글 후에도)
- **결과**:
  - UiState에 `highCaffeineDaySleepTime = "0h 00m"` ([ReportUiState.kt:26](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportUiState.kt#L26)) 기본값
  - ReportViewModel `loadReportData()`가 이 필드를 **절대 update하지 않음** → 항상 "0h 00m"
  - 화면에 "카페인 섭취량이 많았던 날 — 평균 수면 0h 00m" 표시 + 비교 차트 양쪽 0 → 시각적으로 비교 못함
- **권고**: 시연 시 "월간 탭은 추후 구현" 안내. 또는 더미라도 채워 넣기 (예: "7h 30m", "8h 15m")

#### R1-11. Report 추이 탭 — `monthlyCaffeineTrend` 항상 빈 Map

- **화면**: [TrendReportView.kt:68-100](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/TrendReportView.kt#L68)
- **재현 단계**: Report 탭 → "추이" 탭
- **결과**:
  - [TrendReportView.kt:78](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/TrendReportView.kt#L78) `uiState.monthlyCaffeineTrend` 사용
  - ReportViewModel `loadReportData()`가 `monthlyCaffeineTrend` 필드 update 0건 → 기본값 `emptyMap()` ([ReportUiState.kt:31](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportUiState.kt#L31))
  - "최고치: 0mg (-)" 표시. 차트 영역 빈 가로선만
- **권고**: ViewModel에서 `monthlyCaffeineChartData` (이미 계산됨)를 `monthlyCaffeineTrend`로 매핑 추가:
  ```kotlin
  monthlyCaffeineTrend = trendResult.monthlyCaffeineChartData
  ```

#### R1-12. HomeScreen — 첫 진입 5초간 빈 화면 + 백그라운드 5분 폴링

- **화면**: [HomeScreen.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeScreen.kt) / [HomeViewModel.kt:35-46](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeViewModel.kt#L35)
- **재현 단계**: 앱 첫 진입 후 Home 화면
- **결과**:
  - `init { startResidualRefresh() }` ([HomeViewModel.kt:35](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeViewModel.kt#L35)) + `LaunchedEffect(Unit) { viewModel.loadResidualCaffeine() }` ([HomeScreen.kt:52](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeScreen.kt#L52)) — **2회 동시 호출** (init와 LaunchedEffect 양쪽)
  - `refreshJob`이 5분마다 `loadResidualCaffeine()` 호출 ([HomeViewModel.kt:41-45](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeViewModel.kt#L41)) → 백그라운드에서도 계속 동작 (Lifecycle awareness 없음 → 배터리 소모 + Firestore 비용)
  - 실제 첫 진입은 `isLoading = recentLogs.isEmpty()` 분기로 즉시 로딩 표시되므로 사용자 체감 빈 화면은 짧음 ✅. 단 백그라운드 5분 폴링은 점검 필요
- **권고**:
  - `refreshJob`을 `repeatOnLifecycle(Lifecycle.State.STARTED)` 패턴으로 전환 권고. 시연에는 영향 없으나 출시 전 필수
  - init과 LaunchedEffect 중복 호출 한쪽 제거

#### R1-13. SleepScreen 빈 상태 — "기록된 시간 --, 점수 --" 두 카드 모두 빈 값 표시

- **화면**: [SleepScreen.kt:80-81](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepScreen.kt#L80) / [L126-130](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepScreen.kt#L126)
- **재현 단계**: 수면 탭 첫 진입 (수면 기록 0건)
- **결과**:
  - "기록된 시간 --" + "기록된 점수 --" 표시 ([SleepInfoCard](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepScreen.kt#L398) score==0 분기로 "--")
  - 월 평균도 "평균 시간 0h 00m", "평균 점수 --"
  - 빈 상태 안내 (예: "첫 수면을 기록해보세요") 없음 — 단지 비어있는 카드 4장만 보임
- **권고**: 빈 상태에서 안내 텍스트 + Empty illustration 추가 (낮은 우선순위)

---

### 🟡 MEDIUM — 체감 적으나 인지 필요

#### R1-14. ReportEmptyView "수면 기록하기" 버튼 dead-end

- **화면**: [ReportEmptyView.kt:89-92](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportEmptyView.kt#L89)
- **재현 단계**: (더미 데이터 제거 후) Report 탭 진입 → "수면 기록하기" 버튼 탭
- **결과**: `SleepDialog`가 열림 ([ReportScreen.kt:117-132](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportScreen.kt#L117)). 정상 동작 ✅. 단 카페인이 0건일 때 수면 기록만 유도하는 UX는 직관적이지 않음
- **권고**: 빈 상태 카피를 "카페인 또는 수면을 기록해보세요"로 변경 + 양쪽 버튼

#### R1-15. CaffeineMainScreen — 캘린더 점 표시(`recordedDates`) 누적 only

- **화면**: [CaffeinMainViewModel.kt:41-45](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L41)
- **재현 단계**:
  1. 카페인 기록 1건 등록
  2. 카페인 탭 진입
  3. 해당 기록 삭제
  4. 같은 화면에서 캘린더 확인
- **결과**:
  - [CaffeinMainViewModel.kt:42](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L42): `recordedDates = state.recordedDates + records.map { ... }` — **누적만 추가**, 삭제 후에도 점이 남음
  - 다음 화면 재진입 시에는 갱신되나, 같은 화면 머무를 때는 캘린더 점 미동기
- **권고**: `state.recordedDates + records...` → `records.map { ... }.toSet()` (교체로 변경)

#### R1-16. CaffeineMainScreen — 다른 날짜 선택해도 같은 "todayRecords" 표시

- **화면**: [CaffeinMainScreen.kt:240-244](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainScreen.kt#L240) / [CaffeinMainViewModel.kt:60-70](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L60)
- **재현 단계**:
  1. 어제 기록 1건 있는 상태 가정
  2. 카페인 탭 → 캘린더에서 어제 날짜 클릭
- **결과**:
  - [CaffeinMainViewModel.kt:68-69](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L68): `// TODO: 선택한 날짜 기준으로 기록 조회` + `loadTodayRecords()` 호출 — **항상 오늘 기록만 로드**
  - 어제를 선택해도 "오늘 활동 로그" 목록 그대로 표시 → 사용자 혼동
- **권고**: 시연에서 캘린더 다른 날짜 선택 시연 자제. 또는 날짜별 조회 UseCase 추가 (`getCaffeineRecordsByDateRange(start, end)`는 이미 [CaffeineRepositoryImpl.kt:41-53](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/CaffeineRepositoryImpl.kt#L41) 존재 — 1시간 작업)

#### R1-17. CaffeineMainScreen — `onPrevMonth` / `onNextMonth` 데이터 새로고침 누락

- **화면**: [CaffeinMainViewModel.kt:73-84](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L73)
- **재현 단계**:
  1. 카페인 탭 진입
  2. 캘린더 우측 화살표 탭 (다음 달로 이동)
- **결과**:
  - `currentYearMonth`만 update, 기록 재조회 0건. 캘린더가 다음 달로 넘어가도 점이 다 사라지지 않거나 데이터 미반영
  - SleepViewModel의 `onPrevMonth`/`onNextMonth`는 `refreshSleepData()` 호출하므로 비교 시 일관성 부족
- **권고**: 두 함수에 `loadTodayRecords()` 또는 월 단위 조회 추가

#### R1-18. OnboardingViewModel — height/weight/sensitivity 입력값 저장 0건

- **화면**: [OnboardingViewModel.kt:86-94](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/OnboardingViewModel.kt#L86)
- **재현 단계**:
  1. 온보딩 PersonalInfo 화면에서 키 175, 체중 70, 민감도 "민감함" 선택
  2. "다음" → Complete → "시작하기"
  3. Home에서 카페인 입력 → 잔류량 확인
- **결과**:
  - `completeOnboarding()`은 `setOnboardingCompleted(true)`만 호출. height/weight/sensitivity는 ViewModel state에만 보관되었다가 `onCleared` 시 소실 (04 §A2 동일 이슈)
  - [CalculateResidualUseCase.kt:19-26](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/caffeine/CalculateResidualUseCase.kt#L19) `userProfile?.sensitivity` → 항상 `null` → `else -> 5.0` 분기 (보통)
  - 사용자가 어느 민감도를 골라도 잔류량 계산 결과 **동일**
- **권고**: 단기 시연에는 큰 영향 없으나, 강사가 "민감도 선택 의미가 있나요?"라고 물으면 답변 곤란. 시나리오 시연 시 "민감도 데이터 저장 기능은 다음 스프린트" 명시

#### R1-19. Caffeine Search — `CaffeineSearchViewModel.init` 시 dummy 호출

- **화면**: [CaffeineSearchViewModel.kt:32-43](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchViewModel.kt#L32)
- **결과**:
  - `init { ensureDataLoaded() }` → `searchDrinkUseCase(query = "", page = 0, pageSize = 1)` 호출 (음료 검색 화면 들어갈 때마다 1건 페치 시도)
  - 의도된 캐싱 트리거지만, Firestore 차단/네트워크 단절 시 추가 latency. 진입 즉시 사용자가 검색해도 응답 timing 영향
- **권고**: Application onCreate의 `initializeIfEmpty` ([SnoffeeApplication.kt:20-22](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/SnoffeeApplication.kt#L20))로 충분. ViewModel init의 중복 호출 제거 가능

#### R1-20. SleepDialog `tempRecord!!` non-null assertion 사용

- **위치**: [SleepDialog.kt:310](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepDialog.kt#L310), [L322](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepDialog.kt#L322)
- **결과**:
  - `if (showConfirmDialog && tempRecord != null)` 조건 통과 후 사용이므로 NPE 위험은 낮음. 단 Compose recomposition 사이에 `tempRecord` 변경 가능성 있어 이론적으로 NPE 가능
  - 실제 발현 가능성: 매우 낮음. (var → smart cast 불가로 인한 코드 스타일 이슈)
- **권고**: `tempRecord?.let { record -> Text("만족도: ${record.deepSleepRatio}점"); ... }` 패턴으로 변경. 우선순위 낮음

#### R1-21. `Locale.setDefault(Locale.KOREAN)` 부작용 — DatePicker 호출 시 전역 Locale 변경

- **위치**: [PeriodReportView.kt:48](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/PeriodReportView.kt#L48)
- **재현 단계**: Report 기간 탭에서 날짜 선택 클릭 → DatePicker 표시
- **결과**:
  - `Locale.setDefault(Locale.KOREAN)`이 **JVM 전역 default Locale** 변경
  - 다른 화면(특히 `SimpleDateFormat`, 시스템 메시지)에 영향. 강사가 영어 OS 사용 시 갑자기 한국어 표시 등 부작용 가능
- **권고**: `Locale.setDefault` 제거. DatePicker에 Locale 별도 전달하는 방식으로 변경

---

## 시연 직전 빠른 패치 권고 (오늘~내일 30분 이내)

### 우선 1: 즉시 회피 (코드 변경 없음, 강사 행동만)

1. **Firebase Console 점검**: named DB `drink` + `drinks` 컬렉션 + 강사 테스트 직전 음료 1건 read 확인 (R1-6)
2. **시연 시나리오에서 제외**:
   - 설정 탭 클릭 시연 (R1-4)
   - Report 월간/추이 탭 시연 (R1-10, R1-11) — "일간/주간"만 시연
   - 카페인 탭 캘린더 날짜 변경 시연 (R1-16, R1-17)
   - 음료 검색 카드 우측 ⋮ → "수정" 클릭 (R1-5)
3. **사전 준비**:
   - 강사 테스트 단말에서 온보딩 시 **권한 모두 승인** (R1-1 회피)
   - 음료 1~3건 미리 입력 (Report 빈 화면 회피)

### 우선 2: 30분 이내 패치 (단순 코드 수정)

4. **R1-2 더미 데이터 토글**: [ReportViewModel.kt:84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84) 더미 블록 주석 처리 + [L216-224](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L216) 주석 해제. 단 빈 DB 시 ReportEmptyView 노출 — 음료 사전 입력 필수
5. **R1-1 권한 거부 우회**: [OnboardingPermissionScreen.kt:75-87](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L75) `if (grantedPermissions.containsAll(...))` 분기 제거 (승인 여부 무관하게 진행)
6. **R1-3 SleepViewModel 안전화**: [SleepViewModel.kt:55-67](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L55) `viewModelScope.launch` 본문을 `runCatching { ... }.onFailure { ... }`로 감싸기
7. **R1-9 직접 등록 dialog 필드 매핑 수정**: [CaffeineSearchScreen.kt:362-377](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L362) / [L454-468](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L454) — `record.copy(consumedAt = ...)`만 호출, 필드 덮어쓰기 제거

---

## 시연 가능 시나리오 (안전 경로)

다음 시나리오는 위 패치 4-7 적용 시 **안정적 시연 가능**:

1. **온보딩 통과** (모든 권한 승인) → Home 진입
2. **Home → "카페인 추가"** → CaffeineSearchScreen → 음료 검색 → 선택 → 시간 선택 → "기록하기" → Home 복귀
3. **Home에서 잔류 카페인 확인** (게이지, 시간, 농도 카드)
4. **Home → "전체보기"** → CaffeineMainScreen → 활동 로그 확인 → "삭제" 정상 동작
5. **Home → "카페인 추가"** → "직접 등록하기" 배너 → 음료명/카페인/용량 입력 → 시간 선택 → "기록하기"
6. **하단 탭 "수면"** → "수면 추가하기" → 다이얼로그에서 날짜/시간/만족도 → 저장 → 캘린더에 표시
7. **하단 탭 "리포트"** → "일간/주간" 탭 (월간/추이는 제외) → 실 데이터 차트 확인 (패치 4 적용 후)

---

## 점검 한계

- **실 단말 실행 미테스트** (정적 분석만). 단말별 행동 차이 (예: Health Connect 버전, OS API level) 미검증
- **ProGuard release 빌드 영향 미검증**: `isMinifyEnabled = false` 상태이므로 debug 빌드 한정 점검
- **Firebase Console 상태 미확인**: `drinks` 컬렉션 실 데이터 존재 여부, named DB `drink` 생성 여부, 보안 규칙 console 설정 모두 코드로는 검증 불가 (강사 직접 확인 필수)
- **Health Connect 실 환경**: SamsungHealthDataSourceImpl이 throw하는 정확한 Exception 종류와 message는 실 단말 실행 시 가변
- **테스트 코드 0줄**: [CalculateResidualUseCaseTest.kt](teams-docs/1team/repo/app/src/test/java/com/snoffee/app/usecase/CalculateResidualUseCaseTest.kt) 빈 파일 — 회귀 검증 자동화 0%
- **Wear 모듈**: 04 §C1-5/C1-15에서 빈 껍데기 확인 완료. 본 점검에서는 시연 제외 권고만 재확인
- **FCM**: [FcmService.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/notification/FcmService.kt) / [NotificationHelper.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/notification/NotificationHelper.kt) 빈 파일. AndroidManifest에 service 등록 0건. 시연 시 "푸시 알림 항목 제외" — 04 §C1-6 확인 완료
- **Gemini AI 호출 경로**: `GetCutoffTimeUseCase` / `GetWeeklyInsightUseCase`가 어떤 presentation 코드에서도 호출되지 않음 확인 (grep 0건). 따라서 04 §C1-2의 `TODO("Not yet implemented")` 크래시는 **현 UI 흐름에서 trigger되지 않음** — 04 보고서 "AI 컷오프 버튼 누르면 크래시" 시나리오는 **현 버전에서 dormant**
