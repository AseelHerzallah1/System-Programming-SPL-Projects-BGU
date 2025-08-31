#include "Order.h"

Order::Order(int id, int customerId, int distance):
 id(id) , customerId(customerId) , distance(distance),  status(OrderStatus::PENDING), collectorId(NO_VOLUNTEER),
  driverId(NO_VOLUNTEER){}

int Order::getId() const{
    return id;
}

int Order::getCustomerId() const{
    return customerId;
}

void Order::setStatus(OrderStatus status){
    this->status = status;
}

void Order::setCollectorId(int collectorId){
    this->collectorId = collectorId;
}

void Order::setDriverId(int driverId){
    this->driverId = driverId;
}

int Order::getCollectorId() const{
    return collectorId;
}

int Order::getDriverId() const{
    return driverId;
}

OrderStatus Order::getStatus() const{
    return status;
}

const string Order::toString() const{
    string order = "OrderId: " + std::to_string(id) +"\n" + "OrderStatus: ";
    OrderStatus status = getStatus();
    if (status == OrderStatus::PENDING ){
        order = order + "Pending\n";
        order = order + "CustomerID: " + std::to_string(getCustomerId()) + "\n";
        if(getCollectorId() != NO_VOLUNTEER){
            order = order + "Collector: " + std::to_string(getCollectorId()) + "\n";
        }
        else{
            order = order + "Collector: None\n";
        }
        if(getDriverId() != NO_VOLUNTEER){
            order = order + "Driver: " + std::to_string(getDriverId()) + "\n";
        }
        else{
            order = order + "Driver: None\n";
        }
    }
    if (status == OrderStatus::COLLECTING ){
        order = order + "Collecting\n";
        order = order + "CustomerID: " + std::to_string(getCustomerId()) + "\n";
        order = order + "Collector: " + std::to_string(getCollectorId()) + "\n";
        order = order + "Collector: None\n";
    }
    if (status == OrderStatus::DELIVERING ){
        order = order + "Delivering\n";
        order = order + "CustomerID: " + std::to_string(getCustomerId()) + "\n";
        order = order + "Collector: " + std::to_string(getCollectorId()) + "\n";
        order = order + "Driver: " + std::to_string(getDriverId()) + "\n";
    }
    if (status == OrderStatus::COMPLETED ){
        order = order + "Completed\n";
        order = order + "CustomerID: " + std::to_string(getCustomerId()) + "\n";
        order = order + "Collector: " + std::to_string(getCollectorId()) + "\n";
        order = order + "Driver: " + std::to_string(getDriverId()) + "\n";
    }
    return order;

}

int Order::getDistance() const{
    return distance;
}

