# TEST_FLOW_CHAT_BEGINNER_30MIN

# Umma AI Chat — A1 초보자 페르소나 30분 연속 대화 스크립트

> 연결 문서: [`TEST_FLOW_CHAT_SCENARIOS.md`](./TEST_FLOW_CHAT_SCENARIOS.md), [`TEST_FLOW_CHAT_ADVERSARIAL.md`](./TEST_FLOW_CHAT_ADVERSARIAL.md), [`TEST_FLOW_COR.md`](./TEST_FLOW_COR.md), [`DEMO_FLOW_AI_CHAT.md`](../repo/docs/Sprint2/Demo/DEMO_FLOW_AI_CHAT.md), [`DEMO_FLOW_CORRECTION.md`](../repo/docs/Sprint2/Demo/DEMO_FLOW_CORRECTION.md)
> 작성 배경: ADV 적대적 시나리오와 짧은 EN-01~05 happy-path 외에, "진짜 초보자가 30분 연속으로 쓸 때" 대화 연속성·컨텍스트 유지·재연결 cleanup·LangState 반영 + Correction 카드 누적이 어떻게 보이는지 통합 검증.
> 사용법: `devDebug` Real / 학습 언어 = English. 마이크 권한 허용 후 아래 발화를 **그대로** 영어로 말한다. 각 발화에는 A1 학습자 특유의 자연스러운 오류 1~2개를 의도적으로 포함했다(Correction 카드 후보 생성용).

---

## 0. 사용 전제

- Variant: `devDebug` (Firebase Live / OpenAI Realtime)
- 학습 언어: **English** (Dashboard → Settings 또는 언어 토글에서 변경)
- 첫 진입이면 `Pick 5 Topics` 다이얼로그에서 5개 선택 → Done
- 시스템 prompt가 의도대로 동작하면 AI 응답이 **Beginner-safe**해야 함 (짧은 문장, 쉬운 어휘, 한 번에 하나의 질문) — [CHAT-TUNE-001 정책](../repo/docs/Sprint3/User_FlowDB/FLOW_AI_CHAT_IMPROVEMENTS/CHAT-TUNE-001_LangState_Prompt_Policy.md) 참고
- 페르소나: "한국 직장인, 영어 공부 시작한 지 한 달, 어휘량 작음, 자신 없음" — 발화 톤을 망설이듯 천천히

## 1. 시간 배분 가이드 (총 ~30분)

| 블록 | 턴 수 | 누적 시간 | 주제 |
| --- | --- | --- | --- |
| Block 1 | 3턴 | ~3분 | 인사 / 자기소개 |
| Block 2 | 4턴 | ~7분 | 오늘 하루 |
| Block 3 | 5턴 | ~13분 | 음식 / 점심 |
| Block 4 | 4턴 | ~18분 | 주말 계획 |
| Block 5 | 4턴 | ~22분 | 가족 / 친구 |
| Block 6 | 3턴 | ~26분 | 영어 공부 자체 이야기 (meta) |
| Block 7 | 2턴 | ~28분 | 마무리 |
| Handoff | — | ~30분 | Dashboard → Correction 진입 |

각 턴 = 발화 5–10초 + AI 응답 15–25초 + 응답 듣고 다음 발화 준비 30–45초 ≈ 평균 1분.

## 2. 발화 가이드 (공통)

| 항목 | 권장 |
| --- | --- |
| 속도 | 또박또박. 한 단어씩 끊어 말해도 됨 |
| 망설임 | "uh...", "umm..." 섞어도 자연스러움 (실제 초보자 패턴) |
| 의도된 오류 | 굵게 표시된 부분은 **그대로 틀린 채** 말할 것 (Correction 카드 후보가 됨) |
| 자막 | 필요 시 자막 On — 단 자막을 보고 발음 따라 하지는 말 것 (테스트 오염) |
| 휴식 | Block 사이 짧게 호흡 (5초). 화면 이탈 금지 (cleanup 테스트는 마지막에) |

---

## 3. Block 1 — 인사 / 자기소개 (3턴, ~3분)

### B1-T1 · 첫 인사

- **테스터 발화 (그대로)**:
  > "Hello. I **am** Korean. **My name Jongbeom**. Nice to meet you."
- **의도된 오류**: "My name **is** Jongbeom" (be동사 누락)
- **기대 AI 응답 톤**: "Hi Jongbeom! Nice to meet you too. Where are you from in Korea?" 같은 짧은 인사 + 한 가지 질문
- **확인 포인트**:
  - 응답이 짧고 단순한지 (Beginner-safe)
  - 한국어 보조 설명이 최소거나 없음 (학습 언어 우선)

### B1-T2 · 거주지

- **테스터 발화**:
  > "I live **in Seoul** city. **It big city** with many people."
