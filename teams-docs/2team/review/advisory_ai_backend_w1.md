# Advisory — 2팀 / Umma / AI 활용 한계 + 백엔드·DB 결합

> **이 문서의 목적**: [`brief_review_w1.md`](brief_review_w1.md) v5 § 3-7, 3-8, 3-9 의 상세 자료. 1주차 1:1 시 학생들에게 직접 전달 가능한 의사결정 가이드.
>
> **대상**: 박재민 (팀장, 시스템 설계) / 정원화 (부팀장, 기획) / 김태환 (SRS 알고리즘) — 핵심 의사결정 3인. 김명준 / 정재훈은 결정 결과를 공유.
>
> **검토 시점**: 2025-05-07
> **검토자**: 보조강사

---

## 0. 이 문서의 의미

[1차 회의록](../mom/260506.pdf) 의 7-Layer 30+ 지표 + [System Architecture](../spec/SYS-APP-ARCHITECTURE.pdf) 의 *"Networking: -"* 가 의미하는 두 가지 미정의 영역을 1주차 안에 결정해야 합니다:

1. **AI를 직접 학습 vs Gemini API 활용 (Prompt Engineering)** — 학문적으로 다른 영역. 6주 + NLP 전공자 0명 현실에서 후자만 가능.
2. **백엔드 비즈니스 로직 위치 + 데이터 흐름** — 현재 시스템 다이어그램에 *"Firestore와 분석 로직 연결"* 이 없음. 분석이 어디서 도는지 미정의.

이 문서는 **"왜 이게 위험한지" + "어떻게 결정하는지"** 를 구체 수치 + 의사결정 트리로 제시합니다.

---

## 1. AI 활용 한계 — 구체 수치 4가지

### 1-1. 응답 일관성 부재 (LLM 본질)

| 입력 | 1차 응답 | 2차 응답 | 3차 응답 |
|---|---|---|---|
| *"I goes to school yesterday"* article_accuracy | 75 | 65 | 80 |
| 같은 입력 vocabulary_level | A2 | B1 | A2 |

**문제**: 사용자가 *"내 article 점수 75점"* 을 신뢰할 수 있나? **신뢰 불가**.

**완화책 (1주차 결정 필요)**:
- (a) 점수 → **등급화** (예: 70~80 = "양호") — 분산 흡수
- (b) **5번 평균** — 비용 5배 증가 (비추천)
- (c) **deterministic 프롬프트** (temperature=0) — 일부 개선, 완전 해결 X

→ 권장: **(a) 등급화** + 점수 0~100 표기 금지

### 1-2. CEFR 등급 추정 정확도

CEFR (A1/A2/B1/B2/C1/C2)는 유럽 표준이지만 **Gemini도 정확하게 라벨링하지 못합니다**. 이유:
- CEFR 라벨링 학습 데이터 부족
- 텍스트 1줄로 등급 판정 불가 (CEFR은 발화 패턴 누적 평가)

**검증 방법** (Week 1 데모 후 1시간 작업):
- 명확한 A1 문장 5개 + 명확한 B1 문장 5개 + 명확한 C1 문장 5개 = 15개 샘플
- Gemini에 vocabulary_level 분류 요청
- 정답률 측정. **70% 미만이면 vocabulary_level 지표 폐기 권장**

### 1-3. 응답 비용 폭증

7-Layer 30+ 지표를 매 회화 종료 시 평가하면:

| 시나리오 | 토큰 추정 | 일일 비용 (사용자 1명, 회화 3회/일) |
|---|---|---|
| 회화 자체 (AI 응답) | ~2,000 토큰 | 가정값 X (Live API) |
| 30+ 지표 평가 | ~5,000~10,000 토큰 | $0.05~0.10 |
| 7일 누적 | - | $0.35~0.70 |
| 1,000명 1주 | - | **$350~700/주** |

부트캠프 데모 단위 사용자 10~100명이라도 **누적 비용은 무시할 수 없음**. 1차 회의록 11p *"AI API 비용 최적화 필요"* 라고 적었지만 **30+ 지표 비용 계산은 안 함**.

