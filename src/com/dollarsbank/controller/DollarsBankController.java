package com.dollarsbank.controller;

import java.util.Map;
import java.util.Scanner;

import com.dollarsbank.model.Account;
import com.dollarsbank.model.Customer;
import com.dollarsbank.utility.ColorsUtility;
import com.dollarsbank.utility.ConsolePrinterUtility;
import com.dollarsbank.utility.DataGeneratorStubUtil;
import com.dollarsbank.utility.FileStorageUtility;
import com.dollarsbank.utility.StringUtil;
import com.dollarsbank.utility.ValidationUtility;

public class DollarsBankController {

    private static final int GUEST_MENU_NUM = 3;
    private static final int CUSTOMER_MENU_NUM = 6;
    
    // Collection of customers
    // Will import saved user data, or start with an empty map if no data exists
    private Map<String, Customer> customers = FileStorageUtility.importData();

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

    // GUEST LOGIC

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
        
        // Post customer's inital deposit
        DataGeneratorStubUtil.postTransaction(customer, DataGeneratorStubUtil.transactionStub("Initial Deposit Amount", initialDeposit, customer.getAccount()));

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

    public boolean exitProgram(Scanner sc) {
        // Confirm whether the user is done with the program
        boolean confirm = ValidationUtility.getConfirmation(sc, "Are you sure you want to quit the program?");

        // User confirms intent to exit program
        if (confirm) {
            ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS,
                "\nThank you for banking with Dollars Bank.\nHave a nice day!");
        }

        // Save user data to data file
        FileStorageUtility.exportData(customers);

