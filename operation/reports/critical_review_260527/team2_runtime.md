# 2팀 Umma 화면 동작 오류 점검 (2026-05-27)

> 베이스: develop @ e1b6ec8, 5/22 이후 +14409/-2014 (52 commits, 243 files)
> 관점: 강사진 실 단말 테스트 시 만나는 크래시/무반응/오작동 (보안/구조 제외)
> 5/22 알려진 이슈는 제외 — 신규/해소/잔존만 기록
> 정적 분석 기반(실 단말 빌드 미수행). 시연 시나리오로 정리.

---

## 점검 한 줄 요약

- **즉시 크래시 위험은 거의 없음** — Hilt `@Inject` / `init {}` 단순, `!!` 0건, `TODO()` 0건. 패키지 리네임(com.example.umma → com.app.umma)은 코드/manifest/build/google-services.json 전부 깔끔.
- 5/22 핵심 무반응 이슈 중 **R2-3 (AudioPlayer 재진입 silent fail) 해소** ⭐⭐⭐, **R2-6 (Chat 이탈 시 stopChat 부재) 해소** ⭐⭐. AI Chat 재진입 시연 가능.
- 5/22 핵심 무반응 이슈 중 **R2-1 (placeholder 3개 화면) 전부 실구현 완료** ⭐⭐⭐ — Statistics/Correction/SrsStudy 모두 살아 있는 화면. 시연 동선 차단 해소.
- **잔존 이슈 2건**: R2-2 (Topic Dialog cancel 빈 람다 유지), R2-4 (MyPage 모국어 설정 silent fail 유지). 5/22 핫픽스 권고 미반영.
- **신규 발견 2건**: (1) SRS 학습 — `Again` 평가 시 같은 카드를 cards 리스트 끝에 append하는데, 종료 조건 계산이 `cards.size`에 의존해 같은 카드를 무한히 Again 누르면 cards가 무한 성장(메모리 누수, 시연 시 영향 미미). (2) SRS 학습 — `onConfirmRating` 의 `getCurrentUserUid()` 가 마이그레이션 후 null이면 silent 종료(저장 안 됨, 화면 표시 없음).

---

## 🔴 즉시 크래시 위험 (신규)

### 없음

5/22 이후 신규 작성된 ViewModel(SrsStudyViewModel / StatisticsViewModel / CorrectionViewModel 확장 / TextToSpeechController) 모두 `init {}` 가 단순하거나 try/catch + `Result` 래핑으로 보호되어 있어 즉시 크래시 경로는 발견되지 않았다.

`StatisticsMetricLineChartDialog` 의 Vico 3.0.3 API 사용은 신뢰도 한계가 있다 — 특히 `CartesianMarkerController.rememberToggleOnTap()` 호출은 Vico 공식 문서에서 확인되지 않은 시그니처라 컴파일 실패 가능성 (점검 한계 §1 참조).

---

## 🟠 무반응 / dead-end (신규)

### [N1] SRS — 비로그인 상태에서 카드 평가 → silent 무반응

