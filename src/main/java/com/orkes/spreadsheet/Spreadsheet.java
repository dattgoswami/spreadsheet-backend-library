package com.orkes.spreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariuszgromada.math.mxparser.Expression;

/**
 * The Spreadsheet class represents a simple in-memory spreadsheet.
 * It is responsible for managing cells and their associated values or formulas.
 * This class provides methods for setting and retrieving cell values, evaluating expressions within cells,
 * detecting circular references in cell dependencies, and supporting undo/redo operations.
 * The Spreadsheet utilizes a HashMap to store cell values and relies on the mxparser library for expression evaluation.
 */
public class Spreadsheet {
    private final Map<String, Object> cells;
    private final Map<String, String> formulas;
    private final Deque<Cell> history;
    private final Deque<Cell> future;
    private final Map<String, Set<String>> dependencies;
    private static final Logger logger = LoggerFactory.getLogger(Spreadsheet.class);

    public Spreadsheet() {
        this.cells = new HashMap<>();
        this.formulas = new HashMap<>();
        this.history = new ArrayDeque<>();
        this.future = new ArrayDeque<>();
        this.dependencies = new HashMap<>();
    }

    /**
     * Sets a cell value.
     * The value can be either a raw value or a formula. The method also performs formula validation
     * for circular reference and updates cell dependencies accordingly.
     * @param cellId identifier of the cell
     * @param value value to be set in the cell
     * @throws IllegalArgumentException if the formula has a circular reference is detected
     */
    public void setCellValue(String cellId, Object value) {
        validateCellId(cellId);
        if (value instanceof String && ((String) value).startsWith("=")) {
            updateDependencies(cellId, (String) value);
            formulas.put(cellId, (String) value);
        } else if (formulas.containsKey(cellId)) {
            formulas.remove(cellId);
            dependencies.remove(cellId);
        }
        future.clear();
        history.push(new Cell(cellId, cells.getOrDefault(cellId, null)));
        this.cells.put(cellId, value);
    }

    /**
     * Validates a cell ID by checking if it's null, empty or if it does not adhere to the cell ID format.
     * The expected cell ID format is a letter (or letters) followed by a number, such as 'A1' or 'BC12'.
     * @param cellId the cell ID to validate.
     * @throws IllegalArgumentException if the cell ID is null, empty or does not match the expected format.
     */
    private void validateCellId(String cellId) {
        if (cellId == null || cellId.isEmpty()) {
            logger.error("Invalid cellId");
            throw new IllegalArgumentException("Invalid cellId");
        }
        if (!cellId.matches("[A-Z]+[1-9][0-9]*")) {
            logger.error("Invalid cellId format");
            throw new IllegalArgumentException("Invalid cellId format");
        }
    }

    /**
     * Updates the dependencies of a cell based on a new formula.
     * If the cell already had dependencies, they are replaced with the new ones.
     * If the cell didn't have dependencies, the new ones are added.
     * @param cellId the ID of the cell to update.
     * @param formula the new formula.
     * @throws IllegalArgumentException if a circular dependency is detected.
     */
    private void updateDependencies(String cellId, String formula) {
        Set<String> newDependencies = extractDependenciesFromFormula(formula);
        if (dependencies.containsKey(cellId)) {
            dependencies.get(cellId).clear();
            dependencies.get(cellId).addAll(newDependencies);
        } else {
            dependencies.put(cellId, newDependencies);
        }
        if (detectCircularDependency(cellId)) {
            logger.error("Circular reference detected in cell {}", cellId);
            throw new IllegalArgumentException("Circular reference detected");
        }
    }

    /**
     * Extracts the dependencies from a formula.
     * A dependency is defined as a non-numeric part of the formula.
     * @param formula the formula to extract dependencies from.
     * @return a set of dependencies.
     */
    private Set<String> extractDependenciesFromFormula(String formula) {
        Set<String> dependencies = new HashSet<>();
        String[] parts = formula.substring(1).split("(?<=[+\\-*/])|(?=[+\\-*/])");
        for (String part : parts) {
            if (!isNumeric(part.trim())) {
                dependencies.add(part);
            }
        }
        return dependencies;
    }


