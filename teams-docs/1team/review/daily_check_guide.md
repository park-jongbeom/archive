# 1팀 일일 진척 체크 가이드 (Scoffee)

> **사용 트리거**: 사용자가 "1팀 작업 현황 체크해줘" 라고 발화하면, Claude는 이 문서를 **반드시 먼저 읽고** 절차에 따라 점검을 수행합니다.
> **문서 위치**: [`teams-docs/1team/review/daily_check_guide.md`](.) (본 문서)
> **최초 생성**: 2026-05-13 (Week 2 진입)
> **갱신 주기**: 매주 일요일 또는 단계 전환 시
> **검토자**: 보조강사

---

## 0. 빠른 호출 절차 (Claude용)

사용자가 "1팀 작업 현황 체크해줘" 호출 시:

1. **이 문서를 먼저 읽는다** (절차/체크리스트 확인)
2. [`../snapshots/`](../snapshots/) 에서 **가장 최근 스냅샷**을 읽어 직전 상태 파악
3. [`./issue_pr_matrix.md`](./issue_pr_matrix.md) 에서 Issue↔PR 추적표 현황 확인
4. [`../figma/README.md`](../figma/README.md) 에서 FigJam 보드 구조 확인
5. § 2 "데이터 수집" 명령을 실행해 **현재 스냅샷 데이터** 확보 (git + GitHub + Figma)
6. § 4~7 체크리스트로 진단
7. § 8 "출력 템플릿" 으로 보고
8. 보고 후 **오늘자 스냅샷 파일을 `../snapshots/YYMMDD.md`로 저장**
9. § 9 트리거 해당 시 **즉시 강조 표시**

---

## 1. 저장소 기본 정보

| 항목 | 값 |
|---|---|
| Repo URL | https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee |
| 기본 브랜치 | `develop` (Git Flow) |
| 정식 릴리즈 브랜치 | `main` |
| 로컬 분석용 클론 경로 | `/tmp/snoffee-analysis/Snoffee` (없으면 새로 clone) |
| 프로젝트 기간 | 2026-05-06 ~ 2026-06-16 (6주) |
| 팀 인원 | 4명 |

### 1-1. 팀원 매핑 (커밋 이메일 → 실제 이름 → 역할)

| 이름 | 역할 | GitHub 계정(들) | 커밋 이메일 | 비고 |
|---|---|---|---|---|
| **송성호** | 팀장 / PO | `sdfg7979-glitch` (Issue 작성 확인) | (미확인) | **비전공자, 커밋보다 Issue/리뷰 중심 활동 가능성** |
| **손지희** | 부팀장 / 비공식 테크 리드 | `starlightfjh` | `son@…`, `starlightfjh@…` (이메일 2개) | 전공자, Firebase 전담 |
| **이제이** | 팀원 | `juu124` | `juu12424@gmail.com` | 전공자, UI/디자인 시스템 |
| **임정섭** | 팀원 | `ljs990326-cloud` | `ljs990326@gmail.com` (커밋명 `정섭`/`JungSub` 혼용) | 전공자, Hilt/잔류량 계산 |

> ⚠️ **PO 가시성 미해결**: 송성호 커밋이 shortlog에 안 잡힘. 이 표는 추정 단계이며, 1:1로 확인 후 확정 필요. 확정되면 이 표를 업데이트.

---

## 2. 데이터 수집 명령 (5분 절차)

매일 체크 시 다음 명령을 순서대로 실행해 raw 데이터를 확보:

