# reliability-calculator
Computes availability and Mean Time Between Failures for components in series/parallel using sample data or data uploaded by the user. The program also demonstrated which arrangement of components produces a higher availabilty when used.
How to run:
Step 1: Compile C++backend.Make a new project in visual studio code and add contents of ReliabilityCalculator_backend.cpp to the default code. Add httplib.h to the header files. Build and run soution. Do not close the Visual Studio Code tab.
Step 2: After running succesfully, find the .exe file in the debug folder of the project folder and open it. It should show Server running on port 8080. Keep this window open
Step 3: Compile Java Frontend:Navigate to project folder and in the address bar type javac ReliabilityCalculator.java
Step 4:Start Java Frontend: Open Command Prompt and type java ReliabilityCalculator and the calculator terminal opens.
Step 5: Run the calculator: Click Load sample and 4 sample components will appear. Selectseries andclick calculate and results will apppear. Switch to parallel and click calculate and the results will appear.
