package com.orkes.spreadsheet;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class SpreadsheetTest {
    private Spreadsheet spreadsheet;

    @Before
    public void setup() {
        spreadsheet = new Spreadsheet();
    }

    @Test
    public void testCellValueSetAndGet() {
        spreadsheet.setCellValue("A1", 13);
        assertEquals(13, spreadsheet.getCellValue("A1"));
    }

    @Test
    public void testCellAddition() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 20);
        spreadsheet.setCellValue("A3", "=A1+A2");
        assertEquals(30.0, spreadsheet.getCellValue("A3"));
    }

    @Test
    public void testCellAdditionMultiple() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 20);
        spreadsheet.setCellValue("A3", "=A1+A2");
        spreadsheet.setCellValue("A4", "=A1+A2+A3");
        assertEquals(60.0, spreadsheet.getCellValue("A4"));
    }

    @Test
    public void testCircularDependency() {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setCellValue("A1", "=A2");
        try {
            spreadsheet.setCellValue("A2", "=A1");
            fail("IllegalArgumentException exception not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Circular reference detected", e.getMessage());
        }
    }

    @Test
    public void testNonExistentCell() {
        assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.getCellValue("B1");
        });
    }

    @Test
    public void testInvalidCellId() {
        assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.setCellValue(null, 10);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.setCellValue("", 10);
        });
    }

    @Test
    public void testUndo() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 20);
        spreadsheet.setCellValue("A3", "=A1+A2");
        spreadsheet.undo();
        assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.getCellValue("A3");
        });
    }

    @Test
    public void testCellHistory() {
        spreadsheet.setCellValue("A1", 1);
        spreadsheet.setCellValue("A1", 2);
        spreadsheet.undo();
        assertEquals(1, spreadsheet.getCellValue("A1"));
    }

    @Test
    public void testRedo() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 20);
        spreadsheet.setCellValue("A3", "=A1+A2");
        spreadsheet.undo();
        spreadsheet.redo();
        assertEquals(30.0, spreadsheet.getCellValue("A3"));
    }

    @Test
    public void testNonIntegerValues() {
        spreadsheet.setCellValue("A1", 10.5);
        spreadsheet.setCellValue("A2", 20.5);
        spreadsheet.setCellValue("A3", "=A1+A2");
        assertEquals(31.0, spreadsheet.getCellValue("A3"));
    }

    @Test
    public void testFormulaPreservation() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 20);
        spreadsheet.setCellValue("A3", "=A1+A2");
        spreadsheet.undo();
        spreadsheet.redo();
        spreadsheet.setCellValue("A1", 5);
        assertEquals(25.0, spreadsheet.getCellValue("A3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyCellId() {
        spreadsheet.setCellValue("", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberCellId() {
        spreadsheet.setCellValue("123", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCellId() {
        spreadsheet.setCellValue(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidExpression() {
        spreadsheet.setCellValue("A1", "=B1 + ");
        spreadsheet.getCellValue("A1");
    }

    @Test
    public void testFormulaCalculation() {
        spreadsheet.setCellValue("B1", 2);
        spreadsheet.setCellValue("A1", "=B1 + 2");
        assertEquals(4.0, spreadsheet.getCellValue("A1"));
    }

    @Test
    public void testMultiplication() {
        spreadsheet.setCellValue("A1", 3);
        spreadsheet.setCellValue("A2", 4);
        spreadsheet.setCellValue("B1", "=A1 * A2");
        assertEquals(12.0, spreadsheet.getCellValue("B1"));
    }

    @Test
    public void testDivision() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 2);
        spreadsheet.setCellValue("B1", "=A1 / A2");
        assertEquals(5.0, spreadsheet.getCellValue("B1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivisionByZero() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 0);
        spreadsheet.setCellValue("B1", "=A1 / A2");
        spreadsheet.getCellValue("B1");
    }

    @Test
    public void testSubtraction() {
        spreadsheet.setCellValue("A1", 5);
        spreadsheet.setCellValue("A2", 2);
        spreadsheet.setCellValue("B1", "=A1 - A2");
        assertEquals(3.0, spreadsheet.getCellValue("B1"));
    }

    @Test
    public void testMultipleOperations() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 2);
        spreadsheet.setCellValue("A3", 3);
        spreadsheet.setCellValue("B1", "=A1 * A2 / A3");
        assertEquals((10.0 * 2.0 / 3.0), spreadsheet.getCellValue("B1"));
    }

    @Test
    public void testStringCellValue() {
        spreadsheet.setCellValue("A1", "Hello World");
        assertEquals("Hello World", spreadsheet.getCellValue("A1"));
    }

    @Test
    public void testUndoWithNoHistory() {
        try {
            spreadsheet.undo();
        } catch (Exception e) {
            fail("Exception was not expected");
        }
    }

    @Test
    public void testRedoWithNoFuture() {
        try {
            spreadsheet.redo();
        } catch (Exception e) {
            fail("Exception was not expected");
        }
    }

    @Test
    public void testMultipleUndoRedo() {
        spreadsheet.setCellValue("A1", 1);
        spreadsheet.setCellValue("A1", 2);
        spreadsheet.setCellValue("A1", 3);
        spreadsheet.undo();
        spreadsheet.undo();
        assertEquals(1, spreadsheet.getCellValue("A1"));
        spreadsheet.redo();
        assertEquals(2, spreadsheet.getCellValue("A1"));
    }

    @Test
    public void testFormulaWithMultipleSpaces() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", 20);
        spreadsheet.setCellValue("A3", "= A1  +   A2");
        assertEquals(30.0, spreadsheet.getCellValue("A3"));
    }

    @Test
    public void testUndoRedoWithFormula() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", "=A1*2");
        spreadsheet.setCellValue("A1", 20);
        spreadsheet.undo();
        assertEquals(20.0, spreadsheet.getCellValue("A2"));
        spreadsheet.redo();
        assertEquals(40.0, spreadsheet.getCellValue("A2"));
    }

    @Test
    public void testCellReferenceUpdate() {
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", "=A1");
        spreadsheet.setCellValue("A1", 20);
        assertEquals(20.0, spreadsheet.getCellValue("A2"));
    }
}