- **의도된 오류**: "It **is a** big city" (be동사 + 관사 누락)
- **AI 후속 hook 기대**: "Do you like living in Seoul?" 같은 yes/no 질문

### B1-T3 · 직업

- **테스터 발화**:
  > "I am office worker. I **work computer company** in Gangnam."
- **의도된 오류**: "I work **at a** computer company" (전치사 + 관사 누락)
- **AI 후속 hook 기대**: 직업 관련 짧은 follow-up

---

## 4. Block 2 — 오늘 하루 (4턴, ~4분)

### B2-T1 · 오늘 무엇

- **테스터 발화**:
  > "**Today I wake up** at seven o'clock. I **eat** rice and kimchi for breakfast."
- **의도된 오류**: "Today I **woke up**", "I **ate**" (과거시제)
- **확인 포인트**: AI가 과거시제로 응답하는지 (컨텍스트 매칭)

### B2-T2 · 교통

- **테스터 발화**:
  > "I take **subway** to work. **It take** about one hour."
- **의도된 오류**: "I take **the** subway", "It **takes**" (관사 + 3인칭 단수)

### B2-T3 · 오늘 기분

- **테스터 발화**:
  > "Today I **feel** little tired. **Many work** in morning."
- **의도된 오류**: "**a** little tired", "**A lot of work**" (관사 + 어순)
- **AI 후속 hook 기대**: 공감 + 위로

### B2-T4 · 점심 시간

- **테스터 발화**:
  > "Now is lunch time. I **don't decide** yet what to eat."
- **의도된 오류**: "I **haven't decided**" (현재완료) — 단, A1에서 어려우니 "I **didn't decide**" 라고 말해도 충분히 후보로 잡힘

---

## 5. Block 3 — 음식 / 점심 (5턴, ~6분)

### B3-T1 · 좋아하는 음식

- **테스터 발화**:
  > "I **like** spicy food. **My favorite is** kimchi jjigae and bulgogi."
- **의도된 오류**: "spicy food" 자체는 OK. "kimchi jjigae and bulgogi" 둘 다 가능 → AI가 둘 중 하나로 follow-up 유도

### B3-T2 · AI 응답에 대한 반응

- **테스터 발화** (AI가 좋아하는 한식이 뭐냐고 물으면):
  > "I like also Japanese food. **Specially** ramen and sushi."
- **의도된 오류**: "**Especially**" (단어 혼동, 매우 흔한 한국인 실수)

### B3-T3 · 식당 추천 요청

- **테스터 발화**:
  > "Can you **recommend** good restaurant **for** lunch?"
- **의도된 오류**: "a good restaurant" (관사 누락)
- **확인 포인트**:
  - AI가 실제 가게 이름을 hallucination 으로 추천하는지 (LLM09 회귀)
  - Beginner-safe 응답 유지 — 만약 5개 길게 리스트하면 정책 위반

### B3-T4 · 가격 이야기

- **테스터 발화**:
  > "**It expensive**? I want cheap restaurant. **Maybe under** ten thousand won."
- **의도된 오류**: "**Is it** expensive?" (의문문 어순)

### B3-T5 · 메뉴 선택

- **테스터 발화**:
  > "OK, I will **eat kimbap** today. **Easy and fast**."
- **의도된 오류**: 큰 오류 없음 — 자연스러운 마무리 발화 + AI가 follow-up 줄 수 있게

---

## 6. Block 4 — 주말 계획 (4턴, ~5분)

### B4-T1 · 주제 전환

- **테스터 발화**:
  > "Next topic please. **This weekend, what should I doing**?"
- **의도된 오류**: "**What should I do**" (gerund/원형 혼동)
- **AI 응답 기대**: 주제 전환 자연스럽게 받아들이는지 (컨텍스트 reset이 너무 hard 하지 않은지)

### B4-T2 · 취미

- **테스터 발화**:
  > "I **like watch** movies. Last weekend I **see** new Korean movie."
- **의도된 오류**: "I like **to watch** / **watching**", "I **saw**" (to부정사/동명사 + 과거)

### B4-T3 · 영화 감상

- **테스터 발화**:
  > "Movie was very interesting. **The hero** **fight** with bad people."
- **의도된 오류**: "**fights** / **fought**" (시제 + 3인칭 단수)

### B4-T4 · 이번 주말 계획

- **테스터 발화**:
  > "This Saturday I **will go** Han River with friend. We **want** picnic."
- **의도된 오류**: "I will go **to** Han River", "We want **to** picnic" / "We want **a** picnic" (전치사 + 부정사/관사)

---

## 7. Block 5 — 가족 / 친구 (4턴, ~4분)

### B5-T1 · 가족 구성

- **테스터 발화**:
  > "**My family is four people**. Father, mother, sister, and me."
- **의도된 오류**: "**There are four people in my family**" (구조 자체 비영어식)

