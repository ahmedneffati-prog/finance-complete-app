@echo off
echo Starting Finance Analytics Platform...

echo Starting Spring Boot backend...
start "Backend" cmd /k "cd backend && mvnw.cmd spring-boot:run"

echo Waiting 30 seconds for backend...
timeout /t 30

echo Starting Angular frontend...
start "Frontend" cmd /k "cd frontend && npm install --legacy-peer-deps && npm start"

echo.
echo Application started!
echo   Frontend: http://localhost:4200
echo   Backend:  http://localhost:8080
echo.
echo Close the backend and frontend windows to stop.
pause
