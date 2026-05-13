# 2팀 고유 점검 항목 (Umma)

> **사용 방법**: 공통 [risk_taxonomy.md](../../.shared/risk_taxonomy.md) R1~R14 + 본 문서의 2팀 특화 항목을 동시에 점검
> **갱신 시점**: 매주 일요일, Pre-mortem 결과 반영
> **현재 가중치**: Week 2 (2026-05-13 ~ 05-19) 종료 시점

---

## 1. 2팀 절대 빠뜨리지 말 것 (Top 5)

매 미팅 전 무조건 점검:

| # | 점검 항목 | 명령 / 방법 | 정상 상태 |
|---|---|---|---|
| 1 | **김명준 / 정재훈 활동** | `git log --since="24 hours ago" --author="jssmt247\\|jjh83301476"` + GitHub Issue 작성 grep | commit 또는 Issue 또는 PR ≥ 1 |
| 2 | **박재민 WIP 제한** | GitHub 박재민에게 assign된 Open Issue 수 | < 4 |
| 3 | **`google-services.json` 노출 (R13)** | `git ls-files \| grep google-services` | (해소될 때까지 반복 환기) |
| 4 | **정원화 부담 점검** | 부팀장 + 멘토 + 개발자 활동 합산 (Issue/PR/리뷰/커밋) | 주간 활동 ≤ 박재민의 1.3배 |
| 5 | **Gemini Live 비용/레이턴시** | Firebase 콘솔 또는 사용량 로그 | 예산 내 |

---

## 2. PRD Must 매트릭스 (정보 입수 후 갱신)

⚠️ 2팀 공식 PRD 미입수. [GitHub README](https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma) 기반 추정:

| Must # | 기능 | 추천 담당 | 현재 상태 |
|---|---|---|---|
| 1 | AI 자유 회화 | 박재민 | (정보 미수집) |
| 2 | 사용자 문장 교정 | 박재민 + 정원화 | (정보 미수집) |
| 3 | 교정 문장 플래시카드 | **김명준** + 정원화 페어 | 🚨 **김명준 커밋 0** |
| 4 | SRS 반복학습 알고리즘 | 김태환 | (정보 미수집) |
| 5 | 기본 통계 | **정재훈** + 김태환 페어 | 🚨 **정재훈 커밋 0** |

→ Week 3 진입 전 **PRD/spec 입수** 후 본 표 정식 채움.

---

## 3. 2팀 코드 미시 품질 점검

### 3-1. 보안 / 위생 (즉시 환기 항목)

```bash
cd /tmp/umma-analysis/Umma

# R13: google-services.json
git ls-files | grep "google-services\\|firebase-adminsdk\\|service-account"

# R14: IDE 아티팩트 (특히 .idea/shelf/*.patch)
git ls-files | grep -E "^\\.idea/|^\\.vscode/|\\.DS_Store" | wc -l
```

→ 현재 발견된 항목 (2026-05-13 기준):
- 🚨 `app/google-services.json` 커밋됨
- 🟡 `.idea/shelf/Uncommitted_changes_*.patch` 12개+ 커밋됨

### 3-2. 적응 단계 멤버 PR 사이즈

[members](../members) 운영 노트: "작은 PR 자주 머지" 코칭 권장. 김명준/정재훈이 머지하면:
- PR 변경 라인 수 ≤ 200 → 🟢 적정
- 200 ~ 500 → 🟡 페어 리뷰 권장
- > 500 → 🚨 분할 권유

### 3-3. 박재민 WIP 위반 점검

GitHub Issues에서 박재민에게 동시 assign된 open issue 수:
- ≤ 3 → 🟢
- 4 이상 → 🚨 R-WIP (members 운영 노트 명시)

---

## 4. 인물별 매일 활동표 (양식)

매일 양식의 인물별 활동표는 5인 모두 표기:

| 멤버 | 24h commit | 24h Issue/PR | 24h 댓글 | 신호 |
|---|---|---|---|---|
| 박재민 | N | N | N | 🟢/🟡/🚨 |
| 정원화 | N | N | N | 🟢/🟡/🚨 |
| 김명준 | N | N | N | 🟢/🟡 **R-out** |
| 김태환 | N | N | N | 🟢/🟡 |
| 정재훈 | N | N | N | 🟢/🟡 **R-out** |

