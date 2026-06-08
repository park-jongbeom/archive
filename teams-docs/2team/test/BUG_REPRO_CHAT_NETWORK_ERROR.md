# BUG_REPRO_CHAT_NETWORK_ERROR

# 버그 재현 가이드 — 관심사 설정 직후 Chat 진입 시 "네트워크 문제" 오표시

---

## 0. 버그 요약

신규 계정으로 로그인 → Pick 5 Topics 완료 → AI Chat 진입 시, 네트워크가 정상인데도 "네트워크 문제로 동작할 수 없다" 류 안내가 표시된다.

`users/{uid}/user_learning_preference/current` 문서 작성이 Chat 진입 시점보다 느릴 때 발생하는 race condition 으로 추정. **타이밍이 좁을수록 잘 재현된다.**

## 1. 사전 준비 (디바이스만)

- 실 디바이스에 `devDebug` 빌드 설치
- Wi-Fi 또는 LTE 정상
- 다음 중 하나 준비:
  - 한 번도 앱에 로그인한 적 없는 **새 Google 계정**, 또는
  - 이미 쓰던 테스트 계정 + 앱 데이터 초기화 후 재로그인

> 앱 데이터 초기화: Android 설정 → 앱 → Umma → 저장공간 → "데이터 삭제"

## 2. 재현 흐름 (디바이스만)

1. (필요 시) 앱 데이터 초기화 → 앱 실행 → Google 로그인.
2. Pick 5 Topics 다이얼로그가 뜨면 5개 선택 → **Done** 탭.
3. Dashboard 가 보이는 순간 **지체 없이 AI 대화 카드를 즉시 탭**한다.
   - 다른 카드 둘러보지 말 것. 1초도 기다리지 말 것.
4. Chat 화면에 "네트워크 문제" 류 안내가 표시되면 → **재현 성공**.

## 3. 재현율을 올리는 팁

race 가 좁아 한 번에 안 잡힐 수 있다. 다음 중 하나를 적용해 윈도우를 넓힌다.

| 방법 | 어떻게 | 효과 |
| --- | --- | --- |
| 네트워크 throttling | Android 개발자 옵션 → "네트워크 속도 제한" 또는 Wi-Fi 끄고 LTE 약한 신호에서 시도 | preference 동기화가 느려져 race window 가 넓어짐 |
| 비행기 모드 토글 직후 시도 | Pick 5 선택 직전에 비행기 모드 ON → OFF → 바로 Done | Firestore write 가 큐잉되어 지연 |
| 백그라운드 앱 다수 | 메모리 압박이 큰 상태로 시도 | preference write 가 더 늦게 commit 됨 |
| 재시도 | 매 시도 사이에 앱 데이터 초기화 후 처음부터 다시 | 한 번의 라운드를 깨끗하게 분리 |

## 4. 네트워크가 진짜 정상임을 함께 확인

같은 디바이스에서 아래가 정상이어야 "네트워크 문제" 메시지가 오표시임이 명확해진다.

- Dashboard 의 통계·플래시카드 카드 데이터가 정상 표시된다.
- 다른 화면(통계, 플래시카드) 진입에 문제 없다.
- Wi-Fi / LTE 표시 정상.

## 5. 재현된 직후 회복 동작 (참고)

- 앱 강제 종료 후 재실행 → Chat 정상 진입되는 경우가 많다.
- 또는 Dashboard 학습 언어 selector 를 다른 언어로 바꿨다 되돌리면 preference 가 재작성되어 정상화된다.

이 두 회복 경로가 동작한다는 것 자체가 "데이터 누락 (preference 미작성)" 케이스를 "네트워크 문제" 로 잘못 분류한 메시지 매핑 결함이라는 증거다.

## 6. 자연 재현이 너무 안 잡힐 때 (선택 사항)

자연 재현 시도를 여러 번 해도 안 잡히면, 같은 상태를 디바이스 외부에서 결정적으로 만들 수 있는 도구를 따로 마련해 뒀다.
시간 압박이 있을 때만 사용한다.

- 도구: [`ResetUserStateE2ETest#deleteUserPreferences_reproducesBug`](../repo/app/src/androidTest/java/com/app/umma/devtools/ResetUserStateE2ETest.kt)
- 자연 발생 케이스와 화면 상 결과(메시지·UI)는 동일하다.
