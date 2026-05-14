---
name: team-meeting-transcribe
description: "각 팀 미팅 녹음(.mkv/.mp4/.wav 등)을 Buzz 번들 whisper로 transcribe 후, 회의 결과를 v2 양식 스냅샷에 반영 (강사 사전 통지 ✅/❌ + carry-over 이행률 + 신호등 회고) + 회의록(mom) 작성. 사용법: /team-meeting-transcribe <팀번호|all> [날짜 YYMMDD] [시간대 am|pm]. 보조강사 관점."
effort: medium
---

# team-meeting-transcribe — 회의 녹음 변환 + 점검 문서 갱신 (v2 양식)

> **양식**: [meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) (2026-05-14부 정식 적용)
> **목적**: 회의 결과를 v2 양식 14개 섹션에 반영하여 carry-over 이행률 측정 + 신호등 align 추적

## 사용법

```
/team-meeting-transcribe 1                    ← 1팀, 오늘 PM 자동 인식
/team-meeting-transcribe 2 260514 am          ← 2팀, 2026-05-14 오전
/team-meeting-transcribe all                  ← 3팀 모두, 오늘 PM
/team-meeting-transcribe all 260513 pm        ← 3팀 모두, 명시 날짜
```

## 0. 인자 파싱

| 인자 | 값 | 기본 |
|---|---|---|
| 팀번호 | `1` / `2` / `3` / `all` | (필수) |
| 날짜 | `YYMMDD` (예: `260513`) | 오늘 (`date +%y%m%d`) |
| 시간대 | `am` / `pm` | 현재 시각 기준 (12시 이전 am, 이후 pm) |

팀별 경로 매핑:

| 팀 | meeting 폴더 | PM 스냅샷 |
|---|---|---|
| 1 | `teams-docs/1team/meeting/` | `teams-docs/1team/snapshots/YYMMDD_(am\|pm).md` |
| 2 | `teams-docs/2team/meeting/` | `teams-docs/2team/snapshots/YYMMDD_(am\|pm).md` |
| 3 | `teams-docs/3team/meeting/` | `teams-docs/3team/snapshots/YYMMDD_(am\|pm).md` |

---

## 1. 사전 환경 점검 (1회만 필요)

### 1-1. Buzz 번들 도구 확인

다음 경로에 파일이 있어야 함 (Buzz 1.4+ 설치 후 자동):

```bash
"/c/Program Files (x86)/Buzz/_internal/ffmpeg.exe"
"/c/Program Files (x86)/Buzz/_internal/ffprobe.exe"
"/c/Program Files (x86)/Buzz/_internal/buzz/whisper_cpp/whisper-cli.exe"
```

설치 경로가 다르면 `C:\Program Files\Buzz` 또는 `winget search Buzz` 로 확인 후 변수로 교체.

### 1-2. Whisper 모델 확인 (한국어용 medium)

```bash
ls -la "/c/Users/ibebu/AppData/Local/Buzz/Buzz/Cache/models/ggml-medium.bin"
```

- **있으면**: 다음 단계 진행 (예상 크기 ~1.53GB)
- **없으면**: 다음 명령으로 다운로드 (~5~10분, 1회만):
  ```bash
  curl -L --progress-bar -o "/c/Users/ibebu/AppData/Local/Buzz/Buzz/Cache/models/ggml-medium.bin" \
    "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin"
  ```

### 1-3. 모델 선택 기준 (참고)

| 모델 | 크기 | 한국어 정확도 | 변환 속도 (CPU 4-thread) | 추천 |
|---|---|---|---|---|
| tiny | ~75MB | 낮음 | RTF 0.1 | ❌ |
| base | ~150MB | 보통 | RTF 0.2 | △ 짧은 회의 |
| small | ~500MB | 양호 | RTF 0.3~0.5 | ○ 빠른 1차 확인 |
| **medium** | **~1.5GB** | **좋음** | **RTF ~1.0** | **⭐ 기본** |
| large-v3 | ~3GB | 최고 | RTF 2~3 | 회고/공식 기록용 |

RTF = Real-Time Factor (1.0 = 실시간과 동일). 즉 30분 미팅이 medium 기준 약 30분 변환.

---

## 2. 단계별 절차 (순서 엄수)

### Step 1. 녹음 파일 검색

각 팀별로 다음 우선순위:

