# 1팀 FigJam 보드 자료 (Scoffee)

> **원본 URL**: https://www.figma.com/board/NKBVG1F8BcFXzjpnJxsC4u/파이널-1조-피그마
> **fileKey**: `NKBVG1F8BcFXzjpnJxsC4u`
> **타입**: FigJam Board (작업/계획용)
> **최초 수집**: 2026-05-13 (Week 2 마감 시점)
> **접근 방법**: Figma Personal Access Token + REST API

---

## 1. 폴더 구조

```
teams-docs/.shared/.figma_token    ← 공유 토큰 (1/2/3팀 공통, gitignored)

teams-docs/1team/figma/
├── README.md                  ← 본 문서
├── raw/                       ← API 원본 JSON (디핑용)
│   ├── board_depth1.json
│   ├── page1_depth2.json
│   ├── sprint0.json
│   ├── sprint1.json
│   ├── node_207_144.json
│   └── image_urls.json
└── screens/                   ← 섹션별 PNG (vision 분석용, daily check 임베드)
    ├── wireframes_YYMMDD.png       ← 와이어프레임 전체 흐름
    ├── flow_a_YYMMDD.png            ← FLOW-A (카페인 입력 → 잔류량 → 워치)
    ├── flow_b_YYMMDD.png            ← FLOW-B (수면 → 리포트)
    ├── design_output_YYMMDD.png     ← 실제 UI 디자인 출력
    ├── app_theme_YYMMDD.png         ← 디자인 시스템 (색/타이포)
    ├── db_YYMMDD.png                ← DB 스키마
    ├── mvp_priorities_YYMMDD.png    ← MVP Must/Should/Won't 우선순위
    ├── mindmap_YYMMDD.png           ← 주제 마인드맵
    └── file_versions_YYMMDD.png     ← 파일 버전 관리
```

---

## 2. 보드 구조 (2026-05-13 기준)

FigJam Page 1에는 **Sprint 0 / Sprint 1** 두 섹션이 핵심:

### Sprint 0 (id=197:246) — Week 1~2 작업물 누적
13개 하위 섹션:

| 섹션명 | Node ID | 크기 | PRD 매핑 | 상태 |
|---|---|---|---|---|
| 5/4 프리뷰 | 1:5 | 2352×2096 | (사전 계획) | ✅ |
| 5/6 주제 마인드 맵 | 20:10 | 1712×1104 | (사전 계획) | ✅ |
| 앱 예상 기능 및 MVP 우선순위 | 50:240 | 1712×1024 | Brief § 7 | ✅ |
| 5/7 - 디렉터리 구조 | 88:30 | 2064×4716 | 아키텍처 | ✅ |
| 5/7 - Flow - A | 131:33 | 2102×4250 | **Brief § 10 FLOW-A** | ✅ |
| Section 3 | 137:224 | 1008×352 | (작은 메모) | — |
| Section 4 | 137:320 | 1×984 | (비어있음) | — |
| DB | 142:81 | 1773×2032 | Must-2/3 데이터 모델 | ✅ |
| App Theme | 151:116 | 1405×2160 | 디자인 시스템 | ✅ |
| 5/8 - Flow - B | 201:392 | 2416×4196 | **Brief § 10 FLOW-B** | ✅ |
| 와이어프레임 | 207:144 | 2944×2672 | 전체 화면 흐름 | ✅ |
| 파일 버전 | 381:422 | 1664×1072 | (관리용) | — |
| **디자인 출력** | 430:686 | **7174×5952** | 실제 UI 산출물 | ✅ |

### Sprint 1 (id=439:631) — Week 3+ 계획
**현재 비어있음** (children: 0). Week 3 시작이 임박했는데 계획이 안 잡혀있다는 신호 🟡 → 일일 체크 시 강조.

### 페이지 최상위 떠다니는 메모 (8개)
디자인 피드백 메모:
- "활동 로그 앞에 카페 아이콘 배제 (우선순위 후순위)"
- "주간말고 캘린더로 뽑아내기"
- "흰거 샤아~ 한거 말고 그림자로"
- "번짐 효과 말고 그림자"
- "음료이름 추가"
- 다이얼로그 / image 65 / Shape with text

→ **실시간 디자인 토론의 흔적**. 살아있는 작업 보드라는 좋은 신호.

---

## 3. PRD ↔ Figma 정합성

| PRD Must | Figma 섹션 | 매칭? |
|---|---|---|
| Must-1 온보딩 | 와이어프레임 (권한 동의 → 온보딩) | ✅ |
| Must-2 카페인 입력 | Flow-A + 와이어프레임 (카페인 분기) | ✅ |
| Must-3 잔류량 계산 | MVP 우선순위 (C(t)=C₀×(1/2)^(t/T) 공식) | ✅ |
| Must-4 Gemini 컷오프 | (별도 시각화 없음) | 🟡 |
| Must-5 WearOS 알림 | (별도 시각화 없음) | 🟡 |
| Must-6 Health Services | 와이어프레임 (수면 분기) | 🟡 |
| Must-7 주간 리포트 | Flow-B + 와이어프레임 (리포트 분기 5개 화면) | ✅ |

