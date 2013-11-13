*********************************************************************README************************************************************************
***************************************************************************************************************************************************

Name: Madhusudhan Sankaran.

***************************************************************************************************************************************************
***************************************************************************************************************************************************

This program was developed in the Eclipse Juno IDE in Microsoft Windows 7 Operating System using java as the programming language.

There are two java file included. It is Indexing.java and Stemmer.java. Stemmer was java was gotten from the internet and proper citations have been made in the program. The program implements the Porter stemmer algorithm.

Instructions for compiling the program:
---------------------------------------

1. Compile the program using the javac command as follows:

		javac Stemmer.java
		javac Indexing.java
		

2. This should produce the .class files and the program should be ready for execution.
 

Instructions for running the program:
-------------------------------------

1. To run the program, type the following command :
		
		java Indexing <arg1> <arg2> <arg3>
		
2. arg1, arg2 and arg3 stand for the following :

		arg1 : The path where the directory(in this case Cranfield) is located.
		arg2 : The path where the stopwords file is located.
		arg3 : The path where the WordList is located. WordList is a file that contains the words for which Df, Tf and inverted list length (in bytes) is to be calculated. Extra terms can be added in subsequent lines in the file.
		
3. For example, the following is a valid execution of the algorithm ;

		java Tokens ./Cranfield/ ./stopwords ./Words

4. This should run the program and the output should be produced.


Read the Program Description file for more details.