**완화책**:
- (a) **활성 Layer 1~2개로 압축** (review v4 § 3-2 권장)
- (b) **회화 종료 시 1회 평가** (실시간 X)
- (c) **사용자가 명시적으로 "분석 보기" 클릭 시에만**

→ 권장: **(a) + (b)** 조합

### 1-4. 응답 지연

Gemini Flash 응답 시간:
- 짧은 프롬프트 + 짧은 응답: 1~3초
- 7-Layer 30+ 지표 분석 (긴 프롬프트 + JSON 구조 응답): **5~15초**

**문제**: 사용자가 *"분석 보기"* 누르고 15초 대기는 UX 깨짐.

**완화책**:
- 회화 종료 즉시 백그라운드 분석 시작 → 사용자가 다음 화면 진입 시 결과 표시
- Loading UI 명시 (가이드 § 5-2 정합)

---

## 2. 7-Layer 신뢰성 검증 절차 (Week 2 안에 1번 실행)

**핵심 메시지**: 30+ 지표 중 **어떤 게 신뢰 가능한지를 모르면 6주 동안 부정확한 데이터를 사용자에게 보여주는 것**입니다. **반드시 1번은 검증**.

### 절차 (1시간)

**1단계 — 1 Layer만 선택** (추천: Vocabulary > lexical_diversity)
- 이유: 텍스트 통계로 객관적 검증 가능. *"a, the, and"* 빈도 비율로 lexical diversity 직접 계산 후 Gemini 결과와 비교 가능.

**2단계 — 분산 측정**

```python
# 의사 코드
sample_input = "I goes to school yesterday and meet my friends"
gemini_results = []
for i in range(5):
    score = ask_gemini(f"Rate lexical_diversity (0-100) of: {sample_input}")
    gemini_results.append(score)

# 분산 계산
import statistics
print(f"Mean: {statistics.mean(gemini_results)}")
print(f"Stdev: {statistics.stdev(gemini_results)}")
```

**3단계 — 판정**

| 표준편차 | 판정 | 액션 |
|---|---|---|
| < 5 | 🟢 신뢰 | 그대로 사용 |
| 5~15 | 🟡 등급화 필요 | 0~100 점수 X, 4단계 등급 (낮음/보통/높음/매우높음) |
| > 15 | 🔴 폐기 | 해당 지표 제거 또는 deterministic 알고리즘으로 대체 |

**4단계 — Decision Log 1줄**

```
"Vocabulary Layer lexical_diversity: 표준편차 X.X로 판정 [🟢/🟡/🔴].
신뢰성 [높음/보통/낮음]. UI 표시 방식: [점수/등급/X]."
```

**5단계 (선택) — 다른 Layer로 확장**

Week 3~4에 동일 절차로 article_accuracy / topic_expansion 등 추가 검증.

---

## 3. 백엔드 비즈니스 로직 위치 — 의사결정 트리

### 데이터별 의사결정 (1주차 60분 회의)

```
플래시카드
  ├─ 작성/저장: 클라이언트 → Firestore (단순 CRUD) ✓
  ├─ SRS 시점 계산: 클라이언트 (수학 공식) ✓ 김태환님 영역
  └─ 카드 동기화: Firestore 단일 출처

대화 세션 (회화 로그)
  ├─ 진행 중: 클라이언트 메모리 (Room 임시)
  ├─ 종료 후: Firestore + Room 동기화
  ├─ 100턴 누적: Firestore에만 (대용량)
  └─ 7-Layer 분석: 회화 종료 시 클라이언트 → Gemini → Firestore에 결과 저장

7-Layer 점수 시계열
  ├─ 누적 위치: Firestore 컬렉션 (사용자별 / 일자별)
  ├─ 집계: Week 1~3 = 클라이언트가 read 후 계산 / Week 4+ Functions로 옮길지 평가
  └─ 시각화: 클라이언트 차트 (단순 그래프)

사용자 데이터 (닉네임/프로필)
  └─ Firestore (단일 출처) ✓

프롬프트 (페르소나 등)
  ├─ 시스템 프롬프트: 앱 내 상수
  └─ 사용자 커스텀 프롬프트: Firestore
```

