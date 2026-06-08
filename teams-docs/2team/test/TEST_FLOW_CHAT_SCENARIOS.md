# TEST_FLOW_CHAT_SCENARIOS

# Umma AI Chat — 영어/일본어 테스트 대화 시나리오

> 연결 문서: [`TEST_FLOW_CHAT.md`](./TEST_FLOW_CHAT.md), [`DEMO_FLOW_AI_CHAT.md`](../repo/docs/Demo/DEMO_FLOW_AI_CHAT.md)
> 사용법: `devDebug` Real 검증에서 학습 언어(English / 日本語)별 PTT → AI 응답 → 자막 → Correction handoff 흐름을 검증하기 위한 대화 스크립트.

---

## 0. 사용 전제

- Variant: `devDebug` (Firebase Live API)
- Dashboard에서 학습 언어를 **English** 또는 **日本語**로 전환 후 Chat 진입
- 매 시나리오 종료 시 Dashboard 복귀 → 교정 대기 카드 활성 확인 (TC-CH-12)
- 발화 길이: 한 턴당 5–12초, PTT press 후 0.5초 호흡 → 발화 → release

## 1. 테스터 공통 가이드라인

| 항목 | 권장 |
| --- | --- |
| 환경 | 조용한 실내 + 이어폰 |
| 속도 | 한국어 평소 속도의 90% |
| 발음 | 또렷하게, 문장 끝 톤 떨어뜨리기 |
| 의도적 오류 | "Correction trigger" 표시된 문장은 **틀린 채로** 그대로 말할 것 (교정 카드 검증 목적) |
| 화면 이탈 테스트 | TC-CH-13/14는 시나리오 중간에 의도적으로 뒤로가기로 끼워넣기 |

---

## 2. 영어 시나리오 (EN-01 ~ EN-05)

### EN-01 · 자기소개 (Cold start, 1턴)

- **연결 AC/TC**: TC-CH-02, TC-CH-07~09 (PTT + AI 응답 happy path), TC-CH-10 (자막)
- **목적**: 학습 언어가 English로 반영되는지, 단일 턴이 정상 종료되는지
- **테스터 발화**:
  > "Hi, I'm a tester from Korea. I work as a software engineer and I'm trying out this app for the first time."
- **기대 AI 응답 특성**: 영어, friendly tone, follow-up 질문(직업/거주지 등) 1개 포함
- **확인 포인트**: 자막에 위 문장이 final로 정확히 캡션되는지

### EN-02 · 취미 대화 (2턴, 자연스러운 이어가기)

- **연결 AC/TC**: TC-CH-11 (1~2턴 연속), TC-CH-04 AC5 (재대화 복귀)
- **테스터 발화 (Turn 1)**:
  > "On weekends I usually go hiking near my house. It's a small mountain but the view is really nice."
- **(AI 응답 수신 → READY 복귀 확인)**
- **테스터 발화 (Turn 2)**:
  > "Yes, I went there last Saturday with my friend. We had coffee at the top."
- **확인 포인트**:
  - 각 턴 후 input animation 종료 → output animation → READY/IDLE 복귀
  - 두 번째 발화가 첫 발화의 컨텍스트(등산)를 잇는지 (학습 언어 컨텍스트 유지)

### EN-03 · 과거시제 오류 트리거 (Correction handoff 핵심)

- **연결 AC/TC**: TC-CH-12 (Correction handoff), CHAT-005 AC1·2·3
- **목적**: USER final이 교정 가능한 오류를 충분히 포함하도록 유도
- **테스터 발화** (오류 그대로):
  > "Yesterday I **go** to the restaurant and I **eat** pizza. It was very **delicious taste**."
- **의도된 오류**: 동사 시제(go→went, eat→ate), 어휘 중복(delicious + taste)
- **확인 포인트**:
  - Dashboard 교정 대기 카드 활성
  - Correction 화면 진입 시 위 오류 중 최소 1건이 카드 후보로 노출되는지

### EN-04 · 관사/전치사 오류 (Correction 다중 추출)

- **연결 AC/TC**: TC-CH-12, CHAT-005 AC1·2
- **테스터 발화** (오류 그대로):
  > "I have **interest in** **the** music. **In** weekend, I listen **to the** jazz **in** my car."
- **의도된 오류**: I'm interested in / on weekends / listen to jazz / in my car (마지막은 의도적 ambiguity)
- **확인 포인트**: 짧은 한 문장에서 final turn이 한 번만 저장되는지 (중복 저장 방지, CHAT-005 AC5와 교차 확인)

### EN-05 · 의견 표현 (긴 발화, i+1 난이도)

- **연결 AC/TC**: TC-CH-09 (긴 응답 음성 재생), TC-CH-11, CHAT-004 AC5
- **테스터 발화**:
  > "I think remote work is better than office work because I can focus more at home, but sometimes I miss talking with my coworkers in person."
- **확인 포인트**:
  - AI 응답이 사용자 수준보다 약간 높은(i+1) 표현·후속 질문을 포함
  - 출력 애니메이션이 응답 종료와 정확히 동기화

---

## 3. 일본어 시나리오 (JA-01 ~ JA-05)

### JA-01 · 自己紹介 (Cold start, 1턴)

- **연결 AC/TC**: TC-CH-02, TC-CH-07~10
- **테스터 발화**:
  > 「はじめまして。韓国から来ました。ソフトウェアエンジニアです。今日はこのアプリを初めて使います。」
  > 한글 음차: **하지메마시테. 캉코쿠카라 키마시타. 소후토웨아 엔지니아데스. 쿄-와 코노 아푸리오 하지메테 츠카이마스.**
  > 의미: 처음 뵙겠습니다. 한국에서 왔습니다. 소프트웨어 엔지니어입니다. 오늘 이 앱을 처음 사용합니다.
