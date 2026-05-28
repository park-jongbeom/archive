# 코호트 크리티컬 점검 최종 리포트 (2026-05-22)

> 점검 시점: 2026-05-22 — 강사진 테스트 지원 단계
> 데이터 소스: 각 팀 `teams-docs/{N}team/repo/` 로컬 git 워킹트리
> 점검 방식: 3팀 병렬 정적 분석 (general-purpose Agent × 3)
> 어조: 객관·실용 (보조강사 자료, 학생 비난 어조 금지)

## 신호등 요약

| 팀 | 상태 | 🔴 | 🟠 | 🟡 | 한 줄 |
|---|---|---|---|---|---|
| 1팀 Snoffee | 🔴 | 6 | 3 | 3 | 핵심 기능(AI/Watch/Report) 미구현, README와 실 구현 간극 큼 |
| 2팀 Umma | 🔴 | 5 | 6 | 9 | 구현은 진척, 보안 자세(키 평문/App Check/fake repo) 미흡 |
| 3팀 BBipIt | 🔴 | 6 | 3 | 9 | 서버 functions 빈 파일 + rules 부재 — 5/26 데드라인 영향 직접 |

---

## 🔴 강사진 테스트 직전 즉시 조치 권고 (코호트 횡단)

### A. 보안 — Firestore/Storage rules 부재 (3팀 공통) ⭐⭐⭐
| 팀 | 상태 |
|---|---|
| 1팀 | `firestore.rules` 부재 |
| 2팀 | `firestore.rules` / `firebase.json` 부재 |
| 3팀 | `firestore.rules` / `storage.rules` 부재, `firebase.json`은 functions만 |

→ **코호트 100% 발현**. 강사진 테스트 계정으로 cross-user read 가능성. 콘솔 룰 캡처 + repo 추가 권고.

### B. Gemini / API key 노출 (2팀)
- 2팀: `local.properties`에 평문 `AIzaSy***` + `BuildConfig.API_KEY`로 APK 임베드 (실 사용처는 0건 dead code, 단 APK 디컴파일 노출)
- **즉시 GCP 콘솔에서 키 회수(rotate) 권고**
- App Check 미적용 — 2026년 정책상 quota abuse 위험

### C. 서버/핵심 인프라 미구현
- **3팀 `functions/index.js`가 32줄 boilerplate (실 함수 0건)** — 클라이언트가 호출하는 17개 함수 (`createChatRoom`, `sendMessage`, `requestFriend`, `sendVoiceMessage` 등) 전부 `NOT_FOUND` 실패. 강사 데모 거의 모든 경로 막힘. **콘솔에 별도 deploy 됐는지 즉시 확인 필요**
- **1팀 Gemini AI 전체 TODO 스텁** — "AI 컷오프 추천"/"주간 리포트" 버튼 누르면 `NotImplementedError` 크래시
- **1팀 워치 모듈 빈 껍데기** + `applicationId="com.example.wear"` (모바일 `com.snoffee.app`와 불일치) — Wear ↔ Mobile Data Layer 페어링 불가
- **1팀 FCM 인프라 전무** (`FcmService.kt` 0줄)
- **3팀 PTT 워치 수신 UID 하드코딩** ([BackgroundListenerService.kt:439](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L439) — `targetUid = "Wy102dzyw4buC0V6YJuqxjtf6qA2"`) — 워치 PTT가 한 사람에게만 전송

### D. 데이터 정합 / UI 신뢰성
- **1팀 ReportViewModel이 더미 데이터 하드코딩** — 시연 시 항상 같은 가짜 숫자
- **1팀 온보딩 입력값(키/몸무게/카페인 민감도)이 어디에도 저장되지 않음** — 카페인 계산에 개인화 0
- **1팀 Room `fallbackToDestructiveMigration(dropAllTables=true)`** — 스키마 변경 시 사용자 데이터 전체 삭제
- **2팀 `data/repository/fake/` 3개가 `main/java/` 안에** + `isMinifyEnabled=false` — DI 한 줄 토글로 fake 활성화 위험
- **3팀 FCM `onMessageReceived` 미구현** ([MyFirebaseMessagingService.kt](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/notification/MyFirebaseMessagingService.kt) 41줄, onNewToken만) — push 알림 표시 안 됨