→ Must-4 (Gemini), Must-5 (WearOS)는 FigJam엔 시각화 안 됨. 코드로만 진행 중. **Sprint 1에 시각화 추가 권장**.

---

## 4. 매일 자동 수집 절차 (보조강사용)

### 4-1. 토큰 환경

토큰은 [`teams-docs/.shared/.figma_token`](../../.shared/.figma_token) 파일에 1줄로 저장 (gitignored).
**1/2/3팀 공통**으로 사용. 변경 정책 및 신규 팀 추가는 [`teams-docs/.shared/README.md`](../../.shared/README.md) 참조.

### 4-2. 수집 명령어 (체크 시마다 실행)

```bash
cd /c/Users/ibebu/bootcamp6_final/archive
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
FILE_KEY="NKBVG1F8BcFXzjpnJxsC4u"
DATE=$(date +%y%m%d)

# 1. 보드 메타 (lastModified 비교용)
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/$FILE_KEY?depth=1" \
  -o teams-docs/1team/figma/raw/board_${DATE}.json

# 2. Sprint 0/1 구조 (섹션 추가/삭제 감지)
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/$FILE_KEY/nodes?ids=197:246,439:631&depth=2" \
  -o teams-docs/1team/figma/raw/sprints_${DATE}.json

# 3. 핵심 섹션 PNG 갱신
IDS="207:144,131:33,201:392,430:686,151:116,142:81,50:240"
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/images/$FILE_KEY?ids=$IDS&format=png&scale=1" \
  -o /tmp/figma_urls.json
# (이후 jq 또는 PowerShell로 URL 파싱 후 wget/curl로 다운로드)
```

### 4-3. 디핑 (변경 감지)

매일 다음 항목을 직전 스냅샷과 비교:

| 항목 | 비교 방법 |
|---|---|
| `lastModified` 타임스탬프 | board_YYMMDD.json 의 `lastModified` 필드 |
| Sprint 0/1 자식 노드 수 | sprints_YYMMDD.json 의 `children.length` |
| 신규 섹션 추가 | 직전 노드 ID 목록과 set 차집합 |
| 디자인 출력 영역 크기 변화 | 노드 430:686의 `absoluteBoundingBox` 비교 |

---

## 5. 일일 체크 통합 항목

`teams-docs/1team/review/daily_check_guide.md` § 5에 추가된 Figma 점검 체크리스트:

- [ ] 보드 `lastModified` 24h 이내 갱신 있었는가?
- [ ] Sprint 1에 신규 항목이 들어갔는가? (Week 3 계획 가시화)
- [ ] 와이어프레임/디자인 출력 영역에 신규 화면 추가됐는가?
- [ ] 떠다니는 피드백 메모에 미반영 사항이 있는가?
- [ ] FigJam은 갱신됐는데 코드는 변화 없는 영역이 있는가?
- [ ] Must-4(Gemini)/Must-5(WearOS) 시각화 추가됐는가?

---

## 6. 보안 — 토큰 관리

### 현재 토큰 상태

| 항목 | 값 |
|---|---|
| 파일 위치 | [`teams-docs/.shared/.figma_token`](../../.shared/.figma_token) (1/2/3팀 공통) |
| Git 추적 | ❌ gitignored ([archive/.gitignore](../../../.gitignore)) |
| Scope | File content: Read-only (권장) |
| 소유 계정 | qk54r71z@gmail.com (JongBeom Park) |
| 발급일 | 2026-05-13 |
| 관리 가이드 | [`teams-docs/.shared/README.md`](../../.shared/README.md) |

### ⚠️ 즉시 조치 권장 — 토큰 재발급

이 토큰은 **최초 설정 과정에서 대화 transcript에 노출**됐습니다. 보안 모범 사례를 따르려면:

1. **figma.com → Settings → Security → Personal access tokens** 접속
2. 현재 토큰(`claude-ta-readonly` 또는 발급명) **Revoke**
3. **새 토큰 발급** (동일 스코프: File content Read-only)
4. `teams-docs/.shared/.figma_token`에 새 토큰 덮어쓰기

→ Read-only + bootcamp 학습 자료라 실질 위험은 낮으나, 습관화 차원에서 권장.

### 6주 프로젝트 종료 후

2026-06-16 이후 토큰 **반드시 Revoke**. 더 이상 필요 없습니다.

### 토큰 누출 시

만약 토큰이 GitHub/공개 채널에 유출되면:
1. 즉시 figma.com → Revoke
2. 새 토큰 발급
3. git history에 남았다면 `git filter-branch` 또는 BFG로 제거 + force push

---

## 7. 참조

- [Daily Check Guide](../review/daily_check_guide.md)
- [Issue ↔ PR Matrix](../review/issue_pr_matrix.md)
- [Brief Review Week 1](../review/brief_review_w1.md)
- [Snapshots](../snapshots/)
- [Figma REST API Docs](https://www.figma.com/developers/api)
