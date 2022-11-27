@echo off

java -cp ^
%~dp0/lib/commons-math3-3.3/commons-math3-3.3.jar;^
%~dp0/lib/jfreechart-1.0.17/hamcrest-core-1.3.jar;^
%~dp0/lib/jfreechart-1.0.17/jcommon-1.0.21.jar;^
%~dp0/lib/jfreechart-1.0.17/jfreechart-1.0.17.jar;^
%~dp0/lib/jfreechart-1.0.17/jfreechart-1.0.17-experimental.jar;^
%~dp0/lib/jfreechart-1.0.17/jfreechart-1.0.17-swt.jar;^
%~dp0/lib/jfreechart-1.0.17/jfreesvg-1.4.jar;^
%~dp0/lib/jfreechart-1.0.17/junit-4.11.jar;^
%~dp0/lib/jfreechart-1.0.17/orsoncharts-1.0-eval.jar;^
%~dp0/lib/jfreechart-1.0.17/orsonpdf-1.3-eval.jar;^
%~dp0/lib/jfreechart-1.0.17/servlet.jar;^
%~dp0/lib/jfreechart-1.0.17/swtgraphics2d.jar;^
%~dp0/lib/traffic2015/traffic2015.jar;^
 cn.ac.ioa.hccl.atm.demo.SimulationDemo
