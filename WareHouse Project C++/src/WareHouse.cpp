#include "WareHouse.h"

#include "Volunteer.h"
#include "Action.h"
#include "Order.h"
#include "Customer.h"

#include <iostream>
#include <sstream>
#include <fstream>
#include <string>


using namespace std;


WareHouse::WareHouse(const string &configFilePath):isOpen(true), actionsLog(),volunteers(), pendingOrders(),inProcessOrders(),completedOrders(),customers(),
customerCounter(0), volunteerCounter(0), orderCounter(0){
    fstream file;
    file.open(configFilePath);
    if(file.is_open()){
        string line;
        while(getline(file, line)){
            stringstream ss(line);
            string word;
            string sName;
            string sType;
            string sCoolDown;
            string sMaxOrders;
            string sDistance;
            string sMaxDistance;
            string sDistancePerStep;
            while(ss >> word){
                if (word == "#"){
                    break;
                }
                // builds the volunteers
                if (word == "volunteer"){
                    ss >> sName;
                    ss >> sType;
                    if (sType == "collector"){
                        ss >> sCoolDown;
                        Volunteer *collector = new CollectorVolunteer(volunteerCounter,sName, stoi(sCoolDown, 0, 10));
                        volunteers.push_back(collector);
                    }
                    if (sType == "limited_collector"){
                        ss >> sCoolDown;
                        ss >> sMaxOrders;
                        Volunteer *limitedCollector = new LimitedCollectorVolunteer(volunteerCounter, sName, stoi(sCoolDown, 0 ,10), stoi(sMaxOrders,0,10));
                        volunteers.push_back(limitedCollector);
                    }
                        
                    if (sType == "driver"){
                        ss >> sMaxDistance;
                        ss >> sDistancePerStep;
                        Volunteer *driver = new DriverVolunteer(volunteerCounter, sName, stoi(sMaxDistance, 0 ,10), stoi(sDistancePerStep,0,10));
                        volunteers.push_back(driver);
                    }
                    if (sType == "limited_driver"){
                        ss >> sMaxDistance;
                        ss >> sDistancePerStep;
                        ss >> sMaxOrders;
                        Volunteer *limitedDriver = new LimitedDriverVolunteer(volunteerCounter, sName, stoi(sMaxDistance, 0 ,10), stoi(sDistancePerStep,0,10), stoi(sMaxOrders,0,10));
                        volunteers.push_back(limitedDriver);
                    }
                    volunteerCounter++;
                }
                //builds the customers
                else if(word == "customer"){
                    ss >> sName;
                    ss >> sType;
                    ss >> sDistance;
                    ss >> sMaxOrders;
                    if (sType == "soldier"){
                        Customer *soldier = new SoldierCustomer(customerCounter, sName, stoi(sDistance, 0, 10), stoi(sMaxOrders, 0, 10));
                        customers.push_back(soldier);
                    }
                    else if(sType == "civilian"){
                        Customer *civilian = new CivilianCustomer(customerCounter, sName, stoi(sDistance, 0, 10), stoi(sMaxOrders, 0, 10));
                        customers.push_back(civilian);
                    }
                    customerCounter++;
                }

            }
        }
    }
    file.open(configFilePath);

    
}

 //copy constructor
WareHouse::WareHouse(const WareHouse &other):
isOpen(other.isOpen),actionsLog(),volunteers(), pendingOrders(),inProcessOrders(),completedOrders(),customers(), customerCounter(other.getCustomerCounter()), volunteerCounter(other.getVolunteerCounter()), orderCounter(other.getOrderCounter()) {
    for(int i=0; i < (int)other.actionsLog.size(); i++){
        actionsLog.push_back(other.actionsLog.at(i)->clone());
    }
    for(int i=0; i < (int)other.volunteers.size(); i++){
        volunteers.push_back(other.volunteers.at(i)->clone());
    }
    for(int i=0; i < (int)other.pendingOrders.size(); i++){
        pendingOrders.push_back(new Order(*other.pendingOrders.at(i)));
    }
    for(int i=0; i < (int)other.inProcessOrders.size(); i++){
        inProcessOrders.push_back(new Order(*other.inProcessOrders.at(i)));
    }
    for(int i=0; i < (int)other.completedOrders.size(); i++){
        completedOrders.push_back(new Order(*other.completedOrders.at(i)));
    }
    for(int i=0; i < (int)other.customers.size(); i++){
        customers.push_back(other.customers.at(i)->clone());
    }
}