### Firebase Functions 도입 여부 결정

**Functions 필요한 시점**:
- 클라이언트가 read 하기 무거운 집계 (예: 1년 누적 통계)
- 보안 민감 로직 (예: 결제 — 본 프로젝트엔 없음)
- 대용량 일괄 처리

**Week 1~5 권장**: **Functions 미사용** — 모든 비즈니스 로직 클라이언트
**Week 5~6 결정 시점**: 시계열 집계 부담 시 Functions 도입 검토 (Decision Log)

→ Brief / Architecture 에 **"Backend logic location: Client-side (Week 1~5) / Reassess Week 5"** 한 줄 명시 권장

### 명시적 데이터 흐름 다이어그램 (학생들이 그려야 할 것)

```
[사용자 발화]
    ↓
[AudioRecorder] → [Gemini Live API] → [응답 음성 + 텍스트]
                                            ↓
                              [Room: 회화 임시 저장]
                                            ↓
[회화 종료 트리거]
    ↓
[활성 Layer 분석 요청 → Gemini] (1~2 Layer만)
    ↓
[Firestore: 점수 + 회화 로그 저장]
    ↓
[다음 화면: 교정 카드 추출 → 사용자 선택 → 플래시카드 저장]
    ↓
[Firestore: 플래시카드 + Room 캐시]
    ↓
[SRS 시점 계산 (클라이언트)] → [복습 알림 + 워치 알림]
```

**1주차 액션**: 위 흐름을 [`SYS-APP-ARCHITECTURE.pdf`](../spec/SYS-APP-ARCHITECTURE.pdf) 다이어그램에 추가 또는 별도 다이어그램으로 작성.

---

## 4. 시계열 집계 단순화

**원칙**: Week 1엔 시계열 분석 X. Week 4+ 추이 분석 도전.

### Week별 진행

| Week | 데이터 처리 | 사용자 노출 |
|---|---|---|
| Week 1~2 | 회화 1회 → 1 Layer 점수 1개 저장 | 단일 점수만 (시계열 X) |
| Week 3 | 1주일 누적 → 단순 평균 (클라이언트 계산) | 1주 평균 vs 오늘 비교 |
| Week 4~5 | 30일 누적 → 추이 그래프 | 막대그래프 (월별 변화) |
| Week 6 | (Should) Functions로 집계 이전 | 부하 측정 후 결정 |

### 데이터 모델 (Firestore 컬렉션)

```
users/{userId}
  ├─ profile: { nickname, createdAt, ... }
  ├─ flashcards/{cardId}: { front, back, srsState, ... }
  ├─ conversations/{convId}: { topic, startedAt, transcript, ... }
  └─ language_scores/{scoreId}:
       { date: 2026-05-12, layer: "vocabulary",
         metric: "lexical_diversity", value: 75, grade: "good" }
```

이렇게 두면 *"날짜별 lexical_diversity 추이"* 쿼리는 `where date >= 30일전 order by date` 한 번이면 됨.

---

## 5. 동기화 정책 — 단일 디바이스 우선

**원칙**: Week 1~5는 폰 단독. 워치는 read-only.

### 디바이스별 권한

| 디바이스 | 데이터 read | 데이터 write |
|---|---|---|
| 폰 (메인) | ✅ 모든 데이터 | ✅ 모든 데이터 |
| 워치 (보조) | ✅ Firestore의 학습 카드 / 알림 | ❌ (Week 1~5) |

→ **워치 단독 회화 / 워치에서 카드 작성 = Won't** (1차 회의에서 *"선택 기능"* 으로 분류했지만 더 명시적으로 Won't 권장)

**Decision Log 1줄**:
> *"디바이스 동기화 정책: 단일 디바이스 우선 (폰 메인). 워치 read-only. 다중 폰 동기화는 Week 6 이후 검토."*

---

## 6. 1주차 액션 체크리스트

학생 PO (정원화) / 팀장 (박재민) 가 1주차 끝까지 결정해야 할 항목:

