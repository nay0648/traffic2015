# An Acoustic Traffic Monitoring System: Design and Implementation

Yueyue Na, Yanmeng Guo, Qiang Fu, Yonghong Yan

The Key Laboratory of Speech Acoustics and Content Understanding</br>
Institute of Acoustics, Chinese Academy of Sciences</br>
Beijing, China</br>
{nayueyue, guoyanmeng, qfu, yanyonghong}@hccl.ioa.ac.cn

# Abstract

Vehicle emits sounds as it travels along the road, which can be used for traffic monitoring. In this paper, an acoustic based traffic monitoring system is designed and implemented. The system utilizes a cross microphone array to collect road-side acoustic signals. Then, lane positions are automatically detected by the built-in lane detection module. Eventually, different measuring indices which reflect the road condition and traffic quality are derived according to the collected signals and the detected lanes. Since acoustic sensor is less expensive than other types of vehicle sensors, and acoustic features are robust against light, weather, and environmental variations, we expect that the proposed acoustic traffic monitoring system will have lower hardware cost, and become a good complement to the existing traffic monitoring techniques.

# The Demo
Currently only the simulation demo is available. A four-lane road is simulated, the lanes and the vehicles can be detected from the simulated moving sound sources.
![image](https://github.com/nay0648/traffic2015/blob/main/fig/gui.png)

# How to Run
1. run on Windows: Just execute run.bat.
2. run on Eclipse: Import the project as a Eclipse java project, and run cn.ac.ioa.hccl.atm.demo.SimulationDemo.
