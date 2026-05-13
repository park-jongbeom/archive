# 2팀 컨텍스트 (Umma)

> **사용 방법**: 매일 점검 시 [meeting_prep_template.md](../../.shared/meeting_prep_template.md) 작성 전, 본 문서를 환기
> **갱신 시점**: 단계 전환, 팀 구성 변화 시

---

## 1. 프로젝트 정체

| 항목 | 값 |
|---|---|
| 앱명 | **Umma (움마)** |
| 팀 닉네임 | **라스트댄스** ([members](../members) 기준) — *Figma 파일명 "라스트딴따라"와 차이 있음, 통일 필요* |
| 도메인 | **AI 기반 초개인화 영어 학습 (대화 → 교정 → 저장 → 반복학습 → 성장 추적)** |
| 기간 | 2026-05-06 ~ 2026-06-16 (6주) |
| 방법론 | **애자일** |
| 핵심 기술 | Firebase AI Logic + **Gemini Live** + Push-to-Talk |
| 슬로건 | "아기는 문법을 배워서 말하지 않습니다. '말하고 싶어서' 배웁니다." |

---

## 2. 팀 구성 (5명 — 1팀보다 1명 많음)

| 역할 | 이름 | 배경 | GitHub | 강도 분류 (강사 운영 노트) |
|---|---|---|---|---|
| **팀장** | 박재민 | 전공자, 관련학과 | `woals6318-hash`, `jammd1` (woals/박재민1/박재민2) | 🟢 상위 — AI/알고리즘/음성 처리 가능 |
| **부팀장** | 정원화 | 비전공자 (학습 성취 매우 높음) | `sangsangcat` (Wanna/wanna) | 🟢 상위 — 적응 단계 멤버 비공식 멘토 가능 |
| 팀원 | 김명준 | 비전공자 | `jssmt247-crypto` | 🟡 **적응 단계 — 페어 프로그래밍 권장** |
| 팀원 | 김태환 | 비전공자, 전직 수학강사 (학습 성취 높음) | `taehwan-dev` | 🟢 상위 — 수학 강점, SRS 알고리즘 적합 |
| 팀원 | 정재훈 | 비전공자 (학습 성취 낮음, 개선 추세) | `jjh83301476` | 🟡 **적응 단계 — 작은 PR 코칭 권장** |

상세 강사 운영 노트: [members](../members)

### 2-1. 현재 커밋 분포 (2026-05-13 기준)

| 멤버 | 커밋 수 | 비율 | 평가 |
|---|---|---|---|
| 박재민 (팀장) | **91** (woals 75 + 박재민1/2 16) | ~91% | 🚨 R8 후보 |
| 정원화 (부팀장) | 8 (Wanna 5 + wanna 3) | ~8% | 🟡 |
| 김태환 | 1 | ~1% | 🚨 R-quiet |
| 김명준 | **0** | 0% | 🚨 **R-out 후보** |
| 정재훈 | **0** | 0% | 🚨 **R-out 후보** |

→ **김명준 / 정재훈 Week 2 종료 시점에 커밋 0건** — 이미 [members](../members) 강사 운영 노트에 명시된 위험 신호 (페어 매칭 재조정 필요).

---

## 3. 외부 자료 위치

| 자료 | 위치 |
|---|---|
| GitHub | https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma |
| 로컬 클론 | `/tmp/umma-analysis/Umma` (없으면 새로 clone) |
| Figma 보드 | https://www.figma.com/design/BTzbV8SDChx6ywQwycniQi (Design, fileKey: `BTzbV8SDChx6ywQwycniQi`) |
| 2팀 figma 자료 | [../figma/](../figma/) |
| 2팀 회의록 | [../mom/](../mom/) |
| 2팀 spec | [../spec/](../spec/) |
| 2팀 docs | [../docs/](../docs/) |

---

## 4. 앱 핵심 기능 (Umma)

[GitHub README](https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma) 기준:

| 기능 | 설명 |
|---|---|
| AI 자유 회화 | 관심사 기반, i+1 난이도, **Push-to-Talk** 음성 |
| 문장 교정 | 대화 후 핵심 발화 추출 → 선택적 저장 |
| 플래시카드 | 저장된 교정 문장 반복 학습 (SRS 알고리즘) |
| Dashboard | 최근 대화 + 교정 대기 + 복습 카드 + 성장 지표 |
| Statistics | 언어별 학습 통계 |