WareHouse& WareHouse:: operator=(const WareHouse& other){
    if(&other != this){
        isOpen = other.isOpen;
        customerCounter = other.customerCounter;
        volunteerCounter = other.volunteerCounter;
        orderCounter = other.orderCounter;

        for(int i =0; i < (int)actionsLog.size();i++){
            if(actionsLog.at(i) != nullptr){
                delete actionsLog.at(i);
            }
        }
        actionsLog.clear();
        for(int i =0; i < (int)other.actionsLog.size();i++){
            actionsLog.push_back(other.actionsLog.at(i)->clone());
        }

        for(int i =0; i < (int)volunteers.size();i++){
            if(volunteers.at(i) != nullptr){
                delete volunteers.at(i);
            }
        }
        volunteers.clear();
        for(int i =0; i < (int)other.volunteers.size();i++){
            volunteers.push_back(other.volunteers.at(i)->clone());
        }

        for(int i =0; i < (int)pendingOrders.size();i++){
            if(pendingOrders.at(i) != nullptr){
                delete pendingOrders.at(i);
            }
        }
        pendingOrders.clear();
        for(int i =0; i < (int)other.pendingOrders.size();i++){
            pendingOrders.push_back(new Order(*other.pendingOrders.at(i)));
        }

        for(int i =0; i < (int)inProcessOrders.size();i++){
            if(inProcessOrders.at(i) != nullptr){
                delete inProcessOrders.at(i);
            }
        }
        inProcessOrders.clear();
        for(int i =0; i < (int)other.inProcessOrders.size();i++){
            inProcessOrders.push_back(new Order(*other.inProcessOrders.at(i)));
        }

        for(int i =0; i < (int)completedOrders.size();i++){
            if(completedOrders.at(i) != nullptr){
                delete completedOrders.at(i);
            }
        }
        completedOrders.clear();
        for(int i =0; i < (int)other.completedOrders.size();i++){
            completedOrders.push_back(new Order(*other.completedOrders.at(i)));
        }

        for(int i =0; i < (int)customers.size();i++){
            if(customers.at(i) != nullptr){
                delete customers.at(i);
            }
        }
        customers.clear();
        for(int i =0; i < (int)other.customers.size();i++){
            customers.push_back(other.customers.at(i)->clone());
        }
    }
    return *this;
}

//destructor
 WareHouse::~WareHouse(){
    for(int i =0; i < (int)actionsLog.size();i++){
        delete actionsLog.at(i);
        actionsLog.at(i) = nullptr;
    }
    actionsLog.clear();
    for(int i =0; i < (int)volunteers.size();i++){
        delete volunteers.at(i);
        volunteers.at(i) = nullptr;
    }
    volunteers.clear();
    for(int i =0; i < (int)pendingOrders.size();i++){
        delete pendingOrders.at(i);
        pendingOrders.at(i) = nullptr;
    }    
    pendingOrders.clear();
    for(int i =0; i < (int)inProcessOrders.size();i++){
        delete inProcessOrders.at(i);
        inProcessOrders.at(i) = nullptr;
    }    
    inProcessOrders.clear();
    for(int i =0; i < (int)completedOrders.size();i++){
        delete completedOrders.at(i);
        completedOrders.at(i) = nullptr;
    }
    completedOrders.clear();
    for(int i =0; i < (int)customers.size();i++){
        delete customers.at(i);
        customers.at(i) = nullptr;
    }
    customers.clear();
 }

