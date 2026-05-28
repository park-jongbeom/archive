# 1팀 Snoffee 화면 동작 오류 점검 (2026-05-27)

> 베이스: develop @ 6932a15, 5/22 이후 +1259/-329 (26 commits)
> 관점: 강사진 실 단말 테스트 시 만나는 크래시/무반응/오작동 (보안/구조 제외)
> 5/22 알려진 이슈는 제외 — 신규/해소/잔존만 기록
> 점검 방식: 정적 코드 분석 (실 단말 빌드/실행 미수행)

---

## 한눈에 — Top 신규/잔존 시연 리스크

| # | 시나리오 | 결과 | 위치 |
|---|---------|------|------|
| 1 | Report 탭 진입 (모든 서브탭) | **여전히 가짜 데이터** (스타벅스 150mg 등) — 5/22 이후 미해소 | [ReportViewModel.kt:84-214](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84) |
| 2 | Health Connect 권한 ON + Sleep 탭 진입 | **Health Connect에서 fetch한 모든 데이터가 표시 안 됨** (deepSleepRatio=0 필터) | [SleepViewModel.kt:82](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L82) ↔ [SamsungHealthDataSourceImpl.kt:71](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L71) |
| 3 | 설정 → 수면시간 06:00 또는 기상시간 18:00 저장 후 재오픈 | **다이얼로그 초기값이 12시간 어긋남** | [SettingScreen.kt:352-396](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L352) |
| 4 | 수면 탭 → 기록 삭제 | **DB에 row 남음** (deepSleepRatio=0 tombstone) | [DeleteSleepDataUseCase.kt:11](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/sleep/DeleteSleepDataUseCase.kt#L11) |
| 5 | 카페인 음료 직접 등록에서 음료명 빈값 + 시간만 입력 | **빈 이름으로 저장됨** (5/22 R1-8 잔존) | [CaffeinInputDialog.kt:343-374](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/dialog/CaffeinInputDialog.kt#L343) |

---

## 🔴 즉시 크래시 위험 (신규/잔존)

### [N1] (없음 — 신규 크래시 경로 발견되지 않음)

5/22 보고서 R1-3 (Sleep 탭 SecurityException) 은 **해소됨** (✅ 아래 참조).
5/22 R1-1 (권한 dead-end) 도 **해소됨**.
5/22에서 식별된 다른 dormant 크래시 경로(AI Gemini TODO throw 등)는 여전히 호출 경로 없음.

---

## 🟠 무반응 / dead-end / 잘못된 데이터 (신규)

### [O1] Health Connect 권한 ON이어도 Sleep 화면에 데이터 0건 (sentinel 신규)

- **시나리오**: 사용자가 온보딩에서 Health Connect 권한을 정상 승인하고 Sleep 탭에 진입
- **현상**: 캘린더에 점/색 표시 없음, "기록된 시간 --", "월 평균 0h 00m" — Health Connect에 수면 데이터가 있어도 화면에는 전혀 안 보임. "수면 추가하기"로 수동 입력해야만 표시됨
- **근거 — 2개 코드의 모순**:
  - [SamsungHealthDataSourceImpl.kt:66-73](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L66): Health Connect에서 fetch한 모든 SleepSessionRecord에 `deepSleepRatio = 0` 하드코딩
    ```kotlin
    return response.records.map { record ->
        SleepDataDto(
            date = LocalDateTime.ofInstant(record.startTime, ZoneId.systemDefault()),
            sleepStart = record.startTime.toEpochMilli(),
            sleepEnd = record.endTime.toEpochMilli(),
            deepSleepRatio = 0   // ← 항상 0
        )
    }
    ```
  - [SleepViewModel.kt:80-103](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L80): 가공 단계에서 `deepSleepRatio > 0`인 데이터만 캘린더/평균에 포함
    ```kotlin
    sleepList.forEach { sleepData ->
        if (sleepData.deepSleepRatio > 0) {   // ← Health Connect 데이터는 모두 탈락
            ...
            scoresMap[localDate] = finalScore
            ...
        }
    }
    ```
- **부수 영향**: 매 화면 진입마다 Health Connect → Room에 동일 데이터가 새 id로 REPLACE 삽입됨 ([SleepMapper.kt:44-52](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/mapper/SleepMapper.kt#L44)에서 id 누락 → autoGenerate). 화면에는 안 보여도 DB는 매번 누적 → 시연 중 점진적 슬로우다운 가능
- **권고**:
  - 단기 시연: "Health Connect 자동 연동은 다음 스프린트, 현재는 수동 입력 위주" 명시. 수동 기록 1~2건 사전 입력
  - 본질: SamsungHealthDataSourceImpl에서 점수 계산 또는 임시 기본값(예: 70) 부여, 그리고 SleepViewModel의 `> 0` 필터 재검토

### [O2] 설정 — 수면시간 06:00 또는 기상시간 18:00 입력 시 TimePicker 12h 어긋남

- **시나리오**: 설정 탭 → "목표 수면 시간" 클릭 → TimePicker로 "AM 06:00" 선택 → 변경 → 카드에 "06:00" 정상 표시 → 다시 카드 클릭하여 TimePicker 재오픈
- **현상**: TimePicker 초기값이 "PM 18:00"으로 뜸. 기상 시간도 마찬가지로 "PM 18:00" 저장 후 재오픈 시 "AM 06:00"으로 뜸
- **위치**: [SettingScreen.kt:352-359](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L352) (수면), [L394-401](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L394) (기상)
- **근거**:
  ```kotlin
  // 수면 TimePicker
  initialHour = if (rawSleep.length >= 4) {
      val parsedHour = rawSleep.substring(0, 2).toIntOrNull() ?: 22
      if (parsedHour < 12) parsedHour + 12 else parsedHour   // ← 12 미만은 무조건 PM 변환
  } else 22,
  ```
  저장된 값 자체는 06:00 그대로지만, 재편집 시 dialog가 12시간 더해서 18:00으로 표시. 사용자가 그대로 확인 누르면 18:00이 다시 저장됨 → 값 손상
- **권고**: AM/PM 강제 보정 제거. `parsedHour`를 그대로 사용. is24Hour 인자도 true 검토

### [O3] 카페인 직접 등록 — 음료명 비워도 저장 가능 (5/22 R1-8 잔존)

- **시나리오**: 카페인 추가 → "직접 등록하기" → 음료명 비움, 카페인 50, 용량 100 입력 → "기록하기"
- **현상**: 빈 이름으로 저장되고 Home/Caffeine 활동 로그에 이름이 빈 카드로 등장
- **위치**: [CaffeinInputDialog.kt:343-374](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/dialog/CaffeinInputDialog.kt#L343)
- **근거**:
  ```kotlin
  Button(
      onClick = { ... onConfirm(finalRecord); onDismiss() },
      enabled = !isTimeError,    // ← drinkName.isNotBlank() 검증 없음
      ...
  )
  ```
- **권고**: `enabled = !isTimeError && drinkName.isNotBlank() && caffeineInputString.isNotBlank()`

### [O4] 카페인 메인 — 캘린더 점이 UTC 기준 → 자정 근처 off-by-one

- **시나리오**: 23:30~24:00 사이 KST에서 카페인 기록 → 카페인 탭 캘린더 확인
- **현상**: 점이 다음 날 (UTC 기준) 칸에 찍힘
- **위치**: [CaffeinMainViewModel.kt:47](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L47)
- **근거**:
  ```kotlin
  recordedDates = records
      .map { LocalDate.ofEpochDay(it.consumedAt / 86400000) }   // ← UTC epoch day, KST 미반영
      .toSet()
  ```
  `Instant.ofEpochMilli(it.consumedAt).atZone(ZoneId.systemDefault()).toLocalDate()` 사용해야 함
- **권고**: 화면 다른 곳([CaffeinMainScreen.kt:668-670](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainScreen.kt#L668))에서는 `atZone(ZoneId.systemDefault())` 패턴 사용 중. 동일 패턴으로 통일

### [O5] 수면 "기록 삭제" — DB row가 실제로 삭제되지 않고 점수 0 tombstone 남음

- **시나리오**: 수면 탭 → 캘린더에서 기록 있는 날짜 선택 → "기록 삭제" → "삭제" 확인
- **현상**: 화면에서는 사라짐 (정상 동작처럼 보임). 그러나 DB row는 남고, 같은 날짜에 새 기록 추가 시 동작 비결정적 (autoGenerate id로 새 row가 만들어지므로 동일 날짜에 2개 row — 하나는 0점 tombstone, 하나는 새 점수)
- **위치**: [DeleteSleepDataUseCase.kt:10-12](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/sleep/DeleteSleepDataUseCase.kt#L10) + [SleepDao.kt](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/local/dao/SleepDao.kt) (delete 메서드 없음)
- **근거**:
  ```kotlin
  suspend operator fun invoke(sleepData: SleepData): Result<Unit> = runCatching {
      repository.saveSleepData(sleepData.copy(deepSleepRatio = 0))   // ← 진짜 delete 아님
  }
  ```
  SleepDao에 `@Delete` 메서드 자체가 없음. SleepViewModel의 `> 0` 필터로 가려서 화면에는 안 보임
- **권고**: 단기 시연: 동일 날짜에 새 기록 추가 시연 회피. 본질: SleepDao에 `@Delete` 추가 또는 `@Query("DELETE FROM sleep_data WHERE id = :id")` 추가하고 UseCase 교체

### [O6] 카페인 검색 — Firestore 실패 시 무한 빈 결과 (5/22 R1-6 잔존)

- **시나리오**: 카페인 추가 → 검색창에 "아메리카노" 입력 → 사전조건: Firestore named DB `drink` 미생성 또는 보안 규칙 차단
- **현상**: "검색 결과 0개" 영원히 표시, 재시도 UI 없음
- **위치**: [DrinkRemoteDataSourceImpl.kt](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt) (5/22 이후 미변경) + [CaffeineSearchViewModel.kt:128-136](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchViewModel.kt#L128)
- **근거**: catch 블록은 `error = e.message` UiState에 저장하나, [CaffeineSearchScreen.kt](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt)에서 `uiState.error`를 화면에 표시하지 않음 → 사용자에게 오류 미고지
- **권고**: 5/22 권고와 동일 (Firebase Console 사전 확인). 추가로 `uiState.error` Snackbar 표시 또는 빈 결과 화면에 "재시도" 버튼

---

## 🟡 잘못된 데이터 표시 / UX 결함 (잔존)

### [Y1] Report 모든 탭이 여전히 더미 데이터 (5/22 R1-2 미해소)

- **시나리오**: 리포트 탭 진입 → 5개 서브탭 어디든
- **현상**: 5/22와 완전히 동일 — 스타벅스 아메리카노 150mg, 돌체라떼 230mg, 빽다방 콜드브루 200mg, 핫식스 100mg, 7일 더미 차트 표시. 사용자가 카페인을 새로 추가해도 리포트 숫자는 불변
- **위치**: [ReportViewModel.kt:84-214](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84) 더미 블록 그대로, [L216-224](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L216) 실 UseCase 호출 4줄 모두 주석 처리 그대로
- **근거**:
  ```kotlin
  // ─── [테스트용 임시 더미 데이터 세팅 시작] ───
  val dummyCaffeineRecords = listOf(
      CaffeineRecord(id = 1, drinkName = "아메리카노", brandName = "스타벅스", intakeCaffeine = 150.0, ...),
      ...
  )
  ...
  //  val dailyDeferred = async { getReportUseCase(ReportPeriod.DAILY, nowMillis) }
  //  val dailyResult = dailyDeferred.await()   ← 여전히 주석
  ```
- **권고**: 5/22 권고 동일 — 더미 블록 [L84-214] 삭제, [L216-224] 주석 해제. 단 사전에 카페인/수면 기록 1~3건 입력 필수

### [Y2] Report 월간/추이 탭 — `highCaffeineDaySleepTime`/`monthlyCaffeineTrend` 여전히 미계산 (5/22 R1-10/R1-11 잔존)

- **시나리오**: 리포트 → "월간" 또는 "추이" 탭
- **위치**: [ReportUiState.kt:26-31](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportUiState.kt#L26)
- **근거**: ReportViewModel `loadReportData()`가 `highCaffeineDaySleepTime`/`lowCaffeineDaySleepTime`/`monthlyCaffeineTrend` 필드를 update하지 않음 → 기본값 "0h 00m" / emptyMap 그대로
- **현상**: 월간 탭의 "카페인 과다일 평균 수면 0h 00m" 비교바 미표시, 추이 탭 "최고치: 0mg (-)" 차트 빈 가로선
- **권고**: 5/22와 동일 — 시연 시 두 탭 회피, 일간/주간만 시연

### [Y3] CaffeineSearch — `EmptySearchState`의 "+추가" 버튼이 onConfirmSuccess 호출하여 화면이 닫힘

- **시나리오**: 검색창 비운 상태에서 EmptySearchState의 + 아이콘 → 직접 입력 다이얼로그 → 음료명/카페인 입력 → "기록하기"
- **현상**: 정상 저장되나, 직후 `onConfirmSuccess()` 호출 ([CaffeineSearchScreen.kt:377](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L377)) → 화면이 자동으로 뒤로 가서 Home으로 이동. 동시에 viewModel의 `isSaved` 변경 → LaunchedEffect도 onConfirmSuccess 시도. 두 번 popBackStack — 두 번째는 no-op이라 안전하지만 의도와 다른 동작 (popBack 1회면 충분)
- **위치**: [CaffeineSearchScreen.kt:362-377](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L362), [L88-90](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L88)
- **권고**: dialog confirm 측에서 `onConfirmSuccess()` 호출 제거, LaunchedEffect만 단일 진입점으로 사용

### [Y4] CaffeineSearchScreen — 검색어 입력했지만 결과 0건일 때 헤더 "검색 결과 0개" 표시 (5/22 R1-7 잔존)

- 5/22 권고 그대로. 큰 영향 없음

### [Y5] Setting — userProfile 로드 전 짧은 시간 동안 하드코딩 기본값 노출

- **시나리오**: Setting 탭 첫 진입
- **현상**: 약 100~500ms 동안 "-cm", "-kg", "22:30", "06:30" (display 기본값) 표시 후 실제 데이터로 교체
- **위치**: [SettingScreen.kt:87-94](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L87)
- **권고**: 낮은 우선순위. CircularProgressIndicator로 가리거나 `userProfile == null`일 때 별도 placeholder

### [Y6] Setting — 다크 모드 스위치는 토글만 가능, 앱 테마 반영 0건

- **위치**: [SettingScreen.kt:77, 197-202](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L77)
- **근거**: `var isDarkMode by remember { mutableStateOf(false) }` — 로컬 상태만, ViewModel/Theme에 연결 안 됨
- **권고**: 시연에서 다크 모드 스위치 시연 자제. 또는 enabled=false + "준비 중" 라벨

### [Y7] Setting — wakeTime/userSleepTime은 저장되나 다른 화면에서 소비 0건

- **시나리오**: 설정에서 기상 시간 07:00으로 변경 → 다른 화면 (Home, Sleep, Report) 확인
- **현상**: 변경이 다른 화면에 어떤 영향도 주지 않음
- **위치**: `wakeTime` grep 결과 — Setting 화면 내부 표시 + DB 저장 외 사용처 없음
- **권고**: 시연에서 "설정 변경 시 반영되는 화면" 강조 회피. 또는 "현재는 저장만 되고 실제 알림/계산은 다음 스프린트" 안내

---

## 5/22 이후 해소 확인된 이슈 ✅

### ✅ R1-1. 온보딩 권한 거부 dead-end 해소 (PR #81)

- **변경**: [OnboardingPermissionScreen.kt:90-105](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L90)
  - 거부 분기에서 `showPermissionDeniedMessage = true` 상태 표시
  - 버튼 카피가 "Health Connect 설정 열기 →"로 바뀜
  - 클릭 시 `HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS` Intent 실행
  - `DisposableEffect` + `Lifecycle.Event.ON_RESUME` observer로 사용자가 외부 설정에서 권한 허용 후 복귀 시 자동 진행 ([L106-139](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L106))
  - SDK 미설치 시 (`SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED`) market 열기 분기 추가 ([L238-245](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L238))
- **잔존 이슈 없음**. 권한 거부 후에도 진행 가능

### ✅ R1-3. SleepScreen 진입 시 Health Connect SecurityException 해소

- **변경 1**: [SleepRepositoryImpl.kt:48-53](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/SleepRepositoryImpl.kt#L48) — Health Connect 호출을 `runCatching {}.getOrDefault(emptyList())`로 감쌈
- **변경 2**: [SamsungHealthDataSourceImpl.kt:52-54](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L52) — `getSleepDataByDateRange` 진입 시 `!hasPermissions()` 가드로 즉시 emptyList 반환
- **변경 3**: [SleepScreen.kt:131-154](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepScreen.kt#L131) — `uiState.hasHealthPermission` 분기로 `SleepPermissionEmptyView` (권한 안내 화면) 노출. "권한 설정하러 가기" 버튼이 Health Connect 설정 화면으로 이동
- **잔존 이슈**: O1 (Health Connect 데이터가 fetch되어도 화면 표시 안 됨)

### ✅ R1-4. SettingScreen 빈 ViewModel + 하드코딩 — 거의 해소

- **변경 1**: [SettingViewModel.kt](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingViewModel.kt) (5/22엔 2줄) — 신설. `UserProfileRepository` 주입 + `loadUserProfile`/`updateHeight`/`updateWeight`/`updateSensitivity`/`updateSleepTime`/`updateWakeTime` 구현
- **변경 2**: [SettingScreen.kt:79-105](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L79) — `userProfile`을 collectAsState로 구독, 신장/체중/민감도/수면시간/기상시간 모두 동적 표시
- **변경 3**: [SettingScreen.kt:221-432](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingScreen.kt#L221) — 신장/체중/민감도/수면시간/기상시간 각각 다이얼로그 + TimePicker로 변경 가능
- **잔존 이슈**: O2 (시간 picker 12h 어긋남), Y5 (로딩 전 깜빡임), Y6 (다크모드 스위치 미연결), Y7 (저장은 되나 다른 화면 미연동)

### ✅ R1-5. CaffeineMainScreen "수정" 메뉴 dead-end 해소

- **변경 1**: [CaffeinMainScreen.kt:258-261](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainScreen.kt#L258) — `onEditClick = { record -> editingRecord = record; showEditDialog = true }`
- **변경 2**: [CaffeinMainScreen.kt:285-318](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainScreen.kt#L285) — 수정 다이얼로그로 CaffeineInputDialog 재사용, editingRecord 전달
- **변경 3**: [EditCaffeineUseCase.kt](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/caffeine/EditCaffeineUseCase.kt) 신설
- **잔존 이슈 없음**

### ✅ R1-9. 직접 등록 시 intakeSize/intakeCaffeine 필드 swap 해소

- **변경**: [CaffeineSearchScreen.kt:362-377](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L362), [L454-470](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L454) — `intakeSize = record.intakeSize`, `intakeCaffeine = record.intakeCaffeine` 으로 정상화
- **잔존 이슈**: O3 (음료명 빈값 가드 없음)

### ✅ R1-15/R1-16/R1-17. CaffeineMain 캘린더 누적/날짜선택/월이동 모두 해소

- **변경**: [CaffeinMainViewModel.kt:41-98](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/main/CaffeinMainViewModel.kt#L41)
  - `recordedDates` Flow 기반 + `.toSet()` 교체 — 삭제 시 점 즉시 사라짐 ✅
  - `onDateSelected`에서 `observeRecordsByDate(date)` 호출 — 선택 날짜의 기록 표시 ✅
  - `onPrevMonth`/`onNextMonth`에서 `observeMonthRecords(newMonth)` 호출 — 월 점 갱신 ✅
- **잔존 이슈**: O4 (epoch day UTC 변환 off-by-one)

### ✅ R1-18. 온보딩 사용자 정보(height/weight/sensitivity) DB 저장 해소

- **변경**: [OnboardingViewModel.kt:92-117](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/OnboardingViewModel.kt#L92)
  - `completeOnboarding()`이 height/weight/sensitivity로 `UserProfile` 생성하여 `userProfileRepository.saveUserProfile()` 호출
  - `dailyCaffeineLimit = 400.0`, `userSleepTime = 2230L`, `wakeTime = 630L` 기본값으로 저장
- **검증**: [CalculateResidualUseCase.kt:25](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/caffeine/CalculateResidualUseCase.kt#L25)에서 `userProfile?.sensitivity?.halfLifeHours` 소비함 → 민감도가 실제로 잔류량 계산에 반영됨 ✅
- **잔존 이슈 없음** (단, Setting 화면에서 변경한 값이 Home에 반영되는지는 별도 확인 필요 — Y7 참조)

### ✅ R1-12. HomeScreen 백그라운드 폴링 → ON_RESUME 패턴으로 부분 개선

- **변경**: [HomeScreen.kt:49-51](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeScreen.kt#L49) — `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)`로 화면 복귀 시마다 호출
- **잔존**: [HomeViewModel.kt:32-42](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/home/HomeViewModel.kt#L32) — `init { startResidualRefresh() }`로 viewModelScope에서 5분 폴링은 그대로. ViewModel이 살아있는 동안 (앱 백그라운드 포함) 폴링 계속됨. 시연에는 영향 없음

---

## 점검 한계

- **실 단말 실행 미테스트** (정적 분석만). Health Connect 실 동작, 캘린더 OS-locale 표시 등 미검증
- **Firebase Console 상태 미확인**: `drink` named DB, `drinks` 컬렉션 존재 여부, 보안 규칙 모두 코드로는 검증 불가 (강사 직접 확인 필수). O6는 5/22 사전 점검 미수행 시 100% 발현
- **git 명령 권한 차단**: 5/22 이후 26개 커밋의 정확한 commit-by-commit diff 미확인. 사용자 지시된 "신규 변경 파일 목록"과 코드 현 상태 대조로 분석
- **테스트 코드 0줄 변동 없음**: 회귀 자동화 검증 불가
- **Wear OS / FCM**: 5/22 이후 변경 신호 없음으로 추정 (사용자 안내에도 미언급). 시연 제외 권고 유지
- **AI Gemini 경로**: 5/22 시점 `GetCutoffTimeUseCase`/`GetWeeklyInsightUseCase`가 presentation에서 호출되지 않음 확인. 5/22 이후 신규 호출 경로 발견되지 않음 (HomeViewModel/SettingViewModel/SleepViewModel/ReportViewModel grep 결과). 여전히 dormant
