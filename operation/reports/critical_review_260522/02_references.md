# Step 2-3 — 유사 오픈소스/참조 정보 정리 + 체크 항목 도출 (2026-05-22)

## 검색 카테고리

8개 카테고리 검색 완료. 각 항목별 critical-issue 근거를 정리한다.

---

## A. Gemini API 키 보안 (1팀, 2팀 공통)

### 2026년 critical 변경사항
- **Gemini API가 활성화된 GCP 프로젝트의 모든 API key는 Gemini endpoint에 자동 접근 가능** (2026 변경)
- google-services.json의 API key, AndroidManifest.xml의 metadata API key가 그대로 노출되면 사용자가 quota 도용 가능

### 권장 패턴
- **Firebase AI Logic SDK 사용** (client SDK가 server proxy를 통해 호출)
- **Firebase App Check 필수** — 미적용 시 abuse 차단 불가
- **Cloud Functions proxy** — 직접 Gemini API 호출 시 server-side proxy 필요
- 코드에 키 하드코딩 절대 금지, `local.properties`도 BuildConfig 노출 시 APK에 포함됨

### 체크 항목
1. `local.properties`에 `GEMINI_API_KEY` 같은 raw 키 있는지
2. `BuildConfig.GEMINI_API_KEY` 등으로 클라이언트에 노출되는지
3. Firebase AI Logic 사용 시 App Check 활성화 여부
4. Cloud Functions proxy 존재 여부

---

## B. Android 14 FOREGROUND_SERVICE (3팀 위치 + 워키토키)

### 필수 요건 (targetSdk 34+)
1. AndroidManifest의 `<service>` 태그에 `android:foregroundServiceType` 명시
2. 위치는 `foregroundServiceType="location"` + `FOREGROUND_SERVICE_LOCATION` 권한
3. 마이크는 `foregroundServiceType="microphone"` + `FOREGROUND_SERVICE_MICROPHONE` 권한
4. 백그라운드에서 startForegroundService 호출 시 visible activity가 있어야 함 (예외 외)
5. `ACCESS_BACKGROUND_LOCATION` runtime permission 별도 요청 (Android 10+)

### 체크 항목
1. AndroidManifest service 선언에 foregroundServiceType 누락 여부
2. BackgroundListenerService.kt가 startForeground 호출 시 notification + type 매칭
3. ACCESS_BACKGROUND_LOCATION 별도 요청 흐름
4. 위치 권한 거부 시 fallback (앱 종료 방지)

---

## C. AudioRecord/AudioTrack 생명주기 (2팀 Gemini Live, 3팀 워키토키)

### 일반 누수 원인
- DisposableEffect/LaunchedEffect에서 release 누락
- ViewModel onCleared에서 release 누락
- coroutine 중 release 안 하고 cancel만 → native 리소스 누수

### 체크 항목
1. AudioRecord/AudioTrack 인스턴스가 `release()` 호출 경로 보장되는지
2. onPause / onStop에서 stop + release
3. Compose `DisposableEffect`로 wrap 되어 있는지
4. 마이크 사용 중 다른 앱 인터럽트 시 처리

---

## D. Wear OS Data Layer (1팀 Watch + 3팀 BBipIt 워치)

### 핵심 요건
- **handheld와 wearable이 동일 package name, version code, signing certificate** 필요
- `MessageClient.sendMessage()`는 best-effort, 재시도 없음 → TARGET_NODE_NOT_CONNECTED 핸들링 필수
- Wearable API 가용성 체크 안 하면 예외

### 체크 항목
1. wear/build.gradle.kts와 app/build.gradle.kts의 applicationId/versionCode 일치 여부
2. MessageClient 호출에 OnFailureListener 및 재시도 로직
3. Wearable.getNodeClient로 node 존재 확인
4. (3팀) bbipit 모듈 분리되어 있으나 동일 package 검증 필요

---

## E. Firestore Security Rules (3팀 모두 critical)

### 일반 실수
- 기본 `allow read, write: if true;` 또는 `if request.auth != null;` 만으로 친구/그룹 격리 안 됨
- 위치 정보 → 친구만 read 가능해야 함
- DM → 참여자(participants 배열)만 read/write 가능해야 함
- get() 호출은 rule 1회당 최대 10번 제한