// move constructor
 WareHouse::WareHouse(WareHouse&& other) noexcept:
  isOpen(other.isOpen), actionsLog(),volunteers(), pendingOrders(),inProcessOrders(),completedOrders(),customers(), customerCounter(other.customerCounter), volunteerCounter(other.volunteerCounter), orderCounter(other.orderCounter)  {
    for(int i =0; i < (int)other.actionsLog.size();i++){
        actionsLog.push_back(other.actionsLog.at(i));
        other.actionsLog.at(i) = nullptr;
    }

    for(int i =0; i < (int)other.volunteers.size();i++){
        volunteers.push_back(other.volunteers.at(i));
        other.volunteers.at(i) = nullptr;
    }

    for(int i =0; i < (int)other.pendingOrders.size();i++){
        pendingOrders.push_back(other.pendingOrders.at(i));
        other.pendingOrders.at(i) = nullptr;
    }

    for(int i =0; i < (int)other.inProcessOrders.size();i++){
        inProcessOrders.push_back(other.inProcessOrders.at(i));
        other.inProcessOrders.at(i) = nullptr;
    }

    for(int i =0; i < (int)other.completedOrders.size();i++){
        completedOrders.push_back(other.completedOrders.at(i));
        other.completedOrders.at(i) = nullptr;
    }

    for(int i =0; i < (int)other.customers.size();i++){
        customers.push_back(other.customers.at(i));
        other.customers.at(i) = nullptr;
    }
 }
 // move assignment operator
WareHouse& WareHouse:: operator=(WareHouse&& other) noexcept{
    if(&other != this){
        isOpen = other.isOpen;
        customerCounter = other.customerCounter;
        volunteerCounter = other.volunteerCounter;
        orderCounter = other.orderCounter;

        for(int i =0; i < (int)actionsLog.size();i++){
            if(actionsLog.at(i) != nullptr){
                delete actionsLog.at(i);
            }
        }
        actionsLog.clear();
        for(int i =0; i < (int)other.actionsLog.size();i++){
            actionsLog.push_back(other.actionsLog.at(i));
            other.actionsLog.at(i) = nullptr;
        }

        for(int i =0; i < (int)volunteers.size();i++){
            if(volunteers.at(i) != nullptr){
                delete volunteers.at(i);
            }
        }
        volunteers.clear();
        for(int i =0; i < (int)other.volunteers.size();i++){
            volunteers.push_back(other.volunteers.at(i));
            other.volunteers.at(i) = nullptr;
        }

        for(int i =0; i < (int)pendingOrders.size();i++){
            if(pendingOrders.at(i) != nullptr){
                delete pendingOrders.at(i);
            }
        }
        pendingOrders.clear();
        for(int i =0; i < (int)other.pendingOrders.size();i++){
            pendingOrders.push_back(other.pendingOrders.at(i));
            other.pendingOrders.at(i) = nullptr;
        }

        for(int i =0; i < (int)inProcessOrders.size();i++){
            if(inProcessOrders.at(i) != nullptr){
                delete inProcessOrders.at(i);
            }
        }
        inProcessOrders.clear();
        for(int i =0; i < (int)other.inProcessOrders.size();i++){
            inProcessOrders.push_back(other.inProcessOrders.at(i));
            other.inProcessOrders.at(i) = nullptr;
        }

        for(int i =0; i < (int)completedOrders.size();i++){
            if(completedOrders.at(i) != nullptr){
                delete completedOrders.at(i);
            }
        }
        completedOrders.clear();
        for(int i =0; i < (int)other.completedOrders.size();i++){
            completedOrders.push_back(other.completedOrders.at(i));
            other.completedOrders.at(i) = nullptr;
        }

        for(int i =0; i < (int)customers.size();i++){
            if(customers.at(i) != nullptr){
                delete customers.at(i);
            }
        }
        customers.clear();
        for(int i =0; i < (int)other.customers.size();i++){
            customers.push_back(other.customers.at(i));
            other.customers.at(i) = nullptr;
        }
    }
    return *this;
}
void WareHouse :: start(){
    std:: cout << "Warehouse is open!\n";
    while(isOpen){
        //gets input from user 
        string input;
        getline(std::cin, input);
        std::stringstream ssInput(input); 
        string action;
        ssInput >> action; // intialize "action" with the first word of the input 
        // simulate step
        if (action == "step"){
            string sNumOfSteps;
            ssInput >> sNumOfSteps;
            int numOfSteps = stoi(sNumOfSteps, 0, 10);
            BaseAction *step = new SimulateStep(numOfSteps);
            step->act(*this);
        }

        if (action == "order"){
            string sCustomerId;
            ssInput >> sCustomerId;
            int customerId =  stoi(sCustomerId, 0, 10);
            BaseAction *order = new AddOrder(customerId);
            order->act(*this);
        }
        if(action == "customer"){
            string sName;
            string sType;
            string sDistance;
            string sMaxOrders;
            ssInput >> sName;
            ssInput >> sType;
            ssInput >> sDistance;
            ssInput >> sMaxOrders;
            const string& name = sName;
            const string& type = sType;
            int distance = stoi(sDistance, 0, 10);
            int maxOrders = stoi(sMaxOrders, 0 ,10);
            BaseAction *customer = new AddCustomer(name, type, distance, maxOrders);
            customer->act(*this);
        }

        if(action == "orderStatus"){
            string sOrderId;
            ssInput >> sOrderId;
            int orderId = stoi(sOrderId,0,10);
            BaseAction *orderStatus = new PrintOrderStatus(orderId);
            orderStatus->act(*this);
        }

        if(action == "customerStatus"){
            string sCustomerId;
            ssInput >> sCustomerId;
            int customerId = stoi(sCustomerId,0,10);
            BaseAction *customerStatus = new PrintCustomerStatus(customerId);
            customerStatus->act(*this);
        }

        if(action == "volunteerStatus"){
            string sVolunteerId;
            ssInput >>sVolunteerId;
            int volunteerId = stoi(sVolunteerId,0,10);
            BaseAction *volunteerStatus = new PrintVolunteerStatus(volunteerId);
            volunteerStatus->act(*this);
        }

        if(action == "log"){
            BaseAction *log = new PrintActionsLog();
            log->act(*this);
        }
        
        if(action == "close"){
            BaseAction *close = new Close();
            close->act(*this);
        }

        if(action == "backup"){
            BaseAction *backup = new BackupWareHouse();
            backup->act(*this);
        }

        if(action == "restore"){
            BaseAction *restore = new RestoreWareHouse();
            restore->act(*this);
        }


    }
}