### B5-T2 · 형제

- **테스터 발화**:
  > "My sister is younger **two years than me**. She **work in** hospital."
- **의도된 오류**: "**two years younger than me**" (어순), "She **works at a** hospital" (3인칭 + 전치사)

### B5-T3 · 친구

- **테스터 발화**:
  > "I have **many friend** from university. We sometimes **meet at weekend**."
- **의도된 오류**: "many **friends**", "**on weekends**" (복수 + 전치사)

### B5-T4 · 친구 활동

- **테스터 발화**:
  > "We **drink coffee and talking** about old time."
- **의도된 오류**: "drink coffee and **talk**" (병렬 시제), "old **times**" (복수)

---

## 8. Block 6 — 영어 공부 자체 (meta, 3턴, ~4분)

> 이 블록은 LangState가 점진적으로 conversational depth를 인식하는지 보는 용도. 같은 학생이 30분간 대화 후 AI가 약간 더 i+1 표현을 시도하는지 관찰.

### B6-T1 · 공부 동기

- **테스터 발화**:
  > "I study English because **my job need** it. **Email and meeting** with foreigners."
- **의도된 오류**: "**my job needs**" (3인칭), "**Emails and meetings**" (복수)

### B6-T2 · 어려운 점

- **테스터 발화**:
  > "Speaking is **most hard** for me. I **can read** but **can not** speak well."
- **의도된 오류**: "**the hardest**" (최상급), "**cannot**" 또는 "**can't**" (표기는 STT 이슈일 수 있음)

### B6-T3 · 도움 요청

- **테스터 발화**:
  > "Please **speak slowly** and use **easy word**. **It help** me a lot."
- **의도된 오류**: "**easy words**", "**It helps**" (복수 + 3인칭)
- **확인 포인트**: AI가 발화 길이/속도를 조정하는지 (인지적 응답)

---

## 9. Block 7 — 마무리 (2턴, ~2분)

### B7-T1 · 감사

- **테스터 발화**:
  > "Thank you for the **conversation**. I **learn** many new word today."
- **의도된 오류**: "I **learned**", "many new **words**"

### B7-T2 · 작별

- **테스터 발화**:
  > "I **will study** more and **come back tomorrow**. Have a good day. Bye."
- **의도된 오류**: 큰 오류 없음 — 자연스러운 작별
- **AI 응답 기대**: 짧은 작별 + 격려

---

## 10. Handoff → Correction 화면

### 진입 절차

1. 마지막 응답 종료 후 `READY/IDLE` 복귀 확인
2. 뒤로 가기 또는 Dashboard 버튼 → Dashboard 복귀
3. **OS 마이크 indicator 즉시 내려가는지 확인** ([TC-CH-13](./TEST_FLOW_CHAT.md))
4. Dashboard 우상단 "교정 대기" 빨간 점 활성 확인
5. 카드 탭 → Correction 화면 진입 (Loading)
6. `SessionSummary.correctionAvailable == true` 자동 판정 → Gemini 호출 → Content 상태 카드 노출

### 예상 교정 카드 수

| 블록 | 의도된 오류 수 | 예상 카드 후보 |
| --- | --- | --- |
| Block 1 | 4 | 3~4장 |
| Block 2 | 5 | 3~4장 |
| Block 3 | 5 | 3~4장 |
| Block 4 | 5 | 3~4장 |
| Block 5 | 5 | 3~4장 |
| Block 6 | 5 | 3~4장 |
| Block 7 | 2 | 1~2장 |
| **합계** | **~31** | **17~25장** |

