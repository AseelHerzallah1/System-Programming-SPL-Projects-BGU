#include "Action.h"
#include <iostream>
#include "Order.h"
#include "WareHouse.h"
#include "Volunteer.h"
#include "Customer.h"
#include <algorithm>
#include <vector>
using namespace std;

BaseAction :: BaseAction(): errorMsg(""), status(ActionStatus :: ERROR){}

ActionStatus BaseAction :: getStatus() const{
    return status;
}

void BaseAction :: complete(){
    status = ActionStatus :: COMPLETED;
}

void BaseAction :: error(string errorMsg){
    status = ActionStatus :: ERROR;
     std::cout << "Error: " << errorMsg << std::endl;
}

string BaseAction :: getErrorMsg() const{
    return errorMsg;
}

SimulateStep :: SimulateStep(int numOfSteps) :numOfSteps(numOfSteps){}

void SimulateStep :: act(WareHouse &wareHouse){
    for(int i = 0 ; i < numOfSteps; i++){
        wareHouse.pendingOrdersStep();
        wareHouse.volunteersStep();
    } 
    complete();
    wareHouse.addAction(this);
}

std::string SimulateStep :: toString() const{
    string stringSimulateStatus = "simulateStep " + std::to_string(numOfSteps) ;
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringSimulateStatus = stringSimulateStatus + " COMPLETED\n";
    }
    else{
        stringSimulateStatus = stringSimulateStatus + " ERROR\n";
    }
    return stringSimulateStatus;
}

SimulateStep *SimulateStep :: clone() const{
    return new SimulateStep(*this);
}



AddOrder :: AddOrder(int id) : customerId(id){}

void AddOrder :: act(WareHouse &wareHouse){
    // check if customer exsits
    if(customerId < wareHouse.getCustomerCounter() && customerId >= 0){
        Customer &customer = wareHouse.getCustomer(customerId);
        int distance = customer.getCustomerDistance();
        // check if customer did not reach max orders and can make an order
        if (customer.canMakeOrder()){
            // builds a new order, set it's status to pending and adds it to the warehouse
            Order *order = new Order(wareHouse.getOrderCounter(), customerId, distance); 
            order->setStatus(OrderStatus::PENDING);
            wareHouse.addOrder(order); 
            wareHouse.increaseOrderCounter(); 
            customer.addOrder(order->getId());
            complete();
        }
        else{
            error("cannot place this order");
        }
    }
    else{
        error("cannot place this order");
    }
    wareHouse.addAction(this);
}

string AddOrder :: toString() const{
    string order = "order " + to_string(customerId);
    ActionStatus status = getStatus();
    // checks the status of the action and adds it to the string
    if (status == ActionStatus::COMPLETED ){
        order = order + " COMPLETED\n";
    }
    else{
        order = order + " ERROR\n";
    }
    return order;
}

AddOrder *AddOrder :: clone() const{
    return new AddOrder(*this);
}

AddCustomer::AddCustomer(const string &customerName, const string &customerType, int distance, int maxOrders):
BaseAction(),customerName(customerName),customerType(changeCustomerType(customerType)), distance(distance), maxOrders(maxOrders){}

// this function gets a string of customer type and returns the type of the customer
CustomerType AddCustomer:: changeCustomerType(const string &customerType){
    if(customerType == "soldier")
        return CustomerType::Soldier;
    else
        return CustomerType::Civilian;
}


void AddCustomer::act(WareHouse &wareHouse) {
    // checks the type of the customer, builds a new customer and adds it to the warehouse.
    if(customerType == CustomerType::Civilian){
        Customer *customer = new CivilianCustomer(wareHouse.getCustomerCounter(), customerName, distance, maxOrders);
        wareHouse.addCustomer(customer);
    }
    else{
        Customer *customer = new SoldierCustomer(wareHouse.getCustomerCounter(), customerName, distance, maxOrders);
        wareHouse.addCustomer(customer);
    }
    wareHouse.increaseCustomerCounter();
    complete();
    wareHouse.addAction(this);
}

