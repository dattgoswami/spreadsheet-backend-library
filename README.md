# Spreadsheet Backend Library

## Overview
This library provides a simple in-memory Spreadsheet representation with functionalities for setting and retrieving cell
values, evaluating expressions within cells, detecting circular references in cell dependencies, and supporting undo/redo operations.

## Class Overview
The Spreadsheet class provides a basic implementation of a spreadsheet, where each cell in the spreadsheet can hold either
a raw value or a formula. The formula can consist of arithmetic operations on the values of other cells, with the provision
that circular dependencies are not allowed. The spreadsheet also supports undo and redo operations for value assignments to cells.
The class utilizes a HashMap to store cell values and relies on the `mxparser` library for expression evaluation.

## Features
- **CellValue Setting:** You can set the value of a cell in the spreadsheet using the `setCellValue` method.
- **CellValue Getting:** You can retrieve the value of a cell in the spreadsheet using the `getCellValue` method.
- **Expression Evaluation:** The class can evaluate mathematical expressions stored in cells.
- **Circular Reference Detection:** The class can detect circular references in cell dependencies to avoid endless loops.
- **Undo/Redo Operations:** The class supports undo and redo operations which can revert a cell value setting or deletion.

## Dependency
This library relies on the following dependencies:
- `mxparser` library for expression evaluation.
- SLF4J (Simple Logging Facade for Java) framework for logging.
- Logback implementation as the logging backend.
- Apache Commons Lang 3 library for utility class NumberUtils.

Note: As an alternative to the mxparser library, GraalVM was explored to create JavaScript context(/runtime), as well as
the Nashorn JavaScript engine for expression evaluation.

## Logging Configuration
By default, the logging configuration for this library turned off. If you want to customize the logging configuration, you
can update the `logback.xml` file and placed in the `src/main/resources` directory.

## Getting Started
1. Make sure you have Java 8 or higher installed in your system.
2. Import the `Spreadsheet` class into your Java project.
3. Create a new instance of the class to use the spreadsheet functionality in your project.

## Usage Instructions
- Docker is available for running tests: `docker-compose up`.
- Ensure that Maven is installed on your system.
- Open a command prompt or terminal and navigate to the root directory of the project.
- Run the command `mvn clean package` to build the project.
- After a successful build, the application can be run using the Maven exec plugin:
   - From the project's root directory, execute the following command: `mvn exec:java -Dexec.mainClass=com.orkes.spreadsheet.Main`

You will need to have all required dependencies and classpath properly configured to run the application.

## Some Edge Cases
### The spreadsheet class attempts to handle some edge cases such as:
- Circular reference detection helps prevent endless loops caused by a cell referencing itself or another cell that references
  back to it.
- Formula evaluation catches and gracefully handles arithmetic exceptions.

### Limitations:
- **Circular reference detection:** The current implementation only checks for direct circular references, not indirect ones.
   This might be a potential problem if a cell's formula refers to another cell which in turn refers back to the original cell.
   Example:
   ```
   Cell A1 has a formula that references cell B1: A1 = B1 + 1.
   Cell B1 also has a formula that references cell A1: B1 = A1 * 2.
   ```
- **Dependency handling:** While the `updateDependencies` method updates dependencies every time a new formula is added to a
  cell, it doesn't handle removing or updating dependencies when a formula is removed or updated. If the formula for
  a cell is deleted or replaced with a non-formula value, the cell's dependencies are not updated accordingly, which may cause 
  issues with the `detectCircularDependency` method.
- **Thread safety:** If this class is intended to be used in a multi-threaded environment, there could be concurrency issues. 
  Synchronization mechanisms (like using the `synchronized` keyword or a `ReentrantLock`) could be used to prevent potential race conditions.
   Example:
   ```
   Thread t1 = new Thread(() -> spreadsheet.updateCell("A1", "1"));
   Thread t2 = new Thread(() -> spreadsheet.updateCell("A1", "=B1+C1"));
   t1.start();
   t2.start();
   // A1's value could be inconsistent due to race conditions
   ```

## Future Enhancements

1. **Support for More Cell Reference Formats:**
   Currently, the code only works with cell references like "A1", "B2", etc. Future enhancements could expand this to support
   ranges of cells, like "A1:A3", or even entire columns or rows, like "A:A" or "1:1".
2. **Formula Parser:**
   Implementing a proper formula parser would provide better error checking and could allow for more complex formulas, such
   as formulas with nested parentheses.
3. **Persistence:**
   Future versions could add functionality to save and load spreadsheet data from external storage, such as a file or database.
   This would allow users to persist their spreadsheet data and work with it across multiple sessions.
4. **Support for Functions:**
   The current code doesn't support spreadsheet functions like SUM, AVERAGE, etc. Adding support for these would greatly
   increase the utility of the class.
5. **Cell Formatting:**
   Future enhancements could include things like the number of decimal places to display, whether to use a comma as a thousand
   separator, the font and background color of a cell, etc.
6. **User Interface:**
   Building a graphical user interface (GUI) or a web-based interface to interact with the spreadsheet would make it more
   user-friendly and provide features like formatting options, cell merging, cell styling, drag-and-drop functionality, etc.
7. **Collaboration and Sharing:**
   Implementing collaborative features could enable multiple users to work on the same spreadsheet simultaneously. Future
   versions could add options for sharing and controlling access to spreadsheets, allowing users to collaborate and edit the
   same document in real-time.