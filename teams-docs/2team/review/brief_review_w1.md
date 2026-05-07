# Brief 검토 — 2팀 / Umma / Week 1

> **검토 대상**:
> - Brief: [`../product_brief/Product Brief v1 - Umma`](../product_brief/Product%20Brief%20v1%20-%20Umma)
> - 1차 회의록 (5/4 월): [`../mom/260506.pdf`](../mom/260506.pdf) — 17페이지
> - 2차 회의록 (5/6 수): [`../mom/260507.pdf`](../mom/260507.pdf)
> - 시스템 스펙: [`../spec/SYS-APP-ARCHITECTURE.pdf`](../spec/SYS-APP-ARCHITECTURE.pdf), [`../spec/System Flow DB v1 - Umma.pdf`](../spec/System%20Flow%20DB%20v1%20-%20Umma.pdf)
>
> **검토 시점**: 2025-05-07 (v5 갱신 — AI 활용 한계 + 백엔드/DB 결합 우려 반영)
> **검토자**: 보조강사
> **종합 신호등**: 🟡 (v4 🟢 → v5 🟡 보정 — *"AI 학습 vs 활용"*, *"백엔드 비즈니스 로직 위치"* 두 영역이 미정의 상태로 식별됨)
> **동반 문서**: [`advisory_ai_backend_w1.md`](advisory_ai_backend_w1.md) — AI 활용 한계 + 백엔드 의사결정 상세 자료

---

## 0. 한 줄 요약 (v5)

v4에서 종합 🟢 로 평가했으나 **두 가지 미정의 영역이 식별되어 🟡 로 보정**합니다:

1. ⚠️ **"AI를 통한 학습" vs "AI 활용 (Prompt Engineering)"** — 1차 회의록 13~15p의 7-Layer 30+ 지표는 학문적으로 **NLP 박사급 R&D 영역**. 6주 + NLP 전공자 0명으로는 **직접 학습 불가**. 유일한 길은 **Gemini API에 프롬프트로 요청**인데, 이는 응답 일관성 / 정확도 / 비용 / 지연의 4가지 한계가 있음. 회의록 어디에도 이 차이가 명시 안 됨.

2. ⚠️ **백엔드 비즈니스 로직 위치 미정의** — [SYS-APP-ARCHITECTURE](../spec/SYS-APP-ARCHITECTURE.pdf) 의 *"Networking: -"* + Firestore와 분석 로직의 연결 미명시 = **분석이 어디서 도는지 미결정**. 7-Layer 누적 / 시계열 집계 / 동기화 정책 모두 미정.

이 두 영역의 결정 절차는 [advisory_ai_backend_w1.md](advisory_ai_backend_w1.md) 에 상세히 정리됨. 1주차에 결정하면 종합 🟡 → 🟢 회복 가능.

**v1 → v5 변화 요약**:
- v1~v3: Brief 자체만 보던 단계 (🔴 → 🟡)
- v4: 회의록 + 스펙 추가로 *"강팀 + 1차에서 워치 합의 + 깊이 있는 기획"* 검증 → 🟢
- v5: 깊이의 이면에 있는 **구현 격차 + 백엔드 미정의** 식별 → 🟡 보정

---

## 0-1. 팀 구성 (v4 그대로 유지)

| 역할 | 이름 | 분류 | 회의록 검증 |
|---|---|---|---|
| **팀장** | 박재민 | 🟢🟢 상위 (실력자) | 1차 13p *"context 압축 모델 / 2계층 메모리"* — NLP 시스템 설계 가능 |
| **부팀장** | 정원화 | 🟢🟢 상위 (학습 성취 매우 높음) | 1차 7~10p 시장 분석 + 11~12p 프롬프트 설계 — 창업/UX IR 수준 |
| 팀원 | 김태환 | 🟢 상위 | 수학강사 출신 — SRS 알고리즘 (8p) 영역 적합 |
| 팀원 | 김명준 | 🟡 적응 단계 | UI 기본 틀 + 페어 |
| 팀원 | 정재훈 | 🟡 적응 단계 (개선 추세) | UI 기본 틀 + 페어 |

