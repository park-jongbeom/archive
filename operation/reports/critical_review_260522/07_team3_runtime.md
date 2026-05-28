# 3팀 BBipIt — 화면 동작/크래시 재점검 (2026-05-22)

> 관점: 강사진 실 단말 빌드 후 시연/테스트 시 만나는 버그 / 무반응 / 크래시
> 보안·구조 이슈는 [`04_team3_findings.md`](04_team3_findings.md) 참조 (중복 제외)
> 3팀 데드라인 2026-05-26 — 권고는 "오늘 안에 고칠 수 있는 것" 우선
> 점검 범위: `c:\Users\ibebu\bootcamp6_final\archive\teams-docs\3team\repo\` (`app/` + `bbipit/`)
> 빌드 실행은 하지 않음 (정적 분석만). 실제 빌드 가능 여부는 별도 검증 필요.

---

## 0. 요약 — 시연 5분 안에 발견될 Top 5

1. **R3-1 🔴 ChatDetailScreen이 하드코딩된 roomId/receiverId 사용** — 친구 목록에서 어떤 채팅방을 눌러도 항상 동일한 (다른 사람의) 채팅방으로 진입함. **클릭 직후 발견됨**.
2. **R3-2 🔴 모든 Cloud Functions 호출이 `NOT_FOUND` 실패** — 채팅·친구·음성·알림 17개 함수 미배포 (04 § C3-8). 시나리오 거의 전부 실패 경로 진입.
3. **R3-3 🔴 권한 거부 후 BackgroundListenerService startForegroundService 호출이 주석 처리됨** — 권한 다이얼로그에서 "허용" 누른 뒤 첫 진입에서 서비스가 시작되지 않음. 앱 재시작해야 작동.
4. **R3-4 🔴 워치 PTT의 친구 선택 UI 부재 + 모바일 PTT의 receiver는 정상 동작** — 워치에서 무전을 보내면 어떤 친구를 선택해도 무조건 하드코딩 UID(`Wy102dzyw4buC0V6YJuqxjtf6qA2`)에게만 감 (04 § "추가 발견"). 모바일은 `setTargetUid()`로 정상.
5. **R3-5 🟠 ChatListScreen은 `init`에서 1회 + `LaunchedEffect`에서 1회 = `observeChatRooms()` 이중 호출** — Flow 리스너 2개 활성화. 진입 시 동일 데이터 두 번 적재로 잠시 중복 표시 후 보정.

---

## 1. 화면별 상태 매트릭스

각 셀: 시연 시 무엇이 보이는가. ✅ 정상 / ⚠️ 동작은 하나 문제 / ❌ 무반응·크래시 / 🚫 functions 의존 (= 04 § C3-8 미해결 시 무조건 실패).

| 화면 | 진입 | 정상 동작 | 빈 상태 | 에러/거부 | functions 의존 |
|------|------|----------|---------|-----------|--------------|
| **SignIn** | ✅ | ✅ Email/PW + 카카오 + 구글 3종 | - | ⚠️ AppError 매핑은 있으나 Toast가 모달이 아니라 깜빡임 1회 | 없음 (Firebase Auth 직접) |
| **SignUp** | ✅ | ⚠️ GitHub raw URL로 약관 가져옴 — 네트워크 없으면 약관 빈 화면 | - | ⚠️ 약관 GitHub 다운 시 가입 흐름 막힘 | 없음 |
| **Map** | ✅ | ⚠️ 위치 즉시 표출 | ✅ 캐시 없으면 서울시청 기본 좌표 | ❌ launcher 콜백 내 startForegroundService 호출 주석 처리됨 → 권한 거부 후 허용해도 서비스 미가동 | 🚫 `updateUserStatus`, `getLiveStatusByUid` |
| **PTT(무전 다이얼로그)** | ✅ 친구 마커 클릭 → 다이얼로그 | ⚠️ 녹음·업로드는 OK | - | ❌ 마이크 거부 시 권한 launcher 호출되지만 그 후 사용자가 다시 길게 눌러야 함 (재시도 안내 없음) | 🚫 `sendVoiceMessage` (단 client `sendVoiceMessageDirect` Fallback 존재) |
| **ChatList** | ✅ | ⚠️ `init`+`LaunchedEffect` 이중 `observeChatRooms()` | ✅ "채팅 목록이 없습니다" 안내 표시 | - | 데이터 채널은 직접 (`rooms` 컬렉션) — functions 미배포 영향 적음 |
| **ChatDetail** | ❌ **하드코딩 roomId/receiverId** | ❌ navigation arg 미사용 → 같은 방만 보임 | ❌ `partnerName = "상대방 ($roomId)"` 더미 | ⚠️ 네트워크 에러 시 Toast + 메시지 isFailed=true (양호) | 🚫 `sendMessage`, `markMessagesAsRead`, `getAllMessages` |
| **FriendList** | ✅ | ⚠️ 자기 자신 친구 추가 가드 없음 (서버에서 거른다는 가정) | ✅ "친구가 없습니다" 표시 | ⚠️ 친구 삭제 버튼 클릭만 로그 찍힘 — `deleteFriend()` 호출 미연결 | 🚫 `requestFriend`, `getMyAcceptedFriends`, `createChatRoom` |
| **FriendRequest** | ✅ | ⚠️ 수락/거절 즉시 로컬 UI는 사라짐 (낙관 업데이트) | ✅ "받은 친구 요청이 없습니다" | ⚠️ 서버 실패 시 로그만 찍힘 — UI 복원 없음 | 🚫 `acceptFriendRequest`, `declineFriendRequest` |
| **Notification** | ✅ | ⚠️ 헤더에 "[+ DM 추가] [+ 무전 추가] [+ 친구 추가]" 개발용 테스트 버튼 그대로 노출 | ✅ 빈 LazyColumn | ⚠️ 네트워크 끊김 토스트 1회만 | 🚫 `markNotificationsAsRead`, `deleteNotifications` (단 markAsRead 단건은 Firestore 직쓰기로 fallback) |
| **VoicePlayer 오버레이** | ✅ Firestore 리스너 이벤트로 자동 슬라이드인 | ⚠️ MediaPlayer prepareAsync 동안 무음 1~2초 | - | ⚠️ URL 잘못 시 무반응 (재생 실패 UI 없음) | 🚫 `markVoiceMessageAsRead` |
| **MyPage / EditProfile** | ✅ | - | - | - | 🚫 `updateProfile`, `updateOnlineStatus`, `getUserProfile` (04에 미언급 — 추가 발견) |
| **Watch app (워치)** | ✅ 지도 + TimeText | ⚠️ 모바일 페어 안 됐을 때 동작 무한 무응답 (스플래시는 사라짐) | - | ❌ 친구 선택 UI는 있으나 송신 대상이 코드 하드코딩 (04 발견) | 모바일 경유 → 모바일 functions 의존 |
| **NotificationBanner (FCM)** | ❌ 시연 중 푸시 안 옴 | - | - | - | FCM `onMessageReceived` 미구현 (04 § C3-11) — Firestore 리스너로만 트리거 |

> functions 17개 미배포 (04 § C3-8) = 🚫 표시된 셀 전부 실패. 강사 콘솔에 별도 배포되어 있다면 다수가 ✅로 전환됨.

---

## 2. 강사 시연 시 크래시·무반응 시나리오 (발견 순)

### 시나리오 1 — 강사가 SignIn → Map 진입 → 권한 다이얼로그에서 "허용"을 누름. 그래도 친구 마커가 안 보임

**관련 코드**: [`MapScreen.kt:84-102`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)
```kotlin
val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    ...
    if (fineLocationGranted || coarseLocationGranted || bluetoothConnectGranted) {
        Log.d("MapScreen", "권한 승인됨 -> BackgroundListenerService 가동")
        val intent = Intent(context, BackgroundListenerService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else {
//                context.startService(intent)
//            }
    } else { ... }
}
```

**무엇이 보이는가**: 로그는 "권한 승인됨" 찍힘. 그러나 **실제 `startForegroundService` 호출은 주석 처리**됨. → BackgroundListenerService 미가동 → 위치 추적 X, 친구 위치 listener X. 사용자는 "권한 줬는데 왜 안 되지" 상태. **앱을 완전 종료 후 재진입**해야만 LaunchedEffect의 두 번째 분기(이미 권한 보유)가 작동해 서비스 시작됨.

**권고 (5분 패치)**: 주석 해제. 1줄 변경.
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
else context.startService(intent)
```

---

### 시나리오 2 — 강사가 친구 목록에서 친구 카드의 메시지(✉) 아이콘 클릭

**관련 코드**:
- [`FriendListScreen.kt:137-148`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/ui/FriendListScreen.kt) `viewModel.createOrGetChatRoom(...)` → roomId 받아 navigate
- [`FriendListViewModel.kt:117-155`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/viewmodel/FriendListViewModel.kt) `createChatRoom` Cloud Function 호출
- [`Navigation.kt:55`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/navigation/Navigation.kt) `composable<Routes.ChatRoom> { ChatDetailScreen(navController) }` — **route 인자 미전달**
- [`ChatDetailScreen.kt:91-96`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt):
  ```kotlin
  //    val route = navController.currentBackStackEntry?.toRoute<Routes.ChatRoom>()
  //    val roomId = route?.roomId ?: ""
  //    테스트 후 주석 제거 필요
  val roomId = "Wy102dzyw4buC0V6YJuqxjtf6qA2_lNkEvTubtfZJ7WbdZQMw5l5knAc2"
  val receiverId = "lNkEvTubtfZJ7WbdZQMw5l5knAc2"
  ```

**무엇이 보이는가**:
1. functions/index.js가 비어 있으므로 `createChatRoom` 호출 자체가 **`NOT_FOUND`** 로 실패 → onError(...) → Toast "네트워크 연결을 확인해주세요" 또는 "알 수 없는 서버 오류" 표시. **이 단계에서 시연 중단.**
2. 가설 — 콘솔에 별도 배포되어 있다면: roomId 받아 `Routes.ChatRoom(roomId)`로 navigate → **그러나 ChatDetailScreen에서 navigation arg 미사용 + 하드코딩 roomId 사용** → 매번 다른 사람의 채팅방으로 진입. 상대방 이름은 "상대방 (Wy102dzy...)" 으로 표시 (Repository에서 가져오는 partnerName도 ViewModel `loadChatRoomData(roomId)`에서 `"상대방 ($roomId)"` 더미). 즉 강사는 **본인 메시지가 모두 다른 사람의 방에 들어가는 것**을 바로 확인.

**재현**: 친구 A → 메시지 → 채팅방 진입. 친구 B → 메시지 → 채팅방 진입. **두 화면이 동일한 messages 리스트를 보임** + 동일 receiverId로 sendMessage가 실행됨.

**권고 (10분 패치)**:
1. `ChatDetailScreen` 상단 주석 해제 + 하드코딩 2줄 제거:
   ```kotlin
   val route = navController.currentBackStackEntry?.toRoute<Routes.ChatRoom>()
   val roomId = route?.roomId ?: return
   // receiverId 는 Firestore DMs/{roomId}.participants 에서 myUid 아닌 쪽으로 계산
   ```
2. `Routes.ChatRoom`에 `receiverId`도 함께 실어 전달:
   ```kotlin
   @Serializable data class ChatRoom(val roomId: String, val receiverId: String) : Routes
   ```
3. `FriendListScreen` onMessageClick → `navController.navigate(Routes.ChatRoom(roomId, friend.uid))`로 수정.

---

### 시나리오 3 — 강사가 친구 추가 → UID 입력 → "요청 보내기" 클릭

**관련 코드**: [`FriendListScreen.kt:165-183`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/ui/FriendListScreen.kt), [`FriendRemoteDataSourceImpl.kt:80-85`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/friend/FriendRemoteDataSourceImpl.kt) (`requestFriend` Cloud Function 호출)

**무엇이 보이는가**: Cloud Function 미배포 시 `e.message` = "NOT_FOUND" 류 raw exception 메시지가 그대로 토스트에 표시됨. 사용자 친화 메시지 없음.

```kotlin
catch (e: Exception) {
    val errorMessage = e.message ?: "요청 중 오류가 발생했습니다."
    onError(errorMessage)  // → showToastMessage = "NOT_FOUND" 같은 raw 토스트
}
```

**자기 자신 친구 추가 가드**: 클라이언트에 없음. UID 직접 입력하면 본인 UID 입력해도 막지 않음. 서버 미배포 시 어떻게 처리될지 알 수 없음.

**빈 상태**: ✅ "친구가 없습니다.\n친구 추가를 해보세요!" 정상 표시 ([FriendListScreen.kt:120-130](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/ui/FriendListScreen.kt)).

**권고 (오늘 패치 불요)**: functions 배포 후 자기 자신 UID 가드는 서버 측에서 처리. 클라이언트는 raw message 노출만 정리 — `errorMessage` 매핑 추가.

---

### 시나리오 4 — 강사가 친구 요청 화면에서 "수락" 버튼 클릭

**관련 코드**: [`FriendRequestViewModel.kt:68-85`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/viewmodel/FriendRequestViewModel.kt)
```kotlin
fun acceptFriendRequest(targetUid: String) {
    _requestList.value = _requestList.value.filter { it.uid != targetUid }  // 낙관 업데이트

    viewModelScope.launch {
        _isLoading.value = true
        val result = friendRepository.acceptFriendRequest(targetUid)  // Cloud Function 호출
        result.onSuccess { ... }
            .onFailure { appError ->
                android.util.Log.e("FriendRequestViewModel", "수락 실패: ${appError.message}")
                // ← 실패해도 UI 복원 없음, 토스트도 없음
            }
        _isLoading.value = false
    }
}
```

**무엇이 보이는가**:
1. 수락 버튼 클릭 → 즉시 해당 카드 사라짐 (낙관 업데이트).
2. functions 미배포라 서버는 실패. 그러나 **UI는 사라진 상태 그대로**. 사용자 입장에서 "수락됐다"고 오해.
3. 친구 목록(`FriendListScreen`)으로 이동해도 새 친구 없음 → 혼란.

**거절도 동일 패턴** (declineFriendRequest). 양방향 친구 트랜잭션도 functions 의존 (04 § C3-14).

**권고 (오늘 5분 패치)**: onFailure 시 `_requestList.value`에 원복 + Toast.
```kotlin
.onFailure { appError ->
    _requestList.value = (_requestList.value + originalItem).distinct()
    _errorEvent.emit("친구 수락 실패: ${appError.message}")
}
```

---

### 시나리오 5 — 강사가 친구 목록에서 친구 카드를 좌측으로 스와이프 → 삭제 다이얼로그 → "삭제" 클릭

**관련 코드**: [`FriendListScreen.kt:149-153`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/ui/FriendListScreen.kt)
```kotlin
onDelete = {
    // viewModel.deleteFriend(friend.uid) 호출
    android.util.Log.d("FriendList", "${friend.nickname} 삭제 요청")
}
```

**무엇이 보이는가**: 다이얼로그는 닫힘. 그러나 **실제 viewModel.deleteFriend 호출 코드가 주석 처리**되어 있음 (실은 호출 자체가 없음 — 로그만). 친구는 그대로 목록에 남아 있음. 사용자는 "삭제 안 됐다"고 인지.

**권고 (오늘 2분 패치)**: `viewModel.deleteFriend(friend.uid)` 호출 라인 추가 + FriendListViewModel에 `deleteFriend` 함수 작성 (현재 FriendRepository에는 `deleteFriend` 있음 — ViewModel 만 연결 안 됨).

---

### 시나리오 6 — 강사가 채팅방에서 메시지 입력 후 "전송" 클릭

**관련 코드**: [`ChatDetailViewmodel.kt:74-137`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/viewmodel/ChatDetailViewmodel.kt)

**무엇이 보이는가**:
1. 입력창 즉시 비워짐 + 임시 메시지 풍선이 본인 영역에 즉시 표시됨 (낙관 업데이트, 양호).
2. `chatRepository.sendMessage()` → Cloud Function `sendMessage` 호출 → functions 미배포 → `Result.Failure`.
3. **양호한 처리**: errorCause 분석 + Toast + 풍선이 `isFailed = true`로 빨간 재전송/X 아이콘 표시. **이 화면은 functions 실패 처리 UX가 가장 잘 되어 있음**.
4. 단, 시나리오 2의 하드코딩 roomId 문제로 시연용 채팅방은 어차피 실제 데이터와 안 맞음.

**권고**: 변경 없음 (UX 양호).

---

### 시나리오 7 — 강사가 친구 마커 클릭 → 무전 버튼 길게 누름 → 손 뗌

**관련 코드**: [`MapScreen.kt:462-516`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt) + [`PushToTalkViewModel.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt)

**무엇이 보이는가**:
1. 권한 보유 시: `setTargetUid(friend.uid)` 정상 호출 → 녹음 시작 → 손 떼면 stop → duration 계산.
2. duration < 1초면 "녹음 시간이 너무 짧습니다" 안내 (양호).
3. 1초 이상이면 Firebase Storage 업로드 → `sendVoiceMessageDirect` (Firestore 직쓰기). **이 경로는 functions 의존 없음** → 강사 콘솔 룰만 허용하면 동작.
4. 권한 거부 시: launcher 띄움. 사용자가 허용 누른 뒤 **다시 길게 눌러야** 송신. 한 번에 안 되는 UX 불친절.

**자기 자신에게 무전 가드**: 없음. Map에서 내 마커를 클릭하면 → `onClick = { true }`로 다이얼로그 안 뜸 (양호 — [MapScreen.kt:229](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)).

**녹음 중 화면 전환 시 release**: PushToTalkViewModel에 `onCleared()` 없음 → AudioRecorder 점유 가능 (04 § C3-7 권고와 동일).

**권고 (오늘 10분 패치)**: PushToTalkViewModel에 `onCleared() { runCatching { audioRecorder.stop() } }` 추가.

---

### 시나리오 8 — 워치에서 친구 마커 클릭 → 워치 PTT 버튼 길게 누름

**관련 코드**:
- [`WatchMapScreen.kt:140-151`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/map/WatchMapScreen.kt) 친구 다이얼로그 + WatchPushToTalkButton
- [`WatchPushToTalkButton.kt`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/base/WatchPushToTalkButton.kt) 녹음 시작 → audioSender 트리거
- [`WatchAudioSender.kt:80-158`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/util/WatchAudioSender.kt) 폰 상태 체크 → 채널 오픈 → PCM 스트리밍
- [`BackgroundListenerService.kt:436-449`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt) `sendWatchAudioToServer()`:
  ```kotlin
  val targetUid = "Wy102dzyw4buC0V6YJuqxjtf6qA2"  // 하드코딩
  ```

**무엇이 보이는가**:
1. 워치 다이얼로그는 friend 정보를 받아 표시. PTT 버튼은 정상.
2. 녹음 → 스트림 → 폰 BackgroundListenerService 수신 → PCM→M4A 인코딩 → Storage 업로드 → **`sendVoiceMessageDirect(senderUid, "Wy102dzyw4buC0V6YJuqxjtf6qA2", url, duration)`**.
3. **선택한 친구가 누구든 무조건 단일 UID에게만 전달**. 강사 시연 시 즉시 발견됨. (04 § "추가 발견" 동일 — 시나리오 측면에서 재서술).

**권고 (오늘 30분 패치)**:
1. WatchMapScreen에서 친구 선택 시 워치 SharedPreferences 또는 `WatchAudioSender` 인스턴스에 targetUid 저장.
2. 워치→폰 메시지 path에 `targetUid` 추가:
   ```kotlin
   channelClient.openChannel(phoneNode.id, "/audio_stream?target=${targetUid}")
   ```
3. `sendWatchAudioToServer(...)`에서 channel path 파싱.
- 또는 폰 측에 "마지막 모바일 PTT 친구 UID" 캐시 → 워치 PTT 시 그 UID 재사용.

---

### 시나리오 9 — 강사가 알림 화면 진입 → 무전 알림 카드 클릭

**관련 코드**: [`NotificationScreen.kt:149-175`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationScreen.kt)
```kotlin
onClick = {
    if (item.type == "DM") {
        viewModel.markAsRead(item.id)
        navController.navigate(Routes.ChatRoom(roomId = item.roomId))  // ← 시나리오 2와 동일 한계
    } else if (item.type == "WALKIE") {
        ...
        Toast.makeText(navController.context, "무전을 확인합니다.", Toast.LENGTH_SHORT).show()
        // ← 실제 재생 흐름은 없음, 토스트만
    }
    else if (item.type == "REQ") {
        viewModel.markAsRead(item.id)
        Toast.makeText(navController.context, "친구요청을 확인합니다.", Toast.LENGTH_SHORT).show()
        // ← FriendRequestList 화면으로 이동 안 함
    }
}
```

**무엇이 보이는가**:
1. **DM 클릭** → 시나리오 2와 동일 (하드코딩 roomId 화면).
2. **무전 클릭** → "무전을 확인합니다" 토스트만. 실제 음성 재생 없음. expiredVoiceIds에 추가만 됨. 카드는 흐려짐(만료됨 표시).
3. **친구요청 클릭** → "친구요청을 확인합니다" 토스트만. **FriendRequestList 화면으로 이동 안 함**. 사용자 혼란.

**헤더의 개발자 테스트 버튼 노출**: 헤더에 `[+ DM 추가]` `[+ 무전 추가]` `[+ 친구 추가]` 버튼이 일반 사용자 화면에 그대로 노출. 클릭 시 가짜 알림 생성. 시연용으로는 유용하나 **데모 영상에 그대로 찍히면 미완성 느낌**.

**권고 (오늘 5분 패치)**:
1. REQ 클릭 시 `navController.navigate(Routes.FriendRequestList)` 추가.
2. WALKIE 클릭 시 실제 음성 재생 로직 추가 (VoicePlayerViewModel 트리거).
3. 헤더 테스트 버튼은 BuildConfig.DEBUG 분기 or 5/26 직전 1줄 주석 처리.

---

### 시나리오 10 — 강사가 알림 좌측 스와이프로 삭제

**관련 코드**: [`NotificationViewModel.kt:194-211`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationViewModel.kt)
```kotlin
fun markAsReadAndDelete(id: String) {
    _deletedIds.value += id
    _notification.value = _notification.value.filter { it.id != id }  // 즉시 UI 반영
    viewModelScope.launch {
        try {
            notificationRepository.deleteNotifications(currentUserId, id)  // Cloud Function
        } catch (e: Exception) {
            _deletedIds.value -= id  // 단 _notification.value 복원은 없음
        }
    }
}
```

**무엇이 보이는가**: 카드 사라짐 → 서버는 functions 미배포로 실패 → 그러나 **로컬 리스트는 복원되지 않음**. 다음 진입 시 Firestore에서 다시 가져오므로 카드 부활. 사용자 혼란.

**권고 (오늘 3분 패치)**: catch 블록에서 `_notification.value = listOf(item) + _notification.value` 로 원복.

---

### 시나리오 11 — 강사가 알림 헤더 "전체 확인" 클릭

**관련 코드**: [`NotificationViewModel.kt:244-249`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationViewModel.kt)
```kotlin
fun onReadAllClick() {
    _readAllClicked.value = true
    viewModelScope.launch {
        notificationRepository.markNotificationsAsRead("all", null)
        // ← await만 하고 결과 확인 없음, 실패해도 UI 그대로
    }
}
```

**무엇이 보이는가**: 카드들의 보라색 점 일괄 제거. Cloud Function 실패해도 로컬은 그대로 처리됨 (서버 정합성 깨짐 가능). 다음 진입 시 점 다시 나타날 수 있음.

**권고**: onFailure 시 `_readAllClicked.value = false` 원복. 5분.

---

### 시나리오 12 — 강사가 채팅방 0개 상태에서 채팅 탭 진입

**관련 코드**: [`ChatListScreen.kt:87-99`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatListScreen.kt)

**무엇이 보이는가**: ✅ 정상.
```
"채팅 목록이 없습니다.
새로운 대화를 시작해보세요!"
```

**그러나 ChatListViewModel 이중 호출**: `init`에서 `observeChatRooms()` + `LaunchedEffect(Unit) { viewModel.observeChatRooms() }`. 2개 listener 동시 활성화 → trySend 2회 → UI 깜빡임 + 중복 화면. 빈 상태에서는 안 보임. 채팅방 있을 때 진입 시 잠시 같은 채팅방이 두 줄 보였다가 정상화.

**권고 (오늘 1분 패치)**: ChatListScreen의 `LaunchedEffect { viewModel.observeChatRooms() }` 제거. init만 두면 됨.

---

### 시나리오 13 — 강사가 ChatList의 검색 아이콘 클릭 → 검색어 입력

**관련 코드**: [`ChatListScreen.kt:128-205`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatListScreen.kt) + [`ChatListViewmodel.kt:111-128`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/viewmodel/ChatListViewmodel.kt)

**무엇이 보이는가**: ✅ 정상 작동. partnerName으로 필터링. ignoreCase OK.

**단**: ChatListScreen의 두 번째 `LaunchedEffect(Unit) { viewModel.clearSearch(); ... }`에서 `clearSearch()`는 `_uiState`를 `allChatList`로 복원. 그러나 진입 시점에 `allChatList`가 비어 있으면 (init이 아직 안 끝남) 일시적으로 빈 리스트 표시. 큰 문제는 없음.

---

### 시나리오 14 — 강사가 메시지 0개인 채팅방 진입

**관련 코드**: [`ChatDetailScreen.kt:140-181`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt)

**무엇이 보이는가**:
- 로딩 중에는 빈 LazyColumn. `isLoading = true`이면 "// 로딩 UI" 주석만 있고 실제 인디케이터 없음 — 빈 화면.
- 로딩 완료 시 `item { DateHeader("TODAY") }` 만 표시. **메시지 0개 빈 상태 안내가 없음**. 강사 입장에서 "왜 비어 있지?".

**권고 (오늘 5분 패치)**: messages.isEmpty() 분기 추가:
```kotlin
if (uiState.messages.isEmpty()) {
    Text("아직 메시지가 없습니다. 첫 메시지를 보내보세요!", ...)
} else { items(...) }
```

---

### 시나리오 15 — 강사가 잘못된 이메일/비밀번호로 로그인 시도

**관련 코드**: [`SignInScreen.kt:170-172`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/auth/ui/SignInScreen.kt) + [`SignInViewModel.kt:45-65`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/auth/viewmodel/SignInViewModel.kt)

**무엇이 보이는가**:
- `AppError.Email` → emailError 상태 갱신 → InputField 빨간 에러 (정상).
- `AppError.Password` → emailError + pwError **둘 다** 동일 메시지 (코드 명백한 버그: line 59).
  ```kotlin
  is AppError.Password ->
      updateState { copy(emailError = exception.message, pwError = exception.message) }
  ```
- 그 외 → `uiState.error` → `ShowToast(it)` 호출. 그러나 `ShowToast`는 Composable이라 매 recomposition마다 새 토스트 — 깜빡임. 또한 error 상태가 초기화되지 않아 화면 회전·재구성 시 재발.

**권고 (오늘 5분 패치)**:
1. `AppError.Password`는 pwError만 갱신.
2. SignIn flow의 `uiState.error` 표시 후 `viewModel.clearError()` 또는 LaunchedEffect 패턴 사용.

---

### 시나리오 16 — 강사가 회원가입 → 약관 동의 다이얼로그 진입 시 네트워크 차단

**관련 코드**: [`SignUpScreen.kt:200-218`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/auth/ui/SignUpScreen.kt) → `viewModel.getTerms(...)` → [`AuthRemoteDataSourceImpl.kt:191-200`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/auth/AuthRemoteDataSourceImpl.kt) GitHub raw URL

**무엇이 보이는가**: 약관 텍스트 빈 화면. 사용자는 "동의" 누르기 어려움 (불완전). 04 § A4 동일.

---

### 시나리오 17 — 강사가 강제 종료 후 푸시 알림 받음 (FCM 콜드 스타트)

**관련 코드**: [`MyFirebaseMessagingService.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/data/source/remote/notification/MyFirebaseMessagingService.kt) — `onMessageReceived` 미구현 (04 § C3-11)

**무엇이 보이는가**:
- 앱 종료 상태 + FCM `notification` payload: 시스템 트레이 표시됨. 클릭 시 launcher activity. **그러나 인텐트 extras 처리 없음** → 그냥 메인 화면.
- 앱 종료 상태 + FCM `data` payload: 처리 핸들러 없음 → 무반응.
- AppLifecycleObserver가 MainActivity onCreate에서 등록되므로, 콜드 스타트 시 BackgroundListenerService 단독 가동 경로엔 `isAppInForeground` 항상 false (04 § C3-4 동일).

**시연 영향**: 시연 중 외부에서 강사가 푸시 보내도 알림 안 뜸. 단, **앱이 열려 있으면** Firestore listener가 작동해 NotificationBannerHost를 통해 in-app 배너는 표시됨 ([NotificationViewModel.kt:67-118](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationViewModel.kt) 의 `triggerInAppBanner` + 시스템 알림 `showSystemNotification`이 fallback). 즉 **앱 켠 상태로 시연하면 알림은 작동**. 앱 죽인 상태로 푸시 받기는 안 됨.

---

### 시나리오 18 — 강사가 위치 권한만 거부 + Bluetooth 권한만 승인

**관련 코드**: [`MapScreen.kt:91`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)
```kotlin
if (fineLocationGranted || coarseLocationGranted || bluetoothConnectGranted) {
    // ← OR 조건. Bluetooth만 있어도 서비스 가동 분기 진입 (단 startForegroundService 주석 처리됨)
} else {
    Toast.makeText(context, "서비스 이용을 위해 위치 및 블루투스 권한이 필요합니다.", ...)
}
```

**무엇이 보이는가**: OR 조건이라 Bluetooth만 줘도 "권한 승인됨" 분기 진입. 그러나 BackgroundListenerService 가동되어도 `requestLocationUpdates`가 SecurityException 캐치되어 무음 실패 ([BackgroundListenerService.kt:572-575](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt)). 사용자 입장에서 "권한 줬는데 친구 위치 안 보임" — 안내 없음.

**권고 (오늘 2분 패치)**: 위치 권한 별도 분기로 AND 조건화. + 시나리오 1과 함께 ACCESS_BACKGROUND_LOCATION 별도 요청 (04 § C3-3).

---

### 시나리오 19 — GPS 비활성화 상태에서 Map 진입

**관련 코드**: [`MapViewModel.kt:74-114`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/MapViewModel.kt) `fetchLastKnownLocation`

**무엇이 보이는가**: ✅ 양호한 처리. `lastLocation == null` + `getCurrentLocation` 실패 시 서울시청 좌표(37.5665, 126.9780)로 fallback. 무한 로딩 방지 명시. **단** "GPS 꺼져 있어요" 안내 토스트는 없음.

---

### 시나리오 20 — 강사가 앱 종료 (백 키 + 최근앱 스와이프)

**관련 코드**: [`MainActivity.kt:50-67`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt) `onDestroy`

**무엇이 보이는가**:
1. `runBlocking { liveStatusRepository.updateMyLiveStatus(...) }` — 메인 스레드 동기 네트워크 호출. **ANR 위험**. 04 § A1 동일.
2. 네트워크 느릴 시 강사 화면에서 "응답 없음" 다이얼로그가 뜰 수 있음.
3. BackgroundListenerService는 START_STICKY라 계속 실행 → 로그아웃 안 한 상태로 강제 종료 시 사용자 위치가 친구에게 계속 노출되는 사용자 인지 부족 가능.

**권고 (오늘 2분 패치)**: runBlocking 제거 → `applicationScope.launch { ... }` 또는 onStop에서 처리.

---

### 시나리오 21 — 워치 앱 첫 진입 시 모바일 페어링이 안 되어 있음

**관련 코드**:
- [`bbipit/MainActivity.kt:51-75`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/MainActivity.kt) 스플래시 → setContent → WatchMapScreen
- [`WatchMapScreen.kt:60-63`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/map/WatchMapScreen.kt):
  ```kotlin
  LaunchedEffect(Unit) {
      sendPermissionRequestToPhone(context)      // nodes.firstOrNull() null이면 noop
      requestImmediateLocationSync(context)      // 마찬가지
  }
  ```

**무엇이 보이는가**: 워치 지도는 표시 (서울 기본 좌표). 친구 마커 0개. 어떤 안내도 없음. 사용자는 "왜 비어 있지?" 무한 대기. **에러 메시지/페어링 안내 없음.**

**권고 (오늘 5분 패치)**: `nodes.isEmpty()` 분기에서 워치 화면에 "휴대폰과 연결이 필요해요" 텍스트 오버레이.

---

### 시나리오 22 — 워치에서 PTT 버튼 누름, 그러나 폰 BackgroundListenerService 미가동

**관련 코드**: [`WatchAudioSender.kt:80-158`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/util/WatchAudioSender.kt)

**무엇이 보이는가**:
1. `/check_phone_status` 메시지 송신 → 1.5초 timeout 대기.
2. 폰 측 `MobileMessageListenerService`가 응답 안 하면 "❌ 휴대폰 수신 서비스가 응답하지 않습니다." 토스트. ✅ **양호한 처리** (시나리오 1로 폰 서비스가 안 켜져 있다면 자주 발생).
3. `MobileMessageListenerService`가 외부 service 등록되어 있는지 확인 필요 ([AndroidManifest.xml:43-53](../../../teams-docs/3team/repo/app/src/main/AndroidManifest.xml)) — 등록됨. 단 BackgroundListenerService에서 audio_stream을 받는데 이는 ChannelClient로 직접 수신.

**시나리오 5의 결합 효과**: 폰을 켰는데 권한 다이얼로그 응답 후 BackgroundListenerService가 안 켜졌으면(시나리오 1), 워치 PTT도 "휴대폰 수신 서비스 응답 안 함"으로 실패. 핵심 데모 경로 차단.

---

### 시나리오 23 — 로그아웃 후 재로그인

**관련 코드**: Logout 핸들러는 repo에 명확히 없음. [`Navigation.kt:39-43`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/navigation/Navigation.kt)에서 이메일 미인증 시 `authRepository.signOut()` 호출. MyPageScreen 로그아웃은 별도 점검 필요.

**무엇이 보이는가**: BackgroundListenerService는 START_STICKY로 계속 실행 + manageSessionByState()에서 `isUserLoggedIn=false` 시 `lifeCycleManager.stopSession()`만 호출. **Service 자체는 종료 안 됨** (04 § A8 동일). 알림은 "무전기 대기 중" 그대로 표시 → 로그아웃 사용자 혼란.

---

### 시나리오 24 — MapScreen에서 마이크 권한만 영구 거부

**관련 코드**: [`MapScreen.kt:297-302`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt) (FriendProfileDialog 내 audioPermissionLauncher) + [WatchPermissionUtil](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/util/WatchPermissionUtil.kt) 워치만

**무엇이 보이는가**: 모바일 — 권한 launcher 호출 후 거부 시 "무전 기능을 이용하려면 마이크 권한 동의가 필요합니다" 토스트만 띄움. **"다시 묻지 않음" 분기 처리 없음** — 사용자가 시스템 설정으로 가야 한다는 안내 없음. 무전 다이얼로그는 계속 떠 있고 버튼 누르면 같은 토스트만 반복.

**권고 (오늘 5분 패치)**: `shouldShowRequestPermissionRationale` 체크 후 false면 시스템 설정 화면 인텐트 안내 다이얼로그.

---

### 시나리오 25 — Map에서 위치 마커 1000개 / 친구 0명 (빈 상태)

**관련 코드**: [`MapScreen.kt:233-267`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)

**무엇이 보이는가**: 친구 0명이면 `friendsStatuses.forEach`가 noop. 내 마커만 표시. **빈 상태 UI 없음 — 다만 지도 화면이라 채팅처럼 빈 안내 필요는 적음**. 양호.

---

## 3. 발견 상세

### 🔴 Critical Runtime

#### R3-1. ChatDetailScreen 하드코딩 roomId/receiverId
- **위치**: [`ChatDetailScreen.kt:91-96`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt)
- **시나리오**: 위 시나리오 2.
- **재현**: friendList → 친구 메시지 아이콘 → 진입한 화면이 매번 동일.
- **영향**: 데모 흐름의 핵심 channel 차단 sentinel. 5분 안에 발견됨.
- **권고**: roomId·receiverId를 NavBackStackEntry에서 추출. `Routes.ChatRoom`에 receiverId 추가. 또는 ViewModel에서 partnerUid 계산.

#### R3-2. MapScreen 권한 launcher의 startForegroundService 주석 처리
- **위치**: [`MapScreen.kt:93-98`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt)
- **시나리오**: 시나리오 1.
- **영향**: 권한 신규 부여 흐름에서 서비스 미가동. 앱 재시작 필요. **시연 첫 단계에서 발견됨**.
- **권고**: 주석 4줄 해제.

#### R3-3. functions/index.js 17개 함수 미배포 (04 § C3-8와 동일, 화면별 영향 재정리)
- **영향 화면 매핑**:
  | 함수 | 영향 화면 | UX |
  |---|---|---|
  | `createChatRoom` | FriendList → 메시지 클릭 | Toast 실패 메시지, 채팅방 미진입 |
  | `sendMessage` | ChatDetail | isFailed=true 빨간 풍선 (양호) |
  | `getMyChatRooms` | (사용처 없음 — observeChatRooms는 직접 Firestore) | - |
  | `markMessagesAsRead` | ChatDetail 진입 시 | log만 |
  | `getAllMessages` | (사용처 없음) | - |
  | `sendVoiceMessage` | (사용처 없음 — sendVoiceMessageDirect로 대체) | - |
  | `markVoiceMessageAsRead` | 음성 재생 완료 | log만 |
  | `updateUserStatus` | LiveStatusRepository 동기화 | 친구가 내 status 변화 못 봄 |
  | `getLiveStatusByUid` | BackgroundListenerService processLocationUpdate | catch → 신규 LiveStatus 생성 fallback OK |
  | `requestFriend` | FriendList 추가 다이얼로그 | raw 메시지 토스트 |
  | `acceptFriendRequest` | FriendRequest 수락 | UI 카드는 사라졌지만 친구 미추가 |
  | `declineFriendRequest` | FriendRequest 거절 | 동일 |
  | `deleteFriend` | FriendList 스와이프 삭제 | **viewModel 호출조차 안 됨** (시나리오 5) |
  | `getFriendProfileByUid` | (현재 사용처 없음) | - |
  | `getMyAcceptedFriends` | FriendListViewModel | log만 |
  | `markNotificationsAsRead` | "전체 확인" | log만 |
  | `deleteNotifications` | 알림 스와이프 | log만, UI 복원 없음 |
  | **추가 발견** `updateProfile` | EditProfileViewmodel + UserRemoteDataSourceImpl | 프로필 편집 실패 |
  | **추가 발견** `updateOnlineStatus` | UserRemoteDataSourceImpl | 온라인 상태 미반영 |
  | **추가 발견** `getUserProfile` | VoicePlayerViewModel + UserRemoteDataSourceImpl | 발신자 닉네임 "알 수 없음" 표시 |

  → 04 § C3-8 17개 함수 + **추가 3개 = 총 20개**. 4일 안에 모두 작성은 비현실적. **방향 결정 권고: A) 최소 핵심 5개만 작성 (createChatRoom, sendMessage, requestFriend, acceptFriendRequest, declineFriendRequest) + 나머지는 클라이언트 직쓰기 + firestore.rules 강화 / B) 콘솔에 별도 배포본이 이미 있다면 git에 commit 강제.**