AddCustomer *AddCustomer::clone() const {
    return new AddCustomer(*this);
}

string AddCustomer:: toString() const{
    string customer = "customer " + customerName;
    // checks the type of the customer and adds it to the string
    if (customerType == CustomerType::Soldier ){
        customer = customer + " Solider ";
    }
    else{
        customer = customer + " Civilan ";
    }
    customer = customer + std::to_string(distance) + " " + std::to_string(maxOrders) + " COMPLETED\n";
    return customer;
}

PrintOrderStatus:: PrintOrderStatus(int id):
orderId(id){}

void PrintOrderStatus:: act(WareHouse &wareHouse){
    // checks if the order exists
    if(orderId >= 0 && orderId < wareHouse.getOrderCounter()){
        // prints the order status
        Order &order = wareHouse.getOrder(orderId);
        std:: cout << order.toString();
        complete();
    }
    else{
        error("Order doesn't exist");
    }
    wareHouse.addAction(this);

}

PrintOrderStatus *PrintOrderStatus:: clone() const{
    return new PrintOrderStatus(*this);
}

string PrintOrderStatus:: toString() const{
    string stringOrder = "orderStatus " + std::to_string(orderId);
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringOrder = stringOrder + " COMPLETED\n";
    }
    else{
        stringOrder = stringOrder + " ERROR\n";
    }
    return stringOrder;
}

PrintCustomerStatus:: PrintCustomerStatus(int customerId): customerId(customerId){}

void  PrintCustomerStatus :: act(WareHouse &wareHouse){
// checks if the customer exists
    if(customerId >= 0 && customerId < wareHouse.getCustomerCounter()){
        Customer &customer = wareHouse.getCustomer(customerId);
        string customerStatus = "CustomerID: " + std::to_string(customerId) + "\n";
        for(auto& order : customer.getOrdersIds()){
            customerStatus = customerStatus + "OrderID: " + std::to_string(order) + "\n";
            OrderStatus status = wareHouse.getOrder(order).getStatus();
            if (status == OrderStatus::PENDING ){
                customerStatus = customerStatus + "OrderStatus: Pending\n";
            }
            if (status == OrderStatus::COLLECTING ){
                customerStatus = customerStatus + "OrderStatus: Collecting\n";
            }
           if (status == OrderStatus::COMPLETED ){
                customerStatus = customerStatus + "OrderStatus: Completed\n";
            }
            if (status == OrderStatus::DELIVERING ){
                customerStatus = customerStatus + "OrderStatus: Delivering\n";
            }
        }
        int numOrdersLeft = customer.getMaxOrders() - customer.getNumOrders();
        customerStatus = customerStatus + "NumOrdersLeft: " + std::to_string(numOrdersLeft) + "\n";
        std::cout << customerStatus;
        complete();
    }
    else{
        
            error("Costumer doesn't exist");
    }
    wareHouse.addAction(this);
   
}


PrintCustomerStatus *PrintCustomerStatus::clone() const{    
    return new PrintCustomerStatus(*this);
}

string PrintCustomerStatus:: toString() const{
    string stringCustomerStatus = "customerStatus " + std::to_string(customerId);
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringCustomerStatus = stringCustomerStatus + " COMPLETED\n";
    }
    else{
        stringCustomerStatus = stringCustomerStatus + " ERROR\n";
    }
    return stringCustomerStatus;
}

PrintVolunteerStatus::PrintVolunteerStatus(int id): volunteerId(id){}

void PrintVolunteerStatus:: act(WareHouse &wareHouse){
    // checks of the volunteer exists
    bool found = false;
    for (Volunteer *volunteer : wareHouse.getVolunteers()){
        if(volunteer->getId() == volunteerId){
            found = true;
        }
    }
    if(found){
        std::cout << wareHouse.getVolunteer(volunteerId).toString();
        complete();
    }
    else{
        error("Volunteer doesn't exist");
    }
    wareHouse.addAction(this);

}

    
PrintVolunteerStatus *PrintVolunteerStatus::clone() const{
    return new PrintVolunteerStatus(*this);
}

