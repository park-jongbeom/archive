# 3팀 BBipIt — 크리티컬 점검 결과 (2026-05-22)

> **점검 대상**: `c:\Users\ibebu\bootcamp6_final\archive\teams-docs\3team\repo\`
> **점검 범위**: app/ (모바일) + bbipit/ (워치) + functions/ (서버) + AndroidManifest 2종 + Gradle 2종
> **점검 관점**: 보조강사 / 강사진 테스트 직전 실행·심사·데이터 손실·보안 critical 우선
> **3팀 데드라인**: 2026-05-26 (개발 완료 임박, 4일 남음)

---

## 요약 — Top 3 critical (오늘 안에 반드시 고칠 것)

### 1. 🔴 **`functions/index.js`가 사실상 빈 파일** — 앱 핵심 동작 다수 즉시 실패
[`functions/index.js`](../../../teams-docs/3team/repo/functions/index.js)는 32줄 boilerplate (주석 + `setGlobalOptions` 1줄)만 존재. 그런데 클라이언트는 다음 Cloud Functions를 호출함:
- `createChatRoom`, `sendMessage`, `getMyChatRooms`, `markMessagesAsRead`, `getAllMessages`
- `sendVoiceMessage`, `markVoiceMessageAsRead`
- `getLiveStatusByUid`, `updateUserStatus`
- `requestFriend`, `acceptFriendRequest`, `declineFriendRequest`, `deleteFriend`, `getFriendProfileByUid`, `getMyAcceptedFriends`
- `markNotificationsAsRead`, `deleteNotifications`

→ 강사 테스트 시 채팅방 생성/메시지 전송/친구 추가/알림 읽음 처리/온라인 상태 갱신/음성 메시지 사이클 전체가 **`NOT_FOUND` 또는 `UNAUTHENTICATED`** 로 실패. **이 한 가지가 3팀 데모의 거의 모든 경로를 막음.**

### 2. 🔴 **firestore.rules / storage.rules 파일 자체가 repo에 존재하지 않음**
[`firebase.json`](../../../teams-docs/3team/repo/firebase.json)에 functions만 명시되어 있고, firestore/storage 룰 경로 자체가 없음. Glob 결과 `*.rules` 파일 0건.

→ 기본 룰(30일 만료 후 `if false`)이거나, 콘솔에서 설정한 룰이 있더라도 **repo·CI에서 검증·배포 안 됨**. 강사 테스트 시점:
- 친구 위치를 친구 아닌 사용자가 read 가능할 가능성
- DM `participants` 외부에서 read/write 가능할 가능성
- VoiceMessages 컬렉션을 임의 UID로 add 가능 (sendVoiceMessageDirect가 클라이언트 직쓰기)

### 3. 🔴 **PTT 워치 음성 송신 대상 UID가 코드에 하드코딩됨 (1명만 받음)**
[`BackgroundListenerService.kt:439`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt) `sendWatchAudioToServer()`:
```kotlin
val targetUid = "Wy102dzyw4buC0V6YJuqxjtf6qA2"
```
→ 워치에서 보낸 모든 무전 음성이 이 단일 UID로만 전송됨. 다른 사용자/친구에게 보낼 수 없음. 데모 도중 강사가 워치로 PTT 시도 시 즉시 발견됨.

---

## 점검 결과 (체크리스트 순서)

## 🔴 CRITICAL

### C3-1 foregroundServiceType="location" 선언
- **상태**: 🟢
- **근거**: [`app/src/main/AndroidManifest.xml:39`](../../../teams-docs/3team/repo/app/src/main/AndroidManifest.xml)
  ```xml
  <service
      android:name=".core.base.BackgroundListenerService"
      android:foregroundServiceType="connectedDevice|microphone|location|dataSync"
      android:exported="false"/>
  ```
  `FOREGROUND_SERVICE_LOCATION`, `FOREGROUND_SERVICE_MICROPHONE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `FOREGROUND_SERVICE_DATA_SYNC` 권한도 모두 선언됨 (라인 15~18).
- **권고**: 양호. 단, `targetSdk = 36`이므로 Android 14+ 정책 준수해야 함. `startForeground(...)` 호출 시 `ServiceInfo.FOREGROUND_SERVICE_TYPE_*` flag도 [`BackgroundListenerService.kt:596-607`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt)에서 동일하게 4개 type을 OR로 지정 → 정합성 확인.

