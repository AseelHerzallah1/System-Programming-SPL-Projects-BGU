#include "Volunteer.h"

Volunteer :: Volunteer(int id, const string &name) :
 completedOrderId(NO_ORDER),activeOrderId(NO_ORDER), type(volunteerType::NoType), id(id), name(name){}

int Volunteer :: getId() const{
    return id;
}

const string &Volunteer ::getName() const{
    return name;
}

int Volunteer :: getActiveOrderId() const{
    return activeOrderId;
}

int Volunteer :: getCompletedOrderId() const{
    return completedOrderId;
}

bool Volunteer :: isBusy() const{
    return activeOrderId != NO_ORDER;
} 

void Volunteer :: setType(volunteerType volType){
    type = volType;
}

volunteerType Volunteer:: getType() const{
    return type;
}

CollectorVolunteer :: CollectorVolunteer(int id, const string &name, int coolDown):
 Volunteer(id,name),coolDown(coolDown),timeLeft(coolDown){
    setType(volunteerType::CollectorVolunteer);
 }

 CollectorVolunteer *CollectorVolunteer :: clone() const{
    return new CollectorVolunteer(*this);
}

void CollectorVolunteer :: step() {
    if(isBusy()){ //ensures that the steps inside the if block are only executed when the volunteer is actively working on an order.
       if(decreaseCoolDown()){
        completedOrderId = activeOrderId; //This signifies that the order with ID activeOrderId has been successfully processed and is now completed.
        // activeOrderId.setDriverPending(true);
        activeOrderId = NO_ORDER;
        timeLeft = 0; //reset to zero, as the volunteer is not processing any order at this moment.
       } 
    }
}

int CollectorVolunteer :: getCoolDown() const{
    return coolDown;
}

int CollectorVolunteer :: getTimeLeft() const{
    return timeLeft;
}

bool CollectorVolunteer :: decreaseCoolDown(){//Decrease timeLeft by 1,return true if timeLeft=0,false otherwise
     timeLeft--;
    if (timeLeft == 0 || timeLeft < 0)
        return true;
    else
        return false;
}

bool CollectorVolunteer :: hasOrdersLeft() const {
    return true;
}

bool CollectorVolunteer :: canTakeOrder(const Order &order) const {
    return !isBusy();
}

void CollectorVolunteer :: acceptOrder(const Order &order) {
    if(!isBusy()){
        activeOrderId = order.getId();
        timeLeft = coolDown;
    }
}

string CollectorVolunteer::toString() const {
    string s = "VolunteerID: " + std::to_string(this->getId()) + "\nisBusy: ";
    if (this->isBusy()) {
        s = s + "True\nOrderId: " + std::to_string(this->getActiveOrderId()) + "\nTimeLeft: " + std::to_string(this->getTimeLeft()) + "\n";
    }
    else {
        s = s + "False\nOrderId: None\nTimeLeft: None\n";
    }
    s = s + "OrdersLeft: No limit \n";
    return s;
}


LimitedCollectorVolunteer :: LimitedCollectorVolunteer(int id, const string &name, int coolDown ,int maxOrders)
:CollectorVolunteer(id,name,coolDown),maxOrders(maxOrders), ordersLeft(maxOrders) {
    setType(volunteerType::LimitedCollectorVolunteer);
}

LimitedCollectorVolunteer *LimitedCollectorVolunteer :: clone() const{
    return new LimitedCollectorVolunteer(*this);
}

bool LimitedCollectorVolunteer :: hasOrdersLeft() const{
    return ordersLeft > 0;
}

bool LimitedCollectorVolunteer :: canTakeOrder(const Order &order) const{
    return !isBusy()&& hasOrdersLeft();
}

void LimitedCollectorVolunteer :: acceptOrder(const Order &order){
    if(canTakeOrder(order)){
        CollectorVolunteer :: acceptOrder(order);
        ordersLeft--;
    }
}

int  LimitedCollectorVolunteer :: getMaxOrders() const{
    return maxOrders;
}

int LimitedCollectorVolunteer :: getNumOrdersLeft() const{
    return ordersLeft;
}

string LimitedCollectorVolunteer::toString() const {
    string s = "VolunteerID: " + std::to_string(this->getId()) + "\nisBusy: ";
    if (this->isBusy()) { 
        s = s + "True\nOrderId: " + std::to_string(this->getActiveOrderId()) + "\nTimeLeft: " + std::to_string(this->getTimeLeft()) + "\n";
    }
    else { 
        s = s + "False\nOrderId: None\nTimeLeft: None\n";
    }
    s = s + "OrdersLeft: " + std::to_string(this->getNumOrdersLeft()) + "\n";
    return s;
}