**v5 코멘트**: 강팀이라는 평가는 유지. 다만 **"강팀이 NLP 박사급 영역을 6주에 만들 수 있다"** 와는 다른 차원. 강팀은 *"한계를 인식하고 압축하는 결정"* 을 빠르게 할 수 있는 팀.

> 출처: [`../members`](../members), 회의록

---

## 1. 신호등 요약 (v5)

| 항목 | v4 | v5 | 근거 |
|---|---|---|---|
| **종합 신호등** | 🟢 | **🟡** | AI 활용 한계 + 백엔드 미정의 식별 |
| 워치 연동 | 🟢 | 🟢 | 1차 회의 16p Must 합의 (불변) |
| 분량 (Brief 170줄) | 🟡 | 🟡 | 가이드 § 4-4 위반 (불변) |
| Brief vs 1차 회의 정합 | 🟡 | 🟡 | 페르소나/워치 누락 (불변) |
| 시나리오 vs Flow 정의 | 🟡 | 🟡 | User Flow 별도 필요 (불변) |
| 성공 기준 측정 | 🟡 | 🟡 | "늘고 있다는 느낌" — 측정 불가 (불변) |
| MVP Must = 5 | 🟢 | 🟢 | 균형 양호 |
| 시장 분석 (TAM/SAM/SOM) | 🟢🟢 | 🟢🟢 | IR 수준 (불변) |
| 학술 근거 | 🟢🟢 | 🟢🟢 | 상위권 기획 능력 (불변) |
| **7-Layer 언어 분석 모델** | 🟡 | **🟡 (재평가)** | **30+ 지표 — Prompt Engineering 한계로 1~2 Layer 운영만 신뢰 가능** |
| 의존성 사슬 | 🟢 | 🟢 | 강팀 검증 |
| 작업 분배 | ✅ | ✅ | 회의록 검증 |
| 기술 스택 확정 | 🟢 | 🟢 | Clean+MVVM+Gemini Live |
| 데이터 구조 사고 | 🟢🟢 | 🟢🟢 | 6+ 모델 도출 |
| UI 상세 TBD | 🟢 | 🟢 | 가이드 § 5-5 정합 |
| User Flow 정의 | 🟡 | 🟡 | 불변 |
| Week 1 사용자 데모 | 🟡 | 🟡 | 불변 |
| Backlog | 🟡 | 🟡 | 불변 |
| **AI 활용 한계 인식 (신규 v5)** | (미평가) | **🟡** | **응답 일관성 / 정확도 / 비용 / 지연 4가지 명시 필요** |
| **백엔드 비즈니스 로직 위치 (신규 v5)** | (미평가) | **🟡** | **클라이언트 vs Functions 결정 미정** |
| **데이터 흐름 정책 (신규 v5)** | (미평가) | **🟡** | **Room/Firestore 분담 + 시계열 집계 + 동기화 미정** |

---

## 2. 잘한 점 (v4 유지)

v4에서 검증된 강점은 그대로 유지:

1. **상위권 시장 분석** (1차 회의록 7~10p)
2. **학술 근거 4건 인용** (Krashen / Vygotsky / Tomasello / Ebbinghaus)
3. **7-Layer 언어 분석 모델 도출** — 30+ 지표 (구현 한계는 § 3-7 참고)
4. **2계층 메모리 구조** (박재민) — NLP 시스템 설계 능력
5. **워치 연동 1차 합의** — *"필수 기능: 워치로 알림"* (1차 16p)
6. **가이드 정확히 학습 + 적용** — 2차 Backlog 9개 항목
7. **UI 상세 TBD 처리** — 가이드 § 5-5 정합
8. **작업 분배 즉시 실행**
9. **기술 스택 단단** — Clean Architecture + MVVM + Gemini Live API
10. **데이터 구조 사고 진입** — ConversationEntity (1차) + 6개 도메인 모델 (2차)

