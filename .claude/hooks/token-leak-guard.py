#!/usr/bin/env python3
"""
Token Leak Guard — Claude Code PreToolUse Hook.

목적: Write / Edit / NotebookEdit 도구 호출 시 도구 입력에 포함된 시크릿 패턴을
      사전 차단하여 git 저장소에 토큰·API 키·비밀번호가 누출되는 것을 막는다.

검출 패턴:
  - Figma PAT:        figd_[A-Za-z0-9_-]{30,}
  - Google API key:   AIza[A-Za-z0-9_-]{30,}
  - Stripe live key:  sk_live_[A-Za-z0-9]{20,}
  - GitHub PAT:       ghp_[A-Za-z0-9]{36,}
  - GitHub fine PAT:  github_pat_[A-Za-z0-9_]{40,}
  - Slack token:      xox[abprs]-[A-Za-z0-9-]{20,}
  - AWS access key:   AKIA[A-Z0-9]{16}
  - Private key:      -----BEGIN (RSA|EC|OPENSSH|PRIVATE) (PRIVATE )?KEY-----
  - JWT (3-part):     eyJ[A-Za-z0-9_-]{20,}\\.eyJ[A-Za-z0-9_-]{20,}\\.[A-Za-z0-9_-]{20,}

면제 파일 (의도된 시크릿 저장소):
  - .figma_token, .env, .env.*, *.token, *.key, *.pem, *.jks, *.p12, credentials*

면제 폴더:
  - .shared/, .secrets/ 하위 (시크릿 의도 폴더)

프로토콜:
  - 입력: stdin JSON — { tool_name, tool_input: {file_path, content?, new_string?, ...} }
  - 차단: stderr 메시지 + exit 2
  - 허용: exit 0

테스트:
  echo '{"tool_name":"Write","tool_input":{"file_path":"test.md","content":"figd_ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"}}' | python token-leak-guard.py
"""

import json
import os
import re
import sys

# ============================================
# 검출 패턴 (실제 토큰 길이 강제로 false positive 회피)
# ============================================
TOKEN_PATTERNS = [
    ("Figma PAT",           r"figd_[A-Za-z0-9_-]{30,}"),
    ("Google API key",      r"AIza[A-Za-z0-9_-]{30,}"),
    ("Stripe live key",     r"sk_live_[A-Za-z0-9]{20,}"),
    ("Stripe test key",     r"sk_test_[A-Za-z0-9]{20,}"),
    ("GitHub classic PAT",  r"ghp_[A-Za-z0-9]{36,}"),
    ("GitHub fine PAT",     r"github_pat_[A-Za-z0-9_]{40,}"),
    ("Slack token",         r"xox[abprs]-[A-Za-z0-9-]{20,}"),
    ("AWS access key",      r"AKIA[A-Z0-9]{16}"),
    ("Private key block",   r"-----BEGIN (RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----"),
    ("JWT 3-part",          r"eyJ[A-Za-z0-9_-]{20,}\.eyJ[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}"),
    ("Anthropic API key",   r"sk-ant-api03-[A-Za-z0-9_-]{40,}"),
    ("OpenAI API key",      r"sk-(?!ant-)[A-Za-z0-9]{40,}"),
]

# ============================================
# 면제 파일/폴더
# ============================================
EXEMPT_FILE_PATTERNS = [
    r"\.figma_token$",
    r"\.env$",
    r"\.env\.[^/\\]+$",
    r"\.token$",
    r"[^/\\]*\.token$",
    r"\.key$",
    r"\.pem$",
    r"\.jks$",
    r"\.p12$",
    r"^credentials",
    r"[/\\]credentials[^/\\]*$",
]

EXEMPT_DIR_PATTERNS = [
    r"[/\\]\.shared[/\\]",
    r"[/\\]\.secrets[/\\]",
]


