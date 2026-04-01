#!/bin/bash
echo "🛑 Stopping Finance Analytics Platform..."
if [ -f .pids ]; then
  PIDS=$(cat .pids)
  for PID in $PIDS; do
    kill $PID 2>/dev/null && echo "Stopped PID $PID"
  done
  rm .pids
fi
# Also kill by port just in case
fuser -k 8080/tcp 2>/dev/null
fuser -k 4200/tcp 2>/dev/null
echo "✅ Stopped"
