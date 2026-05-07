# 노션 브라우저 자동화 — 작업 계획 (PLAN)

> **한 줄 정의**: 회사 노션 차단 정책 하에서 강사가 매주 수동으로 진행하던 PDF 추출을, **Claude 호출 시점에만** 강사 본인 로그인 세션을 재사용하여 텍스트 자동 추출하도록 전환하기 위한 작업 계획.

---

## 0. 이 문서 — 의미 / 배경 / 방향성 / 목적 / 경계

### 0-1. 의미

이 문서는 **계획 문서(PLAN)** 입니다. 구현 전 **의사결정과 작업 범위를 합의**하기 위함이며, 컨펌 후에야 7개 파일 작업이 시작됩니다.

### 0-2. 배경

- 회사 노션이 Notion API + Export + (확률 높음) Share to web 모두 차단
- 현재 운영안: 강사가 매주 일요일 25분 동안 페이지를 PDF로 인쇄 → `snapshots/week-N/팀명_pdf/` 에 저장
- 강사 의사: PDF 인쇄 자체가 부담. **수동 작업과 동일한 자격/데이터/저장 위치**를 가지면서 시간만 줄이고 싶음
- 이전 결정: 24/7 크롤러는 회사 정책 위반 위험으로 거부됨
- 이번 결정: **on-demand (강사가 Claude 호출 시점에만)** 자동화 — 위험 프로파일이 다름

### 0-3. 방향성

- ❌ 백그라운드 데몬 / cron / GitHub Actions
- ❌ 외부 서비스 전송 (Claude API로 노션 본문 통째 전송 등)
- ❌ 미등록 URL 자동 발견 (teams.yaml 명시 URL 외 방문 금지)
- ❌ 미인증 우회 (Share to web, token_v2, 익명 접근)
- ✅ **강사 본인 로그인 세션 재사용** (수동 작업의 가속)
- ✅ **명시적 트리거** (사용자가 Claude에게 *"Week N 노션 수집"* 호출 시에만)
- ✅ **로컬 한정** (강사 PC + git 레포)

### 0-4. 목적

| 단기 | 중기 | 장기 |
|---|---|---|
| PDF 인쇄 25분 → 1~2분 자동 추출 | 6주 운영 동안 안정적 매주 1회 호출 | 6주 종료 시 세션/자동화 흔적 완전 폐기 |

### 0-5. 경계 (이 문서가 다루지 않는 것)

- 운영진 추가 승인 절차의 정치적 측면 — *"이미 승인 받았는데 자동화 방식 변경 보고 어떻게?"*
- Notion 외 다른 도구(Slack, Figma) 자동 수집
- 학생 GitHub 데이터 — 이미 [`fetch_github.py`](../scripts/fetch_github.py) 로 처리 중, 본 계획 범위 외
- 분석 로직 자체 — `analyze.py` 의 신호등 로직은 입력 소스만 추가, 임계값 변경 없음

### 0-6. 다른 문서와의 관계

```
[01_애자일_팀프로젝트_가이드.md]    ← 부트캠프 운영 규칙 (SSOT)
[operation/CLAUDE.md]                ← Claude 호출 패턴 (수정 대상)
[operation/docs/notion_pdf_workflow.md] ← 현재 PDF 절차 (병행 또는 단계적 폐기 대상)
[plan_notion_browser_automation.md]  ← 본 문서 (계획)
   └── 컨펌 시 → [docs/notion_browser_workflow.md] 로 운영 절차 문서화 + 7개 파일 작업
```

---

## 1. 위험 평가 — 수동 vs On-demand 자동의 실제 차이

| 항목 | 수동 (현재 PDF) | On-demand 자동 (제안) | 24/7 크롤러 (거부됨) |
|---|---|---|---|
| 행위자 | 강사 | 강사 (Claude 트리거) | 자동 데몬 |
| 인증 | 강사 로그인 | 강사 저장 세션 | 강사 저장 세션 |
| 빈도 | 주 1회 | **주 1회 (강사 호출 시)** | 24시간 연속 |
| 데이터 범위 | 강사가 클릭한 페이지 | **teams.yaml 등록 URL만** | URL 무제한 발견 가능 |
| 사람 패턴 구분 | - | 거의 동일 | 명백히 구분됨 |
| 회사 IT 탐지 | 0 | 매우 낮음 | 높음 |
| ToS 명시 위반 | X | 회색 (자동 = 자기 자신 가속) | 회색~위반 |

**판정**: On-demand 자동은 수동의 효율적 자동화 범위 안. 단, 운영 원칙 6가지(§ 3) 준수 필수.

---

## 2. 목표 / 비목표

### 목표

1. 강사가 *"Week N 노션 수집"* 한마디로 모든 팀의 5개 페이지 텍스트 추출 완료
2. 페이지당 5~10초, 5팀 × 5페이지 = **약 2~4분** 내 완료
3. 추출 결과를 `analyze.py` 가 직접 분석하여 신호등 자동 부여
4. SSO/MFA 통과 후 1주~30일 동안 재로그인 없이 운영 가능

### 비목표

1. 노션의 **Block 단위 정밀 추출** — 평문 텍스트만 추출, 표/임베드/이미지 무시
2. **실시간 변경 감지 / Webhook** — 강사 호출 시점 스냅샷만
3. **노션 페이지 작성/수정** — read-only
4. **페이지 캐시/CDN** — 매번 새로 fetch
5. 학생 GitHub 데이터 변경 — 이미 GitHub Status Issue 방식 운영 중

---

## 3. 운영 원칙 6가지 (위험 최소화)

본 자동화를 받아들이는 대신 운영 내내 준수합니다. 위반 시 자동 철수.

1. **빈도 제한**: 주 1회 호출. 디버깅용으로도 일 3회 이내
2. **범위 명시**: `teams.yaml` 의 `notion_urls` 등록 URL 외 절대 자동 방문 금지
3. **속도 제한**: 페이지 간 2~3초 대기 (사람 패턴 유지)
4. **로컬 한정**: 추출 데이터를 외부 서비스/API로 전송 금지. Claude에게는 **분석 결과 요약**만 전달
5. **세션 보안**: `storage_state.json` 은 .gitignore + 외부 노출 절대 금지
6. **6주 후 폐기**: `storage_state.json` 삭제 + 노션 비밀번호 변경 + Playwright 설치 폴더 정리

---

## 4. 기술 스택 / 의존성

| 항목 | 선택 | 비고 |
|---|---|---|
| 언어 | Python 3.11+ | 기존 도구와 동일 |
| 브라우저 자동화 | **Playwright** (Chromium) | Selenium보다 안정적, 비공식 봇 탐지 우회 잘 됨 |
| 모드 | **Headed (사용자 가시) 또는 Headless 둘 다 지원** | SSO MFA는 headed 필수 |
| 세션 저장 | `storage_state.json` (cookies + localStorage) | 1회 로그인 후 재사용 |
| 텍스트 추출 | `page.inner_text()` | DOM 렌더 후 평문 |
| 추가 디스크 | ~250MB (Chromium 번들) | `playwright install chromium` |

설치:
```
pip install playwright
playwright install chromium
```

---

## 5. 파일 변경 계획 (7개)

| # | 파일 | 신규/수정 | 추정 줄수 | 핵심 |
|---|---|---|---|---|
| 1 | `scripts/setup_notion_login.py` | **신규** | ~60 | 1회 로그인 + 세션 저장 |
| 2 | `scripts/fetch_notion_browser.py` | **신규** | ~140 | 세션 재사용 + 페이지 텍스트 추출 |
| 3 | `teams.yaml` | 수정 | +5/팀 | `notion_urls` 5종 필드 추가 |
| 4 | `scripts/analyze.py` | 수정 | +30 | `_notion.json` 텍스트 분석 추가 |
| 5 | `.gitignore` | 수정 | +3 | `storage_state.json`, playwright cache |
| 6 | `CLAUDE.md` | 수정 | +패턴 | *"Week N 노션 수집"* 패턴 추가 |
| 7 | `docs/notion_browser_workflow.md` | **신규** | ~120 | 강사 셋업/매주 절차 (이 PLAN 컨펌 후 작성) |