#### R3-4. 워치 PTT 송신 대상 UID 하드코딩 (04 § "추가 발견" 시나리오)
- **위치**: [`BackgroundListenerService.kt:439`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt)
- **시나리오**: 시나리오 8.
- **영향**: 워치 무전이 항상 단일 UID에게만 감.

#### R3-5. FriendListScreen 친구 삭제 버튼 미연결
- **위치**: [`FriendListScreen.kt:149-152`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/ui/FriendListScreen.kt) onDelete 콜백
- **시나리오**: 시나리오 5.
- **재현**: 친구 카드 좌측 스와이프 → "삭제" 누름 → 다이얼로그 닫힘 + log만 출력 + 카드 그대로.
- **권고**: `viewModel.deleteFriend(friend.uid)` 호출 + 로컬 _friendList 낙관 업데이트.

#### R3-6. NotificationScreen REQ 클릭 시 FriendRequestList 이동 누락
- **위치**: [`NotificationScreen.kt:166-173`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationScreen.kt)
- **시나리오**: 시나리오 9.
- **영향**: 알림 클릭의 핵심 UX 미완성. 5분 안에 발견.

---

### 🟠 High Runtime

#### R3-7. ChatListViewModel observeChatRooms 이중 호출
- **위치**: [`ChatListViewmodel.kt:46-48`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/viewmodel/ChatListViewmodel.kt) `init { observeChatRooms() }` + [`ChatListScreen.kt:78-81`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatListScreen.kt) `LaunchedEffect { viewModel.observeChatRooms() }`
- **시나리오**: 시나리오 12.
- **영향**: snapshot listener 2개 등록. 화면 진입 시 동일 리스트 중복 처리.
- **권고**: LaunchedEffect의 observeChatRooms 호출 제거 (init만 두기).