→ 김명준 / 정재훈 행에 **연속 무활동 일수** 카운터 유지.

---

## 5. Figma 보드 2팀 특화 점검

| 항목 | 방법 |
|---|---|
| 최신 버전 (v1.4) 갱신 여부 | Page 1 Draft 섹션에 v1.5 등장 시 갱신 신호 |
| Draft 페이지 비대 | 244 children → 300 초과 시 정리 권유 (다른 페이지로 이전) |
| "와이어프레임" 페이지 산출물 | 정리된 페이지로의 이전 흐름 점검 |
| 일별 작업 섹션 누적 | 05-13, 05-14 등 매일 생성되는지 |

[2팀 figma/README § 6](../figma/README.md#6-일일-체크-통합-항목) 도 동시 참조.

---

## 6. 2팀 특화 R-항목

본 팀에만 적용되는 위험 신호:

| ID | 위험 | 정량 기준 | 조치 |
|---|---|---|---|
| **R-out-2** | 김명준 / 정재훈 활동 0 | 일주일 누적 commit + Issue + PR + 댓글 = 0 | 즉시 1:1 + 페어 매칭 재조정 |
| **R-WIP** | 박재민 WIP 위반 | 박재민 assign open issue ≥ 4 | 다른 멤버 분산 권장 |
| **R-burn** | 정원화 번아웃 신호 | 부팀장 + 멘토 + 개발자 합산 활동이 박재민의 1.5배 초과 | 부담 분산, 멘토 역할 김태환과 분담 |
| **R13-2** | google-services.json 노출 (지속) | `git ls-files`에 해당 파일 존재 | 매일 환기, 1주 안 해소 안 되면 강사 보고 |

---

## 7. Pre-mortem 결과 반영 (주1회 갱신)

[premortem_template.md](../../.shared/premortem_template.md) 결과를 본 섹션에 누적.

### 2026-05-13 베이스라인 (참고)

(첫 pre-mortem은 다음 금요일 — 본 섹션은 그때 채움)

---

## 8. Week N 가중치 변동

### Week 2 (현재)
- 🎯 google-services.json 노출 환기 (R13-2)
- 🎯 김명준 / 정재훈 첫 PR 머지 시도
- 🎯 박재민 WIP 분산 권장

### Week 3
- 🎯 적응 단계 멤버 (김명준/정재훈) PR 1개 이상 머지
- 🎯 정원화 부담 점검 시작 (R-burn)
- 🎯 김태환 SRS 알고리즘 본격 진입

### Week 4
- 🎯 적응 단계 2명 페어 프로그래밍 1회 ≥
- 🎯 통합 테스트 시작

### Week 5
- 🎯 데모 시나리오 v1
- 🎯 5인 발표 분담 합의

### Week 6
- 🎯 5인 모두 데모 일부 시연 (적응 단계 멤버 포함)

---

## 9. 정보 미수집 항목 (입수 후 본 문서 갱신)

다음 자료를 입수하면 정밀도가 크게 올라감:

- [ ] 2팀 product brief (Brief / PRD)
- [ ] 2팀 spec ([../spec/](../spec/) 폴더에 있는지 확인)
- [ ] 2팀 docs ([../docs/](../docs/))
- [ ] Week 1 검토 (1팀 [brief_review_w1.md](../../1team/review/brief_review_w1.md) 와 같은 산출물 2팀 버전)
- [ ] 2팀 일정표 (Week별 마일스톤)

---

## 10. 참조

- [.shared/daily_check_method.md](../../.shared/daily_check_method.md)
- [.shared/risk_taxonomy.md](../../.shared/risk_taxonomy.md)
- [.shared/meeting_prep_template.md](../../.shared/meeting_prep_template.md)
- [./context.md](./context.md) — 2팀 컨텍스트
- [../members](../members) — 강사 운영 노트
- [../figma/README.md](../figma/README.md)
- **GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma
