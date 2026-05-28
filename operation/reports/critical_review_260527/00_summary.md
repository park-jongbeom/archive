# 코호트 화면 동작 오류 통합 리포트 (2026-05-27)

> 점검 시점: 2026-05-27 (5/22 critical_review로부터 5일 경과)
> 데이터 소스: 각 팀 GitHub `develop` 브랜치 최신 pull
> 점검 방식: 3팀 병렬 정적 분석 (general-purpose Agent × 3) — 실 단말 빌드 미수행
> 관점: **강사진 실 단말 테스트 시 만나는 크래시·무반응·잘못된 데이터 표시 위주** (보안/구조 이슈 제외)
> 어조: 객관·실용 (강사가 판단할 자료, 학생 비난 어조 금지)

---

## 1. 소스 최신화 결과

| 팀 | 레포 | 브랜치 | HEAD | 5/22 → 5/27 변화량 |
|---|---|---|---|---|
| 1팀 Snoffee | `LIKELION-Android-BOOTCAMP-6th/Snoffee` | develop | `6932a15` (2026-05-27) | 26 commits / 34 files / +1259 -329 |
| 2팀 Umma | `LIKELION-Android-BOOTCAMP-6th/Umma` | develop | `e1b6ec8` (2026-05-27) | 52 commits / 243 files / **+14409 -2014** |
| 3팀 BBipit | `LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip` | develop | `36e6afd` (2026-05-27) | 50 commits / 43 files / +1184 -605 |

main 브랜치는 3팀 모두 5/22 시점 그대로 — 강사 빌드는 **develop 기준**이어야 함.

---

## 2. 신호등 요약

