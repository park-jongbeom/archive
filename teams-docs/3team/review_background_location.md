# 3팀 백그라운드 위치 처리 질의 — 보조강사 검토 의견

> **검토 배경:** 3팀이 "백그라운드 위치 연동을 ① Firestore Snapshot 구독 vs ② n초 주기 갱신 중 무엇으로 해야 하는가"를 질의함. 주강사는 "스냅샷이 소켓 방식이라 더 좋다"고 답변. 정우석님이 보조강사 재질의에서 "주기적 갱신(FusedLocationProvider)이 더 적합"이라고 의견 제시.
> **결론 한 줄:** 두 분 모두 맞는 말씀이지만 **서로 다른 레이어를 가리키고 있다**. "vs"가 아니라 **결합**해서 써야 한다.

---

## 1. 가장 먼저 정리할 것: "스냅샷"이 두 가지 의미로 섞여 있음

팀 회의록과 질문을 따라가다 보면, "스냅샷"이라는 단어가 **서로 다른 두 가지 기술**을 동시에 가리키며 혼용되고 있다.

| 구분 | 의미 | 레이어 | 주기 vs 비교 대상 |
|---|---|---|---|
| **A. Firestore `addSnapshotListener`** | 서버 데이터 구독(gRPC 스트림) | **READ** (서버 → 내 단말) | "5초 폴링으로 친구 위치 GET"의 반대 개념 |
| **B. `FusedLocationProvider.getLastLocation()`** | 단말의 마지막 알려진 위치 1회 조회 | **WRITE 측의 입력** (단말 GPS → 앱) | "`requestLocationUpdates` 콜백 구독"의 반대 개념 |

- **주강사 답변**("스냅샷이 소켓방식이라 더 좋다") = **A**(Firestore 리스너) 이야기. **맞는 말씀.**
- **정우석님 답변**("스냅샷(getLastLocation)은 일회성, 주기 갱신이 낫다") = **B**(단말 위치 수집) 이야기. **이 또한 맞는 말씀.**

**즉 두 분이 서로 다른 질문에 답하고 계신다.** 위치 공유 앱은 두 레이어를 동시에 써야 하므로, 어느 한쪽을 고를 문제가 아니다.

---

## 2. 데이터 흐름을 두 방향으로 분리해서 보기

```
[내 단말 GPS] ──(B. 수집)──▶ [내 앱] ──(필터링)──▶ [Firestore]
                                                       │
                                                  (A. 구독)
                                                       ▼
                                            [친구 단말 앱 화면]
```

- **WRITE 측 (내 위치를 Firestore에 쓰기)**
  → `FusedLocationProviderClient.requestLocationUpdates()` 콜백 + Foreground Service. **정우석님 안이 정답.**
- **READ 측 (친구 위치를 내 화면에 표시)**
  → Firestore `addSnapshotListener`. **주강사님 안이 정답.**
- 폴링(5초마다 친구 위치 GET)은 **READ 측에서 비효율** (변화가 없어도 매번 읽기 비용 발생). 회의록 7쪽 "결론" 그대로 **변경 즉시 SnapshotListener + 클라이언트 보간**이 표준.

---

## 3. 정우석님 답안 정·오 검증

### ✅ 정확한 부분

| 항목 | 평가 |
|---|---|
| Foreground Service 필수 | ✅ Android 14+ `FOREGROUND_SERVICE_LOCATION` 타입 선언까지 함께 명시되어 있어 정확 |
| `requestLocationUpdates` 채택 | ✅ 시스템에 콜백 위임 = 자체 타이머 루프보다 배터리 효율 우수 |
| Priority 동적 전환 (HIGH ↔ BALANCED) | ✅ PTT 활성 시 HIGH, 대기 시 BALANCED 전환 표준 패턴 |
| 거리 기반 필터링 (10m) | ✅ Firestore Write 비용 폭증 방지의 핵심 |
| `is_sharing=false`일 때 중단 | ✅ 사용자 의사 + 비용 양측에서 정답 |
| Activity Recognition 도입 | ✅ STILL/WALKING/IN_VEHICLE에 따른 주기 조정 표준 |

### ⚠️ 짚어줄 부분 (틀렸다기보다 보강이 필요한 지점)

#### (1) `getLastLocation()` ≠ "현재 위치 스냅샷"
정우석님 답변에 "스냅샷(getLatLastLocation)은 호출하는 시점의 일회성 데이터"라고 적혀 있는데, 엄밀히는:
- `getLastLocation()` = **마지막으로 알려진 위치**(최신 GPS fix가 아닐 수 있음, 캐시된 값)
- 단발 현재 위치 = `getCurrentLocation()` (Play Services 21+에서 제공)
- 본질적으로 "단발 vs 콜백 스트림"이라는 결론은 동일하므로 의사결정에는 영향 없음. 다만 팀 내 용어 통일은 권장.

