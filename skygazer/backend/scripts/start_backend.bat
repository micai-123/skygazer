@echo off
cd /d D:\MyProject\jishe2\skygazer\backend
"D:\JavaSoft\maven\apache-maven-3.6.3\bin\mvn.cmd" -Dmaven.test.skip=true spring-boot:run > D:\MyProject\jishe2\backend_run.out 2> D:\MyProject\jishe2\backend_run.err
