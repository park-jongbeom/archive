# 1팀 Snoffee — 크리티컬 점검 결과 (2026-05-22)

> 점검 범위: `teams-docs/1team/repo/` 모바일(app/) + 워치(wear/) + Gradle/Manifest/리소스
> 기준 체크리스트: [03_checklist.md](03_checklist.md) C1-1 ~ C1-15
> 점검 어조: 객관적·실용적 (보조강사 자료로 활용, 학생 비난 어조 금지)

---

## 요약 (Top 3 critical)

1. **워치(wear) 모듈이 사실상 빈 껍데기** — `wear/src/main/java/.../*.kt` 7개 파일이 모두 0줄, 워치 AndroidManifest에 Activity/Service 0건, `applicationId = "com.example.wear"` (모바일과 패키지명·서명 모두 불일치). **현재 상태로 워치는 빌드되어도 실행 화면이 없으며, Wear ↔ Mobile Data Layer 페어링이 동작하지 않음**. → [wear/build.gradle.kts:14](teams-docs/1team/repo/wear/build.gradle.kts#L14), [wear/src/main/AndroidManifest.xml](teams-docs/1team/repo/wear/src/main/AndroidManifest.xml)
2. **Gemini AI 기능 전체가 TODO 스텁** — `GeminiRemoteDataSource.kt`는 인터페이스 본문 없음, `GeminiRepositoryImpl.getCutoffTime()` / `getWeeklyInsight()`는 `TODO("Not yet implemented")`. README가 광고하는 "AI 개인화 컷오프"·"주간 자연어 리포트"는 호출 즉시 NotImplementedError 크래시 발생 위험. → [GeminiRepositoryImpl.kt:19](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/GeminiRepositoryImpl.kt#L19)
3. **ReportViewModel이 하드코딩된 더미 데이터로 화면을 그림** — 실 데이터 호출(`getReportUseCase`)은 주석 처리되고 `dummyCaffeineRecords`·`dummySleepData`로 UI 채움. 강사 시연 시 항상 같은 가짜 숫자가 보임. → [ReportViewModel.kt:86-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L86)

---

## 점검 결과 (체크리스트 순서)

### 🔴 CRITICAL

#### C1-1 Gemini API 키 노출

- **상태**: 🟢 안전 (현 시점 노출 없음, 단 미구현 상태이므로 향후 추가 시 재확인 필수)
- **근거**:
  - 전 repo 대상 grep: `GEMINI_API_KEY` / `geminiApiKey` / `BuildConfig.` 모두 0건
  - [local.properties](teams-docs/1team/repo/local.properties) 내용: `sdk.dir`만 존재 (Gemini 키 없음)
  - [app/build.gradle.kts](teams-docs/1team/repo/app/build.gradle.kts) `defaultConfig`에 `buildConfigField` / `manifestPlaceholders` 0건
  - [GeminiRemoteDataSourceImpl.kt:10](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/GeminiRemoteDataSourceImpl.kt#L10): `// TODO: Retrofit 주입 필요` — 실제 호출 코드 없음
- **권고**:
  - 키 도입 전, **Firebase AI Logic SDK** 채택 검토 (클라이언트 SDK가 App Check 게이트 통과 후 서버 프록시 경유). 직접 endpoint 호출 + `BuildConfig.GEMINI_API_KEY` 패턴은 APK 디컴파일 시 키 노출.
  - 향후 추가 시 `local.properties` → `gradle.kts`의 `buildConfigField`로 주입하면 release APK에 포함되므로, **Cloud Functions proxy + App Check 필수**.

#### C1-2 Gemini 호출 채널 (proxy vs 직접 호출)

- **상태**: ⚪ 해당 없음 (실 구현 부재)
- **근거**:
  - [GeminiRemoteDataSource.kt:5-8](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/GeminiRemoteDataSource.kt#L5):
    ```kotlin
    interface GeminiRemoteDataSource
    ```
    인터페이스에 메서드 0개.
  - [GeminiRepositoryImpl.kt:17-25](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/GeminiRepositoryImpl.kt#L17):
    ```kotlin
    override suspend fun getCutoffTime(): CaffeineAnalysis {
        TODO("Not yet implemented")
    }
    override suspend fun getWeeklyInsight(): String {
        TODO("Not yet implemented")
    }
    ```
  - [GetCutoffTimeUseCase.kt:12](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/gemini/GetCutoffTimeUseCase.kt#L12) / [GetWeeklyInsightUseCase.kt:12](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/gemini/GetWeeklyInsightUseCase.kt#L12) — Repository를 그대로 호출하므로 호출 시 즉시 `NotImplementedError`.
  - [NetworkModule.kt:25-30](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/di/NetworkModule.kt#L25): Retrofit baseUrl이 `https://jsonplaceholder.typicode.com/` — Gemini 엔드포인트 아님 (테스트용 placeholder).
- **권고**:
  - 강사 시연에서 "AI 컷오프 시간 추천" 버튼 누르면 크래시 발생. 시연 전 ViewModel에서 해당 UseCase 호출부를 try/catch로 감싸거나, UI에서 "준비 중" 안내 노출.
  - 실 구현 시 Firebase AI Logic 사용 권장. 직접 Retrofit으로 https://generativelanguage.googleapis.com/ 호출 시 키 보호 불가.

#### C1-3 AndroidManifest health permission 선언

- **상태**: 🟢 안전 (READ_SLEEP 선언됨, `<queries>` HealthData 패키지 선언됨)
- **근거**:
  - [app/src/main/AndroidManifest.xml:4](teams-docs/1team/repo/app/src/main/AndroidManifest.xml#L4): `<uses-permission android:name="android.permission.health.READ_SLEEP" />`
  - [AndroidManifest.xml:8-10](teams-docs/1team/repo/app/src/main/AndroidManifest.xml#L8): `<queries><package android:name="com.google.android.apps.healthdata"/></queries>` — Android 11+ 패키지 가시성 OK
  - [AndroidManifest.xml:33-42](teams-docs/1team/repo/app/src/main/AndroidManifest.xml#L33): `ViewPermissionUsageActivity` activity-alias로 `HEALTH_PERMISSIONS` category 처리 — Health Connect 권한 정책 UI 노출 가능
- **권고**:
  - `WRITE_SLEEP`은 선언되지 않았는데 `SamsungHealthDataSourceImpl.saveSleepData()`는 TODO만 있고 실제 쓰기 없음 → 일치. 추후 쓰기 구현 시 권한 추가 필요.
  - `ACTIVITY_RECOGNITION`이 선언되어 있는데 ([AndroidManifest.xml:6](teams-docs/1team/repo/app/src/main/AndroidManifest.xml#L6)) 코드에서 사용처를 찾지 못함. 미사용이면 Play Store 정책 (불필요한 권한) 위반 가능 — 사용 계획 없으면 제거 권고.

#### C1-4 Samsung Health 권한 revoke 후 onResume 재확인

- **상태**: 🟠 부분 발견 (onboarding 1회 요청만, 권한 revoke 시 fallback 없음)
- **근거**:
  - [OnboardingPermissionScreen.kt:159-180](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L159): 권한 1회 요청 후 onNextClick. **승인되지 않으면 다음 화면으로 넘어가지 못함** (else 분기에서 `onNextClick()` 호출하지 않음, [L75-87](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/permission/OnboardingPermissionScreen.kt#L75) — `if (grantedPermissions.containsAll(...))` 조건 미충족 시 무반응).
  - [SamsungHealthDataSourceImpl.kt:75-80](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L75): `hasPermissions()` 메서드 존재하나, `SleepRepositoryImpl.getSleepDataByDateRange()`에서 호출하지 않고 바로 `healthConnectClient.readRecords(...)` 시도 → 권한 미보유 시 `SecurityException` 가능.
  - `SleepViewModel.refreshSleepData()` ([SleepViewModel.kt:55-67](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L55))이 init에서 호출되는데 try/catch 없음 → 권한 revoke 시 ViewModel scope에서 예외 전파.
- **권고**:
  - `SleepRepositoryImpl.getSleepDataByDateRange()`에 진입 시 `healthDataSource.hasPermissions()` 가드 추가, 미보유면 빈 리스트 또는 도메인 에러 반환.
  - 온보딩 권한 거부 분기에 안내 UI + "권한 없이 계속" 우회 경로 권고. 현 흐름은 거부 시 dead-end.

#### C1-5 Wear ↔ Mobile applicationId / signing 일치

- **상태**: 🔴 발견 (사실상 페어링 불가)
- **근거**:
  - 모바일: [app/build.gradle.kts:10](teams-docs/1team/repo/app/build.gradle.kts#L10) `namespace = "com.snoffee.app"`, [L14](teams-docs/1team/repo/app/build.gradle.kts#L14) `applicationId = "com.snoffee.app"`
  - 워치: [wear/build.gradle.kts:6](teams-docs/1team/repo/wear/build.gradle.kts#L6) `namespace = "com.example.wear"`, [L14](teams-docs/1team/repo/wear/build.gradle.kts#L14) `applicationId = "com.example.wear"`
  - signing config: 양쪽 모두 `signingConfig` 미지정 → 둘 다 debug keystore 사용하여 우연히 일치할 수 있으나, 명시적 동일 보장이 없음.
  - Wear OS 가이드: handheld와 wearable이 **동일 package name + 동일 signing certificate** 일치해야 Data Layer (MessageClient/DataClient) 통신 가능.
- **권고**:
  - `wear/build.gradle.kts`의 `namespace` / `applicationId`를 `com.snoffee.wear` (또는 `com.snoffee.app`)로 통일.
  - 추후 release 빌드 시 `signingConfig` 공통화 명시 (둘이 다른 키로 서명되면 페어링 즉시 단절).
  - 워치 모듈에 `play-services-wearable` 의존성이 추가되어 있으나 ([wear/build.gradle.kts:38](teams-docs/1team/repo/wear/build.gradle.kts#L38)) 실제 사용 코드는 0건 (wear 소스 전부 빈 파일).

#### C1-6 FCM `onNewToken` 토큰 서버 업로드

- **상태**: 🔴 발견 (FCM 인프라 전무)
- **근거**:
  - [FcmService.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/notification/FcmService.kt): **0줄 (빈 파일)** — package 선언조차 없음
  - [NotificationHelper.kt](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/notification/NotificationHelper.kt): **0줄 (빈 파일)**
  - AndroidManifest에 `<service>` 태그 0건 (FirebaseMessagingService 미등록)
  - grep `FirebaseMessagingService` / `onNewToken` / `onMessageReceived` 전체 repo 0건
  - 빌드 dependency에 `firebase-messaging` 없음 ([libs.versions.toml](teams-docs/1team/repo/gradle/libs.versions.toml) 47-54: bom/firestore/database/analytics만)
- **권고**:
  - README ([README.md:85](teams-docs/1team/repo/README.md#L85))가 FCM 광고 중 — 실제 미구현 상태. 시연 시 "푸시 알림" 항목 제외 권고.
  - 카페인 컷오프 진동 알림이 README ([L37-38](teams-docs/1team/repo/README.md#L37))의 핵심 가치 중 하나이므로, 추후 구현 시 `FirebaseMessagingService` 등록 + `onNewToken` Firestore upload + `POST_NOTIFICATIONS` 권한 (이미 선언됨) + `NotificationChannel` 생성 일괄 작업.

---

### 🟠 HIGH

#### C1-7 CaffeineCalculator 반감기 공식 정확성

- **상태**: 🟢 안전 (공식 표준 일치)
- **근거**:
  - [CaffeineCalculator.kt:22](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/util/CaffeineCalculator.kt#L22):
    ```kotlin
    return intakeCaffeine * (0.5).pow(elapsedHours / halfLifeHours)
    ```
    → 표준 `C(t) = C₀ × 0.5^(t/halfLife)` 일치.
  - [CaffeineCalculator.kt:17-19](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/util/CaffeineCalculator.kt#L17): `require(intakeCaffeine >= 0)` / `halfLifeHours > 0` / 미래 시간 가드 모두 존재.
  - [CaffeineCalculator.kt:36](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/util/CaffeineCalculator.kt#L36): 0mg 도달 시간 역산 `halfLifeHours * log2(C / 0.1mg)` — 수학적으로 정확.
- **권고**:
  - 임계값 `targetMinCaffeine = 0.1mg`이 가이드(50mg EEG 수면 영향)와 다른 보수적 값. 사용자 표시 컷오프 시간을 "0mg 도달"이 아니라 "50mg 미만 도달"로 바꾸면 의학적 의미가 더 정확함. 결정 필요.

#### C1-8 다중 음료 섭취 시 잔류량 합산

- **상태**: 🟢 안전 (개별 잔류 합 처리)
- **근거**:
  - [CalculateResidualUseCase.kt:29-39](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/caffeine/CalculateResidualUseCase.kt#L29):
    ```kotlin
    val totalResidual = records.sumOf { record ->
        ...
        calculator.calculateResidualCaffeine(
            intakeCaffeine = record.intakeCaffeine,
            consumedAt = safeConsumedAt,
            currentTimeMillis = now,
            halfLifeHours = halfLifeHours
        )
    }
    ```
    각 record별로 잔류량 계산 후 합산 — 약물동력학적으로 올바른 처리.
  - [CalculateResidualUseCase.kt:30](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/caffeine/CalculateResidualUseCase.kt#L30): `if (record.consumedAt > now) now else record.consumedAt` — 미래 시간 안전 가드 존재 (단, 본질적으로 데이터 무결성 문제, 입력 측에서 막는 것이 우선).

#### C1-9 시간 계산 Instant/ZonedDateTime 일관성

- **상태**: 🟡 주의 (혼용 패턴, 큰 버그는 없으나 일관성 부족)
- **근거**:
  - `CaffeineCalculator` / `HomeViewModel`: `System.currentTimeMillis()` epoch 사용
  - `SleepViewModel.refreshSleepData()` [SleepViewModel.kt:57-64](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L57): `ZoneId.systemDefault()` + `atZone` + `toEpochMilli` 일관 적용 OK
  - `CaffeineRepositoryImpl.getStartOfDay()` [CaffeineRepositoryImpl.kt:56-63](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/CaffeineRepositoryImpl.kt#L56): `Calendar.getInstance()` 기본 시간대 — `SleepViewModel`과 다른 API
  - `GetReportUseCase` [GetReportUseCase.kt:94-107](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/report/GetReportUseCase.kt#L94): `Calendar.DAY_OF_WEEK` 사용. `firstDayOfWeek = Calendar.MONDAY` 명시는 OK이나, 한국 외 Locale 기기에선 default가 SUNDAY로 시작하여 차트 라벨 순서 헷갈림 우려.
- **권고**:
  - `java.time.LocalDate.now(ZoneId.systemDefault())` 기반 유틸 함수 1개로 통일 권고. Calendar API와 Instant 혼용은 장기 유지보수에서 timezone 버그 유발.

#### C1-10 Room DB Migration 정책

- **상태**: 🔴 발견 (`fallbackToDestructiveMigration` 사용, production 시 사용자 데이터 삭제 위험)
- **근거**:
  - [DatabaseModule.kt:29](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/di/DatabaseModule.kt#L29):
    ```kotlin
    .fallbackToDestructiveMigration(dropAllTables = true)
    ```
  - [SnoffeeDatabase.kt:17](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/local/SnoffeeDatabase.kt#L17): `version = 2` 이미 1회 마이그레이션 발생 (개발 중 destructive로 처리한 흔적).
  - Production 배포 후 entity 변경 → 사용자 카페인/수면 기록 전부 삭제.
- **권고**:
  - 강사 평가 단계까지는 허용 가능하나, **출시 직전 `Migration` 클래스로 교체 필수**. 워터폴 명세 단계에서 schema freeze 결정 권고.
  - 학생들에게 "강사 테스트 단계에서 데이터 보존 필요 없음"임을 코드 주석으로 명시하면 점수 평가에서 의도성 인정.

#### C1-11 Firestore listener unsubscribe

- **상태**: 🟢 안전 (snapshot listener 사용 0건)
- **근거**:
  - grep `addSnapshotListener` / `awaitClose` / `ListenerRegistration` / `removeListener` 모두 0건
  - Firestore 사용은 [DrinkRemoteDataSourceImpl.kt:30](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L30) `db.collection(COLLECTION).get().await()` 단발 호출만 (Room 캐싱 후 종료).
  - 결과적으로 listener leak 위험 자체가 없음. 다만 실시간 동기화 기능을 추가하면 그때 패턴 검증 필요.

#### C1-12 firestore.rules / storage.rules

- **상태**: 🔴 발견 (Firebase 룰 파일 부재)
- **근거**:
  - `firestore.rules`, `storage.rules`, `firebase.json` 전부 Glob 0건
  - Firestore가 `drinks` 컬렉션 ([DrinkRemoteDataSourceImpl.kt:21](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L21)) 한 곳만 read하지만, Firebase Console 기본 규칙이 `test mode`인 경우 30일 후 모든 요청 거부. `production mode`인 경우 인증 사용자만 read 가능하나 미구현.
  - 현 앱은 Firebase Auth 사용 코드 0건 (`FirebaseAuth` / `signInWith` grep 무) → 익명 read도 불가능할 수 있음.
- **권고**:
  - `firestore.rules` 작성: `drinks` 컬렉션은 모든 사용자 read 허용 (정적 catalog), 다른 컬렉션 차단:
    ```
    match /drinks/{doc} { allow read: if true; allow write: if false; }
    match /{document=**} { allow read, write: if false; }
    ```
  - 현재 `drinks` 컬렉션은 익명 read 정책에 의존하므로, Firebase Console 상태(test mode 만료 일자) 확인 필수. 강사 테스트 당일 read 실패 → 음료 검색 화면 빈 결과.

---

### 🟡 MEDIUM

#### C1-13 Hilt 의존성 / Service 주입

- **상태**: 🟡 주의 (`UserProfileLocalDataSource`가 비어있는데 바인딩됨)
- **근거**:
  - [UserProfileLocalDataSource.kt:5-6](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/local/UserProfileLocalDataSource.kt#L5): `interface UserProfileLocalDataSource` (메서드 0개)
  - [UserProfileLocalDataSourceImpl.kt:6-8](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/local/UserProfileLocalDataSourceImpl.kt#L6):
    ```kotlin
    class UserProfileLocalDataSourceImpl @Inject constructor(
        // TODO: UserProfileDao 주입 필요
    ) : UserProfileLocalDataSource
    ```
  - [UserProfileRepositoryImpl.kt:18-21](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/UserProfileRepositoryImpl.kt#L18): `getUserProfile()`이 항상 `return null`
  - 영향: [CalculateResidualUseCase.kt:19-26](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/domain/usecase/caffeine/CalculateResidualUseCase.kt#L19) `when (userProfile?.sensitivity)` → 항상 `else -> 5.0` 분기로 떨어짐. **온보딩에서 선택한 카페인 민감도(민감/보통/낮음)가 잔류량 계산에 반영되지 않음**.
  - [OnboardingViewModel.kt:86-94](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/OnboardingViewModel.kt#L86): `completeOnboarding()`은 `setOnboardingCompleted(true)`만 호출하고 height/weight/sensitivity는 어디에도 저장 안 됨.
- **권고**:
  - Hilt 컴파일은 통과하나 비즈니스 로직이 무력화. `UserProfileDao` + `UserProfileEntity`(현재 비어있음, [UserProfileEntity.kt:0줄](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/local/entity/UserProfileEntity.kt))를 채우고 `OnboardingViewModel.completeOnboarding()`에서 `UserProfileRepository.saveUserProfile()` 호출 필요.
  - `SnoffeeDatabase.entities = [Caffeine, Drink, Sleep]`에 UserProfile 미포함 ([SnoffeeDatabase.kt:14-18](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/local/SnoffeeDatabase.kt#L14)) — schema 추가 + version 3 + migration 동시에 작업.

#### C1-14 ProGuard / R8 release 설정

- **상태**: 🟡 주의 (`isMinifyEnabled = false`, release 빌드 미검증)
- **근거**:
  - [app/build.gradle.kts:23-31](teams-docs/1team/repo/app/build.gradle.kts#L23):
    ```kotlin
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(...)
        }
    }
    ```
  - [wear/build.gradle.kts:22-30](teams-docs/1team/repo/wear/build.gradle.kts#L22): 동일하게 `isMinifyEnabled = false`
  - `signingConfigs` 블록 없음 → release 빌드 시 debug keystore 자동 사용 (Play Store 업로드 불가).
- **권고**:
  - 현 단계는 debug 빌드 위주 테스트라 영향 없음. 출시 직전 minify 활성화 시 Gson Drink/Sleep/Caffeine DTO `@Keep` 또는 ProGuard 룰 추가 필요. 특히 `DrinkDto` ([DrinkDto.kt:3](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/model/DrinkDto.kt#L3)) `@PropertyName` 사용 → R8 필드 obfuscation 시 Firestore deserialize 실패.

#### C1-15 Wear 모듈 build.gradle.kts dependency 일관성

- **상태**: 🔴 발견 (Hilt/Compose 등 사용 가정 의존성 모두 부재 + 모듈 코드 0줄)
- **근거**:
  - [wear/build.gradle.kts:37-39](teams-docs/1team/repo/wear/build.gradle.kts#L37):
    ```kotlin
    dependencies {
        implementation(libs.play.services.wearable)
    }
    ```
    오직 `play-services-wearable`만 존재. Compose, Hilt, Wear OS Compose Material, Wear OS Tile, kotlinx-coroutines 등 0건.
  - 모바일 app/build.gradle.kts가 firestore/Room/Hilt 등 30개 의존성 등록한 것과 대비.
  - wear plugins 블록 ([wear/build.gradle.kts:1-3](teams-docs/1team/repo/wear/build.gradle.kts#L1))에 `kotlin.compose` / `hilt` / `ksp` 모두 미적용 → kotlinc도 안 돌 가능성 (단 application plugin만으로는 코틀린 컴파일 자동 적용 안 됨, AGP 9.0에서는 명시 필요).
- **권고**:
  - 워치 모듈이 실제 화면 1개라도 띄우려면 최소: `androidx.compose.ui:ui` + `androidx.wear.compose:compose-material` + `kotlin.compose` 플러그인 적용 필요.
  - 현 상태로 시연하면 워치 측 "앱 미설치" 또는 빈 launcher 아이콘만 보임. 페어 데모 항목 제외 또는 wear 모듈 최소 화면 (Text 1줄) 우선 추가 권고.

---

## 추가 발견 (체크리스트 외)

### A1. ReportViewModel 하드코딩 더미 데이터 ⭐⭐ (시연 직격탄)

- **위치**: [ReportViewModel.kt:84-214](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L84)
- **요약**: `loadReportData()`가 실 데이터 호출(`async { getReportUseCase(...) }`)을 주석 처리하고 [ReportViewModel.kt:86](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/report/ReportViewModel.kt#L86)부터 시작하는 `dummyCaffeineRecords` / `dummySleepData` / `dummyCaffeineChart`로 UI를 채움.
- **영향**: 강사가 "리포트 탭"을 누르면 항상 같은 가짜 숫자 (스타벅스 아메리카노 150mg 등) 노출. 사용자가 음료를 새로 입력해도 리포트에는 반영 안 됨.
- **권고**: 시연 전 주석 토글로 실 데이터 경로 활성화 + 더미 블록 제거. 단, 실 데이터 경로 활성화 시 Room이 비어있으면 `isDbEmpty = true` 분기로 빈 화면 → 데모 시나리오 (음료 1~3건 미리 입력) 사전 준비 필요.

### A2. OnboardingViewModel이 사용자 입력을 저장하지 않음 ⭐⭐⭐ (요약 1위 후보)

- **위치**: [OnboardingViewModel.kt:86-94](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/onboarding/OnboardingViewModel.kt#L86)
- **요약**: `completeOnboarding()`이 `onboardingPreferenceDataSource.setOnboardingCompleted(true)`만 호출. height/weight/sensitivity는 ViewModel state에만 보존되었다가 `onCleared` 시 소실.
- **영향**: C1-13과 결합. 카페인 민감도가 잔류량 계산에 절대 반영되지 않으며, 키/몸무게도 미사용. 개인화 UX의 핵심이 무력화.
- **권고**: `UserProfileRepository.saveUserProfile(UserProfile(height, weight, sensitivity))` 호출 추가. UserProfileLocalDataSourceImpl 구현 (UserProfileDao 작성, SnoffeeDatabase entity 등록) 선행 필요. **C1-13와 함께 단일 PR로 처리 권고**.

### A3. SettingViewModel.kt 빈 파일

- **위치**: [SettingViewModel.kt:1-2](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/setting/SettingViewModel.kt#L1) — package 선언만 있음 (2줄)
- **요약**: 설정 화면 ViewModel 미구현. SettingScreen.kt도 미점검이나 ViewModel 부재로 추가 검증 필요.

### A4. Firestore DB 인스턴스 명명 충돌 (정보)

- **위치**: [DrinkRemoteDataSourceImpl.kt:27](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L27)
- **요약**: `FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "drink")`로 named database `drink` 사용. Firebase Console에서 default 외에 `drink` named DB가 생성되어 있어야 함. 미생성 시 fetch 실패.
- **권고**: Firebase Console에서 named DB `drink` 존재 + `drinks` 컬렉션 데이터 적재 + 강사 테스트 직전 read 1회 sanity check 권고.

### A5. SnoffeeApplication에서 GlobalScope 유사 패턴 (정보)

- **위치**: [SnoffeeApplication.kt:19-22](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/SnoffeeApplication.kt#L19)
- **요약**:
  ```kotlin
  CoroutineScope(Dispatchers.IO).launch {
      initializer.initializeIfEmpty()
  }
  ```
  `GlobalScope` 명시 아니지만 Application 수명의 unscoped CoroutineScope. 앱 시작 시 1회 호출이므로 누수 위험은 낮으나, Application 수준 SupervisorScope를 명시하는 패턴이 더 안전.
- **권고**: 우선순위 낮음. 단 `initializeIfEmpty()`에서 Firestore fetch 실패 시 ([DrinkRemoteDataSourceImpl.kt:38-41](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/remote/DrinkRemoteDataSourceImpl.kt#L38) `emptyList()` 반환) 묵묵히 빈 리스트가 캐싱되어 다시 시도 안 됨 → 다음 검색 시에도 동일 시도하지 않음 (`localDataSource.getCount() == 0` 조건이 만족되어도 Firestore가 다시 fail하면 무한 빈 상태). 재시도 로직 권고.

### A6. CaffeineSearchScreen LaunchedEffect listState 무한 페이징 트리거 (잠재)

- **위치**: [CaffeineSearchScreen.kt:91](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/caffeine/input/search/CaffeineSearchScreen.kt#L91) `LaunchedEffect(listState)`
- **확인 필요**: listState 자체에 reactive trigger를 걸면 매 recomposition마다 effect 재시작 가능. 본문 코드는 미정독. 페이징 시 의도된 동작인지 확인 권고.

### A7. MainActivity `collectAsState` 단발 사용 (정보)

- **위치**: [MainActivity.kt:35](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/MainActivity.kt#L35) — `collectAsState(initial = null)` (onboarding 완료 여부 1회 체크)
- **참고**: 다른 화면은 모두 `collectAsStateWithLifecycle` 사용 (HomeScreen, SleepScreen, CaffeinMainScreen, CaffeineSearchScreen, ReportScreen). MainActivity·OnboardingScreen만 `collectAsState`로 통일성 부족하나 Activity 수명 동안 1회성이라 영향 미미.

### A8. test 코드: CalculateResidualUseCaseTest.kt 빈 파일

- **위치**: [CalculateResidualUseCaseTest.kt:0줄](teams-docs/1team/repo/app/src/test/java/com/snoffee/app/usecase/CalculateResidualUseCaseTest.kt)
- **요약**: 테스트 파일은 존재하나 0줄. 단위 테스트 커버리지 0%.

### A9. WriteSleep 권한 미선언 (정보)

- 현재 `READ_SLEEP`만 선언. `SamsungHealthDataSource.saveSleepData()`가 인터페이스에 있지만 구현체 [SamsungHealthDataSourceImpl.kt:31-37](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L31)에서 빈 함수 + TODO. 단 `SleepRepositoryImpl.saveSleepData()` ([SleepRepositoryImpl.kt:18-25](teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/repository/SleepRepositoryImpl.kt#L18))는 Room local에만 저장. → 사용자 수동 수면 입력은 Health Connect로 안 올라가고 앱 내부에만 남음. 의도라면 OK.

---

## 점검 시 한계

- **실행 미수행**: Gradle sync / build / install 검증 0건. AGP 9.0.0 + Kotlin 2.2.10 조합은 비교적 최신 (2026.05 시점)이며 일부 알파/베타 사항이 있을 수 있으나 코드 정적 분석만 수행.
- **Firebase Console 미확인**: `drinks` 컬렉션 실제 존재 여부, named DB `drink` 생성 여부, security rules console 설정 등은 코드로 검증 불가.
- **google-services.json 미열람**: 안전 정책에 따라 내용 미확인. API 키 제한(restrict to Android app + package name) 적용 여부는 Firebase Console 검증 필요.
- **워치 페어링 실기기 테스트 0회**: applicationId 불일치는 코드상 명확하나, 실제 페어링 시도 시 Wear OS 시스템 로그(`Wearable.MessageApi`)에서 어떤 에러로 나타나는지 미검증.
- **CaffeineSearchScreen / 일부 Presentation 미정독**: 600+ 줄 추정 화면 코드는 ViewModel 위주로만 점검. UI 레벨 권한 처리 / 빈 상태 / 에러 토스트 등은 표본 검증.
- **release 빌드 미수행**: ProGuard 룰 부재가 실제 release 빌드에서 문제 일으키는지 미검증 (minify off 상태라 즉시 영향 없음).
- **테스트 실행 0회**: `CalculateResidualUseCaseTest.kt`가 빈 파일이라 실행할 코드 없음.

---

## 시연 직전 체크리스트 (강사 액션)

| 우선순위 | 항목 | 영향 |
|---|---|---|
| 🔴 1 | ReportViewModel 더미 데이터 → 실 데이터 토글 | 리포트 화면 가짜 숫자 노출 방지 |
| 🔴 2 | Gemini 호출 버튼/UseCase 호출부 try/catch 또는 "준비 중" UI | 크래시 방지 |
| 🔴 3 | Firebase Console: `drinks` 컬렉션 + named DB `drink` + security rules 확인 | 음료 검색 0건 방지 |
| 🟠 4 | 워치 페어 데모 항목 시연 시나리오에서 제외 (또는 wear 최소 Text 화면 추가) | 빈 워치 화면 노출 방지 |
| 🟠 5 | 온보딩 권한 거부 분기 안내 (현재 dead-end) | 시연자가 권한 거부 시 막힘 |
| 🟡 6 | 음료 입력 1~3건 미리 등록 (실 데이터 경로 활성화 시) | 빈 리포트 회피 |