### C3-2 foregroundServiceType="microphone" + FOREGROUND_SERVICE_MICROPHONE
- **상태**: 🟢
- **근거**: 동일 service의 foregroundServiceType에 `microphone` 포함. 권한 [`AndroidManifest.xml:16`](../../../teams-docs/3team/repo/app/src/main/AndroidManifest.xml).
- **권고**: 양호.

### C3-3 ACCESS_BACKGROUND_LOCATION 별도 runtime 요청 흐름
- **상태**: 🔴
- **근거**: AndroidManifest에 `ACCESS_BACKGROUND_LOCATION` 선언([라인 10](../../../teams-docs/3team/repo/app/src/main/AndroidManifest.xml))은 되어 있으나, **런타임 요청 로직이 코드에 없음**.
  [`MapScreen.kt:159-166`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)에서 요청하는 권한:
  ```kotlin
  requestMultiplePermissionsLauncher.launch(
      arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.BLUETOOTH_CONNECT,
          Manifest.permission.RECORD_AUDIO
      )
  )
  ```
  Grep 결과 `ACCESS_BACKGROUND_LOCATION` 문자열은 Manifest 1건뿐. Android 10+에서 백그라운드 위치는 별도 2단계 요청 필수 (Android 11+은 "항상 허용"이 시스템 설정 화면으로 분리됨).
- **권고**: FINE 권한 수락 후 백그라운드 권한 별도 요청 (rationale 다이얼로그 → 시스템 설정 안내). 또는 데모 시나리오를 "앱 켜진 상태"로 제한하고 "백그라운드 위치"는 stretch 기능으로 인정. **Android 10+ 기기에서 앱 백그라운드 상태로 전환 시 위치 업데이트가 무음 중단됨.**

### C3-4 BackgroundListenerService startForeground notification + type 일치
- **상태**: 🟢
- **근거**: [`BackgroundListenerService.kt:580-611`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt) `startForegroundServiceNotification()`에서 채널 생성·notification 빌드·`startForeground(id, notification, type)` 호출. Android 14+ 분기 처리 + `ForegroundServiceStartNotAllowedException` catch 후 dataSync로 fallback도 명시.
- **권고**: 양호. 다만 `onStartCommand`에서 `appLifecycleObserver`/repository 등이 Hilt로 주입되는데, **AppLifecycleObserver는 MainActivity가 만들어진 시점에야 ProcessLifecycleOwner에 등록**됨([`MainActivity.kt:79`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt)). FCM이 앱 콜드 스타트로 Service만 띄우는 경우(Activity 없이) lifecycle observer 미등록 상태가 됨 → `isAppInForeground` 항상 false. 큰 영향은 적으나 차후 견고화 포인트.