#### R3-8. FriendRequest 수락/거절 실패 시 UI 복원 없음
- **위치**: [`FriendRequestViewModel.kt:74-84`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/viewmodel/FriendRequestViewModel.kt)
- **시나리오**: 시나리오 4.
- **권고**: onFailure 시 카드 원복 + 토스트.

#### R3-9. NotificationViewModel deleteNotifications 실패 시 UI 복원 없음
- **위치**: [`NotificationViewModel.kt:194-211`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationViewModel.kt)
- **시나리오**: 시나리오 10.
- **권고**: catch 블록에서 _notification 리스트 원복.

#### R3-10. ChatDetail messages 0개 빈 상태 안내 없음
- **위치**: [`ChatDetailScreen.kt:152-181`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt)
- **시나리오**: 시나리오 14.
- **권고**: messages.isEmpty() 분기 추가.

#### R3-11. PushToTalkViewModel onCleared 누락 (04 § C3-7 권고 미적용)
- **위치**: [`PushToTalkViewModel.kt`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt) 전체
- **시나리오**: 시나리오 7.
- **영향**: 녹음 중 화면 회전·전환 시 AudioRecord 점유 leak 가능.

#### R3-12. MainActivity onDestroy runBlocking (04 § A1 동일)
- **시나리오**: 시나리오 20. ANR 위험.

