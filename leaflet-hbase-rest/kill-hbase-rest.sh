if [ `ps -eaf | grep 1234[5] | wc -l` == 1 ]; then
kill -9 `ps -eaf | grep 1234[5] | awk '{print $2}'`
fi
