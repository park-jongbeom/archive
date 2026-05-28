# 2팀 Umma — 화면 동작/크래시 재점검 (2026-05-22)

> 관점: 강사진이 실 단말 빌드 후 화면 클릭 시 만나는 버그
> 보안/구조 이슈는 [`04_team2_findings.md`](04_team2_findings.md) 참조 (중복 제외)
> 점검 범위: `presentation/**`, `core/navigation/**`, `MainActivity.kt`, `UmmaApplication.kt`, `AndroidManifest.xml`, `ChatRepositoryImpl.kt`, ViewModel `init` / 라이프사이클, DI 바인딩

---

## 점검 한 줄 요약

- **즉시 크래시 위험은 거의 없음** — `init {}` 단순(AuthViewModel 1건), `!!`/`requireNotNull` 0건(production 코드 한정), `TODO()` 0건. ChatViewModel/DashboardViewModel 모두 launch + try/catch + onFailure 분기 구비.
- **하지만 "버튼은 눌리는데 아무 일도 일어나지 않는" 무반응/silent 버그가 다수** — Placeholder 3개 화면, MyPage 모국어 저장 미구현, Topic 다이얼로그 cancel 미구현, AudioPlayer @Singleton 재진입 silent fail 등.
- 강사가 단말에서 가장 먼저 만날 시연 차단 시나리오는 **R2-1 ~ R2-5** 로 분류. 데모 직전 30~60분 핫픽스로 R2-1, R2-2, R2-4 처리 권장.

---

## 강사 시연 시 크래시 / 무반응 시나리오 (Top)

### 🔴 시나리오 1 (R2-1): BottomBar에서 "복습/통계/교정" 탭 클릭 → 빈 placeholder 화면

- **재현 단계**:
  1. Google 로그인 → 온보딩 → 닉네임/언어 선택 → Dashboard 진입
  2. 하단 BottomBar에서 "복습" 또는 "통계" 또는 "교정" 탭 클릭