#### R3-13. SignIn AppError.Password 버그
- **위치**: [`SignInViewModel.kt:58-59`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/auth/viewmodel/SignInViewModel.kt)
  ```kotlin
  is AppError.Password ->
      updateState { copy(emailError = exception.message, pwError = exception.message) }
  ```
- **시나리오**: 시나리오 15. 비밀번호 에러인데 이메일 필드도 빨갛게.
- **권고**: emailError는 null 유지.

#### R3-14. SignIn error 상태 자동 클리어 없음
- **위치**: [`SignInScreen.kt:170-172`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/auth/ui/SignInScreen.kt) `uiState.error?.let { ShowToast(it) }`
- **시나리오**: 시나리오 15. 매 recompose마다 토스트 재발.
- **권고**: LaunchedEffect(uiState.error) 패턴 + clearError().

#### R3-15. NotificationScreen 헤더의 개발자 테스트 버튼 노출
- **위치**: [`NotificationScreen.kt:332-341`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationScreen.kt)
- **시나리오**: 시나리오 9. 일반 사용자 화면에 `[+ DM 추가]` 노출.
- **권고**: BuildConfig.DEBUG 분기 또는 시연 직전 1줄 주석.

#### R3-16. NotificationScreen WALKIE 클릭 시 음성 재생 안 됨
- **위치**: [`NotificationScreen.kt:154-165`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/notification/NotificationScreen.kt)
- **시나리오**: 시나리오 9.
- **권고**: 진행 중인 무전이 있으면 VoicePlayerViewModel.triggerReplay(voiceMessageId) 등 연결.

