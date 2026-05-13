# 2팀 Figma 보드 자료 (Umma)

> **프로젝트명**: **Umma (움마)** — AI 기반 초개인화 영어 학습 서비스
> **팀 닉네임**: 라스트딴따라
> **GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma
> **Figma URL**: https://www.figma.com/design/BTzbV8SDChx6ywQwycniQi/멋사-파이널-2팀-라스트딴따라
> **fileKey**: `BTzbV8SDChx6ywQwycniQi`
> **타입**: Figma **Design** 파일 (1팀과 달리 FigJam 아님)
> **권한**: viewer
> **최초 수집**: 2026-05-13 (Week 2 마감 시점)

> ⚠️ **명칭 주의**: Figma 파일명("라스트딴따라")은 팀 닉네임이고 **실제 앱명은 Umma**입니다. 점검 보고서에서는 "Umma"로 통일.

---

## 1. 프로젝트 컨텍스트 (GitHub README + Figma 종합)

| 항목 | 값 | 출처 |
|---|---|---|
| 앱 종류 | **AI 기반 영어 학습 — 초개인화 언어 성장 서비스** | GitHub README |
| 핵심 슬로건 | "아기는 문법을 배워서 말하지 않습니다. '말하고 싶어서' 배웁니다." | GitHub README |
| 핵심 루프 | **대화 → 교정 → 저장 → 반복학습 → 성장 추적** | GitHub README |
| 메뉴 구성 | **AI Chat · 교정 · Flashcard · Dashboard · Statistics** | GitHub README (Figma의 복습/대화/대화기록/플래시카드/통계와 매칭) |
| 핵심 기술 | Firebase AI Logic + **Gemini Live** + Push-to-Talk 음성 대화 | GitHub README |
| 컬러 톤 | 베이지/오렌지 (워밍 톤) | Figma `color 정하기` 섹션 |
| 대상 사용자 | 실전형/꾸준형/성장형 학습자 (20~30대 직장인) | GitHub README |

### 1팀과의 흥미로운 공통점

- 두 팀 모두 **Gemini API 의존** (1팀: 카페인 컷오프 산출 / 2팀: 실시간 음성 회화)
- 두 팀 모두 **MVP에서 학습/추적 루프 강조**
- 단, 1팀은 카페인-수면 도메인, 2팀은 **언어 학습 도메인** — 완전히 다른 분야

---

## 2. 폴더 구조

```
teams-docs/.shared/.figma_token   ← 공유 토큰 (1/2/3팀 공통)

teams-docs/2team/figma/
├── README.md                       ← 본 문서
├── raw/                            ← API 원본 JSON
│   ├── board_depth2.json
│   └── image_urls.json
└── screens/                        ← 섹션별 PNG
    ├── wireframe_prep_YYMMDD.png       ← 와이어프레임 준비 작업
    ├── wireframe_main_YYMMDD.png       ← 와이어프레임 작업 with sitch
    ├── wireframes_page_YYMMDD.png      ← 와이어프레임 페이지의 메인 섹션
    ├── v1_4_latest_YYMMDD.png          ← 최신 버전 (v1.4)
    ├── daily_0511_YYMMDD.png           ← 최근 일별 작업
    ├── color_palette_YYMMDD.png        ← 색상 정하기
    ├── colors_info_YYMMDD.png          ← 색상 정보 (정리본)
    └── fonts_info_YYMMDD.png           ← 글꼴 정보
```

---

## 3. 보드 구조 (2026-05-13 기준)

Design 파일이라 **PAGE 단위**로 분리됨:

### Page 1 — "Draft" (작업 중, 244 children)

핵심 28개 노드:

| 분류 | 섹션명 | Node ID | 크기 |
|---|---|---|---|
| **일별 작업** | 05-07 목 | 90:95 | 2000×2161 |
| | 05-08 금 (v1.2) | 137:2 | 2671×3459 |
| | 05-09 | 268:202 | 1066×1036 |
| | 05-11 | 293:167 | 1066×581 |
| **버전 관리** | v1.1 | 199:6 | 2670×2785 |
| | v1.3 | 251:33 | 2670×2785 |
| | **v1.4 (최신)** | 263:13 | 2670×2785 |
| **와이어프레임** | 와이어프레임 준비 작업 | 266:11 | 3080×4634 |
| | 와이어프레임 작업 with sitch | 340:12 | 5728×4665 |
| **디자인 시스템** | color 정하기 | 179:4 | 1707×2527 |
| **기타** | 섹션 링크 포커스 | 401:73 | 2211×1941 |
| | Section 9 / Section 10 | 482:549 / 527:3758 | 7785×6833 / 9603×8208 |

### Page 2 — "와이어프레임" (정리된 산출물, 4 children)

| 섹션명 | Node ID | 크기 | 비고 |
|---|---|---|---|
| 색상 정보 | 392:38 | 1084×1791 | 정리본 |
| **와이어프레임** | 400:15 | 5854×6846 | 핵심 산출물 |
| 글꼴 정보 | 410:195 | 866×864 | 정리본 |
| Heading 1 (Frame) | 482:2661 | 0×26 | 텍스트 라벨 |

> ✅ **좋은 신호**: 1팀과 달리 **"와이어프레임" 페이지를 별도로 분리**해 정리해두는 패턴. Draft에서 작업하고 와이어프레임 페이지에 최종본을 옮기는 운영 방식으로 추정.

---

## 4. 2팀 작업 패턴 (점검 시 핵심)

### 4-1. 빠른 iteration (v1.1 → v1.3 → v1.4)