```bash
# 1순위: 이미 변환된 transcript가 있는가? (중복 작업 방지)
ls "teams-docs/<X>team/meeting/<YYMMDD>_<am|pm>_transcript.txt" 2>/dev/null

# 2순위: 원본 미디어 파일
find "teams-docs/<X>team/meeting/" \( -iname "*.mkv" -o -iname "*.mp4" -o -iname "*.wav" -o -iname "*.m4a" -o -iname "*.mp3" \) | head -5
```

전제: 파일명에 날짜·팀·시간 정보가 들어 있어야 함 (예: `2026-05-13 1팀 오후미팅.mkv`).
없으면 **가장 최근 mtime** 파일 선택, 사용자에 1회 확인:

> "발견된 녹음 파일: `<filename>` (수정 시각 `<mtime>`). 이 파일로 진행할까요?"

### Step 2. mkv → wav 추출 (ffmpeg)

```bash
FFMPEG="/c/Program Files (x86)/Buzz/_internal/ffmpeg.exe"
FFPROBE="/c/Program Files (x86)/Buzz/_internal/ffprobe.exe"

# whisper.cpp 입력 요구: 16kHz mono WAV (PCM s16le)
"$FFMPEG" -i "<원본 파일>" \
  -ar 16000 -ac 1 -c:a pcm_s16le -vn \
  "teams-docs/<X>team/meeting/<YYMMDD>_<am|pm>.wav" \
  -y -loglevel error -stats

# 길이 확인
"$FFPROBE" -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \
  "teams-docs/<X>team/meeting/<YYMMDD>_<am|pm>.wav"
```

추출 속도: 5~10초 (디스크 IO 위주). 출력 wav는 30분 미팅 = 약 56MB.

### Step 3. whisper-cli로 transcribe

```bash
WHISPER="/c/Program Files (x86)/Buzz/_internal/buzz/whisper_cpp/whisper-cli.exe"
MODEL="/c/Users/ibebu/AppData/Local/Buzz/Buzz/Cache/models/ggml-medium.bin"

"$WHISPER" -m "$MODEL" \
  -f "teams-docs/<X>team/meeting/<YYMMDD>_<am|pm>.wav" \
  -l ko \
  -otxt \
  -nt \
  -of "teams-docs/<X>team/meeting/<YYMMDD>_<am|pm>_transcript" \
  -t 4 -p 1 -pp
```

옵션 의미:
- `-l ko`: 한국어 (auto-detect 대비 정확도↑)
- `-otxt`: 텍스트 출력
- `-nt`: 타임스탬프 제거 (분석 친화적). 타임스탬프 필요하면 제거
- `-of <prefix>`: 출력 파일 prefix (`.txt` 자동 추가)
- `-t 4 -p 1`: 4 thread × 1 processor
- `-pp`: 진행률 출력

**all 모드**: 3개 팀을 순차 변환 (병렬은 CPU 경합으로 비효율). `run_in_background: true` 로 백그라운드 실행 + 완료 알림 대기.

### Step 4. transcript 읽기 + 회의 분석 (v2 metrics)

생성된 `<YYMMDD>_<am|pm>_transcript.txt` 를 Read 도구로 읽고, 다음 **10가지**를 추출:

1. **참여자 식별** — 이름·역할 (transcript는 화자 분리 안 됨 → 발화 내용으로 추정)
2. **회의 분량** — 총 시간 (분/초) + Atlassian 권장(15분) 대비 % — v2 timebox 측정 지표
3. **진척 보고** — 각 멤버 작업 영역 + Must 매핑
4. **강사 지적 사항** — "왜", "이렇게 하셔야죠", "빼주세요" 등 지시어
5. **합의 사항** — "알겠습니다", "그렇게 하겠습니다", "당분간 ~ 체제" 등 확정 표현
6. **R-항목 발현** — google-services, 권한 이슈, 패키지 리팩토링, 부담 신호(R-burn), 결석·조퇴(R-attend), 발언 0건(R-quiet) 등
7. **미합의 / 이월 항목** — 사전 체크리스트에 있었으나 회의에서 다뤄지지 않은 의제
8. **🚨 강사 사전 통지 의제 3건 다뤄짐 여부** ⭐ v2 신규:
   - 직전 스냅샷의 §🚨 강사 사전 통지 3건을 transcript 발화와 매칭
   - 각 항목 ✅ (강사 발화로 다뤄짐) / ❌ (미언급)
   - **회의-내 이행률 = ✅ 수 / 3**