void WareHouse :: addOrder(Order* order){
    pendingOrders.push_back(order);
}

void WareHouse :: addAction(BaseAction* action){
    actionsLog.push_back(action);
}

void WareHouse::addCustomer(Customer* customer){
    customers.push_back(customer);
}

 Customer& WareHouse:: getCustomer(int customerId) const{
    for(const auto& customer : customers){
        if(customer->getId() == customerId)
            return *customer;
    }
    throw std::invalid_argument("Volunteer not found!\n");
    
 }

Volunteer& WareHouse :: getVolunteer(int volunteerId) const{
    for (const auto& volunteer : volunteers) {
        if(volunteer->getId() == volunteerId) {
            return *volunteer;
        }
    }
    throw std::invalid_argument("Volunteer not found!\n");
}

Order& WareHouse :: getOrder(int orderId) const{

    for(const auto& order : pendingOrders){
        if(order->getId() == orderId){
            return *order;
        }
    }
    for(const auto& order : inProcessOrders){
        if(order->getId() == orderId){
            return *order;
        }
    }

    for(const auto& order : completedOrders){
        if(order->getId() == orderId){
            return *order;

        }
    }

    throw std::invalid_argument("Volunteer not found!\n");
 }

const vector<BaseAction*>& WareHouse :: getActions() const{ // gets the list of actions in the action log
    return actionsLog;
}

 void WareHouse :: close(){
    isOpen = false;
 }

 void WareHouse :: open(){
    isOpen = true;
 }

 vector<Order*> WareHouse:: getPendingOrders() const{
    return pendingOrders;
 }

vector<Order*> WareHouse:: getInProccessOrders() const{
    return inProcessOrders;
}

vector<Order*> WareHouse:: getCompletedOrders() const{
    return completedOrders;
}

vector<Volunteer*> WareHouse:: getVolunteers() const{
    return volunteers;
}