5월 7일 시작 후 일주일 만에 **버전 4번 갱신**. 매우 빠른 디자인 iteration. 일일 체크 시:
- v1.x 신규 버전 등장 → 좋은 신호
- v1.x가 일정 기간 정체 → 의사결정 막힘 가능성 점검
- v1.x 사이 변경점 (이름·구조 비교) 추적

### 4-2. 일별 작업 섹션 누적

`05-07 목` `05-08 금 (v1.2)` `05-09` `05-11` ... → **일자 단위 작업 흔적 보존**. 결손된 날짜(05-10 등)는 정상 (주말 가능).

### 4-3. 디자인 ↔ 코드 정합성

2팀 GitHub 저장소 (등록 시 추가) ↔ Figma 페이지 매칭:
- "와이어프레임" 페이지에 있는 화면이 코드 구현되었는가?
- 코드엔 있는데 와이어프레임엔 없는 화면?

---

## 5. 매일 자동 수집 명령

```bash
cd /c/Users/ibebu/bootcamp6_final/archive
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
FILE_KEY="BTzbV8SDChx6ywQwycniQi"
DATE=$(date +%y%m%d)

# 1. 보드 메타 + 페이지 구조
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/$FILE_KEY?depth=2" \
  -o teams-docs/2team/figma/raw/board_${DATE}.json

# 2. 핵심 섹션 PNG 갱신 (최신 버전 + 와이어프레임)
IDS="263:13,266:11,340:12,400:15,179:4"
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/images/$FILE_KEY?ids=$IDS&format=png&scale=1"
```

### 디핑 포인트

- `lastModified` 타임스탬프
- Draft 페이지의 `v1.x` 최대 버전 번호 (신규 버전 진입 감지)
- 일별 섹션 추가 여부 (`05-12`, `05-13` 등)
- 와이어프레임 페이지의 자식 수 변화

---

## 6. 일일 체크 통합 항목 (2팀 특화)

- [ ] 최신 버전(v1.x) 갱신 있었나
- [ ] 신규 일별 섹션 추가 있었나
- [ ] "와이어프레임" 페이지에 신규 화면 옮겨왔나
- [ ] color 정하기 / 글꼴 정보 섹션 변동 있나 (디자인 시스템 안정화 신호)
- [ ] Draft 페이지가 너무 비대해지지 않았나 (244+ children 부담)

---

## 7. Umma 저장소 베이스라인 스캔 결과 (2026-05-13 기준)

GitHub 저장소 (https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma) 스캔:

| 지표 | 값 | 평가 |
|---|---|---|
| 기본 브랜치 | `develop` (Git Flow) | ✅ 1팀과 동일 |
| 총 브랜치 | 11개 (main + develop + feature/ × 3 + system/ × 2 + docs/ × 2 + common) | ✅ 활발 |
| 최근 커밋 | 2026-05-13 10:16 (오늘) | ✅ 활성 |
| 총 PR | 48개 머지 (#1~#48) | 🟢 **1팀(13개) 대비 3.7배** — 매우 활발 |
| 기여자 | woals/박재민(91) · Wanna(8) · taehwan(1) | 🟡 **75:10 비대칭** (R8 후보) |

### 기여자 매핑 (이메일 기반 추정)

| GitHub | 이메일 | 추정 인물 |
|---|---|---|
| `woals` | woals6318@gmail.com | 박재민 (75 커밋) |
| `박재민1` | woals6318@gmail.com | 박재민 (10 커밋, 이메일 동일) |
| `박재민2` | woals6318@hufs.ac.kr | 박재민 (6 커밋, 학교 메일) |
| `Wanna` | sangsangcat@gmail.com | (이름 미확인) (5 커밋) |
| `wanna` | sangsangcat@gmail.com | 동일인 (3 커밋) |
| `taehwan-dev` | xkdl1108@gmail.com | (이름 미확인) (1 커밋) |

→ **3명 정도가 실 활동 중**. 박재민 한 사람이 전체의 ~91% 커밋. **점검 시 R8(단일 인물 과부하) 핵심 항목**.

### 🚨 발견된 보안 우려

| 파일 | 우려 |
|---|---|
| `app/google-services.json` | Firebase 설정. **Git에 커밋되어 있음** — 보통 secrets로 분류. 공개 저장소면 노출 위험 |
| `.idea/shelf/*.patch` | IDE shelf 패치 파일 12개+ 커밋됨 | 

→ 1팀에도 동일 패턴 점검 권장 (Snoffee에도 google-services.json 있는지 확인).

---

## 8. 향후 필요 자료

- ✅ ~~2팀 GitHub URL~~ — 입수됨
- ⬜ **2팀 product brief** — [teams-docs/2team/product_brief/](../product_brief/) 에 있다면 참조
- ⬜ **2팀 명세서(spec)** — [teams-docs/2team/spec/](../spec/) 폴더 존재 확인 필요
- ⬜ **2팀 멤버 정보** — 팀장/역할 분담 (특히 박재민 외 2명 신원)
- ⬜ Week 1 검토 작성 여부 (1팀 [brief_review_w1.md](../../1team/review/brief_review_w1.md) 참고)

---

## 9. 참조

- [공유 가이드](../../.shared/README.md)
- [1팀 figma README (모범 예시)](../../1team/figma/README.md)
- [2팀 product_brief](../product_brief/)
- [2팀 spec](../spec/)
- **2팀 GitHub**: https://github.com/LIKELION-Android-BOOTCAMP-6th/Umma
