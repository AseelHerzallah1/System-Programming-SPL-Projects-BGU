#pragma once
#include <string>
#include <vector>

#include "Order.h"
#include "Customer.h"
#include "Action.h"

class BaseAction;
class Volunteer;

// Warehouse responsible for Volunteers, Customers Actions, and Orders.


class WareHouse {

    public:
        WareHouse(const string &configFilePath); 
        WareHouse(const WareHouse &other); //copy constructor
        WareHouse& operator=(const WareHouse& other); //copy assignment operator
        ~WareHouse(); // destructor
        WareHouse(WareHouse&& other) noexcept; //move constructor
        WareHouse& operator=(WareHouse&& other) noexcept; //move assignment operator
        void start();
        void addOrder(Order* order);
        void addAction(BaseAction* action);
        void addCustomer(Customer* customer);
        Customer &getCustomer(int customerId) const;
        Volunteer &getVolunteer(int volunteerId) const;
        Order &getOrder(int orderId) const;
        const vector<BaseAction*> &getActions() const;
        void close();
        void open();
        vector<Order*> getPendingOrders() const;
        vector<Order*> getInProccessOrders() const;
        vector<Order*> getCompletedOrders() const;
        vector<Volunteer*> getVolunteers() const;
        vector<Customer*> getCustomers() const;
        int getOrderCounter() const;
        void increaseOrderCounter();
        int getCustomerCounter() const;
        int getVolunteerCounter() const;
        void increaseCustomerCounter();
        void increaseVolunteerCounter();
        void volunteersStep();
        void pendingOrdersStep();



    private:
        bool isOpen;
        vector<BaseAction*> actionsLog;
        vector<Volunteer*> volunteers;
        vector<Order*> pendingOrders;
        vector<Order*> inProcessOrders;
        vector<Order*> completedOrders;
        vector<Customer*> customers;
        int customerCounter = 0; //For assigning unique customer IDs
        int volunteerCounter = 0; //For assigning unique volunteer IDs
        int orderCounter = 0; 
};