→ 메뉴 구성: **AI Chat · 교정 · Flashcard · Dashboard · Statistics**

### 4-1. Must 항목별 추천 담당 ([members](../members) 강사 운영 노트)

| Must | 1차 담당 | 페어/보조 | 비고 |
|---|---|---|---|
| AI 자유 회화 | 박재민 | 정원화 자문 | |
| 사용자 문장 교정 | 박재민 | 정원화 페어 | |
| 교정 문장 플래시카드 (CRUD) | **김명준** | **정원화 페어** (멘토) | 표준 패턴, 적응 단계 멤버에 적합 |
| SRS 반복학습 알고리즘 | 김태환 | 박재민 자문 | 수학 강점 활용 |
| 기본 통계 | **정재훈** | **김태환 페어** | |

---

## 5. 1팀과의 비교 (점검 시 차이 인식)

| 항목 | 1팀 (Scoffee) | 2팀 (Umma) |
|---|---|---|
| 팀 인원 | 4명 | **5명** |
| 도메인 | 카페인-수면 | 영어 학습 |
| 핵심 기술 | Gemini API (텍스트) + WearOS | **Gemini Live (음성)** |
| 방법론 | 애자일 | 애자일 |
| 커밋 수 (Week 2 종료) | 59 | ~100 |
| PR 수 | 13 | 48 |
| 활발도 | 보통 | 매우 활발 |
| 적응 단계 멤버 | 없음 | **2명 (김명준 / 정재훈)** |
| 보안 우려 | 미확인 | 🚨 **google-services.json 커밋됨** (R13) |

---

## 6. 2팀 특이 사항 (보조강사 환기)

1. **5인 팀 → 인물별 활동 모니터링 부담 1.25배** (1팀 4명 대비)
2. **김명준/정재훈 적응 단계** — 강사도 인지하고 있으나 보조강사가 매일 활동 점검
3. **박재민 단독 91% 기여 (R8)** — Week 3 이후 부담 분산 필요
4. **`google-services.json` 커밋됨 (R13)** — 즉시 환기 필요
5. **`.idea/shelf/*.patch` 12+ 커밋됨 (R14)** — IDE 위생 정리
6. **Gemini Live (음성) 사용** — 1팀의 Gemini (텍스트)보다 비용·응답 변동 큼

---

## 7. 일정 (계획 기준, 1팀과 동일 구조)

| Week | 기간 | 목표 (추정) |
|---|---|---|
| Week 1 | 05-06 ~ 05-12 | Brief 합의, 디자인 시스템 |
| **Week 2 (현재)** | 05-13 ~ 05-19 | 기본 AI 회화 동작, 핵심 화면 골격 |
| Week 3 | 05-20 ~ 05-26 | 교정 시스템 + 플래시카드 |
| Week 4 | 05-27 ~ 06-02 | SRS 반복학습 + 통계 |
| Week 5 | 06-03 ~ 06-09 | 통합 + Watch 연동 (있다면) |
| Week 6 | 06-10 ~ 06-16 | 데모 + 회고 |

> ⚠️ 본 일정은 [members](../members) 운영 노트 기반 추정. 2팀 product_brief / spec 입수 후 확정.

---

## 8. 회의 톤 가이드 (2팀 전용)

- **5인 팀 균형 의식** — 박재민·정원화·김태환에게 발언 쏠리지 않게
- **김명준 / 정재훈** 에게는 의도적 발언 기회 ("이번 주 작업 보고") 부여
- 박재민에게는 **WIP 제한** 환기 (Issue 4개 이상 동시 assign 시 R-WIP)
- 정원화는 **부팀장 + 멘토 + 개발자 3역 부담** — Week 3 시점부터 번아웃 신호 점검
- 적응 단계 멤버 PR 머지 시 **공개 격려** (다음 PR로 이어지는 동기 부여)

---

## 9. 참조

- [.shared/daily_check_method.md](../../.shared/daily_check_method.md)
- [.shared/risk_taxonomy.md](../../.shared/risk_taxonomy.md)
- [.shared/meeting_prep_template.md](../../.shared/meeting_prep_template.md)
- [./team_specific_checks.md](./team_specific_checks.md) — 2팀 고유 점검 항목
- [../members](../members) — 강사 운영 노트 (1:1 코칭 우선순위 포함)
- [../figma/README.md](../figma/README.md) — Figma 가이드
- **GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma
