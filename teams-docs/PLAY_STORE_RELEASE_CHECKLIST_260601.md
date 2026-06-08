# Google Play 1차 배포 사전 점검 체크리스트 — 코호트 공통

> **작성**: 2026-06-01 / 보조강사
> **적용**: 1·2·3팀 공통
> **근거**: Google Play / Firebase / Android Developer / 카카오 디벨로퍼스 공식 문서

---

## 0. 개요

**시연 통과 ≠ 배포 통과.** 시연 빌드는 debug.keystore + 테스트 모드 Firestore + 로컬 환경에 맞춰져 있어, 그대로 Play Store에 올리면 (1) 보안 사고 (2) 핵심 기능 작동 정지 (3) 심사 거부 중 하나 이상이 발생합니다. 본 문서는 1차 배포 전 **반드시** 확인해야 하는 13개 항목을 정리합니다.

### Severity 정의

| 표기 | 의미 | 미조치 시 결과 |
|---|---|---|
| 🔴 | **즉시 fix** | 배포 즉시 보안 사고 / 핵심 기능 작동 불가 |
| 🟠 | **출시 전 필수** | Google 심사 거부 / 정책 위반 통보 |
| 🟡 | **검수 권장** | Pre-launch Report 경고 / 평가 감점 / 출시 후 1차 개선 필수 |
| 🟢 | **카카오 로그인 특화** | 카카오 측 검수 미통과 시 등록 사용자 외 로그인 불가 |

---

## 1. 항목별 상세

### [🔴-01] Firestore Security Rules 배포

**왜 블로커**: Rules 미배포 상태는 둘 중 하나 — (a) prod 모드라면 모든 read/write 차단(앱 사망), (b) test 모드라면 30일 후 차단되거나 그 전까지 **무방비 = 누구나 모든 사용자 데이터 read/write 가능**. 후자는 개인정보 유출 사고로 직결되며 Play Store 정책의 "User Data" 항목 위반.

**현재 코호트 상태**:
- 5/22 코드 리뷰 결과 **코호트 100% 부재**
- 3팀: [firebase.json](3team/repo/firebase.json) 에 `firestore` 키 자체 없음 → Rules 미배포 확정
- 1·2팀: 6/1 시점 재확인 필요

**조치**:
1. `firestore.rules` 파일 생성 (인증 사용자만 본인 문서 접근 가능 등)
2. `firebase.json`의 `firestore` 키 추가 — `{"firestore": {"rules": "firestore.rules"}}`
3. `firebase deploy --only firestore:rules` 배포
4. Firebase Console → Firestore → Rules 탭에서 적용 확인

**검증**: Firebase Console Rules Playground에서 인증 안 된 요청이 차단되는지 시뮬레이션