string PrintVolunteerStatus:: toString() const {
    string stringVolunteerStatus = "volunteerStatus " + std::to_string(volunteerId);
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringVolunteerStatus = stringVolunteerStatus + " COMPLETED\n";
    }
    else{
        stringVolunteerStatus = stringVolunteerStatus + " ERROR\n";
    }
    return stringVolunteerStatus;
}

PrintActionsLog::PrintActionsLog(){}

void PrintActionsLog::act(WareHouse &wareHouse){
    for(auto& action : wareHouse.getActions()){
        std::cout << action->toString();
    }
    complete();
    wareHouse.addAction(this);
}

PrintActionsLog *PrintActionsLog::clone() const{
    return new PrintActionsLog(*this);
}

string PrintActionsLog:: toString() const{
    string stringLogStatus = "log ";
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringLogStatus = stringLogStatus + "COMPLETED\n";
    }
    else{
        stringLogStatus = stringLogStatus + "ERROR\n";
    }
    return stringLogStatus;
}

Close:: Close(){}

void Close:: act(WareHouse &wareHouse){
    string orderStatus = "";
    for(int orderId =0; orderId < wareHouse.getOrderCounter(); orderId++){
         orderStatus = orderStatus + "OrderID: " + std::to_string(orderId) +", ";
        int customerId = wareHouse.getOrder(orderId).getCustomerId();
        orderStatus = orderStatus + "CustomerID: " + std::to_string(customerId) + ", ";
        OrderStatus status = wareHouse.getOrder(orderId).getStatus();
        if (status == OrderStatus::PENDING ){
            orderStatus = orderStatus + "OrderStatus: Pending\n";
        }
        if (status == OrderStatus::COLLECTING ){
            orderStatus = orderStatus + "OrderStatus: Collecting\n";
        }
        if (status == OrderStatus::COMPLETED ){
            orderStatus = orderStatus + "OrderStatus: Completed\n";
        }
        if (status == OrderStatus::DELIVERING ){
            orderStatus = orderStatus + "OrderStatus: Delivering\n";
        }    
    }
    cout << orderStatus;
    complete(); 
    wareHouse.addAction(this);
    wareHouse.close();
    

}

Close *Close:: clone() const{
    return new Close(*this);
}

string Close:: toString() const {
    string stringCloseStatus = "Close ";
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringCloseStatus = stringCloseStatus + "COMPLETED\n";
    }
    else{
        stringCloseStatus = stringCloseStatus + "ERROR\n";
    }
    return stringCloseStatus;
}

BackupWareHouse::BackupWareHouse(){}

void BackupWareHouse:: act(WareHouse &wareHouse){
    extern WareHouse* backup;
    // if the warehouse was allready backedup, delete the previous backup and overwrite it
    if (backup != nullptr){
        delete backup;
    }
    complete();
    wareHouse.addAction(this);
    backup = new WareHouse(wareHouse);
}

BackupWareHouse *BackupWareHouse::clone() const{
    return new BackupWareHouse(*this);
}

string BackupWareHouse :: toString() const{
    return "backup COMPLETED\n";
}

RestoreWareHouse::RestoreWareHouse(){}

void RestoreWareHouse:: act(WareHouse &wareHouse){
    extern WareHouse* backup;
    // check if the warehouse was backedup
    if (backup == nullptr){
        error("No backup available");
    }

    else{
        // deletes the current warehouse and restore it with the backup
        wareHouse = *backup; 
        complete();
    }
    wareHouse.addAction(this);


}

RestoreWareHouse *RestoreWareHouse :: clone() const{
    return new RestoreWareHouse(*this);
}

string RestoreWareHouse ::toString() const{
    string stringRestoreStatus = "restore ";
    ActionStatus status = getStatus();
    if (status == ActionStatus::COMPLETED ){
        stringRestoreStatus = stringRestoreStatus + "COMPLETED\n";
    }
    else{
        stringRestoreStatus = stringRestoreStatus + "ERROR\n";
    }
    return stringRestoreStatus;
}
