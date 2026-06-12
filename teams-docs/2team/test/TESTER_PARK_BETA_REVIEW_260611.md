# 박종범 테스터 — 2팀 Umma(움마) 베타 점검 (2026-06-11)

> **대상**: 박종범 본인 (Note 10+ AOS12 / Z Fold 4 AOS16 병행)
> **소스 최신화**: `develop` @ `437ca2f` (2026-06-11 15:54, #396 feature/dashboard 머지본) — 2팀은 release 브랜치·태그 없음 → `develop`이 베타 라인
> **이전 문서**: [TESTER_PARK_BETA_REVIEW_260608.md](./TESTER_PARK_BETA_REVIEW_260608.md) (@ `c785e25`) — **그 문서의 채팅 흐름(C-01~C-06)·실전 영어 대본(S-01~S-12)은 여전히 유효.** 본 문서는 그 후 11커밋(13파일)의 **변화분과 신규 결함만** 추린 보강본 + 라인 정정.
> **점검 방식**: 코드 정적분석(read-only). 코드 라인 근거 명시. 디바이스 블랙박스 관찰 중심.

> ⚠️ **6/08 문서를 폐기하지 말 것.** 채팅 어색함의 구조 원인(C-01 6턴 절단, C-02 유령답변)과 영어 대화 대본(S-01~S-12)은 코드가 그대로라 **그 문서가 메인**이다. 본 문서는 **델타**다.

---

## 0. 1줄 결론 (6/08 대비 변화)

> **채팅 코어(OpenAI Realtime + Gemini 교정)는 구조 그대로 — C-01·C-02 여전히 발현.** 6/08 이후 새로 붙은 건 **① 중복 로그인 차단(단일 세션) ② 첫 진입 알림 권한 ③ 대시보드 온보딩 ‘펄스’ 가이드 ④ SRS ‘다시’ 동작 변경**. 이 중 디바이스로 가장 새로 보이는 건 **다른 기기에서 로그인하면 현재 기기가 강제 로그아웃되는 흐름**과 **펄스 가이드의 단계 꼬임**(팀이 #383/#394로 여러 번 고친 영역). 시크릿·App Check·Firestore Rules는 **여전히 양호**(1팀/3팀 대비 우수).

---

## 1. 🔧 6/08 문서 라인 정정 (코드 이동 — 결함은 동일)

리팩토링(#379~#381 Clean Architecture, Summary 계산 이전)으로 줄번호가 밀렸다. **결함 자체는 6/08 그대로**, 참조만 갱신:

| 6/08 ID | 6/08 참조 | **6/11 실제 위치** | 상태 |
|---|---|---|---|
| C-01 컨텍스트 6턴 절단 | BuildPromptUseCase.kt:474/517 | [BuildPromptUseCase.kt:474](../repo/app/src/main/java/com/app/umma/domain/usecase/chat/BuildPromptUseCase.kt#L474) `takeLast(MAX_CONTEXT_TURN_COUNT)` + [:517](../repo/app/src/main/java/com/app/umma/domain/usecase/chat/BuildPromptUseCase.kt#L517) `= 6` | **불변. 여전히 6턴** |
| C-02 빈 응답 자막·저장 스킵 | ChatViewModel.kt:839-841 | [ChatViewModel.kt:907-910](../repo/app/src/main/java/com/app/umma/presentation/chat/ChatViewModel.kt#L907) `handleFinalTranscription { if (event.text.isBlank()) return }` | **불변. 빈 final = 자막·저장 둘 다 스킵(=유령답변)** |
| D-01 TTS 무음 | TextToSpeechController.kt:36-45 | [TextToSpeechController.kt:40-41](../repo/app/src/main/java/com/app/umma/core/tts/TextToSpeechController.kt#L40) `LANG_MISSING_DATA/LANG_NOT_SUPPORTED -> false` | **불변. 비영어 발음 조용히 무음** |
| D-02/D-03 교정 빈설명/unknown id | CorrectionAiResponseMapper.kt | [CorrectionAiResponseMapper.kt:64](../repo/app/src/main/java/com/app/umma/data/repository/correction/CorrectionAiResponseMapper.kt#L64) `BlankExplanationException` 전용 예외 신설 | **개선됨**(COR-FIX-008-C). 빈설명 1회 재시도→drop 계약은 유지 |

> ⚠️ **C-02는 "고쳐진 것이 아님"에 주의.** 줄이 907-910으로 옮겨졌을 뿐 `if (blank) return`으로 **자막·저장을 건너뛰는 동작은 그대로**다. 6/08 C-02 재현 시나리오 그대로 쓰면 된다.

---

## 2. 🔴 신규 결함 — 6/08 이후 추가된 코드

### 🟠 N-01. 다른 기기 로그인 시 현재 기기 강제 로그아웃 — 흐름 자체가 신규
- **근거**: 단일 활성 세션 정책(#366). 세션 claim은 Cloud Function `claimLoginSession` 경유 — [SessionRepositoryImpl.kt:41-62](../repo/app/src/main/java/com/app/umma/data/repository/SessionRepositoryImpl.kt#L41). 활성 세션 검증 — [:64-84](../repo/app/src/main/java/com/app/umma/data/repository/SessionRepositoryImpl.kt#L64) `isCurrentSessionActive()`가 Firestore `users/{uid}.activeSession.sessionId`와 로컬 세션 ID 비교. 불일치 시 FCM/`IsSessionStillActiveUseCase`로 강제 로그아웃 + 안내 플래그([:86](../repo/app/src/main/java/com/app/umma/data/repository/SessionRepositoryImpl.kt#L86) `markPendingForceLogoutNotice`).
- **동작(재현)**: 같은 구글 계정으로 **Note10+와 Z Fold4 동시 로그인** → 나중 로그인 후 먼저 켜둔 기기를 다시 본다(앱 전환/백그라운드 복귀).
- **기대(정상)**: 먼저 기기는 **"다른 기기에서 로그인됨" 안내 후 로그아웃**.
- **깨지면(이상)**: ① 강제 로그아웃이 **안 일어남**(양쪽 동시 사용), ② 또는 안내 없이 **갑자기 로그인 화면으로 튕김**, ③ 대화/녹음 도중 끊기면서 진행 중 세션 유실. 발생 타이밍·화면 기록.

### 🟡 N-02. 레거시 계정은 중복 로그인 차단이 조용히 무력
- **근거**: [SessionRepositoryImpl.kt:78-79](../repo/app/src/main/java/com/app/umma/data/repository/SessionRepositoryImpl.kt#L78) — `activeSession` 필드가 없는 계정은 "정책 도입 이전 사용자"로 보고 `activeSessionId == null || …`로 **항상 유효 처리**. 즉 정책 도입 전 만든 계정은 **단일 세션 강제가 적용 안 됨**.
- **동작(재현)**: (가능하면) 6/08 이전부터 쓰던 계정으로 두 기기 동시 로그인.
- **깨지면(이상)**: 신규 계정과 달리 **두 기기가 모두 로그인 유지**됨(차단 안 됨). 신·구 계정 동작 차이를 기록.
- **참고**: 캐스팅은 안전(`as? String`)이라 크래시는 아님 — **동작 차이**만 관찰 대상.

### 🟠 N-03. 대시보드 온보딩 ‘펄스’ 가이드 단계 꼬임 (팀이 반복 수정한 영역)
- **근거**: 신규 사용자 4단계 가이드(대화→교정→학습→완료). 팀이 단계 전이를 #383/#394로 여러 번 고침 — "교정 저장 시 STUDY 전이 누락 복구"(446527b), "펄스 stage 전이 복구 — 저장 후 STUDY 누락·미산출 후 리셋 누락"(c44a13c). 교정 결과가 **0개(NO_CORRECTION/SAFETY_BLOCKED)**면 단계를 CONVERSATION으로 되돌려야 하는데, 그 리셋 호출이 UI 해제 타이밍과 어긋나면 **CORRECTION 단계에 고착** 가능.
- **동작(재현)**: **신규 계정**으로 첫 대화 → 교정 시도 → (짧고 모호하게 말해서) **교정 0개** 유도 → 교정 화면을 빠르게 닫기 → 대시보드 복귀.
- **기대(정상)**: 가이드 펄스(점)가 자연스럽게 다음 단계로/대화로 복귀.
- **깨지면(이상)**: 교정 버튼에 **빨간 점이 사라지지 않고**, 눌러도 같은 화면 반복하거나 학습 단계로 못 넘어감. 신규 가입 직후 1회성이라 **계정 새로 만들어 재현**.

### 🟡 N-04. 첫 진입 알림 권한 — AOS13+ 타이밍
- **근거**: 첫 진입 알림 권한 요청 + 신규 가입 기본 알림 설정 신설(f326489). 권한 상태는 [MainActivity.kt](../repo/app/src/main/java/com/app/umma/MainActivity.kt)에서 `hasNotificationPermission()`로 확인 후 디바이스 동기화. 권한 다이얼로그가 **로그인 이후** 뜨는 흐름이라, 최초 동기화가 `permissionGranted=false`로 한 번 돌 수 있음.
- **동작(재현)**: AOS13+ 기기(Z Fold4)에서 **새 구글 계정 신규 가입** → 알림 권한 허용/거부 각각.
- **깨지면(이상)**: 신규 가입 직후 알림이 **등록 안 되거나**, 거부 후 허용해도 **다음 로그인까지 반영 안 됨**. 알림이 와야 할 상황(세션 정책 등)에서 누락되는지.

### 🟢 N-05. SRS ‘다시(Again)’ 동작 변경 — 6/08 D-05 무한루프 완화
- **근거**: ‘다시’ 재정의(#384/SRS-FIX-006). [SrsStudyViewModel.kt:204-217](../repo/app/src/main/java/com/app/umma/presentation/srsstudy/SrsStudyViewModel.kt#L204) — "Again을 눌러도 현재 세션에는 재노출하지 않는다" + `currentCardIndex+1`로 선형 진행, `isDone = nextIndex >= cards.size`로 종료. 다시 누른 카드는 **다음 세션에서 due로 재등장**(SM-2).
- **동작(재현)**: 오늘 카드 전부 학습 완료 → "다시" 반복 탭 / 학습 중 "다시" 연타.
- **기대(정상)**: 완료 시 정상 종료(빈 덱 무한루프 아님).
- **깨지면(이상)**: 6/08 D-05의 "다시 무한 로딩"이 **재현되면** 변경이 불완전 — 시각·카드 수 기록. (현 코드 분석상 완화됨)

---

## 3. 🧭 변화 영역 집중 시나리오 (6/08 3장에 추가)

6/08 문서 3장(채팅 경계)·2-B장(영어 대본)을 그대로 수행하되, 아래 신규 항목을 더한다:

1. **두 기기 동시 로그인(N-01)** — Note10+ ↔ Z Fold4 교차 로그인 → 강제 로그아웃 안내·동작. 대화 중 끊기는 케이스도. **신규 최우선.**
2. **레거시 vs 신규 계정 차이(N-02)** — 구계정은 양쪽 유지되는지.
3. **신규 가입 펄스 가이드(N-03)** — 교정 0개 유도 후 단계 고착되는지.
4. **신규 가입 알림 권한(N-04)** — 허용/거부 후 알림 등록 타이밍.
5. **SRS ‘다시’ 종료(N-05)** — 완료 후 무한루프 재현 시도.
6. **(6/08 유지) C-01 6턴 기억·C-02 유령답변·S-01~S-12 영어 대본** — 코드 불변이라 그대로 수행. 이상 응답은 **화면 AI 신고 UI**로 즉시 신고.

---

## 4. ✅ 정상 확인(회귀만) — 6/08 대비 유지

| 항목 | 상태 |
|---|---|
| 시크릿 위생 | `google-services.json`·`local.properties` 언트랙 유지 ✅ |
| Firestore Rules | [firestore.rules](../repo/firestore.rules) `isOwner(uid)`/`isAccountWritable` per-uid 스코프 + 서버 전용 컬렉션(`chat_usage_*`) write 제외 + 계정삭제 락 → **테스트 모드 아님** ✅ |
| App Check 분리 | 디버그 프로바이더가 **`app/src/debug/`** 소스셋에 격리 — [AppCheckProviderInstaller.kt:19](../repo/app/src/debug/java/com/app/umma/AppCheckProviderInstaller.kt#L19) → release엔 미포함 ✅ (3팀 B-01 같은 위험 없음) |
| 세로 고정 | [AndroidManifest.xml:27](../repo/app/src/main/AndroidManifest.xml#L27) `portrait` ✅ |
| 오디오 입출력 | AudioRecord(16kHz)/AudioTrack 에러코드 방어 유지 — 단 [AudioPlayer.kt](../repo/app/src/main/java/com/app/umma/data/source/local/AudioPlayer.kt) **AudioFocusRequest 여전히 부재** → 폴드 시 오디오 끊김/에코 미검증(6/08 동일) |

---

## 5. ⚠️ 콘솔 확인 필요 (코드로 불가)

6/08과 동일하게 유지 — 아래만 재확인:
1. **`claimLoginSession`·`IsSessionStillActive` Cloud Function 생존** — 단일 세션 정책의 서버측 함수 소스는 repo에 없음. 함수가 죽으면 중복 로그인 차단·강제 로그아웃이 **무동작/오작동**. 두 기기 테스트가 이상하면 이 함수를 의심.
2. **Firestore Rules 모드·App Check enforcement** — 6/08 그대로(코드상 양호하나 콘솔 실모드 확인).
3. **OpenAI Realtime 토큰 발급 Cloud Function** — 채팅 자체가 안 붙으면 토큰 함수 의심.

---

## 6. 본인 단말 우선순위

| 순위 | 항목 | 이유 |
|---|---|---|
| 🔥 1 | **(6/08) S-01~S-12 영어 대본 + C-01/C-02** | 팀 특별요청 직격. 코드 불변이라 6/08 문서가 메인 |
| 🔥 2 | **N-01 두 기기 강제 로그아웃** | 6/08 이후 가장 큰 신규 표면. 대화 중 끊김 포함 |
| 🔥 3 | **N-03 신규 가입 펄스 가이드 꼬임** | 팀이 반복 수정한 영역 — 교정 0개 시 단계 고착 |
| ⭐ 4 | **N-02 레거시 계정 차이 / N-04 알림 권한 타이밍** | 조용한 동작 차이 |
| ⭐ 5 | **N-05 SRS ‘다시’ 종료 / 폴드 오디오(AudioFocus 부재)** | 완화 확인 + 본인 폼팩터 |

> **보고 핵심 메시지**: **"채팅 코어는 6/08 그대로 — C-01 6턴·C-02 유령답변 여전, 영어 대본도 그대로 유효. 새로 볼 건 ① 두 기기 로그인 시 강제 로그아웃(N-01) ② 신규 가입 펄스 가이드 단계 꼬임(N-03) ③ 레거시 계정은 중복차단 무력(N-02). 시크릿·App Check·Firestore Rules는 여전히 1팀/3팀보다 양호. C-02는 줄만 907로 옮겼을 뿐 안 고쳐졌다."**

---

## 변경 이력
| 일자 | 변경 |
|---|---|
| 2026-06-11 | develop @ 437ca2f 최신화. 6/08 문서 대비 **델타 보강본**으로 작성(채팅 C-01~C-06·영어 대본 S-01~S-12는 유효하므로 유지). 라인 정정(C-01/C-02/D-01/D-02 코드 이동) + 신규 결함 — 두 기기 강제 로그아웃(N-01)·레거시 계정 중복차단 무력(N-02)·온보딩 펄스 단계 꼬임(N-03)·알림 권한 타이밍(N-04)·SRS ‘다시’ 변경(N-05) 코드 근거 명시. **정정**: SessionRepositoryImpl 캐스팅은 안전(`as? String`)·크래시 아님 / C-02는 "고쳐진 것 아님"(줄만 이동) / App Check는 debug 소스셋 격리로 양호 확인 |