### 5-1. `setup_notion_login.py` 명세

**입력**: 없음
**출력**: `operation/storage_state.json` (gitignored)

**동작**:
1. Playwright Chromium **headed** 모드로 실행
2. https://notion.so 로 이동
3. 강사가 직접 로그인 (SSO/MFA 통과)
4. 회사 워크스페이스 진입 확인되면 콘솔에 *"Press Enter to save session"* 출력
5. Enter 입력 시 `context.storage_state(path="storage_state.json")` 호출
6. 브라우저 종료

**호출 빈도**: 1주차 1회 + 세션 만료 시마다 (보통 7~30일에 1번)

### 5-2. `fetch_notion_browser.py` 명세

**입력**: `--week N`, `--team K팀` (선택)
**출력**: `snapshots/week-N/팀명_notion.json`

**동작**:
1. `storage_state.json` 로드 → Playwright context 생성 (headless OK, headed도 가능)
2. `teams.yaml` 의 각 팀 `notion_urls` 5개 (brief / flow / decision_log / retro / demo) 순회
3. 각 페이지:
   - `page.goto(url, wait_until="networkidle")`
   - `wait_for_selector(".notion-page-content", timeout=30000)`
   - `text = page.inner_text(".notion-page-content")`
   - 페이지 간 2~3초 대기
4. **세션 만료 감지**: URL이 `/login` 으로 리다이렉트되거나 본문 길이 0 → 강사에게 재로그인 안내 후 종료
5. 결과 저장: `{team, fetched_at, brief: text, flow: text, ...}`

**예외 처리**:
- URL 미등록 팀 → skip + 로그 출력
- 페이지 404 / 권한 없음 → 해당 필드 `null` 후 계속
- DOM 셀렉터 변경 → fallback 셀렉터 시도

### 5-3. `teams.yaml` 확장 명세

```yaml
- name: "1팀"
  members: 4
  github:
    repo: "ORG/team1-repo"
  notion_urls:
    brief: "https://www.notion.so/팀명/01-Brief-..."
    flow: "https://www.notion.so/팀명/02-Flow-..."
    decision_log: "https://www.notion.so/팀명/06-Decision-..."
    retro: "https://www.notion.so/팀명/07-Retro-..."
    demo: "https://www.notion.so/팀명/05-Demo-..."   # 선택
```

> 강사가 게스트 편집 권한으로 접근 가능한 URL이어야 함. 1주차 OT 후 학생 PO로부터 페이지 URL 5개 받아 등록.

### 5-4. `analyze.py` 확장 명세

기존 입력(`_github.json`, `_status.json`, `_pdf/`)에 더해 `_notion.json` 추가:

```python
def analyze_notion(notion_json: dict) -> dict:
    brief = notion_json.get("brief", "") or ""
    return {
        "brief_lines": count_nonblank_lines(brief),
        "wont_count": count_bullets_after(brief, WONT_KEYWORDS),
        "must_count": count_bullets_after(brief, MUST_KEYWORDS),
        "target_has_everyone_word": has_word(brief, EVERYONE_WORDS),
        # 자기보고와 cross-check 가능
    }
```

