# 2팀 Umma — 크리티컬 점검 결과 (2026-05-22)

> 점검자: 보조강사 (Android 보안/품질 리뷰)
> 점검 대상: `teams-docs/2team/repo/app/` (단일 모듈, Kotlin/Compose/Hilt/Firebase AI Logic)
> 입력 자료: `03_checklist.md` C2-1 ~ C2-16
> 점검 범위: 강사진 곧 테스트 시작 — 실행/심사/데이터 손실/보안 크리티컬 항목 우선

---

## 요약 (Top 3 critical)

1. **🔴 `local.properties`의 `API_KEY`가 워킹트리에 평문 + BuildConfig 노출**
   루트의 [`local.properties`](teams-docs/2team/repo/local.properties#L9)에 Google `AIzaSy***` 형태의 평문 키가 그대로 저장되어 있고, repo의 [`.gitignore`](teams-docs/2team/repo/.gitignore#L3)에는 `/local.properties`가 포함되어 있음에도 실제로 파일이 워킹트리에 존재. 또한 [`app/build.gradle.kts:33`](teams-docs/2team/repo/app/build.gradle.kts#L33)에서 `buildConfigField("String", "API_KEY", "\"$apiKey\"")`로 APK에 임베드됨. 실 사용처는 Kotlin 코드에서 0건이지만(dead BuildConfig field) APK 디컴파일 시 평문 노출. **즉시 키 회수(rotate) + 파일 untrack 필요**.

2. **🔴 Firebase App Check 미적용 + 클라이언트 SDK 직접 호출**
   [`AIModule.kt`](teams-docs/2team/repo/app/src/main/java/com/example/umma/di/AIModule.kt#L29)는 `Firebase.ai(backend = GenerativeBackend.googleAI())`로 Firebase AI Logic SDK를 사용하지만, App Check 초기화([`UmmaApplication.kt`](teams-docs/2team/repo/app/src/main/java/com/example/umma/UmmaApplication.kt))가 없음. 2026년 정책상 Gemini API key는 동일 GCP 프로젝트의 모든 클라이언트에서 도용 가능 → App Check 없으면 API quota/비용 abuse 가능.

3. **🔴 release 빌드에 `isMinifyEnabled = false` + `data/repository/fake/` 가 main source set에 포함**
   [`app/build.gradle.kts:37-44`](teams-docs/2team/repo/app/build.gradle.kts#L37)에서 release `isMinifyEnabled = false` → 디컴파일 시 소스 그대로 노출 + APK 크기/성능 손실. [`data/repository/fake/`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/fake/) 의 3개 Fake 구현체(`FakeCorrectionRepository`/`FakeFlashcardRepository`/`FakeLearningStateRepo`)가 `main/java/` 안에 위치하여 production APK에 포함됨. 현재 DI 바인딩은 real impl이지만, 코멘트 한 줄만 풀면 fake가 강사 시연 직전에 실수로 활성화될 위험 + 디컴파일 시 fixture/STALE 데이터 노출.

---

## 점검 결과 (체크리스트 순서)

### 🔴 CRITICAL

#### C2-1 Gemini Live API 호출 채널 / API key 노출

- **상태**: 🔴
- **근거**:
  - [`local.properties:9`](teams-docs/2team/repo/local.properties#L9)
    ```
    API_KEY=AIzaSy***[REDACTED]
    ```
    워킹트리에 평문 키(`AIzaSy` prefix Google API key)가 존재. `.gitignore`에 `/local.properties`가 있으므로 git history에는 없을 가능성이 높으나, 파일 자체가 archive 폴더에 포함되어 강사가 보는 시점에 노출됨.
  - [`app/build.gradle.kts:27-33`](teams-docs/2team/repo/app/build.gradle.kts#L27)
    ```kotlin
    val apiKey = properties.getProperty("API_KEY") ?: ""
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
    ```
    `BuildConfig.API_KEY`로 APK에 임베드. APK 디컴파일 시 평문 노출.
  - [`AIModule.kt:29-31`](teams-docs/2team/repo/app/src/main/java/com/example/umma/di/AIModule.kt#L29)
    ```kotlin
    fun provideFirebaseAI(): FirebaseAI =
        Firebase.ai(backend = GenerativeBackend.googleAI())
    ```
    Gemini 호출 자체는 Firebase AI Logic SDK 경유(권장 패턴). 즉 **`BuildConfig.API_KEY`는 dead code**이지만 APK에는 그대로 포함.
  - Kotlin 소스 전체에서 `BuildConfig.API_KEY` / `BuildConfig.` 참조는 0건 (Grep 확인).
- **권고**:
  1. `local.properties`의 키를 **즉시 회수(GCP console에서 regenerate)** 하고 git history에 키가 들어간 적이 있는지 확인.
  2. `buildConfigField("String", "API_KEY", ...)` 라인은 사용처가 없으므로 [`app/build.gradle.kts`](teams-docs/2team/repo/app/build.gradle.kts) 에서 삭제 (BuildConfig 누수 차단).
  3. `local.properties`를 추가 보호하기 위해 archive에 포함될 때 더미값으로 치환 권장.

#### C2-2 Firebase App Check 적용 여부

- **상태**: 🔴
- **근거**:
  - [`UmmaApplication.kt`](teams-docs/2team/repo/app/src/main/java/com/example/umma/UmmaApplication.kt) — Application 클래스가 비어 있어 App Check 초기화 코드 없음.
    ```kotlin
    @HiltAndroidApp
    class UmmaApplication : Application()
    ```
  - Grep 결과 전체 repo에서 `AppCheck` / `appCheck` 매칭 0건.
  - [`app/build.gradle.kts:99-103`](teams-docs/2team/repo/app/build.gradle.kts#L99) — `firebase.bom` / `firebase.ai` / `firebase.auth` / `firebase.firestore` 만 의존성에 포함되어 있고 `firebase-appcheck-playintegrity` / `firebase-appcheck-debug` 부재.
- **권고**:
  1. `libs.versions.toml` + `app/build.gradle.kts` 에 `firebase-appcheck-playintegrity` 추가.
  2. `UmmaApplication.onCreate()` 에서 `FirebaseAppCheck.getInstance().installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())` 호출.
  3. Firebase Console에서 AI Logic / Firestore 에 App Check enforcement 활성화.
  - 강사 테스트 단계에서 이를 빠뜨리면 Gemini Live quota 도용 시 비용 전가 위험.

#### C2-3 AudioRecord / AudioTrack `release()` 보장

- **상태**: 🟡 (AudioRecord OK / AudioTrack 부분적 OK / 화면-lifecycle 분리 시 누수 위험)
- **근거**:
  - **AudioRecord (마이크)** — [`AudioRecorder.kt:48-112`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioRecorder.kt#L48)
    `flow { ... }.flowOn(Dispatchers.IO)` 내부 `try / finally` 에서 `audioRecord.stop()` + `audioRecord.release()` 보장. Flow가 cancel 되면 `finally`가 실행됨 → 안전.
  - **AudioTrack (스피커)** — [`AudioPlayer.kt:11-81`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioPlayer.kt#L11)
    `@Singleton` + `init {}` 에서 `AudioTrack.Builder()` 로 1회 생성, `release()` 메서드 존재.
  - **ViewModel 해제 경로** — [`ChatViewModel.kt:628-631`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L628)
    ```kotlin
    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
    ```
    `onCleared`에서 `audioPlayer.release()` 호출 ✓.
  - **위험 지점 — Singleton AudioTrack**: AudioPlayer가 `@Singleton`이라 ViewModel 1회 release 후 동일 인스턴스를 재진입에서 재사용하면 native crash. 실제로 `onCleared`에서 `release()` → `audioTrack = null` 처리 후 재진입 시 `startPlaying()` 호출 시 `audioTrack?.play()` 가 null-safe로 빠져 **재생이 silent 실패** 가능.
  - **ChatScreen lifecycle**: [`ChatScreen.kt:67-70`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L67) 에서 `LaunchedEffect(Unit) { viewModel.checkInterestTopics(); viewModel.startChat() }` 만 있고 **`DisposableEffect` 없음**. 화면 이탈 시 `viewModel.stopChat()` 호출 없음 → ViewModel은 nav-graph scope에서 살아남으므로 onCleared 호출 지연 가능. AudioRecord는 recordJob cancel 시 즉시 해제되지만, AudioTrack은 release까지 ViewModel scope을 기다림.
- **권고**:
  1. AudioPlayer를 `@Singleton` 대신 ViewModel scope으로 바꾸거나, release 후 재초기화 로직 추가.
  2. `ChatScreen` 에 `DisposableEffect(Unit) { onDispose { viewModel.stopChat() } }` 추가하여 화면 이탈 시 명시적 정리.

#### C2-4 RECORD_AUDIO 권한 거부 시 fallback

- **상태**: 🟢
- **근거**:
  - [`AndroidManifest.xml:6`](teams-docs/2team/repo/app/src/main/AndroidManifest.xml#L6) — `<uses-permission android:name="android.permission.RECORD_AUDIO" />` 선언.
  - [`ChatScreen.kt:57-65`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L57) — `rememberLauncherForActivityResult(RequestPermission())` 로 runtime 요청 후 granted 여부를 ViewModel로 전달.
  - [`ChatViewModel.kt:136-152`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L136) — `startUserTurn(hasRecordAudioPermission: Boolean)` 가 false면 `microphonePermissionDenied = true`, `errorMessage = "마이크 권한이 필요합니다."` 로 UI 분기.
  - [`ChatScreen.kt:140-147`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L140) — UI에서 "마이크 권한이 필요합니다." 메시지 표시.
- **권고**: (선택) "권한 거부됨 → 설정 앱으로 이동" 버튼 추가로 UX 보완.

#### C2-5 Firestore Security Rules

- **상태**: 🔴
- **근거**:
  - repo 전체에 `firestore.rules`, `storage.rules`, `firebase.json` 파일 **없음** (Glob 0건, find 0건).
  - Firestore 사용 경로:
    - [`LearningStateRemoteDataSourceImpl.kt:32`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/remote/LearningStateRemoteDataSourceImpl.kt#L32) — `users/{uid}/...`
    - [`SessionMemoryRemoteDataSource.kt:30-34`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/remote/SessionMemoryRemoteDataSource.kt#L30) — `users/{uid}/sessions/{lang}`
    - [`CorrectionFlashcardRemoteDataSource.kt:41-43`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/remote/CorrectionFlashcardRemoteDataSource.kt#L41) — `users/{uid}/flashcards`
  - 모든 경로가 `users/{uid}/...` 패턴이지만 client-side에서만 `firebaseAuth.currentUser?.uid`를 사용. **server-side rules가 repo에 없음** → Firebase Console에서 직접 관리되고 있을 가능성이 있으나 코드 리뷰만으로는 확인 불가.
- **권고**:
  1. `firestore.rules` 와 `firebase.json` 을 repo에 commit (server-side 정책의 single source of truth).
  2. 기본 룰:
     ```
     match /users/{uid}/{document=**} {
       allow read, write: if request.auth != null && request.auth.uid == uid;
     }
     ```
  3. 강사 테스트 시 다른 사용자의 `users/{otherUid}/sessions/...` 를 read 시도해 차단되는지 확인 권장.

#### C2-6 Session memory (`recentFullContext`) 크기 제한

- **상태**: 🟢
- **근거**:
  - [`SessionMemoryRepositoryImpl.kt:38-58`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/SessionMemoryRepositoryImpl.kt#L38)
    ```kotlin
    private val maxRecentTurns: Int = 100
    ...
    localDataSource.appendTurn(turn = entity, maxRecentTurns = maxRecentTurns)
    ```
  - [`SessionMemoryLocalDataSource.kt:161-174`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/SessionMemoryLocalDataSource.kt#L161) — `trimOverflowTurns` 가 `turns.size - maxRecentTurns` 만큼 오래된 turn 삭제. Room transaction 내에서 처리.
  - [`ExtractCandidatesUseCase.kt:103-104`](teams-docs/2team/repo/app/src/main/java/com/example/umma/domain/usecase/correction/ExtractCandidatesUseCase.kt#L103) — `MAX_SOURCE_TURNS = 100` 으로 Correction 후보 추출도 100턴으로 제한.
- **권고**: (선택) 100턴이 system prompt 토큰 한도에 적합한지 cost monitoring 권장. 1턴당 50토큰 가정 시 100턴 = 5,000토큰 → Gemini Flash context 안에 들어가는 안전 범위.

---

### 🟠 HIGH

#### C2-7 SM-2: ease_factor 최솟값 1.3 floor 적용

- **상태**: 🟠 (부분 적용)
- **근거**:
  - [`ReviewSchedulePolicy.kt:25-44`](teams-docs/2team/repo/app/src/main/java/com/example/umma/domain/usecase/flashcardreview/ReviewSchedulePolicy.kt#L25)
    ```kotlin
    ReviewRating.AGAIN -> 0 to max(1.3, currentEF - 0.20)
    ReviewRating.HARD  -> ... to max(1.3, currentEF - 0.15)
    ReviewRating.GOOD  -> ... to currentEF              // floor 없음 (단조 유지)
    ReviewRating.EASY  -> ... to (currentEF + 0.15)     // floor 없음 (단조 증가)
    ```
  - AGAIN / HARD 에서는 1.3 floor 적용 ✓.
  - GOOD / EASY 는 EF를 줄이지 않으므로 floor 우회는 이론적으로 안전 (이미 1.3 이상이면 그대로 유지/증가).
  - 그러나 **초기 EF가 코드에 어디서 설정되는지 검색 시**: [`FakeFlashcardRepository.kt:27`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/fake/FakeFlashcardRepository.kt#L27) `FlashcardSchedule(0, 2.5, ...)` — 2.5(SM-2 표준 초기값) ✓. real impl의 초기 저장은 [`CorrectionFlashcardDto`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/model/correction/CorrectionFlashcardDto.kt) 에 의해 결정 (별도 확인 필요).
- **권고**:
  - 표준 SM-2와 다른 점: GOOD 시 interval 갱신이 `baseInterval * currentEF` (표준은 6일 → 6\*EF). 단위가 분이라(`baseInterval = 1440`) 1차 검토는 OK. 그러나 첫 GOOD 평가 시 `interval = 1440 * 2.5 = 3600분 = 2.5일` 로 SM-2 표준(6일 또는 6일\*EF)과 다름. 학습 효과 저하 가능.
  - 학습 데이터 손실 위험은 아니지만, 강사가 시연할 때 SM-2 표준과 다른 점을 명확히 인지하도록 README/docs에 정책 명시 권장.

#### C2-8 SM-2: q<3 시 reset 로직 (AGAIN 처리)

- **상태**: 🟠
- **근거**:
  - [`ReviewSchedulePolicy.kt:26-29`](teams-docs/2team/repo/app/src/main/java/com/example/umma/domain/usecase/flashcardreview/ReviewSchedulePolicy.kt#L26)
    ```kotlin
    ReviewRating.AGAIN -> {
        // 다시 봐야 하는 카드라서 간격을 0으로 두고, 세션 재등장은 ViewModel이 관리한다.
        0 to max(1.3, currentEF - 0.20)
    }
    ```
  - `interval = 0` 이면 [`SessionMemoryLocalDataSource.kt`의 due 쿼리](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/CorrectionFlashcardLocalDataSource.kt#L168) `nextReviewAt <= now`에서 즉시 다시 due 처리 ✓.
  - **그러나 SM-2 표준은 `repetitions=0, interval=1` reset**이지만 여기는 `interval=0` 만 적용 → `nextReviewAt = reviewedAt + 0` 으로 즉시 같은 세션에 재등장. 의도된 정책이나 표준과 차이.
  - "세션 재등장은 ViewModel이 관리한다" 라는 주석이 있지만 ViewModel(SrsStudyScreen)은 현재 [placeholder](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/srsstudy/SrsStudyScreen.kt#L16) 라 실제 재등장 로직 부재.
- **권고**:
  - `interval = 0` 의 의미가 "같은 세션 즉시 재출제"이면 SrsStudyScreen 구현 시 명시 필요.
  - 첫 베타 데모 시점에 SrsStudyScreen이 placeholder이므로 강사 테스트에서 deck 재출제 동작이 안 보일 수 있음 → 강사에게 명시 권장.

#### C2-9 SM-2: next_review_at timezone 일관성

- **상태**: 🟢
- **근거**:
  - [`ReviewSchedulePolicy.kt:48`](teams-docs/2team/repo/app/src/main/java/com/example/umma/domain/usecase/flashcardreview/ReviewSchedulePolicy.kt#L48)
    ```kotlin
    val nextReviewAt = reviewedAt + (nextInterval.toLong() * 60 * 1000)
    ```
    `reviewedAt`이 [`ApplyReviewDecisionUseCase.kt:33`](teams-docs/2team/repo/app/src/main/java/com/example/umma/domain/usecase/flashcardreview/ApplyReviewDecisionUseCase.kt#L33) 에서 전달됨. 호출자가 `System.currentTimeMillis()` (UTC epoch ms) 사용 시 timezone-agnostic ✓.
  - [`FlashcardRepositoryImpl.kt:31`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/FlashcardRepositoryImpl.kt#L31) — `now = System.currentTimeMillis()` 사용 ✓.
  - [`CorrectionFlashcardLocalDataSource.kt:168-170`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/CorrectionFlashcardLocalDataSource.kt#L168) — `nextReviewAt <= :now` Room 쿼리도 epoch ms 사용 ✓.
- **권고**: 통일적으로 epoch ms를 사용하고 있어 timezone 버그 위험 낮음.

#### C2-10 DashSummary 캐시와 원본 transcript 일관성

- **상태**: 🟡
- **근거**:
  - [`LearningStateRepoImpl.kt:215-270`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/LearningStateRepoImpl.kt#L215) — `updateFlashcardSummary` 에서 `FlashcardSummary` 와 `DashSummary` 를 같은 input으로 동시 갱신:
    ```kotlin
    val nextDash = previousDash.copy(
        dueFlashcards = input.dueFlashcards,
        savedFlashcards = input.savedFlashcards,
        ...
    )
    ```
    의도적으로 두 Summary가 같은 dueFlashcards/savedFlashcards 값을 가지도록 묶음 ✓.
  - 단, `LearningStateRemoteDataSourceImpl.kt` 의 fetch가 별도 collection(`dashboard_summaries`/`flashcard_summaries`)에서 읽어오므로 server-side에서 두 값이 어긋날 가능성이 있음. local-first 정책상 sync 후 일관성 회복.
  - lang key mismatch는 [`LangCode`](teams-docs/2team/repo/app/src/main/java/com/example/umma/domain/model/learningstate/LearningCoreModels.kt) enum 사용으로 차단되어 typo 위험 없음 ✓.
- **권고**: server-side 수동 편집 시 두 collection을 같이 바꾸는 정책 문서화.

#### C2-11 Firestore snapshot listener unsubscribe

- **상태**: 🟢
- **근거**:
  - Grep `addSnapshotListener` → 0건. **Firestore listener는 사용 없음**. 모든 Firestore 접근은 one-shot `.get().await()` 또는 batch `.commit().await()`.
  - `awaitClose` / `ListenerRegistration` 사용처:
    - [`NetworkConnectivityMonitor.kt:9-49`](teams-docs/2team/repo/app/src/main/java/com/example/umma/core/util/NetworkConnectivityMonitor.kt#L9) — Android ConnectivityManager callback, awaitClose에서 unregister ✓.
    - [`AuthRepositoryImpl.kt:27-36`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/AuthRepositoryImpl.kt#L27) — Firebase AuthStateListener, awaitClose에서 removeAuthStateListener ✓.
- **권고**: snapshot listener를 도입할 경우 동일 awaitClose 패턴 사용 유지.

#### C2-12 Coroutine scope leak (GlobalScope / custom CoroutineScope)

- **상태**: 🟠
- **근거**:
  - Grep `GlobalScope` / `runBlocking` → 0건 ✓.
  - **그러나** [`SessionMemoryRepositoryImpl.kt:61-63`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/SessionMemoryRepositoryImpl.kt#L61) — `appendTurn` 내에서 매 호출마다 `CoroutineScope(Dispatchers.IO).launch { syncPendingTurns(command.language) }` 호출:
    ```kotlin
    if (appendResult.inserted) {
        CoroutineScope(Dispatchers.IO).launch {
            syncPendingTurns(command.language)
        }
    }
    ```
    Repository는 `@Singleton` 이지만, **매번 새 CoroutineScope를 만들고 cancel을 호출하지 않음** → fire-and-forget 패턴. SupervisorJob 없으므로 자식 실패가 부모 cancel을 호출 → 다음 호출은 영향 없으나 누적 잡 누수 + Job 객체 누적. GlobalScope과 사실상 동일한 효과.
  - 또한 [`ChatRepositoryImpl.kt:94`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L94) — `private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)` — Singleton scope를 만들고 cancel 정책 없음. ChatRepository가 Singleton이므로 앱 생명주기와 같이 살아가는 점은 OK 이나, 명시적 cleanup 메서드 없음.
- **권고**:
  - `SessionMemoryRepositoryImpl` 에 `private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)` 추가하고 그 scope에서 launch.
  - 또는 `syncPendingTurns` 호출을 호출자(UseCase)에 옮기고 호출자의 `viewModelScope` 활용.

#### C2-13 `data/repository/fake/` release 빌드 포함 여부

- **상태**: 🔴
- **근거**:
  - 3개 Fake 모두 [`app/src/main/java/com/example/umma/data/repository/fake/`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/fake/) 에 위치 → release APK에 포함 (Kotlin 컴파일 대상).
    - [`FakeCorrectionRepository.kt`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/fake/FakeCorrectionRepository.kt)
    - [`FakeFlashcardRepository.kt`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/fake/FakeFlashcardRepository.kt)
    - [`FakeLearningStateRepo.kt`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/fake/FakeLearningStateRepo.kt) — 235줄, 사용자 fixture (`activeUser`, `corruptedSelectedLang`) 와 `STALE_TIMESTAMP = 1_700_000_000_000L` 등 디버그 코드 다수.
  - DI 바인딩은 real impl 사용 ([`RepositoryModule.kt:54-68`](teams-docs/2team/repo/app/src/main/java/com/example/umma/di/RepositoryModule.kt#L54), [`L100-106`](teams-docs/2team/repo/app/src/main/java/com/example/umma/di/RepositoryModule.kt#L100))이지만 한 줄 코멘트 처리로 fake로 토글되도록 설계 → **강사 데모 직전 실수로 fake가 활성화되어 mocked 데이터가 보여질 위험**.
  - [`build.gradle.kts:37-44`](teams-docs/2team/repo/app/build.gradle.kts#L37) release `isMinifyEnabled = false` 이므로 minify로 fake 클래스를 자동 제거하지도 못함.
- **권고**:
  1. 단기: `data/repository/fake/` 를 `app/src/debug/java/...` 또는 `app/src/androidTest/java/...` 로 이동 → release APK에서 제외.
  2. 단기 차선: `RepositoryModule` 내의 fake 토글 코멘트 줄을 제거 (실수 차단).
  3. 장기: build variant (debug / release / mockData) 도입.

---

### 🟡 MEDIUM

#### C2-14 Audio buffer size 적절성

- **상태**: 🟢
- **근거**:
  - [`AudioRecorder.kt:38-43`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioRecorder.kt#L38) — `AudioRecord.getMinBufferSize(SAMPLE_RATE=16000, MONO, PCM_16BIT) * 2` ✓ (Android 표준 권장 패턴).
  - [`AudioPlayer.kt:32-36`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioPlayer.kt#L32) — `AudioTrack.getMinBufferSize(SAMPLE_RATE=24000, MONO, PCM_16BIT) * 2` ✓.
  - 단, 입력 16kHz vs 출력 24kHz로 sample rate가 다른데 [`ChatRepositoryImpl.kt:559`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L559) `mimeType = "audio/pcm;rate=16000"` 으로 송신 → Gemini Live가 24kHz로 응답하면 일관 (Gemini Live 표준).
- **권고**: 없음.

#### C2-15 AI 호출 중복/디바운싱

- **상태**: 🟡
- **근거**:
  - [`ChatViewModel.kt:81-92`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L81) — `startChat()` 에서 `if (_uiState.value.sessionState == SessionState.LOADING) return@launch` 로 중복 호출 방어 ✓.
  - [`ChatViewModel.kt:158-162`](teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L158) — `beginUserTurn()` 에서 `if (currentState.isRecording) return; if (recordJob?.isActive == true) return` ✓.
  - [`ChatRepositoryImpl.kt:125-134`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L125) — `startSession`이 같은 (langCode, systemInstruction)이면 동일 session 재사용 ✓ + `sessionMutex` 로 동시 호출 직렬화 ✓.
  - 다만 PTT 버튼 연타 시 `endUserTurn` → 즉시 `startUserTurn` 사이클은 Gemini Live에 짧은 audio chunk를 다수 전송할 가능성. 비용 폭발 방어는 없지만 사용자 의도이므로 OK.
- **권고**: PTT 버튼에 50~100ms throttle 권장 (선택).

#### C2-16 Room migration 정책

- **상태**: 🟡
- **근거**:
  - [`DatabaseModule.kt:30`](teams-docs/2team/repo/app/src/main/java/com/example/umma/di/DatabaseModule.kt#L30) — `SessionMemoryDatabase`: `.fallbackToDestructiveMigration(false)` → migration 없으면 **앱 크래시** (데이터 손실은 막지만 사용자 경험 폭발).
  - [`DatabaseModule.kt:52`](teams-docs/2team/repo/app/src/main/java/com/example/umma/di/DatabaseModule.kt#L52) — `CorrectionFlashcardDatabase`: 동일 정책.
  - 양쪽 DB 모두 `version = 1` ([`SessionMemoryDatabase.kt:159-163`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/SessionMemoryDatabase.kt#L159), [`CorrectionFlashcardLocalDataSource.kt:252-256`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/CorrectionFlashcardLocalDataSource.kt#L252)) 라 현재 시점 영향은 없음.
  - `exportSchema = false` 이므로 schema diff 도구 사용 불가.
- **권고**:
  - 강사 테스트 단계는 version=1 고정이므로 영향 없으나, **베타 출시 후 schema 변경 시 반드시 `Migration` 객체 또는 `fallbackToDestructiveMigration(true)` (개발 단계만) 적용 필요**.
  - `exportSchema = true` 로 변경하면 `app/schemas/` 에 schema json이 commit되어 diff 검토 가능.

---

## 추가 발견 (체크리스트 외)

### A1. release 빌드 ProGuard/R8 미적용 🟠

- [`app/build.gradle.kts:37-44`](teams-docs/2team/repo/app/build.gradle.kts#L37) — `isMinifyEnabled = false`.
- [`proguard-rules.pro`](teams-docs/2team/repo/app/proguard-rules.pro) — 기본 boilerplate만, 실 룰 없음.
- **영향**:
  - APK 크기 증가, 소스 코드 디컴파일 시 그대로 노출.
  - `data/repository/fake/` 등 dead code가 제거되지 않음.
- **권고**: 강사 테스트 후 정식 배포 전 minify + Kotlinx Serialization / Firestore DTO `@Keep` 룰 추가.

### A2. AndroidManifest android:allowBackup=true + dataExtractionRules 🟡

- [`AndroidManifest.xml:11-13`](teams-docs/2team/repo/app/src/main/AndroidManifest.xml#L11) — `android:allowBackup="true"` + `dataExtractionRules`/`fullBackupContent` 지정.
- **영향**: 자동 백업이 켜져 있어 로컬 Room DB (`umma_session_memory_db`, `umma_correction_flashcard_db`) + DataStore (`learning_state.preferences_pb`) 가 클라우드 백업 대상. 학습 데이터가 사용자 Google 계정에 백업됨.
- **권고**: `backup_rules.xml` / `data_extraction_rules.xml` 실제 내용 확인 후 민감 데이터 (DataStore preferences, Room DB) 백업 제외 정책 명시 권장. 본 점검에서는 두 xml 파일 내용을 별도 읽지 않음 → **점검 시 한계** 항목 참조.

### A3. README 보안 가이드와 실제 구현 불일치 🟡

- [`README.md:395`](teams-docs/2team/repo/README.md#L395) — "Firebase / Gemini 관련 보안 키는 클라이언트에 직접 노출하지 않는 구조를 원칙으로 합니다." 명시.
- 그러나 [`local.properties:9`](teams-docs/2team/repo/local.properties#L9) 평문 키 + [`build.gradle.kts:33`](teams-docs/2team/repo/app/build.gradle.kts#L33) BuildConfig 노출은 README 원칙과 정면 충돌. 실제로는 dead code이지만 명목상 위반.
- **권고**: BuildConfig field 제거 시 자연스레 해소.

### A4. ChatRepositoryImpl 광범위한 `Log.d` 🟡

- [`ChatRepositoryImpl.kt:224-296`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L224) — 사용자/AI transcript 전체를 `Log.d("ChatRepository", "USER partial text=$text")` 등으로 logcat에 출력.
- **영향**: release 빌드에서도 logcat에 사용자 발화 전체가 평문 출력 → 학습 콘텐츠/개인정보 노출. logcat은 다른 앱에서 직접 읽지는 못하나 ADB 연결 시/디바이스 분실 시 노출 위험.
- **권고**: release 빌드에서 `BuildConfig.DEBUG` 체크 또는 Timber로 변경.

### A5. CoroutineScope 직접 생성 (`ChatRepositoryImpl`, `SessionMemoryRepositoryImpl`) 🟡

- [`ChatRepositoryImpl.kt:399`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L399) — `receiveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)` 매 connectLiveTransport 호출마다 새로 만들고, `closeLiveTransport`에서 `receiveScope?.cancel()` 호출 ✓.
- 단, repository 차원의 `repositoryScope` (line 94) 는 cancel 호출 없음. Singleton이라 앱 생명주기와 같이 가지만, **테스트에서 Hilt 컨테이너 종료 시 누수 잠재력 있음**.
- 자세한 내용은 C2-12 참조.

### A6. network_security_config 미설정 🟡

- Grep 결과 `network_security_config` / `usesCleartextTraffic` 매칭 0건.
- [`AndroidManifest.xml`](teams-docs/2team/repo/app/src/main/AndroidManifest.xml) — `android:networkSecurityConfig` 속성 없음.
- **영향**: Android 9+ 기본값으로 cleartext 차단되지만, 명시적으로 `cleartextTrafficPermitted="false"` 적용 권장.
- **권고**: `res/xml/network_security_config.xml` 생성 + `application` 태그에 추가.

---

## 점검 시 한계

1. **`google-services.json` 내용 미열람** — 사용자 지시대로 파일 존재만 확인. 실제 Firebase project ID, API key restriction, package_name 일치 여부 등은 점검하지 않음.
2. **Firebase Console 설정 미확인** — Firestore rules / Storage rules / App Check enforcement 가 Firebase Console에 직접 입력되어 있을 가능성. 본 점검은 repo 코드만 검토.
3. **`backup_rules.xml` / `data_extraction_rules.xml`** — AndroidManifest에 referenced되지만 실제 내용 미점검 (A2 항목 참조).
4. **`CorrectionFlashcardDto` 초기 EF 값** — SM-2 초기 EF=2.5가 실제 DTO 생성 시점에 설정되는지 별도 확인 필요 (C2-7).
5. **SrsStudyScreen placeholder** — SRS 화면이 placeholder 상태라 실제 deck 렌더링/평가 입력 ViewModel 부재. C2-7 ~ C2-9 평가는 정책 코드(`ReviewSchedulePolicy.kt`)만 기준으로 함.
6. **Gemini 모델 ID** — [`ChatRepositoryImpl.kt:369`](teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L369) `modelName = "gemini-3.1-flash-live-preview"` — preview 모델로 안정성/quota 정책이 GA 모델과 다를 수 있으나 본 점검 범위 밖.
7. **테스트 커버리지** — `app/src/test/` 에 `CorrectionAiResponseMapperTest`, `ExtractCandidatesUseCaseTest` 2개만 존재. SM-2 policy 테스트, ChatViewModel 테스트 부재. 점검 범위 밖이지만 강사 테스트 회귀 위험 신호.

---

## 마무리 — 강사진 테스트 시작 전 권장 action item

| 우선순위 | Action | 예상 작업 시간 |
|---|---|---|
| 🔴 P0 | `local.properties`의 `API_KEY` 즉시 회수 (GCP console regenerate) | 5분 |
| 🔴 P0 | `app/build.gradle.kts` 에서 `buildConfigField("String", "API_KEY", ...)` 라인 삭제 | 5분 |
| 🔴 P0 | Firebase App Check (PlayIntegrity) 적용 + UmmaApplication에서 초기화 | 1~2시간 |
| 🔴 P0 | `firestore.rules` 파일 작성 + commit, Firebase deploy | 1시간 |
| 🔴 P0 | `data/repository/fake/` 를 `src/debug/java/` 또는 `androidTest/` 로 이동 | 30분 |
| 🟠 P1 | `SessionMemoryRepositoryImpl` 의 `CoroutineScope(Dispatchers.IO).launch` 패턴을 repository scope로 변경 | 30분 |
| 🟠 P1 | `ChatScreen` 에 `DisposableEffect { onDispose { viewModel.stopChat() } }` 추가 | 15분 |
| 🟠 P1 | `ChatRepositoryImpl` 의 `Log.d` 를 release에서 끄도록 변경 | 15분 |
| 🟡 P2 | `network_security_config.xml` 작성 + Manifest 등록 | 20분 |
| 🟡 P2 | `proguard-rules.pro` 룰 작성 + release `isMinifyEnabled = true` | 1~2시간 (regression test 포함) |

**Total P0 작업 시간**: 약 3~4시간. 강사 테스트 1일 전 확보 권장.
