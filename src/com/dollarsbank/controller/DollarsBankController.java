package com.dollarsbank.controller;

import java.util.HashMap;
import java.util.Scanner;

import com.dollarsbank.model.Account;
import com.dollarsbank.model.Customer;
import com.dollarsbank.utility.ConsolePrinterUtility;
import com.dollarsbank.utility.StringUtil;
import com.dollarsbank.utility.ValidationUtility;

public class DollarsBankController {

    private static final int GUEST_MENU_NUM = 3;
    private static final int CUSTOMER_MENU_NUM = 6;
    
    // Collection of customers
    private HashMap<String, Customer> customers = new HashMap<String, Customer>();

    // Current logged in user
    private Customer currUser;

    private int numMenuOptions;

    public DollarsBankController() {
        this.currUser = null;
        this.numMenuOptions = GUEST_MENU_NUM;
    }

    public DollarsBankController(Customer user) {
        this.currUser = user;
        this.numMenuOptions = CUSTOMER_MENU_NUM;
    }

    // Check what user is logged in
    public Customer getCurrUser() {
        return this.currUser;
    }

    // Control whether a user is logged in or not
    public void setCurrUser(Customer currUser) {
        this.currUser = currUser;
        
        if (currUser != null) {
            setNumMenuOptions(CUSTOMER_MENU_NUM);
        } else {
            setNumMenuOptions(GUEST_MENU_NUM);
        }

    }

    public int getNumMenuOptions() {
        return this.numMenuOptions;
    }

    public void setNumMenuOptions(int numMenuOptions) {
        this.numMenuOptions = numMenuOptions;
    }

    // Create a new account
    public void createNewCustomer(Scanner sc) {
        boolean isAvailable;

        // Variables needed to create a new account
        String fName, lName, address, email, number, username, password;
        double initialDeposit;

        // Print overall instructions
        ConsolePrinterUtility.printNewAcctHeader();
        
        // User's first name
        fName = ValidationUtility.getValidatedStrInput(sc, "Customer First Name:", StringUtil.NAME);

        // User's last name
        lName = ValidationUtility.getValidatedStrInput(sc, "Customer Last Name:", StringUtil.NAME);

        // User's address
        address = ValidationUtility.getValidatedStrInput(sc, "Customer Address:", StringUtil.ANY);

        // User's email address
        email = ValidationUtility.getValidatedStrInput(sc, "Customer Email:", StringUtil.EMAIL);
        
        // User's phone number
        number = ValidationUtility.getValidatedStrInput(sc, "Customer Contact Number (10-digits):", StringUtil.NUMBER);

        // User's username
        do {
            username = ValidationUtility.getValidatedStrInput(sc, "Username:", StringUtil.USERNAME);

            // Check if the username has already been taken
            isAvailable = !customers.containsKey(username);

            // If the username is already taken
            if (!isAvailable) {
                // Notify user of unavailability
                ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_ERROR, "ERR: Username is unavailable. Try another.");
            }
        } while (!isAvailable);
        
        // User's password
        password = ValidationUtility.getValidatedStrInput(sc, "Password (Min: 8 characters; Must include: lower, upper & special):", StringUtil.PASSWORD);

        // Initial deposit amount
        initialDeposit = Double.parseDouble(ValidationUtility.getValidatedStrInput(sc, "Initial Deposity Amount:", StringUtil.MONETARY));

        // Create a new account for the customer
        Customer customer = new Customer(username, password, fName, lName, address, number, email, new Account(initialDeposit));

        // Store customer account in memory
        customers.put(customer.getUsername(), customer);

        // Notify user of successful creation
        ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS, "Account has been successfully created.");

    }

    // Sign user in
    public void signCustomerIn(Scanner sc) {
        boolean confirm = false;
        String username, password;

        while (!confirm) {
            ConsolePrinterUtility.printLoginHeader();
            
            // Username
            ConsolePrinterUtility.askForInput("Username:");
            username = sc.nextLine();

            // No such existing user
            if (!customers.containsKey(username)) {
                ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_ERROR, "ERR: No such user exists.");

                // Ask if user would like to try again
                confirm = ValidationUtility.getConfirmation(sc, "Would you like to try again?");

                // Try again
                if (confirm) {

                    // Reset boolean to false
                    confirm = false;

                    // Go to next iteration
                    continue;

                // Don't want to try again
                } else {

                    // Breaks out of loop and returns to the menu
                    break;
                }

            } else {
                // Password
                ConsolePrinterUtility.askForInput("Password:");
                password = sc.nextLine();

                // If password entered correctly
                if (password.equals(customers.get(username).getPassword())) {
                    confirm = true;
                    setCurrUser(customers.get(username));

                // Password is incorrect
                } else {
                    confirm = false;
                    ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_ERROR, "Invalid Credentials. Try Again!");
                }
            }
        }
    }

    // Sign the current user out
    public void signCustomerOut(Scanner sc) {
        // Confirm whether the user wishes to sign out
        boolean confirm = ValidationUtility.getConfirmation(sc, "Are you sure you want to sign out?");

        // User confirms intent to sign out
        if (confirm) {
            setCurrUser(null);
            System.out.println(ConsolePrinterUtility.MSG_SYS + "Signing out..." + ConsolePrinterUtility.RESET_TEXT);
        }
    }

    public boolean exitProgram(Scanner sc) {
        // Confirm whether the user is done with the program
        boolean confirm = ValidationUtility.getConfirmation(sc, "Are you sure you want to quit the program?");

        // User confirms intent to exit program
        if (confirm) {
            ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS,
                "\nThank you for banking with Dollars Bank.\nHave a nice day!");
        }

        // Return choice
        return confirm;
    }


}