    /**
     * Detects circular dependencies for a given cell.
     * A circular dependency is detected when a cell depends on itself, either directly or indirectly.
     * @param cellId the ID of the cell to check.
     * @return true if a circular dependency is detected, false otherwise.
     */
    private boolean detectCircularDependency(String cellId) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        visited.add(cellId);
        queue.add(cellId);
        while (!queue.isEmpty()) {
            String currentCell = queue.poll();
            Set<String> dependents = dependencies.get(currentCell);
            if (dependents != null) {
                if (dependents.contains(cellId)) {
                    logger.warn("Circular dependency detected for cellId: {}", cellId);
                    return true;
                }
                for (String dependent : dependents) {
                    if (!visited.contains(dependent)) {
                        visited.add(dependent);
                        queue.add(dependent);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a string is numeric.
     * @param str the string to check.
     * @return true if the string is numeric, false otherwise.
     */
    private boolean isNumeric(String str) {
        return NumberUtils.isCreatable(str);
    }

    /**
     * Returns the value of a given cell.
     * If the value is a formula, it gets evaluated. The method also performs formula validation before evaluation.
     * @param cellId identifier of the cell
     * @return value of the cell
     * @throws IllegalArgumentException if the cell does not exist or the formula is invalid
     */
    public Object getCellValue(String cellId) {
        if (!this.cells.containsKey(cellId)) {
            logger.error("Cell {} does not exist", cellId);
            throw new IllegalArgumentException("Cell does not exist");
        }
        Object value = this.cells.get(cellId);
        if (isFormula(value)) {
            if (formulas.get(cellId) == null) {
                logger.error("Formula does not exist for cell {}", cellId);
                throw new IllegalArgumentException("Formula does not exist");
            }
            try {
                return evaluateExpression(formulas.get(cellId).substring(1), cellId);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid formula in cell {}: {}", cellId, e.getMessage());
                throw new IllegalArgumentException("Invalid formula in cell " + cellId + ": " + e.getMessage(), e);
            }
        }
        return value;
    }

    /**
     * Checks whether a given value is a formula.
     * A formula is defined as a string that starts with '='.
     * @param value the value to check.
     * @return true if the value is a formula, false otherwise.
     */
    private boolean isFormula(Object value) {
        return value instanceof String && ((String) value).charAt(0) == '=';
    }

    /**
     * Evaluates a given mathematical expression.
     * @param expression the expression to evaluate.
     * @param currentCellId The cell ID where the expression is defined.
     * @return the result of the evaluation.
     * @throws IllegalArgumentException if the expression is invalid.
     */
    private double evaluateExpression(String expression, String currentCellId) {
        try {
            double result = prepareExpression(expression, currentCellId);
            logger.debug("Result of the expression {}: {}", expression, result);
            return result;
        } catch (NumberFormatException e) {
            logger.error("Invalid expression: Invalid number format", e);
            throw new IllegalArgumentException("Invalid expression: Invalid number format", e);
        } catch (ArithmeticException e) {
            logger.error("Invalid expression: Arithmetic error", e);
            throw new IllegalArgumentException("Invalid expression: Arithmetic error", e);
        } catch (Exception e) {
            logger.error("Invalid expression: " + e.getMessage(), e);
            throw new IllegalArgumentException("Invalid expression: " + e.getMessage(), e);
        }
    }

    /**
     * Prepares an expression for evaluation by pre-processing it, validating its syntax, replace cell IDs with values and then calculating its result.
     * @param expression the expression to prepare.
     * @param currentCellId The cell ID where the expression is defined.
     * @return the result of the evaluation.
     */
    private double prepareExpression(String expression, String currentCellId) {
        String processedExpression = preprocessExpression(expression, currentCellId);
        validateExpressionSyntax(processedExpression);
        return calculateExpression(processedExpression);
    }

    /**
     * Pre-processes an expression by replacing cell IDs with their corresponding values.
     * Checks for circular dependencies.
     * @param expression the expression to pre-process.
     * @param currentCellId The cell ID where the expression is defined.
     * @return the pre-processed expression with cell IDs replaced by their values.
     * @throws IllegalArgumentException if a circular reference is detected.
     */
    private String preprocessExpression(String expression, String currentCellId) {
        for (String cellId : this.cells.keySet()) {
            if (expression.contains(cellId)) {
                if (dependencies.getOrDefault(cellId, new HashSet<>()).contains(currentCellId)) {
                    logger.error("Circular reference detected in expression: {}", expression);
                    throw new IllegalArgumentException("Circular reference detected");
                }
                Object value = getCellValue(cellId);
                expression = expression.replaceAll("\\b" + cellId + "\\b", value.toString());
            }
        }
        return expression;
    }

    /**
     * Validates the syntax of a mathematical expression.
     * @param expression the expression to validate.
     * @throws IllegalArgumentException if the syntax is invalid.
     */
    private void validateExpressionSyntax(String expression) {
        Expression e = new Expression(expression);
        if (!e.checkSyntax()) {
            logger.error("Invalid syntax in formula: {}. Error: {}", expression, e.getErrorMessage());
            throw new IllegalArgumentException("Invalid syntax in formula: " + e.getErrorMessage());
        }
    }

    /**
     * Calculates the result of a mathematical expression.
     * @param expression the expression to calculate.
     * @return the result of the calculation.
     * @throws IllegalArgumentException if the calculation results in NaN (Not a Number).
     */
    private double calculateExpression(String expression) {
        double result = new Expression(expression).calculate();
        if (Double.isNaN(result)) {
            logger.error("Error in calculations, resulted in NaN. Expression: {}", expression);
            throw new IllegalArgumentException("Error in calculations, resulted in NaN");
        }
        return result;
    }

    /**
     * Undoes the last action.
     * This operation can revert both a cell value setting and a cell value deletion.
     */
    public void undo() {
        if (history.isEmpty()) {
            return;
        }
        Cell previousCell = history.peek();
        future.push(new Cell(previousCell.getId(), cells.get(previousCell.getId())));
        cells.remove(previousCell.getId());
        formulas.remove(previousCell.getId());
        if (previousCell.getValue() != null) {
            cells.put(previousCell.getId(), previousCell.getValue());
            if (isFormula(previousCell.getValue())) {
                updateDependencies(previousCell.getId(), (String) previousCell.getValue());
                formulas.put(previousCell.getId(), (String) previousCell.getValue());
            }
        }
        history.pop();
    }

    /**
     * Redoes the last undone action.
     * This operation can revert both a cell value setting and a cell value deletion.
     */
    public void redo() {
        if (future.isEmpty()) {
            logger.warn("Nothing to redo");
            return;
        }
        Cell cell = future.pop();
        history.push(cell);
        cells.put(cell.getId(), cell.getValue());
        if (isFormula(cell.getValue())) {
            formulas.put(cell.getId(), (String) cell.getValue());
        }
    }

    /**
     * The Cell class represents a cell in the spreadsheet.
     * It holds the cell identifier and its value.
     */
    static class Cell {
        private final String id;
        private final Object value;

        public Cell(String id, Object value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }
        public Object getValue() {
            return value;
        }
    }
}