- **확인 포인트**: 자막이 일본어(한자+히라가나)로 정확히 표기, AI 응답도 일본어로 시작

### JA-02 · 趣味と週末 (2턴 이어가기)

- **연결 AC/TC**: TC-CH-11
- **테스터 발화 (Turn 1)**:
  > 「週末はよく家の近くの山に登ります。小さい山ですが、景色がとてもきれいです。」
  > 한글 음차: **슈-마츠와 요쿠 이에노 치카쿠노 야마니 노보리마스. 치-사이 야마데스가, 케시키가 토테모 키레-데스.**
  > 의미: 주말에는 자주 집 근처 산에 오릅니다. 작은 산이지만 경치가 아주 예쁩니다.
- **테스터 발화 (Turn 2)** (전 턴의 follow-up 답변):
  > 「はい、先週の土曜日に友達と行きました。山の上でコーヒーを飲みました。」
  > 한글 음차: **하이, 센슈-노 도요-비니 토모다치토 이키마시타. 야마노 우에데 코-히-오 노미마시타.**
  > 의미: 네, 지난주 토요일에 친구와 갔습니다. 산 정상에서 커피를 마셨습니다.
- **확인 포인트**: 두 턴 모두 final transcript가 한자/히라가나 혼용으로 캡션, 컨텍스트(登山) 유지

### JA-03 · 助詞ミス・て形ミス (Correction 핵심)

- **연결 AC/TC**: TC-CH-12, CHAT-005 AC1·2·3
- **테스터 발화** (오류 그대로 — 조사·て형 오류 의도):
  > 「昨日、友達**と**レストラン**を**行きました。ピザ**が**食べて、とても美味しいでした。」
  > 한글 음차: **키노-, 토모다치토 레스토랑〈오〉 이키마시타. 피자〈가〉 타베테, 토테모 오이시-〈데시타〉.** (강조 부분이 의도된 오류)
  > 의미: 어제 친구와 레스토랑〈을〉 갔습니다. 피자〈가〉 먹고, 아주 맛있〈었습니다〉.
- **의도된 오류**:
  - 「レストラン**へ/に**行きました」(を→へ/に)
  - 「ピザ**を**食べて」(が→を)
  - 「美味しい**でした**」→「美味し**かったです**」(い형용사 과거형)
- **확인 포인트**: Correction 카드 후보에 위 3건 중 최소 1건 노출

### JA-04 · 敬語/丁寧形ミックス (다중 교정)

- **연결 AC/TC**: TC-CH-12
- **테스터 발화** (오류 그대로 — 정중체/보통체 혼용):
  > 「私は音楽**好き**です。週末**は**ジャズを**聞く**、車の中で**よく聴きます**。」
  > 한글 음차: **와타시와 옹가쿠 〈스키〉데스. 슈-마츠〈와〉 쟈즈오 〈키쿠〉, 쿠루마노 나카데 요쿠 키키마스.** (강조 부분이 의도된 오류)
  > 의미: 저는 음악〈좋아〉합니다. 주말〈에는〉 재즈를 〈듣는다〉, 차 안에서 자주 듣습니다.
- **의도된 오류**: 「音楽**が**好きです」, 「ジャズを**聴いて**、車の中で〜」 (て形 접속)
- **확인 포인트**: 한 final turn에서 final 1회 저장 (중복 방지 교차 확인)

### JA-05 · 意見 (i+1 난이도)

- **연결 AC/TC**: TC-CH-09, CHAT-004 AC5
- **테스터 발화**:
  > 「リモートワークの方がオフィスワークよりいいと思います。家の方が集中できますが、同僚と直接話せないのは少し寂しいです。」
  > 한글 음차: **리모-토 와-쿠노 호-가 오피스 와-쿠요리 이-토 오모이마스. 이에노 호-가 슈-츄- 데키마스가, 도-료-토 쵸쿠세츠 하나세나이노와 스코시 사비시-데스.**
  > 의미: 재택근무가 사무실 근무보다 좋다고 생각합니다. 집이 더 집중할 수 있지만, 동료와 직접 이야기할 수 없는 것은 조금 외롭습니다.
- **확인 포인트**: AI 응답이 「〜について」「〜という点」 같은 i+1 패턴 포함, 음성 재생 끝까지 cleanup 정상

---

## 4. TC 매핑 요약

| 시나리오 | 주 TC | 보조 TC |
| --- | --- | --- |
| EN-01 / JA-01 | TC-CH-02, TC-CH-07~10 | TC-CH-04 (최초 마이크 권한) |
| EN-02 / JA-02 | TC-CH-11 | TC-CH-13 (Turn 사이 뒤로가기 끼워넣기) |
| EN-03 / JA-03 | TC-CH-12 | CHAT-005 AC1·2 |
| EN-04 / JA-04 | TC-CH-12 | CHAT-005 AC5 (중복 저장 방지 교차) |
| EN-05 / JA-05 | TC-CH-09, TC-CH-14 | 긴 AI 응답 중 뒤로가기 cleanup |

## 5. 권장 진행 순서

1. **EN-01 → JA-01** (학습 언어 전환 정상 동작 sanity)
2. **EN-02 → JA-02** (멀티 턴)
3. **EN-03/04 → JA-03/04** (Correction handoff 누적)
4. **EN-05 → JA-05** (장문 + cleanup)
5. 마지막에 Dashboard 교정 대기 카드 개수가 누적된 final turn 수와 일치하는지 일괄 확인