| 팀 | 상태 | 🔴 | 🟠 | 🟡 | 한 줄 |
|---|---|---|---|---|---|
| 1팀 Snoffee | 🟠 | 0 | 6 | 7 | 신규 크래시 없음. Report 더미·Health Connect 0건·TimePicker 12h 어긋남이 시연 차단 |
| 2팀 Umma | 🟢 | 0 | 2 | 4 | 신규 기능 다수 안정. R2-2 Topic Dialog cancel 5일째 미반영만 잔존 |
| 3팀 BBipit | 🔴 | 3 | 7 | 6 | **PTT 무전 100% 실패 회귀** (PR #194 부작용) + Map startForegroundService 누락 악화 + SettingsDrawer 빈 stub |

**코호트 합계: 🔴 3 / 🟠 15 / 🟡 17 (총 35건)**

---

## 3. 🔴 강사진 테스트 직전 즉시 조치 권고 — 모두 3팀

3팀에 🔴이 집중되어 있고, **두 건은 5/22 → 5/27 사이 회귀(악화)**라는 점이 중요한 코호트 신호.

### A. 3팀 PTT 무전 송신 100% 실패 (회귀, 5분 패치 가능) ⭐⭐⭐

- **위치**: [PushToTalkViewModel.kt:108](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt#L108) (모바일), [BackgroundListenerService.kt:535](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L535) (워치), [VoiceRemoteDataSourceImpl.kt:30](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/voice/VoiceRemoteDataSourceImpl.kt#L30)
- **원인**: PR #194 (b201e14, 5/26) "음성 메세지 API 잘못 호출하고 있던 부분 수정" — `sendVoiceMessageDirect` (Firestore 직쓰기) → `sendVoiceMessage` (Cloud Function 호출) 일괄 변경
- **5/22 핵심 미해결 동반**: `functions/index.js` 여전히 빈 파일 → 모든 호출 `NOT_FOUND`
- **시연 영향**: PTT 데모 경로 전부 차단 — 3팀의 핵심 차별화 기능
- **권고**: `sendVoiceMessageDirect` 주석 해제 + 두 호출부 원복 (시연 직전 1회 패치), 또는 콘솔에 `sendVoiceMessage` 배포 여부 즉시 확인

### B. 3팀 MapScreen 권한 launcher의 startForegroundService 누락 (악화) ⭐⭐⭐

- **위치**: [MapScreen.kt:178-184](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L178)
- **원인**: 5/22엔 주석으로 비활성화되어 있던 `startForegroundService(intent)` 호출이, 5/22~5/27 MapScreen 대규모 리팩토링 중 **주석조차 사라지고 `Intent(...)` 인스턴스 생성 한 줄만 남음**
- **시연 영향**: 신규 사용자 첫 권한 허용 시 BackgroundListenerService 영영 안 켜짐 → 내 위치·친구 마커 모두 안 보임. 앱 재시작해야 작동
- **권고 (2분 패치)**: [MapScreen.kt:437-444](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L437)의 정상 분기와 동일하게 `startForegroundService(intent)` 호출 추가

### C. 3팀 SettingsDrawer 빈 stub (신규)

- **위치**: [SettingsDrawer.kt:13-20](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/user/ui/SettingsDrawer.kt#L13)
- **현상**: MyPage → 설정 아이콘 → 드로어 열림 + 하단바 내림 UX는 정상. 그러나 내용은 `Text("드로어블 열림")` 한 줄. 로그아웃·계정 정보 등 없음
- **시연 영향**: 강사가 설정 아이콘 누르면 미완성 화면 노출. PR #193이 컨테이너 UX만 추가하고 내용물 작성 누락
- **권고**: 시연 시 설정 아이콘 누르지 않거나, 5분 안에 ProfileSection + LogoutButton 최소 작성

---

## 4. 🟠 시연 차단/회귀 위험 (팀별 핵심 1건씩)

### 1팀 — Health Connect 권한 ON이어도 Sleep 화면 데이터 0건 (신규, sentinel)
- **위치**: [SamsungHealthDataSourceImpl.kt:71](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/data/datasource/health/SamsungHealthDataSourceImpl.kt#L71) ↔ [SleepViewModel.kt:82](../../../teams-docs/1team/repo/app/src/main/java/com/snoffee/app/presentation/sleep/SleepViewModel.kt#L82)
- **원인**: DataSource가 `deepSleepRatio = 0` 고정 + ViewModel이 `deepSleepRatio > 0`만 필터 — **두 로직의 직접 모순**
- **시연 영향**: Health Connect 자동 연동(1팀 핵심 차별점)이 사실상 동작 안 함. "수면 추가하기"로 수동 입력해야만 표시
- **권고**: 시연 시 수면 기록 1~2건 사전 수동 입력 + "Health Connect 자동 연동은 다음 스프린트" 명시

### 2팀 — Chat 토픽 다이얼로그 cancel 불가 (5/22 R2-2 미해소, 5일째)
- **위치**: [ChatScreen.kt:367](../../../teams-docs/2team/repo/app/src/main/java/com/app/umma/presentation/chat/ChatScreen.kt#L367)
- **원인**: `onCancel = {}` 빈 람다 그대로
- **시연 영향**: 신규 계정으로 Chat 첫 진입 시 5개 미선택 상태에서 X/뒤로가기 불가 → 사용자가 갇힘
- **권고 (10분 패치)**: `onCancel = { viewModel.dismissTopicDialog() }` + ChatViewModel에 dismiss 메서드 추가

### 3팀 — POST_NOTIFICATIONS 런타임 요청 누락 (Android 13+)
- **위치**: [MapScreen.kt:436-443](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L436)
- **원인**: 권한 launcher 배열에 `POST_NOTIFICATIONS` 누락
- **시연 영향**: Android 13+ 단말에선 시스템 알림 일체 미수신. 알림 화면이 핵심 신기능 (PR #200)인데도 트레이엔 안 뜸
- **권고 (1분 패치)**: 권한 배열에 `Manifest.permission.POST_NOTIFICATIONS` 추가

---

## 5. 코호트 패턴 (5/22 → 5/27 비교)

### 패턴 1: 1팀·2팀 안정화 / 3팀 신규 회귀 ⭐⭐⭐

| 팀 | 5/22 🔴 | 5/27 🔴 | 변화 |
|---|---|---|---|
| 1팀 | 6 | 0 | **모두 해소** (권한 dead-end, Sleep SecurityException, 설정/수정 dead-end 등) |
| 2팀 | 5 | 0 | **모두 해소** (placeholder 3건, AudioPlayer 재진입, Chat 이탈 stopChat) |
| 3팀 | 6 | 3 | 일부 해소 (하드코딩 roomId/UID, 친구 삭제, onDestroy runBlocking) — 그러나 **PR #194·MapScreen 리팩토링이 신규 회귀 2건 야기** |

**해석**: 1·2팀은 시연 직전 안정화 단계, 3팀은 마지막 주말 대규모 리팩토링 중 핵심 경로 깨짐. **3팀에 시연 직전 코드 동결·리뷰 권고**가 필요.

### 패턴 2: 2팀 패키지 리네임 깔끔 수습 ⭐⭐

`com.example.umma` → `com.app.umma` 리네임이 한 차례 hotfix(470e07d, 0aba8ca, a40579f)로 완전 정리됨. 코드/manifest/build/google-services.json 전부 일치. 5/22에 우려했던 R import 누락·FileProvider authority 등 잔존 0건. **2팀 리팩토링 자세 sentinel** — 다른 팀이 배울 사례.

### 패턴 3: 1팀 "기능은 살아 있으나 데이터가 안 보임" 신규 클러스터

- O1 Health Connect → Sleep 0건
- O5 Sleep 삭제 → tombstone 남음
- O4 캘린더 점 UTC off-by-one
- Y1 Report 탭 — 5/22 발견된 더미 데이터가 5/27에도 그대로 (`ReportViewModel.kt:84-214` 더미 블록 + 실 UseCase 호출 4줄 모두 주석)

**해석**: 데이터 파이프라인 점검이 PR 리뷰에서 빠짐. 시연 시나리오 워크스루(클릭→화면 표시까지 끝까지 확인)가 필요.

### 패턴 4: 권한 런타임 요청 누락이 코호트 패턴 (3팀)

- POST_NOTIFICATIONS (Android 13+) 누락 (신규)
- ACCESS_BACKGROUND_LOCATION 누락 (5/22 R3-17 잔존)
- startForegroundService 호출 누락 (5/22 R3-2 악화)

→ 3팀 권한 흐름 전체 1회 점검 권고. 시연 단말 Android 버전 사전 확인 필수.

---

## 6. 팀별 즉시 조치 권고 (시연 시작 전, 우선순위 순)

### 3팀 (오늘 — 시연 직전 필수) ⭐⭐⭐

| 우선순위 | 항목 | 패치 시간 | 위치 |
|---|---|---|---|
| 1 | PTT `sendVoiceMessageDirect` 원복 (또는 콘솔 함수 배포 확인) | 5분 | PushToTalkViewModel.kt:108, BackgroundListenerService.kt:535 |
| 2 | MapScreen startForegroundService 호출 추가 | 2분 | MapScreen.kt:178-184 |
| 3 | POST_NOTIFICATIONS 런타임 요청 추가 | 1분 | MapScreen.kt:436-443 |
| 4 | SettingsDrawer 최소 콘텐츠 작성 (또는 데모 동선에서 제외) | 5분 | SettingsDrawer.kt |
| 5 | functions/index.js deploy 상태 콘솔 확인 | — | Firebase 콘솔 |

### 1팀 (오늘~내일)

| 우선순위 | 항목 | 위치 |
|---|---|---|
| 1 | 시연 전 수면 기록 1~2건 수동 입력 + "Health Connect 자동 연동 다음 스프린트" 안내 | 시연 시나리오 |
| 2 | Report 탭 5/22 더미 데이터 잔존 — 시연 동선에서 제외 또는 "샘플 데이터" 라벨 | ReportViewModel.kt:84-214 |
| 3 | Setting TimePicker AM/PM 강제 보정 제거 | SettingScreen.kt:352, L394 |
| 4 | CaffeinInputDialog 음료명 빈값 가드 | CaffeinInputDialog.kt:343 |
| 5 | 수면 삭제 진짜 Dao @Delete 추가 (또는 시연 동선에서 제외) | SleepDao.kt + DeleteSleepDataUseCase.kt |

### 2팀 (오늘)

| 우선순위 | 항목 | 위치 |
|---|---|---|
| 1 | ChatScreen Topic Dialog cancel 람다 + dismiss 메서드 (5/22부터 알려진 미해소) | ChatScreen.kt:367 + ChatViewModel |
| 2 | MyPage 모국어 설정 silent fail — 임시 버튼 숨김 | MyPageScreen.kt:95-97 |
| 3 | SRS 비로그인 silent return — hasSaveError UI 노출 | SrsStudyViewModel.kt:165 |
| 4 | Vico 3.0.3 `CartesianMarkerController.rememberToggleOnTap()` 실 빌드 컴파일 검증 | StatisticsMetricLineChartDialog |

---

## 7. 강사진 테스트 매니저 1줄 보고

> "**3팀 PTT 무전 송신 100% 실패 회귀 (PR #194 부작용) + MapScreen 권한 흐름 악화** — 5분 패치 가능. 1팀은 신규 크래시 없으나 Health Connect 자동 연동·Report 더미가 시연 차단, 2팀은 코호트 중 가장 안정적이며 Topic Dialog cancel 1건만 5일째 미해소. 보고서: `operation/reports/critical_review_260527/`"

---

## 8. 5/22 이후 해소 확인된 이슈 (학생 칭찬 자료)

### 1팀 ⭐⭐⭐
- R1-1 권한 dead-end → PR #81 ON_RESUME observer 패턴으로 해소
- R1-3 Sleep SecurityException → runCatching + hasHealthPermission 가드 + PermissionEmptyView
- R1-4 Setting 화면 신설 → UserProfileRepository 연동
- R1-5 수정 메뉴 dead-end → EditCaffeineUseCase 신설 + 다이얼로그 재사용
- R1-18 온보딩 입력값 미저장 → UserProfile 저장 연결 (메모리 기록 패턴 해소)

### 2팀 ⭐⭐⭐ (코호트 안정화 1위)
- R2-1 Placeholder 3개 화면 → Statistics/Correction/SrsStudy 모두 실구현 (471/559/213줄)
- R2-3 AudioPlayer 재진입 silent fail → `release()` → `stopPlaying()` 변경
- R2-6 Chat 이탈 stopChat 부재 → DisposableEffect 추가
- 패키지 리네임 hotfix 깔끔 수습

### 3팀 ⭐⭐
- R3-1 하드코딩 roomId 해소
- R3-4 PTT 워치 하드코딩 UID `Wy102dzyw4buC0V6YJuqxjtf6qA2` 제거
- R3-5 친구 삭제 미연결 해소
- R3-6 REQ 클릭 라우팅
- R3-12 onDestroy runBlocking 해소
- 위치 토글 양방향 일관성
- 부재중 음성 자동재생 차단 (PR #196)
- 친구 드로어 네비바 가려짐 fix

→ **3팀이 양호 항목도 가장 많음**. 다만 PR #194·MapScreen 리팩토링이 회귀 2건을 동시에 도입한 게 결정적 — 시연 직전엔 fix 외 PR 동결 권고.

---

## 9. 점검 한계 (사용자 인지 필요)

- **실 단말 빌드/실행 미수행** — 정적 분석만. 강사가 실 단말 실행 시 추가 발견 가능
- **콘솔 측 상태 미확인** — 특히 3팀 `functions/index.js` 콘솔 배포 여부는 repo로 판단 불가 (5/22 보고서 동일 한계 이월)
- **2팀 Vico 3.0.3 차트 API 호출 (`rememberToggleOnTap()` 등)** — 공식 문서에서 확인되지 않은 시그니처. 실 빌드 컴파일 검증 권장
- **테스트 계정/단말 OS 버전 미지정** — POST_NOTIFICATIONS, ACCESS_BACKGROUND_LOCATION 등은 단말 Android 버전에 따라 영향 다름

---

## 10. 산출물

- [00_summary.md](00_summary.md) — **본 문서 (통합 리포트)**
- [team1_runtime.md](team1_runtime.md) — 1팀 Snoffee 상세 (263줄)
- [team2_runtime.md](team2_runtime.md) — 2팀 Umma 상세 (260줄)
- [team3_runtime.md](team3_runtime.md) — 3팀 BBipit 상세 (250줄)

이전 점검: [../critical_review_260522/](../critical_review_260522/)