vector<Customer*> WareHouse:: getCustomers() const{
    return customers;
}

int WareHouse::getOrderCounter() const{
    return orderCounter;
}
void WareHouse::increaseOrderCounter() {
    orderCounter++;
}
int WareHouse::getCustomerCounter() const{
    return customerCounter;
}
int WareHouse::getVolunteerCounter() const{
    return volunteerCounter;
}
void WareHouse::increaseCustomerCounter() {
    customerCounter++;
}
void WareHouse::increaseVolunteerCounter() {
    volunteerCounter++;
}

void WareHouse::pendingOrdersStep(){
        int j = 0;
        while(j < (int)pendingOrders.size()){
            // Hand over the order for the next operation based on its status
            Order *order = pendingOrders.at(j);
            
            if(order->getStatus() == OrderStatus::PENDING){ //checks if the order is waiting for a collector
                for( Volunteer* volunteer : volunteers){
                    if(volunteer->getType() == volunteerType::CollectorVolunteer || volunteer->getType() == volunteerType::LimitedCollectorVolunteer ){ //check if the current volunteer is a collector
                        if(volunteer->canTakeOrder(*order)){
                            volunteer->acceptOrder(*order);
                            order->setStatus(OrderStatus::COLLECTING);
                            order->setCollectorId(volunteer->getId());
                            inProcessOrders.push_back(order);
                            pendingOrders.erase(pendingOrders.begin() + j);//delete
                            j--;
                            break; // once the order is assigned we break the loop.
                        }
                    }
                }
            }
            else{ // the order is waiting for a driver
                for( Volunteer* volunteer : volunteers){
                    if(volunteer->getType() == volunteerType::DriverVolunteer|| volunteer->getType() == volunteerType::LimitedDriverVolunteer){ //check if the current volunteer is a driver 
                        if(volunteer->canTakeOrder(*order)){
                            volunteer->acceptOrder(*order);
                            order->setStatus(OrderStatus::DELIVERING);
                            order->setDriverId(volunteer->getId());
                            inProcessOrders.push_back(order);
                            pendingOrders.erase( pendingOrders.begin() + j);//delete
                            j--;
                            break; // once the order is assigned we break the loop.
                        }
                    }
                }
            }
            
            j++;
        }

}

void WareHouse::volunteersStep(){
        for(int j = 0; j < (int)volunteers.size(); j++){
            Volunteer *volunteer = volunteers.at(j);
            int activeOrderId = volunteer->getActiveOrderId();
            volunteer->step();
            // check if the volunteer finished prccessing the order
            if(activeOrderId != NO_ORDER && volunteer->getCompletedOrderId() == activeOrderId){ 
                int orderId = volunteer->getCompletedOrderId();
                Order *currentOrder = &getOrder(orderId); 
                // get the index of the order in the inProccessOrders vector
                bool found = false;
                int index = 0;
                for(index = 0; !found && index < (int)inProcessOrders.size(); index++){
                    if(inProcessOrders.at(index)->getId() ==orderId){
                        found =true;
                    }
                }
                index--;
                inProcessOrders.erase( inProcessOrders.begin() + index);

                // check if the volunteer is a driver. if so- the order is completed
                bool isDriver = volunteer->getType() == volunteerType::DriverVolunteer ||  volunteer->getType() == volunteerType::LimitedDriverVolunteer;

                if(isDriver){
                    currentOrder->setStatus(OrderStatus::COMPLETED);
                    completedOrders.push_back(currentOrder);
                }
                else{ // if the volunteer is a collector, the order is moved to the pending order and waiting for a driver
                    pendingOrders.push_back(currentOrder);
                }
            }
            // step 4: check if the volunteer has reached it's limit, if so - delete it
            if(volunteer->getType() == volunteerType::LimitedCollectorVolunteer || volunteer->getType() == volunteerType::LimitedDriverVolunteer) {
                if(!volunteer->hasOrdersLeft()){
                    delete volunteers.at(j); // delete the limited volunteer from the heap 
                    volunteers.erase(volunteers.begin() + j); // erase it from the volunteers vector 
                    j--;

                }
            }
        }
}


