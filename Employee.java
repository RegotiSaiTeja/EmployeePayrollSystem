import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Locale;

public class Employee {
    private int empId;
    private String name;
    private double basic;

    private static String companyName = "Example Enterprises";
    private static double taxRate = 10.0;
    private static double hraPercent = 20.0;
    private static double daPercent = 10.0;
    private static int employeeCount = 0;

    // All constructor paths reach this constructor exactly once.
    public Employee() {
        employeeCount++;
    }

    public Employee(int empId, String name) {
        this();
        setEmpId(empId);
        setName(name);
    }

    public Employee(int empId, String name, double basic)
            throws InvalidSalaryException {
        this(empId, name);
        setBasic(basic);
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBasic() {
        return basic;
    }

    public void setBasic(double basic) throws InvalidSalaryException {
        validateNonNegativeSalary(basic, "Basic salary");
        this.basic = basic;
    }

    public static void validateNonNegativeSalary(double value, String label)
            throws InvalidSalaryException {
        if (!Double.isFinite(value) || value < 0) {
            throw new InvalidSalaryException(label + " must be a finite non-negative number: " + value, value);
        }
    }

    public static String getCompanyName() {
        return companyName;
    }

    public static void setCompanyName(String companyName) {
        Employee.companyName = companyName;
    }

    public static double getTaxRate() {
        return taxRate;
    }

    public static void setTaxRate(double rate) {
        if (!Double.isFinite(rate) || rate < 0 || rate > 100) {
            throw new InvalidTaxRateException("Tax rate must be between 0 and 100: " + rate);
        }
        taxRate = rate;
    }

    public static double getHraPercent() {
        return hraPercent;
    }

    public static double getDaPercent() {
        return daPercent;
    }

    public static int getEmployeeCount() {
        return employeeCount;
    }

    public double calculateGross() {
        return basic + (basic * hraPercent / 100.0) + (basic * daPercent / 100.0);
    }

    public double calculateNet() {
        double gross = calculateGross();
        return gross - (gross * taxRate / 100.0);
    }

    public double calculateNet(double bonus) throws InvalidSalaryException {
        validateNonNegativeSalary(bonus, "Bonus");
        double taxable = calculateGross() + bonus;
        return taxable - (taxable * taxRate / 100.0);
    }

    public int compareSalary(Employee other) {
        return calculateNet() >= other.calculateNet() ? empId : other.empId;
    }

    public void displayPayroll(BufferedWriter bw) throws IOException {
        bw.write("Employee ID: " + empId);
        bw.newLine();
        bw.write("Name: " + name);
        bw.newLine();
        bw.write("Basic Salary: " + money(basic));
        bw.newLine();
        bw.write("Gross Salary: " + money(calculateGross()));
        bw.newLine();
        bw.write("Net Salary (without bonus): " + money(calculateNet()));
        bw.newLine();
    }

    public static double totExpensesForSalaryPerMonth(Employee[] list) {
        double total = 0.0;
        for (Employee employee : list) {
            if (employee != null) {
                total += employee.calculateNet();
            }
        }
        return total;
    }

    protected static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