- **결과**: 헤더(`UmmaAppBar`)만 보이고 본문에 "복습 플레이스 홀더" / "통계 플레이스 홀더" / "교정 플레이스 홀더" 라는 **한 줄 텍스트** 만 표시
- **호출 경로**:
  - [`UmmaBottomAppBar.kt:60-104`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/core/navigation/UmmaBottomAppBar.kt#L60) — `NavItem.list` 의 5개 탭 그대로 노출
  - [`UmmaNavHost.kt:93-110`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/core/navigation/UmmaNavHost.kt#L93) — Statistics / SrsStudy / Correction 모두 라우트 매핑 정상
  - [`StatisticsScreen.kt`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/statistics/StatisticsScreen.kt) (41줄), [`SrsStudyScreen.kt`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/srsstudy/SrsStudyScreen.kt) (36줄), [`CorrectionScreen.kt`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/correction/CorrectionScreen.kt) (38줄) — 모두 `Text("XX 플레이스 홀더")` 한 줄
- **04 보고 보완**: 04는 `SrsStudyScreen` 만 placeholder로 명시 ([04 C2-8 주석](04_team2_findings.md)) — 사실 **3개 화면이 동시 placeholder**. 5개 탭 중 60% (3/5) 가 데모 가능 화면 없음.
- **권고**:
  1. 데모 동선을 **Dashboard ↔ Chat 2개 탭** 로 한정해 강사에게 사전 안내.
  2. 또는 placeholder 3개 화면에 "이 기능은 다음 스프린트 예정입니다." 안내 + 일러스트 추가.
  3. BottomBar의 미구현 탭에 `Badge` 또는 회색 처리로 시각적 구분.

---

### 🔴 시나리오 2 (R2-2): Chat 진입 → 토픽 다이얼로그 5개 선택 강제 → 취소/뒤로가기 불가 (사용자 갇힘)

- **재현 단계**:
  1. 신규 사용자가 Chat 탭 진입
  2. `checkInterestTopics()` 가 `interestTopics.isEmpty()` 판정 → `showTopicDialog = true` (관심 주제 5개 선택 다이얼로그)
  3. 사용자가 0~4개만 선택하고 "완료" 버튼 누름 → "주제를 정확히 5개 선택해 주세요." 에러
  4. 사용자가 **취소(X)** 버튼 또는 **뒤로가기** 버튼 누름 → **다이얼로그 닫히지 않음**
- **결과**: 5개 선택 전에는 Chat 화면 자체에 접근 불가. 신규 사용자가 우연히 Chat에 들어왔다가 빠져나갈 수 없음.
- **호출 경로**:
  - [`ChatScreen.kt:159-165`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L159)
    ```kotlin
    UmmaDialog(
        title = "관심 주제 선택 5개",
        ...
        onCancel = {},                    // ← 빈 람다
        onConfirm = { viewModel.saveInterestTopics() },
        confirmText = "완료"
    )
    ```
  - [`UmmaDialog.kt:73`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/core/ui/component/UmmaDialog.kt#L73) — `Dialog(onDismissRequest = { onCancel() })` → 빈 람다 호출되므로 dismiss 안 됨.
  - [`ChatViewModel.kt:658-687`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L658) — `topics.size != 5` 면 errorMessage 만 세팅 후 return.
- **권고**: 단기 핫픽스
  ```kotlin
  onCancel = { viewModel.dismissTopicDialog() }  // ViewModel에 메서드 추가
  ```
  + 다이얼로그 cancel 시 Chat 화면을 BackStack pop 또는 placeholder 메시지("관심 주제를 등록해야 대화를 시작할 수 있어요") 표시.

---

### 🔴 시나리오 3 (R2-3): Chat 재진입 시 AI 음성이 silent fail (AudioPlayer @Singleton release 후 미재초기화)

- **재현 단계**:
  1. Chat 탭 진입 → "내 말하기 시작" → AI 응답 음성 정상 재생
  2. Dashboard로 돌아감 (BottomBar) → ChatViewModel.onCleared() 호출 → `audioPlayer.release()` 호출
  3. [`AudioPlayer.kt:78-81`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioPlayer.kt#L78) — `audioTrack?.release(); audioTrack = null`
  4. Chat 탭 다시 진입 → 새 ChatViewModel 인스턴스 만들어지지만 `audioPlayer`는 **같은 @Singleton 인스턴스** (init 블록은 1회만 실행됨)
  5. `startPlaying()` → `audioTrack?.state` 가 **null** → `audioTrack?.play()` no-op (silent)
  6. `playAudioChunk(audio)` → `audioTrack?.write(...)` no-op
- **결과**: AI partial transcript는 텍스트로 보이지만 **음성이 안 들림**. 사용자는 "AI가 응답 중입니다" 라는 상태 표시만 보고 마이크가 고장났다고 오해.
- **호출 경로**:
  - [`AudioPlayer.kt:30-57`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioPlayer.kt#L30) — `init {}` 에서 1회 AudioTrack 생성 → release 후 재생성 로직 없음.
  - [`AudioModule.kt:23-27`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/di/AudioModule.kt#L23) — `@Binds @Singleton` 으로 SingletonComponent에 박혀 있음.
  - [`ChatViewModel.kt:628-631`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L628) — `onCleared` 에서 무조건 `audioPlayer.release()`.
- **04 보고 보완**: 04 C2-3 에서 "Singleton AudioTrack" 위험 언급 — 본 시나리오는 **재현 가능한 실 시나리오**로 격상. 강사가 시연 도중 Chat→Dashboard→Chat 사이클을 한 번이라도 돌리면 무조건 발생.
- **권고** (난이도 순):
  1. **즉시 핫픽스**: `AudioPlayer.startPlaying()` 호출 시 `audioTrack == null` 이면 init 블록 재실행:
     ```kotlin
     override fun startPlaying() {
         if (audioTrack == null) reinitialize()
         if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) audioTrack?.play()
     }
     ```
  2. 또는 `@Singleton` 제거 후 `@ViewModelScoped`로 변경 (Hilt 어노테이션 추가 필요).
  3. 또는 `ChatViewModel.onCleared` 에서 release 하지 말고 `stopPlaying` 만 호출.

---

### 🟠 시나리오 4 (R2-4): MyPage "모국어 설정" — 선택 후 "완료" 눌러도 저장 안 됨 (silent UX 버그)

- **재현 단계**:
  1. Dashboard 우상단 마이페이지 아이콘 → MyPageScreen
  2. "모국어 설정" 버튼 → 다이얼로그 열림
  3. "English" 선택 → 라디오 강조 표시됨
  4. "완료" 버튼 누름 → 다이얼로그 닫힘
  5. 다시 "모국어 설정" 진입 → **선택 상태가 초기화 (한국어로 돌아감)** + 어디에도 저장되지 않음
- **결과**: 선택은 가능하지만 저장이 silent로 실패. 사용자는 "저장됐다" 라고 오해.
- **호출 경로**:
  - [`MyPageScreen.kt:88-116`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/MyPageScreen.kt#L88)
    ```kotlin
    UmmaDialog(
        title = "모국어 선택",
        ...
        onConfirm = {
            showNativeLanguageDialog = false   // ← 다이얼로그 닫기만 함
        },
        confirmText = "완료"
    )
    ```
  - `selectedNativeLanguage` 는 [`MyPageScreen.kt:52`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/MyPageScreen.kt#L52) `remember { mutableStateOf(LangCode.KO) }` 로 **컴포지션 로컬**. 화면 이탈 시 사라짐.
  - `authViewModel.updateNativeLanguage(...)` 같은 메서드 호출 없음.
- **권고**:
  1. 단기: "모국어 설정" 버튼/다이얼로그 자체를 **임시 숨김** (강사에게 다음 스프린트 예정 명시).
  2. 또는 `AuthViewModel`/`DashboardViewModel`에 `updateNativeLang(lang: LangCode)` 추가 + Repo 까지 wire.

---

### 🟠 시나리오 5 (R2-5): OnBoarding 닉네임/언어 다이얼로그 — 뒤로가기로 dismiss 불가 (사용자 갇힘)

- **재현 단계**:
  1. 신규 로그인 → Dashboard 진입 직후 `authViewModel.startInitialSetupFlow()` 자동 호출
  2. 닉네임 입력 다이얼로그 표시
  3. 뒤로가기 또는 X 버튼 누름 → 다이얼로그 닫히지 않음
- **결과**: 신규 사용자가 닉네임을 등록하지 않으면 앱을 사용할 수 없는 lock-in. 일반 UX이지만 **닉네임 형식 에러 발생 후 "X 버튼"으로 종료 불가** 라는 점은 UX 결함.
- **호출 경로**:
  - [`DashboardScreen.kt:210-246`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L210)
    ```kotlin
    InitialSetupDialogStep.NICKNAME -> {
        UmmaDialog(
            title = "닉네임 설정",
            ...
            onCancel = {},                    // ← 빈 람다
            onConfirm = { authViewModel.onNicknameConfirm(nicknameInput) },
            confirmText = "확인",
        )
    }
    ```
  - 동일 패턴: [`DashboardScreen.kt:249-289`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L249) (학습 언어 선택 다이얼로그).
- **권고**: 신규 사용자 강제 등록은 의도된 UX일 수 있으나, 최소한 X 버튼은 숨겨야 함:
  ```kotlin
  // UmmaDialog에 hideCloseButton: Boolean 옵션 추가
  ```
  또는 강사에게 "신규 사용자는 닉네임 등록을 완료해야 종료 가능" 안내.

---

### 🟡 시나리오 6 (R2-6): Chat 화면 이탈 시 명시적 stopChat() 호출 없음 → Gemini Live 세션 계속 활성

- **재현 단계**:
  1. Chat 탭 진입 → 대화 진행 → AI 응답 받는 중
  2. 사용자가 BottomBar "홈" 탭 누름 → ChatScreen이 BackStack에 push되어 있는 상태로 잠시 사라짐
  3. ChatViewModel 은 nav-graph scope 으로 살아있어 **onCleared 즉시 호출되지 않음**
  4. Gemini Live `LiveSession` 은 그대로 active → 백그라운드에서 quota 소비
- **결과**: 시연 시점에는 보이지 않지만 **billing 비용 발생** + 사용자가 다시 Chat에 돌아왔을 때 partial transcript 누적된 상태로 표시 가능.
- **호출 경로**:
  - [`ChatScreen.kt:67-70`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L67) — `LaunchedEffect(Unit) { ... startChat() }` 만 있고 `DisposableEffect` 없음.
  - [`ChatViewModel.kt:313-327`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L313) — `stopChat()` 메서드는 정의되어 있지만 **호출되지 않음**.
  - 04 보고 C2-3 권고와 동일.
- **권고**:
  ```kotlin
  // ChatScreen.kt
  DisposableEffect(Unit) {
      onDispose { viewModel.stopChat() }
  }
  ```

---

### 🟡 시나리오 7 (R2-7): Chat 진입 직후 `startChat()` 실패 → "내 말하기 시작" 버튼이 영구 disabled

- **재현 단계**:
  1. 인터넷 끊긴 상태에서 Chat 탭 진입
  2. `LaunchedEffect(Unit)` 가 `viewModel.startChat()` 호출
  3. [`ChatRepositoryImpl.startSession`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/data/repository/ChatRepositoryImpl.kt#L121-L155) → `liveModel.connect()` 실패 → `_events.emit(AIEvent.Error)` + `Result.failure`
  4. ChatViewModel.startChat onFailure → `sessionState = SessionState.ERROR`
  5. ChatScreen 의 "내 말하기 시작" 버튼은 `enabled = uiState.sessionState == SessionState.READY` → **영구 disabled**
- **결과**: 화면에 "대화를 시작할 수 없습니다." 텍스트만 표시. **재시도 버튼이 노출되지 않음**.
  - `isRecoverableError` 플래그는 `RetryConnectionUseCase` 에서만 세팅됨. `startSession` 실패에는 세팅 안 됨 → "다시 연결" 버튼 노출 조건 불충족.
- **호출 경로**:
  - [`ChatViewModel.kt:99-107`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L99) — `startChat onFailure` 에서 `isRecoverableError` 세팅 안 함.
  - [`ChatScreen.kt:131-138`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L131) — `if (uiState.isRecoverableError)` 분기로 "다시 연결" 버튼 노출.
- **권고**:
  ```kotlin
  // ChatViewModel.startChat onFailure
  _uiState.update {
      it.copy(
          sessionState = SessionState.ERROR,
          aiState = AIState.ERROR,
          isRecoverableError = true,       // ← 추가
          errorMessage = error.message ?: "대화를 시작할 수 없습니다."
      )
  }
  ```
  단 retryConnectionUseCase가 활성 session 없으면 동작 안 하므로 `startChat()` 재호출하도록 별도 분기 필요.

---

### 🟡 시나리오 8 (R2-8): 신규 사용자 Dashboard — Empty 상태가 카드별로 toast 4개 동시 발생 가능

- **재현 단계**:
  1. 신규 사용자 로그인 → Dashboard 진입
  2. `DashSummary` 가 신규 → 모든 카드가 `isEmpty=true`
  3. 사용자가 "학습" 카드 빠르게 연속 탭 → "저장된 카드 없음" Toast + 화면 이동 → 다시 돌아옴 → 다른 카드 탭 → "데이터 부족" Toast
- **결과**: 빠르게 탭하면 Toast가 큐에 쌓여 5초 가까이 화면 하단에 메시지가 떠 있음. 그리고 **빈 카드를 탭하면 placeholder 화면(시나리오 1)으로 이동** → 사용자 혼란.
- **호출 경로**:
  - [`DashboardScreen.kt:434-487`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L434) — StudyCard/FeedbackCard/StatisticsCard 모두 `onClick { if (isXxxEmpty) Toast.makeText(...) ; navigateXxx() }`. 즉 **Toast 표시 후 무조건 navigate**.
- **권고**: Empty 카드 클릭 시 navigate 호출 안 함 (early return):
  ```kotlin
  onClick = {
      if (isStudyEmpty) {
          Toast.makeText(context, "저장된 카드 없음", Toast.LENGTH_SHORT).show()
          return@StudyCard      // 또는 화면 이동 막기
      }
      onNavigateToSrsStudy()
  }
  ```

---

### 🟡 시나리오 9 (R2-9): AppEntry — Firebase 미초기화 시 splash 1초 대기 후 ERROR 표시 (재시도 무한 루프 가능)

- **재현 단계**:
  1. `google-services.json` 누락 또는 잘못된 SHA-1 등록 → Firebase 초기화 실패 (강사 환경에서 빈번)
  2. AppEntryScreen 진입 → `viewModel.checkSession()` → 1초 delay 후 `authRepository.hasValidSession()` → Failure
  3. `sessionError = "네트워크 오류가 발생했습니다 재시도 바랍니다"` 표시 + "다시 시도" 버튼
  4. 강사가 "다시 시도" 누름 → 같은 결과 무한 반복
- **결과**: 실제 네트워크 문제가 아니라 설정 문제임을 강사가 알아채기 어려움. logcat을 봐야만 원인 파악 가능.
- **호출 경로**:
  - [`AuthViewModel.kt:240-289`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/AuthViewModel.kt#L240) — `Log.e("Auth", "checkSession 실패", e)` 만 호출하고 사용자에게는 일반 메시지.
  - [`AppEntryScreen.kt:59-69`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/AppEntryScreen.kt#L59) — Error UI는 단순.
- **권고**: 시연 환경에서 `google-services.json` 정상 동작 사전 검증 + AppEntry Error UI에 "확인 사항: 인터넷 연결 / Google 로그인 가능 여부" 안내 추가.

---

### 🟡 시나리오 10 (R2-10): 화면 회전 시 OnBoarding의 pagerState 페이지가 초기화될 수 있음

- **재현 단계**:
  1. OnBoarding 진입 → 2페이지로 swipe
  2. 디바이스 회전 (세로 → 가로 등)
  3. `rememberPagerState`는 `rememberSaveable`이 아니므로 초기 페이지(0)로 리셋
- **결과**: 사용자가 처음부터 다시 swipe.
- **호출 경로**:
  - [`OnBoardingScreen.kt:60`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/OnBoardingScreen.kt#L60) — `rememberPagerState(pageCount = { onBoardingPages.size })` 만 사용 (saveable 아님).
- **권고**: `rememberSaveable(saver = PagerState.Saver) { ... }` 또는 회전 시 ViewModel에 currentPage 백업.
- **참고**: AndroidManifest에 `android:configChanges` 미설정이므로 회전 시 Activity가 재생성됨. 다행히 ViewModel 은 보존되어 인증/대화 상태는 유지됨.

---

## 화면별 상태 매트릭스

| 화면 | 진입 | 정상 동작 | 빈 상태 | 에러/거부 | 비고 |
|------|------|-----------|---------|-----------|------|
| **AppEntry** (Splash) | ✅ 1초 splash | ✅ 자동 dispatch (Dashboard or OnBoarding) | N/A | 🟡 "다시 시도" 무한 루프 가능 (R2-9) | `delay(1000L)` 명시 |
| **OnBoarding** | ✅ | ✅ pager 3페이지 + Google 로그인 | N/A | ✅ Toast 메시지 | 🟡 회전 시 페이지 리셋 (R2-10) |
| **Google Sign-In Dialog** | ✅ | ✅ CredentialManager | N/A | ✅ `getGoogleIdToken` 예외 catch | webClientId가 strings.xml에 박혀 있는지 확인 필요 |
| **Dashboard** | ✅ | ✅ 카드 4개 + 언어 selector | ✅ Empty 카드 → 회색 + Toast | ✅ DashboardError + Retry | 🟡 빈 카드 클릭해도 placeholder로 navigate (R2-8) |
| **닉네임/언어 다이얼로그** | 신규 사용자 자동 | ✅ 닉네임 2~10자 검증 | N/A | ✅ inline 에러 | 🟠 X 버튼/뒤로가기로 dismiss 불가 (R2-5) |
| **MyPage** | ✅ AppBar 진입 | ✅ 로그아웃 버튼 | N/A | ✅ Toast | 🟠 "모국어 설정" silent fail (R2-4) |
| **Chat (AI 회화)** | ✅ | ✅ 마이크 권한 분기 + Gemini Live 연결 | ✅ AI/User transcript 영역에 "-" 표시 | ✅ "마이크 권한이 필요합니다" 메시지 | 🔴 토픽 다이얼로그 갇힘 (R2-2), 🔴 AudioPlayer 재진입 silent fail (R2-3), 🟡 stopChat 호출 부재 (R2-6), 🟡 startSession 실패 시 retry 없음 (R2-7) |
| **Topic Dialog** | 신규 + interestTopics 비었을 때 | ✅ 5개 선택 검증 | N/A | ✅ inline 에러 | 🔴 cancel 불가 (R2-2) |
| **Correction** | ✅ | ❌ "교정 플레이스 홀더" 한 줄만 | ❌ | ❌ | 🔴 placeholder (R2-1) |
| **SrsStudy** | ✅ | ❌ "복습 플레이스 홀더" 한 줄만 | ❌ | ❌ | 🔴 placeholder (R2-1) |
| **Statistics** | ✅ | ❌ "통계 플레이스 홀더" 한 줄만 | ❌ | ❌ | 🔴 placeholder (R2-1) |
| **SignInScreen** | dead code | N/A | N/A | N/A | UmmaNavHost에서 호출 안 됨, [`SignInScreen.kt`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/SignInScreen.kt) 미사용 |

---

## 발견 상세

### 🔴 Critical Runtime

#### R2-1 Placeholder 3개 화면 (시연 동선 차단)

- **화면**: Correction, SrsStudy, Statistics
- **재현**: BottomBar 탭 클릭
- **결과**: 헤더만 보이고 본문은 한 줄 텍스트
- **근거**:
  - [`StatisticsScreen.kt:23-41`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/statistics/StatisticsScreen.kt#L23)
  - [`SrsStudyScreen.kt:16-36`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/srsstudy/SrsStudyScreen.kt#L16)
  - [`CorrectionScreen.kt:20-38`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/correction/CorrectionScreen.kt#L20)
- **권고**: 시나리오 1 참조.

#### R2-2 Topic Dialog cancel 불가 (사용자 갇힘)

- **화면**: ChatScreen 진입 직후 (신규 사용자)
- **재현**: 시나리오 2
- **결과**: 5개 선택 전에는 X 버튼/뒤로가기로 dismiss 안 됨
- **근거**: [`ChatScreen.kt:163`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L163) `onCancel = {}`
- **권고**: 시나리오 2 참조.

#### R2-3 AudioPlayer @Singleton 재진입 silent fail

- **화면**: Chat 재진입 시 AI 음성 안 들림
- **재현**: 시나리오 3
- **결과**: AudioTrack null → playAudioChunk no-op
- **근거**: [`AudioPlayer.kt:30-81`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioPlayer.kt#L30)
- **권고**: 시나리오 3 참조 (3가지 옵션 중 1) 즉시 reinitialize 패치 권장).

---

### 🟠 High

#### R2-4 MyPage "모국어 설정" silent fail

- **화면**: MyPage
- **재현**: 시나리오 4
- **결과**: 선택 후 "완료" 눌러도 어디에도 저장 안 됨
- **근거**: [`MyPageScreen.kt:93-98`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/MyPageScreen.kt#L93)
- **권고**: 시나리오 4 참조.

#### R2-5 OnBoarding 다이얼로그 강제 lock-in

- **화면**: 신규 사용자 Dashboard 진입 직후
- **재현**: 시나리오 5
- **결과**: 닉네임 입력 안 하면 뒤로가기 불가
- **근거**: [`DashboardScreen.kt:214`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L214), [`DashboardScreen.kt:254`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L254)
- **권고**: X 버튼 숨김 옵션 추가 + 강사 시연용 신규 계정 사전 등록 권장.

---

### 🟡 Medium

#### R2-6 ChatScreen 이탈 시 stopChat 호출 부재

- **화면**: Chat → 다른 탭 이동
- **재현**: 시나리오 6
- **결과**: Gemini Live 세션 active 상태 유지 (quota 소비)
- **근거**: [`ChatScreen.kt:67-70`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L67) — DisposableEffect 부재.
- **권고**: 시나리오 6 참조. 04 보고 C2-3 권고와 동일.

#### R2-7 Chat startSession 실패 시 재시도 버튼 미노출

- **화면**: Chat 진입 직후 네트워크 끊김
- **재현**: 시나리오 7
- **결과**: "내 말하기 시작" 버튼 영구 disabled + 재시도 UI 없음
- **근거**: [`ChatViewModel.kt:99-107`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L99) — `isRecoverableError` 세팅 누락.
- **권고**: 시나리오 7 참조.

#### R2-8 신규 사용자 빈 카드 클릭 시 placeholder로 navigate

- **화면**: Dashboard
- **재현**: 시나리오 8
- **결과**: Toast + placeholder 화면 이동 → 사용자 혼란
- **근거**: [`DashboardScreen.kt:438-446`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L438) — Empty 분기에서 navigate를 막지 않음.
- **권고**: 시나리오 8 참조.

#### R2-9 AppEntry checkSession 실패 시 일반 메시지

- **화면**: AppEntry (앱 시작 직후)
- **재현**: 시나리오 9
- **결과**: "다시 시도" 무한 루프 가능
- **권고**: 시연 전 google-services.json 검증.

#### R2-10 OnBoarding pagerState 회전 시 리셋

- **화면**: OnBoarding
- **재현**: 시나리오 10
- **결과**: 페이지 0으로 돌아감
- **권고**: `rememberSaveable(saver = PagerState.Saver)` 사용.

#### R2-11 BottomBar visibility — Dashboard에서 안 보임 (의도된 UX인지 재확인)

- **화면**: Dashboard
- **재현**: Dashboard 진입
- **결과**: BottomBar 미표시
- **근거**: [`MainActivity.kt:53-58`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/MainActivity.kt#L53)
  ```kotlin
  val showBottomBar = navBackStackEntry?.destination?.hierarchy?.any {
      it.hasRoute<Route.Statistics>()
          || it.hasRoute<Route.Chat>()
          || it.hasRoute<Route.CorrectionList>()
          || it.hasRoute<Route.SrsStudy>()
  } == true
  ```
  Dashboard / MyPage / Auth 그래프에서 BottomBar 미노출.
- **영향**: Dashboard에서 다른 화면으로 이동하려면 카드를 통해서만 가능. 의도된 UX일 수 있으나 일반적인 Android 패턴과 다름. 강사에게 명시 권장.

#### R2-12 ChatScreen 화면 회전 시 UI 상태 유지 + 카드 일관성 보장

- **화면**: Chat
- **재현**: Chat 진입 → 녹음 중 회전
- **결과**: ViewModel 보존 → uiState 유지 ✓ (정상 동작)
- **근거**: ChatViewModel은 `@HiltViewModel` + viewModelScope 이므로 회전 시 보존됨.
- **참고**: AudioRecord 의 recordJob 은 viewModelScope 라 회전과 무관하게 active. 정상.

---

## 시연 직전 빠른 패치 권고

| 우선순위 | Action | 예상 작업 시간 | 영향 |
|---|---|---|---|
| 🔴 P0 | placeholder 3개 화면 강사 안내 (시연 동선 제한) | 5분 | R2-1 |
| 🔴 P0 | `AudioPlayer.startPlaying()` 에 null check + reinitialize 추가 | 10분 | R2-3 |
| 🔴 P0 | Topic Dialog `onCancel` 에 dismiss 로직 | 10분 | R2-2 |
| 🟠 P1 | MyPage "모국어 설정" 버튼 임시 숨김 | 5분 | R2-4 |
| 🟠 P1 | `ChatScreen` 에 `DisposableEffect { onDispose { viewModel.stopChat() } }` | 10분 | R2-6 |
| 🟠 P1 | `ChatViewModel.startChat onFailure` 에 `isRecoverableError = true` | 5분 | R2-7 |
| 🟡 P2 | Empty 카드 클릭 시 navigate 막기 | 10분 | R2-8 |
| 🟡 P2 | OnBoarding pagerState `rememberSaveable` | 5분 | R2-10 |
| 🟡 P2 | 시연용 신규 계정 사전 등록 (R2-5 lock-in 회피) | 5분 | R2-5 |
| 🟡 P2 | google-services.json 정상 동작 사전 검증 | 5분 | R2-9 |

**Total P0+P1 핫픽스 시간**: 약 45분.

---

## 추가 관찰 (시나리오로 격상하지 않은 항목)

### O1. ChatRepositoryImpl `Log.d` 양산

- 04 보고 A4와 동일. release 빌드에서도 사용자 발화 전체가 logcat에 평문 출력.
- 시연 시 직접 영향은 없으나 보안 관점에서 release 전 가드 필수.

### O2. `BackHandler` 가 Dashboard에만 있음

- [`DashboardScreen.kt:105-113`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/dashboard/DashboardScreen.kt#L105) — 더블탭 종료 패턴 ✓.
- Chat / Statistics / SrsStudy / Correction / MyPage 에는 BackHandler 없음 → 뒤로가기로 자동 popBackStack. 정상.
- 단, **Topic Dialog 가 떠 있는 상태에서 뒤로가기** 시 dialog dismiss 시도 → onCancel 빈 람다 → 닫히지 않음 → 한 번 더 누르면 ChatScreen이 pop. UX 부자연스러움.

### O3. AudioRecord 초기화 실패 시 UI fallback

- [`AudioRecorder.kt:57-59`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioRecorder.kt#L57) — `STATE_INITIALIZED` 아니면 `IllegalStateException("마이크 초기화에 실패했습니다.")`.
- [`ChatViewModel.kt:171-190`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L171) — `recordJob` 의 catch block 에서 errorMessage 세팅. UI에서 buildStatusText가 표시 ✓.
- 정상 fallback.

### O4. 다른 앱이 마이크 점유 중일 때 (예: 통화 중 Chat 진입)

- AudioRecord 초기화는 성공할 수 있으나 `audioRecord.read` 가 ERROR_DEAD_OBJECT 등 반환 가능.
- [`AudioRecorder.kt:84`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/data/source/local/AudioRecorder.kt#L84) — `ERROR_DEAD_OBJECT -> throw IllegalStateException("오디오 객체가 소멸되었습니다.")`.
- ChatViewModel catch → errorMessage 세팅 → UI 표시 ✓.
- 정상 fallback.

### O5. Compose recompose 시 LaunchedEffect 재실행

- [`ChatScreen.kt:67-70`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatScreen.kt#L67) — `LaunchedEffect(Unit)` 키가 `Unit` 이라 화면 entering 시 1회만 실행 ✓.
- ChatViewModel.startChat 의 첫 줄 `if (_uiState.value.sessionState == SessionState.LOADING) return@launch` 가 중복 호출 방어 ✓.
- 회전 시 LaunchedEffect 재실행되지만 ChatViewModel은 보존되므로 sessionState=READY이면 startSession이 같은 session id 반환 ✓ (`startSession` 의 동일 lang/instruction 캐싱 분기).

### O6. RECORD_AUDIO 권한 일시적 거부 후 허용 시나리오

- 첫 진입 시 권한 거부 → `microphonePermissionDenied = true` + 에러 메시지.
- 사용자가 설정에서 권한 허용 후 앱 복귀 → `hasRecordAudioPermission()` 이 true 반환 → "내 말하기 시작" 버튼 누르면 정상 동작 ✓.
- 단, `microphonePermissionDenied` 플래그가 별도 리셋되지 않으면 "마이크 권한이 필요합니다." 텍스트가 화면에 남아 있을 수 있음. 실제로는 `startUserTurn(true)` 호출 시 [`ChatViewModel.kt:147-149`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/chat/ChatViewModel.kt#L147) 에서 false로 리셋 ✓.

### O7. `recentFullContext` 100턴 차면 UI 표시

- 04 C2-6 참조 — Room transaction에서 trimOverflowTurns로 자동 정리.
- UI에는 별도 표시 없음 (의도된 UX). 사용자 입장에서는 그냥 대화 계속.

### O8. POST_NOTIFICATIONS 권한 부재 (Android 13+)

- AndroidManifest에 `POST_NOTIFICATIONS` 선언 없음.
- 현재 앱은 알림 채널/노티 송신 코드 없음 → 영향 없음.
- 향후 알림 기능 추가 시 권한 + 채널 동시 구현 필요.

### O9. Splash 1초 delay (UX 의도)

- [`AuthViewModel.kt:249-250`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/AuthViewModel.kt#L249) — `delay(1000L)` "스플래시 화면 0.1초 만에 사라져서 지연 추가".
- 의도된 UX. 정상.

### O10. `SignInScreen.kt` dead code

- [`SignInScreen.kt`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/SignInScreen.kt) 는 UmmaNavHost에서 호출되지 않음 (인용 0건).
- 32줄짜리 placeholder 파일. release APK에 컴파일됨 (영향 미미). dead code 정리 권장.

### O11. 빠른 PTT 연타 시 endUserTurn → startUserTurn 짧은 사이클

- 04 C2-15 참조. ChatRepositoryImpl 의 sessionMutex가 직렬화 보장 ✓.
- UI 입장에서 버튼 throttle 없으나, Gemini Live 짧은 chunk 다수 전송은 정상 동작.

---

## 점검 한계

1. **실 디바이스 빌드/실행 미수행** — 본 점검은 정적 코드 분석 기반. 실제 단말에서 시나리오 1~10을 재현했을 때 framework 단 추가 크래시 가능성 존재.
2. **Firebase AI Logic 응답 형식** — `gemini-3.1-flash-live-preview` 모델의 실 응답 schema 미확인. `LiveServerContent` 필드가 항상 채워진다고 가정.
3. **Compose recomposition edge cases** — Compose Skipping 동작은 컴파일러 버전/플래그에 따라 다를 수 있음. `@Stable` 미부착 데이터 클래스 다수.
4. **Google Sign-In** — `webClientId` 가 `default_web_client_id` 리소스로 참조됨([`OnBoardingScreen.kt:64`](../../../teams-docs/2team/repo/app/src/main/java/com/example/umma/presentation/auth/OnBoardingScreen.kt#L64)). `google-services.json` 가공/Gradle plugin이 자동 생성하는 값. 실 단말에서 SHA-1 mismatch 시 `getCredential` 단계에서 예외 발생 (OnBoarding catch에서 처리 ✓).
5. **AudioPlayer init 블록 실패** — `AudioTrack.Builder().build()` 가 throw할 수 있음 (Android 11+). Singleton init 단계에서 실패하면 Hilt Provider가 throw → 최초 ChatViewModel 생성 시 크래시. 04 C2-3 점검 시점에는 미언급. **시연 시 단말 audio routing 충돌 시 크래시 가능성** — 별도 시나리오 격상 권장 단 빈도 낮음.
6. **DataStore 마이그레이션** — 04 C2-16에 Room 마이그레이션만 언급. DataStore preferences가 비어 있거나 corrupt할 때의 동작 미점검.
7. **04 보고서 SrsStudy placeholder 단독 언급** — Statistics/Correction도 동일 상태임을 본 보고에서 확인.
8. **모국어 설정 silent fail (R2-4)** — 04 보고 미포함. presentation layer 표면 점검에서 발견.

---

## 부록: 강사 시연 권장 동선

**OK 동선** (현재 빌드에서 정상 시연 가능):
1. AppEntry (1초 splash)
2. OnBoarding (3페이지 swipe + Google 로그인)
3. Dashboard (카드 4개 + 언어 selector + 학습 언어 변경)
4. MyPage (로그아웃)
5. Chat 진입 → 토픽 5개 선택 (신규 계정만) → 음성 대화 1턴 → Dashboard 복귀
6. Google 재로그인

**금지 동선** (placeholder + 갇힘 + silent fail):
- Statistics / Correction / SrsStudy 탭 (R2-1)
- Chat 재진입 후 음성 응답 (R2-3)
- MyPage 모국어 변경 (R2-4)
- 토픽 다이얼로그 cancel (R2-2)
- 신규 사용자 닉네임 입력 도중 X 버튼 (R2-5)