```bash
# 0. 로컬 클론 준비 (최초 1회 또는 .git 없을 때만 clone)
[ -d /tmp/snoffee-analysis/Snoffee/.git ] || (mkdir -p /tmp/snoffee-analysis && cd /tmp/snoffee-analysis && git clone https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee.git)

# 1. 최신 동기화
cd /tmp/snoffee-analysis/Snoffee && git fetch --all --prune

# 2. 최근 24시간 커밋 (어제 09:00 기준 — 매일 오전 체크 시)
git log --all --since="24 hours ago" --pretty=format:"%ai | %an | %h | %s"

# 3. 브랜치 상태 (origin 기준)
git for-each-ref --sort=-committerdate refs/remotes/origin --format='%(committerdate:iso8601) | %(refname:short) | %(authorname)' | head -20

# 4. 기여자 누적 통계 (develop 기준)
git shortlog -sne origin/develop

# 5. 발견된 신규 TODO (어제 대비)
git log --all --since="24 hours ago" -p | grep -E "^\+.*TODO" | head -20
```

GitHub Issues/PR은 `gh` CLI 미설치 환경이므로 **WebFetch**로 수집:

- Issues: `https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee/issues?q=is%3Aissue` (open + closed 둘 다 별도 쿼리)
- PRs: `https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee/pulls?q=is%3Apr` (open + closed 둘 다)
- 특정 Issue: `https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee/issues/{N}`

### 2-1. Figma 보드 수집 (REST API + 토큰)