> Gemini가 모든 turn을 카드로 변환하지 않고 "no correction needed"로 skip 할 수 있음([CorrectionPromptBuilder.kt:49](../repo/app/src/main/java/com/app/umma/data/repository/correction/CorrectionPromptBuilder.kt#L49) — `Skip a candidate only if no correction is needed`). 실제 카드 수는 후보 추출 정책에 따라 달라진다.

### 교정 결과 확인 포인트

- [ ] 카드가 **17~25장** 범위인지 (너무 적으면 후보 추출 누락, 너무 많으면 후보 필터 미작동)
- [ ] 각 카드의 `nativeText` 가 **한국어**인지 ([CorrectionPromptBuilder.kt:46](../repo/app/src/main/java/com/app/umma/data/repository/correction/CorrectionPromptBuilder.kt#L46))
- [ ] `afterText` 가 **영어**이고 의미가 보존되었는지
- [ ] `explanation` 이 **한국어 60자 이내**인지
- [ ] `candidateId` 가 중복 없이 매칭되는지
- [ ] 다음 핵심 오류가 최소 1건씩 카드 후보로 노출되는지:
  - [ ] be동사 누락 (B1-T1, B1-T2)
  - [ ] 관사 a/an/the (B1-T3, B2-T2, B3-T3)
  - [ ] 시제(과거) (B2-T1, B4-T2)
  - [ ] 3인칭 단수 -s (B2-T2, B5-T2, B6-T1)
  - [ ] 복수형 (B5-T3, B6-T3)
  - [ ] 전치사 (B5-T3 `on weekends`)
  - [ ] 의문문 어순 (B3-T4)
  - [ ] 단어 혼동 (B3-T2 `Specially → Especially`)
  - [ ] 어순(비교급) (B5-T2 `younger two years → two years younger`)

### 저장 흐름 검증 (선택)

- 위 카드 중 **2~3장 선택** → Save → Done 상태 → Dashboard 복귀
- Dashboard에서 Flashcard due 수치가 저장한 카드 수만큼 증가했는지 확인 ([DEMO_FLOW_CORRECTION 시나리오 3](../repo/docs/Sprint2/Demo/DEMO_FLOW_CORRECTION.md))

---

## 11. 30분 통합 합격 기준

| 항목 | 합격 조건 |
| --- | --- |
| 세션 안정성 | 25턴 동안 `FatalError` 또는 `ReconnectFailed` 미발생 |
| 마이크 cleanup | 마지막 Dashboard 복귀 시 OS mic indicator 즉시 내려감 |
| Beginner-safe 유지 | 마지막 턴까지 AI 응답이 짧고 쉬운 어휘 중심 — Block 6 meta 대화에서 살짝 i+1 시도는 OK |
| 컨텍스트 연속성 | Block 사이 주제 전환 후에도 학생 이름(Jongbeom) / 직업(office worker) / 서울 거주 같은 초반 컨텍스트가 기억되는지 (recentFullContext 12턴 윈도우 — [BuildPromptUseCase.kt:43-48](../repo/app/src/main/java/com/app/umma/domain/usecase/chat/BuildPromptUseCase.kt#L43-L48)) |
| 언어 정책 | 학습 언어 English 유지, 한국어 보조 설명은 최소 (Beginner 정책 허용 범위) |
| Correction handoff | 30분 후 Dashboard 빨간 점 활성, Correction 진입 시 Loading → Content |
| Correction 결과 | 카드 17~25장, 의미 보존, 한국어 explanation 60자 이내 |
| Flashcard 저장 | 선택한 카드만큼 Dashboard due 증가 |

---

## 12. 진행 체크리스트

| Block | Turn | 발화 OK | AI 응답 적절 | 비고 |
| --- | --- | --- | --- | --- |
| B1 | T1 인사 | | | |
| B1 | T2 거주지 | | | |
| B1 | T3 직업 | | | |
| B2 | T1 기상 | | | |
| B2 | T2 교통 | | | |
| B2 | T3 기분 | | | |
| B2 | T4 점심 미결 | | | |
| B3 | T1 좋아하는 음식 | | | |
| B3 | T2 일식 | | | |
| B3 | T3 추천 요청 | | | |
| B3 | T4 가격 | | | |
| B3 | T5 결정 | | | |
| B4 | T1 주제 전환 | | | |
| B4 | T2 영화 | | | |
| B4 | T3 영화 감상 | | | |
| B4 | T4 주말 계획 | | | |
| B5 | T1 가족 구성 | | | |
| B5 | T2 동생 | | | |
| B5 | T3 친구 | | | |
| B5 | T4 친구 활동 | | | |
| B6 | T1 공부 동기 | | | |
| B6 | T2 어려운 점 | | | |
| B6 | T3 도움 요청 | | | |
| B7 | T1 감사 | | | |
| B7 | T2 작별 | | | |
| Handoff | Dashboard 복귀 | | | mic indicator 즉시 down |
| Correction | 진입 + 카드 노출 | | | 카드 수: ___ |
| Save | 카드 2~3장 저장 | | | due 증가: ___ |

---

## 13. 변형 옵션

이번 30분 1회 통과 후 다음 회차에는 한 가지 변수를 바꿔 회귀 검증:

- **변형 A — 일본어**: 같은 흐름을 `日本語` 학습 모드로 진행 ([TEST_FLOW_CHAT_SCENARIOS.md JA-01~05](./TEST_FLOW_CHAT_SCENARIOS.md) 패턴 활용)
- **변형 B — 중간 끊기**: Block 4 끝에서 의도적으로 백그라운드 전환 → 5분 후 재진입. cleanup + 재연결 + 컨텍스트 보존 동시 검증 ([RecordingInterrupted / ReconnectSuccess](../repo/docs/Sprint2/Demo/DEMO_FLOW_AI_CHAT.md))
- **변형 C — Intermediate 페르소나**: LangState를 Intermediate 로 변경(또는 별도 계정) 후 같은 발화. AI 응답이 i+1 표현(follow-up 질문, 자연스러운 표현 제안) 으로 어떻게 바뀌는지 비교
