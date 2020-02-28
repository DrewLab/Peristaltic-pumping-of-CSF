Written by Ravi Kedarasetti(2020), Department of Engineering science and mechancs, Pennsylvania State University , PA, USA
rpk5196_at_psu_dot_edu

All the .m  files in this repository work with Matlab® 2019 (Mathworks Inc.)
All the .java and .class files in this repository work with Comsol Multiphysics® 5.4
All the .nb files in this repository works with Wolfram Mathematica® 12
**************************************************************************************************
Note: the output of the Mathematica notebooks are already provided in text files. It is not necessary to run the notebooks or to even own a mathematica license to succesfully run the code. The notebooks contain the governing equations in their weak form and the necessary code to convert the tensor equations to their component form in a format that is compatible with Comsol.

Note: The comsol programs are written as .java files and compiled with Comsol 5.4 and  oraclejdk 1.8.0. The compiled class files are provided and can be run without oraclejdk.


*************************************************************************************************
Programs required:
Matlab® 2019 or above
Comsol Multiphysics® 5.4 or above
************************************************************************************************
Before opening any of the class files check that comsol has access to all files and system properties
1. In Comsol GUI: go to Options > Preferences > Security 
2. Under Methods and Java libraries, check "Allow access to system properties" and select "All files" under "File system access"
On linux, make sure to launch comsol from the directory with all the variable and parameter files

***********************************************************************************************
to compile .java files use the command 
on Windows: comsolcompile <filename>.java
on Mac and Liux: comsol compile <filename>.java
the generated .class file can be opened using Comsol Multiphysics® GUI
alternately, the class files can be run using the commandline
comsol -nn <number_of_nodes> batch -inputfile <inputfile>.class -otputfile <outputfile>.mph -batchlog <logfile>.log

**********************************************************************************************
**************** 
**************** Generate result figures
****************
**********************************************************************************************
To create the 2d demo of fluid motion under wall movement:
1. Run "Peristalsis_demo_2d.class" with COMSOL
this will generate the results file demo2dResults.txt
the output .mph file will contain the pressure surface plot in Fig 1b
2. Run "peristalsis_mechanism_demo.m" with Matlab
this generates the particle tracking plots in Fig 1a and the video SV1
**********************************************************************************************
To create the 2d demo of fluid motion under periodic wall movement:
1. Run "Peristalsis_wave_2d.class" with COMSOL
this will generate the results file wave2dResults.txt
the output .mph file will contain the pressure surface plot in Fig S1b
2. Run "peristalsis_wave_demo.m" with Matlab
this generates the particle tracking plots in Fig S1a and the video SV2
*********************************************************************************************





All Wolfram Mathematica files
Copyright (c) 2020 Francesco Costanzo

All Matlab and Java(Comsol) files
Copyright (c) 2020 Ravi Kedarasetti 


THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.