### 체크 항목
1. `firestore.rules` 파일이 repo에 있는지
2. 사용자 위치 → 친구 검증 룰 (3팀)
3. DM 채팅방 → participants 검증 룰 (3팀, 2팀은 단독 사용자라 적은 영향)
4. 사용자 학습 데이터 → uid 격리 (2팀)
5. cross-user write 차단

---

## F. 카페인 반감기 계산 (1팀)

### 표준 공식
```
잔류량(t) = 섭취량 × 0.5^(경과시간 / 반감기)
```
- 표준 반감기 5h (실 범위 3~7h, 흡연자 ~3h, 임산부 9~11h)
- 50mg 이상 잔류 시 EEG 수면 disruption 시작 (논문 기준)
- 6h 전 카페인도 수면 영향 있음 (Drake et al.)

### 체크 항목
1. CaffeineCalculator.kt 공식이 표준 모델인지
2. 시간차 계산 timezone/Instant 처리
3. 다중 섭취 합산 시 각 항목별 잔류량 누적 처리
4. 컷오프 시간 계산 — 사용자 취침 시간 대비 50mg 임계

---

## G. PTT/워키토키 latency (3팀)

### 아키텍처 비교
- Cloud Storage 업로드 → 다운로드: 300~900ms (cloud-based 최선 ~300ms)
- WebRTC: 100ms 이하 가능하나 복잡도 ↑
- Firebase Storage + Firestore reference: 비용/지연 trade-off

### 체크 항목
1. 청크 단위 분할 전송인지, 전체 파일 업로드 후 알림인지
2. Storage 권한 룰 (인증된 친구만 read)
3. 음성 코덱 (AAC/Opus) 및 비트레이트
4. 채널/대화방 분리 (다중 청자 fanout)

---

## H. SRS / SM-2 (2팀)

### 표준 SM-2
- 입력: quality (0~5), 이전 interval, repetitions, ease_factor
- ease_factor 새 값 = 이전 EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
- ease_factor 최솟값 1.3 floor 필수 (없으면 "ease hell")
- q < 3이면 repetitions=0, interval=1 로 reset
- next_review_at = now + interval days

### 체크 항목
1. ease_factor 최솟값 1.3 clamping
2. q<3 시 reset 로직
3. next_review_at UTC 처리 (timezone 버그)
4. 첫 review interval = 1, 두 번째 = 6 (SM-2 표준)
5. 5단계 평가 → q 매핑 (1=0, 2=1, 3=3, 4=4, 5=5 등)

---

## I. Firestore listener leak (모든 팀)

### 핵심
- snapshot listener는 unsubscribe 안 하면 ViewModel과 함께 누수 + bandwidth 낭비
- Compose: DisposableEffect의 onDispose에서 ListenerRegistration.remove() 호출
- ViewModel.onCleared에서 명시적 remove

### 체크 항목
1. addSnapshotListener 호출 위치에서 ListenerRegistration 보관 + remove 호출
2. flow callbackFlow 사용 시 awaitClose에서 remove
3. 화면 전환 시 listener 누적 여부

---

## J. Samsung Health / Health Connect (1팀)

### 권한 분리
- READ_SLEEP, WRITE_SLEEP 별도 권한
- 사용자가 언제든 권한 revoke 가능 → onResume마다 permission check 필요
- 2026년 Google Fit API → Health Connect로 transition 중 (Google Fit 연말 종료)

### 체크 항목
1. AndroidManifest에 health permission 선언
2. permission 거부/revoke 시 fallback UI
3. Health Connect 미설치 기기 대응 (안내 또는 Play Store deeplink)
4. Samsung Health SDK 직접 사용 시 권한 별개 처리

---

## 다음 단계 (Step 4)

검색 결과로 충분히 critical 영역이 도출되었음. 추가 검색이 필요할 영역:
- Hilt scoping 누수 (Service에 ViewModel 주입 시) — 일반 지식으로 커버 가능
- ProGuard/R8 minify (release 빌드 누락 가능) — 별도 단계에서 체크

→ Step 3에서 팀별 체크리스트 확정 후 Step 5 진행.
