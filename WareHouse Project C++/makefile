# All Targets

all: bin/warehouse


# Tool invocations

# Executable "warehouse" depends on the files main.o bin/WareHouse.o bin/Volunteer.o bin/Customer.o bin/Order.o bin/Action.o


bin/warehouse: clean bin/main.o bin/WareHouse.o bin/Volunteer.o bin/Customer.o bin/Order.o bin/Action.o

	@echo 'Building target: warehouse'

	@echo 'Invoking: C++ Linker'

	g++ -o bin/warehouse bin/main.o bin/WareHouse.o bin/Volunteer.o bin/Customer.o bin/Order.o bin/Action.o

	@echo 'Finished building target: warehouse'


# Depends on the source and header files

bin/main.o: src/main.cpp

	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/main.o src/main.cpp

# Depends on the source and header files

bin/WareHouse.o: src/WareHouse.cpp

	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/WareHouse.o src/WareHouse.cpp

# Depends on the source and header files

bin/Volunteer.o: src/Volunteer.cpp

	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Volunteer.o src/Volunteer.cpp

# Depends on the source and header files

bin/Customer.o: src/Customer.cpp

	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Customer.o src/Customer.cpp

# Depends on the source and header files

bin/Order.o: src/Order.cpp

	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Order.o src/Order.cpp


# Depends on the source and header files

bin/Action.o: src/Action.cpp

	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Action.o src/Action.cpp

#Clean the build directory

clean:

	rm -f bin/*