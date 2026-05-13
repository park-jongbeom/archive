#!/usr/bin/env bash
# token-leak-guard.sh
# token-leak-guard.py 실행을 위한 cross-platform Python 탐색 래퍼.
# Python 미발견 시 graceful exit 0 (write 차단 X — 보안 hook 자체 부재로 사용자 작업이 멈추면 안 됨)

set -u

HOOK_DIR="$(cd "$(dirname "$0")" && pwd)"
PY_SCRIPT="$HOOK_DIR/token-leak-guard.py"

if [ ! -f "$PY_SCRIPT" ]; then
    echo "[token-leak-guard] Python script not found: $PY_SCRIPT" >&2
    exit 0
fi

# Python 탐색 순서:
# 1. python3 (Unix-style 표준)
# 2. python (path 우선)
# 3. uv-managed Python (사용자 환경 fallback)
# 4. WindowsApps stub은 0.0.0.0 버전으로 작동 안 함 → 건너뜀
PYTHON=""

for cand in python3 python; do
    if command -v "$cand" >/dev/null 2>&1; then
        # WindowsApps stub 제외 (실제 Python 아님)
        path=$(command -v "$cand")
        if echo "$path" | grep -q "WindowsApps"; then
            continue
        fi
        # 버전 확인 (실제 실행 가능?)
        if "$cand" --version >/dev/null 2>&1; then
            PYTHON="$cand"
            break
        fi
    fi
done

# Fallback: uv-managed Python
if [ -z "$PYTHON" ]; then
    for cand in \
        "/c/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe" \
        "C:/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe"; do
        if [ -x "$cand" ]; then
            PYTHON="$cand"
            break
        fi
    done
fi

if [ -z "$PYTHON" ]; then
    echo "[token-leak-guard] Python 미발견 — 보안 검사 건너뜀 (write 허용)" >&2
    echo "[token-leak-guard] 활성화하려면 PATH에 Python 추가 또는 uv 설치 필요" >&2
    exit 0
fi

# stdin을 그대로 전달
exec "$PYTHON" "$PY_SCRIPT"