### C3-5 firestore.rules 파일 존재 — 친구 위치/DM 격리
- **상태**: 🔴
- **근거**: Glob `**/firestore.rules` 결과 0건. [`firebase.json`](../../../teams-docs/3team/repo/firebase.json)에 `firestore.rules` 경로 명시 없음 (functions만 정의).
- **권고**: **오늘 안에 `firestore.rules` 파일을 추가하고 `firebase.json`에 등록.** 최소 보호 룰 (강사 테스트 시점 데이터 노출 차단):
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{db}/documents {
      match /Users/{uid} {
        allow read: if request.auth != null;
        allow write: if request.auth.uid == uid;
        match /Friendships/{friendUid} {
          allow read, write: if request.auth.uid == uid;
        }
      }
      match /Live/{uid} {
        // 친구 검증은 Functions로 위임하거나 friendsList 배열로 단순화
        allow read: if request.auth != null;
        allow write: if request.auth.uid == uid;
      }
      match /DMs/{roomId} {
        allow read, write: if request.auth != null
          && request.auth.uid in resource.data.participants;
        match /Messages/{msgId} {
          allow read, write: if request.auth != null
            && request.auth.uid in get(/databases/$(db)/documents/DMs/$(roomId)).data.participants;
        }
      }
      match /VoiceMessages/{receiverUid}/Messages/{msgId} {
        // 클라이언트 직쓰기 차단 → Cloud Function 사용 강제 검토
        allow read: if request.auth.uid == receiverUid;
        allow create: if request.auth != null
          && request.resource.data.sender_id == request.auth.uid;
        allow update, delete: if false;
      }
      match /Notifications/{uid}/Notification/{nId} {
        allow read, write: if request.auth.uid == uid;
      }
    }
  }
  ```
  데모 직전이라면 최소 `request.auth != null` 만이라도 적용. 현재는 콘솔 룰 상태를 GitHub에서 확인 불가 → **콘솔 화면 캡처 + 강사진 보고 필요**.

### C3-6 Firebase Storage rules — 음성 파일 접근 제어
- **상태**: 🔴
- **근거**: Glob `**/storage.rules` 0건. [`VoiceRemoteDataSourceImpl.kt:64-71`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/voice/VoiceRemoteDataSourceImpl.kt)에서 `voices/{UUID}.m4a` 경로에 putFile + downloadUrl을 그대로 Firestore에 저장 (사실상 public URL):
  ```kotlin
  val fileName = "voices/${UUID.randomUUID()}.m4a"
  val voiceRef = storage.reference.child(fileName)
  return voiceRef.putFile(localFileUri).continueWithTask { task ->
      ...
      voiceRef.downloadUrl
  }.await().toString()
  ```
  Firebase Storage `downloadUrl`은 **토큰 포함 URL**이라 알기만 하면 누구나 접근 가능. 추가로 storage.rules가 없으면 콘솔 기본 정책(인증 필요) 또는 만료된 30일 정책에 의존.
- **권고**: `storage.rules` 추가 + `firebase.json`에 등록. 데모 직전이라면 `voices/` 경로는 최소 `request.auth != null`로 read 제한. 정식 가드라면 다음:
  ```
  match /voices/{file} {
    allow read: if request.auth != null;
    allow write: if request.auth != null
      && request.resource.size < 5 * 1024 * 1024
      && request.resource.contentType.matches('audio/.*');
  }
  ```
  현재 downloadUrl을 그대로 Firestore에 적재하는 구조 자체가 **친구 외 사용자 노출 위험** 존재 — 데모 후 receiver-uid 기반 경로로 재설계 권고.

### C3-7 AudioRecord/AudioTrack release 보장
- **상태**: 🟢
- **근거**: [`AudioRecorder.kt:74-82`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt) finally 블록에서 `recorder?.release()` 명시 + null 처리. [`AudioPlayer.kt:45-58`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/util/AudioPlayer.kt) MediaPlayer는 OnCompletion/OnError에서 release. `stopAudio()`도 명시적 release.
- **권고**: 양호. 단 한 가지 보강 — [`PushToTalkViewModel.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt)에서 `audioRecorder`가 **Singleton AudioRecorder 인스턴스를 Hilt로 주입**([`FirebaseModule.kt:78-84`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/di/FirebaseModule.kt)) → ViewModel onCleared 시 release 호출 없음. 녹음 중 화면 전환되면 leak 가능. `onCleared()` 추가 권고:
  ```kotlin
  override fun onCleared() {
      super.onCleared()
      runCatching { audioRecorder.stop() }
  }
  ```

### C3-8 Cloud Functions `functions/index.js` — admin SDK 인증 검증
- **상태**: 🔴 (전체 코드 없음 = 모든 호출 실패)
- **근거**: [`functions/index.js`](../../../teams-docs/3team/repo/functions/index.js) 전체 32줄, 의미있는 export 0건. 단지:
  ```js
  const {setGlobalOptions} = require("firebase-functions");
  setGlobalOptions({ maxInstances: 10 });
  ```
  [`package.json`](../../../teams-docs/3team/repo/functions/package.json)에 `firebase-admin: ^13.6.0`, `firebase-functions: ^7.0.0` 의존성은 있으나 **실제 함수가 작성되지 않음**.
  반면 클라이언트는 17개 Functions를 호출 (목록은 위 "요약 1번" 참조).
- **권고**: 두 가지 옵션 — (A) **콘솔에서 별도 deploy된 functions가 있는지 즉시 확인 필요.** repo와 디스크 상태가 다를 가능성. 강사진 보고용으로 콘솔 functions 목록 캡처 필요. (B) 정말 비어 있다면 **5/26 데드라인 내 17개 함수 작성 불가능** → 클라이언트 직쓰기로 전환 + firestore.rules 강화로 우회 (단 권한 검증 복잡도↑).
  **이건 3팀 진행 상태 실측에 직결되는 신호이므로 PM이 즉시 학생에게 "콘솔에 functions 배포돼 있어요?" 확인 권고.**

---

## 🟠 HIGH

