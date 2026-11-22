@echo off
cd /d "%~dp0"
echo 正在同步并覆盖推送到 GitHub...
git push -u origin main --force
echo.
if %errorlevel% equ 0 (
    echo ==============================================
    echo [成功] 代码已全部同步到 GitHub！
    echo GitHub Actions 正在自动打包 APK，请查看：
    echo https://github.com/bourgjavisdes29990-ctrl/seu-shuttle/actions
    echo ==============================================
) else (
    echo 推送失败，请检查网络或登录状态。
)
echo.
pause