#### R3-17. ACCESS_BACKGROUND_LOCATION runtime 요청 누락 (04 § C3-3 동일)
- **시나리오**: 시나리오 18 결합. 백그라운드 전환 시 친구가 내 위치 못 봄.

#### R3-18. MyFirebaseMessagingService onMessageReceived 미구현 (04 § C3-11)
- **시나리오**: 시나리오 17.

#### R3-19. BackgroundListenerService scope에 SupervisorJob 없음 (04 § A10 동일)
- **영향**: 단일 child 실패 시 위치+음성+워치 전부 중단.

#### R3-20. Watch app 모바일 페어 안 됐을 때 안내 없음
- **시나리오**: 시나리오 21.
- **위치**: [`WatchMapScreen.kt:60-63`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/map/WatchMapScreen.kt)
- **권고**: nodes.isEmpty() 시 워치 화면에 안내 텍스트 오버레이.

---

### 🟡 Medium Runtime

#### R3-21. AppLifecycleObserver 등록 시점이 MainActivity onCreate (04 § C3-4 동일)
- BackgroundListenerService가 FCM 콜드 스타트로 단독 가동 시 `isAppInForeground` 항상 false → manageSessionByState에서 silent stop. 영향은 적음.

#### R3-22. FriendList의 자기 자신 친구 추가 가드 누락
- **위치**: [`FriendListViewModel.kt:92-116`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/friendship/viewmodel/FriendListViewModel.kt)
- **시나리오**: 시나리오 3. 본인 UID 입력 시 차단 없음. 서버 의존.
- **권고**: targetCode가 본인 UID와 같으면 즉시 토스트 + return.

