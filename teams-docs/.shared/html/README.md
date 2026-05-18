# HTML 변환 시스템

> **목적**: MD 스냅샷을 빠른 인지가 가능한 HTML로 자동 변환 + 코호트 메타 대시보드 제공
> **설계**: A + C 혼합 — 스냅샷별 HTML + 코호트 메타 대시보드
> **MD가 source of truth, HTML은 사용자 읽기 전용** (Claude는 MD만 편집·검색)

---

## 1. 구성 파일

| 파일 | 용도 |
|---|---|
| `snapshot_template.html` | 공통 템플릿 (모든 팀 동일 양식 보장) |
| `md_to_html.py` | MD → HTML 변환기 (외부 의존성 0) |
| `generate_dashboard.py` | 코호트 메타 대시보드 생성기 |
| `dashboard.html` | 가장 최근 대시보드 (자동 갱신) |
| `dashboard_YYMMDD_(am\|pm).html` | 일자/시간대별 대시보드 |

---

## 2. 사용법

### 2-1. 스냅샷 1개 변환

```bash
PYTHON="/c/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe"
cd c:/Users/ibebu/bootcamp6_final/archive/teams-docs
"$PYTHON" .shared/html/md_to_html.py 1team/snapshots/260518_pm.md
```

→ `1team/snapshots/260518_pm.html` 생성 (MD 옆 위치)

### 2-2. 코호트 대시보드 생성 (오늘 자동 인식)

```bash
"$PYTHON" .shared/html/generate_dashboard.py
```

→ 현재 시각 기준 자동으로 AM/PM 결정. `dashboard.html` + `dashboard_YYMMDD_<am|pm>.html` 생성.

### 2-3. 특정 날짜/시간대 대시보드

```bash
"$PYTHON" .shared/html/generate_dashboard.py 260518 pm
```

### 2-4. 한 번에 모두 변환 (3팀 × AM·PM = 6건 + 대시보드 2건)

```bash
PYTHON="/c/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe"
DATE="260518"  # YYMMDD
cd c:/Users/ibebu/bootcamp6_final/archive/teams-docs
for slot in am pm; do
  for team in 1team 2team 3team; do
    [ -f "$team/snapshots/${DATE}_${slot}.md" ] && "$PYTHON" .shared/html/md_to_html.py "$team/snapshots/${DATE}_${slot}.md"
  done
  "$PYTHON" .shared/html/generate_dashboard.py "$DATE" "$slot"
done
```

---

## 3. HTML 구조 (사용자 인지 우선)

### 3-1. 헤더 + 정량 지표 카드 (4개 가로 배치)
- 회의 시간 / 사전 통지 이행률 / Carry-over 이행률 / 신호등 회고

### 3-2. 🎯 이번 회의에서 확인할 사항 (최상단 강조 박스 ⭐ 사용자 핵심 요구)
- 🚨 강사 사전 통지 의제 3건 (✅/❌/△ 자동 마킹)
- ⚡ 즉시 조치 (있을 때)
- 🔒 회의 외 강사 1:1 (있을 때)

### 3-3. 🟢 한 줄 요약 + 회의 결과 인용

### 3-4. ▼ 펼침 섹션 (회의 외에는 접힘)
- 18h/6h Finished/Will finish
- 액션 아이템 진척 (PM)
- 보드 cross-check
- Carry-over 이행률 표
- R-항목 점검 표
- 인물별 활동 + 회의 발화 정합성
- 보조강사 권장 액션
- 신호등 회고 (PM)
- Phase-gate (3팀)

→ 일부 섹션은 기본 펼침 (회의 cheat sheet / 보조강사 권장 액션 / 6h 변화 등)

---

## 4. 운영 권장 사항

### 4-1. 회의 후 워크플로우
1. `/team-meeting-transcribe` 호출 → MD 스냅샷 갱신 + mom 작성 + 메모리 갱신
2. **HTML 자동 재생성** (위 2-4 명령) → 사용자가 HTML로 인지
3. 대시보드 (`dashboard.html`)로 코호트 차원 비교

### 4-2. 스킬에 통합 (선택)
`team-check-am` / `team-check-pm` / `team-meeting-transcribe` 스킬의 마지막 단계에 HTML 자동 생성 명령 추가 가능. 단 매 호출마다 HTML 생성은 디스크 부하 (현재 1개 스냅샷 ~45KB).

### 4-3. 브라우저로 열기
- 단일 스냅샷: `1team/snapshots/260518_pm.html` 더블클릭 (또는 `file://` 직접 열기)
- 대시보드: `.shared/html/dashboard.html` 또는 `dashboard_260518_pm.html`

### 4-4. CDN 의존성
- Tailwind CSS는 CDN 로드 (`https://cdn.tailwindcss.com`)
- **오프라인에서는 스타일 깨짐** — 단 구조와 내용은 그대로 표시 가능
- 인터넷 가능 환경에서 사용 권장

---

## 5. 한계 / 개선 여지

1. **음성 인식 오류**: MD 본문의 transcript 인용은 그대로 HTML로 옮겨짐 (한국어 인식 오류 포함)
2. **표 컬럼 너무 많을 때**: 가로 스크롤 발생 (모바일 대응 미흡)
3. **정량 지표 추출 정규식**: 양식 변경 시 미스 가능 — `md_to_html.py` `extract_metrics()` 함수 조정 필요
4. **신호등 회고 추출**: "❌ N일 연속" 같은 짧은 패턴만 추출 — 풍부한 상태는 본문 펼침에서만 확인

---

## 6. 트러블슈팅

### Python 없음
```bash
"$PYTHON" --version
```
→ Python 3.14.x 확인. 없으면 사용자에게 안내.

### Tailwind 스타일 깨짐
→ 인터넷 연결 확인. 또는 인라인 CSS로 전환 (현재 `<style>` 블록에 핵심 스타일 포함되어 있어 기본 가독성은 유지됨)

### 변환 결과 누락
→ MD 양식이 v2 표준과 다르면 `extract_*` 함수들이 잘못된 결과 반환 가능. MD 양식 표준 유지 권장.