DriverVolunteer :: DriverVolunteer(int id, const string &name, int maxDistance, int distancePerStep)
 : Volunteer(id,name), maxDistance(maxDistance),distancePerStep(distancePerStep),distanceLeft(0){
    setType(volunteerType::DriverVolunteer);
 }

DriverVolunteer *DriverVolunteer :: clone() const{
    return new DriverVolunteer(*this);
}

int DriverVolunteer :: getDistanceLeft() const{
    return distanceLeft;
}

int DriverVolunteer :: getMaxDistance() const{
    return maxDistance;
}

int DriverVolunteer :: getDistancePerStep() const{
    return distancePerStep;
}  

bool DriverVolunteer :: decreaseDistanceLeft(){//Decrease distanceLeft by distancePerStep,return true if distanceLeft<=0,false otherwise
    distanceLeft -= distancePerStep;
    if(distanceLeft > 0){
        return false;
    }
    return true;
}
bool DriverVolunteer :: hasOrdersLeft() const{
    return true;
}

bool DriverVolunteer ::  canTakeOrder(const Order &order) const{ // Signal if the volunteer is not busy and the order is within the maxDistance
    return !isBusy() && order.getDistance() <= maxDistance;
}

void DriverVolunteer :: acceptOrder(const Order &order){// Assign distanceLeft to order's distance
    if(canTakeOrder(order)){
        distanceLeft = order.getDistance();
        activeOrderId = order.getId();
    }
} 

void DriverVolunteer:: step(){ // Decrease distanceLeft by distancePerStep
    if(isBusy()){
        if(decreaseDistanceLeft()){
            completedOrderId = activeOrderId;
            activeOrderId = NO_ORDER;
            distanceLeft = 0;
        }
    }
} 


string DriverVolunteer::toString() const {
    string s = "VolunteerID: " + std::to_string(this->getId()) + "\nisBusy: ";
    if (this->isBusy()) {
        s = s + "True\nOrderId: " + std::to_string(this->getActiveOrderId()) + "\nTimeLeft: " + std::to_string(this->getDistanceLeft()) + "\n";
    }
    else {
        s = s + "False\nOrderId: None\nTimeLeft: None\n";
    }
    s = s + "OrdersLeft: No limit\n";
    return s;

}

void DriverVolunteer::setDistanceLeft(int distance){
    distanceLeft = distance;
}

LimitedDriverVolunteer:: LimitedDriverVolunteer(int id, const string &name, int maxDistance, int distancePerStep,int maxOrders) 
:DriverVolunteer(id,name,maxDistance,distancePerStep),maxOrders(maxOrders),ordersLeft(maxOrders){
    setType(volunteerType::LimitedDriverVolunteer);
}

LimitedDriverVolunteer *LimitedDriverVolunteer :: clone() const {
    return new LimitedDriverVolunteer(*this);
}

int LimitedDriverVolunteer :: getMaxOrders() const{
    return maxOrders;
}
int LimitedDriverVolunteer ::  getNumOrdersLeft() const{
    return ordersLeft;
}

bool LimitedDriverVolunteer ::  hasOrdersLeft() const{
    return ordersLeft > 0;
}

bool LimitedDriverVolunteer ::  canTakeOrder(const Order &order) const{  // Signal if the volunteer is not busy, the order is within the maxDistance.
    return !isBusy() && this->hasOrdersLeft() && order.getDistance() <= this->getMaxDistance() ;
}

void LimitedDriverVolunteer :: acceptOrder(const Order &order){ // Assign distanceLeft to order's distance and decrease ordersLeft
    if(canTakeOrder(order)){
        setDistanceLeft(order.getDistance());
        activeOrderId = order.getId();
        ordersLeft--;
    }
}


string LimitedDriverVolunteer::toString() const {
    string s = "VolunteerID: " + std::to_string(this->getId()) + "\nisBusy: ";
    if (this->isBusy()) {
        s = s + "True\nOrderId: " + std::to_string(this->getActiveOrderId()) + "\nTimeLeft: " + std::to_string(this->getDistanceLeft()) + "\n";
    }
    else {
        s = s + "False\nOrderId: None\nTimeLeft: None\n";
    }
    s = s + "OrdersLeft: " + std :: to_string(this -> getNumOrdersLeft()) + "\n";
    return s;

}