토큰은 [`../../.shared/.figma_token`](../../.shared/.figma_token)에 저장돼 있음 (1/2/3팀 공통, gitignored).
상세 절차는 [`../figma/README.md` § 4](../figma/README.md#4-매일-자동-수집-절차-보조강사용) 및 공유 가이드 [`../../.shared/README.md`](../../.shared/README.md) 참조.

```bash
cd /c/Users/ibebu/bootcamp6_final/archive
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
FILE_KEY="NKBVG1F8BcFXzjpnJxsC4u"
DATE=$(date +%y%m%d)

# 변경 감지용 메타
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/$FILE_KEY?depth=1" \
  -o teams-docs/1team/figma/raw/board_${DATE}.json

# Sprint 0/1 구조
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/$FILE_KEY/nodes?ids=197:246,439:631&depth=2" \
  -o teams-docs/1team/figma/raw/sprints_${DATE}.json
```

**디핑 포인트** (직전 스냅샷과 비교):
- `lastModified` 타임스탬프
- Sprint 0/1 자식 노드 수
- 신규/삭제된 섹션 ID

---

## 3. 일일 체크 핵심 5문항 (Quick Pass — 2분)

시간이 없을 때 이 5개만 답해도 80% 커버:

1. **어제 대비 develop에 머지된 PR 수 / 새 커밋 수는?**
2. **Issue 신규 / Issue 닫힘 / Issue 신규 댓글 수는?**
3. **24시간 무활동 PR이 있는가?** (리뷰 정체)
4. **PRD Must 7개 중 오늘 진척이 있던 항목은?** (§ 5 매트릭스 참조)
5. **송성호 PO 활동 흔적이 있는가?** (commit / issue 작성 / issue 댓글 / PR 리뷰 중 하나라도)

---

## 4. 카테고리별 체크리스트 (Deep Pass — 15분)

### A. 활동 (Activity)

- [ ] 24시간 내 신규 커밋 수 — 0이면 ⚠️ 사유 파악 필요
- [ ] 신규 브랜치 / 삭제된 브랜치 (정상적 정리 vs 무관심 방치)
- [ ] develop에 머지된 PR 수
- [ ] 새 PR (open 상태) — 누가, 어떤 기능
- [ ] develop이 main보다 얼마나 앞서있는가 (`git rev-list --count origin/main..origin/develop`)
- [ ] 머지 충돌 흔적 (`Merge branch 'develop' into ...` 패턴 빈도) — 잦으면 작업 분리 실패 신호

### B. Issue ↔ PR 추적 (가장 중요)

- [ ] Open Issue 수 변화
- [ ] **Closed Issue 수 변화** ← 1팀 핵심 약점 (현재 0건)
- [ ] PR description에 `Closes #N` / `Fixes #N` 키워드 포함 비율
- [ ] PR이 어떤 Issue와 연결되는지 [`./issue_pr_matrix.md`](./issue_pr_matrix.md) 갱신
- [ ] 7일 이상 무활동 Issue (stale) 식별
- [ ] PRD Must 항목 중 Issue로 등록 안 된 것 식별

### C. 협업 / PR 사이클

- [ ] PR open → merge 평균 소요 시간 (1일 이상 = 🟡, 3일 이상 = 🚨)
- [ ] PR당 리뷰 코멘트 수 — 0건 일관이면 🟡 (셀프 머지 패턴)
- [ ] **셀프 머지** 비율 — PR 작성자 = 머지자 (코드 리뷰 문화 약화 신호)
- [ ] Approve 없이 머지된 PR 식별
- [ ] 리뷰 리더십이 한 명에게 쏠리는지 (정섭이 PR #11~#18 머지 도맡고 있음 — 부담 분산 권장)

### D. PO(송성호) 가시성 (Week 1 검토 § 3-2 후속)

- [ ] 24시간 내 송성호 명의 커밋 — 있다면 어떤 영역
- [ ] 송성호가 작성한 Issue / Issue 댓글
- [ ] 송성호가 한 PR 리뷰 코멘트
- [ ] Co-Authored-By 기록에 송성호 포함된 커밋
- [ ] **주간 누적 기여 = 0** 이면 즉시 1:1 의제로 에스컬레이션
- [ ] 송성호가 회의록(MoM)에 보고한 "완료"가 실제 산출물(코드/이슈/문서)로 추적 가능한지

### E. 코드 품질 (Quality)

- [ ] 신규 TODO 누적 (`TODO` / `TODO("Not yet implemented")`) — 줄지 않고 늘기만 하면 🟡
- [ ] 신규 단위 테스트 추가 여부 (Week 3 진입 전 최소 5개 목표)
- [ ] 아키텍처 위반 — `domain/` 패키지가 `data/` 또는 `presentation/` import하는지 확인 (`grep -r "import com.snoffee.app.data" app/src/main/java/com/snoffee/app/domain` — 결과 있으면 🚨)
- [ ] 도메인 enum이 무력화되는 패턴 (예: sensitivity Int 분기 등)
- [ ] 파일 크기 폭증 (단일 파일 800줄 초과 시 🟡)
- [ ] 매직 넘버 / 매직 스트링 신규 발생

### H. 저장소 위생 / 보안 (Repo Hygiene & Secrets) — 전팀 공통

> 신규 항목 (2팀 Umma에서 발견된 패턴 기반). 1/2/3팀 모두 점검.

- [ ] **`google-services.json` 커밋 여부** — `git ls-files | grep google-services` (있으면 R13 🚨)
- [ ] **Firebase / GCP 키 파일 노출** — `*.json`(서비스 계정), `*-key.json`, `keystore`, `*.jks`, `*.p12` 등
- [ ] **`.env`, `local.properties` 등 환경 설정 파일 커밋 여부** (gitignore 점검)
- [ ] **API 키 평문 노출** — 코드에 `AIza...` (Google), `sk_live_...` (Stripe), `figd_...` (Figma) 등 패턴 grep
- [ ] **IDE 아티팩트 커밋 누적** — `.idea/`, `.vscode/`, `.DS_Store`, `Thumbs.db` (R14 🟡)
- [ ] **shelf/patch 파일 커밋** — `.idea/shelf/*.patch` (R14 🟡)
- [ ] **`.gitignore` 존재 및 적절성** — 없거나 부실하면 🟡

### F. 진척 정렬 (PRD Alignment) — § 5 매트릭스 참조

- [ ] Must 항목 중 정해진 주차에 시작 안 한 것
- [ ] Should/Won't 항목에 시간 쓰고 있는지 (스코프 드리프트)
- [ ] mock-first 합의 위반 (Week 3 전에 Gemini/Health Services 실연동 진입)

### G. FigJam 보드 동기성 (디자인 ↔ 코드 정합성)

- [ ] **Sprint 1에 신규 항목이 들어갔는가** (Week 3 계획 가시화 신호 — 현재 비어있음 🟡)
- [ ] 와이어프레임/디자인 출력 영역에 신규 화면 추가됐는가
- [ ] 떠다니는 피드백 메모 중 미반영 사항 (예: "주간말고 캘린더로 뽑아내기")
- [ ] FigJam은 갱신됐는데 코드는 변화 없는 영역이 있는가 (디자인 → 구현 지연)
- [ ] 코드는 진행됐는데 FigJam엔 없는 영역 (구현 → 디자인 동기화 누락)
- [ ] Must-4(Gemini) / Must-5(WearOS) 시각화 추가됐는가 (현재 미시각화)
- [ ] 보드 `lastModified`가 24시간 이내인가 (작업 활성도)

---

## 5. PRD Must 7개 진척 매트릭스 (매일 갱신)

| # | Must 기능 | 목표 주차 | 현재 상태 | 코드 위치 | mock-first | 점검일 |
|---|---|---|---|---|---|---|
| 1 | 온보딩 (수면/민감도/기상) | W2 | 🟢 5개 화면 골격 완성 | `presentation/onboarding/*` | — | 2026-05-13 |
| 2 | 카페인 입력 | W2 | 🟢 화면+ViewModel 완성 | `presentation/caffeine/CaffeineInputScreen` | — | 2026-05-13 |
| 3 | 잔류량 실시간 계산 | W2 | 🟢 완료 (반감기 공식) | `domain/util/CaffeineCalculator` | — | 2026-05-13 |
| 4 | Gemini 컷오프 산출 | W3~ | 🟡 스텁 | `domain/usecase/gemini/*` | ✅ | 2026-05-13 |
| 5 | WearOS 알림 3종 | W2(섭취직후)→W4 | 🟡 화면만 골격 | `wear/presentation/alert/*` | ✅ 단계 전개 | 2026-05-13 |
| 6 | Health Services 수면 | W3~ | 🟡 DataSource 스텁 | `data/datasource/health/*` | ✅ | 2026-05-13 |
| 7 | 주간 카페인-수면 리포트 | W4~ | 🟡 빈 UseCase | `presentation/report/*` | — | 2026-05-13 |

> 매일 체크 시 "현재 상태"와 "점검일"을 업데이트. 🔴(미시작) → 🟡(진행 중) → 🟢(완성) 3단계.

---

## 6. 1팀 고유 위험 신호 (Anti-pattern Triggers)

다음 중 **하나라도 해당하면 즉시 보고서에 강조**:

| # | 위험 신호 | 정량 기준 | 조치 |
|---|---|---|---|
| R1 | PO(송성호) 1주 무활동 | 7일 누적 commit+issue+comment = 0 | 1:1 의제 1순위로 격상 |
| R2 | Issue 미닫힘 누적 | open issue ≥ 10 + closed = 0 지속 | PR↔Issue 연결 워크플로우 교육 |
| R3 | 셀프 머지 우세 | 최근 5 PR 중 4개 이상 셀프 머지 | 페어 리뷰 의무화 권고 |
| R4 | mock-first 위반 | W3 전 Gemini API 키 노출 또는 실호출 | 즉시 commit revert 또는 mock 복원 |
| R5 | 테스트 정체 | Week 3 진입 시 테스트 ≤ 2개 | TDD 1회 시연 + Mapper 테스트 권장 |
| R6 | 머지 충돌 폭증 | 1일 내 `Merge branch 'develop' into ...` 3회 이상 | 작업 분리 / 동기화 빈도 협의 |
| R7 | Architecture 위반 | domain → data import | 즉시 리팩토링 요청 |
| R8 | 단일 인물 과부하 | 1인이 develop 커밋의 60% 이상 차지 (주 단위) | 작업 재분배 권고 |
| R9 | Won't 기능 작업 | "회원가입", "소셜", "친구 공유" 관련 키워드 commit/PR | Brief § 7 Won't 환기 |
| R10 | Demo Scenario 부재 | Week 2 종료 시 Demo Scenario v0 미작성 | 다음 회의 의제 강제 |
| R11 | Sprint 1 미계획 | Week 3 진입(05-13) 이후 Sprint 1 섹션 비어있음 지속 | 1:1로 Sprint 1 백로그 등록 요청 |
| R12 | 디자인 ↔ 코드 비동기 | FigJam엔 있는 화면이 코드엔 없거나 그 반대 7일 지속 | 동기화 의제 격상 |
| **R13** | **시크릿/설정 파일 커밋** 🚨 | `google-services.json`, `*.keystore`, `.env`, 서비스 계정 JSON, API 키 평문 등이 git에 추적됨 | 즉시 보고 + 재발급(rotation) 권고 + `.gitignore` 보강 + git history 정화 권유 |
| **R14** | **IDE 아티팩트 누적** | `.idea/`, `.vscode/`, `.idea/shelf/*.patch` 등 IDE/OS 파일 커밋. 3개 이상 발견 시 🟡 | `.gitignore`에 표준 패턴 추가 권고 |

---

## 7. 단계별(Week N) 포커스 전환

매주 강조 포인트가 다릅니다. 체크 시 해당 주차 항목에 가중치 두기:

### Week 2 (현재 — 2026-05-06 ~ 05-12 마감)
- 🎯 **FLOW-A 데모 가능성** (카페인 입력 → 잔류량 → Home 표시)
- 🎯 Issue↔PR 연결 워크플로우 정착
- 🎯 Decision Log 6건 기록 완료 (Week 1 검토 § 4 요구)
- 🎯 송성호 PO 가시성 확보

### Week 3 (2026-05-13 ~ 05-19) — 시작
- 🎯 mock → 실연동 전환 시작 (Gemini, Health Services)
- 🎯 WearOS 섭취 직후 알림 실제 발신 동작
- 🎯 단위 테스트 5개 이상 확보
- 🎯 Demo Scenario v1 작성

### Week 4 (05-20 ~ 05-26)
- 🎯 컷오프 알림 + 취침 전 알림 추가
- 🎯 Health Services 권한 다이얼로그 완성
- 🎯 FLOW-B 시작 (수면 → 리포트)
- 🎯 통합 테스트 시작

### Week 5 (05-27 ~ 06-02)
- 🎯 주간 리포트 화면 + Gemini 인사이트
- 🎯 에러/로딩/빈 화면 일관성 점검
- 🎯 디바이스 페어링 디버깅 마무리

### Week 6 (06-03 ~ 06-16)
- 🎯 데모 시나리오 2~5분 무중단 리허설
- 🎯 main 브랜치로 release 머지
- 🎯 README/스크린샷/시연 영상 정비
- 🎯 회고 + 기여도 정산

---

## 8. 출력 템플릿 (보고 형식)

매일 체크 결과는 다음 형식으로 보고:

```markdown
# 1팀 일일 체크 — YYYY-MM-DD (Week N, D일차)

## 🟢 한 줄 요약
{전체 신호등 + 핵심 변화 1문장}

## 📊 24시간 변화
- 신규 커밋: N건 (어제 M건)
- 머지된 PR: N건 (#X, #Y)
- 신규/닫힌 Issue: +N / -N (open 총 N개)
- 활성 기여자: {이름들}

## ✅ 진척 (PRD Must 매트릭스)
- 진척이 있던 항목: Must-N, Must-M
- 다음 마일스톤까지 차이: {잔여}

## 🟡 주의 항목
{R1~R10 중 발견된 것 또는 일반 주의 사항}

## 🚨 즉시 조치 필요
{있을 때만 표시. 없으면 "없음"}

## 👤 PO 가시성 (송성호)
- 24h 활동: {커밋 N / 이슈 N / 댓글 N / 리뷰 N}
- 누적(주 단위): {합계}

## 📋 Issue↔PR 정합성
- 신규 PR 중 `Closes #N` 명시: N/M
- 신규 닫힌 Issue: {번호들}
- 7일+ stale Issue: {번호들}

## 🎨 FigJam 보드 변화
- lastModified: {타임스탬프, 24h 이내 여부}
- Sprint 0 신규 섹션: {있으면 이름들}
- Sprint 1 상태: {빈/N개 항목}
- 신규 피드백 메모: {있으면 요약}
- 디자인↔코드 정합성 이슈: {있으면 항목}

## 💡 보조강사 권장 액션 (오늘)
1. {액션}
2. {액션}

## 📁 스냅샷 저장
`teams-docs/1team/snapshots/YYMMDD.md` 갱신 완료
```

---

## 9. 즉시 에스컬레이션 트리거

다음 중 하나라도 발견되면 **보고서 상단에 🚨 박스로 강조**, 사용자에게 별도 알림:

- [ ] 송성호 PO 7일 누적 무활동 (R1)
- [ ] **시크릿 노출** (R13): `google-services.json` / `.env` / `*.keystore` / 서비스 계정 JSON / API 키 평문 커밋 발견
- [ ] main 브랜치로 직접 push (Git Flow 위반)
- [ ] force push 흔적 (`git reflog show origin/develop` 비정상)
- [ ] 라이선스 위반 라이브러리 추가
- [ ] 데모 시연 1주 전인데 FLOW-A 미동작
- [ ] 팀원 1인 14일 무활동 (이탈 신호)

---

## 10. 스냅샷 저장 & 디핑 전략

매일 체크 후 **반드시** [`../snapshots/YYMMDD.md`](../snapshots/) 파일 생성/갱신:

- 파일명: `260513.md` (YY=26, MM=05, DD=13)
- 내용: § 8 출력 템플릿 그대로
- 다음날 체크 시 직전 스냅샷을 읽어 "어제 대비" 변화 산출
- 주말은 건너뛰어도 무방 (월요일에 "지난 금요일 대비"로 보고)

### 스냅샷 활용 예시

```
오늘(05-15) 체크 → 어제(05-14) 스냅샷 비교 →
"PR #19 신규 머지 / Issue #7 여전히 open 5일째 / 송성호 활동 0건 3일 연속" 식 변화 추출
```

---

## 11. 다른 팀과의 차이 (주의)

이 문서는 **1팀 전용**입니다. 다른 팀은 다른 방법론/제약을 따릅니다:

- **3팀**: 워터폴 방식 (애자일 권고 ❌, 명세 완성도 강조) — 별도 가이드 필요
- **2팀, 4팀**: 별도 product_brief 확인 후 가이드 작성 권장

각 팀의 신호 기준이 다르므로, 이 가이드를 그대로 복사해 다른 팀에 적용하지 말 것.

---

## 12. 가이드 자체 갱신 트리거

이 문서는 살아있는 문서입니다. 다음 시점에 갱신:

- 매주 일요일: § 5 매트릭스 + § 7 단계 가중치 재조정
- 팀 구성 변경 시: § 1-1 업데이트
- 신규 위험 신호 발견 시: § 6 R-항목 추가
- Week 1 검토(brief_review_w1.md)에 대응되는 새 검토가 나오면 cross-link 추가

---

## 13. 참조 문서

- [Product Brief Scoffee ver 1.0](../product_brief/Product%20Brief_Scoffee_ver%201.0)
- [Brief Review Week 1](./brief_review_w1.md)
- [Members](../members)
- [MoM (회의록)](../mom/)
- [Issue↔PR Matrix](./issue_pr_matrix.md)
- [Snapshots](../snapshots/)
