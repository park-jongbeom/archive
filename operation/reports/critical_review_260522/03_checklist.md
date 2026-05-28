# Step 3 — 팀별 최종 크리티컬 체크리스트 (2026-05-22)

`02_references.md`의 검색 근거 + `01_domains.md`의 도메인 분석 정합 결과.

> **점검 기준**: "강사진 테스트 단계"에서 실행/보안/데이터 손실/심사 거부를 유발할 수 있는 항목 우선.

---

## 1팀 Snoffee — 체크리스트

### 🔴 CRITICAL (실행/심사/데이터 손실)
- [ ] **C1-1** Gemini API 키가 코드/local.properties/BuildConfig에 직접 노출되는지 → grep `GEMINI_API_KEY`, `gemini.api`, `geminiApiKey`
- [ ] **C1-2** Gemini 호출이 Firebase AI Logic 또는 Cloud Functions proxy 경유인지, 직접 https endpoint 호출인지
- [ ] **C1-3** AndroidManifest의 health permission (`android.permission.health.READ_SLEEP` 등) 선언 누락
- [ ] **C1-4** Samsung Health 권한 revoke 후 onResume 재확인 안 함 → 크래시 가능
- [ ] **C1-5** Wear ↔ Mobile 패키지명/applicationId/signing config 일치 여부 (Data Layer 동작 전제)
- [ ] **C1-6** FCM `onNewToken` 토큰 서버 업로드 누락 시 다중 디바이스 알림 실패

### 🟠 HIGH (정합성/배터리/누수)
- [ ] **C1-7** CaffeineCalculator 반감기 공식 정확성 (`C × 0.5^(t/h)`)
- [ ] **C1-8** 다중 음료 섭취 시 잔류량 합산 (각 항목 개별 잔류 합) 처리
- [ ] **C1-9** 시간 계산 Instant/ZonedDateTime 일관성 (timezone 버그)
- [ ] **C1-10** Room DB Migration 정책 (production 배포 후 schema 변경 가능성)
- [ ] **C1-11** Firestore listener unsubscribe (Compose `DisposableEffect` / `awaitClose`)
- [ ] **C1-12** firestore.rules 파일 존재 + uid 기반 격리

### 🟡 MEDIUM (UX/코드 품질)
- [ ] **C1-13** Hilt 의존성 순환 / Service 주입
- [ ] **C1-14** ProGuard/R8 release 설정 (minify 켜져 있고 keep 룰)
- [ ] **C1-15** Wear 모듈 build.gradle.kts dependency 일관성

---

## 2팀 Umma — 체크리스트

### 🔴 CRITICAL
- [ ] **C2-1** Gemini Live API 호출 채널 (Firebase AI Logic SDK vs 직접 호출), API key 노출
- [ ] **C2-2** App Check 적용 여부 (Gemini quota abuse 방지)
- [ ] **C2-3** AudioRecord/AudioTrack release 보장 (ViewModel.onCleared / DisposableEffect)
- [ ] **C2-4** RECORD_AUDIO 권한 거부 시 fallback (앱 종료/무한 로딩 방지)
- [ ] **C2-5** firestore.rules — `users/{uid}/**` uid 격리, `request.auth.uid == uid`
- [ ] **C2-6** Session memory (`recentFullContext`) 크기 제한 — 무한 누적 시 메모리/토큰 폭발

### 🟠 HIGH
- [ ] **C2-7** SM-2: ease_factor 최솟값 1.3 floor 적용
- [ ] **C2-8** SM-2: q<3 시 repetitions=0, interval=1 reset
- [ ] **C2-9** SM-2: next_review_at timezone 일관성 (UTC)
- [ ] **C2-10** DashSummary 캐시와 원본 transcript 일관성 (lang key mismatch)
- [ ] **C2-11** Firestore snapshot listener unsubscribe 패턴
- [ ] **C2-12** Coroutine scope leak (GlobalScope 사용 여부)
- [ ] **C2-13** `data/repository/fake/` 코드가 release 빌드에 포함되는지 (테스트 코드 누수)

### 🟡 MEDIUM
- [ ] **C2-14** Audio buffer size 적절성 (`AudioRecord.getMinBufferSize`)
- [ ] **C2-15** AI 호출 중복/디바운싱 (사용자 연타 시 cost 폭발)
- [ ] **C2-16** Room migration 정책

---

## 3팀 BBipIt — 체크리스트

### 🔴 CRITICAL
- [ ] **C3-1** AndroidManifest의 `foregroundServiceType="location"` + FOREGROUND_SERVICE_LOCATION 권한 선언
- [ ] **C3-2** AndroidManifest의 `foregroundServiceType="microphone"` (PTT용) + FOREGROUND_SERVICE_MICROPHONE
- [ ] **C3-3** ACCESS_BACKGROUND_LOCATION 별도 runtime 요청 흐름 (Android 10+)
- [ ] **C3-4** BackgroundListenerService startForeground notification + type 일치
- [ ] **C3-5** **firestore.rules 파일 존재** — 친구 위치는 친구만 read, DM은 participants만 read/write
- [ ] **C3-6** Firebase Storage rules — 음성 파일 접근 제어
- [ ] **C3-7** AudioRecord/AudioTrack release 보장 (PushToTalkViewModel onCleared)
- [ ] **C3-8** Cloud Functions `functions/index.js` 트리거 권한/룰 우회 점검 (admin SDK 사용 → rules 우회)

### 🟠 HIGH
- [ ] **C3-9** Firestore snapshot listener unsubscribe (Chat/Friend/LiveStatus 다수 화면)
- [ ] **C3-10** 위치 업데이트 interval/displacement (배터리 영향)
- [ ] **C3-11** FCM data-only 메시지 vs notification 메시지 처리 (백그라운드 시 표시)
- [ ] **C3-12** MyFirebaseMessagingService 토큰 갱신 처리
- [ ] **C3-13** Wear 모듈(bbipit) 패키지/sigining 일치
- [ ] **C3-14** 친구 추가 — 양방향 트랜잭션 (한쪽만 추가되어 일관성 깨짐 방지)
- [ ] **C3-15** 워키토키 청크 분할/Storage 비용

### 🟡 MEDIUM
- [ ] **C3-16** Compose lifecycle (collectAsStateWithLifecycle 사용)
- [ ] **C3-17** ProGuard/R8 (모델 직렬화 keep)
- [ ] **C3-18** 다중 마커 시 지도 성능

---

## Step 5 실행 순서

1. **빠른 검증 (각 팀 30분 이내)**
   - AndroidManifest 권한/서비스 선언
   - firestore.rules / storage.rules 파일 존재 + 내용
   - local.properties / BuildConfig API key
   - functions/index.js (3팀)
2. **코드 패턴 grep**
   - `addSnapshotListener`, `release()`, `onCleared`, `GlobalScope`
   - `DisposableEffect`, `LaunchedEffect`
   - `gemini`, `BuildConfig.`
3. **핵심 파일 정독**
   - 1팀 CaffeineCalculator.kt
   - 2팀 AudioRecorder/AudioPlayer + Flashcard SRS 로직
   - 3팀 BackgroundListenerService, MyFirebaseMessagingService, PushToTalkViewModel
4. **Findings 별도 파일로 작성** (`04_team{N}_findings.md`)

### 사용 도구
- Grep (패턴 검색)
- Read (핵심 파일 정독)
- Glob (파일 존재 확인)
- Agent(Explore) — 팀별 병렬 실행 가능 (각 팀 별도 worker)
