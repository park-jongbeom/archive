# Tech Overview

> 목표: “상세 명세”가 아니라 팀의 기술 선택/정책을 1~2페이지로 공유하기 위한 개요 문서입니다.

## 아키텍처
- (예: Client(Android) / Server(BaaS or API) 구조)
- (예: Presentation: MVVM/MVI, Layer: 2-layer 또는 Clean 3-layer)

## 상태 관리
- (예: ViewModel + UiState(StateFlow) / 단방향 데이터 흐름)

## 데이터 흐름
- UI → UseCase/Repository → DataSource/API → Domain/State → UI

## 주요 정책
- 인증/세션 정책(예: 로그인 유지 여부, 재로그인 조건)
- 에러/재시도 정책(예: 공통 에러 메시지, Retry UX)
- Empty 정책(예: 0개 목록은 Empty 필수)

## 의존성/도구(선택)
- DI: (예: Hilt)
- Network: (예: Retrofit/OkHttp)
- Image: (예: Coil)

## 관련 링크(선택)
- Decision Log: (링크)
- Demo Scenario: (링크)