9. **🔁 Carry-over 이행률** ⭐ v2 신규:
   - 직전 스냅샷의 §🔁 Carry-over 표를 transcript 발화와 매칭
   - 각 항목 ✅ / ❌ + 이월 횟수 +1
   - 회의-내 이행률 = ✅ 수 / 전체 carry-over 수
   - **2회+ 이월 항목 식별** → 다음 회차 §🚨 강사 사전 통지 1순위 자동 격상
10. **🚦 신호등 1단어 회고 결과** (PM only) ⭐ v2 신규:
    - 회의 종료 직전 강사/보조강사/팀장의 색 + 1단어 발화 추출
    - 같은 색이면 align ✅ / 다르면 내일 AM 1순위 자동 격상 + mom 기록
11. **인용할 핵심 발화** — 1~2개 (스냅샷에 인용 블록으로 직접 삽입)

**중요**: transcript는 음성 인식 오류 포함 (예: "PO" → "비야", "google-services" → "구글 서비스"). 추측이 큰 부분은 `?` 또는 `(추정)`으로 표기.

**회의 분량이 매우 짧음 (< 5분)** = 형식적 보고 우려. **§3-5 회의가 매우 짧을 때** 절차 적용.

### Step 5. AM/PM 스냅샷 갱신 (v2 양식 14개 섹션)

회의 **시간대(am/pm)**에 따라 v2 양식 적용:

- **AM**: [meeting_prep_template_v2.md §1 AM 양식](../../../teams-docs/.shared/meeting_prep_template_v2.md)
- **PM**: [meeting_prep_template_v2.md §2 PM 양식](../../../teams-docs/.shared/meeting_prep_template_v2.md)

이미 사전 체크리스트로 작성된 스냅샷이 있으면 **Write로 덮어쓰기** (회의 결과 추가 + 빈칸 채움). 없으면 새로 작성.

#### 5-1. 회의 결과 반영 (각 섹션 갱신 위치)

| v2 섹션 | 회의 후 갱신 내용 |
|---|---|
| 🔍 사전 점검 자동화 명령 결과 | (사전 작성 그대로 유지) |
| 🚨 즉시 조치 필요 | 회의에서 강사가 직접 발화한 즉시 조치 추가 (인용 블록) |
| 🚨 강사 사전 통지 의제 3건 | **각 항목 옆에 ✅ (다뤄짐) / ❌ (미언급) 마킹** ⭐ v2 핵심 |
| 🟢 한 줄 요약 | 회의 핵심 1문장 (사전 → 회의 후 갱신) |
| 📊 변화 (Finished / Will finish) | 회의에서 청취한 Will finish by when 채움 |
| 🎯 오늘 끝낼 수 있는 작업 3개 (AM only) | 회의 발화로 확정된 항목으로 갱신 |
| ✅ 오전 회의 액션 아이템 진척 (PM only) | 오전 합의 → 현재 상태 검증 |
| 🔁 Carry-over 자동 재출현 + 이행률 | **각 항목 ✅/❌ 마킹 + 이행률 N/M% 계산** ⭐ v2 핵심 |
| 🚦 R-항목 점검 | 회의 후 상태 갱신 (오전 → 오후 변화 강조 PM only) |
| 👤 인물별 활동 + 회의 발화 정합성 | **회의 발화 ✅/❌ 컬럼 채움 + R-quiet 자동 flag** |
| 📋 Issue↔PR 정합성 (1·2팀) | 회의 발화 정합 |
| 🎨 FigJam/Design 변화 | (사전 작성 그대로) |
| 💡 보조강사 권장 액션 3건 | 회의 후 push 못한 항목 별도 메모 |
| 🌙 내일 AM carry-over (PM only) | **미해결 + 새 발생 항목 자동 이월 + 이월 횟수 +1 + 2회+ 자동 🚨** ⭐ B2 |
| 🚦 오늘 신호등 1단어 회고 (PM only) | **강사/보조강사/팀장 색 + 1단어 transcript 발화에서 추출. 색 align 여부 명시** ⭐ v2 신규 |
| 🚧 Phase-gate Must Meet (3팀) | 회의에서 점검된 항목 충족률 갱신 |
| 📚 근거 인용 | 신규 R-항목 도입시만 |

#### 5-2. 회의 분량 + 정량 지표 기록 (양식 헤더)