### C3-9 Firestore snapshot listener unsubscribe
- **상태**: 🟢
- **근거**: 8개 파일에서 `addSnapshotListener` 사용 — 모두 검사 결과 적절한 해제 패턴:
  - [`VoiceRemoteDataSourceImpl.kt:60`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/voice/VoiceRemoteDataSourceImpl.kt): `awaitClose { subscription.remove() }`
  - [`ChatRemoteDataSourceImpl.kt:67,89`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/chat/ChatRemoteDataSourceImpl.kt): 동일
  - [`LiveStatusRemoteDataSourceImpl.kt:65`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/live/LiveStatusRemoteDataSourceImpl.kt): 동일
  - [`ChatRepositoryImpl.kt:74`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/repository/ChatRepositoryImpl.kt): 동일
  - [`NotificationRepositoryImpl.kt:93`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/repository/NotificationRepositoryImpl.kt): 동일
  - [`FriendRepositoryImpl.kt:44`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/repository/FriendRepositoryImpl.kt): `friendsListener?.remove()` (수동 lifecycle, `stopObservingFriends()`는 [BackgroundListenerService.kt:208-210](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt)의 onDestroy에서 호출).
- **권고**: 양호. 단 `FriendRepositoryImpl`은 callbackFlow 패턴이 아닌 manual `addSnapshotListener` → BackgroundListenerService onDestroy 외 lifecycle (예: 로그아웃, 사용자 전환)에서 해제 보장 필요. 현재 `stopObservingFriends()` 호출 경로는 단일 (Service onDestroy)이라 OK.

### C3-10 위치 업데이트 interval/displacement
- **상태**: 🟡
- **근거**: [`BackgroundListenerService.kt:500-509`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt):
  ```kotlin
  val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
      .setMinUpdateDistanceMeters(10f)
      .build()
  ```
  10초 + 10m. 모바일 백그라운드 + HIGH_ACCURACY는 배터리 부담 큼. 다만 짧은 데모용으론 적절.
- **권고**: 데모 후 BALANCED + 30초로 조정. 그 외에는 즉시 조치 불요.

