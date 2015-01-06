For my data, you should:

1. Add ./source/java/lib/\*.jar to your CLASSPATH. And add ./source/java/src to PATH.
2. Add your weka.jar to CLASSPATH. 
3. Edit ./Runner.py -> path_work
4. Edit ./lib/Path.py->class ExternalPaths->self.R (where your Rscript.exe locate)
5. Edit paths in ./lib/source/R/rma.R. 
6. Run ./Runner.py and follow instructions. 

For new data, you should: 

1.	Add ./source/java/lib/\*.jar to your CLASSPATH. And add ./source/java/src to PATH.
2.	Add your weka.jar to CLASSPATH. 
3.	Edit ./lib/Labels.py (nearly all values)
4.	Edit ./lib/source/R/rma.R (paths, names, and so on) 
5.	Edit ./Runner.py -> path_work
6.	Edit ./lib/Path.py->class ExternalPaths->self.R (where your Rscript.exe locate)
7.	Edit ./lib/Path.py->class IOPath
8.	Run ./Runner.py and follow instructions. 