### E. Android 14+ 권한 흐름
- **3팀 ACCESS_BACKGROUND_LOCATION 런타임 요청 누락** ([MapScreen.kt:159](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L159)) — 백그라운드 전환 시 위치 무음 중단
- **1팀 Samsung Health 권한 revoke 가드 없음** — `SleepRepositoryImpl.getSleepDataByDateRange()` 진입 시 hasPermissions 미체크 → SecurityException 가능

---

## 코호트 패턴 (이번 점검에서 새로 발견된 신호)

### 패턴 1: **README 광고 vs 실 구현 간극** (1팀 ⭐⭐⭐ / 3팀 부분)
- 1팀 README는 Galaxy Watch 페어, Gemini AI 컷오프, FCM 알림, 주간 자연어 리포트 모두 광고 → 실제는 전부 TODO/0줄/더미
- 3팀은 functions 17개 호출하나 서버 본문 비어 있음
- → **PM 1:1에서 "이번 주말까지 무엇이 실제 동작하는지" 자기보고 필요**

### 패턴 2: **Firestore Rules 부재가 코호트 100%** ⭐⭐⭐
- 강사진 테스트 계정으로 다른 학생 데이터 cross-read 가능성
- 콘솔 룰 캡처 강사진 보고 필수

### 패턴 3: **3팀이 보안/lifecycle 가드는 가장 견고하나, 핵심 서버가 빈 상태**
- AudioRecord try/finally release, snapshot listener awaitClose, foregroundServiceType 4종 OR, App Check Debug 적용 — 모두 양호
- 그러나 `functions/index.js` 빈 파일 → 데모 동작 불가 가능성
- **3팀 5/26 데드라인 영향**: 함수 17개 4일 안에 작성 불가능 → 클라이언트 직쓰기 + rules 강화로 전환 결정 필요

### 패턴 4: **2팀이 보안 자세가 가장 미흡**
- 1팀: 키 자체가 없음 (구현 안 됨)
- 3팀: App Check Debug라도 적용
- 2팀: 키 평문 + App Check 0 + minify off + fake main set
- → 2팀 박재민 팀장 R-burn 약신호(메모리 기록)와 무관하게 "구현 진척 vs 자세" 사이 불균형 신호

---

## 팀별 즉시 조치 권고 (강사 테스트 시작 전)

### 1팀 (오늘~내일)
1. README의 "AI 컷오프 시간"/"주간 자연어 리포트" 버튼에 try-catch + "준비 중" UI — 크래시 차단
2. ReportViewModel 더미 데이터 표시에 "샘플 데이터" 라벨 추가 (오해 방지)
3. firestore.rules 추가 — 최소 `request.auth != null && request.auth.uid == uid`
4. 워치 모듈 — 데모 시나리오에서 제외하거나 applicationId 통일 + 최소 화면 추가
5. 온보딩 입력값 저장소 연결 (DataStore 또는 Firestore) — Day 1 핵심 흐름

### 2팀 (오늘)
1. **GCP 콘솔에서 `AIzaSy***` 키 회수 + 재발급** (가장 시급)
2. `local.properties`의 키를 더미값으로 치환 (archive 보호)
3. `buildConfigField("String", "API_KEY", ...)` 라인 삭제 (dead code, APK 누수 차단)
4. App Check Debug provider 초기화 (UmmaApplication.onCreate) — 테스트 환경 quota 보호
5. firestore.rules 추가 (Console 작성 후 export)
6. `data/repository/fake/`를 `androidTest/`로 이동 또는 `BuildConfig.DEBUG` 가드