### C3-11 FCM data-only 메시지 vs notification 메시지
- **상태**: 🔴
- **근거**: [`MyFirebaseMessagingService.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/notification/MyFirebaseMessagingService.kt) 전체 41줄, **`onMessageReceived(RemoteMessage)` 미구현**. 오직 `onNewToken`만 override.
  → 앱이 포그라운드일 때 FCM `notification` payload는 시스템 트레이에 표시 안 됨 + `data` payload는 처리되지 않음. 백그라운드면 시스템이 트레이에 표시는 하나 in-app banner 등 커스텀 UI 불가.
- **권고**: `onMessageReceived` 구현 추가 (notification payload 처리 + data 페이로드로 in-app banner emit). NotificationBannerHost가 있는데 FCM과 연결되어 있지 않은 상태. 데모용으론 Firestore 리스너로 신규 메시지 감지가 동작하므로 우선순위는 데모 후 가능.

### C3-12 MyFirebaseMessagingService 토큰 갱신 처리
- **상태**: 🟢
- **근거**: [`MyFirebaseMessagingService.kt:23-35`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/notification/MyFirebaseMessagingService.kt): `onNewToken` 시 `isAutoLogin()` 확인 후 `userRepository.updateProfile(fcmToken = token)`. 단 SupervisorJob coroutine scope.
- **권고**: 양호. 다만 신규 디바이스 첫 로그인 시 초기 토큰 강제 fetch (`FirebaseMessaging.getInstance().token.await()`)도 SignIn flow에 넣어두면 견고. 현재 다른 디바이스로 갈아탄 사용자의 초기 토큰 누락 가능.

### C3-13 Wear 모듈(bbipit) 패키지/signing 일치
- **상태**: 🟢 (signing 일치) + 🟡 (별도 signingConfig 미정의)
- **근거**: applicationId 양쪽 모두 `com.bbip.bbipit` ([`app/build.gradle.kts:29`](../../../teams-docs/3team/repo/app/build.gradle.kts), [`bbipit/build.gradle.kts:20`](../../../teams-docs/3team/repo/bbipit/build.gradle.kts)). versionCode 모두 1. signingConfigs 블록 grep 0건 → 양쪽 모두 default debug keystore (동일). 데모 시점엔 OK. namespace도 동일.
  bbipit minSdk=30, app minSdk=24 (워치는 Android 11+만 지원) — 페어링 가능.
- **권고**: 정식 배포 전 release용 signingConfig 통일 필요 (양쪽 동일 keystore). 데모 직전 조치 불요.

### C3-14 친구 추가 — 양방향 트랜잭션
- **상태**: 🟡 (서버 의존)
- **근거**: [`FriendRemoteDataSourceImpl.kt:80-85`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/friend/FriendRemoteDataSourceImpl.kt) `sendFriendRequest`/`acceptFriendRequest` 등 **모든 친구 관계 변경이 Cloud Functions httpsCallable로 위임**됨. 즉 클라이언트 직쓰기 0건 (양호한 설계).
  → 단 functions/index.js가 비어 있으므로(C3-8) 양방향 트랜잭션 자체가 동작하지 않음 = 진단 보류.
- **권고**: 서버 함수 작성 시 `db.runTransaction()` 으로 `Users/{a}/Friendships/{b}` + `Users/{b}/Friendships/{a}` 동시 write. 한쪽만 성공해 단방향 friend 상태 남는 것 방지.

### C3-15 워키토키 청크 분할/Storage 비용
- **상태**: 🟡
- **근거**: PTT는 전체 .m4a 파일 업로드 후 Firestore에 URL 1건 저장하는 방식 ([`VoiceRemoteDataSourceImpl.kt:64-71`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/voice/VoiceRemoteDataSourceImpl.kt)). 청크 분할 없음. 워치 PCM → 모바일 M4A 인코딩 → Storage 업로드 → URL → Firestore Add → 수신자 snapshotListener → MediaPlayer prepareAsync. 종단간 latency 1~3초 예상.
- **권고**: 실시간성 측면 좋진 않으나 데모 기능 입증엔 충분. 16kHz/16kbps HE-AAC 인코딩 ([`AudioRecorder.kt:31-37`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt)) 적절. **단 sample rate 16000Hz에 bitrate 16000bps는 HE-AAC 기준 매우 낮음** — 음성 명료도 떨어질 수 있음. 32kbps 권고 (그래도 데모용은 OK).

### **추가 발견** — 🔴 PTT 워치 수신자 하드코딩 (체크리스트 외 critical)
- **상태**: 🔴
- **근거**: [`BackgroundListenerService.kt:436-449`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt) `sendWatchAudioToServer()`:
  ```kotlin
  val senderUid = authRepository.getCurrentUserUid() ?: return@launch
  val targetUid = "Wy102dzyw4buC0V6YJuqxjtf6qA2"
  ```
  워치에서 PTT 한 음성이 항상 이 단일 UID로만 전송됨. 친구 선택 UI 없음.
- **권고**: 즉시 수정 필요. 워치에서 친구 선택 화면 → 선택된 UID를 메시지에 포함시켜 모바일로 송신 → 모바일이 그 UID로 sendVoice. 또는 마지막으로 모바일에서 PTT 누른 친구 UID를 캐시 후 재사용. 데모용으론 후자가 빠름.

### **추가 발견** — 🔴 App Check Debug provider만 설치 (production 무방비)
- **상태**: 🔴
- **근거**: [`MainActivity.kt:73-76`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt):
  ```kotlin
  FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
      DebugAppCheckProviderFactory.getInstance()
  )
  ```
  Build variant 분기 없이 항상 Debug provider. release 빌드에 그대로 들어감 + Google Maps key·Kakao key는 [`local.properties`](../../../teams-docs/3team/repo/local.properties)에서 BuildConfig로 노출.
  - **MAP_KEY**: `AIzaSy***Adn88` (현재 코드 manifestPlaceholders 경유 → manifest 라인 91)
  - **KAKAO_KEY**: `b20***6f5` (BuildConfig 노출 — [`build.gradle.kts:41`](../../../teams-docs/3team/repo/app/build.gradle.kts))
  → APK 디컴파일하면 두 키 모두 추출 가능. `local.properties`는 .gitignore에 포함되어 git에는 안 올라감(양호)이나 release APK엔 노출.
- **권고**: (1) Google Maps API key는 Google Cloud Console에서 **Android app package + SHA-1 fingerprint 제한** 즉시 설정 (이건 콘솔 작업). (2) Kakao key도 Kakao Developers Console에서 키 제한. (3) release variant는 `PlayIntegrityAppCheckProviderFactory` 사용. 데모 직전이라 (1)(2) 우선.

---

## 🟡 MEDIUM

### C3-16 Compose lifecycle (collectAsStateWithLifecycle 사용)
- **상태**: 🟡
- **근거**: 13개 화면 중 6개만 `collectAsStateWithLifecycle` 사용. 다음 화면들은 일반 `collectAsState`:
  - [`MainActivity.kt:90`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt) (chatUiState)
  - [`MapScreen.kt:71,72`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)
  - [`ChatDetailScreen.kt:100`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt)
  - [`ChatListScreen.kt:70,129`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatListScreen.kt)
  - [`NotificationScreen.kt:53,56,57,58`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationScreen.kt)
  - [`NotificationBannerHost.kt:23,24`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationBannerHost.kt)
  - [`VoicePlayerScreen.kt:33`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/base/VoicePlayerScreen.kt)
- **권고**: 화면 백그라운드 진입 시에도 Flow 수집 지속 → 배터리·네트워크 낭비. 일관성 위해 모두 `collectAsStateWithLifecycle`로 통일. 데모 후 정리해도 무방.

### C3-17 ProGuard/R8 (모델 직렬화 keep)
- **상태**: 🟢 (현재 비활성화로 영향 없음)
- **근거**: [`app/build.gradle.kts:44-51`](../../../teams-docs/3team/repo/app/build.gradle.kts):
  ```kotlin
  release {
      isMinifyEnabled = false
      proguardFiles(...)
  }
  ```
  bbipit도 동일. minify off라 직렬화 mangle 없음.
- **권고**: 정식 배포 전 minify 켜기 + Firestore DTO/`@Serializable` 클래스 keep 룰 필요. 데모용 release면 현 상태 유지.

### C3-18 다중 마커 시 지도 성능
- **상태**: 🟢
- **근거**: [`MapScreen.kt:233-267`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt) — `mapUiState.friendsStatuses.forEach`로 친구 수만큼 Marker 렌더. 키 기반 `remember` 적용, 좌표 변화 시 MarkerState 갱신 적절. 친구 수 데모 시점 < 10명 가정 시 성능 문제 없음.
- **권고**: 친구 수 50+면 clustering 도입 (`maps-compose-utils`). 데모 직전 조치 불요.

---

## Cloud Functions (`functions/index.js`) 분석

### 실제 코드 상태
[`functions/index.js`](../../../teams-docs/3team/repo/functions/index.js) 32줄 전부:
```js
const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

