#!/bin/bash
echo "🚀 Starting Finance Analytics Platform..."

# Start backend
echo "📦 Starting Spring Boot backend..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"
cd ..

# Wait for backend to start
echo "⏳ Waiting for backend to start (30s)..."
sleep 30

# Start frontend
echo "🎨 Starting Angular frontend..."
cd frontend
npm install --legacy-peer-deps
npm start &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"
cd ..

echo ""
echo "✅ Application started!"
echo "   Frontend: http://localhost:4200"
echo "   Backend:  http://localhost:8080"
echo "   API Docs: http://localhost:8080/actuator/health"
echo ""
echo "Press Ctrl+C to stop..."

# Save PIDs
echo "$BACKEND_PID $FRONTEND_PID" > .pids

wait