        // Return choice
        return confirm;
    }

    // CUSTOMER LOGIC

    // Deposit money into user's account
    public void makeDeposit(Scanner sc) {
        // Prompt user for deposit amount
        double deposit = Double.parseDouble(ValidationUtility.getValidatedStrInput(sc, "Deposit Amount:", StringUtil.MONETARY));

        // Make the deposit (increase the accounts balance)
        Account customerAcct = currUser.getAccount();
        customerAcct.setBalance(customerAcct.getBalance() + deposit);

        // Post the transaction to the user's account
        String transaction = DataGeneratorStubUtil.transactionStub("Deposit", deposit, customerAcct);
        DataGeneratorStubUtil.postTransaction(currUser, transaction);

        ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS, "\n" + transaction);

    }

    // Withdraw money from user's account
    public void makeWithdrawal(Scanner sc) {
        double withdrawal = Double.parseDouble(ValidationUtility.getValidatedStrInput(sc, "Withdrawal Amount:", StringUtil.MONETARY));;

        // If withdrawal amount is greater than the available balance
        if (ValidationUtility.checkForSsufficientFunds(currUser.getAccount(), withdrawal)) {
            Account customerAcct = currUser.getAccount();
            customerAcct.setBalance(customerAcct.getBalance() - withdrawal);

            String transaction = DataGeneratorStubUtil.transactionStub("Withdrawal", withdrawal, customerAcct);
            DataGeneratorStubUtil.postTransaction(currUser, transaction);

            ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS, "\n" + transaction);
        }
        
    }

    // Transfer funds to another account
    public void transferFunds(Scanner sc) {
        boolean valid = false, confirmation;
        String transferee = "";
        Customer destination = null;
        double transferAmt = 0;

        // Only applicable if there is more than one account in the system
        if (customers.size() > 1) {

            // Ask for which account funds should be transferred to
            while (!valid) {
                // Display the accounts in a table format
                getAccounts(currUser);

                ConsolePrinterUtility.askForInput("\nEnter the username of the person you would like to transfer funds to:");
                transferee = sc.nextLine();

                // If the specified user is found
                if (customers.containsKey(transferee)) {
                    valid = true;
                    destination = customers.get(transferee);

                    // If the selected destination is the user's own account
                    if (destination == currUser) {
                        valid = false;
                        ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_ERROR, "ERR: Cannot transfer money to yourself!");
                    }

                // User not found
                } else {
                    ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_ERROR, "ERR: No such user exists.");
                }

            }

            // Prompt for amount to be transferred
            do {
                transferAmt = Double.parseDouble(ValidationUtility.getValidatedStrInput(sc, "Enter the amount you would like to transfer:", StringUtil.MONETARY));

                // Check if user has enough funds to make the transfer
                valid = ValidationUtility.checkForSsufficientFunds(currUser.getAccount(), transferAmt);
            } while (!valid);

            // Confirm that user wants to go through with transfer
            confirmation = ValidationUtility.getConfirmation(sc,
                String.format("Confirm transfer of $%.2f to %s (%s) [%s]?",
                    transferAmt, 
                    destination.getFullName(), 
                    transferee, 
                    destination.getAccount().getAccountId())
            );

            if (confirmation) {
                // Perform the transfer
                // Remove the transfer amount from the user's account
                currUser.getAccount().setBalance(currUser.getAccount().getBalance() - transferAmt);

                String transferTransaction = DataGeneratorStubUtil.transferToStub(transferAmt, currUser.getAccount(), destination);

                // Post transaction to user's account
                DataGeneratorStubUtil.postTransaction(currUser, transferTransaction);

                // Add the funds to the destination account
                destination.getAccount().setBalance(destination.getAccount().getBalance() + transferAmt);

                // Post transaction to the destination account
                DataGeneratorStubUtil.postTransaction(destination, DataGeneratorStubUtil.transferFromStub(transferAmt, currUser, destination.getAccount()));

                ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS, transferTransaction);
            }

        // There are no other users in the system
        } else {

            ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_ERROR, "Uh-oh.");
            ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_SYS, "There exists no other users to transfer funds to.");

        }
        
    }

    // Customer's 5 recent transaction
    public void printRecentTransactions() {
        ConsolePrinterUtility.printRecentTransHeader();

        for (String transaction : currUser.getTransactions()) {
            System.out.println(transaction + "\n");
        }

    }

    // Displays the customers information
    public void printCustomerInformation() {
        String defaultDisplayFormat = "%s%-16s %s%s%n";

        ConsolePrinterUtility.printCustomerInfoHeader();
        System.out.printf("%s%-16s %s%s %s%n", ConsolePrinterUtility.MSG_SYS, "Name:", ConsolePrinterUtility.RESET_TEXT, currUser.getFName(), currUser.getLName());
        System.out.printf(defaultDisplayFormat, ConsolePrinterUtility.MSG_SYS, "Username:", ConsolePrinterUtility.RESET_TEXT, currUser.getUsername());
        System.out.printf(defaultDisplayFormat, ConsolePrinterUtility.MSG_SYS, "Address:", ConsolePrinterUtility.RESET_TEXT, currUser.getAddress());
        System.out.printf(defaultDisplayFormat, ConsolePrinterUtility.MSG_SYS, "Email:", ConsolePrinterUtility.RESET_TEXT, currUser.getEmail());
        System.out.printf(defaultDisplayFormat, ConsolePrinterUtility.MSG_SYS, "Contact Number:", ConsolePrinterUtility.RESET_TEXT, currUser.getPhoneNumber());
        System.out.printf(defaultDisplayFormat, ConsolePrinterUtility.MSG_SYS, "Account Id:", ConsolePrinterUtility.RESET_TEXT, currUser.getAccount().getAccountId());
        System.out.printf("%s%-16s %s%.2f%n", ConsolePrinterUtility.MSG_SYS, "Account Balance:", ConsolePrinterUtility.RESET_TEXT, currUser.getAccount().getBalance());
    }

    // Sign the current user out
    public void signCustomerOut(Scanner sc) {
        // Confirm whether the user wishes to sign out
        boolean confirm = ValidationUtility.getConfirmation(sc, "Are you sure you want to sign out?");

        // User confirms intent to sign out
        if (confirm) {
            setCurrUser(null);
            System.out.println(ConsolePrinterUtility.MSG_SYS + "\nSigning out..." + ConsolePrinterUtility.RESET_TEXT);
        }
    }

    // Retrieve list of accounts
    private void getAccounts(Customer current) {
        String format = "%s  %-15s  %s";
        String color;

        // Header
        ConsolePrinterUtility.printMessage(ConsolePrinterUtility.MSG_HEADER, "\n" + String.format(format, "Account", "Username", "Customer"));

        // Print list of accounts in the system
        for (Customer customer : customers.values()) {
            // Use red for user's account, green for the other user accounts
            color = customer == currUser ? ColorsUtility.ANSI_RED.value : ConsolePrinterUtility.RESET_TEXT;
            ConsolePrinterUtility.printMessage(color, String.format(format, customer.getAccount().getAccountId(), customer.getUsername(), customer.getFullName()));
        }
    }


}
