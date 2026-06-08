# 박종범 테스터 — 3팀 BBipit 베타 배포본 점검 (2026-06-08)

> **대상**: 박종범 본인 (Note 10+ AOS12 / Z Fold 4 AOS16 병행)
> **소스 최신화**: `release/v1` @ `870ffd1` (origin/release/v1, #340 fix/release-v1-notification 머지본) — **현재 베타 배포 라인**
> **이전 가이드**: [TESTER_PARK_GUIDE_260601.md](./TESTER_PARK_GUIDE_260601.md) (78개 TC 본인 전용) — 본 문서는 그 후 1주일 변화 + 배포본 정적분석 + 웹 검증 결과를 **배포 블로커/실발생 오류 중심**으로 재정리한 것
> **점검 방식**: 코드 정적분석(read-only) + 공식 문서 교차검증. 코드 라인 근거 명시.

---

## 0. 1줄 결론

> **공개 베타 부적합. 단, "콘솔 2개 사실"(App Check enforcement / Firestore Rules 모드)만 확인되면 비공개 내부 테스트는 가능.** 5/27 메모리의 "마이크 FGS 크래시" 공포는 **해소됨**(PTT가 포그라운드 서비스가 아닌 인앱 `MediaRecorder`로 녹음) — 대신 **App Check 디버그 프로바이더가 release에 박혀** 백엔드 전체가 한 번에 죽을 수 있는 게 1순위 리스크.

**최신화로 확인된 긍정 변화 (5/27 → 6/08):**
- 알림 LazyColumn key 누락(알림_22) **수정됨** — [NotificationScreen.kt:166](../repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt#L166) `key = { it.id }` + `animateItem()`
- 지도 마커 index↔friend 불일치(지도_02/04/07) **수정됨** — [MapScreen.kt:585](../repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L585) `key(friend.uid)` 안정 키
- DM 방 중복 생성 → 서버(Cloud Function) `ALREADY_EXISTS` 처리로 정리
- 시크릿 외부화 정상 — Maps/Kakao 키가 git-ignored `local.properties`에 분리

---

## 1. 🔴 배포 블로커 (코드 근거 + 본인 단말 검증법)

### 🔴 B-01. Firebase **Debug** App Check 프로바이더가 release 빌드에 포함
- 근거: [MainActivity.kt:143-145](../repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt#L143)
  ```kotlin
  FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
      DebugAppCheckProviderFactory.getInstance()   // ← BuildConfig.DEBUG 가드 없음
  )
  ```
  + `app/build.gradle.kts`가 `firebase-appcheck-debug`를 `debugImplementation`이 아닌 **일반 `implementation`**으로 선언.
- **무슨 일이 벌어지나**: App Check **enforcement가 켜져 있으면** 배포 APK가 미등록 디버그 토큰을 발급 → **모든 `httpsCallable` 호출 + Firestore read + Storage 업로드가 거부**됨. 즉 친구/DM/무전/프로필/알림 **전부 네트워크 오류처럼 실패**. enforcement가 꺼져 있으면 App Check가 아무 보호도 안 함(이것도 release엔 부적합).
- **본인 단말 검증 (가장 먼저!)**: Note10+/Z Fold4에 **새로 설치** → 친구추가·DM·무전·프로필이 **전부** 네트워크/권한류 오류로 실패하면 이 토큰 거부를 의심. → **강사·팀에 "Firebase 콘솔 App Check enforcement 켜져 있는지" 1줄 확인 요청.** 켜져 있으면 이 빌드는 그대로 못 씀.
- 웹 근거: [App Check 디버그 프로바이더 — production 사용 금지](https://firebase.google.com/docs/app-check/android/debug-provider)

### 🔴 B-02. Firestore / Storage 보안 규칙 미배포 (데이터 개방 위험)
- 근거: `firebase.json`에 `functions` 키만 존재 — **`firestore`·`storage` 키 0건** (`grep -c firestore firebase.json` = 0). repo 어디에도 `firestore.rules`/`storage.rules` 없음. [PLAY_STORE_RELEASE_CHECKLIST 🔴-01](../../PLAY_STORE_RELEASE_CHECKLIST_260601.md) 그대로 미해소.
- 클라이언트가 **Functions를 우회해 Firestore/Storage를 직접 read/write**하는 핫패스가 존재 → 규칙이 유일한 방어선:
  - 음성: [VoiceRemoteDataSourceImpl.kt:60-103](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/voice/VoiceRemoteDataSourceImpl.kt#L60) — `VoiceMessages/{uid}/Messages` 리스너 + Storage `voices/{uid}/…` 직접 putFile
  - 알림: [NotificationRepositoryImpl.kt:79-84](../repo/app/src/main/java/com/bbip/bbipit/data/repository/NotificationRepositoryImpl.kt#L79) — `Notifications/{userId}/Notification` 리스너
  - 친구: [FriendRemoteDataSourceImpl.kt:57-62](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/friend/FriendRemoteDataSourceImpl.kt#L57) — `Users/{uid}/Friendships` 직접 get
- **위험**: 콘솔 룰셋이 "테스트 모드"라면 인증만 되면(혹은 무인증) **타 사용자의 음성·위치·알림·친구그래프 전부 read 가능** = 개인정보 유출. 코드만으론 확인 불가 → **콘솔 Rules 탭 직접 확인 필수.**
- **본인 단말 검증**: 2개 계정으로 로그인 후, A계정에서 B계정의 음성/알림 데이터가 흘러 들어오는지 관찰(테스트모드면 교차 노출 관측됨).

### 🟠 B-03. `functions/index.js`가 빈 boilerplate인데 앱은 콜러블 ~26개 의존
- 근거: [functions/index.js](../repo/functions/index.js) 전체 주석처리(`helloWorld` 예시뿐). 그러나 앱은 `getHttpsCallable`로 region `asia-northeast3`에서 다음을 호출: `requestFriend`, `acceptFriendRequest`, `deleteFriend`, `createChatRoom`, `sendMessage`, `markMessagesAsRead`, `sendVoiceMessage`, `markVoiceMessageAsRead`, `updateProfile`, `updateOnlineStatus`, `updateHeartbeat`, `getLiveStatusByUid`, `markNotificationsAsRead`, `createHistory`, `fetchNearbyHistory`, `deleteAccountData` 등.
- **정정**: 5/27에 의심했던 "Retrofit 커스텀 서버로 피벗"은 **사실 아님** — repo에 Retrofit/OkHttp 의존성 자체가 없음. **여전히 Cloud Functions 기반.** 즉 이 repo의 빈 index.js는 실배포 함수 소스가 **다른 곳**에 있다는 뜻 → 서버 검증로직(자기자신 친구추가 차단, 양방향 friends atomic write, 방 dedup)이 **이 체크아웃에 없어 검증 불가.**
- **크래시 연결고리**: 배포된 함수의 응답 shape가 앱 기대와 어긋나면 `result.data as Map<*,*>` 캐스팅이 `ClassCastException`. 예: [FriendRemoteDataSourceImpl.kt:29,38,48](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/friend/FriendRemoteDataSourceImpl.kt#L29)

### 🟠 B-04. 포그라운드 서비스 타입 — AOS14+ (Z Fold4 = AOS16) 주의
- 근거: 매니페스트 서비스가 `foregroundServiceType="location"`만 선언. `FOREGROUND_SERVICE_MICROPHONE/CONNECTED_DEVICE/DATA_SYNC` 권한·타입은 [AndroidManifest.xml:16-18,38-42](../repo/app/src/main/AndroidManifest.xml#L16) **주석 처리됨**.
- **좋은 소식 (크래시 공포 해소)**: PTT 녹음은 [AudioRecorder.kt](../repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt)의 인앱 `MediaRecorder` — **포그라운드 서비스가 마이크를 안 씀**. 따라서 앱-포그라운드 무전 경로는 `MissingForegroundServiceTypeException`을 **안 침**.
- **남은 🟠 위험 (Z Fold4 실테스트 대상)**: [BackgroundListenerService.kt:758-800](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L758)가 `startForeground(1, n, FOREGROUND_SERVICE_TYPE_LOCATION)` 호출. **AOS14+는 서비스 시작 시점에 위치 권한을 재확인** → 권한 없으면 `SecurityException`으로 서비스 제거/크래시. 코드가 `onStartCommand`에서 catch→`stopSelf`로 자기보호하지만, 그러면 **실시간 위치·무전 수신이 조용히 멈춤**.
  - 웹 근거: [FGS 타입 필수 — AOS14](https://developer.android.com/about/versions/14/changes/fgs-types-required), [백그라운드 FGS 시작 제한](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- **워치→폰 깨우기 경로**: [MobileMessageListenerService.kt:61-81](../repo/app/src/main/java/com/bbip/bbipit/core/base/MobileMessageListenerService.kt#L61)가 백그라운드에서 `startForegroundService` → AOS12+는 `ForegroundServiceStartNotAllowedException`(catch됨). 즉 **앱 백그라운드 시 "워치가 폰을 깨운다" 기능은 AOS12/16 양쪽에서 조용히 무동작.**

### 🟠 B-05. Maps/Kakao 키 제한 미확인 + release minify 미적용
- [local.properties](../repo/local.properties): `MAP_KEY=AIza…`, `KAKAO_KEY=…` — git-ignored로 분리(✅) 이지만 APK 매니페스트엔 평문으로 실림(Maps 불가피). **GCP 콘솔에서 package+release SHA-1로 제한 안 하면 키 추출·도용 가능.** [체크리스트 🔴-04](../../PLAY_STORE_RELEASE_CHECKLIST_260601.md)
- `isMinifyEnabled = false` (release) — R8 난독화 없음. 크래시 원인은 아니나(오히려 `as Map` 캐스팅 보호) 완전 역공학 가능 APK. 베타 허용, production 전 수정.

---

## 2. 🟠 기능 버그 / 크래시 리스크

| ID | 위치 | 증상 / 본인 단말 검증 |
|---|---|---|
| **F-01 중복로그인 차단 비활성** | [SignInViewModel.kt:76-95](../repo/app/src/main/java/com/bbip/bbipit/presentation/auth/viewmodel/SignInViewModel.kt#L76) | `checkDuplicateLogin()` + 다이얼로그 **주석 처리**. `fix/v1-duplicate-login` 작업이 이 빌드에선 무력. **양 단말 동시 로그인 → 차단 안 됨**(의도 확인 필요) |
| **F-02 PTT 짧은녹음 빈파일** | [PushToTalkViewModel.kt:56-122](../repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt#L56), [AudioRecorder.kt:55-92](../repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt#L55) | 1초 게이트는 UI 벽시계 추정 → 실제 인코딩 프레임 아님. ~1초 직후 `MediaRecorder.stop()`이 `RuntimeException` → `uri==null` → "녹음된 파일이 없거나 유효하지 않습니다". `stopRecording`의 `delay(500)` 중 재누름 시 recorder state desync. **본인: 1초 안팎 빠른 연타로 재현** (5/27 무전_05 잔존) |
| **F-03 무전 수신 = FGS 단일장애점** | [BackgroundListenerService.kt:809-852](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L809) | 무전 자동재생/배너 분기가 **FGS 살아있을 때만** 동작. [MyFirebaseMessagingService](../repo/app/src/main/java/com/bbip/bbipit/core/base/MyFirebaseMessagingService.kt)는 `onNewToken`만 처리 → **FCM data 핸들러 없음**. 즉 실시간 전달이 전부 FGS 내부 Firestore 리스너 1개에 의존. **본인: 앱 백그라운드/recents 스와이프 후 몇 분 뒤 타 단말서 무전 → 도착하는지** |
| F-04 위치공유 OFF 잔여추적 | [LiveStatusRepositoryImpl.kt:124-134](../repo/app/src/main/java/com/bbip/bbipit/data/repository/LiveStatusRepositoryImpl.kt#L124) | OFF 시 업로드는 멈춤(✅)이나 FGS의 `requestLocationUpdates`는 계속 + 상시 알림 유지 → 사용자 오해. 배터리/알림 동작 확인 |

---

## 3. 🧭 앱 방향성 기반 — 본인이 일부러 시도할 실발생 오류 시나리오

위치기반 소셜 + PTT + Firebase 앱의 특성상 **반드시 깨질 수 있는 지점**:

1. **App Check 킬스위치 (B-01)** — 새 설치 후 백엔드 전부 실패하면 토큰 거부 의심. **최우선.**
2. **Firestore 룰 개방 (B-02)** — 2계정 교차 데이터 노출 관측.
3. **Z Fold4(AOS16) 위치권한 세션 중 회수 (B-04)** — 실행→전체 허용→설정에서 위치 회수 → 상시 알림("Bipp-it 대기 중") 사라지고 실시간 위치/무전이 조용히 멈추는지.
4. **백그라운드 무전 전달 (F-03)** — 백그라운드/스와이프 단말에 무전 → 배너 오는지(설계상 와야 함, FGS 죽으면 실패).
5. **워치→폰 백그라운드 깨우기 (B-04)** — 폰 백그라운드 시 워치 `check_phone_status` → AOS12+에서 조용히 실패 예상.
6. **PTT 1초 안팎 연타 (F-02)** — 빈 파일/전송 실패.
7. **위치 영구거부 rationale** — AOS13+ 2회 연속 거부 후 3회째 시스템 다이얼로그 안 뜸(지도_06). 사용자 안내 멈추는지.
8. **마커/프로필 무이미지 친구** — 프로필 이미지 없는 친구가 fallback 마커로 렌더되는지, 빠르게 움직이는 친구 마커가 깜빡이지 않는지.
9. **계정 탈퇴** — `deleteAccountData` 콜러블 후 FCM 토큰 정리 + FGS 자기종료([uid==null 시 stop](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L171)) 동작 확인.
10. **Maps 키 미제한 (B-05)** — release SHA가 화이트리스트 안 되면 **지도 자체가 백지**로 뜸. 지도 빈 화면이면 키 제한/SHA 확인.

---

## 4. ✅ 회귀 안전(수정 확인) — 재검증만

| 항목 | 상태 |
|---|---|
| 알림 순서 튐(알림_22) | [NotificationScreen.kt:166](../repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt#L166) 안정 키 적용 → 수정 |
| 지도 마커 친구 불일치(지도_02/04/07) | [MapScreen.kt:585](../repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L585) UID 키 → 수정 |
| 마이크 FGS 크래시(5/27 공포) | PTT가 인앱 MediaRecorder → 해당 크래시 회피 |
| DM 방 중복 | 서버 `ALREADY_EXISTS` 처리 |
| 시크릿 노출 | local.properties git-ignored 분리 |

---

## 5. 본인 단말 우선순위 (6/08 배포본 기준)

| 순위 | 항목 | 이유 |
|---|---|---|
| 🔥 1 | **B-01 App Check 킬스위치** | 백엔드 전체를 한 번에 죽일 수 있는 최대 리스크. 새 설치 후 친구/DM/무전/프로필 전수 실패 여부 |
| 🔥 2 | **B-04 Z Fold4 위치권한 회수** | 본인만 가진 AOS16 폼팩터 + FGS 단일장애점 결합 |
| 🔥 3 | **F-03 백그라운드 무전 전달** | 시연 핵심 기능의 실사용 신뢰성 |
| ⭐ 4 | **B-02 룰 개방 교차노출** | 개인정보 사고 직결 |
| ⭐ 5 | **F-02 PTT 1초 연타** | 5/27 무전_05 잔존 회귀 |
| 6 | F-01 중복로그인 / 탈퇴 / Maps 키 | 정책·일관성 |

> ⚠️ **콘솔 확인 2건은 코드로 불가** — 강사·팀에 (a) App Check enforcement ON/OFF (b) Firestore Rules 현재 모드 를 반드시 물어볼 것. 이 둘이 본인 단말 테스트 결과 해석의 전제.

---

## 변경 이력
| 일자 | 변경 |
|---|---|
| 2026-06-08 | release/v1 @ 870ffd1 배포본 최신화 + 정적분석/웹검증 기반 블로커·실발생오류 재정리 |
