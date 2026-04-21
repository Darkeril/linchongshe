cd ..
echo "" > web.log
nohup pnpm dev > web.log &
echo "Start GPTHUB Web complete!"
tail -f web.log