setGlobalOptions({ maxInstances: 10 });
// (이하 주석만)
```
- **export된 함수**: 0개
- **admin SDK 초기화**: 없음
- **`context.auth` / `request.auth` 검증**: 해당 없음

### 클라이언트가 호출하는 함수 목록 (17개)
| 함수명 | 호출 위치 |
|---|---|
| `createChatRoom` | `ChatRemoteDataSourceImpl.kt:36` |
| `sendMessage` | `ChatRemoteDataSourceImpl.kt:48` |
| `getMyChatRooms` | `ChatRemoteDataSourceImpl.kt:94` |
| `markMessagesAsRead` | `ChatRemoteDataSourceImpl.kt:113` |
| `getAllMessages` | `ChatRemoteDataSourceImpl.kt:121` |
| `sendVoiceMessage` | `VoiceRemoteDataSourceImpl.kt:35` |
| `markVoiceMessageAsRead` | `VoiceRemoteDataSourceImpl.kt:93` |
| `updateUserStatus` | `LiveStatusRemoteDataSourceImpl.kt:29` |
| `getLiveStatusByUid` | `LiveStatusRemoteDataSourceImpl.kt:76` |
| `requestFriend` | `FriendRemoteDataSourceImpl.kt:82` |
| `acceptFriendRequest` | `FriendRemoteDataSourceImpl.kt:48` |
| `declineFriendRequest` | `FriendRemoteDataSourceImpl.kt:38` |
| `deleteFriend` | `FriendRemoteDataSourceImpl.kt:92` |
| `getFriendProfileByUid` | `FriendRemoteDataSourceImpl.kt:29` |
| `getMyAcceptedFriends` | `FriendRemoteDataSourceImpl.kt:102` |
| `markNotificationsAsRead` | `NotificationRepositoryImpl.kt:55` |
| `deleteNotifications` | `NotificationRepositoryImpl.kt:104` |

리전: `asia-northeast3` (서울) 명시됨 ([`FirebaseModule.kt:56-57`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/di/FirebaseModule.kt)).

### 결론
**가설 A (콘솔에 별도 deploy됨)**: repo의 functions/index.js는 빈 boilerplate이지만, 학생이 로컬에서 develop branch 작업 중인 별도 파일을 콘솔에 배포해뒀을 가능성. **이 경우 git에 commit이 안 되어 PM이 추적 불가**. 즉시 학생에게 확인 + commit push 요구.
**가설 B (실제로 미구현)**: 데드라인 4일 + 17개 함수 작성·인증 검증·트랜잭션 = 비현실적. 클라이언트 직쓰기 + firestore.rules로 우회 권고.

## 추가 발견

### A1. 🟠 MainActivity onDestroy에서 runBlocking
[`MainActivity.kt:56-65`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt):
```kotlin
override fun onDestroy() {
    ...
    kotlinx.coroutines.runBlocking {
        liveStatusRepository.updateMyLiveStatus(...)
    }
    super.onDestroy()
}
```
- **문제**: 메인 스레드에서 네트워크 호출 동기 대기. ANR 위험.
- **권고**: `applicationScope.launch { ... }` 패턴으로 변경하거나, isOnline=false 갱신을 onStop으로 옮기고 LifeCycleManager가 처리하게 위임.

### A2. 🟡 위치 동기화 카운터 — Singleton 상태 변수
[`SyncMyLocationUseCase.kt:13`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/domain/usecase/SyncMyLocationUseCase.kt):
```kotlin
@Singleton
class SyncMyLocationUseCase ... {
    private var locationSyncCounter = 0
```
- **문제**: Singleton + 동시성 미고려 var. coroutine 다중 호출 시 race.
- **권고**: `AtomicInteger` 또는 Mutex. 데모용엔 정확도 문제 없음.

### A3. 🟢 `local.properties` git 비추적 (양호)
[`.gitignore:3,15`](../../../teams-docs/3team/repo/.gitignore) `local.properties` 명시. `app/google-services.json`도 ignore. 따라서 키가 git에 노출되진 않음 — 다만 빌드된 APK에는 포함 (위 App Check 항목 참조).

### A4. 🟡 hardcoded GitHub raw URL로 약관 받기
[`AuthRemoteDataSourceImpl.kt:191-200`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/auth/AuthRemoteDataSourceImpl.kt):
```kotlin
"https://raw.githubusercontent.com/LIKELION-Android-BOOTCAMP-6th/FinalProject-BBipit-BBip/refs/heads/develop/docs/terms.md"
```
- **문제**: `develop` branch가 사라지거나 repo private 전환 시 약관 로드 실패. Play 정책상 약관은 항상 표시 가능해야 함. 또한 `URL.readText()`로 cleartext인지 cipher인지 검증 없음 (raw.githubusercontent는 HTTPS라 OK).
- **권고**: 약관 텍스트를 asset에 포함 + fallback. 데모용엔 OK.

### A5. 🟡 `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` 권한 선언만 있고 사용처 미확인
manifest 라인 12-13 선언. `BLUETOOTH_CONNECT`는 [`MapScreen.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt) 권한 요청 묶음에 포함 + [`MobileMessageListenerService.kt:126`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/MobileMessageListenerService.kt)의 워치 상태 체크에 활용. 의도 명확.
- **권고**: `BLUETOOTH_SCAN`은 실제 사용처 grep 0건. 사용 안 한다면 제거 (불필요 권한 = Play 심사 시 정당화 요구).