---

## 3. 짚어드릴 점 (v5 — 우선순위 순)

### 3-1. 🟡 Brief vs 1차 회의 정합 (v4 유지)

워치 / 페르소나 / 회화 정책 등 1차 회의 결정을 Brief에 반영. 상세는 v4 § 3-1 그대로.

### 3-2. 🟡 7-Layer MVP 압축 (v4 유지)

Week 1~2: 1~2 Layer (Vocabulary + Fluency 추천). 상세 v4 § 3-2.

### 3-3. 🟡 사용자 Flow vs 시스템 Flow 분리 (v4 유지)

User Flow DB + Backlog DB 구조 분리. 상세 v4 § 3-3.

### 3-4. 🟡 Week 1 사용자 데모 1개 (v4 유지)

AI 1턴 회화 추천. 상세 v4 § 3-4.

### 3-5. 🟡 Backlog 5~7 Issue 분해 (v4 유지)

회의록 9항목 형식 활용. 상세 v4 § 3-5.

### 3-6. 🟡 분량 / 시나리오 / 성공 기준 (v2 유지)

분량 80줄 / 단일 Flow / 측정 가능 문장. 상세 v2 § 3-2~3-4. 깊이 있는 자료는 `portfolio/` 분리.

---

### 3-7. 🟡 "AI 학습" vs "AI 활용" 인식 정리 (신규 v5)

**핵심 문제**: 1차 회의록 13~15p의 7-Layer 30+ 지표는 다음 학문적 영역에 속합니다:

| 지표 | 학문 영역 |
|---|---|
| Grammar layer (article / preposition / tense / ...) | **GED (Grammatical Error Detection)** — NLP 박사급 |
| vocabulary_level (CEFR A1~C2) | 코퍼스 언어학 + NLP 모델 |
| Fluency layer (response_latency / pause_frequency) | 음성 신호 처리 + 발화 분석 |
| Conversation layer (topic_expansion) | **담화 분석 (Discourse Analysis)** |
| Confidence layer (expression_confidence) | 심리 상태 추정 |

**현실**: 6주 + 5명 + NLP 전공자 0명 → **직접 학습 불가**. 유일한 길은 **Gemini API에 프롬프트로 요청 (Prompt Engineering)**.

**Prompt Engineering 4가지 한계** (구체 수치 → [advisory_ai_backend_w1.md § 1](advisory_ai_backend_w1.md)):

1. **응답 일관성 부재** — 같은 입력에 article_accuracy 75 / 65 / 80 다른 응답
2. **CEFR 등급 부정확** — Gemini도 정확하지 않음 (라벨링 부족)
3. **응답 비용 폭증** — 30+ 지표 평가 → 사용자당 $0.05~0.10/일
4. **응답 지연** — 7-Layer 분석 5~15초 (실시간 불가)

**Decision Log 필요 사항** (1주차 안에):
- 활성 Layer 1~2개 결정 (§ 3-2)
- 점수 → **등급화** (분산 흡수)
- 회화 종료 시 **1회 batch 평가** (실시간 X)
- *"우리는 NLP 학습이 아니라 prompt engineering으로 우회한다"* 명시

**1:1 시간**: 30분 (advisory § 1, § 7 참고)

---

### 3-8. 🟡 백엔드 비즈니스 로직 위치 결정 (신규 v5)

**현재 상태**:
- [SYS-APP-ARCHITECTURE](../spec/SYS-APP-ARCHITECTURE.pdf) — *"Networking: -"* (API 서버 없음 표시)
- 데이터 흐름 다이어그램에 **Firestore와 분석 로직 연결 미명시**

**미결정 4가지**:
1. 데이터 위치 (Room vs Firestore)
2. 분석 로직 위치 (클라이언트 vs Firebase Functions)
3. 시계열 집계 (실시간 vs 배치)
4. 다중 디바이스 동기화 (단일 / 폰+워치 / 폰+폰)

