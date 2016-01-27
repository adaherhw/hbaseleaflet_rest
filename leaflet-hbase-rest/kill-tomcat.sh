if [ `ps -eaf | grep tomca[t] | grep leaflet | wc -l` == 1 ]; then
kill -9 `ps -eaf | grep tomca[t] | grep leaflet | awk '{print $2}'`
fi

