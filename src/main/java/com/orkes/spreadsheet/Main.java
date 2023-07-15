package com.orkes.spreadsheet;

public class Main {
    public static void main(String[] args) {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setCellValue("A1", 13);
        spreadsheet.setCellValue("A2", 14);
        spreadsheet.setCellValue("A3", "=A1+A2");
        spreadsheet.setCellValue("A4", "=A1+A2+A3");
        System.out.println("A1: " + spreadsheet.getCellValue("A1"));
        System.out.println("A2: " + spreadsheet.getCellValue("A2"));
        System.out.println("A3: " + spreadsheet.getCellValue("A3"));
        System.out.println("A4: " + spreadsheet.getCellValue("A4"));
        spreadsheet.setCellValue("B1", "Hello");
        spreadsheet.setCellValue("C1", "=A1 + 10");
        System.out.println("B1: " + spreadsheet.getCellValue("B1"));
        System.out.println("C1: " + spreadsheet.getCellValue("C1"));
        spreadsheet.undo();
        try {
            System.out.println("C1 after undo: " + spreadsheet.getCellValue("C1"));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        spreadsheet.redo();
        System.out.println("C1 after redo: " + spreadsheet.getCellValue("C1"));
        spreadsheet.setCellValue("C2", 1.5);
        System.out.println("C2: " + spreadsheet.getCellValue("C2"));
        spreadsheet.setCellValue("A7", "=A4-A1");
        spreadsheet.setCellValue("A8", "=A1*A2");
        spreadsheet.setCellValue("A9", "=A4/A3");
        System.out.println("A7: " + spreadsheet.getCellValue("A7"));
        System.out.println("A8: " + spreadsheet.getCellValue("A8"));
        System.out.println("A9: " + spreadsheet.getCellValue("A9"));
        spreadsheet.setCellValue("A12", "=A2+A1");
        spreadsheet.setCellValue("A12", "=A2-A1");
        spreadsheet.undo();
        System.out.println("A12: " + spreadsheet.getCellValue("A12"));
        spreadsheet.redo();
        System.out.println("A12: " + spreadsheet.getCellValue("A12"));
        spreadsheet.setCellValue("A1", 10);
        spreadsheet.setCellValue("A2", "=A1");
        spreadsheet.setCellValue("A1", 20);
        try {
            System.out.println("A2: " + spreadsheet.getCellValue("A2"));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        try {
            spreadsheet.setCellValue("121", "10");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
