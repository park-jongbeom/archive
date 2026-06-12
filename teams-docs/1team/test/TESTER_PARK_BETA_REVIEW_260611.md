# 박종범 테스터 — 1팀 Snoffee 베타 디바이스 점검 (2026-06-11)

> **대상**: 박종범 본인 (Note 10+ AOS12 / Z Fold 4 AOS16 병행)
> **소스 최신화**: `develop` @ `5921936` (2026-06-11 13:29, #125 fix/language-issue 머지본) — 1팀은 release 브랜치·태그 없음 → `develop`이 베타 라인
> **이전 문서**: [TESTER_PARK_BETA_REVIEW_260608.md](./TESTER_PARK_BETA_REVIEW_260608.md) (@ `4c8e29b`) — **그 문서는 이제 대부분 무효.** 6/08 이후 워치 메인 UI·Gemini 인사이트·폰 알림이 **새로 구현됨**(아래 ‘프레임 전환’ 참고)
> **점검 방식**: 코드 정적분석(read-only) + 공식 문서 교차검증. 코드 라인 근거 명시. **디바이스로 블랙박스 관찰 가능한 항목 중심**, 콘솔/빌드 설정은 분리.
> **읽는 법**: "이렇게 한다 → 이렇게 보일 것이다(기대) → 깨지면 이렇게 보인다."

---

## ⚠️ 프레임 전환 — 6/08 문서가 무효가 된 이유

6/08 문서의 1줄 결론은 **"워치 3종 전멸 · AI 부재 · 푸시 부재 — 디바이스로 볼 진짜 크래시는 사실상 없음"**이었다. 그 사이 1팀은 **죽어 있던 간판 기능을 실제로 구현**했다:

| 6/08 당시 | 6/11 현재 | 근거 |
|---|---|---|
| 워치 메인 UI 없음 | **워치 홈/입력/설정 화면 구현** (#119/#120/#123) | `wear/` 전체 |
| AI 인사이트 미연결 | **Gemini 2.5 Flash 인사이트 연결** ("Gemini 인사이트 완료!") | [GeminiRemoteDataSourceImpl.kt](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/GeminiRemoteDataSourceImpl.kt) |
| 폰 푸시/알림 부재 | **폰 알림 3종 구현** (취침 1h 전·역연산 컷오프·누적 금지, #112/#113) | [PhoneNotificationHelper.kt](../repo/app/src/main/java/com/snoffee/app/service/PhoneNotificationHelper.kt) |
| 카페인 입력 검증 약함 | **이름 10자·용량 10~400mg 제한** (#109) | 폰/워치 입력 화면 |

→ 따라서 이번 점검의 무게중심은 **"없는 기능 확인"이 아니라 "새로 구현됐는데 방어가 안 된 신규 코드"**다. 새 코드는 **언더테스트**라 디바이스로 깨질 여지가 6/08보다 훨씬 크다.

---

## 0. 1줄 결론 (디바이스 관점)

> **로컬 카페인·수면 코어는 여전히 견고. 그러나 새로 붙은 워치 동기화가 "테스트 모드 하드코딩"으로 덮여 있어, 워치가 실제 연결 상태와 무관하게 항상 ‘연결됨’으로 보이면서 토스트는 ‘연결 끊김’을 띄우는 모순 상태다.** 게다가 폰↔워치를 잇는 **Capability 문자열이 양쪽 불일치**(`caffeine_app` vs `verify_snoffee_phone_app`)라 진짜 연결 판정 경로는 영원히 실패한다. 워치 커스텀 음료 이름 입력은 **입력값이 상태에 반영조차 안 됨**. AI는 네트워크 실패를 **조용한 폴백 문구**로 가려 "AI가 맹탕"처럼 보일 수 있다. 디바이스로 볼 **크래시는 여전히 적지만**, **"고장처럼 보이는 신규 기능"**이 워치·AI·알림 전반에 깔렸다.

---

## 1. 🔴 신규 코드 결함 — 디바이스로 바로 보이는 지점

### 🔴 D-01. 워치 Capability 문자열 양쪽 불일치 → 실연결 판정 영원히 실패
- **근거**: 폰은 `"caffeine_app"`을 등록 — [SnoffeeApplication.kt:29](../repo/app/src/main/java/com/snoffee/app/SnoffeeApplication.kt#L29) `capabilityClient.addLocalCapability("caffeine_app")`. 그런데 워치는 `"verify_snoffee_phone_app"`을 조회 — [WearDataClient.kt:35](../repo/wear/src/main/java/com/snoffee/wear/data/WearDataClient.kt#L35) `CAPABILITY_PHONE_APP = "verify_snoffee_phone_app"` + [:71](../repo/wear/src/main/java/com/snoffee/wear/data/WearDataClient.kt#L71) `getCapability(CAPABILITY_PHONE_APP, …)`. **두 문자열이 절대 안 맞음** → 워치의 `isPhoneConnected`는 실제 페어링과 무관하게 **항상 false**가 되는 게 정상 경로.
- **동작(재현)**: 워치-폰 페어링 + 양쪽 앱 설치 후 워치 홈 진입.
- **깨지면(이상)**: 아래 D-02의 하드코딩이 이 결함을 **가리고 있어** 홈 게이지는 ‘연결됨’처럼 뜨지만, 진입 직후 **"연결이 끊어졌습니다" 토스트**가 뜬다(아래 D-02 참조). 즉 화면과 토스트가 **서로 모순**.

### 🔴 D-02. 워치 `isEmulatorTestMode = true` 하드코딩 → 홈은 항상 ‘연결됨’, 토스트는 ‘끊김’
- **근거**: [WearMainActivity.kt:111](../repo/wear/src/main/java/com/snoffee/wear/presentation/WearMainActivity.kt#L111) `val isEmulatorTestMode = true` (상수 하드코딩). 이 값으로 HomeScreen 연결 상태를 **강제 override** — [:177-180](../repo/wear/src/main/java/com/snoffee/wear/presentation/WearMainActivity.kt#L177) `isPhoneConnected = if (isEmulatorTestMode) true else …`, `connectionError = if (isEmulatorTestMode) false …`, `onRetryClick = { if (!isEmulatorTestMode) … }`. **그런데** 같은 화면의 토스트는 override 안 된 **실제** 값을 씀 — [:113-117](../repo/wear/src/main/java/com/snoffee/wear/presentation/WearMainActivity.kt#L113) `LaunchedEffect(isPhoneConnected) { if (!isPhoneConnected) Toast("연결이 끊어졌습니다.") }`. 또 **음료 입력 화면(index 0)은 실제 값**을 받음 — [:167](../repo/wear/src/main/java/com/snoffee/wear/presentation/WearMainActivity.kt#L167) `isPhoneConnected = isPhoneConnected`.
- **동작(재현)**: 워치 홈 진입 → 곧바로 음료 추가 화면 진입.
- **기대(정상)**: 실제 연결 상태에 따라 일관된 UI.
- **깨지면(이상)**: ① 홈 게이지는 **항상 ‘연결됨’**(=문제 은폐), ② 진입 즉시 **"연결이 끊어졌습니다" 토스트**, ③ 재시도 버튼 **무동작**(`if (!isEmulatorTestMode)`로 막힘), ④ 입력 화면은 ‘끊김’ 분기로 동작. **시연 단말이 곧 실기기인데 ‘에뮬레이터 테스트 모드’가 켜진 채 배포되는 것**이 핵심 리스크.

### 🔴 D-03. 워치 커스텀 음료 이름 — 입력값이 상태에 반영조차 안 됨
- **근거**: [CaffeineInputScreen.kt:76](../repo/wear/src/main/java/com/snoffee/wear/presentation/caffeine/CaffeineInputScreen.kt#L76) `var drinkName by remember { mutableStateOf("") }`. RemoteInput 결과를 읽지만 — [:84-90](../repo/wear/src/main/java/com/snoffee/wear/presentation/caffeine/CaffeineInputScreen.kt#L84) `rawInput`을 만들어 `> 10`이면 토스트만 띄우고 **`drinkName`에 대입하는 코드가 아예 없음**(10자 이하 정상 입력도 미반영). 확인 버튼은 [:77](../repo/wear/src/main/java/com/snoffee/wear/presentation/caffeine/CaffeineInputScreen.kt#L77) `isNameValid = drinkName.isNotEmpty() && drinkName.length <= 10`로 게이트 → `drinkName`이 계속 `""`라 **버튼이 영원히 비활성**.
- **동작(재현)**: 워치 음료 입력 → "직접 입력" → 음성/키보드로 이름 입력 → 확인 시도.
- **깨지면(이상)**: 입력을 마쳐도 **이름 칸이 비어 보이고 확인 버튼이 안 켜짐**. 워치에서 커스텀 음료 등록이 사실상 불가.

### 🟠 D-04. 워치 `sendMessage` 예외 미방어 → 동기화 실패가 조용히 사라짐
- **근거**: [WearDataClient.kt:80-87](../repo/wear/src/main/java/com/snoffee/wear/data/WearDataClient.kt#L80) — `scope.launch { … getMessageClient().sendMessage(node.id, path, data).await() }`에 **try-catch 없음**. `getConnectedNodes()` 직후 폰이 사라지면 `await()`가 던지고 **코루틴만 조용히 죽음**, 호출부엔 실패 통지 없음.
- **동작(재현)**: 워치에서 카페인 입력 직후 폰 앱을 강제종료/블루투스 차단.
- **깨지면(이상)**: 워치는 "보냄"으로 간주하고 화면을 넘기지만 **폰엔 반영 안 됨**(유실). 에러 안내 없음.

### 🟠 D-05. AI 인사이트 — 네트워크/쿼터 실패가 ‘조용한 폴백 문구’로 가려짐
- **근거**: Gemini는 **하드코딩 키가 아니라 Firebase AI Logic** 경유 — [GeminiRemoteDataSourceImpl.kt:16-22](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/GeminiRemoteDataSourceImpl.kt#L16) `Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-2.5-flash")`. 키 노출 위험 없음 ✅. 단 호출 래퍼가 — [:112-130](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/GeminiRemoteDataSourceImpl.kt#L112) `runCatching { … }.getOrElse { Log.e(…); fallbackMessage() }` — **모든 실패를 폴백 문구로 치환**. 응답이 비어도 [:121-122](../repo/app/src/main/java/com/snoffee/app/data/datasource/remote/GeminiRemoteDataSourceImpl.kt#L121) `?: fallbackMessage()`.
- **동작(재현)**: 비행기 모드/약한 네트워크에서 홈·주간/월간 인사이트 진입. 또는 짧은 시간에 인사이트를 여러 번 갱신(쿼터 유발).
- **기대(정상)**: "지금 분석할 수 없어요" 류 **명시적 오류 + 재시도** 또는 로딩.
- **깨지면(이상)**: 실패인데도 **항상 같은 일반 문구**가 떠서 사용자는 "AI가 맹탕/맨날 똑같은 말"로 오해. 진짜 인사이트인지 폴백인지 구분 불가. 실패가 UI로 전파되지 않음.

### 🟠 D-06. 폰 알림 — 권한 회수/재부팅 시 조용히 안 옴
- **근거**: 정시 알람은 **AOS12+ 정확 알람 권한을 올바르게 분기**함 ✅ — [PhoneNotificationHelper.kt:125-137](../repo/app/src/main/java/com/snoffee/app/service/PhoneNotificationHelper.kt#L125) `if (SDK>=S && !canScheduleExactAlarms()) setAndAllowWhileIdle(…) else setExactAndAllowWhileIdle(…)` (컷오프도 [:162-174](../repo/app/src/main/java/com/snoffee/app/service/PhoneNotificationHelper.kt#L162) 동일). 문제는 **발송 시점**: [:90-95](../repo/app/src/main/java/com/snoffee/app/service/PhoneNotificationHelper.kt#L90) "권한 체크는 호출부에서 처리됨을 전제" 주석 + `try { notify(…) } catch (SecurityException) { e.printStackTrace() }` → **POST_NOTIFICATIONS를 예약 후 회수**하면 알람은 떠도 `notify()`가 SecurityException으로 **조용히 묵살**. 또 매니페스트에 **`RECEIVE_BOOT_COMPLETED` 없음** → AlarmManager 예약은 **재부팅 시 전부 소멸**.
- **동작(재현)**: ① 알림 켠 상태로 취침 시간 예약 → 설정에서 알림 권한 OFF → 컷오프 시각 대기. ② 알림 예약 후 **폰 재부팅** → 예약 시각 대기.
- **깨지면(이상)**: ①②에서 **알림이 영원히 안 옴**(에러도 없음). 취침/컷오프 알림 기능이 "가끔 안 오는" 게 아니라 이 두 경로에서 **확정적으로 사라짐**.

### 🟡 D-07. 시간 역행/이상 시계 → 잔류량 계산 입력 방어
- **근거**: 동기화 시 잔류 계산은 음수 경과시간을 거르지만 — `if (timeDiff > 0)` 가드 존재(폰 리스너) — **결과를 [0, 섭취량] 범위로 클램프하지는 않음**. 반감기 수학 자체는 안전.
- **동작(재현)**: 기기 시간을 과거로 크게 돌린 뒤 카페인/리포트 화면 진입, 또는 미래로 크게 돌리기.
- **깨지면(이상)**: 크래시는 아니나 게이지/잔류 숫자가 **순간적으로 어색하게 튐**(특히 폰↔워치가 서로 다른 시계 기준일 때 두 화면 숫자 불일치). 튀면 시각·조작 내용 기록.

---

## 2. 🧭 앱 방향성 기반 — 일부러 시도할 디바이스 시나리오

카페인·시간·수면 + 워치 동기화 앱 특성상 경계를 직접 친다:

1. **워치 ‘테스트 모드’ 노출(D-02)** — 워치 홈 진입 즉시 ‘연결됨 게이지 + 끊김 토스트’ 모순이 보이는지. 재시도 버튼 눌러도 무반응인지. **최우선.**
2. **워치 커스텀 음료 이름(D-03)** — "직접 입력"으로 이름 넣고 확인 버튼이 안 켜지는지.
3. **폰↔워치 양방향 동기화** — 폰에서 카페인 기록 → 워치 게이지 갱신되는지 / 워치에서 기록 → 폰 반영되는지(D-04 유실 포함). 시각차 두고 관찰.
4. **AI 인사이트 오프라인(D-05)** — 비행기 모드에서 인사이트 진입 → 항상 같은 폴백 문구만 뜨는지(=실패 은폐).
5. **알림 권한 회수 + 재부팅(D-06)** — 두 경로 각각에서 컷오프/취침 알림이 조용히 사라지는지.
6. **자정 넘김** — 기록 후 자정 통과: "오늘 섭취량" 0 리셋 + 잔류 이월이 게이지에서 자연스러운지.
7. **0 카페인 커스텀 음료** — 0짜리 등록 시 크래시 없이 "안전" 표시.
8. **시계 뒤로/앞으로(D-07)** — 음수·초대형 경과시간에 폰/워치 숫자가 튀거나 어긋나는지.
9. **앱 업데이트 후 데이터(데이터 손실)** — 버전 올린 빌드 재설치 시 **기존 로컬 카페인·수면 기록이 사라지는지**(아래 ⚠️ DB 파괴적 마이그레이션). 사라지면 기록.
10. **Z Fold4 폴드/언폴드 중 상태 유지** — 음료 입력·리포트 보는 중 펼침↔접힘 → 입력값·스크롤·선택 유지되는지(portrait 잠금이라 회전 config change는 없지만 재구성 가능). 참조: [Compose 폴드 인식 상태 보존](https://developer.android.com/develop/ui/compose/layouts/adaptive/foldables/make-your-app-fold-aware)
11. **Health Connect 미설치(Note10+)** — 수면/리포트 진입 시 크래시 없이 빈 처리되는지(리포지토리 `runCatching` 방어 유지). HC 설치+READ_SLEEP 거부 경로도.

---

## 3. ✅ 정상 확인(디바이스로 본 코어) — 회귀만

| 항목 | 상태 |
|---|---|
| 카페인 잔류량(반감기) 계산 | 수학 정확 — 0카페인·시계역행에도 크래시 없는지만 재확인 |
| 수면 리포트(HC 설치+권한 시) | 동작 — 표시 확인 |
| Health Connect 미설치 가드 | `SleepRepositoryImpl`의 `runCatching` 방어 유지 → HC 없어도 크래시 아님, 빈 처리 |
| 정확 알람 권한 분기 | [PhoneNotificationHelper.kt:125,162](../repo/app/src/main/java/com/snoffee/app/service/PhoneNotificationHelper.kt#L125) `canScheduleExactAlarms()` 체크 후 inexact 폴백 ✅ |
| Gemini 키 위생 | Firebase AI Logic 경유 → 평문 키 없음 ✅ |
| 세로 고정 | [AndroidManifest.xml:32](../repo/app/src/main/AndroidManifest.xml#L32) `screenOrientation="portrait"` ✅ |

---

## 4. ⚠️ 콘솔/빌드 확인 필요 (디바이스로 불가 — 팀/강사 질의)

1. **DB 파괴적 마이그레이션 — 데이터 손실 위험**: [DatabaseModule.kt:30](../repo/app/src/main/java/com/snoffee/app/data/di/DatabaseModule.kt#L30) `fallbackToDestructiveMigration(dropAllTables = true)` + DB `version = 4` ([SnoffeeDatabase.kt:20](../repo/app/src/main/java/com/snoffee/app/data/local/SnoffeeDatabase.kt#L20)). 스키마 한 번이라도 바뀐 빌드를 **업데이트 설치하면 로컬 카페인·수면 기록 전부 삭제**. 베타 중 버전 올릴 때 사용자 데이터가 날아갈 수 있음 → 마이그레이션 전략 필요. **디바이스 검증**: 데이터 쌓은 뒤 새 빌드 덮어쓰기 → 기록 유지되는지.
2. **`isEmulatorTestMode = true` 배포 여부**: 위 D-02. 이 상수가 **release에도 true로 박힌 채** 나가는지 팀 확인 필수. 시연 단말=실기기인데 ‘에뮬레이터 모드’면 워치 연결 UI 전체가 가짜.
3. **AI 인사이트 호출 비용/쿼터**: Firebase AI Logic 경유라 App Check로 보호되지만, 인사이트를 화면 진입마다 호출하면 쿼터/요금 이슈 가능 → 호출 빈도·캐시 정책 확인.

---

## 5. 본인 단말 우선순위

| 순위 | 항목 | 이유 |
|---|---|---|
| 🔥 1 | **D-01/D-02 워치 연결 모순 + 테스트모드 하드코딩** | 신규 워치 기능의 최대 결함. ‘연결됨 게이지 + 끊김 토스트’가 시연 직격 |
| 🔥 2 | **D-03 워치 커스텀 음료 이름 미반영** | 입력값이 상태에 안 들어가 확인 버튼 영구 비활성 — 명확한 기능 불능 |
| 🔥 3 | **D-06 알림 권한회수/재부팅 시 소실** | 새로 만든 알림 3종이 두 경로에서 확정적으로 사라짐 |
| ⭐ 4 | **D-04 워치 동기화 유실 / D-05 AI 실패 은폐** | 조용한 실패 2종 |
| ⭐ 5 | **DB 파괴적 마이그레이션(데이터 손실)** | 업데이트 설치 시 기록 삭제 — 콘솔/빌드 확인과 병행 |
| 6 | 자정/0카페인/시계역행/폴드 상태유지 | 코어 경계값(수학 안전, UI 확인) |

> **보고 핵심 메시지**: **"6/08 ‘없던 기능’이 워치·AI·알림으로 전부 구현됐다. 그런데 워치는 ‘에뮬레이터 테스트 모드’가 하드코딩(D-02)되고 Capability 문자열이 불일치(D-01)해 연결 UI가 가짜+모순. 워치 커스텀 이름은 입력값이 상태에 반영 안 됨(D-03). 알림은 권한회수/재부팅에 조용히 소실(D-06). AI는 키 노출 없음(Firebase AI Logic) ✅이나 실패를 폴백 문구로 은폐(D-05). 코어 수학·크래시 안전성은 여전히 양호. 데이터 손실(파괴적 마이그레이션)은 콘솔/빌드와 함께 확인."**

---

## 변경 이력
| 일자 | 변경 |
|---|---|
| 2026-06-11 | develop @ 5921936 최신화. 6/08 문서 **무효화**(워치·AI·알림이 신규 구현됨). 신규 코드 정적분석으로 재작성 — 워치 Capability 불일치(D-01)·`isEmulatorTestMode=true` 하드코딩(D-02)·커스텀 이름 미반영(D-03)·워치 sendMessage 예외 미방어(D-04)·AI 실패 폴백 은폐(D-05)·알림 권한회수/재부팅 소실(D-06) 코드 라인 근거 명시. **정정**: Gemini는 하드코딩 키 아님(Firebase AI Logic) / 정확 알람 권한은 올바르게 분기됨. DB 파괴적 마이그레이션 데이터 손실 경고 추가 |
