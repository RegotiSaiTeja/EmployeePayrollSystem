import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static final String INPUT_PATH = "input/employees.txt";
    private static final String REPORT_PATH = "output/payroll_report.txt";
    private static final String ERROR_PATH = "output/errors.txt";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static class ParsedRecord {
        private final Employee employee;
        private final double bonus;

        private ParsedRecord(Employee employee, double bonus) {
            this.employee = employee;
            this.bonus = bonus;
        }
    }

    // Validate every field, including bonus, BEFORE constructing any object.
    // This guarantees rejected input does not increase employeeCount.
    private static ParsedRecord parseRecord(String line)
            throws InvalidRecordException, InvalidSalaryException, InvalidTeamSizeException {
        String[] fields = line.split(",", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        if (fields.length == 0 || (!fields[0].equals("E") && !fields[0].equals("M"))) {
            throw new InvalidRecordException("Unknown employee type code");
        }

        int expectedFields = fields[0].equals("E") ? 5 : 7;
        if (fields.length != expectedFields) {
            throw new InvalidRecordException("Expected " + expectedFields
                    + " fields but found " + fields.length);
        }

        try {
            int id = Integer.parseInt(fields[1]);
            String name = fields[2];
            if (name.isEmpty()) {
                throw new InvalidRecordException("Name cannot be empty");
            }

            double basic = Double.parseDouble(fields[3]);
            Employee.validateNonNegativeSalary(basic, "Basic salary");

            if (fields[0].equals("E")) {
                double bonus = Double.parseDouble(fields[4]);
                Employee.validateNonNegativeSalary(bonus, "Bonus");
                return new ParsedRecord(new Employee(id, name, basic), bonus);
            }

            int teamSize = Integer.parseInt(fields[4]);
            double allowance = Double.parseDouble(fields[5]);
            double bonus = Double.parseDouble(fields[6]);
            Manager.validateTeamSize(teamSize);
            Employee.validateNonNegativeSalary(allowance, "Travel allowance");
            Employee.validateNonNegativeSalary(bonus, "Bonus");
            return new ParsedRecord(new Manager(id, name, basic, teamSize, allowance), bonus);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new InvalidRecordException("Non-numeric or missing field: " + ex.getMessage(), ex);
        }
    }

    private static void logRejected(BufferedWriter errorWriter, int lineNumber,
                                    String line, Exception ex) throws IOException {
        errorWriter.write("Line " + lineNumber + " | Record: " + line
                + " | Exception: " + ex.getClass().getSimpleName()
                + " | Message: " + ex.getMessage());
        errorWriter.newLine();
    }

    private static void checkInput(File file) throws IOException, EmptyInputFileException {
        if (!file.exists()) {
            throw new FileNotFoundException("Missing input file: " + file.getPath());
        }
        if (!file.isFile()) {
            throw new FileNotFoundException("Input path is not a file: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read input file: " + file.getPath());
        }
        if (file.length() == 0) {
            throw new EmptyInputFileException("Input file is empty: " + file.getPath());
        }
    }

    private static int readExpectedCount(String header)
            throws EmptyInputFileException, InvalidRecordException {
        if (header == null || header.trim().isEmpty()) {
            throw new EmptyInputFileException("Missing record count N in input file");
        }
        try {
            int count = Integer.parseInt(header.trim());
            if (count <= 0) {
                throw new EmptyInputFileException("Record count N must be greater than zero");
            }
            return count;
        } catch (NumberFormatException ex) {
            throw new InvalidRecordException("Record count N is not a valid integer", ex);
        }
    }

    private static void generatePayroll()
            throws IOException, EmptyInputFileException, InvalidRecordException {
        File inputFile = new File(INPUT_PATH);
        checkInput(inputFile);

        File outputFolder = new File("output");
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            throw new IOException("Could not create output directory");
        }

        File reportFile = new File(REPORT_PATH);
        File errorFile = new File(ERROR_PATH);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter report = new BufferedWriter(new FileWriter(reportFile));
             BufferedWriter errors = new BufferedWriter(new FileWriter(errorFile, true))) {

            errors.write("=== Payroll run: " + LocalDateTime.now().format(DATE_FORMAT) + " ===");
            errors.newLine();

            int expected = readExpectedCount(reader.readLine());
            Employee[] employees = new Employee[expected];
            double[] bonuses = new double[expected];
            int recordsRead = 0;
            int accepted = 0;
            int rejected = 0;
            int fileLineNumber = 1;

            String line;
            while ((line = reader.readLine()) != null) {
                fileLineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                try {
                    if (recordsRead >= expected) {
                        throw new InvalidRecordException("Extra record beyond declared N=" + expected);
                    }
                    ParsedRecord parsed = parseRecord(line);
                    employees[accepted] = parsed.employee;
                    bonuses[accepted] = parsed.bonus;
                    accepted++;
                } catch (InvalidSalaryException ex) {
                    logRejected(errors, fileLineNumber, line, ex);
                    rejected++;
                } catch (InvalidTeamSizeException ex) {
                    logRejected(errors, fileLineNumber, line, ex);
                    rejected++;
                } catch (InvalidRecordException ex) {
                    logRejected(errors, fileLineNumber, line, ex);
                    rejected++;
                } finally {
                    recordsRead++;
                }
            }

            if (recordsRead < expected) {
                errors.write("Input count mismatch: N=" + expected
                        + ", actual non-comment records=" + recordsRead);
                errors.newLine();
            }

            report.write("========== " + Employee.getCompanyName() + " ==========");
            report.newLine();
            report.write("EMPLOYEE PAYROLL REPORT");
            report.newLine();
            report.write("Tax Rate: " + Employee.money(Employee.getTaxRate()) + "%"
                    + " | HRA: " + Employee.money(Employee.getHraPercent()) + "%"
                    + " | DA: " + Employee.money(Employee.getDaPercent()) + "%");
            report.newLine();
            report.newLine();

            for (int i = 0; i < employees.length; i++) {
                Employee employee = employees[i];
                if (employee == null) {
                    continue;
                }
                report.write("----------------------------------------");
                report.newLine();
                report.write("Designation: " + (employee instanceof Manager ? "Manager" : "Employee"));
                report.newLine();
                employee.displayPayroll(report); // Dynamic dispatch for Manager.
                report.write("One-time Bonus: " + Employee.money(bonuses[i]));
                report.newLine();
                report.write("Net Salary (including taxable bonus): "
                        + Employee.money(employee.calculateNet(bonuses[i])));
                report.newLine();
                report.newLine();
            }

            report.write("================ SUMMARY ================");
            report.newLine();
            if (accepted >= 2) {
                report.write("Higher net salary (first two accepted): Employee ID "
                        + employees[0].compareSalary(employees[1]));
                report.newLine();
            } else {
                report.write("Higher net salary (first two accepted): Not enough valid employees");
                report.newLine();
            }
            report.write("Total Monthly Net Salary Expense (excluding bonuses): "
                    + Employee.money(Employee.totExpensesForSalaryPerMonth(employees)));
            report.newLine();
            report.write("Final employeeCount: " + Employee.getEmployeeCount());
            report.newLine();
            report.write("Records read: " + recordsRead + ", accepted: " + accepted
                    + ", rejected: " + rejected + ".");
            report.newLine();

            report.flush();
            errors.flush();
        } catch (InvalidSalaryException ex) {
            // A bonus was prevalidated before object construction, so this is a safety net.
            throw new IOException("Unexpected invalid bonus during report generation", ex);
        }
    }

    private static boolean logFatal(Exception ex) {
        File folder = new File("output");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try (BufferedWriter errors =
                     new BufferedWriter(new FileWriter(ERROR_PATH, true))) {
            errors.write("=== Payroll run failed: "
                    + LocalDateTime.now().format(DATE_FORMAT) + " ===");
            errors.newLine();
            errors.write(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            errors.newLine();
            errors.flush();
            return true;
        } catch (IOException loggingError) {
            return false; // Main will report the log failure in its single status line.
        }
    }

    public static void main(String[] args) {
        boolean success = false;
        boolean errorLogWritten = true;
        try {
            Employee.setTaxRate(10.0);
            generatePayroll();
            success = true;
        } catch (FileNotFoundException ex) {
            errorLogWritten = logFatal(ex);
        } catch (EmptyInputFileException ex) {
            errorLogWritten = logFatal(ex);
        } catch (InvalidRecordException ex) {
            errorLogWritten = logFatal(ex);
        } catch (InvalidTaxRateException ex) {
            errorLogWritten = logFatal(ex);
        } catch (IOException ex) {
            errorLogWritten = logFatal(ex);
        }
        System.out.println(success
                ? "Processing complete. See output folder."
                : errorLogWritten
                    ? "Processing failed. See output/errors.txt."
                    : "Processing failed; the error log could not be written.");
    }
}