def is_exempt_path(file_path: str) -> bool:
    """면제 경로인지 판정."""
    if not file_path:
        return False
    norm = file_path.replace("\\", "/")
    # 디렉터리 면제 (단, .shared 안의 README/MD 파일은 면제 ❌ —
    # 시크릿 자체 파일만 면제)
    # → 더 보수적으로: 디렉터리 면제는 제외하고 파일명 패턴만 사용
    basename = os.path.basename(norm)
    for pat in EXEMPT_FILE_PATTERNS:
        if re.search(pat, basename, re.IGNORECASE):
            return True
        if re.search(pat, norm, re.IGNORECASE):
            return True
    return False


def extract_content(tool_name: str, tool_input: dict) -> str:
    """Write/Edit/NotebookEdit 도구 입력에서 검사할 콘텐츠 추출."""
    parts = []
    # Write
    if "content" in tool_input:
        parts.append(str(tool_input.get("content", "")))
    # Edit (단일)
    if "new_string" in tool_input:
        parts.append(str(tool_input.get("new_string", "")))
    # Edit replace_all용에도 new_string 적용됨 (위에서 잡힘)
    # MultiEdit (Edit 다회)
    if "edits" in tool_input and isinstance(tool_input["edits"], list):
        for e in tool_input["edits"]:
            if isinstance(e, dict):
                parts.append(str(e.get("new_string", "")))
    # NotebookEdit
    if "new_source" in tool_input:
        parts.append(str(tool_input.get("new_source", "")))
    return "\n".join(parts)


def scan_secrets(content: str):
    """콘텐츠에서 토큰 패턴 검출. 발견된 패턴 이름 리스트 반환."""
    if not content:
        return []
    found = []
    for name, pattern in TOKEN_PATTERNS:
        m = re.search(pattern, content)
        if m:
            # 매치된 일부를 마스킹해서 출력 (전체 토큰 stderr에도 노출 금지)
            matched = m.group(0)
            preview = matched[:8] + "***" + matched[-4:] if len(matched) > 16 else "***"
            found.append((name, preview))
    return found


def main():
    # Windows 콘솔에서 한글 stderr 깨짐 방지
    try:
        sys.stderr.reconfigure(encoding="utf-8")
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass  # Python 3.6 이하 또는 reconfigure 미지원 환경

    try:
        payload = json.load(sys.stdin)
    except Exception as e:
        # 페이로드 불량 — 허용 (PreToolUse hook 자체가 깨지면 안 됨)
        sys.stderr.write(f"[token-leak-guard] payload 파싱 실패: {e}\n")
        sys.exit(0)

    tool_name = payload.get("tool_name", "")
    tool_input = payload.get("tool_input", {}) or {}

    # Write/Edit/MultiEdit/NotebookEdit 만 검사
    if tool_name not in ("Write", "Edit", "MultiEdit", "NotebookEdit"):
        sys.exit(0)

    file_path = tool_input.get("file_path", "") or tool_input.get("notebook_path", "")

    # 면제 파일이면 허용
    if is_exempt_path(file_path):
        sys.exit(0)

    content = extract_content(tool_name, tool_input)
    leaks = scan_secrets(content)

    if leaks:
        msg = "🚨 token-leak-guard: 시크릿 패턴 감지 — 쓰기 차단\n"
        msg += f"   대상 파일: {file_path}\n"
        msg += f"   감지된 패턴 ({len(leaks)}건):\n"
        for name, preview in leaks:
            msg += f"     - {name}: {preview}\n"
        msg += "\n조치:\n"
        msg += "  1. 실제 시크릿이면 즉시 발급 기관(Figma/Google/GitHub 등)에서 revoke + 재발급\n"
        msg += "  2. 의도된 저장소(.figma_token 등)에 저장하려면 파일명을 면제 패턴에 맞춤\n"
        msg += "     면제 패턴: .figma_token, *.token, *.env, *.env.*, *.key, *.pem, *.jks, *.p12, credentials*\n"
        msg += "  3. 코드/문서 내 마스킹 (예: figd_***)이 필요하면 토큰 일부 글자만 표시\n"
        msg += "  4. 본 hook은 .claude/hooks/token-leak-guard.py — 패턴 추가는 해당 파일 수정\n"
        sys.stderr.write(msg)
        sys.exit(2)

    sys.exit(0)


if __name__ == "__main__":
    main()