v2 양식 헤더의 "회의 timebox 권장 / 실제 시간"에 **실제 회의 시간 (분:초)** 기록.
1주 후 회고 시 v1 baseline(5'21") 대비 변화 추적용.

### Step 6. 회의록 (mom) 작성 — 필요 시 (v2 트리거 확장)

다음 조건 중 **하나라도** 충족하면 별도 회의록 작성:

- 인원 변경 / PO·팀장 교체 / 부팀장 공백 결정
- R13/R-out/R-demo 등 즉시 에스컬레이션 R-항목 발현
- **R-attend (출결)** 신규 발현 — 결석·조퇴 사유 + 분담 합의 기록
- **R-burn 본격 발현** — 강사 명시 경고 또는 본인 "힘들다" 발언 시
- Decision Log 0건 상태에서 첫 결정 사항 발생
- **🚦 신호등 회고에서 강사·보조강사·팀장 색이 불일치 (다음 AM 1순위 자동 격상)** ⭐ v2 신규
- **carry-over 2회+ 이월 항목이 있는 경우** ⭐ v2 신규 (B2 — 회의 외 강사 1:1 통지 근거)
- 1팀의 경우: 보조강사가 강사에게 별도 통지 필요한 사안

경로: `teams-docs/<X>team/mom/<YYMMDD>_<am|pm>_minutes.md`

양식 골격 (v2 반영):
```markdown
# <팀명> 회의록 — <YYYY-MM-DD> <AM|PM>

> 일시: <시작> ~ <종료> (<N>분 <M>초)
> 참여: ...
> 원본: [meeting/<YYMMDD>_<am|pm>_transcript.txt](../meeting/...)
> 작성: 보조강사 (transcript 기반 정리)
> 양식: v2 ([meeting_prep_template_v2.md](../../.shared/meeting_prep_template_v2.md))

## 1. 진척 보고
## 2. 강사 지적 사항 (인용 포함)
## 3. 운영 체제 합의
## 4. 결정 사항 (Decision Log) — 표
## 5. 액션 아이템 — 표 (액션 / 담당 / 기한 / 검증)
## 6. 🚨 강사 사전 통지 의제 ✅/❌ 결과 (v2 신규)
   - 1. (1순위) → ✅/❌ + 이행 결과 1줄
   - 2. ... 3. ...
## 7. 🔁 Carry-over 이행 결과 (v2 신규)
   | 항목 | Owner | 회의에서 다뤘나? | 이월 횟수 | 다음 회차 |
## 8. 🚦 신호등 회고 결과 (PM only, v2 신규)
   - 강사: 🔴/🟡/🟢 + 1단어
   - 보조강사: 동일
   - 팀장/PO: 동일
   - **Align? ✅ / ❌ (다음 AM 1순위 자동 격상)**
## 9. 이월 항목 (다음 회차 §🚨 강사 사전 통지 후보)
## 10. 참조
```

### Step 7. 임시 파일 정리

```bash
# wav는 자동 삭제 (mkv가 원본이므로 wav는 재생성 가능)
rm -f teams-docs/<X>team/meeting/<YYMMDD>_<am|pm>.wav
```

mkv 원본 / transcript / 모델은 유지 (다음 회차 재사용 / 추적 자료).

### Step 8. 메모리 갱신 — 필요 시

회의에서 **다음 회차에도 적용될 패턴**을 발견하면 project 타입 메모리 저장:

- 새 인원 변경 / 역할 교체
- R-attend / R-burn 신규 발현 → 추후 회고 baseline
- 반복적 R-항목 발현 패턴 (예: 적응 멤버 본인 발언 0건)
- 보조강사 다음 회의 사전 통지 의제 후보
- **신호등 align 불일치 패턴** ⭐ v2 신규 (강사·보조강사 시선 차이 추적)

기존 메모리 (예: `team1_personnel_change_260512.md`, `team1_burn_attend_260514_am.md`)와 중복되면 **갱신**, 새 사안이면 **신규 파일** + `MEMORY.md` 인덱스 1줄 추가.

### Step 9. 사용자 보고 (v2 정량 지표 포함)

다음 골격으로 채팅 출력:

```markdown
회의 분석 + 문서 갱신 완료. 총 <시간> 소요.

## 회의 분석 핵심 (v2 정량 지표)
### <팀명> (<분량>)
- 🟢 ...
- 🚨 ...
- ❌ ...
- 🚨 강사 사전 통지 의제 이행: N/3 (%)
- 🔁 Carry-over 이행률: N/M (%)
- 🚦 신호등 회고: 강사 X / 보조강사 Y / 팀장 Z (align ✅/❌)
- 회의 시간: <분:초> (v1 baseline 5'21" 대비 N%)

## 생성·갱신된 문서
| 파일 | 내용 |
| ... | ... |

## 🚨 2회+ 이월 항목 (회의 외 강사 1:1 통지 권장 — B2)
- {항목 1}
- {항목 2}

## 다음 회차 강사 사전 통지 의제 후보 (1순위 자동 격상)
1. (회의 첫 안건 권유) {2회+ 이월 항목 또는 R-항목 critical}
2. {신규 발현 R}
3. {좋은 점 격려}
```

**all 모드**: 3개 팀 모두 분석 → 종합 보고 + 팀별 v2 정량 지표 (이행률 / 회의 시간 / 신호등) 비교 표.

### Step 10. 1주 운영 후 v2 효과 측정 (5/21 일요일 회고)

매주 일요일 v2 효과 측정 ([template_v1_v2_diff.md §6](../../../operation/docs/research/template_v1_v2_diff.md) 참조):

| 지표 | v1 baseline | 이번 주 측정값 | 목표 |
|---|---|---|---|
| 회의-내 carry-over 이행률 | 0% (1팀 0/7) | (transcribe 결과 누적) | ≥ 30% |
| 회의 평균 시간 | 5'21" (4회) | (transcribe 결과 누적) | ≥ 7'00" (AM) |
| 24h+ 정체 🚨 항목 | 1건 (R13) | (스냅샷 누적) | 0건 |
| 강사 사전 통지 이행률 | — | (transcribe 결과) | 100% |
| 신호등 align 비율 | — | (PM transcribe 누적) | ≥ 70% |

→ 5/21 회고 시 본 표 채워서 v3 결정 (timebox 강제 도입 / 무기명 폼 도입 여부).

---

## 3. 자주 발생하는 이슈 + 대처

### 3-1. Python 직접 호출 실패 (`exit 49`)

Windows에서 `python` 명령은 Microsoft Store stub일 수 있음. **본 스킬은 Python을 직접 호출하지 않으므로 무관**. 하위 도구가 Python을 요구하면 다음 경로:

```bash
"/c/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe"
```

### 3-2. Figma API JSON 파싱 실패

PM 스냅샷 §6 Figma 상태 채울 때 Python `json.load` 호출이 Windows store stub로 막힐 수 있음. 대안:

```bash
curl -s -H "X-Figma-Token: $TOKEN" "<API URL>" | grep -oE '"lastModified":"[^"]*"'
```

### 3-3. transcript 음성 인식 오류

medium 모델로도 다음 오류 빈번:
- 영문 약어 (PO, PR, FCM 등) → "비야", "PR", "FCM" 등 한글화
- 인명 — "정섭"/"정훈"/"성호" 등 비슷한 발음 혼동
- 외래어 ("Firebase" → "파이어베이스")

대처: 추측이 큰 부분은 `?` 또는 `(추정)` 표기. 이전 회차 transcript와 비교하면 정확도↑.

### 3-4. 모델 다운로드 중단

curl 다운로드는 resume 옵션 사용 가능:
```bash
curl -L -C - --progress-bar -o "...ggml-medium.bin" "..."
```

크기가 ~1.5GB 이지만 실제 파일 크기는 `1533763059` bytes. 검증:
```bash
size=$(stat -c%s "...ggml-medium.bin")
[ "$size" -lt 1500000000 ] && echo "❌ 다운로드 불완전"
```

### 3-5. 회의가 매우 짧음 (~5분 미만)

transcript 길이 < 1KB이면 형식적 진척 보고 가능성. PM 스냅샷의 **합의 사항 / 액션 아이템 / R-항목 발현이 거의 비어있다는 사실 자체**를 다음 회의 의제 후보로 기록.

→ 메모리 [team_check_meeting_insights_260513.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team_check_meeting_insights_260513.md) 의 "공통 패턴 — 회의 자체의 한계" 참조.

### 3-6. mkv 디스크 공간 누적

3팀 일 2회 × 6주 미팅 = 평균 200MB × 60건 = ~12GB 누적. 다음 정책 권장:
- Week 종료 시 (매주 일요일) 해당 주 mkv 일괄 zip 압축 또는 삭제
- transcript .txt는 영구 보관 (~수 KB)
- 데모 영상은 별도 분리 (`teams-docs/<X>team/demo/`)

---

## 4. 출력 끝 표준 멘트

```
---
✅ 변환 완료:
  - 1팀: meeting/<YYMMDD>_<am|pm>_transcript.txt (<N>줄)
  - 2팀: ...
  - 3팀: ...

✅ 스냅샷 갱신: snapshots/<YYMMDD>_<am|pm>.md
✅ 회의록 (생성된 경우): mom/<YYMMDD>_<am|pm>_minutes.md
✅ 메모리 (생성된 경우): memory/<file>.md

다음 회차: 내일 09:30 `/team-check-am <팀번호>` 호출 시 본 스냅샷이 비교 기준.
```

---

## 5. 워크플로우 변형 — 부분 실행

### 5-1. transcript만 생성 (분석 ❌)

```
/team-meeting-transcribe 1 --transcribe-only
```

→ Step 1~3, 7만 실행. 사용자가 직접 분석할 때.

### 5-2. 기존 transcript로 갱신만 (변환 ❌)

```
/team-meeting-transcribe 1 --analyze-only
```

→ Step 4~9만 실행. 사용자가 외부 도구로 변환한 .txt 가 이미 있을 때.

(현재 인자 파싱은 기본 모드만 구현 — 옵션 플래그는 추가 작업 시 도입)

---

## 6. 도구 경로 변수 (한 곳에서 관리)

향후 환경 변경 시 본 섹션만 갱신:

```bash
BUZZ_INTERNAL="/c/Program Files (x86)/Buzz/_internal"
FFMPEG="$BUZZ_INTERNAL/ffmpeg.exe"
FFPROBE="$BUZZ_INTERNAL/ffprobe.exe"
WHISPER="$BUZZ_INTERNAL/buzz/whisper_cpp/whisper-cli.exe"
MODEL_DIR="/c/Users/ibebu/AppData/Local/Buzz/Buzz/Cache/models"
DEFAULT_MODEL="$MODEL_DIR/ggml-medium.bin"
DEFAULT_MODEL_URL="https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin"
DEFAULT_LANG="ko"
DEFAULT_THREADS=4
```

---

## 7. 참조

### 자매 SKILL
- [team-check-am SKILL](../team-check-am/SKILL.md) — AM 회의 사전 준비 (본 스킬은 회의 종료 후 진행)
- [team-check-pm SKILL](../team-check-pm/SKILL.md) — PM 회의 사전 준비

### 양식 / 점검 항목 (v2)
- [.shared/meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) — **현재 적용 양식** (2026-05-14부)
- [.shared/meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md) — v1 참조용
- [.shared/risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md) — R-항목 분류 (R-attend·R-burn 표준 포함)
- [.shared/daily_check_method.md](../../../teams-docs/.shared/daily_check_method.md) — 점검 방법론
- [.shared/premortem_template.md](../../../teams-docs/.shared/premortem_template.md) — 주1회 사전 부검

### 사내 가이드
- [final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)
- [ta-guides/애자일_예제기반_FAQ.md](../../../ta-guides/애자일_예제기반_FAQ.md) — §9 안티패턴 식별표

### v2 설계 근거
- [operation/docs/research/external_practices.md](../../../operation/docs/research/external_practices.md)
- [operation/docs/research/current_pattern_gap.md](../../../operation/docs/research/current_pattern_gap.md)
- [operation/docs/research/v2_simulation_results.md](../../../operation/docs/research/v2_simulation_results.md)
- [operation/docs/research/template_v1_v2_diff.md](../../../operation/docs/research/template_v1_v2_diff.md)
- [operation/docs/meeting_template_v2_quick_apply.md](../../../operation/docs/meeting_template_v2_quick_apply.md)

### 메모리
- [team_check_meeting_insights_260513.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team_check_meeting_insights_260513.md) — 본 스킬 적용 1회차 결과 + v2 supersede
- [team1_burn_attend_260514_am.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_burn_attend_260514_am.md) — R-attend·R-burn 발현 baseline

### 외부 도구
- [Buzz GitHub](https://github.com/chidiwilliams/buzz)
- [whisper.cpp](https://github.com/ggerganov/whisper.cpp)
- [Huggingface 모델](https://huggingface.co/ggerganov/whisper.cpp)

---

## 8. 변경 이력

| 날짜 | 변경 |
|---|---|
| 2026-05-13 | 초안 작성 — Buzz 1.4.4 + ggml-medium 한국어 워크플로우 검증 (1·2·3팀 PM 회의 변환 1회차) |
| 2026-05-14 | **v2 양식 적용** — Step 4 분석 7→10 항목 (강사 사전 통지 ✅/❌ + carry-over 이행률 + 신호등 회고 추가) / Step 5 v2 14개 섹션 매핑 / Step 6 mom 트리거 확장 (R-attend·R-burn·신호등 불일치·carry-over 2회+) / Step 9 v2 정량 지표 보고 / Step 10 1주 회고 측정 표 추가 |