- **시나리오**: 사용자가 SRS 학습 화면에서 카드를 뒤집고 평가 버튼(Again/Hard/Good/Easy)을 누른 다음 "다음 카드" FAB를 누르면
- **현상**: 호출은 정상 진행되지만 [`onConfirmRating`](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/srsstudy/SrsStudyViewModel.kt#L165) 의 `getCurrentUserUid() ?: return` 분기에 걸려 아무 일도 일어나지 않음. Loading/Error 표시 없음, 다음 카드로도 넘어가지 않음.
- **위치**: [SrsStudyViewModel.kt:165](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/srsstudy/SrsStudyViewModel.kt#L165)
- **근거**:
  ```kotlin
  val card = _uiState.value.currentCard ?: return
  val userId = getCurrentUserUid.getCurrentUserUid() ?: return  // ← silent return
  viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, hasSaveError = false) }
      ...
  ```
- **재현 조건**: `onEnter()` 의 `startReviewSession()` 은 `getCurrentUserUid()` 를 호출하지 않으므로 (언어 정보만 가져옴), 카드는 정상 로드된다. 그런데 카드 평가 단계에서만 uid 필요. 강사가 빌드 직후 미로그인 상태로 진입하거나, Firebase Auth 토큰이 만료된 경우 발현.
- **권고**:
  ```kotlin
  val userId = getCurrentUserUid.getCurrentUserUid() ?: run {
      _uiState.update { it.copy(hasSaveError = true) }
      return
  }
  ```
  또는 `onEnter()` 단계에서도 uid 체크해서 인증 안 되면 hasInitError = true 로 보내기.

### [N2] SRS — `Again` 평가 누적 시 cards 리스트 무한 성장

- **시나리오**: 사용자가 같은 카드를 반복해서 `Again` 으로 평가 → 다음 카드 FAB 클릭 → 같은 카드 재출현 → Again → ...
- **현상**: cards 리스트가 무한히 자라며 currentCardIndex도 함께 증가. 화면 표시(`{현재}/{총}`)가 `1/1` → `2/2` → `3/3` 식으로 늘어남. 메모리는 list 객체 참조뿐이라 크리티컬 누수는 아님.
- **위치**: [SrsStudyViewModel.kt:178-184](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/srsstudy/SrsStudyViewModel.kt#L178)
- **근거**:
  ```kotlin
  val updatedCards = if (rating == ReviewRating.AGAIN) {
      state.cards + card           // ← 매번 append (중복 가능)
  } else {
      state.cards
  }
  val nextIndex = state.currentCardIndex + 1
  val isDone = nextIndex >= updatedCards.size  // ← Again이면 size도 늘어남
  ```
- **권고**: 카운터 표시("3/3")가 매번 늘어나는 게 의도가 아니면, Again 시 cards 끝에 append 대신 "다음 N장 이후 재출현" (예: index+3) 같은 단순 정책 추가. SM-2 알고리즘 정합 검토 필요. 시연 시점에는 사용자가 Again을 2~3회 이상 누르지 않으면 영향 없음.

---

### 잔존 무반응 (5/22에서 이월)

### [R2-2 잔존] Chat — 토픽 다이얼로그 cancel 불가 (사용자 갇힘)

- **상태**: ❌ 미해소. 5/22 핫픽스 권고 미반영.
- **위치**: [ChatScreen.kt:367](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatScreen.kt#L367)
- **근거**:
  ```kotlin
  if (uiState.showTopicDialog) {
      UmmaDialog(
          title = "Pick 5 Topics",
          ...
          onCancel = {},                       // ← 여전히 빈 람다
          onConfirm = { viewModel.saveInterestTopics() },
          confirmText = "Done"
      )
  ```
- **신규 관찰**: 5/22 보다 더 나쁜 점은 다이얼로그 문구가 한국어("관심 주제 선택 5개") → 영어("Pick 5 Topics")로 변경되었는데 에러 메시지(`viewModel.saveInterestTopics()` 가 세팅하는 "주제를 정확히 5개 선택해 주세요.") 는 여전히 한국어. 시연 시 강사가 신규 계정으로 Chat 진입 시 일관성 깨진 모달 노출.
- **권고**: 5/22 권고 동일. `onCancel = { viewModel.dismissTopicDialog() }` + ChatViewModel에 dismiss 메서드 추가.

### [R2-4 잔존] MyPage — 모국어 설정 silent fail

- **상태**: ❌ 미해소. `onCancel = { showNativeLanguageDialog = false }` (cancel은 작동) / `onConfirm = { showNativeLanguageDialog = false }` (저장 호출 없음).
- **위치**: [MyPageScreen.kt:95-97](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/dashboard/MyPageScreen.kt#L95)
- **근거**:
  ```kotlin
  onConfirm = {
      showNativeLanguageDialog = false        // ← 다이얼로그만 닫고 저장 안 함
  },
  confirmText = "완료"
  ```
  `selectedNativeLanguage` 는 [MyPageScreen.kt:52](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/dashboard/MyPageScreen.kt#L52) 의 `remember { mutableStateOf(LangCode.KO) }` 로 컴포지션 로컬 — 화면 이탈 시 사라짐.
- **권고**: 5/22 권고 동일. 단기 핫픽스로 "모국어 설정" 버튼 자체를 임시 숨김.

---

## 🟡 잘못된 데이터 표시 / UX 결함 (신규)

### [N3] 통계 — 차트 dialog가 한 번 열리고 즉시 카드 재탭하면 versioning은 보호되지만 dismiss 직전 결과는 표시될 수 있음

- **시나리오**: 사용자가 통계 카드 A를 누름 → Loading → 카드 B를 빠르게 누름 → A의 응답이 잠깐 도착 → 즉시 B의 응답으로 덮임
- **현상**: A의 결과가 한 frame 노출 가능. 사용자가 인지하기 어려운 깜빡임.
- **위치**: [StatisticsViewModel.kt:322-323](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/statistics/StatisticsViewModel.kt#L322)
- **근거**: `chartRequestVersion` 으로 stale response 가드는 있으나, `onMetricClick(B)` 직후 dialog는 즉시 `Loading(B)` 로 갱신되고 그 사이에 A의 onSuccess가 도착하면 분기 마지막 `requestVersion != chartRequestVersion` 체크가 막아준다. 실제로는 잘 보호되고 있음 — false alarm 가능성. 실 단말 검증 권장.

### [N4] 통계 — `summary.startValue` 텍스트가 "처음 N" 형식이지만 metricType별 단위 표시 부재

- **시나리오**: 사용자가 차트 카드 클릭 → dialog 상단 칩에 "처음 73 / 지금 80 / 총성장 +7 / 히스토리 5개" 표시
- **현상**: "Vocabulary 73 단어" 와 "Fluency 73점" 의 단위 차이가 칩에 표시되지 않음. metric별 의미가 안 보임.
- **위치**: [StatisticsMetricLineChartDialog.kt:387-396](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/statistics/component/StatisticsMetricLineChartDialog.kt#L387)
- **근거**:
  ```kotlin
  texts = listOf(
      "처음 ${summary.startValue}",
      "지금 ${summary.currentValue}",
      ...
  )
  ```
  startValue/currentValue는 String이라 unit 포함 여부는 변환 함수에 따라 다름. 점검 한계 §4 참조.
- **권고**: `summary.startValue` 가 unit 포함하는지 `StatisticsChartSummary` 정의 확인 필요. 시연 시 통계 데이터 부족(신규 사용자)으로 Empty 분기로 빠질 가능성이 더 큼.

### [N5] SRS — 학습 언어가 LangCode.ES (스페인어) 일 때 TTS 발음 재생 무반응

- **시나리오**: 사용자가 학습 언어를 "스페인어"로 설정 → SRS 카드 뒷면 → 스피커 버튼 누름
- **현상**: 발음 안 들림. `isSpeaking = false` 상태 유지.
- **위치**: [TextToSpeechController.kt:38-43](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/core/tts/TextToSpeechController.kt#L38)
- **근거**:
  ```kotlin
  val locale = when (langCode) {
      LangCode.KO -> Locale.KOREAN
      LangCode.EN -> Locale.ENGLISH
      LangCode.JA -> Locale.JAPANESE
      else -> return false       // ← ES, FR 등은 모두 false
  }
  ```
  Dashboard 의 학습 언어 선택은 4종(KO/EN/JA/ES) 모두 노출되는데 TTS는 3종만 지원. 사용자 ES 선택 + 카드 평가 → 발음 안 들림.
- **권고**: ES, ZH 등 사용 예정 언어를 when 분기에 추가하거나, 다이얼로그에서 미지원 언어 비활성화. 시연 시 영어/일본어/한국어 카드만 보여주면 영향 없음.

### [N6] 챗 — startSession 실패 시 재시도 버튼 미노출 (R2-7 잔존)

- **상태**: ❌ 미해소. ChatViewModel.startChat onFailure 에 `isRecoverableError` 세팅 없음.
- **위치**: [ChatViewModel.kt:111-118](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatViewModel.kt#L111)
- **근거**:
  ```kotlin
  .onFailure { error ->
      _uiState.update {
          it.copy(
              sessionState = SessionState.ERROR,
              aiState = AIState.ERROR,
              errorMessage = error.message ?: "대화를 시작할 수 없습니다."
              // isRecoverableError = true 세팅 누락
          )
      }
  }
  ```
- **권고**: 5/22 권고 동일.

---

## 패키지 리네임 후속 점검

- ✅ `app/src/main/java/com/example/umma/` 디렉토리 부재 (전 파일 이동 완료)
- ✅ AndroidManifest `android:name=".UmmaApplication"` + `.MainActivity` — 상대 경로라 namespace 자동 확장 ([AndroidManifest.xml:10,20](../../../teams-docs/2team/repo/app/src/main/AndroidManifest.xml#L10))
- ✅ build.gradle.kts `namespace = "com.app.umma"` / `applicationId = "com.app.umma"` ([build.gradle.kts:15-19](../../../teams-docs/2team/repo/app/build.gradle.kts#L15))
- ✅ google-services.json — `com.app.umma` 클라이언트 항목 9건 존재 (`com.example.umma` 항목도 leftover 로 남아 있으나 multi-client 설정이라 영향 없음)
- ✅ FileProvider authority — `<provider>` 선언 자체가 없음 (코드에서 FileProvider 미사용)
- ✅ BuildConfig 참조 — `BuildConfig.API_KEY` 만 사용. 패키지 자동 정합.
- ✅ Deep link scheme — `<intent-filter>` 에 LAUNCHER 만 있음, custom scheme 없음
- ✅ 모든 테스트 파일도 `com.app.umma` 로 통일됨 (test/ 디렉토리 311 occurrences 전부 com.app.umma)
- ✅ docs/ 폴더는 일부 `com.example.umma` 참조 있으나 코드 외부 문서라 빌드 영향 없음

**핫픽스 470e07d / 0aba8ca / a40579f 의 정리 작업이 완전히 수습된 것으로 보임.**

---

## 5/22 이후 해소 확인된 이슈 ✅

### [R2-3] AudioPlayer @Singleton 재진입 silent fail — **해소** ⭐⭐⭐

- 5/22: ChatViewModel.onCleared 가 `audioPlayer.release()` 호출 → AudioTrack null → Chat 재진입 시 silent.
- 현재: [ChatViewModel.kt:755](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatViewModel.kt#L755) `audioPlayer.stopPlaying()` 만 호출. AudioTrack은 STATE_INITIALIZED 유지 → 재진입 시 `startPlaying()` 의 `playState != PLAYING` 분기에서 `audioTrack?.play()` 가 정상 재활성화. AudioPlayer 자체도 [`startPlaying()`](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/data/source/local/AudioPlayer.kt#L74) 에 `playState` 가드 추가됨.
- 추가 보강: [`startPlaybackWorkerIfNeeded`](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/data/source/local/AudioPlayer.kt#L114) 가 playbackJob 활성 여부를 체크해 중복 launch 방지.
- ⚠️ 단, `audioPlayer.release()` 는 코드 전체에서 호출되는 곳이 없음 — 앱 프로세스 종료 시까지 AudioTrack 점유. 시연 시 영향 없으나 백그라운드 정책 검토 권장.

### [R2-6] ChatScreen 이탈 시 stopChat 호출 부재 — **해소** ⭐⭐

- 5/22: DisposableEffect 부재로 Chat → Dashboard 이동 시 Gemini Live 세션 유지.
- 현재: [ChatScreen.kt:121-125](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatScreen.kt#L121) 에 추가됨.
  ```kotlin
  DisposableEffect(Unit) {
      onDispose {
          viewModel.stopChat()
      }
  }
  ```
- `stopChat` 은 eventJob/recordJob cancel + audioPlayer.stopPlaying + stopSessionUseCase + state 리셋 모두 처리.

### [R2-1] Placeholder 3개 화면 (시연 동선 차단) — **해소** ⭐⭐⭐

- 5/22: SrsStudy/Statistics/Correction 모두 "XX 플레이스 홀더" 한 줄.
- 현재 SrsStudy: 471줄. 카드 앞/뒷면 전환, 4종 평가 버튼, TTS 발음 재생, Loading/Error/Empty/Done 분기 완전.
- 현재 Correction: 559줄 + 별도 component/. Empty/EmptyResult/Error/Loading/Ready/Generating/Content/Done/Retry 9단계 phase 분기, Dashboard 복귀 1회성 이벤트, 같은 saveRequest 재진입.
- 현재 Statistics: 213줄 + 차트 dialog 619줄. Loading/Error/Overview 분기 + 차트 dialog Loading/Empty/Error/Ready 분기.

### Chat — final turn 후 correctionAvailable 신호 전파 — **신규 구현 확인** ⭐⭐

- [ChatViewModel.kt:543-578](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatViewModel.kt#L543) `handoverCorrectionAvailableSignal` 가 USER role 의 final turn 저장 후 `applyCorrectionSignalUpdateUseCase` 호출. `sessionMemoryKey` 빌드는 `"${uid}_${lang.code}"` 단순 string concat (충돌 위험 낮음). uid 없을 때 silent skip + Log.w.

### 사용자 닉네임 조회 — **신규 구현 확인**

- [ChatViewModel.kt:766-779](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatViewModel.kt#L766) `checkInterestTopics()` 가 `getUserNicknameUseCase(uid)` 호출. 결과는 ChatUiState.userNickname 에 보관, 자막 영역의 사용자 닉네임 표시에 사용.

### 기본 학습 언어 폴백 — **신규 구현 확인**

- [DashboardScreen.kt:165](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/dashboard/DashboardScreen.kt#L165) `val selected = uiState.selectedLearningLanguage ?: LangCode.EN` — 영어 폴백.

---

## 시연 동선 권고 (강사)

**OK 동선**:
1. AppEntry (1초 splash) → OnBoarding → Google 로그인 → Dashboard
2. Dashboard 카드 4종 + 학습 언어 selector
3. 마이페이지 진입 → 로그아웃 (단, "모국어 설정" 버튼은 누르지 말 것 R2-4)
4. Chat 진입 (기존 계정만 — 토픽 다이얼로그 회피 R2-2) → 1턴 대화 → Dashboard 복귀 → Chat 재진입 → 음성 정상 재생 확인 (R2-3 해소)
5. SRS 학습 진입 (영어/일본어 카드 권장 — 스페인어는 TTS 무반응 N5)
6. Correction 진입 → Empty 또는 Content → 카드 선택 → 저장 → Dashboard 자동 복귀
7. Statistics 진입 → 카드 클릭 → 차트 dialog (데이터 2건 이상일 때만 Ready)

**금지 동선**:
- 신규 계정으로 Chat 진입 후 토픽 5개 선택 도중 X 버튼 (R2-2)
- MyPage 모국어 변경 (R2-4)
- 스페인어 학습 언어로 SRS 발음 버튼 (N5)
- 비로그인 상태에서 SRS 카드 평가 (N1)

---

## 점검 한계

1. **Vico 3.0.3 API 검증 불가** — `StatisticsMetricLineChartDialog` 의 `CartesianMarkerController.rememberToggleOnTap()` (363줄), `LineCartesianLayer.Companion.rememberLine(...)` (343줄), `Insets(horizontal=, vertical=)` (250줄) 는 Vico 공식 문서에서 확인 불가. 실 빌드에서 컴파일 에러 가능성 있음. 강사 점검 시 `./gradlew :app:compileDevDebugKotlin` 한 번 실행 권장.
2. **실 단말 빌드 미수행** — 본 점검은 정적 코드 분석 기반. Compose recomposition / Hilt graph 초기화 실패 등은 빌드 직전까지 발견 불가.
3. **Firebase AI Logic 응답 형식** — `gemini-3.1-flash-live-preview` 모델의 실 응답 schema 미확인. Correction 파이프라인의 `GenerateSuggestionsUseCase` 가 항상 정상 JSON을 반환한다고 가정.
4. **StatisticsChartSummary 필드 형식** — `summary.startValue/currentValue/changeValue` 가 unit (단어/점/초) 포함하는지 정의 미확인. N4 의 unit 부재는 false alarm 일 수 있음.
5. **TextToSpeechController 인스턴스 격리** — TtsModule 이 `@Provides` 만 사용 (스코프 없음) 해서 호출마다 새 인스턴스 가정. 다른 ViewModel 이 같은 TTS를 받지 않으므로 shutdown race 없음 — 단 Hilt 내부 동작에 의존.
6. **DataStore preferences corrupt 시 동작** — 04 보고와 동일하게 미점검.
7. **AudioPlayer.release() 호출 부재** — 앱 프로세스 종료 시 AudioTrack 점유 해제 미보장. 실 단말 백그라운드 처리 정책 검토 필요.
8. **SRS Again 무한 누적 (N2)** — 실 단말에서 사용자가 Again만 10회 이상 누르는 시나리오는 비현실적이라 시연 영향 없을 듯하나, 디자인 검토 권장.