### AI 활용 측면
- [ ] (1주차 끝) **활성 Layer 1~2개 선택** + Decision Log 기록 (review v5 § 3-2 참고)
- [ ] (Week 2 데모 후) **선택 Layer 신뢰성 검증** (§ 2 절차) + Decision Log
- [ ] (Brief v1.1) AI 활용 한계 명시 ("LLM 응답 분산 흡수를 위해 점수 → 등급화" 등)

### 백엔드/DB 측면
- [ ] (1주차 끝) **데이터별 위치 결정** (§ 3 의사결정 트리) + Decision Log
- [ ] (Brief v1.1) 백엔드 로직 위치 1줄 명시 ("Week 1~5: 클라이언트, Week 5+ 재평가")
- [ ] ([SYS-APP-ARCHITECTURE](../spec/SYS-APP-ARCHITECTURE.pdf)) 데이터 흐름 다이어그램 추가
- [ ] (Brief v1.1) 시계열 집계 정책 1줄 명시 ("Week 3: 단순 평균 / Week 4+: 추이")
- [ ] (Brief v1.1) 동기화 정책 1줄 명시 ("폰 메인 / 워치 read-only")
- [ ] ([Firestore]) 컬렉션 구조 (§ 4 예시) 결정

### 검증 측면
- [ ] (Week 2) 1 Layer 5번 평가 분산 측정 (§ 2 5단계)
- [ ] (Week 3) 1주 누적 평균 / 오늘 점수 비교 화면 동작
- [ ] (Week 4) Firebase Firestore 부하 점검 — Functions 도입 여부 결정

---

## 7. 학생들에게 전달할 핵심 메시지 (1:1 시 강사가 읽을 수 있는 톤)

> *"1차 회의록의 깊이는 매우 좋습니다. TAM/SAM/SOM 분석, 학술 근거, 7-Layer 모델 — 창업 IR 수준입니다. 이건 자산입니다.*
>
> *다만 6주 + 5명 + NLP 전공자 0명 현실에서, 7-Layer 30+ 지표를 우리가 직접 'AI 학습'으로 만드는 건 불가능합니다. 우리가 할 수 있는 건 **Gemini API에 프롬프트로 요청**하는 것뿐입니다.*
>
> *그런데 Gemini는 같은 입력에 다른 출력을 줍니다 (LLM 본질). 응답 비용도 있습니다. 즉 7-Layer 모두를 신뢰성 있게 운영하려면 6주가 아니라 6개월이 필요합니다.*
>
> *그러니 1주차에 결정해야 할 것: **어떤 1~2 Layer만 운영할지, 결과를 점수가 아닌 등급으로 표시할지, 비용은 어디서 절감할지.***
>
> *그리고 백엔드 — 현재 시스템 다이어그램에 'Networking: -' 이라고 적혀 있습니다. Firebase 만 쓴다는 의미인데, 그렇다면 7-Layer 분석 / 시계열 집계 / 다중 디바이스 동기화는 어디서 일어나는지 명시되어야 합니다. 결정 안 하면 Week 4쯤 'Firestore에서 30일 평균 어떻게 가져오지?' 가 막힙니다.*
>
> *결론: 1주차에 위 두 가지 결정만 하면 됩니다. 자료 깊이는 그대로 자산이고, 못 담는 부분은 portfolio/ 폴더로 보관하면 됩니다."*

---

## 8. 참고 문서

- 검토 v5: [`brief_review_w1.md`](brief_review_w1.md)
- 가이드 § 4-4 (Brief 분량/MVP 범위): [`../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md`](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)
- 1차 회의록 (시장 분석 + 7-Layer): [`../mom/260506.pdf`](../mom/260506.pdf)
- 시스템 아키텍처: [`../spec/SYS-APP-ARCHITECTURE.pdf`](../spec/SYS-APP-ARCHITECTURE.pdf)
- Flow DB v1: [`../spec/System Flow DB v1 - Umma.pdf`](../spec/System%20Flow%20DB%20v1%20-%20Umma.pdf)
