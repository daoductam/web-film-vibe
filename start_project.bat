@echo off
echo Starting Web Film Project...

echo Starting Backend...
cd web-film-backend
start "Web Film Backend" cmd /k "mvnw.cmd spring-boot:run"
cd ..

echo Starting Frontend...
cd web-film-frontend
start "Web Film Frontend" cmd /k "npm run dev"
cd ..

echo All services started locally.