#### R3-23. ChatList isLoading 인디케이터는 있으나 ChatDetail isLoading은 빈 화면
- **위치**: [`ChatDetailScreen.kt:140-141`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt) `if (uiState.isLoading) { // 로딩 UI }` 빈 분기.
- **권고**: CircularProgressIndicator 추가.

#### R3-24. ChatList의 ChatItem isOnline 항상 false
- **위치**: [`ChatListViewmodel.kt:106`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/viewmodel/ChatListViewmodel.kt) `isOnline = false` 하드코딩
- **영향**: 채팅 리스트의 온라인 점 표시 항상 회색. 디자인엔 있으나 실데이터 미연결.

#### R3-25. ChatDetail isOnline 임시값
- **위치**: [`ChatDetailScreen.kt:224-225`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/ui/ChatDetailScreen.kt) `val isOnline = true` 하드코딩.

#### R3-26. ChatRepositoryImpl vs ChatRemoteDataSourceImpl 컬렉션명 불일치 (04 § A9)
- Repository: `rooms`. RemoteDataSource: `DMs`. 데이터 일관성 깨질 위험.
- ChatListViewModel은 chatRepository.observeChatRooms() → ChatRepositoryImpl의 `rooms` 사용.
- ChatList screen에서 직접 `db.collection("DMs")`로 unreadCount 조회([ChatListViewmodel.kt:77-83](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/viewmodel/ChatListViewmodel.kt)) → 컬렉션 2종 혼용. 시연 시 unread 0으로만 보일 가능성.