### A6. 🟡 Compose `collectAsState` (lifecycle 미인식) 7건 — C3-16 참조

### A7. 🟢 callbackFlow + awaitClose 패턴 일관성
모든 snapshot listener가 `awaitClose { ... remove() }` 패턴 사용. listener leak 위험은 낮음.

### A8. 🟠 정상 로그아웃 시 BackgroundListenerService stop 경로 불확실
Logout flow를 grep으로 확인 못 함. BackgroundListenerService는 `manageSessionByState()`에서 isUserLoggedIn=false 시 `lifeCycleManager.stopSession()` 호출하지만 **Service 자체는 START_STICKY로 계속 실행** ([라인 156](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt)). 로그아웃 후에도 Service가 살아있어 알림 표시 + 위치 권한 사용. 사용자 혼란 + Play 심사 risk.
- **권고**: 로그아웃 핸들러에서 `context.stopService(Intent(context, BackgroundListenerService::class.java))` 호출.

### A9. 🟡 `ChatRepositoryImpl`와 `ChatRemoteDataSourceImpl` 둘 다 `observeChatRooms` 정의
[`ChatRepositoryImpl.kt:53`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/repository/ChatRepositoryImpl.kt) (rooms 컬렉션) 와 [`ChatRemoteDataSourceImpl.kt:53`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/chat/ChatRemoteDataSourceImpl.kt) (DMs 컬렉션) — **컬렉션명이 다름** (`rooms` vs `DMs`). Repository는 `rooms`를 직접 listen하지만 `sendMessage`/`getAllMessages`는 functions를 거쳐 다른 컬렉션을 다룰 가능성. 데이터 일관성 깨질 위험.
- **권고**: 컬렉션명 단일화 + 어느 한쪽 제거. ChatListViewModel이 실제로 어느 Flow를 구독하는지 확인 후 정리.

