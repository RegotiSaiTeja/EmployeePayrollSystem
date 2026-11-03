import java.io.BufferedWriter;
import java.io.IOException;

public class Manager extends Employee {
    private int teamSize;
    private double travelAllowance;

    private static double teamAllowancePerMember = 500.0;
    private static int incentiveTeamLimit = 5;

    public Manager() {
        super();
    }

    public Manager(int empId, String name, double basic, int teamSize,
                   double travelAllowance)
            throws InvalidSalaryException, InvalidTeamSizeException {
        super(empId, name, basic);
        setTeamSize(teamSize);
        setTravelAllowance(travelAllowance);
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) throws InvalidTeamSizeException {
        validateTeamSize(teamSize);
        this.teamSize = teamSize;
    }

    public static void validateTeamSize(int teamSize) throws InvalidTeamSizeException {
        if (teamSize < 0) {
            throw new InvalidTeamSizeException("Team size cannot be negative: " + teamSize);
        }
    }

    public double getTravelAllowance() {
        return travelAllowance;
    }

    public void setTravelAllowance(double travelAllowance) throws InvalidSalaryException {
        validateNonNegativeSalary(travelAllowance, "Travel allowance");
        this.travelAllowance = travelAllowance;
    }

    public static double getTeamAllowancePerMember() {
        return teamAllowancePerMember;
    }

    public static int getIncentiveTeamLimit() {
        return incentiveTeamLimit;
    }

    @Override
    public double calculateGross() {
        return super.calculateGross() + travelAllowance
                + (teamSize * teamAllowancePerMember);
    }

    public double calculateIncentive() {
        if (teamSize > incentiveTeamLimit) {
            return 0.02 * calculateGross() * (teamSize - incentiveTeamLimit);
        }
        return 0.0;
    }

    @Override
    public void displayPayroll(BufferedWriter bw) throws IOException {
        super.displayPayroll(bw);
        bw.write("Team Size: " + teamSize);
        bw.newLine();
        bw.write("Travel Allowance: " + money(travelAllowance));
        bw.newLine();
        bw.write("Manager Incentive (separate; not in gross/net): " + money(calculateIncentive()));
        bw.newLine();
    }
}
