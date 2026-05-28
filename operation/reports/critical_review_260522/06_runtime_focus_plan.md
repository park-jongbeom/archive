# 화면 동작 / 크래시 위주 재점검 계획 (2026-05-22 추가)

## 관점 전환

이전 점검(04_team{N}_findings.md, 05_final_report.md)은 **보안/구조/품질** 관점.
이번 재점검은 **강사진이 빌드해서 실 단말로 누를 때 만나는 크래시/UI 무반응/오작동** 관점.

→ 보안 이슈(API key 평문, App Check, rules)는 사용자 체감 버그가 아니므로 제외.

## 점검 영역 (사용자 체감 우선)

1. **즉시 크래시 (Force Close)** — NPE / NotImplementedError / SecurityException / unhandled exception
2. **TODO 호출 경로** — 버튼 누르면 throw 되는 미구현 함수
3. **권한 거부 dead-end** — 거부 후 다음 화면 못 가거나 무반응
4. **외부 호출 실패 UI 처리** — Functions/Firestore/Network 실패 시 무한 로딩/빈 화면
5. **네비게이션 누락** — start destination 누락, route 정의와 실제 화면 불일치
6. **ViewModel init throw** — 화면 진입 즉시 크래시
7. **Hilt 주입 실패** — `@Inject` 누락 → 런타임 ClassCastException 또는 다이얼로그 띄우기 실패
8. **잘못된 더미 / placeholder** — `jsonplaceholder.typicode.com`, 하드코딩 UID
9. **빈 데이터 / null 처리 누락** — Firestore 빈 컬렉션 첫 진입 시 NPE
10. **백그라운드 → 포어그라운드 복귀 시** 리스너 재구독 누락

## 산출물

- `07_team1_runtime.md`, `07_team2_runtime.md`, `07_team3_runtime.md`
- 통합: `08_runtime_summary.md`

각 보고서는 **시연 시나리오 기반** 정리: "사용자가 X 버튼 누르면 → Y 발생"