### A10. 🔴 `BackgroundListenerService` 단일 `private val scope = CoroutineScope(Dispatchers.IO)` (SupervisorJob 미사용)
[`BackgroundListenerService.kt:72`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt) `CoroutineScope(Dispatchers.IO)` — `SupervisorJob` 없이 생성됨. 단일 child 실패 시 전체 scope cancel → 위치·음성·워치 통신 모두 중단. `MyFirebaseMessagingService`는 `SupervisorJob()` 명시한 것과 대비됨.
- **권고**: `CoroutineScope(SupervisorJob() + Dispatchers.IO)` 로 변경. 5분 이내 1줄 수정.

### A11. 🟡 hard-coded sample rate/encoding 일치성
- 워치/모바일 PCM 인코딩 sample rate 16kHz로 일치 ([`BackgroundListenerService.kt:295`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt) + [`AudioRecorder.kt:35`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt))
- 워치 bitrate 64kbps (라인 297), 모바일 bitrate 16kbps (HE-AAC, 라인 36) — **불일치**. 모바일 PTT는 음질 낮음.
- **권고**: 모바일 bitrate도 32~64kbps로 통일.

---

## 점검 시 한계

1. **콘솔 측 상태 확인 불가**: firestore.rules / storage.rules / 배포된 Cloud Functions가 콘솔에는 있을 수 있음. repo만으론 판단 한계. **강사진 학생 면담 시 콘솔 캡처 요구 권고.**
2. **google-services.json 확인 안 함**: 사용자 지시로 read 금지. Firebase 프로젝트 ID·App ID·API key는 검증 못 했음.
3. **워치 모듈 코드 정독 미실시**: bbipit/ 모듈은 manifest + build.gradle만 확인. PTT 송신단/수신단 워치 코드(`base/WatchVoiceReceiverService.kt` 등) 미점검. 단 워치단 동작은 모바일 PTT 진단으로 간접 추정 가능.
4. **빌드 실제 성공 여부 미검증**: gradle build 실행 안 함. compileSdk 36 + Android Studio 최신 안정버전 호환 가능성은 보장 못 함 (단 코드 import는 정상 보임).
5. **실 데이터 흐름 (E2E) 미테스트**: 클라이언트 코드만 정독, Firebase 콘솔 실제 트래픽 미관찰.
6. **친구 위치 보안 검증**: `LiveStatusRepositoryImpl`이 `friendsList`를 friendRepository의 `myFriends`로 필터하고 있어 클라이언트 측 격리는 작동하지만, **악성 클라이언트가 다른 친구 UID로 직접 Firestore Live/{uid} document를 read하는 것은 firestore.rules 없이는 차단되지 않음**.

---

## PM 권고 사항 — 5/26 데드라인 대비 우선순위

### 오늘(5/22) 안에 반드시
1. **콘솔에서 functions 배포 상태 확인** (학생 면담 + 캡처). 비어 있으면 어느 함수가 필요하고 어느 함수는 클라이언트 직쓰기로 우회할지 즉시 결정.
2. **firestore.rules / storage.rules 작성 + repo 추가 + firebase.json 등록 + deploy**. 최소 `request.auth != null` 가드라도 추가.
3. **PTT 워치 수신자 하드코딩 UID 제거**. 친구 선택 UI 또는 마지막 모바일 PTT 친구 캐시.

### 5/23~5/24 (잔여 2일)
4. App Check release variant용 PlayIntegrity 분기 — 단, debug 모드만 사용하더라도 콘솔에서 App Check enforce를 끄면 동작은 함 (보안만 약함).
5. MyFirebaseMessagingService onMessageReceived 구현 (in-app banner 연결).
6. MainActivity onDestroy의 runBlocking 제거.
7. BackgroundListenerService scope에 SupervisorJob 추가 (1줄).

### 5/25 (데모 직전 점검)
8. release APK 빌드 시 Maps/Kakao 키 콘솔 제한 확인.
9. 로그아웃 → BackgroundListenerService stop 경로 추가.
10. 컬렉션명 단일화 (`rooms` vs `DMs`) 확인.