**가이드 § 11**: *"공용 계약 (모델/라우트/Repo 인터페이스)은 Owner가 관리"* — Owner 미정 상태.

**의사결정 트리** ([advisory § 3](advisory_ai_backend_w1.md)):

```
플래시카드 → 클라이언트 → Firestore (단순 CRUD)
대화 세션 → 진행 중 Room / 종료 후 Firestore
7-Layer 점수 → Firestore (사용자별 / 일자별)
시계열 집계 → Week 1~3 클라이언트 / Week 4+ Functions 검토
사용자 데이터 → Firestore
프롬프트 → 시스템 = 상수 / 사용자 커스텀 = Firestore
```

**1주차 산출물**:
- [SYS-APP-ARCHITECTURE](../spec/SYS-APP-ARCHITECTURE.pdf) 다이어그램에 데이터 흐름 추가
- Firestore 컬렉션 구조 결정 ([advisory § 4](advisory_ai_backend_w1.md))
- Brief에 백엔드 로직 위치 1줄 명시 (*"Week 1~5: 클라이언트 / Week 5+ 재평가"*)

**1:1 시간**: 60분 (advisory § 3 의사결정 트리 같이 보면서)

---

### 3-9. 🟡 7-Layer 신뢰성 검증 — 1 Layer 5번 평가 분산 측정 (신규 v5)

**원칙**: *"30+ 지표 중 어떤 게 신뢰 가능한지 모르면 6주 동안 부정확한 데이터를 사용자에게 보여주는 것"* 이 됩니다.

**검증 절차** ([advisory § 2](advisory_ai_backend_w1.md)):

1. 1 Layer 1 지표 선택 (추천: Vocabulary > lexical_diversity)
2. 5번 평가 → 표준편차 측정
3. 판정:
   - 표준편차 < 5 → 🟢 신뢰
   - 5~15 → 🟡 등급화 필요
   - > 15 → 🔴 폐기 또는 deterministic 알고리즘

**시점**: Week 2 데모 직후 (1시간 작업).

---

## 4. 1주차 1:1 의제 (v5 — 우선순위 순)

| # | 의제 | 시간 | 산출물 |
|---|---|---|---|
| **1** | 🟡 Brief v1.1 — 1차 회의 결정 반영 (워치/페르소나/회화) (§ 3-1) | 45분 | Brief v1.1 + Decision Log 2~3 |
| **2** | 🟡 7-Layer MVP 압축 — 활성 Layer 1~2개 결정 (§ 3-2) | 60분 | 활성 Layer 표 + Decision Log |
| **3** | 🟡 User Flow vs System Flow 분리 (§ 3-3) | 45분 | Flow DB(User) + Backlog DB |
| 4 | 🟡 Week 1 데모 시나리오 (AI 1턴) (§ 3-4) | 30분 | Demo Scenario v0 |
| 5 | 🟡 Backlog 5~7 Issue 분해 (§ 3-5) | 60분 | Ready Issue 5~7 |
| 6 | 🟡 깊이 있는 자료를 `portfolio/` 분리 (§ 3-6) | 20분 | 폴더 정리 |
| **7** | 🟡 *"AI 학습 vs 활용"* 인식 정리 (§ 3-7) | 30분 | Decision Log + Brief 1줄 명시 |
| **8** | 🟡 백엔드 비즈니스 로직 위치 + 데이터 흐름 결정 (§ 3-8) | 60분 | Architecture 다이어그램 + Decision Log |
| 9 | 🟡 1 Layer 신뢰성 검증 (Week 2 작업) (§ 3-9) | 60분 | 표준편차 측정 + Decision Log |

총 **약 6시간 30분** — 2~3회 회의로 분할.

**Phase 1 (1주차 안 — 2회, 4시간)**: 의제 1, 2, 7, 8
**Phase 2 (Week 2 시작 전 — 1회, 2시간)**: 의제 3, 4, 5, 6
**Phase 3 (Week 2 데모 직후 — 1시간)**: 의제 9

