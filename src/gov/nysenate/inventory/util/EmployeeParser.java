package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gov.nysenate.inventory.model.Employee;

import java.util.ArrayList;
import java.util.List;

public class EmployeeParser {
    private static final Gson gson = new Gson();

    public static Employee parseEmployee(String json) {
        Employee emp;
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        emp = gson.fromJson(obj, Employee.class);

        return emp;
    }

    public static List<Employee> parseMultipleEmployees(String json) {
        List<Employee> employees = new ArrayList<Employee>();
        JsonParser parser = new JsonParser();
        JsonArray obj = parser.parse(json).getAsJsonArray();
        for (int i = 0; i < obj.size(); i++) {
            employees.add(parseEmployee(obj.get(i).toString()));
        }

        return employees;
    }
}