**공식 문서**:
- [Firestore 보안 규칙 시작하기](https://firebase.google.com/docs/firestore/security/get-started?hl=ko)
- [규칙 작성 가이드](https://firebase.google.com/docs/firestore/security/rules-structure?hl=ko)
- [Firebase CLI를 통한 규칙 배포](https://firebase.google.com/docs/rules/manage-deploy?hl=ko)
- [Google Play 사용자 데이터 정책](https://support.google.com/googleplay/android-developer/answer/10144311?hl=ko)

---

### [🔴-02] Cloud Functions 배포

**왜 블로커**: 클라이언트가 `httpsCallable` 등으로 함수를 호출하는데 함수가 배포돼 있지 않으면 release 빌드에서 해당 기능 전체 실패.

**현재 코호트 상태**:
- 3팀: [functions/index.js](3team/repo/functions/index.js) export 0건 (boilerplate `setGlobalOptions`만 존재). 5/27 메모리상 **Cloud Function 호출 코드 9건 발견** — 전부 release에서 실패 예정 (PTT 무전 포함)
- 1·2팀: 함수 사용 여부 + 배포 상태 확인 필요

**조치**:
1. 클라이언트에서 호출하는 함수 목록 정리 (`grep -rn "httpsCallable\|onCall\|firebase.functions()"`)
2. 각 함수를 `functions/index.js` (또는 별도 파일)에 `exports.functionName = onCall(...)` 형식으로 작성
3. `cd functions && npm install` 의존성 설치
4. `firebase deploy --only functions` 배포 (region 명시 권장 — 클라이언트 region과 일치해야 호출 가능, 예: `asia-northeast3`)
5. Firebase Console → Functions에서 배포 확인

**검증**: Firebase Console → Functions 로그에서 호출 시 실행 기록 확인

**공식 문서**:
- [Cloud Functions for Firebase 시작하기](https://firebase.google.com/docs/functions/get-started?hl=ko)
- [함수 호출 (Callable Functions)](https://firebase.google.com/docs/functions/callable?hl=ko)
- [함수 배포·관리](https://firebase.google.com/docs/functions/manage-functions?hl=ko)
- [리전 선택](https://firebase.google.com/docs/functions/locations?hl=ko)

---

### [🔴-03] Release Keystore SHA-1 / SHA-256 Firebase 등록

**왜 블로커**: google-services.json에 등록된 SHA fingerprint는 **그 SHA로 서명된 빌드에서만** Google/Firebase 인증 작동. 현재 google-services.json은 debug.keystore SHA만 등록 — release keystore로 서명한 빌드는 **구글 로그인 100% 실패** ("구글 계정을 불러올 수 없습니다" 토스트).

**현재 코호트 상태**:
- 3팀: SHA1 5개 모두 debug.keystore 형식 추정. Release keystore 자체 미생성 가능성
- 1·2팀: release keystore 보유 여부 + Firebase 등록 여부 확인 필요

**조치**:
1. Release keystore 생성 (한 번만): `keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 25000`
   - ⚠️ **분실 시 영구 복구 불가** — 안전한 곳(예: 1Password, GitHub Secrets, 팀 공유 vault)에 백업
2. Release SHA-1·SHA-256 추출: `keytool -list -v -keystore release.keystore -alias release`
3. (Play App Signing 사용 시) Play Console → 앱 → Setup → App signing 에서 **Google이 관리하는 Upload key + App signing key** SHA 값 모두 확보
4. Firebase Console → 프로젝트 설정 → Android 앱 → "지문 추가" 에 SHA-1·SHA-256 모두 등록
5. `google-services.json` 재다운로드 → `app/google-services.json` 교체 → release 빌드 재생성

**검증**: Release APK·AAB로 직접 설치 후 구글/카카오 로그인 동작 확인

**공식 문서**:
- [Android 앱 서명](https://developer.android.com/studio/publish/app-signing?hl=ko)
- [Firebase에 SHA 인증서 추가](https://firebase.google.com/docs/android/setup?hl=ko#add-config-file)
- [Play App Signing 설정](https://support.google.com/googleplay/android-developer/answer/9842756?hl=ko)
- [OAuth 2.0 client SHA 등록](https://developers.google.com/android/guides/client-auth?hl=ko)

---

### [🔴-04] API Key Restrictions

**왜 블로커**: google-services.json의 web API key는 평문으로 APK에 포함됨 → APK 디컴파일하면 누구나 추출 가능. 제한이 없으면 **타인이 본인 도메인에서 호출하여 Firebase 무료 quota 소진 / 과금 폭탄** 유발 가능.

**현재 코호트 상태**:
- 5/22 코드 리뷰: 2팀 평문 API key 발견. 1·3팀도 동일 패턴 의심
- 3팀: google-services.json의 `current_key` = `AIzaSyAxI4...` (정상 — 단 restrictions 필수)

**조치**:
1. Google Cloud Console → APIs & Services → Credentials → Android API Key 선택
2. **Application restrictions** → "Android apps" → package name + SHA-1 등록 (debug + release 둘 다)
3. **API restrictions** → 실제 사용하는 API만 화이트리스트 (Firebase Auth, Firestore, Maps SDK 등)
4. 지도 키 (3팀 `MAP_KEY`) 별도 등록 필요
5. 카카오 키도 카카오 디벨로퍼스 콘솔에서 동일한 SHA·package 제한

**검증**: 제한 적용 후 release 빌드에서 정상 동작하는지 확인 (제한이 너무 좁으면 정상 호출도 거부됨)

**공식 문서**:
- [API key 관리 (Cloud)](https://cloud.google.com/docs/authentication/api-keys)
- [Firebase 프로젝트 API key 보안](https://firebase.google.com/docs/projects/api-keys?hl=ko)
- [Maps SDK API key 제한](https://developers.google.com/maps/api-security-best-practices?hl=ko)

---

### [🟠-05] Background Location 특별 심사

**왜 블로커**: `ACCESS_BACKGROUND_LOCATION` 권한을 사용하면 Google Play가 **별도 정책 심사**를 진행. 핵심 기능 정당화 + in-app prominent disclosure + 영상 데모 제출 필요. 통상 **2~4주** 소요. 미통과 시 앱 등록 자체가 거부됨.

**현재 코호트 상태**:
- 1팀: [1team/repo](1team/) 에 `review_background_location.md` 보유 → 이미 인지
- 3팀: BackgroundListenerService 존재 → 해당 의심
- 2팀: 위치 백그라운드 사용 여부 확인 필요

**조치**:
1. 백그라운드 위치가 **핵심 기능에 정말 필요한지** 재검토 — 포그라운드만으로 가능하면 권한 제거가 가장 빠른 길
2. 필요한 경우:
   - Play Console → App content → Sensitive permissions and APIs → Location 섹션 작성
   - **prominent disclosure** UI 추가 (앱 첫 진입 시 "백그라운드에서 위치를 사용하는 이유" 명시)
   - 데모 영상 (1~3분) 제작 — 백그라운드 위치 활용 시나리오 시연
3. Play Console에서 신청 → 심사 대기

**검증**: Play Console → Policy → App content 항목에서 ✅ 표기

**공식 문서**:
- [위치 권한 정책](https://support.google.com/googleplay/android-developer/answer/9799150?hl=ko)
- [백그라운드 위치 권한 요청](https://developer.android.com/develop/sensors-and-location/location/permissions/background?hl=ko)
- [중요 권한·API에 대한 선언](https://support.google.com/googleplay/android-developer/answer/9214102?hl=ko)
- [Prominent disclosure 가이드](https://support.google.com/googleplay/android-developer/answer/11150561?hl=ko)

---

### [🟠-06] Privacy Policy URL 외부 호스팅

**왜 블로커**: Play Console 등록 시 **공개 URL 형태의 개인정보처리방침** 필수. 앱 내 텍스트만으로는 불가 — Google이 크롤링 가능한 URL이어야 함.

**현재 코호트 상태**:
- 코호트 전체: 앱 내 약관 HTML은 있지만 외부 URL 호스팅은 미확인
- 3팀: SignUp 약관 → 서버에서 fetch 하는 구조 (URL이 외부 공개되어 있는지 확인 필요)

**조치**:
1. 무료 호스팅 옵션 (5분 작업):
   - **GitHub Pages**: 리포 `docs/privacy.html` → Pages 활성화 → `https://{user}.github.io/{repo}/privacy.html`
   - **Notion 공개 페이지**: 약관 페이지 → "Share to web" → 공개 URL
   - **Google Sites**: 무료 사이트 빌더
2. URL을 Play Console → Store presence → Main store listing → Privacy policy URL 에 등록
3. **추가 권장**: 앱 내 설정 화면에서도 동일 URL 링크 제공 (`Intent.ACTION_VIEW`)

**검증**: 시크릿 브라우저에서 URL 접근 가능 + 한국어로 작성됨 + 본 앱 명시

**공식 문서**:
- [개인정보처리방침 정책](https://support.google.com/googleplay/android-developer/answer/13316080?hl=ko)
- [Play Console에서 개인정보처리방침 추가](https://support.google.com/googleplay/android-developer/answer/9859455?hl=ko)
- [개인정보보호위원회 표준 양식](https://www.pipc.go.kr/np/cop/bbs/selectBoardArticle.do?bbsId=BS217&mCode=D050030000)

---

### [🟠-07] Data Safety Form 작성

**왜 블로커**: Play Console 등록 시 **데이터 수집·공유 명세** 필수 입력. 위치·마이크·연락처·메시지·계정 등 본 앱이 수집하는 데이터를 정확히 신고해야 함. **거짓 신고 시 앱 정지 + 개발자 계정 페널티**.

**현재 코호트 상태**:
- 코호트 전체: Data Safety Form 작성 미확인
- 본 코호트 앱이 수집할 가능성 높은 항목: 위치(정밀), 음성 녹음(무전), 이메일·이름·프로필 사진(인증), 메시지(DM), 연락처(친구)

**조치**:
1. Play Console → App content → Data safety
2. 아래 카테고리 빠짐없이 신고:
   - **Personal info**: 이름, 이메일, 사용자 ID
   - **Location**: 정밀 위치 (앱 핵심)
   - **Messages**: DM (앱 내 통신)
   - **Audio**: 음성 녹음 (무전 PTT)
   - **Photos**: 프로필 사진
3. 각 항목별로 (a) 수집 여부 (b) 공유 여부 (c) 선택/필수 (d) 사용 목적 (e) 암호화 여부 (f) 사용자 삭제 요청 가능 여부 명시
4. **계정 삭제 기능** 필수 (Google 정책 강화 — 앱 내 + 웹 양쪽에서 가능해야 함, 2023년 12월부터)

**검증**: Play Console에서 "Form complete" 표기

**공식 문서**:
- [Data Safety Form 가이드](https://support.google.com/googleplay/android-developer/answer/10787469?hl=ko)
- [데이터 안전성 섹션 정확성](https://support.google.com/googleplay/android-developer/answer/13327091?hl=ko)
- [계정 삭제 요구사항](https://support.google.com/googleplay/android-developer/answer/13327826?hl=ko)
- [Android 데이터 수집·공유 가이드](https://developer.android.com/guide/practices/privacy?hl=ko)

---

### [🟠-08] Target SDK API Level 적합성

**왜 블로커**: Google Play는 매년 신규 앱·업데이트의 최소 targetSdk 요구를 1단계씩 올림. 2026년 6월 현재 기준 신규 앱은 **API 35 (Android 15)** 이상 필수. 미충족 시 업로드 거부.

**현재 코호트 상태**:
- 각 팀 `app/build.gradle.kts`의 `targetSdk` 값 직접 확인 필요
- 본 코호트 시작 시점 기준 API 34가 권장이었으나 1차 배포 시점에는 API 35 강제

**조치**:
1. `app/build.gradle.kts` 에서 `targetSdk = 35` (또는 그 이상) 설정
2. `compileSdk`도 같이 35+로 변경
3. API 변경점 확인:
   - API 34: 사진/동영상 선택자 (Photo Picker), foreground service type 명시 의무화
   - API 35: edge-to-edge enforcement (앱이 시스템 bar 영역까지 그려야 함)
4. release 빌드로 전 화면 회귀 테스트 (특히 상하단 inset 처리)

**검증**: `./gradlew :app:bundleRelease` 통과 + 전 화면 시각 검증

**공식 문서**:
- [Target API level requirements (공식)](https://developer.android.com/google/play/requirements/target-sdk?hl=ko)
- [Play Console target API 정책](https://support.google.com/googleplay/android-developer/answer/11926878?hl=ko)
- [Android 15 (API 35) 동작 변경 사항](https://developer.android.com/about/versions/15/behavior-changes-15?hl=ko)
- [Android 14 (API 34) 동작 변경 사항](https://developer.android.com/about/versions/14/behavior-changes-14?hl=ko)

---

### [🟠-09] App Bundle (.aab) + Play App Signing

**왜 블로커**: Play Store는 2021년 8월부터 **신규 앱은 .aab 형식 의무**. APK 업로드는 불가. 또한 Play App Signing 설정 시 Google이 서명 키를 관리하므로 키 분실 리스크 방지.

**현재 코호트 상태**:
- 각 팀 `./gradlew :app:bundleRelease` 실행 가능 여부 확인 필요
- release signing config가 `build.gradle.kts`에 명시되어 있는지 확인

**조치**:
1. `app/build.gradle.kts` 에 release signingConfig 설정:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release.keystore")
           storePassword = System.getenv("KEYSTORE_PASSWORD")
           keyAlias = System.getenv("KEY_ALIAS")
           keyPassword = System.getenv("KEY_PASSWORD")
       }
   }
   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           // ...
       }
   }
   ```
   - 비밀번호 등은 **gradle.properties 또는 환경 변수로 분리** (절대 git commit X)
2. `./gradlew :app:bundleRelease` 빌드 → `app/build/outputs/bundle/release/app-release.aab` 생성
3. Play Console → 새 앱 만들기 → Internal testing 트랙에 .aab 업로드
4. Play App Signing 활성화 — Google이 app signing key 관리, 개발자는 upload key만 보관

**검증**: Internal testing 링크로 본인 디바이스 설치 후 로그인 등 핵심 동작 확인

**공식 문서**:
- [Android App Bundle 개요](https://developer.android.com/guide/app-bundle?hl=ko)
- [Play App Signing 사용](https://support.google.com/googleplay/android-developer/answer/9842756?hl=ko)
- [앱 서명 구성 (Gradle)](https://developer.android.com/studio/publish/app-signing?hl=ko#sign-apk)
- [internal testing 트랙 가이드](https://support.google.com/googleplay/android-developer/answer/9845334?hl=ko)

---

### [🟠-10] ProGuard / R8 설정 검증

**왜 블로커**: release 빌드는 기본적으로 `minifyEnabled = true` (R8 코드 축소·난독화). Firebase·Retrofit·Coil·Kotlinx Serialization 등 **리플렉션 기반 라이브러리는 ProGuard rule 누락 시 release에서 silent crash**. debug 빌드에선 발견 불가 → release 빌드 직접 테스트만이 유일한 검증법.

**현재 코호트 상태**:
- 각 팀 `app/proguard-rules.pro` 내용 + release 빌드 직접 설치 테스트 여부 확인 필요
- 3팀: [proguard-rules.pro](3team/repo/app/proguard-rules.pro) 존재 — 내용 확인 필요

**조치**:
1. `app/build.gradle.kts` 에서 release `minifyEnabled = true`, `shrinkResources = true` 확인
2. 사용 중인 라이브러리별 공식 ProGuard rule 추가:
   - **Kotlinx Serialization**: `@Serializable` 클래스 keep
   - **Retrofit**: `META-INF/proguard/retrofit2.pro` 자동 포함되지만 모델 클래스는 별도 keep 필요
   - **Firebase**: 대부분 자동, 단 `@Keep` 또는 명시적 keep 권장
   - **Coil**: 자동
3. release 빌드 설치 → 전 화면 회귀 — 특히 **JSON 역직렬화 / Firebase 모델 / Reflection 사용 부분** 중점
4. Crashlytics 활성화 시 mapping.txt 자동 업로드 설정 — release crash 역추적 가능

**검증**: release .aab 설치 후 모든 기능 1회 이상 동작

**공식 문서**:
- [코드 축소·난독화 (R8)](https://developer.android.com/build/shrink-code?hl=ko)
- [Crashlytics 난독화 매핑](https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?hl=ko&platform=android)
- [Retrofit ProGuard rules](https://github.com/square/retrofit/blob/trunk/retrofit/src/main/resources/META-INF/proguard/retrofit2.pro)
- [Kotlinx Serialization ProGuard](https://github.com/Kotlin/kotlinx.serialization?tab=readme-ov-file#android)

---

### [🟡-11] Pre-launch Report 통과

**왜 권장**: Play Console은 .aab 업로드 시 **자동으로 실제 디바이스에 설치하고 robot crawler로 화면을 돌려보며** crash·ANR·accessibility·security 이슈를 자동 검출. 1차 배포 전에 internal testing 트랙에 한 번 올리면 무료로 받을 수 있음. 이 단계에서 portrait/fontScale 이슈, ProGuard 누락 crash 등 다수 사전 발견 가능.

**현재 코호트 상태**: Play Console 미생성 추정 → 모든 팀 미실행

**조치**:
1. Play Console → 앱 생성 → Internal testing 트랙에 .aab 업로드
2. 24시간 내 Pre-launch Report 자동 생성
3. 리포트 카테고리 점검:
   - **Stability** (crash/ANR) — 발견 시 즉시 fix
   - **Performance** — 시작 시간, 메모리
   - **Accessibility** — fontScale, contrast, touch target — 🔴 이슈 발견될 가능성 높음 (현재 임시 조치 상태)
   - **Security & trust** — 평문 통신, 알려진 취약점 라이브러리

**검증**: Pre-launch Report에 🔴(critical) 이슈 0건

**공식 문서**:
- [Pre-launch Report 사용](https://support.google.com/googleplay/android-developer/answer/9842757?hl=ko)
- [Pre-launch Report 카테고리](https://support.google.com/googleplay/android-developer/answer/7002270?hl=ko)
- [Android 접근성 가이드](https://developer.android.com/guide/topics/ui/accessibility?hl=ko)

---

### [🟢-12] 카카오 디벨로퍼스 출시 모드 검수

**왜 블로커**: 카카오 로그인은 기본이 **개발 모드** — 카카오 디벨로퍼스 콘솔에 명시적으로 등록된 사용자(앱 관리자가 직접 추가)만 로그인 가능. **출시 모드 전환을 위해서는 카카오 측 검수 통과 필요** (보통 1~2주). 미통과 시 일반 사용자 로그인 100% 실패.

**현재 코호트 상태**: 전 팀 카카오 로그인 사용. 검수 신청 여부 확인 필요

**조치**:
1. 카카오 디벨로퍼스 콘솔 → 내 애플리케이션 → 비즈니스 채널 등록
2. 사업자 정보 등록 (개인 개발자도 가능 — 사업자 등록증 불필요)
3. **비즈 앱 전환 신청** → 카카오 검수 요청
4. 검수 자료:
   - 앱 소개·기능 설명
   - 카카오 로그인 사용 화면 스크린샷
   - 개인정보처리방침 URL (위 [🟠-06] 결과 재사용)
5. 검수 통과 후 → 콘솔에서 "앱 상태"가 "운영 중"으로 전환

**검증**: 등록 외 사용자(타인 계정)로 로그인 성공

**공식 문서**:
- [카카오 로그인 시작하기](https://developers.kakao.com/docs/latest/ko/kakaologin/common)
- [Android 카카오 로그인 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/android)
- [비즈 앱 전환 가이드](https://developers.kakao.com/docs/latest/ko/getting-started/app)
- [카카오 디벨로퍼스 콘솔](https://developers.kakao.com/console/app)

---

### [🟢-13] 카카오 Release Key Hash 등록

**왜 블로커**: 카카오 SDK는 호출 시점에 **앱 서명의 key hash를 카카오 서버로 전송하여 검증**. 카카오 콘솔에 release 키의 hash가 등록되어 있지 않으면 로그인 거부.

**현재 코호트 상태**: 모든 팀 debug key hash만 등록되어 있을 가능성

**조치**:
1. Release keystore의 key hash 추출 (Kakao SDK 콘솔에 추출 명령 안내됨):
   ```bash
   keytool -exportcert -alias release -keystore release.keystore | openssl sha1 -binary | openssl base64
   ```
2. Play App Signing 사용 시 — Play Console에서 제공하는 **app signing key**의 SHA1을 base64로 변환한 값도 추가
3. 카카오 디벨로퍼스 콘솔 → 앱 → 플랫폼 → Android → 키 해시 등록 (debug·release·App Signing 모두)

**검증**: Release 빌드 직접 설치 후 카카오 로그인 성공

**공식 문서**:
- [카카오 Android 키 해시 등록](https://developers.kakao.com/docs/latest/ko/android/getting-started#add-key-hash)
- [Play App Signing 키 해시 변환](https://developers.kakao.com/docs/latest/ko/android/getting-started#kakao-applink-key-hash)

---

## 2. 팀별 진척 매트릭스

각 팀 담당자가 ☐ → ✅ 로 변경. ❌는 미해당. ⚠️는 진행 중.

| # | Sev | 항목 | 1팀 | 2팀 | 3팀 |
|---|---|---|---|---|---|
| 01 | 🔴 | Firestore Security Rules 배포 | ☐ | ☐ | ☐ |
| 02 | 🔴 | Cloud Functions 배포 | ☐ | ☐ | ☐ |
| 03 | 🔴 | Release SHA-1·SHA-256 Firebase 등록 | ☐ | ☐ | ☐ |
| 04 | 🔴 | API Key Restrictions | ☐ | ☐ | ☐ |
| 05 | 🟠 | Background Location 특별 심사 | ☐ | ☐ | ☐ |
| 06 | 🟠 | Privacy Policy URL 호스팅 | ☐ | ☐ | ☐ |
| 07 | 🟠 | Data Safety Form 작성 | ☐ | ☐ | ☐ |
| 08 | 🟠 | Target SDK 35+ | ☐ | ☐ | ☐ |
| 09 | 🟠 | App Bundle (.aab) + Release Signing | ☐ | ☐ | ☐ |
| 10 | 🟠 | ProGuard/R8 검증 | ☐ | ☐ | ☐ |
| 11 | 🟡 | Pre-launch Report 통과 | ☐ | ☐ | ☐ |
| 12 | 🟢 | 카카오 비즈 앱 검수 | ☐ | ☐ | ☐ |
| 13 | 🟢 | 카카오 Release Key Hash 등록 | ☐ | ☐ | ☐ |

---

## 3. 권장 진행 순서 / 타임라인

**1차 배포 D-day 기준 역산**:

| 시점 | 작업 | 이유 |
|---|---|---|
| **D-21** | Background Location 특별 심사 신청 [🟠-05] / 카카오 비즈 앱 검수 신청 [🟢-12] | 외부 심사 소요 2~4주 — 가장 먼저 시작 |
| **D-14** | Firestore Rules 작성·배포 [🔴-01] / Functions 작성·배포 [🔴-02] / Release Keystore 생성·SHA 등록 [🔴-03] [🟢-13] | 코드 작업 즉시 가능, 미루면 다른 작업 검증 불가 |
| **D-10** | API Key Restrictions [🔴-04] / Target SDK 35 변경 [🟠-08] / Release signingConfig 설정 [🟠-09] | 보안 + 호환성 |
| **D-7** | ProGuard 검증 [🟠-10] / Privacy Policy URL 호스팅 [🟠-06] / Data Safety Form 작성 [🟠-07] | 1차 release 빌드 검증 |
| **D-5** | Internal testing 트랙 .aab 업로드 → Pre-launch Report 수신 [🟡-11] | 자동 검출 이슈 fix 시간 확보 |
| **D-3** | Pre-launch Report 이슈 fix → 재업로드 | 최종 검증 |
| **D-1** | Production 트랙 출시 신청 → Google 심사 (보통 24~72시간) | 심사 후 자동 공개 |

⚠️ Background Location·카카오 비즈 앱 검수는 **D-day에서 가장 멀리 떨어진 시점에 신청** — 외부 심사 시간이 가장 큰 리스크.

---

## 4. 빠른 진단 명령어 모음

각 팀 repo 루트에서 실행:

```bash
# (1) Firestore Rules 배포 여부 — firebase.json에 firestore 키 존재?
cat firebase.json | grep -A2 firestore

# (2) Cloud Functions export 개수 — 0이면 [🔴-02]
grep -c "^exports\." functions/index.js

# (3) Cloud Function 호출 코드 위치 — 함수 미배포 시 실패할 지점
grep -rn "httpsCallable\|getHttpsCallable\|onCall(" app/src

# (4) 백그라운드 위치 권한 사용 여부 — [🟠-05] 해당?
grep -n "ACCESS_BACKGROUND_LOCATION" app/src/main/AndroidManifest.xml

# (5) Target SDK 확인
grep -n "targetSdk\s*=" app/build.gradle.kts

# (6) Release signingConfig 존재?
grep -A5 "signingConfigs" app/build.gradle.kts

# (7) minifyEnabled / ProGuard 설정
grep -A3 "release\s*{" app/build.gradle.kts

# (8) Debug keystore SHA1 (현재 등록된 게 본 키 맞는지)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep "SHA1\|SHA-1"

# (9) Release keystore SHA (생성 후)
keytool -list -v -keystore release.keystore -alias release | grep "SHA1\|SHA-1\|SHA256\|SHA-256"

# (10) 카카오 키 해시 (release)
keytool -exportcert -alias release -keystore release.keystore | openssl sha1 -binary | openssl base64
```

---

## 5. 참고 — 모든 항목의 상위 공식 출처

| 출처 | 영역 |
|---|---|
| [Firebase 공식 문서 (한국어)](https://firebase.google.com/docs?hl=ko) | Firestore, Functions, Auth, API key |
| [Android 개발자 가이드 (한국어)](https://developer.android.com/?hl=ko) | SDK, ProGuard, App Bundle, 권한 |
| [Google Play Console 고객센터 (한국어)](https://support.google.com/googleplay/android-developer/?hl=ko) | 정책, 심사, Data Safety, Pre-launch |
| [Google Play 정책 센터 (한국어)](https://support.google.com/googleplay/android-developer/topic/9858052?hl=ko) | 모든 정책 (위치, 데이터, 권한) |
| [카카오 디벨로퍼스 (한국어)](https://developers.kakao.com/docs/latest/ko) | 카카오 로그인 |
| [개인정보보호위원회 (한국어)](https://www.pipc.go.kr/) | 개인정보 처리방침 표준 양식 |

---

## 6. 변경 이력

| 일자 | 작성자 | 변경 |
|---|---|---|
| 2026-06-01 | 보조강사 | 최초 작성. 13개 항목 / 4 severity / 코호트 공통 |
