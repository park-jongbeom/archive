# 박종범 테스터 — 3팀 BBipit 베타 점검 (2026-06-11)

> **대상**: 박종범 본인 (Note 10+ AOS12 / Z Fold 4 AOS16 병행)
> **소스 최신화**: `develop` @ `8567dff` (2026-06-11 09:11, #368 fix/history-ui 머지본)
> **이전 문서**: [TESTER_PARK_BETA_REVIEW_260608.md](./TESTER_PARK_BETA_REVIEW_260608.md) (@ `release/v1` `870ffd1`) — 그 문서는 **6/08 release 라인** 기준. 본 문서는 **3일 더 진행된 `develop`**(히스토리 기능 신규 + App Check 변경 + 중복로그인 재활성) 기준이라 **블로커 상태가 여럿 바뀜**.
> **점검 방식**: 코드 정적분석(read-only) + 공식 문서 교차검증. 코드 라인 근거 명시.

> ⚠️ **어느 APK인지 먼저 확인.** `release/v1`(870ffd1, 6/08)과 `develop`(8567dff, 6/11)이 **다른 라인**이다. develop엔 **히스토리(사진 피드)·워치 무전 재생**이 새로 들어갔다. 팀이 어느 브랜치로 베타 APK를 뽑았는지 1줄 확인 — release/v1이면 6/08 문서, develop이면 본 문서.

---

## ⚠️ 6/08 대비 블로커 상태 변화 (release/v1 → develop)

| 6/08 ID | 6/08 상태 | **develop(6/11) 상태** | 근거 |
|---|---|---|---|
| **B-01 App Check 디버그 프로바이더 release 포함** | 🔴 설치 활성(킬스위치) | **설치 자체가 주석처리됨** → 이제 App Check **아무것도 안 깔림** | [MainActivity.kt:259-260](../repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt#L259) 주석 |
| **B-04 FGS 타입 권한 주석처리** | 🟠 권한/타입 미선언 | **매니페스트에 정상 선언됨** (location/connectedDevice/dataSync) | [AndroidManifest.xml:14-17,39](../repo/app/src/main/AndroidManifest.xml#L14) |
| **F-01 중복 로그인 차단 비활성** | 🟠 주석처리(무력) | **재활성됨** (다이얼로그 동작) | [SignInViewModel.kt:72-73](../repo/app/src/main/java/com/bbip/bbipit/presentation/auth/viewmodel/SignInViewModel.kt#L72) |
| **B-02 Firestore/Storage Rules 미배포** | 🔴 미해소 | **여전히 미해소** (`firebase.json`에 functions만) | `firebase.json` |
| **B-03 functions/index.js 빈 boilerplate** | 🟠 미해소 | **여전히 빈 파일** (실함수 다른 곳) | `functions/index.js` |

→ **B-01의 ‘킬스위치’ 공포는 사라졌지만**(아무것도 안 깔리므로), 그 대가로 **App Check 보호가 0**이 됐다. B-02(Rules)·B-03(함수 소스 부재)는 그대로다. 그리고 **히스토리·워치무전이라는 새 표면**이 추가됐다.

---

## 0. 1줄 결론

> **여전히 공개 베타 부적합. 콘솔 1건(Firestore Rules 모드)만 확인되면 비공개 내부 테스트는 가능.** App Check는 이제 **설치 안 됨**(킬스위치 해소 ✅, 단 보호도 0). **실시간 무전·알림이 여전히 FCM data 핸들러 없이 FGS Firestore 리스너 1개에 의존**(MyFirebaseMessagingService에 `onMessageReceived` 없음) — 백그라운드/Doze에서 무전 유실이 1순위. 신규 **히스토리 사진 업로드는 용량 제한·부분실패 롤백이 없어** 큰 사진 다중 업로드 시 일부만 올라가거나 멈출 수 있다. Cloud Function 응답을 **`as Map` 하드캐스팅**하는 친구 모듈은 서버 응답 shape가 어긋나면 크래시.

---

## 1. 🔴 배포 블로커 (코드 근거 + 본인 단말 검증법)

### 🟠 B-01(변경). App Check — 이제 아무 프로바이더도 설치 안 됨
- **근거**: [MainActivity.kt:259-260](../repo/app/src/main/java/com/bbip/bbipit/presentation/main/MainActivity.kt#L259) — `installAppCheckProviderFactory(DebugAppCheckProviderFactory…)`가 **주석처리**됨(f7856dd "App 디버깅 체크 비활성화"). 그런데 `firebase-appcheck-debug`는 여전히 **`debugImplementation`이 아니라 일반 `implementation`** — [app/build.gradle.kts:89](../repo/app/build.gradle.kts#L89) `implementation(libs.firebase.appcheck.debug)`.
- **무슨 일**: ① 6/08의 "디버그 토큰이 release에서 백엔드 거부" **킬스위치는 해소** ✅(설치 코드가 꺼졌으므로). ② 대신 **App Check가 아무 보호도 안 함** — release에서 토큰 미발급. enforcement가 켜져 있으면 오히려 **정상 토큰이 없어 거부**될 수 있음. ③ 디버그 라이브러리가 release APK에 **그대로 포함**(주석 한 줄만 풀면 다시 킬스위치 부활하는 지뢰 + 불필요 용량).
- **본인 단말 검증**: 새로 설치 → 친구추가/DM/무전/프로필이 **전부** 네트워크/권한류 오류면 enforcement+토큰부재 의심. → **강사·팀에 "App Check enforcement ON/OFF" 1줄 확인.**
- 웹 근거: [App Check 디버그 프로바이더 — production 금지](https://firebase.google.com/docs/app-check/android/debug-provider)

### 🔴 B-02(유지). Firestore / Storage 보안 규칙 미배포
- **근거**: `firebase.json`에 `functions` 키만 — `firestore`·`storage` 키 0건. repo에 `firestore.rules`/`storage.rules` **없음**(6/08 그대로).
- 클라이언트가 Functions를 우회해 Firestore/Storage를 **직접 read/write**하는 핫패스가 규칙이 유일 방어선이고, **히스토리가 그 표면을 더 넓혔다**:
  - 히스토리 피드: [HistoryRemoteDataSourceImpl.kt:46-50](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/history/HistoryRemoteDataSourceImpl.kt#L46) `History` 컬렉션 `whereIn("userId", …)` 직접 리스너
  - 히스토리 댓글: [:75-82](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/history/HistoryRemoteDataSourceImpl.kt#L75) `History/{id}/Comments` 직접 리스너
  - 히스토리 사진: [:141-144](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/history/HistoryRemoteDataSourceImpl.kt#L141) Storage `history/{uid}/…` 직접 putBytes
  - (기존) 음성/알림/친구 직접 read/write
- **위험**: 룰셋이 "테스트 모드"면 인증만 되면 **타 사용자 히스토리/사진/음성/위치/친구그래프 read 가능** = 개인정보 유출. **콘솔 Rules 탭 직접 확인 필수.**
- **본인 단말 검증**: 2계정으로 A에서 B의 히스토리/음성/알림이 흘러 들어오는지(테스트모드면 교차 노출).

### 🟠 B-03(유지). functions/index.js 빈 boilerplate인데 앱은 콜러블 ~30개 의존
- **근거**: `functions/index.js` 전체 주석(helloWorld만). 앱이 region `asia-northeast3`에서 호출하는 콜러블에 **히스토리 4종 추가**: `createHistory`, `deleteHistory`, `toggleHistoryLike`, `addHistoryComment`, `fetchNearbyHistory` + 기존(`requestFriend`/`createChatRoom`/`sendVoiceMessage`/`deleteAccountData` 등).
- **실함수 소스가 이 체크아웃에 없음** → 서버 검증로직(권한·dedup·원자성) **검증 불가**.
- **크래시 연결고리**: 응답 shape 불일치 시 캐스팅 크래시 — 아래 F-05.

### 🟠 B-04(변경). FGS 타입 선언됨 — 그러나 AOS14+ 위치권한 재확인 위험은 잔존
- **근거**: 6/08엔 주석이던 FGS 권한·타입이 **정상 선언**됨 ✅ — [AndroidManifest.xml:14-17](../repo/app/src/main/AndroidManifest.xml#L14) `FOREGROUND_SERVICE_LOCATION/CONNECTED_DEVICE/DATA_SYNC` + [:39](../repo/app/src/main/AndroidManifest.xml#L39) `foregroundServiceType="connectedDevice|location|dataSync"`. **그러나** 코드의 `startForeground`는 여전히 **LOCATION 타입만** 사용 — [BackgroundListenerService.kt:916-921](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L916)(DATA_SYNC/CONNECTED_DEVICE/MICROPHONE 라인은 주석).
- **남은 🟠 위험 (Z Fold4 AOS16 실테스트)**: **AOS14+는 location FGS 시작 시점에 위치 권한 재확인** → 권한 없으면 `SecurityException`. 코드는 [:297-299](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L297) `catch (Exception) { Log…("ForegroundServiceStartNotAllowedException 방어"); }`로 자기보호하지만, 그러면 **실시간 위치·무전 수신이 조용히 멈춤**.
- **워치→폰 깨우기**: [MobileMessageListenerService.kt:110-114](../repo/app/src/main/java/com/bbip/bbipit/core/base/MobileMessageListenerService.kt#L110) `startForegroundService(intent)`가 **백그라운드에서** 호출 → AOS12+ `ForegroundServiceStartNotAllowedException`이 generic catch로 삼켜짐 → **앱 백그라운드 시 "워치가 폰 깨우기"는 AOS12/16 양쪽 조용히 무동작.**
- 웹 근거: [FGS 타입 필수 — AOS14](https://developer.android.com/about/versions/14/changes/fgs-types-required), [백그라운드 FGS 시작 제한](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)

### 🟠 B-05(유지). Maps/Kakao 키 제한 미확인 + release minify 미적용
- 키는 git-ignored `local.properties` 분리(✅)이나 APK 매니페스트엔 평문(Maps 불가피). **GCP 콘솔 package+release SHA-1 제한** 없으면 키 도용 가능. `isMinifyEnabled = false`(release, [build.gradle.kts:35,59](../repo/app/build.gradle.kts#L35)) — R8 없음, 완전 역공학 가능. 베타 허용, production 전 수정.

---

## 2. 🟠 기능 버그 / 크래시 리스크 (신규 + 잔존)

| ID | 위치 | 증상 / 본인 단말 검증 |
|---|---|---|
| **F-05 친구 콜러블 하드캐스팅 크래시** 🔴 | [FriendRemoteDataSourceImpl.kt:29](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/friend/FriendRemoteDataSourceImpl.kt#L29) | `return result.data as Map<String, Any>` — **`as?`가 아닌 하드 `as`**(38/48/82/92/102도 `as Map<*,*>`). 서버가 기대와 다른 shape(예: `{success:true}`) 반환 시 **ClassCastException 크래시**. 히스토리 모듈은 `as?`라 안전한데(아래) **친구 모듈만 하드캐스팅**. 본인: 친구추가/수락/삭제 시 앱이 튕기는 순간 기록 |
| **F-06 히스토리 사진 업로드 용량/부분실패 방어 없음** 🟠 | [HistoryRemoteDataSourceImpl.kt:130-152](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/history/HistoryRemoteDataSourceImpl.kt#L130) | `putBytes(byteArray)` 전 **size 체크 없음** + 루프에 **try-catch 없음**. 3장 중 2번째가 네트워크 실패하면 1번째 URL은 이미 올라간 채 **예외 전파 → 전체 저장 실패 + Storage에 고아 파일**. 또 원본이 PNG/WebP여도 [:140](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/history/HistoryRemoteDataSourceImpl.kt#L140) `.jpg`로 고정 저장. 본인: **큰 사진 3장**으로 히스토리 작성 → 멈추거나 일부만 보이는지 |
| **F-03(유지) 무전 수신 = FGS 단일장애점** 🔴 | [MyFirebaseMessagingService.kt:23](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/notification/MyFirebaseMessagingService.kt#L23) | **`onMessageReceived` 없음**(onNewToken만). 실시간 무전/알림 전달이 전부 [BackgroundListenerService.kt:524-542](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L524) FGS Firestore 리스너 1개에 의존. FGS가 Doze/OEM킬로 죽으면 **FCM 폴백 없음**. 본인: 앱 백그라운드/recents 스와이프 후 몇 분 뒤 타 단말서 무전 → 도착하는지 |
| **F-07 폰↔워치 무전 dedup 레이스** 🟠 | [BackgroundListenerService.kt:977-992](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L977) | WALKIE 수신 시 `isAppInForeground`/`isPhysicalConnected` 값으로 **폰재생 vs 워치전송 vs 시스템알림** 분기. 체크와 `sendVoiceToWatch` 사이에 워치 연결이 바뀌면 **알림 누락 또는 중복**. 워치 연결 시 폰 배너는 **의도적으로 제거**([:992](../repo/app/src/main/java/com/bbip/bbipit/core/base/BackgroundListenerService.kt#L992) 주석). 본인: 워치 연결 상태로 무전 수신 → 폰/워치 어디서 울리는지, 둘 다 안 울리는 순간 있는지 |
| **F-02(유지) PTT 짧은녹음 빈파일** 🟠 | [PushToTalkViewModel.kt:58](../repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt#L58), [AudioRecorder.kt:55-72](../repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt#L55) | `if (duration < 1)`은 **UI 벽시계** 기준(인코더 프레임 아님). ~1초 직후 `stop()`이 `RuntimeException`([:64](../repo/app/src/main/java/com/bbip/bbipit/core/util/AudioRecorder.kt#L64)) → 파일 delete → [PushToTalkViewModel.kt:89-90](../repo/app/src/main/java/com/bbip/bbipit/presentation/map/viewmodel/PushToTalkViewModel.kt#L89) `uri==null` → "녹음된 파일이 없거나…". `delay(500)` 중 재누름 시 state desync. 본인: **1초 안팎 빠른 연타**로 재현(5/27 무전_05 잔존) |
| **F-08 워치 무전 4.5초 게이트 벽시계** 🟡 | `bbipit/.../WatchVoiceViewModel.kt` | 워치 PTT 4.5초 강제종료가 **벽시계 기준** → 느린 워치 CPU에서 인코딩 지연 시 사용자는 4.5초 말했는데 **실제 2~3초만 전송**. 본인: 워치에서 길게 말한 무전이 수신측에서 짧게 들리는지 |
| F-04(유지) 위치공유 OFF 잔여추적 🟡 | [LiveStatusRepositoryImpl.kt:124-134](../repo/app/src/main/java/com/bbip/bbipit/data/repository/LiveStatusRepositoryImpl.kt#L124) | OFF 시 업로드는 멈추나 FGS `requestLocationUpdates`·상시 알림은 유지 → 사용자 오해. 배터리/알림 확인 |

> ✅ **히스토리 읽기 경로는 방어 양호**: [observeHistoriesByUidList:40-67](../repo/app/src/main/java/com/bbip/bbipit/data/source/remote/history/HistoryRemoteDataSourceImpl.kt#L40)는 빈 리스트 가드(whereIn 크래시 방지)·`.take(30)`(whereIn 30개 제한)·문서별 try-catch(파싱 실패 시 null)로 견고. 댓글/캐스팅도 `as?` 안전. **취약점은 업로드(F-06)와 친구 하드캐스팅(F-05)에 국한.** (댓글 쿼리에 `.limit()` 부재 → 댓글 수천 개 시 메모리 압박 가능하나 베타 규모에선 후순위.)

---

## 3. 🧭 앱 방향성 기반 — 본인이 일부러 시도할 실발생 오류 시나리오

위치기반 소셜 + PTT + 히스토리 + Firebase 앱 특성상 깨질 수 있는 지점:

1. **Firestore 룰 개방 (B-02)** — 2계정 교차 데이터(히스토리·사진·음성) 노출 관측. **최우선(콘솔 병행).**
2. **백그라운드 무전 전달 (F-03)** — 백그라운드/스와이프 단말에 무전 → 배너 오는지(FGS 죽으면 실패).
3. **Z Fold4(AOS16) 위치권한 세션 중 회수 (B-04)** — 실행→전체 허용→설정에서 위치 회수 → 상시 알림 사라지고 실시간 위치/무전 조용히 멈추는지.
4. **히스토리 큰 사진 다중 업로드 (F-06)** — 5MB+ 사진 3장 → 일부만 올라가거나 멈추는지, .jpg 강제로 화질 깨지는지.
5. **친구추가/수락 크래시 (F-05)** — 친구 콜러블 호출 시 ClassCastException 튕김.
6. **폰↔워치 무전 dedup (F-07)** — 워치 연결 상태에서 무전 → 폰/워치 어디서 울리는지, 둘 다 누락되는 순간.
7. **워치→폰 백그라운드 깨우기 (B-04)** — 폰 백그라운드 시 워치 `check_phone_status` → AOS12+ 조용히 실패 예상.
8. **PTT 1초 안팎 연타 (F-02)** + **워치 4.5초 게이트(F-08)** — 빈 파일/짧은 전송.
9. **중복 로그인 재활성 (F-01 변경)** — 두 기기 동시 로그인 → 차단 다이얼로그 뜨는지([SignInViewModel.kt:72,145](../repo/app/src/main/java/com/bbip/bbipit/presentation/auth/viewmodel/SignInViewModel.kt#L72)). 6/08엔 무력이던 게 살아났으니 동작 확인.
10. **계정 탈퇴** — `deleteAccountData` 후 FCM 토큰 정리 + FGS 자기종료 동작. 히스토리 사진(`history/{uid}`)도 지워지는지.
11. **Maps 키 미제한 (B-05)** — release SHA 화이트리스트 안 되면 지도 **백지**.

---

## 4. ✅ 회귀 안전(수정/개선 확인) — 재검증만

| 항목 | 상태 |
|---|---|
| 알림 순서 튐(알림_22) | [NotificationScreen.kt](../repo/app/src/main/java/com/bbip/bbipit/presentation/notification/ui/NotificationScreen.kt) 안정 키 적용 → 수정(6/08) |
| 지도 마커 친구 불일치(지도_02/04/07) | [MapScreen.kt](../repo/app/src/main/java/com/bbip/bbipit/presentation/map/ui/MapScreen.kt) UID 키 → 수정(6/08) |
| App Check 킬스위치(B-01) | 설치 주석처리 → 킬스위치 해소 ✅(단 보호 0·라이브러리 잔존) |
| FGS 타입 권한 | 매니페스트 정상 선언 ✅(코드는 LOCATION 타입만 사용) |
| 중복 로그인 차단(F-01) | 재활성 ✅ — 동작 재검증 필요 |
| 히스토리 읽기 경로 | whereIn 가드·take(30)·문서별 try-catch로 방어 양호 |
| 시크릿 노출 | local.properties git-ignored 분리 |

---

## 5. 본인 단말 우선순위 (6/11 develop 기준)

| 순위 | 항목 | 이유 |
|---|---|---|
| 🔥 1 | **F-03 백그라운드 무전 전달** | FCM data 핸들러 부재 + FGS 단일장애점. 시연 핵심 기능 신뢰성 |
| 🔥 2 | **B-02 룰 개방 교차노출** | 개인정보 사고 직결 + 히스토리/사진으로 표면 확대 |
| 🔥 3 | **B-04 Z Fold4 위치권한 회수 / 워치 깨우기** | 본인만 가진 AOS16 + FGS 단일장애점 |
| ⭐ 4 | **F-05 친구 하드캐스팅 크래시 / F-06 히스토리 업로드** | 신규/잔존 크래시·데이터 손실 |
| ⭐ 5 | **F-07 폰↔워치 무전 dedup / F-02 PTT 연타** | 무전 신뢰성 |
| 6 | F-01 중복로그인 재활성 / 탈퇴 / Maps 키 | 정책·일관성 |

> ⚠️ **콘솔 확인은 이제 1건으로 축소** — App Check는 코드상 ‘설치 안 함’이라 킬스위치 공포는 해소됐고, **남은 필수 확인은 (a) Firestore Rules 현재 모드**다. (b) App Check enforcement는 "켜져 있으면 토큰 부재로 거부될 수 있음"만 부수 확인.

---

## 변경 이력
| 일자 | 변경 |
|---|---|
| 2026-06-11 | **`develop` @ 8567dff** 기준 재작성(6/08은 release/v1 라인). 블로커 상태 변화 명시 — B-01 App Check **설치 주석처리**(킬스위치 해소, 보호 0)·B-04 FGS 타입 **선언됨**·F-01 중복로그인 **재활성**·B-02/B-03 **잔존**. 신규 표면 — 히스토리 사진 업로드 방어 부재(F-06)·친구 콜러블 하드캐스팅 크래시(F-05)·폰↔워치 무전 dedup 레이스(F-07)·워치 무전 4.5초 벽시계(F-08) 코드 근거 명시. 히스토리 **읽기 경로는 방어 양호** 확인. 콘솔 확인 2건→1건(Rules) 축소 |
