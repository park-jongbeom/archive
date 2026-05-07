# 진행 점검 자동화 — 1주차 OT 공지문 / 동의 양식

> 보조강사가 1주차 OT에서 학생들에게 다음을 공지하고 동의를 받는다. 그대로 슬라이드/노션 공지로 옮겨도 됨.

---

## 공지 (학생에게 그대로 전달)

> **6주 동안 보조강사가 각 팀의 진행 상황을 주 1회 점검합니다.**
>
> ### 무엇을 보나요?
> 1. **GitHub 레포의 Issue / PR 메타데이터** — 자동 수집 (제목, 라벨, AC 체크 개수, PR 머지 시점, 1차 리뷰 시점 등)
> 2. **각 팀이 매주 작성하는 "Weekly Status" Issue** — 자기 점검 6필드 (Brief 작성 여부 / MVP Must·Should·Won't 개수 / Flow 수 / Decision Log 변화 / Demo Scenario / 한 줄 요약)
> 3. **노션 산출물** — 강사가 매주 일요일 직접 페이지를 PDF로 인쇄해서 강사 로컬에 보관 (학생 추가 작업 없음)
>
> ### 무엇을 보지 않나요?
> - 개인 메시지 / DM / 카카오톡 / 사적 대화
> - 학생 개인 정보 (이메일, 전화번호 등)
> - GitHub의 비공개 코멘트 / 강사 권한 외 영역
>
> ### 왜 보나요?
> 가이드 기준(Must/Should/Won't, AC 명확성, Flow 개수, 24h 리뷰 SLA 등) 충족 여부를 점검해서, **1:1 멘토링에서 어떤 팀에 어떤 항목을 짚을지** 판단하기 위함입니다. 평가/채점 도구가 아닙니다.
>
> ### 어디에 저장되나요?
> - 강사 로컬 git 레포 (외부 비공개)
> - 6주 종료 후 raw 데이터 삭제. 익명화된 회고 리포트만 유지 가능.
>
> ### 동의하지 않으려면?
> **1:1로 보조강사에게 알려주세요.** 해당 팀은 GitHub 자동 수집 제외 + Status Issue 미작성 가능. 불이익 없습니다.

---

## 학생 액션 (1주차 OT 후 1일 이내)

각 팀 PO가 진행:

### 1. GitHub 레포에 강사 초대 (Read 권한)
팀 레포 → Settings → Collaborators → **Read 권한**으로 강사 GitHub 핸들 추가

### 2. Weekly Status Issue Template 적용
강사가 별도로 전달하는 [`templates/weekly-status.md`](../templates/weekly-status.md) 파일을 팀 레포의 **`.github/ISSUE_TEMPLATE/weekly-status.md`** 경로에 복사 + 커밋
- 자세한 절차: [`weekly_status_setup.md`](weekly_status_setup.md)

### 3. `teams.yaml` 등록 정보 제출
다음을 강사에게 (팀 리드/PO 메시지로) 전달:

```
팀명: 1팀
인원: 4명
PO: 김OO
GitHub 레포 URL: https://github.com/.../...
```

> 노션 페이지 ID는 이번에는 제공할 필요 없습니다. (회사 정책상 API 접근이 차단되어 강사가 PDF로 직접 추출합니다.)

---

## 학생 매주 액션 (Week 1 시작 ~ Week 6)

매주 **일요일 23:59까지** 팀 PO가 GitHub에 **Weekly Status Issue 1개** 작성:
- 제목: `[Status] Week N - 팀명`
- 라벨: `weekly-status`
- 본문: 6필드 자기 점검 (Template 그대로)
- 5분이면 완료

상세: [`weekly_status_setup.md`](weekly_status_setup.md)

---

## 강사 측 약속

- 데이터를 외부에 공유하지 않음 (부트캠프 운영진 보고용 **익명 통계** 제외)
- 학생 GitHub은 **읽기만** (Read 권한). 수정/머지 권한 없음
- 노션은 **강사 본인이 직접 PDF로 추출**. 학생에게 추가 작업 부담 없음
- 점검 결과는 **학생 평가 자료가 아님**. 강사가 어디에 개입할지 판단하는 자료
- 학생이 동의 철회 시 즉시 해당 팀 데이터 폐기

---

## 6주 종료 시 절차

1. 강사가 모든 팀에 공지: *"수집 종료, 데이터 폐기"*
2. 강사가 GitHub 레포에서 본인 협업자 제거 요청
3. 강사가 `snapshots/` 폴더 삭제 (또는 익명화 후 회고 보관)
4. (선택) `weekly-status` 라벨은 학생 팀이 자체 회고용으로 유지 가능
