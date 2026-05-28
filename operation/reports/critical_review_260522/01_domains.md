# Step 1 — 팀별 프로젝트 도메인 및 기능 정리 (2026-05-22)

각 팀 README, repo 구조, prd, product_brief 기반으로 정리.

---

## 1팀 — Snoffee (☕ 카페인-수면 관리)

### 도메인
- **카페인 섭취 관리 + 수면 인사이트 + Wear OS 페어링**
- 분류: Health & Wellness, Wearable companion

### 핵심 기능
| 기능 | 설명 | 리스크 도메인 |
|------|------|--------------|
| Smart Caffeine Tracking | 음료 기반 카페인 기록 + 자동 시간 저장 + 커스텀 음료 | Room DB, Firebase Firestore 일관성 |
| Residual Caffeine Analysis | 카페인 반감기 기반 잔류량 계산 + 실시간 게이지 | **수치 계산 정확도 (반감기 5~6h)**, 시간대 처리 |
| AI Personalized Cutoff Time | Gemini API로 컷오프 시간 추천 | **API 키 노출 위험**, Backend Functions 우회 여부 |
| Galaxy Watch Integration | WearOS 페어 (입력/진동/알림) | **Wearable Data Layer 경로 (MessageClient/DataClient)**, 페어링 끊김 처리 |
| Sleep Data Integration | Samsung Health / Health Connect | **민감정보 권한 (HEALTH_DATA)**, 동의 흐름, 백그라운드 read |
| Weekly Insight Report | 주간 패턴 분석 + AI 자연어 리포트 | 비용 (Gemini 호출 빈도), 캐싱 |

### 기술 스택
- Kotlin, Compose, MVVM + Clean Lite, Hilt, Coroutine+Flow
- Room (로컬), Firestore (원격), Firebase Functions (백엔드)
- Gemini API, FCM, WearOS, Samsung Health SDK, Health Connect

### Repo 구조 핵심
- `app/` (모바일) + `wear/` (WearOS) — **분리 모듈 sync 검증 필요**
- `data/datasource/health/SamsungHealth*` — 헬스 권한 매개 코드
- `data/datasource/remote/DrinkRemoteDataSource` (Firestore)
- `presentation/notification/FcmService.kt` — FCM 수신

### 크리티컬 후보 영역 (선험적)
1. **반감기 계산** — `domain/util/CaffeineCalculator.kt` 수식 정확성
2. **Wear ↔ Mobile 통신** — DataLayer 경로, 노드 ID, 데이터 충돌
3. **Samsung Health 권한** — runtime permission, 사용자 동의 unrevoke
4. **Gemini API 키** — `local.properties` vs 코드 직접 노출, Functions proxy 여부
5. **FCM 토큰 갱신** — onNewToken 처리, 다중 디바이스

---

## 2팀 — Umma (🍼 AI 영어 회화 학습)

### 도메인
- **AI 자유 회화 (Gemini Live) + 문장 교정 + SRS 반복학습 + 성장 추적**
- 분류: EdTech (Language Learning), Voice AI

### 핵심 기능
| 기능 | 설명 | 리스크 도메인 |
|------|------|--------------|
| AI 자유 회화 (PTT) | Firebase AI Logic / Gemini Live 실시간 음성 | **AudioRecord/AudioTrack 버퍼 관리**, 권한, 메모리 누수, 스트리밍 재연결 |
| 문장 교정 | 세션 메모리 `recentFullContext`에서 발화 추출, AI 교정 | **세션 메모리 무한 누적**, 비용 폭발, 토큰 한도 |
| Dashboard (DashSummary) | 언어별 학습 컨텍스트, `selectedLearningLanguage` 공유 | 캐시 일관성, lang 키 불일치 |
| SRS Flashcard | 5단계 자기평가 + `interval`/`ease_factor`/`next_review_at` | **SM-2 알고리즘 정확도**, 시간대 처리 |
| 성장 추적 | Internal/External Metrics 분리, Vocabulary/Grammar 등 | AI 분석 비용, 캐싱 |

### 기술 스택
- Kotlin, Compose, Clean Architecture + MVVM, Hilt, Coroutine+Flow
- Room, Firestore, Cloud Functions, FCM
- Gemini (Firebase AI Logic, Gemini Live)
- AudioRecord/AudioTrack 또는 Media3 ExoPlayer

### Repo 구조 핵심
- `data/source/local/AudioRecorder.kt`, `AudioPlayer.kt` — **저레벨 오디오 API**
- `data/repository/correction/` — Correction 파이프라인
- `data/source/local/SessionMemoryDatabase.kt` — Room
- `data/source/remote/LearningStateRemoteDataSource.kt` — Firestore

### Firestore 데이터 모델
```
users/{uid}
├── user_learning_preference/current
├── language_states/{lang}
├── dashboard_summaries/{lang}
├── sessions/{lang} (recentFullContext)
└── flashcards/{cardId}
```