#### R3-27. ChatListViewModel `users` vs `Users` 컬렉션 케이스 불일치
- **위치**: [`ChatListViewmodel.kt:90`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/chat/viewmodel/ChatListViewmodel.kt) `db.collection("users")` (소문자)
- 반면 FriendRemoteDataSourceImpl은 `db.collection("Users")` (대문자) — Firestore는 case-sensitive → **다른 컬렉션**. 한쪽이 비어있을 가능성.
- 시연 시 채팅 리스트의 상대방 이름이 "알 수 없는 사용자"로 표시될 수 있음.

#### R3-28. VoicePlayerScreen URL 잘못 시 재생 실패 UI 없음
- **위치**: [`VoicePlayerViewModel.kt:80-87`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/base/VoicePlayerViewModel.kt) `audioPlayer.playFromUrl(url) { ... }` — 실패 콜백 없음.
- **권고**: AudioPlayer의 OnErrorListener 콜백을 ViewModel로 전달.

#### R3-29. Bluetooth 권한만 부여 시 위치 미작동 안내 부재 (시나리오 18)

#### R3-30. 워치 PTT 중 화면 백그라운드 진입 시 cleanup
- **위치**: [`WatchAudioSender.kt:163-183`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/util/WatchAudioSender.kt)
- WatchVoiceViewModelWatch는 onCleared 없음 → 워치 ViewModel 소멸 시 audioSender.destroy() 미호출. AudioRecord leak.