#### (2) Activity Recognition은 만능이 아님
- **인식 지연:** STILL → WALKING 전환 감지에 보통 **30~60초** 걸린다. 사용자가 막 출발했을 때 첫 30초 위치가 누락되거나 끊길 수 있음.
- **오인식:** 차량 정차/지하철 정지 시 STILL로 잘못 분류되는 경우가 흔함.
- **권장 보완:** 단독 트리거로 쓰지 말고, 다음 둘과 **OR 조건**으로 묶을 것.
  - `Location.getSpeed() > 0.5 m/s` (실측 속도 기반)
  - 마지막 위치로부터 거리 변화 (`distanceTo() > 임계값`)
- 즉 "Activity Recognition 결과 STILL이면 무조건 중단"이 아니라, "STILL **이고** 5분간 위치 변화 없음"처럼 **다중 조건**으로 잠그는 것이 안전.

#### (3) "백그라운드에서 SnapshotListener 유지" 함정 주의
회의록 16쪽에도 정확히 적혀 있는 내용인데, **다시 한 번 강조**가 필요하다:
- **백그라운드에서 SnapshotListener 유지는 권장 X.** OS가 일정 시간 후 소켓을 닫고, 리스너는 먹통 또는 재연결 루프로 자원 소모.
- 따라서 **A(Firestore 구독)는 앱이 포그라운드일 때만 활성화**, **B(내 위치 WRITE)만 Foreground Service로 백그라운드 유지**가 맞다.
- "친구가 PTT 보낸 알림"은 SnapshotListener가 아니라 **FCM 푸시**로 깨워야 정상 동작한다.

#### (4) 주기 설계 시 Firestore 1Hz 제한
- 회의록 6쪽에 명시되어 있듯, **Firestore 단일 문서 쓰기는 초당 1회(1Hz) 이상 비권장**.
- 정우석님 안의 "PTT 활성 시 3~5초"는 안전한 범위. 다만 **HIGH_ACCURACY 전환 시에도 Write 주기는 3초 이상 유지**한다는 명세 한 줄을 박아두면 후속 분쟁 차단 가능.

---

## 4. 보조강사가 전달할 권고 — 명세에 박을 5줄

3팀이 워터폴로 가기로 한 만큼, **명세 단계에서 다음 5줄을 결정 사항으로 동결**하는 것이 핵심이다.

```
1. WRITE: Foreground Service(LOCATION 타입) + FusedLocationProvider.requestLocationUpdates 콜백.
2. WRITE 필터: 거리 ≥ 10m AND (시간 ≥ 5초) 충족 시에만 Firestore Write. 단일 문서 1Hz 상한 준수.
3. WRITE 주기 정책: PTT/이동 = 3~5초, 도보 = 10~30초, STILL+속도0+이동없음 3분 = 일시 중단.
4. READ: 포그라운드에서만 SnapshotListener 구독. 백그라운드에서는 끊고 FCM으로 대체.
5. Activity Recognition: 단독 판단 X. 속도/거리와 OR 조건으로만 사용.
```

---

## 5. 응답 시 톤 가이드 (보조강사 발화용 초안)

> "두 분 답변이 충돌하는 게 아니라 서로 다른 레이어 이야기라서, 결합해서 쓰셔야 합니다.
>
> - **친구 위치를 내 화면에 가져오는 부분(READ)** 은 주강사님 말씀대로 Firestore SnapshotListener가 정답입니다. gRPC 스트림이라 폴링보다 비용·실시간성 모두 우수합니다.
> - **내 위치를 Firestore에 쓰는 부분(WRITE)** 은 정우석님 말씀대로 FusedLocationProvider의 `requestLocationUpdates` 콜백 + Foreground Service가 정답입니다.
>
> 다만 두 가지만 주의하세요.
>
> 1. **백그라운드에서는 SnapshotListener를 유지하지 마세요.** OS가 소켓을 닫습니다. WRITE만 Foreground Service로 유지하고, READ는 포그라운드에서만 구독하시면 됩니다. PTT 도착 알림은 FCM으로 받으세요.
> 2. **Activity Recognition은 단독 판단 금지입니다.** 인식 지연이 30~60초라 첫 출발 시점 위치가 끊깁니다. 속도(getSpeed)나 거리 변화와 OR 조건으로 묶어서 쓰세요.
>
> 그리고 워터폴로 가시기로 하셨으니, 위 정책을 명세 단계에서 5줄 정도로 못 박아두시면 구현 단계에서 흔들림이 없습니다."

---

## 6. 한 줄 총평

> 정우석님 안은 거의 정답에 가깝다. **개념 혼동(스냅샷 두 가지)**을 풀어주고, **백그라운드에서 SnapshotListener 끊기**와 **Activity Recognition 다중 조건**만 보강해주면 명세로 그대로 사용 가능하다.
