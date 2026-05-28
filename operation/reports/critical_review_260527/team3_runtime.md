# 3팀 BBipit 화면 동작 오류 점검 (2026-05-27)

> 베이스: develop @ 36e6afd, 5/22 이후 +1184/-605 (50 commits)
> 관점: 강사진 실 단말 테스트 시 만나는 크래시/무반응/오작동 (보안/구조 제외)
> 5/22 알려진 이슈는 제외 — 신규/해소/잔존만 기록
> 점검 범위: `app/`(모바일) + `bbipit/`(워치) + `functions/`
> 빌드 실행 안 함 — 정적 분석만

---

## 0. 요약 — 시연 5분 안에 발견될 Top 3 (5/22 이후 신규)

1. **R3-N1 🔴 PTT 모바일·워치 무전 전송이 NOT_FOUND로 100% 실패 (회귀)** — 5/22엔 `sendVoiceMessageDirect`로 Firestore 직쓰기였는데 5/26 PR #194에서 **클라 직쓰기 코드 전체 주석 + `sendVoiceMessage` Cloud Function 호출로 변경**. functions/index.js는 여전히 빈 상태 → 모든 무전 송신이 NOT_FOUND. PTT 시연 경로가 완전 차단됨.
2. **R3-N2 🔴 MapScreen 권한 launcher의 startForegroundService가 여전히 미호출 (잔존, 5/22 R3-2 그대로)** — 5/22 보고에서 "주석 4줄 해제" 권고했으나, 5/22~5/27 사이 MapScreen 대대적 리팩토링 중 `Intent(context, BackgroundListenerService::class.java)` 한 줄만 남고 startForegroundService 호출이 **삭제됨** (전보다 악화). 권한 첫 부여 시 서비스 영영 안 켜짐. 앱 재시작해야 작동.
3. **R3-N3 🟠 SettingsDrawer가 "드로어블 열림" 한 줄짜리 빈 stub** — 마이페이지에서 설정 아이콘 누르면 드로어가 열리지만 안에 메뉴/계정 정보/로그아웃 등 아무것도 없음. (PR #193 e8bdf87)

---

## 🔴 즉시 크래시 위험 / 핵심 데모 차단 (신규)

### R3-N1. PTT 모바일·워치 무전 송신 100% 실패 (신규 회귀, Critical)
- **시나리오 A**: 강사가 친구 마커 클릭 → 무전 다이얼로그 → 무전 버튼 길게 누름 → 손 뗌. **모바일 PTT**
- **시나리오 B**: 강사가 워치에서 친구 다이얼로그 → 워치 PTT 버튼 길게 누름. **워치 PTT**
- **현상**: 녹음·업로드까지는 정상. 마지막 송신 단계에서 Cloud Functions `sendVoiceMessage` 호출 → **NOT_FOUND** → `VoiceUiState.error = "NOT_FOUND..."` 토스트만 뜸. 상대방은 무전을 절대 수신하지 못함.
- **위치**: 
  - PTT 모바일: [`PushToTalkViewModel.kt:108`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt#L108) — `voiceRepository.sendVoiceMessage(targetUid, url, correctedDuration)`
  - PTT 워치: [`BackgroundListenerService.kt:535`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L535) — `voiceRepository.sendVoiceMessage(targetUid, url, duration)`
  - RemoteDataSource: [`VoiceRemoteDataSourceImpl.kt:30-38`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/voice/VoiceRemoteDataSourceImpl.kt#L30-38)
- **근거**:
```kotlin
// VoiceRemoteDataSourceImpl.kt:30-38
override suspend fun sendVoiceMessage(receiverId: String, voiceUrl: String, duration: Int): Boolean {
    val data = hashMapOf(
        "receiverId" to receiverId,
        "voiceUrl" to voiceUrl,
        "duration" to duration
    )
    val result = functions.getHttpsCallable("sendVoiceMessage").call(data).await()
    return (result.data as? Map<*, *>)?.get("isOnline") as? Boolean ?: false
}
```
```kotlin
// VoiceRepositoryImpl.kt:91-109 — 5/22 fallback이었던 client direct 쓰기는 전체 주석 처리됨
//    override suspend fun sendVoiceMessageDirect(...) { ... }
```
- **functions/index.js 상태**: 여전히 빈 파일 — `helloWorld` 주석만 있고 `exports.sendVoiceMessage` 없음. 5/22 보고서의 가설 B(콘솔에 별도 배포)에 100% 의존하는 상태가 됨.
- **회귀 원인**: PR #194 (b201e14) "음성 메세지 API잘못 호출하고 있던 부분 수정" 커밋이 `sendVoiceMessageDirect` → `sendVoiceMessage` 일괄 변경. 의도는 "잘못된 API 호출 정정"이었지만 functions가 비어 있는 상태에선 **모든 PTT 실패**로 귀결.
- **권고 (오늘 5분 패치)**: VoiceRepositoryImpl/VoiceRemoteDataSourceImpl의 `sendVoiceMessageDirect` 주석 해제 + PushToTalkViewModel.kt:108, BackgroundListenerService.kt:535에서 `sendVoiceMessage` → `sendVoiceMessageDirect(senderUid, targetUid, url, duration)`로 원복. 시연 직전 1회만 적용. 또는 콘솔에 `sendVoiceMessage` 함수 배포 완료 확인.

### R3-N2. MapScreen 권한 launcher의 startForegroundService 호출 누락 (5/22 R3-2 잔존 + 악화)
- **시나리오**: 신규 사용자가 처음 SignIn → Map 진입 → 권한 다이얼로그에서 모두 "허용" 누름. **그래도 친구 마커도, 내 위치도 안 보임.**
- **현상**: 로그엔 "권한 승인됨 -> BackgroundListenerService 가동" 찍힘. 그런데 BackgroundListenerService는 **시작되지 않음**. 앱 종료 후 재진입해야만 `LaunchedEffect(Unit)` 두 번째 분기(이미 권한 보유)가 작동.
- **위치**: [`MapScreen.kt:178-184`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L178-184)
- **근거**:
```kotlin
if (fineLocationGranted || coarseLocationGranted || bluetoothConnectGranted) {
    Log.d(TAG, "권한 승인됨 -> BackgroundListenerService 가동")
    Intent(context, BackgroundListenerService::class.java)   // ← 인스턴스만 생성하고 끝
} else {
    Toast.makeText(context, "서비스 이용을 위해 위치 및 블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
}
```
- 5/22 보고서엔 `startForegroundService(intent)` 호출이 주석 처리되어 있었음. 이번 점검에선 **주석조차 사라지고 Intent 생성 한 줄만 남음** — 리팩토링 중 의도치 않게 핵심 코드 삭제 (악화).
- **권고 (오늘 2분 패치)**: 437-444라인의 정상 분기와 동일하게 처리:
```kotlin
val intent = Intent(context, BackgroundListenerService::class.java)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
else context.startService(intent)
```

### R3-N3. SettingsDrawer가 빈 stub (신규)
- **시나리오**: 강사가 MyPage 진입 → 우측 상단 톱니바퀴(설정) 아이콘 클릭.
- **현상**: 드로어는 열리고 하단바도 정상으로 내려감 (의도대로). 그러나 드로어 내용이 "드로어블 열림" 텍스트 한 줄. **로그아웃·계정 정보·환경설정 메뉴 등 모든 항목 없음**. 사용자가 정상 진입했는지 의심.
- **위치**: [`SettingsDrawer.kt:13-20`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/user/ui/SettingsDrawer.kt#L13-20)
- **근거**:
```kotlin
@Composable
fun SettingsDrawer(email: String, LoginType: String, onClose: () -> Unit, modifier: Modifier) {
    ModalDrawerSheet(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("드로어블 열림")
        }
    }
}
```
- 파라미터(`email`, `LoginType`, `onClose`) 모두 전달되지만 미사용. PR #193 (1e89736)에서 드로어 열림/하단바 내림 UX는 추가했지만 안의 내용물 작성 누락.
- **권고 (시연 직전 결정)**: 시연 시 설정 아이콘 누르지 않거나, 5분 안에 ProfileSection + LogoutButton 정도만 추가.

---

## 🟠 무반응 / dead-end / 회귀

### R3-N4. POST_NOTIFICATIONS 런타임 요청 누락 — Android 13+ 알림 0개 (신규 발견)
- **시나리오**: Android 13(API 33) 이상 단말로 시연. SignUp → Map 진입 → 권한 다이얼로그.
- **현상**: 권한 다이얼로그에 위치/블루투스/마이크만 표시 — **알림 권한 요청 없음**. 사용자가 명시 거부한 적 없는데 시스템 알림이 절대 안 뜸. NotificationBanner도 트레이엔 안 나타남(in-app 배너만 보임).
- **위치**: 
  - 매니페스트엔 선언됨: [`AndroidManifest.xml:19`](../../../teams-docs/3team/repo/app/src/main/AndroidManifest.xml#L19) `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`
  - 런타임 요청 누락: [`MapScreen.kt:436-443`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L436-443)
- **근거**:
```kotlin
requestMultiplePermissionsLauncher.launch(
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.RECORD_AUDIO
        // POST_NOTIFICATIONS 누락
    )
)
```
- **권고 (오늘 1분 패치)**: 위 배열에 `Manifest.permission.POST_NOTIFICATIONS` 추가. 강사 단말이 Android 12 이하면 영향 없음.

### R3-N5. ACCESS_BACKGROUND_LOCATION 런타임 요청 잔존 (5/22 R3-17 그대로)
- **현상**: 매니페스트엔 선언됐으나 런타임 요청 launcher에 없음. 백그라운드 위치 추적이 정책상 막힘 → 친구가 내 위치를 백그라운드 상태에서 못 봄.
- **위치**: [`MapScreen.kt:436-443`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L436-443) (포그라운드 권한과 동시에 요청 중)
- **권고**: 포그라운드 위치 허용 후 별도 다이얼로그로 "항상 허용" 요청 (Android 정책 — 동시 요청 불가).

### R3-N6. MyPage 서버 호출 실패 시 정보 영구 빈 상태 (신규)
- **시나리오**: 네트워크 오프라인 상태로 MyPage 진입 / 또는 `getMyProfile` Cloud Function 실패.
- **현상**: `nickname = "불러오는 중..."` 영구 표시. profileImageUrl 빈 상태 → 기본 아이콘. UniqueID 빈 텍스트. 토스트만 1회 뜨고 사라짐. **재시도 버튼 없음** — 화면을 떠났다 다시 들어가야 재시도.
- **위치**: [`MyPageViewmodel.kt:97-133`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/user/viewmodel/MyPageViewmodel.kt#L97-133)
- **근거**:
```kotlin
.onFailure { exception ->
    Log.e("프로필 받아오기 실패", exception.message.toString())
    _uiState.update { state ->
        state.copy(
            isLoading = false,
            toast = exception.message    // ← 토스트만, UI에 에러 상태 표시 없음
        )
    }
}
```
- LaunchedEffect도 `Unit` key라 1회만 실행. 사용자가 "다시 시도"할 방법 없음.
- **권고 (오늘 5분 패치)**: 실패 시 빈 화면 대신 "프로필을 불러올 수 없습니다. [다시 시도]" 버튼 UI 추가.

### R3-N7. 알림 클릭 핫스타트(앱이 켜져 있는 상태)에서 무한 대기 가능 (신규)
- **시나리오**: 강사 앱이 켜진 상태로 Map 화면 → 외부에서 푸시 도착 → 시스템 트레이 알림 클릭.
- **현상**: PendingIntent의 `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK` 조합으로 task 재시작. MainActivity.onNewIntent → `pendingNotificationIntent` 갱신. **그러나 NavHost startDestination은 이미 결정된 상태** → BBipItNavigation의 LaunchedEffect 안 while 루프가 "현재 라우트가 Notification인지" 대기:
```kotlin
// Navigation.kt:76
while (navController.currentBackStackEntry?.destination?.route?.contains("Notification") != true) {
    delay(50)
}
```
- 현재 화면이 Map인데 자동 navigate 코드 없음 → **무한 대기**. 알림 클릭해도 ChatRoom으로 안 가고, 사용자 입장에선 "알림 클릭이 작동 안 함".
- **위치**: [`Navigation.kt:71-89`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/navigation/Navigation.kt#L71-89), [`MainActivity.kt:67-71`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt#L67-71)
- **참고**: 콜드 스타트(앱 종료 상태)는 정상 작동 — startDestination = Notification으로 시작 → LaunchedEffect 통과 → ChatRoom navigate.
- **권고 (오늘 5분 패치)**: 핫스타트 케이스를 위해 `pendingNotificationIntent` 변경 시 명시적으로 `navController.navigate(Routes.Notification)` 먼저 호출.

### R3-N8. 위치 공유 토글 OFF 시 친구 마커 즉시 사라지지 않을 가능성 (구조 점검 필요)
- **시나리오**: 강사가 Map 진입 → 친구 위치 확인 → 위치 공유 토글 OFF.
- **현상 (정상)**: `LiveStatusRepositoryImpl.observeFriendsLiveStatus`가 `_isLocationSharingEnabled` Flow를 combine으로 결합 → OFF 시 emptyList → `_friendsLiveStatusFlow.value = emptyList()` → MapContent의 `mapUiState.friendsStatuses`가 빈 리스트 → forEach noop → 친구 마커 사라짐 ✅.
- **잠재 이슈**: `observeFriendsLiveStatus`가 호출되어 launchIn된 시점이 한 번뿐일 수 있음 ([BackgroundListenerService.kt:544-557](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L544-557)에서 호출). 만약 서비스가 죽었다 살아나거나 호출 자체가 누락되면 토글 effect 없음. R3-N2(서비스 미가동)와 결합되면 토글이 작동 안 함.
- **재현 필요**: 단순 시연만으론 정상 보이지만, "서비스 미가동 + 토글 OFF + 토글 ON" 시퀀스에서 회귀 가능. 점검은 R3-N2 해결 후 재검증.

### R3-N9. 알림 화면 헤더의 개발자 테스트 버튼 노출 (5/22 R3-15 잔존)
- **위치**: [`NotificationScreen.kt:338-340`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt#L338-340)
- **현상**: 일반 사용자 화면에 `[+ DM 추가]` `[+ 무전 추가]` `[+ 친구 추가]` 텍스트 버튼 노출. 클릭 시 가짜 알림 생성.
- **권고**: 시연 직전 라인 주석 처리 또는 BuildConfig.DEBUG 분기.

### R3-N10. 무전 알림 카드 클릭 시 실제 재생 흐름 부재 (5/22 R3-16 잔존)
- **위치**: [`NotificationScreen.kt:159-170`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt#L159-170)
- **현상**: WALKIE 알림 클릭 → "무전을 확인합니다." 토스트만 표시. expiredVoiceIds에 추가만 됨. 실제 음성 재생 없음. 만료됨 표시로 회색 처리.

---

## 🟡 잘못된 데이터 표시 / UX 결함

### R3-N11. 히스토리 작성 시 빈 장소 이름 자동 치환 (의도된 동작이나 시연 충격)
- **시나리오**: 히스토리 작성 → 카테고리 선택 → **장소 이름 비워둠** → 내용 입력 → 저장.
- **현상**: PR #197 (46293e4)로 placeName 초기값 제거됨. 빈 상태로 제출하면 → [MapScreen.kt:373](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L373) `placeName.ifEmpty { "알 수 없음" }` → 지도 마커에 "[카테고리] 알 수 없음" 표시됨.
- **근거**:
```kotlin
historyViewModel.createNewHistory(
    category = selectedCategory,
    placeName = placeName.ifEmpty { "알 수 없음" },   // ← 빈 입력은 "알 수 없음"으로 강제 치환
    ...
)
```
- 의도는 명확하나 강사가 빈 칸 채워서 저장하면 "알 수 없음"이 표시됨 → 데모 영상에 노출되면 미완성으로 보임.
- **권고**: 강사 시연 시 placeName 입력 안내 또는 "장소 이름을 입력해 주세요" validation 추가.

### R3-N12. ChatList isOnline 항상 false (5/22 R3-24 잔존)
- **위치**: ChatListViewmodel `isOnline = false` 하드코딩. 디자인엔 온라인 점 있으나 항상 회색.

### R3-N13. FCM onMessageReceived 미구현 잔존 (5/22 C3-11/R3-18 그대로)
- **위치**: [`MyFirebaseMessagingService.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/notification/MyFirebaseMessagingService.kt) — `onNewToken`만 override. `onMessageReceived` 없음. PR #200 (notification)에서 추가될 거라 기대했으나 NotificationViewModel + Banner UX만 개선됨.
- **현상**: 앱 종료 상태에서 FCM data payload 도착 → 처리 핸들러 없음 → 무반응. 시스템 트레이는 FCM notification payload만으로 표시.

### R3-N14. ChatRepository.joinChatRoom 인터페이스 삭제 (구조 변경, 영향 확인 필요)
- **위치**: PR #196 (c73c8b1) — `ChatRepository.kt`에서 `fun joinChatRoom(roomId, receiverId, navController)` 삭제됨.
- **현상**: 인터페이스에선 사라졌지만 ChatRepositoryImpl에 실제 구현이 있는지/사용처는 어디인지 미점검. 컴파일 에러 가능성. 단 develop @ 36e6afd 빌드 상태이므로 다른 곳에 영향은 없을 것으로 추정. 5/22 R3-1 해결의 부산물로 짐작.

### R3-N15. ChatDetailScreen receiverId가 빈 문자열일 때 처리 (5/22 R3-1 해소 + 잔존 코너 케이스)
- **위치**: [`ChatDetailScreen.kt:100-102`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt#L100-102) — 하드코딩 roomId 해소 ✅. 그러나 `receiverId = route?.receiverId ?: ""` 빈 문자열 fallback. NotificationScreen에서 senderId가 null이면 빈 문자열 전달됨([NotificationScreen.kt:157](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt#L157)).
- **현상**: receiverId 빈 채로 메시지 전송 시도 → 서버에서 NOT_FOUND or validation error. 토스트는 뜨지만 사용자 입장에서 "왜 메시지 안 가지" 혼란.

### R3-N16. 워치 PTT 워치 측 권한 안내 없음 (잔존)
- **시나리오**: 워치에서 마이크 권한 미허용 상태로 PTT 길게 누름.
- **현상**: `WatchAudioSender.initAudioRecord()`가 `SuppressLint("MissingPermission")` 처리 + `AudioRecord(MIC, ...)` 직접 생성. 권한 없으면 STATE_INITIALIZED 실패 → IllegalStateException → catch 블록에서 "오디오 스트리밍 전반적 실패" 로그만. 사용자에게 안내 없음.
- **위치**: [`WatchAudioSender.kt:226-241`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/util/WatchAudioSender.kt#L226-241)

---

## 5/22 이후 해소 확인된 이슈 ✅

- **R3-1 ChatDetailScreen 하드코딩 roomId/receiverId** — ✅ 해소 ([ChatDetailScreen.kt:100-102](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt#L100-102) `route?.roomId ?: ""` + `route?.receiverId ?: ""`로 정상 추출). Routes.ChatRoom에 receiverId 추가됨.
- **R3-4 워치 PTT 송신 대상 UID 하드코딩** — ✅ 해소. `WatchAudioSender.kt:130`에서 channel path를 `/audio_stream/$targetUid`로 동적 생성. BackgroundListenerService.kt:481 `channel.path.substringAfter("$PATH_AUDIO_STREAM_PREFIX/", "")`로 파싱. 친구별 무전 정상 분기. 단 R3-N1에 의해 결국 NOT_FOUND로 막힘.
- **R3-5 FriendListScreen 친구 삭제 버튼 미연결** — ✅ 해소. `viewModel.deleteFriend(...)` 호출 정상 연결됨 ([FriendListScreen.kt:157-170](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/ui/FriendListScreen.kt#L157-170)). 공통 다이얼로그 사용.
- **R3-6 NotificationScreen REQ 클릭 시 FriendRequestList 이동 누락** — ✅ 해소. `navController.navigate(Routes.FriendRequestList)` 추가됨 ([NotificationScreen.kt:172](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt#L172)).
- **R3-12 MainActivity onDestroy runBlocking** — ✅ 해소. [MainActivity.kt:63-65](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt#L63-65) onDestroy가 super 호출만 남음. ANR 위험 사라짐.
- **위치 공유 토글 양방향** — ✅ 해소 ([LiveStatusRepositoryImpl.kt:69-111](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/repository/LiveStatusRepositoryImpl.kt#L69-111)). 토글 OFF 시 친구 목록 emptyList로 떨어져 자기도 친구도 안 보임. (0fe4249 정상 적용)
- **부재중 음성 자동 재생 안 함** — ✅ 해소 (c73c8b1). `isInitial` 플래그를 VoiceMessageDto/Repository에 전파해 첫 snapshot은 emitMobileVoiceEvent skip. 접속 시 누적된 부재중 메시지가 자동 재생되지 않음.
- **친구 목록 드로어 네비바 가려짐** — Popup + clippingEnabled=false로 해소 ([MapScreen.kt:311-358](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt#L311-358)).
- **세팅 드로어 열림 시 하단바 내림** — ✅ UX는 정상 동작. 다만 R3-N3로 드로어 내용물이 비어 있음.

## 5/22 이후 해소되지 않은 (잔존) 이슈

- **functions/index.js 빈 파일** — ❌ **잔존**. 5/22 이후 새 함수 0건 추가. 신규 R3-N1 회귀와 결합되어 영향 확대됨. (helloWorld 주석만 있음)
- **MyFirebaseMessagingService.onMessageReceived** — ❌ **잔존** (R3-N13). PR #200으로 추가될 거라 기대했으나 알림 ViewModel/Banner UX만 개선됨.
- **MapScreen 권한 launcher startForegroundService 누락** — ❌ **잔존 + 악화** (R3-N2). 5/22엔 주석이라도 있었는데 이번엔 라인 자체 삭제됨.
- **ACCESS_BACKGROUND_LOCATION 런타임 요청** — ❌ **잔존** (R3-N5).
- **ChatList isOnline 항상 false** — ❌ **잔존** (R3-N12).
- **NotificationScreen 헤더 테스트 버튼 노출** — ❌ **잔존** (R3-N9).

---

## 시연 직전 빠른 패치 권고 우선순위

총 4건 / 15분 안에 가능 (5/27 PM 또는 5/28 AM).

1. **R3-N1 (5분)** — VoiceRepositoryImpl/VoiceRemoteDataSourceImpl/VoiceRepository의 `sendVoiceMessageDirect` 주석 해제 + PushToTalkViewModel.kt:108, BackgroundListenerService.kt:535에서 `sendVoiceMessage` → `sendVoiceMessageDirect(senderUid, targetUid, url, duration)`로 원복. **데모 핵심 PTT 살리기**.
2. **R3-N2 (2분)** — MapScreen.kt:178-184에 `startForegroundService` 호출 추가. **권한 첫 부여 시 서비스 가동 보장**.
3. **R3-N4 (1분)** — MapScreen.kt:436-443의 권한 launcher 배열에 `Manifest.permission.POST_NOTIFICATIONS` 추가. **Android 13+ 알림 살리기**.
4. **R3-N9 (1분)** — NotificationScreen.kt:338-340 테스트 버튼 3줄 주석. **데모 영상 미완성 인상 차단**.
5. **R3-N3 (선택)** — 시연 시 설정 아이콘 누르지 않거나 5분 안에 SettingsDrawer에 ProfileSection + LogoutButton 채움.

---

## 점검 한계

1. **빌드 실행 안 함**: 정적 분석만. `ChatRepository.joinChatRoom` 삭제(R3-N14) 등 컴파일 영향은 별도 검증 필요.
2. **콘솔 functions 배포 상태 미확인**: R3-N1의 핵심. 강사 콘솔에 `sendVoiceMessage` 함수가 별도 배포되어 있다면 PTT는 정상 작동.
3. **워치 측 권한 다이얼로그 흐름 미점검**: WatchPermissionUtil은 점검했으나 실제 워치에서 권한 요청 → 결과 처리 시나리오는 정적 추정.
4. **알림 클릭 핫스타트(R3-N7) 추정**: NavHost startDestination이 핫스타트에 어떻게 동작하는지는 Compose Navigation 버전 의존. 실 단말 재현 필요.
5. **MapScreen 마커 fix 회귀 미관찰**: `isCameraInitialized` 분리로 카메라 init 안정화는 정상 보임. snap/animate 분기 정상.
6. **DM/채팅 전송 실패 UX**: 5/22엔 isFailed=true 빨간 풍선이 정상. 5/22 이후 ChatDetailViewmodel 변경 미점검.
7. **R3-N6 MyPage 재시도 흐름**: 실제 네트워크 오프라인으로 재현 안 함 — 코드 흐름만 추정.
