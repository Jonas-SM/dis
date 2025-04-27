CREATE TABLE EstateAgent (
    id serial PRIMARY KEY,
    name TEXT,
    address TEXT,
    login TEXT unique,
    password TEXT
);

CREATE TABLE estate (
    id serial PRIMARY KEY,
    City TEXT,
    PostalCode INTEGER,
    Street TEXT,
    StreetNumber INTEGER,
    SquareArea REAL,
    EstateAgentId INT REFERENCES EstateAgent(id)
);

CREATE TABLE Apartment (
    id serial PRIMARY KEY,
    Floor INTEGER,
    Rent INTEGER,
    Rooms INTEGER,
    Balcony BOOLEAN,
    EstateId int REFERENCES estate(id)
); -- INHERITS (estate);

CREATE TABLE House (
    id serial PRIMARY KEY,
    Floors INTEGER,
    Price REAL,
    Garden BOOLEAN,
    EstateId int REFERENCES estate(id)
); -- INHERITS (estate);

CREATE TABLE Person (
    id serial PRIMARY KEY,
    FirstName TEXT,
    LastName TEXT,
    HomeAddress TEXT
);

CREATE TABLE Contract (
    ContractNumber serial PRIMARY KEY,
    CreationDate DATE,
    Place TEXT
);

CREATE TABLE TenancyContract (
    id serial PRIMARY KEY,
    StartDate DATE,
    Duration INTEGER,
    AdditionalCosts REAL,
    ContractNumber int REFERENCES Contract(ContractNumber)
); -- INHERITS (Contract);

CREATE TABLE PurchaseContract (
    id serial PRIMARY KEY,
    InstallmentsNumber INTEGER,
    InterestRate REAL,
    ContractNumber int REFERENCES Contract(ContractNumber)
); --INHERITS (Contract);

-- Relationships
-- irgendwas mit unique hinzufügen (siehe AB)
CREATE TABLE rents (
    rentId serial PRIMARY KEY,
    ApartmentId int REFERENCES Apartment(id),
    TenancyContractId int REFERENCES TenancyContract(id),
    PersonId int REFERENCES Person(id)
);

CREATE TABLE sells (
    sellId serial PRIMARY KEY,
    HouseId int REFERENCES House(id),
    PersonId int REFERENCES Person(id),
    PurchaseContractId int REFERENCES PurchaseContract(id)
);

-- Insert sample values

INSERT INTO estate (City, PostalCode, Street, StreetNumber, SquareArea)
    VALUES ('Hamburg', 22081, 'neuer wall', 11, 124.5);

INSERT INTO Apartment (Floor, Rent, Rooms, Balcony, EstateId) 
    VALUES (100, 100000, 50, True, 1);

INSERT INTO House (Floors, Price, Garden, EstateId)
    VALUES (20, 10, True, 1);

INSERT INTO Person (FirstName, LastName, HomeAddress)
    VALUES ('Mark', 'Muster', 'Am Burchardkai 11');

INSERT INTO Contract (CreationDate, Place)
    VALUES ('2025-05-01', 'i dont know');

INSERT INTO TenancyContract (StartDate, Duration, AdditionalCosts, ContractNumber)
    VALUES ('2025-05-01', 15, 100, 1);

INSERT INTO PurchaseContract (InstallmentsNumber, InterestRate, ContractNumber)
    VALUES (10, 100, 1);

INSERT INTO EstateAgent (Name, address, login, password, id)
    VALUES ('Peter Pustekuchen', 'am aal 123', 'peter-p', 'password', 1);