#### R3-31. WatchMapViewModel onMessageReceived try/catch이 너무 넓음
- **위치**: [`WatchMapViewModel.kt:42-55`](../../../teams-docs/3team/repo/bbipit/src/main/java/com/bbip/bbipit/map/WatchMapViewModel.kt) catch (Exception) — 어떤 메시지든 파싱 실패 시 silent. 워치 화면에 디버깅 정보 없음.

#### R3-32. SignUp 약관 동의 흐름의 isAgreed 토글
- **위치**: [`SignUpScreen.kt:200-218`](../../../teams-docs/3team/repo/app/src/main/java/com/bbip/bbipit/presentation/auth/ui/SignUpScreen.kt) onDismissRequest에 `isAgreed = it` — `it`이 dismiss 사유라 boolean 의미가 모호. 약관 동의 안 했는데 동의 처리될 가능성 있음 (코드 의도 불명확).

---

## 4. 화면별 functions 영향 압축 표

| 화면 | functions 미배포 시 시연 차단 정도 |
|------|-----------------------------------|
| SignIn | 🟢 영향 없음 (Firebase Auth 직접) |
| SignUp | 🟢 영향 없음 (Auth + 약관 GitHub raw) |
| Map | 🟡 위치 update 안 됨, 친구 위치 read는 가능 (Firestore 직접) |
| PTT 무전 | 🟢 sendVoiceMessageDirect 클라 직쓰기 — 영향 적음 |
| ChatList | 🟢 observeChatRooms는 직접 (rooms/DMs 혼용 문제만) |
| ChatDetail | 🔴 sendMessage / markMessagesAsRead 전부 functions — **그러나 isFailed UX는 양호** |
| FriendList | 🔴 createChatRoom (메시지 진입), requestFriend, deleteFriend(미연결) |
| FriendRequest | 🔴 acceptFriendRequest, declineFriendRequest |
| Notification | 🟡 단건 markAsRead는 Firestore 직쓰기 — markNotificationsAsRead(전체)와 deleteNotifications만 functions |
| MyPage/EditProfile | 🔴 updateProfile, getUserProfile (04에 미언급 발견) |
| VoicePlayer | 🟡 markVoiceMessageAsRead만 functions, 재생 자체는 OK |
| Watch | 🔴 모바일 functions 의존 — 모바일 화면 따라감 |

---

## 5. 시연 직전 빠른 패치 권고 (오늘 안 가능, 5/22)

총합 60분 안에 다음 11개 패치 가능.

1. **R3-2 (2분)** — MapScreen 권한 launcher의 startForegroundService 주석 4줄 해제.
2. **R3-1 (10분)** — ChatDetailScreen 하드코딩 roomId/receiverId 제거. Routes.ChatRoom에 receiverId 추가. FriendListScreen onMessageClick 수정. ChatListScreen에서 onChatItemClicked → roomId만 전달하던 부분도 receiverId 같이 전달 (DMs/{roomId}.participants에서 추출).
3. **R3-5 (2분)** — FriendListScreen onDelete에 `viewModel.deleteFriend(friend.uid)` 추가 + FriendListViewModel에 deleteFriend 함수.
4. **R3-6 (1분)** — NotificationScreen REQ 클릭 시 `navController.navigate(Routes.FriendRequestList)` 추가.
5. **R3-7 (1분)** — ChatListScreen의 `LaunchedEffect { viewModel.observeChatRooms() }` 삭제.
6. **R3-8 (5분)** — FriendRequestViewModel onFailure에 카드 원복 + 토스트.
7. **R3-9 (3분)** — NotificationViewModel deleteNotifications catch에서 _notification 리스트 원복.
8. **R3-10 (5분)** — ChatDetailScreen messages.isEmpty() 분기 추가.
9. **R3-11 (3분)** — PushToTalkViewModel onCleared { runCatching { audioRecorder.stop() } }.
10. **R3-13 (1분)** — SignInViewModel AppError.Password → pwError만 갱신.
11. **R3-15 (1분)** — NotificationScreen 헤더 테스트 버튼 BuildConfig.DEBUG로 감싸기 또는 주석.

**+ 추가 권고 (5/23~5/25)**:
- **R3-4 (30분)** — 워치 PTT targetUid 전달 흐름 (channel path 또는 SharedPreferences).
- **R3-17 (10분)** — ACCESS_BACKGROUND_LOCATION 별도 요청 다이얼로그.
- **R3-22 (3분)** — 자기 자신 친구 추가 가드.
- **R3-20 (5분)** — Watch app 모바일 페어 안 됐을 때 안내.
- **R3-3 결정** — 콘솔 functions 배포 상태 확인 + 핵심 5개만 작성 결정.

---

## 6. 점검 한계

1. **빌드 실행 안 함**: 정적 분석만. compileSdk=36 + Android Studio 호환은 04 § 점검 한계 동일.
2. **워치 모듈 일부 미점검**: WatchNotificationOverlay, WatchVoiceReceiverService는 점검. WatchBaseViewModel, WatchAudioPlayer는 코드 일부만 본 상태.
3. **MyPage/EditProfile 코드 정독 미실시**: getHttpsCallable Grep으로 functions 의존(`updateProfile`)만 확인. 화면 시나리오 미수행.
4. **MobileMessageListenerService 미점검**: 04 § A5에 언급된 BLUETOOTH 권한 사용처. 워치-폰 통신 검증 시 추가 필요.
5. **실제 시연 환경 미테스트**: GPS·페어링·Firestore 데이터 등 환경 의존 시나리오는 정적 추정.
6. **콘솔 functions 배포 상태 미확인** (04 § C3-8 동일): 가설 A·B 둘 다 가능.
7. **시나리오 결합 효과 일부 누락**: 예) "신규 사용자가 OAuth 가입 + 약관 동의 + 권한 거부 + 워치 페어링 없음" 등 다단 결합 경로는 부분 추정.
8. **AudioPlayer 정독 안 함**: VoicePlayerViewModel의 playFromUrl 실패 콜백 부재(R3-28)는 추정.

---

## 7. 04와 중복 회피·재구성 정리

| 04의 발견 | 본 보고서 재구성 |
|---|---|
| C3-3 ACCESS_BACKGROUND_LOCATION | R3-17 (시나리오 18 결합) |
| C3-7 AudioRecord release | R3-11 (시나리오 7) |
| C3-8 functions/index.js 비어있음 | R3-3 (화면별 영향 매핑 + 추가 3개 함수 발견) |
| C3-11 FCM onMessageReceived | R3-18 (시나리오 17) |
| C3-14 친구 양방향 트랜잭션 | 시나리오 3·4 흐름의 일부로 포함 |
| 추가 발견: 워치 PTT 하드코딩 | R3-4 (시나리오 8) |
| A1 runBlocking | R3-12 (시나리오 20) |
| A8 로그아웃 시 Service stop | 시나리오 23 |
| A9 컬렉션명 rooms/DMs | R3-26 |
| A10 SupervisorJob | R3-19 |

**본 보고서 신규 (04에 없음)**: R3-1, R3-2, R3-5, R3-6, R3-7, R3-8, R3-9, R3-10, R3-13, R3-14, R3-15, R3-16, R3-20, R3-22, R3-23, R3-24, R3-25, R3-27, R3-28, R3-30, R3-31, R3-32 — 총 22건.