### 크리티컬 후보 영역 (선험적)
1. **AudioRecord 권한/lifecycle** — RECORD_AUDIO, onPause 시 stop, 버퍼 해제
2. **Gemini Live API 키** — Firebase AI Logic 안전 채널 여부, 클라이언트 노출 금지
3. **세션 메모리 무한 증가** — `recentFullContext` 크기 제한, 토큰 cost
4. **SRS 시간대 계산** — `next_review_at`, UTC vs Local
5. **Firestore security rules** — uid 기반 격리, language_states 접근 권한

---

## 3팀 — BBipIt (👋 친구 위치 + DM + 워키토키)

### 도메인
- **실시간 위치 공유 + 친구 인터렉션 + DM + 푸시-투-토크 워키토키 + 푸시알림**
- 분류: Social Location, Realtime Voice, Messaging

### 핵심 기능 (PRD 파일명 기반)
| 기능 | 설명 | 리스크 도메인 |
|------|------|--------------|
| 회원가입 / 로그인 | 소셜 (구글?) | Firebase Auth 토큰 관리 |
| 위치 권한 획득 및 초기 설정 | ACCESS_FINE_LOCATION, BACKGROUND | **권한 단계적 요청 (Android 12+)**, 배터리 영향 |
| 실시간 위치 추적 및 공유 | FusedLocation + Firestore 또는 RTDB | **백그라운드 위치 (FOREGROUND_SERVICE_LOCATION)**, 배터리, 권한 거부 처리 |
| 친구 위치 확인 및 인터렉션 | 지도 마커, 알림 | 마커 다수 시 성능, 지도 SDK 키 |
| 친구 추가 | 검색, 요청, 수락 | Firestore 트랜잭션, 양방향 그래프 |
| 워키토키 (Push-to-Talk) | 실시간 음성 송수신 | **AudioRecord/AudioTrack**, **Firebase Storage 업로드/다운로드**, latency, 채널 분리 |
| 대화방 목록 관리 (DM) | 1:1 또는 그룹 | 채팅방 상태 동기화, 안 읽음 카운트 |
| 실시간 메시지 송수신 (DM) | Firestore 또는 RTDB 리스너 | 페이지네이션, 리스너 누수 |
| 푸시알림 | FCM (앱 외 위치/메시지 알림) | **백그라운드 알림 (MyFirebaseMessagingService)**, 토큰 갱신, 알림 채널 |
| 프로필 편집 | 프로필 이미지 등 | Storage 권한 |

### 기술 스택
- Kotlin, Compose, MVVM + Clean (com.bbip.bbipit 패키지)
- Hilt, Firestore, Firebase Auth, FCM, Firebase Storage
- Cloud Functions (`functions/index.js` 존재 → 서버 트리거 있음)
- Wear OS (`bbipit/` 별도 모듈 — WearOS 컴패니언)

### Repo 구조 핵심
- `core/base/BackgroundListenerService.kt`, `MobileMessageListenerService.kt`, `AppLifecycleObserver.kt`, `LifeCycleManager.kt` — **백그라운드 서비스/생명주기**
- `core/util/AudioRecorder.kt`, `AudioPlayer.kt`, `PermissionUtil.kt` — 오디오/권한
- `data/source/remote/voice/`, `notification/MyFirebaseMessagingService.kt`
- `domain/usecase/SyncMyLocationUseCase.kt` — **위치 동기화**
- `presentation/map/viewmodel/PushToTalkViewModel.kt` — 워키토키
- `bbipit/` — 워치 모듈

### 크리티컬 후보 영역 (선험적)
1. **백그라운드 위치 권한** — Android 10+ BACKGROUND_LOCATION 분리 요청 / FOREGROUND_SERVICE_LOCATION foregroundServiceType
2. **위치 업데이트 빈도/배터리** — interval, smallestDisplacement, doze mode
3. **워키토키 latency / Storage 비용** — 업로드 vs RTDB binary vs WebRTC
4. **Firestore Security Rules** — 친구만 위치 read, DM 참여자만 read
5. **FCM 백그라운드 페이로드** — data-only message, notification message 차이
6. **CHAT 리스너 누수** — onStop에서 detach 누락
7. **Cloud Functions** — `functions/index.js` 트리거 검토 (서버 권한, 룰 우회)
8. **앱 ↔ 워치 통신** — Wearable Data Layer MessageClient/DataClient

---

## 공통 (3팀 횡단) 영역

- **API 키 / 비밀** — `local.properties`, `google-services.json` (Firebase는 클라이언트 식별자라 노출 OK이나, API key 제한 필요), Gemini key
- **Firestore Security Rules 존재 여부** — 모든 팀이 Firestore 사용 → rules 파일 없으면 critical
- **Compose lifecycle** — `LaunchedEffect`, `collectAsStateWithLifecycle` vs `collectAsState` 사용
- **권한 거부 시 fallback** — 카메라/마이크/위치/헬스
- **테스트 커버리지** — 통합/E2E 부재 시 강사 테스트 시 회귀 위험