**핵심**: 의제 7, 8 이 1주차 후반의 가장 중요한 두 결정. 둘 다 [advisory_ai_backend_w1.md](advisory_ai_backend_w1.md) 에 상세 의사결정 가이드 있음.

---

## 5. 자주 보는 안티패턴 매칭 (v5)

| 안티패턴 | 매칭? | 비고 |
|---|---|---|
| Brief 5페이지 넘음 | ⚠️ | 170줄 — 압축 필요 |
| MVP에 Won't 없음 | ❌ | 3개 명시 |
| 타깃 "모든 사용자" | ❌ | 구체적 |
| Must 6개 이상 | ❌ | 5개 |
| Flow 5개 이상 | ⚠️ | System Flow 12개 — User Flow 분리 필요 |
| Brief에 워치 미정의 | ✅ → ⚠️ | 1차 회의 합의됨, Brief 반영 남음 |
| Brief vs 회의 결정 불일치 | ⚠️ | 페르소나 / 워치 등 누락 |
| MVP 지표 30+개 (과대) | ⚠️ | 7-Layer 압축 필요 |
| 1주차 디자인 풀 완성 시도 | ❌ | TBD 처리 |
| 타깃 추상적 표현 | ⚠️ | "초개인화" 마케팅 톤 |
| 작업 분배 미정 | ❌ | ✅ 완료 |
| Week 1 사용자 데모 부재 | ⚠️ | 미정 |
| Backlog 미시작 | △ | 박재민 준비 중 |
| **"AI 학습"과 "AI 활용" 혼동 (신규)** | ⚠️ | **회의록에 차이 명시 없음** |
| **백엔드 비즈니스 로직 위치 미정 (신규)** | ⚠️ | **클라이언트 vs Functions 결정 X** |
| **데이터 흐름 다이어그램 불완전 (신규)** | ⚠️ | **Firestore 연결 미명시** |
| **시계열 집계 정책 미정 (신규)** | ⚠️ | **누적 분석 미설계** |
| **다중 디바이스 동기화 미정 (신규)** | ⚠️ | **폰+워치 정책 미명시** |

---

## 6. 다음 검토 시점

- **24시간 내**: § 3-1 Brief v1.1 + § 3-2 7-Layer 압축 + **§ 3-7 AI 한계 인식 + § 3-8 백엔드 결정** Decision Log 확인
- **Week 1 종료 (2025-05-11)**: Brief v1.1 + Backlog 5~7 + Week 1 데모 1개 + portfolio/ 분리 + Architecture 다이어그램 갱신 검증
- **Week 2 데모 직후**: § 3-9 1 Layer 신뢰성 검증 결과 확인
- **Week 2 시작 전 (2025-05-12)**: FLOW-CHAT-CORRECT-SAVE 1턴 + Vocabulary Layer 1개 mock 동작
- **Week 3 시점**: 정원화님 부담(PO+UX+Researcher+멘토+개발 5역) 점검 / 박재민님 WIP 점검 / 적응 단계 2명 PR 머지
- **Week 4 시점**: Firebase Firestore 부하 → Functions 도입 여부 결정
- **Week 4 시점**: 7-Layer 추가 활성화 (1~2 → 3~4) 결정
- 매주 일요일 **Weekly Status Issue** 자동 점검은 별도 진행

---

## 7. 참고 문서

