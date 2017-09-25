#!/bin/sh
nohup java ${vm} -jar ${project}-${bboss_version}.jar restart --shutdownLevel=9 > ${project}.log &
tail -f ${project}.log