신호등 추가:
- 🟡 **자기보고 vs 노션 본문 불일치** (예: Status Issue Won't=3인데 노션 본문에선 0개)
  - cross-validation, 자기보고 부풀리기 자동 탐지

### 5-5. `.gitignore` 추가

```
operation/storage_state.json
operation/.playwright-cache/
operation/playwright-browsers/
```

### 5-6. `CLAUDE.md` 패턴 추가

```markdown
### 패턴 E: "Week N 노션 수집" 또는 "노션 자동 추출"

1. `python scripts/fetch_notion_browser.py --week N`
2. 세션 만료 시 사용자에게 `setup_notion_login.py` 재실행 안내
3. 정상 완료되면 `python scripts/analyze.py --week N` 실행 → 신호등 갱신
4. 보고: "5팀 × 5페이지 추출 완료. 🔴 = 0 / 🟡 = 2 (자기보고 vs 노션 불일치)"

⚠️ 절대 금지:
- 학생 동의 없이 새 URL 추가
- 강사가 직접 호출하지 않은 시점에 자동 실행
- 추출 본문을 외부 API로 전송 (Claude에는 분석 결과만 전달)
```

### 5-7. `docs/notion_browser_workflow.md` (PLAN 컨펌 후 작성)

목차 예정:
1. 사전 셋업 (1회) — pip install / playwright install / setup_notion_login
2. teams.yaml URL 등록
3. 매주 호출 절차
4. 세션 만료 대응
5. DOM 변경 시 셀렉터 갱신 절차
6. 6주 후 폐기 절차
7. 트러블슈팅

---

## 6. 운영 흐름 (확정 시)

### 6-1. 1회 사전 셋업 (강사, 5분)

```
1. pip install playwright
2. playwright install chromium             ~250MB 다운로드
3. python scripts/setup_notion_login.py    헤드 브라우저 띄움
4. 강사가 회사 노션 SSO/MFA 통과
5. 회사 워크스페이스 진입 확인 후 터미널에서 Enter
6. storage_state.json 저장 완료
```

### 6-2. 1주차 OT 후 (강사, 10분)

```
1. 학생 PO들로부터 팀별 노션 URL 5개씩 수집
2. teams.yaml 의 notion_urls 필드 채움
3. 1팀만 시범 호출: python scripts/fetch_notion_browser.py --week 1 --team 1팀
4. snapshots/week-1/1팀_notion.json 본문 검증
5. 검증 OK시 전체 팀 일괄 호출
```

### 6-3. 매주 일요일 23:59 이후 (강사, 1~2분)

```
1. Claude에게: "Week N 노션 수집"
2. Claude가 fetch_notion_browser.py 실행 (~3분)
3. analyze.py 자동 실행
4. reports/week-N.md 갱신
5. Claude가 🔴 / 🟡 만 5줄 요약 보고
```

### 6-4. 세션 만료 감지 시 (강사, 1분)

```
1. fetch 스크립트가 "Session expired" 출력 + exit
2. python scripts/setup_notion_login.py 재실행
3. 다시 fetch 스크립트 호출
```

---

## 7. 위험 / 완화 매트릭스

| 위험 | 가능성 | 영향 | 완화 |
|---|---|---|---|
| 회사 SSO가 매번 MFA 요구 | 중 | 자동화 무효화 | Headed 모드 + 강사가 매번 통과 (반자동, 시간 비용은 증가하지만 PDF보단 빠름) |
| Notion DOM 셀렉터 변경 | 중 (분기마다) | 추출 0줄 | fallback 셀렉터 + 매주 검증, 변경 시 1줄 수정 |
| Notion 페이지 로딩 지연 | 낮 | 일부 누락 | `wait_for_selector` 30s + 재시도 1회 |
| 세션 만료 미감지 | 중 | 빈 데이터 저장 | 본문 길이 < 50 시 fail-fast + 알림 |
| storage_state.json 누출 | 낮 | 강사 노션 전체 탈취 | .gitignore + 강사 PC 외 노출 금지 |
| 회사 IT 모니터링 탐지 | 매우 낮 | 강사 계정 차단 가능 | 빈도/속도 제한 + 사람 패턴 유지 + 운영진 사전 보고 |
| Notion ToS 분쟁 | 매우 낮 | 강사 책임 | 운영 원칙 6가지 문서화로 *"수동 가속"* 의도 입증 |
| 학생 동의 갱신 거부 | 낮 | 해당 팀 제외 | OT 공지문에 *"브라우저 자동화 기반"* 명시 + 동의 거부 시 PDF 수동 fallback |
| 6주 후 폐기 누락 | 중 | 흔적 잔류 | Week 6 회고 후 체크리스트로 폐기 강제 |

---

## 8. PDF 방식과의 관계

| 옵션 | 설명 | 권장도 |
|---|---|---|
| **A. PDF 즉시 폐기** | 자동화만 사용 | △ Week 1~2 안정성 미검증 |
| **B. 영구 병행** | PDF + 자동 둘 다 | ✗ 강사 시간 부담 가중 |
| **C. Week 1~2 병행 → Week 3부터 자동만** | 과도기 + 검증 | **◎ 추천** |

**추천 운영**:
- Week 1~2: PDF + 자동 둘 다 추출, 결과 cross-check
- Week 2 말 회고: 자동 추출이 PDF와 일치하면 자동 단독 전환
- Week 3~6: 자동만, PDF 폐기

---

## 9. 진행 / 철수 트리거

### 진행 OK 조건 (모두 충족)

- [ ] 운영진에 *"강사 트리거 자동화 도구 사용 방식 변경"* 추가 보고 + 묵시적 OK
- [ ] 강사 PC에 Python 3.11+ + 250MB 디스크 여유
- [ ] 회사 노션 게스트 계정 SSO 통과 가능
- [ ] 1팀 시범 호출에서 5페이지 텍스트 추출 검증 완료

### 철수 조건 (1개라도 충족 시)

- [ ] 회사 IT 정책으로 Playwright/자동화 명시 차단
- [ ] Notion DOM 격변으로 매주 셀렉터 수정 30분 이상 소요
- [ ] 회사 SSO 정책 강화로 호출마다 MFA 필요 (사실상 수동과 동일)
- [ ] 학생 동의 갱신 거부 (50% 이상 팀)
- [ ] 운영진 추가 보고에서 거부 회신

→ 철수 시 즉시 PDF 방식으로 회귀, `storage_state.json` 삭제, Playwright 정리.

---

## 10. 다음 액션 (이 PLAN 컨펌 후)

순서대로:

1. **사용자 컨펌** (본 문서 검토 + 진행 여부 결정)
2. **운영진 추가 보고** (사용자가 1줄 메시지 작성, 필요 시 강사가 작성 도움 요청)
3. **PC 환경 점검** (Python 3.11+, 디스크, 회사 SSO 노션 로그인 가능 여부)
4. **7개 파일 작업** (한 번에, 약 30분)
5. **1팀 시범 호출** (검증)
6. **PDF 방식과 1~2주 병행** (옵션 C)
7. **Week 3 안정 시 PDF 단독 폐기**

---

## 11. 갱신 정책

- 이 PLAN 문서는 **컨펌 시점에 한 번 작성**, 이후 갱신하지 않음
- 컨펌 후 [`docs/notion_browser_workflow.md`](notion_browser_workflow.md) 가 운영 SSOT 역할
- 본 PLAN은 회고용으로 보관 (왜 이 결정을 했는지 기록)

---

## 12. 사용자 컨펌 체크리스트

진행 여부 결정 전 다음을 확인해주세요:

- [ ] 운영진에게 추가 보고할 의향이 있다 (또는 사전 승인 범위에 자동화 방식 변경이 포함된다고 판단)
- [ ] 강사 PC에 Python + Playwright 설치 가능 (관리자 권한 / 회사 IT 정책 확인)
- [ ] § 3 운영 원칙 6가지를 6주간 준수 가능
- [ ] PDF + 자동 1~2주 병행 (옵션 C) 동의 또는 다른 옵션 명시
- [ ] § 9 철수 조건 발동 시 즉시 PDF 회귀에 동의

5개 모두 OK이면 *"PLAN 컨펌, 진행해라"* 한마디로 7개 파일 작업 시작합니다.