- **동반 advisory**: [`advisory_ai_backend_w1.md`](advisory_ai_backend_w1.md) — AI 활용 한계 + 백엔드 의사결정 상세
- 가이드 본문: [`../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md`](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)
- 안티패턴 식별표: [`../../../ta-guides/애자일_예제기반_FAQ.md`](../../../ta-guides/애자일_예제기반_FAQ.md) § 9
- 워치 연동 처리 (FAQ): [`../../../ta-guides/애자일_예제기반_FAQ.md`](../../../ta-guides/애자일_예제기반_FAQ.md) § 6
- User Flow 작성 예시: [`../../../final-project/docs/애자일/example/02_flow/flow_login.md`](../../../final-project/docs/애자일/example/02_flow/flow_login.md)
- Brief 작성 예시 (50줄): [`../../../final-project/docs/애자일/example/01_product_brief.md`](../../../final-project/docs/애자일/example/01_product_brief.md)
- Backlog/Issue 분해 예시: [`../../../final-project/docs/애자일/example/03_issue/`](../../../final-project/docs/애자일/example/03_issue/)
- 측정 가능 성공 기준: [`../../1team/product_brief/Product Brief_Scoffee_ver 1.0`](../../1team/product_brief/Product%20Brief_Scoffee_ver%201.0) § 10
- 역할 분담 가이드: [`../../../final-project/docs/애자일/04_애자일_역할_배분_가이드.md`](../../../final-project/docs/애자일/04_애자일_역할_배분_가이드.md)
- 팀 구성 출처: [`../members`](../members)

---

## 8. v1 → v5 변경 요약

| 변경 항목 | v1 | v2 | v3 | v4 | v5 |
|---|---|---|---|---|---|
| **종합 신호등** | 🔴 | 🔴 | 🟡 | 🟢 | **🟡 (보정)** |
| 워치 연동 | 🔴 | 🔴 | 🟡 | 🟢 | 🟢 |
| 의존성 사슬 | 🟡 | 🟢 | 🟢 | 🟢 | 🟢 |
| 작업 분배 | - | 🟡 | ✅ | ✅ | ✅ |
| 기술 스택 | - | - | 🟢 | 🟢 | 🟢 |
| 시장 분석 / 학술 근거 | - | - | - | 🟢🟢 | 🟢🟢 |
| 7-Layer 분석 모델 | - | - | - | 🟡 | 🟡 (Prompt Engineering 한계 명시) |
| Brief vs 회의 정합 | - | - | - | 🟡 | 🟡 |
| User Flow 정의 | - | - | 🟡 | 🟡 | 🟡 |
| Week 1 사용자 데모 | - | - | 🟡 | 🟡 | 🟡 |
| Backlog | - | - | 🟡 | 🟡 | 🟡 |
| **AI 활용 한계 인식 (v5 신규)** | - | - | - | - | **🟡** |
| **백엔드 비즈니스 로직 위치 (v5 신규)** | - | - | - | - | **🟡** |
| **데이터 흐름 정책 (v5 신규)** | - | - | - | - | **🟡** |

**v5 변경 트리거**:
- 사용자 우려 제기 (2025-05-07): *"AI 학습은 전공 영역인데, 그 부분 없이 그냥 진행하는 부분"* + *"학습은 백단과 DB 결합으로 처리해야 하는데 그런 부분이 고려된 걸로 보이는가"*
- 점검 결과: 두 우려 모두 정당. 회의록/스펙 어디에도 명시 안 됨
- 결과: 종합 🟢 → 🟡 보정 + 의제 3개 추가 (7, 8, 9) + 동반 advisory 문서 작성

**v4 → v5 핵심 메시지**:
- v4의 *"강팀 + 깊이 있는 기획"* 평가는 유지
- 다만 *"강팀이 NLP 박사급 영역을 6주에 만들 수 있다"* 는 별개 문제
- 강팀의 진짜 강점: *"한계 인식 후 빠르게 압축 결정"* 가능
- 1주차에 § 3-7, 3-8 결정만 하면 종합 🟡 → 🟢 회복

**v5 → v6 트리거 (다음 갱신 시점)**:
- Brief v1.1 작성 후 1차 회의 결정 반영 검증
- 7-Layer MVP 압축 + AI 활용 한계 인식 결정 결과
- 백엔드 비즈니스 로직 위치 + 데이터 흐름 다이어그램 갱신
- Week 1 데모 1턴 동작 검증
- Week 2 1 Layer 신뢰성 검증 결과
