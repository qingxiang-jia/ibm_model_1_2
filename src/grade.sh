#!/bin/bash
echo "Qingxiang Jia" 
echo "qj2125"
echo "COMS4705 Project3"
echo "Compiling program for Question 4..."
time javac EM1.java
echo "Running program for Question 4..."
time java EM1 > q4_output.txt
echo "Compiling program for Question 5..."
time javac EM2.java
echo "Running program for Question 5..."
time java EM2 > q5_output.txt
echo "Compiling program for Question 6..."
time javac Aligner.java
echo "Running program for Question 6..."
time java Aligner > unscrambled.en
python eval_scramble.py unscrambled.en original.en
echo "Many files have been generated, see report for details."
echo "Thanks for grading, bye."