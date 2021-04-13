import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.util.stream.Collectors;



public class App {
    private static String path = "C:\\Users\\azureuser\\Documents\\code\\nci\\hdsdev\\src\\employees_data.csv";
    public static void main(String[] args) throws Exception {
    
        File csvFile = new File(path);
        if (csvFile.isFile()) {
            System.out.println("Database OK.");

            Scanner reader = new Scanner(System.in);

            System.out.print("Please, enter [1] to search for an associate, [2] to add a new entry into the database, and [3] to measure sorting performance: ");
            String action = reader.next();

            switch (action) {
                case "1":
                    searchEmployees();
                    break;
                case "2":
                    addEmployee();
                    break;
                case "3":
                    timeComplexityCalcs();
                    break;
                default:
                    System.out.print("Invalid entry. Quitting now.");
                    break;
            }
            reader.close();
        }
    }

    private static void addEmployee() throws Exception
    {
        Employee newEmp = new Employee();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Scanner reader = new Scanner(System.in);

        // business rules validation
        try {
            System.out.print("First name: ");
            String first_name = reader.nextLine().toUpperCase();
            
            if (first_name.matches("[0-9]+") || first_name.length() <= 0)
                throw new Exception("First name is mandatory and cannot be composed by numbers only.");
            
            newEmp.setFirst_name(first_name);
            
            System.out.print("Last name: ");
            String last_name = reader.nextLine().toUpperCase();

            if (last_name.matches("[0-9]+") || last_name.length() <= 0)
                throw new Exception("Last name is mandatory and cannot be composed by numbers only.");

            newEmp.setLast_name(last_name);
    
            System.out.print("Birth date (e.g. 2000-12-31): ");
            String dob = reader.nextLine().toUpperCase();

            if (dob.length() <= 0)
                throw new Exception("Date of birth requires an specific format and cannot be left blank.");

            LocalDate date = LocalDate.parse(dob);
    
            if (date.isBefore(LocalDate.parse("1950-01-01")))
                throw new Exception("Associates who were born before 1950 cannot get registered.");
    
            long age = LocalDate.from(date).until(LocalDate.now(), ChronoUnit.YEARS);
            if (age < 18)
                throw new Exception("Only associates over 18 are allowed in this platform.");
    
            newEmp.setBirth_date(date.format(formatter));
    
            System.out.print("Gender: ");
            String gender = reader.nextLine().toUpperCase();
            newEmp.setGender(gender);
    
            reader.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
        finally
        {
            reader.close();
        }
        
        try {
            FileWriter pw = new FileWriter(path, true);
            
            

            List<Employee> employees = readEmployeesFromCSV();
            int lastId = Integer.parseInt(employees.get(employees.size() - 1).getEmp_no());
        
            String newLine = String.format("\n\"%1$s\",\"%2$s\",\"%3$s\",\"%4$s\",\"%5$s\",\"%6$s\"",
            (++lastId),
            newEmp.getBirth_date(),
            newEmp.getFirst_name(),
            newEmp.getLast_name(),
            newEmp.getGender(),
            LocalDate.now().format(formatter));

            pw.append(newLine);
            pw.close();

            System.out.println("Associate has been successfully registered.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void timeComplexityCalcs()
    {
        int[] nums = {10, 100, 1000, 5000, 10000};

        for (int i : nums) {
            List<Employee> rawDataset = readEmployeesFromCSV();
            Instant start = Instant.now();
            sortEmployees(rawDataset.subList(0,i));
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            System.out.println("Time taken for " + i + " rows is " + timeElapsed.toMillis() + " milliseconds.");
        }
    }

    private static void searchEmployees()
    {
        Scanner reader = new Scanner(System.in);
        System.out.print("Please, enter the employee's first name: ");
        String n = reader.next().toUpperCase();
        reader.close();
        
        List<Employee> data = sortEmployees(readEmployeesFromCSV());
        
        List<Employee> filtered = data.stream()
            .filter(a -> a.getFirst_name().toUpperCase().contains(n))
            .collect(Collectors.toList());

            if (filtered.size() == 0)
            {
                System.out.print("Not an employee!");
            }
            else
            {
                System.out.println("The platform has found " + filtered.size() + " employees in the database.");

                for (Employee employee : filtered) {
                    System.out.println(String.format("%2$s, %1$s", employee.getFirst_name().toUpperCase(), employee.getLast_name().toUpperCase()));
                }
            }        
    }

    private static List<Employee> sortEmployees(List<Employee> raw)
    {
        Collections.sort(raw, new Comparator<Employee>(){
            @Override
            public int compare(final Employee object1, final Employee object2) {
                return object1.getFirst_name().compareTo(object2.getFirst_name());
            }
        });
        return (raw);
    }

    private static List<Employee> readEmployeesFromCSV() {
        
        List<Employee> employees = new ArrayList<>();
        
        Path pathToFile = Paths.get(path);
         try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) { 
             
            String line = br.readLine();
            int iteration = 0;

            while (line != null) {
                if (iteration > 0)
                {
                    line = line.replace("\"", "");
                    String[] attributes = line.split(",");
                    Employee employee = new Employee(attributes[0], attributes[1], attributes[2], attributes[3], attributes[4], attributes[5]);
                    employees.add(employee);
                }
                line = br.readLine();
                iteration++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return employees;
    }
}

class Employee {
    public Employee()
    {

    }

    public Employee(String emp_no, String birth_date, String first_name, String last_name, String gender, String hire_date) {
        this.emp_no = emp_no;
        this.birth_date = birth_date;
        this.first_name = first_name;
        this.last_name = last_name;
        this.gender = gender;
        this.hire_date = hire_date;
    }
    
    private String emp_no;

    public String getEmp_no() {
        return this.emp_no;
    }

    public void setEmp_no(String emp_no) {
        this.emp_no = emp_no;
    }

    public String getBirth_date() {
        return this.birth_date;
    }

    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    public String getFirst_name() {
        return this.first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return this.last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHire_date() {
        return this.hire_date;
    }

    public void setHire_date(String hire_date) {
        this.hire_date = hire_date;
    }
    private String birth_date;
    private String first_name;
    private String last_name;
    private String gender;
    private String hire_date;
}