### 3팀 (오늘 — 5/26 데드라인 영향) ⭐⭐⭐
1. **`functions/index.js` 실제 deploy 상태 즉시 확인** — repo 0건이지만 콘솔에 별도 deploy 됐는지 학생 확인 필요. PM에게 즉시 알릴 것
2. PTT 워치 수신 UID 하드코딩 제거 ([BackgroundListenerService.kt:439](teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L439))
3. firestore.rules + storage.rules + firebase.json 경로 추가 — 최소 룰 적용
4. ACCESS_BACKGROUND_LOCATION 런타임 요청 추가 (FINE 수락 후 2단계)
5. FCM `onMessageReceived` 구현 — notification 표시
6. `voices/` Storage downloadUrl 그대로 Firestore 저장하는 구조 — 임시로 friendsList 기반 read 룰만 적용 후 데모 후 receiver-uid 경로 재설계

---

## 강사진 테스트 매니저 1줄 보고용

> "3팀 모두 Firestore Rules 파일 부재, 1팀 핵심 기능(AI/Watch/Report) 미구현, 2팀 평문 API key + App Check 미적용, 3팀 서버 functions 빈 파일 — 강사 테스트 계정 데이터 cross-read 위험 및 데모 경로 다수 실패 가능. 점검 보고서: `operation/reports/critical_review_260522/`"

---

## 부록 — 양호한 부분 (학생 칭찬 자료)

### 1팀
- AndroidManifest health permission 선언 + queries 패키지 가시성 OK
- READ_SLEEP / WRITE_SLEEP 분리 인식

### 2팀 ⭐ (보안 외 영역에서 가장 견고)
- RECORD_AUDIO 권한 거부 fallback 구현
- AudioRecord try/finally release
- recentFullContext 100턴 제한 (세션 메모리 무한 증가 차단)
- snapshot listener 미사용 (의도적, 누수 위험 0)
- callbackFlow awaitClose 패턴 일관
- epoch ms timezone 통일

### 3팀 ⭐⭐
- foregroundServiceType 4종(`connectedDevice|microphone|location|dataSync`) OR + 권한 4개 매칭
- AudioRecord/MediaPlayer release 견고 (try/finally + Completion/Error)
- snapshot listener 8개 위치 모두 awaitClose / 명시적 remove
- App Check Debug provider 적용 (Release 미적용은 별개)
- 위치 BackgroundListenerService startForeground + type + Android 14 fallback 명시

→ **3팀이 Android 14 정책 준수 + lifecycle 자세가 가장 견고**. 다만 서버/콘텐츠 작성이 미달.

---

## 점검 한계 (사용자 인지 필요)

- **콘솔 측 설정 미확인** — Firestore rules, Functions deploy, App Check enforcement, Storage rules는 모두 콘솔 상태가 최종. repo만으로 단정 불가
- **google-services.json 내용 미열람** — 의도적 (클라이언트 식별자라 무해하나 보고서 노출 회피)
- **실 동작 미테스트** — 정적 분석만. 강사가 실 단말에서 실행하면 추가 발견 가능
- **Git history 미열람** — 키가 과거 커밋에 들어간 적이 있는지는 별도 점검 필요 (2팀)

---

## 작성 파일

- [00_progress.md](00_progress.md) — 작업 진행 기록
- [01_domains.md](01_domains.md) — 팀별 도메인/기능 정리
- [02_references.md](02_references.md) — 검색된 참조 정보
- [03_checklist.md](03_checklist.md) — 47개 체크리스트
- [04_team1_findings.md](04_team1_findings.md) — 1팀 상세 (330줄)
- [04_team2_findings.md](04_team2_findings.md) — 2팀 상세
- [04_team3_findings.md](04_team3_findings.md) — 3팀 상세
- [05_final_report.md](05_final_report.md) — **본 문서**
