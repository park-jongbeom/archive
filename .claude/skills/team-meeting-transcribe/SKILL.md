---
name: team-meeting-transcribe
description: "각 팀 미팅 녹음(.mkv/.mp4/.wav 등)을 Buzz 번들 whisper로 transcribe 후, 회의 결과를 PM 스냅샷에 반영하고 1팀은 회의록(mom)까지 작성. 사용법: /team-meeting-transcribe <팀번호|all> [날짜 YYMMDD] [시간대 am|pm]. 보조강사 관점."
effort: medium
---

# team-meeting-transcribe — 회의 녹음 변환 + 점검 문서 갱신

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

### Step 4. transcript 읽기 + 회의 분석

생성된 `<YYMMDD>_<am|pm>_transcript.txt` 를 Read 도구로 읽고, 다음 7가지를 추출:

1. **참여자 식별** — 이름·역할 (transcript는 화자 분리 안 됨 → 발화 내용으로 추정)
2. **진척 보고** — 각 멤버 작업 영역 + Must 매핑
3. **강사 지적 사항** — "왜", "이렇게 하셔야죠", "빼주세요" 등 지시어
4. **합의 사항** — "알겠습니다", "그렇게 하겠습니다", "당분간 ~ 체제" 등 확정 표현
5. **R-항목 발현** — google-services, 권한 이슈, 패키지 리팩토링, 부담 신호, 발언 0건 등
6. **미합의 / 이월 항목** — 사전 체크리스트에 있었으나 회의에서 다뤄지지 않은 의제
7. **인용할 핵심 발화** — 1~2개 (PM 스냅샷에 인용 블록으로 직접 삽입)

**중요**: transcript는 음성 인식 오류 포함 (예: "PO" → "비야", "google-services" → "구글 서비스"). 추측이 큰 부분은 `?` 또는 `(추정)`으로 표기.

### Step 5. PM/AM 스냅샷 갱신

회의 **시간대(am/pm)**에 따라 다른 양식 적용:

- **PM**: [.shared/meeting_prep_template.md §2 PM 양식](../../../teams-docs/.shared/meeting_prep_template.md) 기반 + 회의 결과 반영
- **AM**: [.shared/meeting_prep_template.md §1 AM 양식](../../../teams-docs/.shared/meeting_prep_template.md) 기반

이미 사전 체크리스트로 작성된 스냅샷이 있으면 **Write로 덮어쓰기** (회의 결과 추가 + 빈칸 채움). 없으면 새로 작성.

핵심 섹션:

| 섹션 | 내용 |
|---|---|
| 🟢 한 줄 요약 | 회의 핵심 1문장 |
| 🔴 즉시 조치 항목 | 강사가 직접 지시한 R13/R-out 등 (인용 블록 포함) |
| 📊 시간 변화 | 사전 수집된 git/Figma 데이터 |
| ✅ 회의 발화에서 확인된 작업 분담 | 표 형식 |
| 1. 사전 Top 5 점검 결과 | ✅/❌ + 회의 메모 |
| 4. R-항목 점검 결과 | 회의 후 상태 갱신 |
| 5. 인물별 활동 | commit + 회의 발화 + 신호 |
| 7. 합의 사항 | 번호 매김 |
| 8. 액션 아이템 | 표 (액션/담당/기한/검증) |
| 9. 다음 회차 carry-over | 미합의·이월 항목 |
| 10. 참조 | transcript 링크 포함 |

### Step 6. 회의록 (mom) 작성 — 필요 시

다음 조건 중 **하나라도** 충족하면 별도 회의록 작성:

- 인원 변경 / PO·팀장 교체 / 부팀장 공백 결정
- R13/R-out/R-demo 등 즉시 에스컬레이션 R-항목 발현
- Decision Log 0건 상태에서 첫 결정 사항 발생
- 1팀의 경우: 보조강사가 강사에게 별도 통지 필요한 사안

경로: `teams-docs/<X>team/mom/<YYMMDD>_<am|pm>_minutes.md`

양식 골격:
```markdown
# <팀명> 회의록 — <YYYY-MM-DD> <AM|PM>

> 일시: ...
> 참여: ...
> 원본: [meeting/<YYMMDD>_<am|pm>_transcript.txt](../meeting/...)
> 작성: 보조강사 (transcript 기반 정리)

## 1. 진척 보고
## 2. 강사 지적 사항 (인용 포함)
## 3. 운영 체제 합의
## 4. 결정 사항 (Decision Log) — 표
## 5. 액션 아이템 — 표
## 6. 이월 항목
## 7. 참조
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
- 반복적 R-항목 발현 패턴 (예: 적응 멤버 본인 발언 0건)
- 보조강사 다음 회의 사전 통지 의제 후보

기존 메모리 (예: `team1_personnel_change_260512.md`)와 중복되면 **갱신**, 새 사안이면 **신규 파일** + `MEMORY.md` 인덱스 1줄 추가.

### Step 9. 사용자 보고

다음 골격으로 채팅 출력:

```markdown
회의 분석 + 문서 갱신 완료. 총 <시간> 소요.

## 회의 분석 핵심
### <팀명> (<분량>)
- 🟢 ...
- 🚨 ...
- ❌ ...

## 생성·갱신된 문서
| 파일 | 내용 |
| ... | ... |

## 다음 AM 강사 사전 통지 의제 (1줄)
> "..."
```

**all 모드**: 3개 팀 모두 분석 → 종합 보고 + 팀별 핵심 의제 3건씩.

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

- [team-check-am SKILL](../team-check-am/SKILL.md) — AM 회의 사전 준비 (본 스킬은 회의 종료 후 진행)
- [team-check-pm SKILL](../team-check-pm/SKILL.md) — PM 회의 사전 준비
- [.shared/meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md) — AM/PM 양식
- [.shared/risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md) — R-항목 분류
- 외부 도구:
  - [Buzz GitHub](https://github.com/chidiwilliams/buzz)
  - [whisper.cpp](https://github.com/ggerganov/whisper.cpp)
  - [Huggingface 모델](https://huggingface.co/ggerganov/whisper.cpp)
- 본 스킬 적용 1회차 결과 (2026-05-13): [memory/team_check_meeting_insights_260513.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team_check_meeting_insights_260513.md)

---

## 8. 변경 이력

| 날짜 | 변경 |
|---|---|
| 2026-05-13 | 초안 작성 — Buzz 1.4.4 + ggml-medium 한국어 워크플로우 검증 (1·2·3팀 PM 회의 변환 1회차) |
